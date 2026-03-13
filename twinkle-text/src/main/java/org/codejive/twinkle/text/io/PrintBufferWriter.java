package org.codejive.twinkle.text.io;

import java.io.PrintWriter;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.ansi.util.AnsiTricks;
import org.codejive.twinkle.text.Buffer;
import org.codejive.twinkle.text.util.Size;
import org.jspecify.annotations.NonNull;

public class PrintBufferWriter extends PrintWriter {
    protected BufferWriter writer;

    public PrintBufferWriter(@NonNull BufferWriter writer) {
        super(writer);
        this.writer = (BufferWriter) super.out;
    }

    protected Size size() {
        return writer.buffer.size();
    }

    /**
     * Get the current cursor X position.
     *
     * @return the current cursor X position
     */
    public int cursorX() {
        return writer.cursorX >= size().width() ? size().width() - 1 : writer.cursorX;
    }

    /**
     * Get the current cursor Y position.
     *
     * @return the current cursor Y position
     */
    public int cursorY() {
        return writer.cursorY >= size().height() ? size().height() - 1 : writer.cursorY;
    }

    /**
     * Move the cursor to the given position.
     *
     * @param x the X position to move the cursor to
     * @param y the Y position to move the cursor to
     * @return a reference to this BufferWriter, for chaining
     */
    public @NonNull PrintBufferWriter at(int x, int y) {
        writer.at(x, y);
        return this;
    }

    /**
     * Check if line wrapping is enabled.
     *
     * @return true if line wrapping is enabled, false otherwise
     */
    public boolean wrap() {
        return writer.lineWrap;
    }

    /**
     * Enable or disable line wrapping. When line wrapping is enabled, the cursor will automatically
     * move to the beginning of the next line when it reaches the end of the current line and
     * another character is printed.
     *
     * @param lineWrap true to enable line wrapping, false to disable it
     * @return a reference to this BufferWriter, for chaining
     */
    public @NonNull PrintBufferWriter wrap(boolean lineWrap) {
        writer.lineWrap = lineWrap;
        return this;
    }

    /**
     * Get the current style.
     *
     * @return the current style
     */
    public @NonNull Style style() {
        return writer.curStyle;
    }

    /**
     * Set the current style.
     *
     * @param style the style to set
     * @return a reference to this BufferWriter, for chaining
     */
    public @NonNull PrintBufferWriter style(Style style) {
        writer.curStyle = style;
        return this;
    }

    /**
     * Get the transparant characters. Transparant characters are characters that are skipped when
     * overlaying text on top of existing content. The default value is "\0", which means that by
     * default null characters are transparant.
     *
     * @return the transparant characters
     */
    public @NonNull String transparant() {
        return writer.transparantCharacters;
    }

    /**
     * Set the transparant characters. Transparant characters are characters that are skipped when
     * overlaying text on top of existing content. Setting this to an empty string will disable
     * transparant characters.
     *
     * @param transparantCharacters the transparant characters to set
     * @return a reference to this BufferWriter, for chaining
     */
    public @NonNull PrintBufferWriter transparant(@NonNull String transparantCharacters) {
        writer.transparantCharacters = transparantCharacters;
        return this;
    }

    /**
     * Prints the given text to the buffer. When the text consists of multiple lines, the cursor
     * will each time be moved down from the starting point of the previous line, so that the text
     * will be printed in a block.
     *
     * @param text the text to print
     * @return a reference to this BufferWriter, for chaining
     */
    public @NonNull Buffer printBlock(String text) {
        AnsiTricks.blockify(this, text);
        return writer.buffer;
    }
}
