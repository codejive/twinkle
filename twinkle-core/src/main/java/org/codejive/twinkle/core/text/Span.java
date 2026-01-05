package org.codejive.twinkle.core.text;

import java.io.IOException;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.util.Printable;
import org.jspecify.annotations.NonNull;

public class Span implements Printable {
    private final @NonNull String text;
    private final Style style;
    private final int length;

    public static Span of(@NonNull String text) {
        return new Span(text, Style.of(Style.F_UNSTYLED));
    }

    public static Span of(@NonNull String text, Style style) {
        return new Span(text, style);
    }

    public static Span of(@NonNull String text, long styleState) {
        return new Span(text, Style.of(styleState));
    }

    protected Span(@NonNull String text, Style style) {
        this.text = text;
        this.style = style;
        this.length = text.codePointCount(0, text.length());
    }

    public @NonNull String text() {
        return text;
    }

    public int length() {
        return length;
    }

    public @NonNull Style style() {
        return style;
    }

    @Override
    public String toString() {
        return "Span{text='" + text + "', style=" + style + "}";
    }

    @Override
    public @NonNull Appendable toAnsi(Appendable appendable, long currentStyleState)
            throws IOException {
        style.toAnsi(appendable, currentStyleState);
        appendable.append(text);
        return appendable;
    }
}
