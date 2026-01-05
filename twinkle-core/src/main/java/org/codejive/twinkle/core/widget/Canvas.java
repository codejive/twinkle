package org.codejive.twinkle.core.widget;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.util.StyledIterator;
import org.jspecify.annotations.NonNull;

public interface Canvas extends Sized {
    char charAt(int x, int y);

    int codepointAt(int x, int y);

    @NonNull String graphemeAt(int x, int y);

    long styleStateAt(int x, int y);

    @NonNull Style styleAt(int x, int y);

    default void setCharAt(int x, int y, @NonNull Style style, char c) {
        setCharAt(x, y, style.state(), c);
    }

    void setCharAt(int x, int y, long styleState, char c);

    default void setCharAt(int x, int y, @NonNull Style style, int cp) {
        setCharAt(x, y, style.state(), cp);
    }

    void setCharAt(int x, int y, long styleState, int cp);

    default void setCharAt(int x, int y, @NonNull Style style, @NonNull CharSequence grapheme) {
        setCharAt(x, y, style.state(), grapheme);
    }

    void setCharAt(int x, int y, long styleState, @NonNull CharSequence grapheme);

    default int putStringAt(int x, int y, @NonNull Style style, @NonNull CharSequence str) {
        return putStringAt(x, y, style.state(), str);
    }

    int putStringAt(int x, int y, long styleState, @NonNull CharSequence str);

    int putStringAt(int x, int y, @NonNull StyledIterator iter);

    default void drawHLineAt(int x, int y, int x2, Style style, char c) {
        drawHLineAt(x, y, x2, style.state(), c);
    }

    void drawHLineAt(int x, int y, int x2, long styleState, char c);

    default void drawVLineAt(int x, int y, int y2, Style style, char c) {
        drawVLineAt(x, y, y2, style.state(), c);
    }

    void drawVLineAt(int x, int y, int y2, long styleState, char c);

    void copyTo(Canvas canvas, int x, int y);

    default @NonNull Canvas view(int left, int top, int width, int height) {
        return view(new Rect(left, top, width, height));
    }

    @NonNull Canvas view(@NonNull Rect rect);
}
