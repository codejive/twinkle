package org.codejive.twinkle.core.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.util.Printable;
import org.codejive.twinkle.util.StyledIterator;
import org.jspecify.annotations.NonNull;

public class Line implements Printable {
    private final List<Span> spans;

    public List<Span> spans() {
        return Collections.unmodifiableList(spans);
    }

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
        long currentStyleState = Style.F_UNKNOWN;
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Line{spans=[");
        for (Span span : spans) {
            sb.append(span.text()).append(", ");
        }
        sb.append("]}");
        return sb.toString();
    }

    @Override
    public @NonNull Appendable toAnsi(Appendable appendable, long currentStyleState)
            throws IOException {
        for (Span span : spans) {
            span.toAnsi(appendable, currentStyleState);
            currentStyleState = span.style().state();
        }
        return appendable;
    }
}
