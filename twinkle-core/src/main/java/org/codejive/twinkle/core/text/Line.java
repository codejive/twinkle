package org.codejive.twinkle.core.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
        return new Line(Collections.singletonList(Span.of(text)));
    }

    public static Line of(String text, Style style) {
        return new Line(Collections.singletonList(Span.of(text, style)));
    }

    public static Line of(Span... spans) {
        return new Line(Arrays.asList(spans));
    }

    public static Line of(Line line) {
        return new Line(line.spans);
    }

    protected Line(List<Span> spans) {
        this.spans = new ArrayList<>(spans.size());
        this.spans.addAll(spans);
    }

    public static Line of(StyledIterator iter) {
        List<Span> spans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        Style currentStyle = Style.UNKNOWN;
        while (iter.hasNext()) {
            long cp = iter.next();
            if (cp == '\n') {
                break;
            }
            if (iter.style() != currentStyle) {
                if (sb.length() > 0) {
                    spans.add(Span.of(sb.toString(), currentStyle));
                    sb.setLength(0);
                }
                currentStyle = iter.style();
            }
            sb.appendCodePoint((int) cp);
        }
        if (sb.length() > 0) {
            spans.add(Span.of(sb.toString(), currentStyle));
        }
        return new Line(spans);
    }

    public Line concat(Line other) {
        return concat(other.spans);
    }

    public Line concat(List<Span> spans) {
        this.spans.addAll(spans);
        return this;
    }

    public Line concat(Span... spans) {
        this.spans.addAll(Arrays.asList(spans));
        return this;
    }

    public Line concat(String text) {
        this.spans.add(Span.of(text));
        return this;
    }

    public Line concat(String text, Style style) {
        this.spans.add(Span.of(text, style));
        return this;
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
    public @NonNull Appendable toAnsi(Appendable appendable, Style currentStyle)
            throws IOException {
        for (Span span : spans) {
            span.toAnsi(appendable, currentStyle);
            currentStyle = span.style();
        }
        return appendable;
    }
}
