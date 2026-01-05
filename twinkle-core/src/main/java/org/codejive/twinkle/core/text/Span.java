package org.codejive.twinkle.core.text;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.widget.Renderable;

public class Span implements Renderable {
    private final String text;
    private final long styleState;
    private final int length;

    public static Span of(String text) {
        return new Span(text, Style.F_UNSTYLED);
    }

    public static Span of(String text, Style style) {
        return new Span(text, style.state());
    }

    public static Span of(String text, long styleState) {
        return new Span(text, styleState);
    }

    protected Span(String text, long styleState) {
        this.text = text;
        this.styleState = styleState;
        this.length = text.codePointCount(0, text.length());
    }

    public int length() {
        return length;
    }

    @Override
    public void render(Canvas canvas) {
        canvas.putStringAt(0, 0, styleState, text);
    }
}
