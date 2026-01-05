package org.codejive.twinkle.core.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.widget.Canvas;
import org.codejive.twinkle.core.widget.Renderable;
import org.codejive.twinkle.util.StyledIterator;

public class Text implements Renderable {
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
    public void render(Canvas canvas) {
        int y = 0;
        for (Line line : lines) {
            line.render(canvas.view(0, y, canvas.size().width(), 1));
            y++;
        }
    }
}
