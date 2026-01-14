package org.codejive.twinkle.core.text;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.util.Rect;
import org.codejive.twinkle.core.util.Sized;
import org.codejive.twinkle.util.StyledIterator;
import org.jspecify.annotations.NonNull;

public interface Canvas extends Sized {
    char charAt(int x, int y);

    int codepointAt(int x, int y);

    @NonNull String graphemeAt(int x, int y);

    @NonNull Style styleAt(int x, int y);

    void putCharAt(int x, int y, @NonNull Style style, char c);

    void putCharAt(int x, int y, @NonNull Style style, int cp);

    void putCharAt(int x, int y, @NonNull Style style, @NonNull CharSequence grapheme);

    int putStringAt(int x, int y, @NonNull Style style, @NonNull CharSequence str);

    int putStringAt(int x, int y, @NonNull StyledIterator iter);

    void drawHLineAt(int x, int y, int x2, @NonNull Style style, char c);

    void drawVLineAt(int x, int y, int y2, @NonNull Style style, char c);

    void copyTo(Canvas canvas, int x, int y);

    default @NonNull Canvas view(int left, int top, int width, int height) {
        return view(new Rect(left, top, width, height));
    }

    @NonNull Canvas view(@NonNull Rect rect);
}
