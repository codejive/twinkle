package org.codejive.twinkle.core.text;

import org.codejive.twinkle.ansi.Style;
import org.jspecify.annotations.NonNull;

public interface StyledBuffer extends StyledCharSequence {

    char REPLACEMENT_CHAR = '\uFFFD';

    default void setCharAt(int index, @NonNull Style style, char c) {
        setCharAt(index, style.state(), c);
    }

    void setCharAt(int index, long styleState, char c);

    default void setCharAt(int index, @NonNull Style style, int cp) {
        setCharAt(index, style.state(), cp);
    }

    void setCharAt(int index, long styleState, int cp);

    default void setCharAt(int index, @NonNull Style style, @NonNull CharSequence grapheme) {
        setCharAt(index, style.state(), grapheme);
    }

    void setCharAt(int index, long styleState, @NonNull CharSequence grapheme);

    default int putStringAt(int index, @NonNull Style style, @NonNull CharSequence str) {
        return putStringAt(index, style.state(), str);
    }

    int putStringAt(int index, long styleState, @NonNull CharSequence str);

    int putStringAt(int index, @NonNull StyledCharSequence str);

    @NonNull StyledBuffer resize(int newSize);

    /**
     * Converts the buffer to an ANSI string, including ANSI escape codes for styles. This method
     * resets the current style to default at the start of the string.
     *
     * @return The ANSI string representation of the styled buffer.
     */
    @NonNull String toAnsiString();

    /**
     * Converts the buffer to an ANSI string, including ANSI escape codes for styles. This method
     * takes into account the provided current style to generate a result that is as efficient as
     * possible in terms of ANSI codes.
     *
     * @param currentStyle The current style to start with.
     * @return The ANSI string representation of the styled buffer.
     */
    default @NonNull String toAnsiString(Style currentStyle) {
        return toAnsiString(currentStyle.state());
    }

    /**
     * Converts the buffer to an ANSI string, including ANSI escape codes for styles. This method
     * takes into account the provided current style to generate a result that is as efficient as
     * possible in terms of ANSI codes.
     *
     * @param currentStyle The current style to start with.
     * @return The ANSI string representation of the styled buffer.
     */
    @NonNull String toAnsiString(long currentStyle);

    StyledBuffer EMPTY =
            new StyledCodepointBuffer(0) {
                @Override
                public @NonNull StyledCodepointBuffer resize(int newSize) {
                    if (newSize != 0) {
                        throw new UnsupportedOperationException("Cannot resize EMPTY");
                    }
                    return this;
                }
            };

    static @NonNull StyledBuffer of(int width) {
        return new StyledCodepointBuffer(width);
    }
}
