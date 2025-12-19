package org.codejive.twinkle.core.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.widget.Canvas;
import org.codejive.twinkle.core.widget.Renderable;

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

    @Override
    public void render(Canvas canvas) {
        int x = 0;
        for (Span span : spans) {
            span.render(canvas.view(x, 0, span.length(), 1));
            x += span.length();
        }
    }
}
