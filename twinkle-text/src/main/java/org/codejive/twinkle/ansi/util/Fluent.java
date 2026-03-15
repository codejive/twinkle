package org.codejive.twinkle.ansi.util;

import static org.codejive.twinkle.ansi.Constants.*;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Style;

/**
 * A utility class that provides a fluent API for building ANSI strings. This class is designed to
 * be used in a chainable manner, allowing for easy composition of ANSI styles and commands.
 */
public class Fluent {
    protected final Appendable appendable;
    protected final NegatableCommands negatableCommands;
    protected final BackgroundColors backgroundColors;
    protected final ScreenCommands screenCommands;
    protected final LineCommands lineCommands;
    protected final Style startingStyle;
    protected Style currentStyle;
    protected Deque<Style> styleStack;

    /**
     * Creates a new String-based Fluent instance
     *
     * @return a new Fluent instance
     */
    public static Fluent string() {
        return string(Style.DEFAULT);
    }

    /**
     * Creates a new String-based Fluent instance with the given starting style
     *
     * @param startingStyle the style that is currently active
     * @return a new Fluent instance
     */
    public static Fluent string(Style startingStyle) {
        return new StringFluent(startingStyle);
    }

    /**
     * Creates a new Fluent instance that writes to the given Appendable
     *
     * @param appendable the Appendable to write to
     * @return a new Fluent instance
     */
    public static Fluent of(Appendable appendable) {
        return of(appendable, Style.DEFAULT);
    }

    /**
     * Creates a new Fluent instance that writes to the given Appendable and starts with the given
     * style
     *
     * @param appendable the Appendable to write to
     * @param startingStyle the style that is currently active
     * @return a new Fluent instance
     */
    public static Fluent of(Appendable appendable, Style startingStyle) {
        return new Fluent(appendable, startingStyle);
    }

    protected Fluent(Appendable appendable, Style startingStyle) {
        this.appendable = appendable;
        this.negatableCommands = new NegatableCommands();
        this.backgroundColors = new BackgroundColors();
        this.screenCommands = new ScreenCommands();
        this.lineCommands = new LineCommands();
        this.startingStyle = startingStyle;
        this.currentStyle = startingStyle;
        this.styleStack = new ArrayDeque<>();
    }

    /**
     * Returns the current style combining the styles that have been applied since the creation of
     * this Fluent instance.
     *
     * @return the current style
     */
    public Style style() {
        return currentStyle;
    }

    /**
     * Applies the given style by calculating the difference between the new style and the current
     * style. This will output only the necessary ANSI codes to transition from the current style to
     * the new style.
     *
     * @param newStyle the new style to apply
     * @return this Fluent instance for chaining
     */
    public Fluent style(Style newStyle) {
        Style diff = newStyle.diff(currentStyle);
        append(diff.toAnsi(currentStyle));
        currentStyle = newStyle;
        return this;
    }

    /**
     * Pushes the current style onto the style stack. This allows you to save the current style
     * before applying a new one, and then later call {@link #pop()} to return to the previous
     * style.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent push() {
        styleStack.push(currentStyle);
        return this;
    }

    /**
     * Pushes the current style onto the style stack and applies the given new style. This is a
     * convenience method that combines the functionality of {@link #push()} and {@link
     * #style(Style)}.
     *
     * @param newStyle the new style to apply
     * @return this Fluent instance for chaining
     */
    public Fluent push(Style newStyle) {
        styleStack.push(currentStyle);
        style(newStyle);
        return this;
    }

    /**
     * Pops the most recently pushed style from the style stack and applies it. If the style stack
     * is empty, this method does nothing.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent pop() {
        if (styleStack.isEmpty()) {
            return this;
        }
        Style popped = styleStack.pop();
        Style diff = popped.diff(currentStyle);
        append(diff.toAnsi(currentStyle));
        currentStyle = popped;
        return this;
    }

    /**
     * Like {@link #text(Object, Object...)}, but for Printables. This will write the ANSI
     * representation of the Printable as text.
     *
     * @param prt the Printable to write as text
     * @return this Fluent instance for chaining
     */
    public Fluent text(Printable prt) {
        // We assume that Printables should always be printed as blocks
        return block(prt);
    }

    /**
     * Writes the string representation of the given object, applying any active styles. The string
     * can include format specifiers, which will be replaced by the corresponding arguments. See
     * {@link String#format(String, Object...)} for details on the supported format.
     *
     * @param obj the text or format string
     * @param args the arguments for the format string
     * @return this Fluent instance for chaining
     */
    public Fluent text(Object obj, Object... args) {
        return append(String.format(String.valueOf(obj), args));
    }

    /**
     * Like {@link #block(Object, Object...)}, but for Printables. This will write the ANSI
     * representation of the Printable as a block.
     *
     * @param prt the Printable to write as a block
     * @return this Fluent instance for chaining
     */
    public Fluent block(Printable prt) {
        return block(prt.toAnsi(currentStyle));
    }

    /**
     * Writes the string representation of the given object as a block, meaning that any newlines
     * encountered in the text will return the the original column the cursor was at when the block
     * command was called. This is useful for writing multi-line text while maintaining the same
     * indentation. Any active styles will be applied to the entire text block. The string can
     * include format specifiers, which will be replaced by the corresponding arguments. See {@link
     * String#format(String, Object...)} for details on the supported format.
     *
     * @param obj the text or format string
     * @param args the arguments for the format string
     * @return this Fluent instance for chaining
     */
    public Fluent block(Object obj, Object... args) {
        return append(AnsiTricks.blockify(String.format(String.valueOf(obj), args)));
    }

    /**
     * Writes a hyperlink with the given URL and link text. This is the equivalent of calling {@code
     * url(url).text(text).lru()}.
     *
     * @param text the text to display for the hyperlink
     * @param url the URL of the hyperlink
     * @return this Fluent instance for chaining
     */
    public Fluent link(String text, String url) {
        return url(url).append(text).lru();
    }

    /**
     * Writes a hyperlink with the given URL and link text. This is the equivalent of calling {@code
     * url(url, id).text(text).lru()}.
     *
     * @param text the text to display for the hyperlink
     * @param url the URL of the hyperlink
     * @param id the ID for the hyperlink
     * @return this Fluent instance for chaining
     */
    public Fluent link(String text, String url, String id) {
        return url(url, id).append(text).lru();
    }

    /**
     * Begins a hyperlink with the given URL. The link will continue until {@link #lru()} is called.
     *
     * @param url the URL of the hyperlink
     * @return this Fluent instance for chaining
     */
    public Fluent url(String url) {
        return append(Ansi.link(url));
    }

    /**
     * Begins a hyperlink with the given URL and ID. The link will continue until {@link #lru()} is
     * called. The ID can be used by some terminal emulators to identify the link.
     *
     * @param url the URL of the hyperlink
     * @param id an optional ID for the hyperlink
     * @return this Fluent instance for chaining
     */
    public Fluent url(String url, String id) {
        return append(Ansi.link(url, id));
    }

    /**
     * Ends the current hyperlink. This should be called after {@link #url(String)} to properly
     * close the hyperlink.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent lru() {
        return append(Ansi.linkEnd());
    }

    /**
     * Resets to the default style.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent reset() {
        Ansi.styles(appendable, RESET);
        currentStyle = Style.DEFAULT;
        return this;
    }

    /**
     * Applies the bold style. Alias for {@link #bold()}.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent b() {
        return bold();
    }

    /**
     * Applies the bold style.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent bold() {
        Ansi.styles(appendable, BOLD);
        currentStyle = currentStyle.bold();
        return this;
    }

    /**
     * Applies the faint style.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent faint() {
        Ansi.styles(appendable, FAINT);
        currentStyle = currentStyle.faint();
        return this;
    }

    /**
     * Applies the normal style.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent normal() {
        Ansi.styles(appendable, NORMAL);
        currentStyle = currentStyle.normal();
        return this;
    }

    /**
     * Applies the italic style. Alias for {@link #italic()}.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent i() {
        return italic();
    }

    /**
     * Applies the italic style.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent italic() {
        Ansi.styles(appendable, ITALICIZED);
        currentStyle = currentStyle.italic();
        return this;
    }

    /**
     * Applies the underline style. Alias for {@link #underline()}.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent ul() {
        return underline();
    }

    /**
     * Applies the underline style.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent underline() {
        Ansi.styles(appendable, UNDERLINED);
        currentStyle = currentStyle.underlined();
        return this;
    }

    /**
     * Applies the blink style.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent blink() {
        Ansi.styles(appendable, BLINK);
        currentStyle = currentStyle.blink();
        return this;
    }

    /**
     * Applies the inverse style.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent inverse() {
        Ansi.styles(appendable, INVERSE);
        currentStyle = currentStyle.inverse();
        return this;
    }

    /**
     * Applies the invisible style.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent invisible() {
        Ansi.styles(appendable, INVISIBLE);
        currentStyle = currentStyle.hidden();
        return this;
    }

    /**
     * Applies the strikethrough style.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent strikethrough() {
        Ansi.styles(appendable, CROSSEDOUT);
        currentStyle = currentStyle.strikethrough();
        return this;
    }

    /**
     * Begins a negation command, allowing you to specify turn off styles. Examples would be {@code
     * fluent.not().italic()} to turn off italic or {@code fluent.not().underline().not().blink()}
     * to turn off underline and then turn off blink.
     *
     * @return a NegatableCommands instance for chaining a negatable command
     */
    public NegatableCommands not() {
        return negatableCommands;
    }

    /**
     * Applies a black foreground color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.BLACK}.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent black() {
        return color(Color.BasicColor.BLACK);
    }

    /**
     * Applies a red foreground color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.RED}.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent red() {
        return color(Color.BasicColor.RED);
    }

    /**
     * Applies a green foreground color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.GREEN}.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent green() {
        return color(Color.BasicColor.GREEN);
    }

    /**
     * Applies a yellow foreground color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.YELLOW}.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent yellow() {
        return color(Color.BasicColor.YELLOW);
    }

    /**
     * Applies a blue foreground color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.BLUE}.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent blue() {
        return color(Color.BasicColor.BLUE);
    }

    /**
     * Applies a magenta foreground color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.MAGENTA}.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent magenta() {
        return color(Color.BasicColor.MAGENTA);
    }

    /**
     * Applies a cyan foreground color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.CYAN}.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent cyan() {
        return color(Color.BasicColor.CYAN);
    }

    /**
     * Applies a white foreground color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.WHITE}.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent white() {
        return color(Color.BasicColor.WHITE);
    }

    /**
     * Applies the given foreground color.
     *
     * @param color the color to apply
     * @return this Fluent instance for chaining
     */
    public Fluent color(Color color) {
        return append(color.toAnsiFg());
    }

    /**
     * Applies the given indexed foreground color.
     *
     * @param idx the index of the color to apply (0-255)
     * @return this Fluent instance for chaining
     */
    public Fluent color(int idx) {
        return color(Color.indexed(idx));
    }

    /**
     * Applies the given RGB foreground color.
     *
     * @param r the red component (0-255)
     * @param g the green component (0-255)
     * @param b the blue component (0-255)
     * @return this Fluent instance for chaining
     */
    public Fluent color(int r, int g, int b) {
        return color(Color.rgb(r, g, b));
    }

    /**
     * Begins a ackground color command chain, allowing you to specify a background color in the
     * same fluent style as the foreground color commands. Alias for {@link #background()}.
     *
     * @return a BackgroundColors instance for chaining background color commands
     */
    public BackgroundColors bg() {
        return backgroundColors;
    }

    /**
     * Begins a ackground color command chain, allowing you to specify a background color in the
     * same fluent style as the foreground color commands.
     *
     * @return a BackgroundColors instance for chaining background color commands
     */
    public BackgroundColors background() {
        return backgroundColors;
    }

    /**
     * Positions the cursor at the specified column (x) and row (y). Coordinates are 0-based (the
     * top-left corner is 0,0).
     *
     * @param x the column (0-based, 0 is leftmost)
     * @param y the row (0-based, 0 is topmost)
     * @return this Fluent instance for chaining
     */
    public Fluent at(int x, int y) {
        return append(Ansi.cursorPos(x, y));
    }

    /**
     * Moves the cursor up by one row. Alias for {@link #up(int)} with n=1.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent up() {
        return up(1);
    }

    /**
     * Moves the cursor up by the specified number of rows.
     *
     * @param n the number of rows to move up
     * @return this Fluent instance for chaining
     */
    public Fluent up(int n) {
        return append(Ansi.cursorUp(n));
    }

    /**
     * Moves the cursor down by one row. Alias for {@link #down(int)} with n=1.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent down() {
        return down(1);
    }

    /**
     * Moves the cursor down by the specified number of rows.
     *
     * @param n the number of rows to move down
     * @return this Fluent instance for chaining
     */
    public Fluent down(int n) {
        return append(Ansi.cursorDown(n));
    }

    /**
     * Moves the cursor right by one column. Alias for {@link #forward()}.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent right() {
        return forward();
    }

    /**
     * Moves the cursor right by the specified number of columns.
     *
     * @param n the number of columns to move right
     * @return this Fluent instance for chaining
     */
    public Fluent right(int n) {
        return forward(n);
    }

    /**
     * Moves the cursor forward by one column. Alias for {@link #forward(int)} with n=1.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent forward() {
        return forward(1);
    }

    /**
     * Moves the cursor forward by the specified number of columns.
     *
     * @param n the number of columns to move forward
     * @return this Fluent instance for chaining
     */
    public Fluent forward(int n) {
        return append(Ansi.cursorForward(n));
    }

    /**
     * Moves the cursor left by one column. Alias for {@link #backward()}.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent left() {
        return backward();
    }

    /**
     * Moves the cursor left by the specified number of columns. Alias for {@link #backward(int)}.
     *
     * @param n the number of columns to move left
     * @return this Fluent instance for chaining
     */
    public Fluent left(int n) {
        return backward(n);
    }

    /**
     * Moves the cursor backward by one column. Alias for {@link #backward(int)} with n=1.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent backward() {
        return backward(1);
    }

    /**
     * Moves the cursor backward by the specified number of columns.
     *
     * @param n the number of columns to move backward
     * @return this Fluent instance for chaining
     */
    public Fluent backward(int n) {
        return append(Ansi.cursorBackward(n));
    }

    /**
     * Moves the cursor to the beginning of the next line. Alias for {@link #next(int)} with n=1.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent next() {
        return next(1);
    }

    /**
     * Moves the cursor to the beginning of the n-th line down.
     *
     * @param n the number of lines to move down
     * @return this Fluent instance for chaining
     */
    public Fluent next(int n) {
        return append(CSI + n + CURSOR_NEXT_LINE);
    }

    /**
     * Moves the cursor to the beginning of the previous line. Alias for {@link #prev(int)} with
     * n=1.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent prev() {
        return prev(1);
    }

    /**
     * Moves the cursor to the beginning of the n-th line up.
     *
     * @param n the number of lines to move up
     * @return this Fluent instance for chaining
     */
    public Fluent prev(int n) {
        return append(CSI + n + CURSOR_PREV_LINE);
    }

    /**
     * Positions the cursor at the specified column on the current row. The column is 0-based (0 is
     * leftmost). Alias for {@link #column(int)}.
     *
     * @param column the column (0-based, 0 is leftmost)
     * @return this Fluent instance for chaining
     */
    public Fluent col(int column) {
        return column(column);
    }

    /**
     * Positions the cursor at the specified column on the current row. The column is 0-based (0 is
     * leftmost).
     *
     * @param column the column (0-based, 0 is leftmost)
     * @return this Fluent instance for chaining
     */
    public Fluent column(int column) {
        return append(Ansi.cursorToColumn(column));
    }

    /**
     * Hides the cursor.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent hide() {
        return append(Ansi.cursorHide());
    }

    /**
     * Shows the cursor.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent show() {
        return append(Ansi.cursorShow());
    }

    /**
     * Marks the current cursor position.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent mark() {
        return append(Ansi.cursorSave());
    }

    /**
     * Jumps to the previously marked cursor position.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent jump() {
        return append(Ansi.cursorRestore());
    }

    /**
     * Enables or disables automatic line wrapping. When enabled, text that exceeds the right edge
     * of the terminal will wrap to the next line. When disabled, text will continue on the same
     * line, potentially overflowing and not being visible.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent wrap() {
        return append(Ansi.autoWrap(true));
    }

    /**
     * Restores the style to what it was when this Fluent instance was created.
     *
     * @return this Fluent instance for chaining
     */
    public Fluent restore() {
        Style diff = currentStyle.diff(startingStyle);
        append(diff.toAnsi(currentStyle));
        currentStyle = startingStyle;
        return this;
    }

    /**
     * The same as {@link #restore()} but meant to signal the end of a fluent chain and therefore
     * returns the underlying Appendable instead of the Fluent instance.
     *
     * @return the underlying Appendable
     */
    public Appendable done() {
        restore();
        // Bit of a trick, but it makes life easier
        if (appendable instanceof Writer) {
            try {
                ((Writer) appendable).flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return appendable;
    }

    /**
     * Switches to screen commands, which are used for things like clearing the screen or switching
     * to the alternate buffer.
     *
     * @return a ScreenCommands instance for chaining screen-related commands
     */
    public ScreenCommands screen() {
        return screenCommands;
    }

    /**
     * Switches to line commands, used for clearing parts of the current line.
     *
     * @return a LineCommands instance for chaining line-related commands
     */
    public LineCommands line() {
        return lineCommands;
    }

    private Fluent append(String str) {
        try {
            appendable.append(str);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public class NegatableCommands {
        /**
         * Turns off the italic style. Alias for {@link #italic()}.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent i() {
            return italic();
        }

        /**
         * Turns off the italic style.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent italic() {
            Ansi.styles(appendable, NOTITALICIZED);
            currentStyle = currentStyle.italicOff();
            return Fluent.this;
        }

        /**
         * Turns off the underline style. Alias for {@link #underline()}.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent ul() {
            return underline();
        }

        /**
         * Turns off the underline style.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent underline() {
            Ansi.styles(appendable, NOTUNDERLINED);
            currentStyle = currentStyle.underlinedOff();
            return Fluent.this;
        }

        /**
         * Turns off the blink style.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent blink() {
            Ansi.styles(appendable, STEADY);
            currentStyle = currentStyle.blinkOff();
            return Fluent.this;
        }

        /**
         * Turns off the inverse style.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent inverse() {
            Ansi.styles(appendable, POSITIVE);
            currentStyle = currentStyle.inverseOff();
            return Fluent.this;
        }

        /**
         * Turns off the invisible style.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent invisible() {
            Ansi.styles(appendable, VISIBLE);
            currentStyle = currentStyle.hiddenOff();
            return Fluent.this;
        }

        /**
         * Turns off the strikethrough style.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent strikethrough() {
            Ansi.styles(appendable, NOTCROSSEDOUT);
            currentStyle = currentStyle.strikethroughOff();
            return Fluent.this;
        }

        /**
         * Disables automatic line wrapping.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent wrap() {
            return append(Ansi.autoWrap(false));
        }
    }

    public class BackgroundColors {
        /**
         * Applies a black background color. Alias for {@link #color(Color)} with {@code
         * Color.BasicColor.BLACK}.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent black() {
            return color(Color.BasicColor.BLACK);
        }

        /**
         * Applies a red background color. Alias for {@link #color(Color)} with {@code
         * Color.BasicColor.RED}.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent red() {
            return color(Color.BasicColor.RED);
        }

        /**
         * Applies a green background color. Alias for {@link #color(Color)} with {@code
         * Color.BasicColor.GREEN}.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent green() {
            return color(Color.BasicColor.GREEN);
        }

        /**
         * Applies a yellow background color. Alias for {@link #color(Color)} with {@code
         * Color.BasicColor.YELLOW}.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent yellow() {
            return color(Color.BasicColor.YELLOW);
        }

        /**
         * Applies a blue background color. Alias for {@link #color(Color)} with {@code
         * Color.BasicColor.BLUE}.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent blue() {
            return color(Color.BasicColor.BLUE);
        }

        /**
         * Applies a magenta background color. Alias for {@link #color(Color)} with {@code
         * Color.BasicColor.MAGENTA}.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent magenta() {
            return color(Color.BasicColor.MAGENTA);
        }

        /**
         * Applies a cyan background color. Alias for {@link #color(Color)} with {@code
         * Color.BasicColor.CYAN}.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent cyan() {
            return color(Color.BasicColor.CYAN);
        }

        /**
         * Applies a white background color. Alias for {@link #color(Color)} with {@code
         * Color.BasicColor.WHITE}.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent white() {
            return color(Color.BasicColor.WHITE);
        }

        /**
         * Applies the given background color.
         *
         * @param color the color to apply
         * @return this Fluent instance for chaining
         */
        public Fluent color(Color color) {
            return append(color.toAnsiBg());
        }

        /**
         * Applies the given indexed background color.
         *
         * @param idx the index of the color to apply (0-255)
         * @return this Fluent instance for chaining
         */
        public Fluent color(int idx) {
            return color(Color.indexed(idx));
        }

        /**
         * Applies the given RGB background color.
         *
         * @param r the red component (0-255)
         * @param g the green component (0-255)
         * @param b the blue component (0-255)
         * @return this Fluent instance for chaining
         */
        public Fluent color(int r, int g, int b) {
            return color(Color.rgb(r, g, b));
        }
    }

    public class ScreenCommands {
        /**
         * Clears the entire screen.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent clear() {
            return append(CSI + SCREEN_ERASE_FULL);
        }

        /**
         * Clears from the cursor position to the end of the screen.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent clearToEnd() {
            return append(CSI + SCREEN_ERASE_END);
        }

        /**
         * Clears from the cursor position to the start of the screen.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent clearToStart() {
            return append(CSI + SCREEN_ERASE_START);
        }

        /**
         * Switches to the alternate screen buffer. Alias for {@link #alternate()}.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent alt() {
            return alternate();
        }

        /**
         * Switches to the alternate screen buffer.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent alternate() {
            return append(CSI + SCREEN_SAVE);
        }

        /**
         * Restores the primary screen buffer.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent restore() {
            return append(CSI + SCREEN_RESTORE);
        }
    }

    public class LineCommands {
        /**
         * Clears the entire current line.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent clear() {
            return append(CSI + LINE_ERASE_FULL);
        }

        /**
         * Clears from the cursor position to the end of the line.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent clearToEnd() {
            return append(CSI + LINE_ERASE_END);
        }

        /**
         * Clears from the cursor position to the start of the line.
         *
         * @return this Fluent instance for chaining
         */
        public Fluent clearToStart() {
            return append(CSI + LINE_ERASE_START);
        }
    }

    public static class StringFluent extends Fluent {
        public StringFluent(Style startingStyle) {
            super(new StringBuilder(), startingStyle);
        }

        @Override
        public String toString() {
            return ((StringBuilder) appendable).toString();
        }
    }
}
