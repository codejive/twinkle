package org.codejive.twinkle.core.text;

import java.io.IOException;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.util.Printable;
import org.jspecify.annotations.NonNull;

public class Span implements Printable {
    private final @NonNull String text;
    private final @NonNull Style style;
    private final int length;

    public static Span of(@NonNull String text) {
        return new Span(text, Style.UNSTYLED);
    }

    public static Span of(@NonNull String text, @NonNull Style style) {
        return new Span(text, style);
    }

    protected Span(@NonNull String text, @NonNull Style style) {
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
    public @NonNull Appendable toAnsi(Appendable appendable, Style currentStyle)
            throws IOException {
        currentStyle.diff(style).toAnsi(appendable, currentStyle);
        appendable.append(text);
        return appendable;
    }
}
