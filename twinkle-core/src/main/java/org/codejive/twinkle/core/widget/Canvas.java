package org.codejive.twinkle.core.widget;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.text.StyledCharSequence;
import org.jspecify.annotations.NonNull;

public interface Canvas extends Sized {
    char charAt(int x, int y);

    int codepointAt(int x, int y);

    @NonNull String graphemeAt(int x, int y);

    long styleStateAt(int x, int y);

    @NonNull Style styleAt(int x, int y);

    void setCharAt(int x, int y, @NonNull Style style, char c);

    void setCharAt(int x, int y, long styleState, char c);

    void setCharAt(int x, int y, @NonNull Style style, int cp);

    void setCharAt(int x, int y, long styleState, int cp);

    void setCharAt(int x, int y, @NonNull Style style, @NonNull CharSequence grapheme);

    void setCharAt(int x, int y, long styleState, @NonNull CharSequence grapheme);

    int putStringAt(int x, int y, @NonNull Style style, @NonNull CharSequence str);

    int putStringAt(int x, int y, long styleState, @NonNull CharSequence str);

    int putStringAt(int x, int y, @NonNull StyledCharSequence str);

    default @NonNull Canvas view(int left, int top, int width, int height) {
        return view(new Rect(left, top, width, height));
    }

    @NonNull Canvas view(@NonNull Rect rect);
}
