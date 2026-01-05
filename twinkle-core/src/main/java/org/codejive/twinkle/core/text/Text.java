package org.codejive.twinkle.core.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.util.Printable;
import org.codejive.twinkle.util.StyledIterator;
import org.jspecify.annotations.NonNull;

public class Text implements Printable {
    private final List<Line> lines;

    public static Text of(String text) {
        return new Text(new Line((Span.of(text))));
    }

    public static Text of(String text, Style style) {
        return of(text, style.state());
    }

    public static Text of(String text, long styleState) {
        return new Text(new Line((Span.of(text, styleState))));
    }

    public static Text of(Line... lines) {
        return new Text(lines);
    }

    public static Text of(StyledIterator iter) {
        List<Line> lines = new ArrayList<>();
        while (iter.hasNext()) {
            lines.add(Line.of(iter));
        }
        return new Text(lines.toArray(new Line[0]));
    }

    protected Text(Line... lines) {
        this.lines = new ArrayList<>(lines.length);
        Collections.addAll(this.lines, lines);
    }

    @Override
    public @NonNull String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Text{lines=[\n");
        for (Line line : lines) {
            sb.append("   ").append(line.toString()).append(",\n");
        }
        sb.append("]}");
        return sb.toString();
    }

    @Override
    public @NonNull Appendable toAnsi(Appendable appendable, long currentStyleState)
            throws IOException {
        boolean first = true;
        for (Line line : lines) {
            if (!first) {
                appendable.append('\n');
            }
            line.toAnsi(appendable, currentStyleState);
            if (!line.spans().isEmpty()) {
                currentStyleState = line.spans().get(line.spans().size() - 1).style().state();
            }
            first = false;
        }
        return appendable;
    }
}
