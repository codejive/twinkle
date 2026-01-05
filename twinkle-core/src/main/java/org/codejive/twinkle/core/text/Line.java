package org.codejive.twinkle.core.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.widget.Canvas;
import org.codejive.twinkle.core.widget.Renderable;
import org.codejive.twinkle.util.StyledIterator;

public class Line implements Renderable {
    private final List<Span> spans;

    public static Line of(String text) {
        return new Line((Span.of(text)));
    }

    public static Line of(String text, Style style) {
        return of(text, style.state());
    }

    public static Line of(String text, long styleState) {
        return new Line((Span.of(text, styleState)));
    }

    public static Line of(Span... spans) {
        return new Line(spans);
    }

    protected Line(Span... spans) {
        this.spans = new ArrayList<>(spans.length);
        Collections.addAll(this.spans, spans);
    }

    public static Line of(StyledIterator iter) {
        List<Span> spans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        long currentStyleState = -1;
        while (iter.hasNext()) {
            long cp = iter.next();
            if (cp == '\n') {
                break;
            }
            if (iter.styleState() != currentStyleState) {
                if (sb.length() > 0) {
                    spans.add(Span.of(sb.toString(), currentStyleState));
                    sb.setLength(0);
                }
                currentStyleState = iter.styleState();
            }
            sb.appendCodePoint((int) cp);
        }
        if (sb.length() > 0) {
            spans.add(Span.of(sb.toString(), currentStyleState));
        }
        return new Line(spans.toArray(new Span[0]));
    }

    @Override
    public void render(Canvas canvas) {
        int x = 0;
        for (Span span : spans) {
            span.render(canvas.view(x, 0, span.length(), 1));
            x += span.length();
        }
    }
}
