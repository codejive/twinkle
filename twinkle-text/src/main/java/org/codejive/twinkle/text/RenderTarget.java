package org.codejive.twinkle.text;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.text.util.Size;
import org.codejive.twinkle.text.util.StyledIterator;
import org.jspecify.annotations.NonNull;

public interface RenderTarget {

    public interface PrintOption {}

    /**
     * Get the size of this Buffer.
     *
     * @return
     */
    Size size();

    /**
     * Print a character at the specified position in the buffer with the given style. If the
     * position is out of bounds, the character will not be printed.
     *
     * @param x the x-coordinate of the character
     * @param y the y-coordinate of the character
     * @param style the style to apply to the character
     * @param c the character to print
     */
    void putAt(int x, int y, @NonNull Style style, char c);

    /**
     * Print a codepoint at the specified position in the buffer with the given style. If the
     * position is out of bounds, the codepoint will not be printed.
     *
     * @param x the x-coordinate of the codepoint
     * @param y the y-coordinate of the codepoint
     * @param style the style to apply to the codepoint
     * @param cp the codepoint to print
     */
    void putAt(int x, int y, @NonNull Style style, int cp);

    /**
     * Print a grapheme at the specified position in the buffer with the given style. If the
     * position is out of bounds, the grapheme will not be printed.
     *
     * @param x the x-coordinate of the grapheme
     * @param y the y-coordinate of the grapheme
     * @param style the style to apply to the grapheme
     * @param grapheme the grapheme to print
     */
    void putAt(int x, int y, @NonNull Style style, @NonNull CharSequence grapheme);

    /**
     * Print a string at the specified position in the buffer with the given style. If the string is
     * fully out of bounds, the string will not be printed.
     *
     * @param x the x-coordinate of the string
     * @param y the y-coordinate of the string
     * @param style the style to apply to the string
     * @param str the string to print
     * @param options the print options to apply
     */
    void printAt(
            int x, int y, @NonNull Style style, @NonNull CharSequence str, PrintOption... options);

    /**
     * Print a styled string at the specified position in the buffer.
     *
     * @param x the x-coordinate of the string
     * @param y the y-coordinate of the string
     * @param iter a StyledIterator
     * @param options the print options to apply
     */
    void printAt(int x, int y, @NonNull StyledIterator iter, PrintOption... options);

    /**
     * Clear the cell at the specified position in the buffer, setting it to the default state. If
     * the position is out of bounds, no action will be taken.
     *
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     */
    void clearAt(int x, int y);
}
