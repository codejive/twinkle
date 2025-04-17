package org.codejive.twinkle.core.text;

import org.codejive.twinkle.ansi.Style;
import org.jspecify.annotations.NonNull;

public interface StyledCharSequence {

    int length();

    /**
     * Returns the {@code char} value at the specified index. In contrast to the original {@link
     * CharSequence#charAt(int)} specification, this method never throws an exception and always
     * returns a valid character. If the index is out of bounds, it returns the Unicode replacement
     * character.
     *
     * @param index the index of the {@code char} value to be returned
     * @return the specified {@code char} value
     */
    char charAt(int index);

    int codepointAt(int i);

    @NonNull String graphemeAt(int i);

    long styleStateAt(int i);

    @NonNull Style styleAt(int i);

    // @Override
    @NonNull StyledCharSequence subSequence(int start, int end);

    static @NonNull StyledCharSequence fromString(@NonNull Style style, @NonNull String str) {
        StyledStringBuilder builder = new StyledStringBuilder(str.length());
        builder.append(style, str);
        return builder;
    }

    static @NonNull StyledCharSequence fromString(long styleState, @NonNull String str) {
        StyledStringBuilder builder = new StyledStringBuilder(str.length());
        builder.append(styleState, str);
        return builder;
    }
}
