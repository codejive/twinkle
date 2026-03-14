package org.codejive.twinkle.ansi.util;

import static org.codejive.twinkle.ansi.Constants.*;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Style;

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

    public Fluent text(Printable prt) {
        // We assume that Printables should always be printed as blocks
        return block(prt);
    }

    public Fluent text(Object obj, Object... args) {
        return append(String.format(String.valueOf(obj), args));
    }

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
        return append(Ansi.linkStart(url));
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
        return append(Ansi.linkStart(url, id));
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

    public Fluent reset() {
        Ansi.styles(appendable, RESET);
        currentStyle = Style.DEFAULT;
        return this;
    }

    public Fluent b() {
        return bold();
    }

    public Fluent bold() {
        Ansi.styles(appendable, BOLD);
        currentStyle = currentStyle.bold();
        return this;
    }

    public Fluent faint() {
        Ansi.styles(appendable, FAINT);
        currentStyle = currentStyle.faint();
        return this;
    }

    public Fluent normal() {
        Ansi.styles(appendable, NORMAL);
        currentStyle = currentStyle.normal();
        return this;
    }

    public Fluent i() {
        return italic();
    }

    public Fluent italic() {
        Ansi.styles(appendable, ITALICIZED);
        currentStyle = currentStyle.italic();
        return this;
    }

    public Fluent ul() {
        return underline();
    }

    public Fluent underline() {
        Ansi.styles(appendable, UNDERLINED);
        currentStyle = currentStyle.underlined();
        return this;
    }

    public Fluent blink() {
        Ansi.styles(appendable, BLINK);
        currentStyle = currentStyle.blink();
        return this;
    }

    public Fluent inverse() {
        Ansi.styles(appendable, INVERSE);
        currentStyle = currentStyle.inverse();
        return this;
    }

    public Fluent invisible() {
        Ansi.styles(appendable, INVISIBLE);
        currentStyle = currentStyle.hidden();
        return this;
    }

    public Fluent strikethrough() {
        Ansi.styles(appendable, CROSSEDOUT);
        currentStyle = currentStyle.strikethrough();
        return this;
    }

    public NegatableCommands not() {
        return negatableCommands;
    }

    public Fluent black() {
        return color(Color.BasicColor.BLACK);
    }

    public Fluent red() {
        return color(Color.BasicColor.RED);
    }

    public Fluent green() {
        return color(Color.BasicColor.GREEN);
    }

    public Fluent yellow() {
        return color(Color.BasicColor.YELLOW);
    }

    public Fluent blue() {
        return color(Color.BasicColor.BLUE);
    }

    public Fluent magenta() {
        return color(Color.BasicColor.MAGENTA);
    }

    public Fluent cyan() {
        return color(Color.BasicColor.CYAN);
    }

    public Fluent white() {
        return color(Color.BasicColor.WHITE);
    }

    public Fluent color(Color color) {
        return append(color.toAnsiFg());
    }

    public Fluent color(int idx) {
        return color(Color.indexed(idx));
    }

    public Fluent color(int r, int g, int b) {
        return color(Color.rgb(r, g, b));
    }

    /**
     * Begins a ackground color command chain, allowing you to specify a background color in the
     * same fluent style as the foreground color commands.
     *
     * @return a BackgroundColors instance for chaining background color commands
     */
    public BackgroundColors bg() {
        return backgroundColors;
    }

    public BackgroundColors background() {
        return backgroundColors;
    }

    public Fluent at(int x, int y) {
        return append(Ansi.cursorPos(y, x));
    }

    public Fluent up() {
        return up(1);
    }

    public Fluent up(int n) {
        return append(Ansi.cursorUp(n));
    }

    public Fluent down() {
        return down(1);
    }

    public Fluent down(int n) {
        return append(Ansi.cursorDown(n));
    }

    public Fluent right() {
        return forward(1);
    }

    public Fluent right(int n) {
        return forward(n);
    }

    public Fluent forward() {
        return forward(1);
    }

    public Fluent forward(int n) {
        return append(Ansi.cursorForward(n));
    }

    public Fluent left() {
        return backward(1);
    }

    public Fluent left(int n) {
        return backward(n);
    }

    public Fluent backward() {
        return backward(1);
    }

    public Fluent backward(int n) {
        return append(Ansi.cursorBackward(n));
    }

    public Fluent next() {
        return next(1);
    }

    public Fluent next(int n) {
        return append(CSI + n + CURSOR_NEXT_LINE);
    }

    public Fluent prev() {
        return prev(1);
    }

    public Fluent prev(int n) {
        return append(CSI + n + CURSOR_PREV_LINE);
    }

    public Fluent col(int column) {
        return column(column);
    }

    public Fluent column(int column) {
        return append(CSI + column + CURSOR_COLUMN);
    }

    public Fluent hide() {
        return append(Ansi.cursorHide());
    }

    public Fluent show() {
        return append(Ansi.cursorShow());
    }

    public Fluent mark() {
        return append(Ansi.cursorSave());
    }

    public Fluent jump() {
        return append(Ansi.cursorRestore());
    }

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
        public Fluent i() {
            return italic();
        }

        public Fluent italic() {
            Ansi.styles(appendable, NOTITALICIZED);
            currentStyle = currentStyle.italicOff();
            return Fluent.this;
        }

        public Fluent ul() {
            return underline();
        }

        public Fluent underline() {
            Ansi.styles(appendable, NOTUNDERLINED);
            currentStyle = currentStyle.underlinedOff();
            return Fluent.this;
        }

        public Fluent blink() {
            Ansi.styles(appendable, STEADY);
            currentStyle = currentStyle.blinkOff();
            return Fluent.this;
        }

        public Fluent inverse() {
            Ansi.styles(appendable, POSITIVE);
            currentStyle = currentStyle.inverseOff();
            return Fluent.this;
        }

        public Fluent invisible() {
            Ansi.styles(appendable, VISIBLE);
            currentStyle = currentStyle.hiddenOff();
            return Fluent.this;
        }

        public Fluent strikethrough() {
            Ansi.styles(appendable, NOTCROSSEDOUT);
            currentStyle = currentStyle.strikethroughOff();
            return Fluent.this;
        }

        public Fluent wrap() {
            return append(Ansi.autoWrap(false));
        }
    }

    public class BackgroundColors {
        public Fluent black() {
            return color(Color.BasicColor.BLACK);
        }

        public Fluent red() {
            return color(Color.BasicColor.RED);
        }

        public Fluent green() {
            return color(Color.BasicColor.GREEN);
        }

        public Fluent yellow() {
            return color(Color.BasicColor.YELLOW);
        }

        public Fluent blue() {
            return color(Color.BasicColor.BLUE);
        }

        public Fluent magenta() {
            return color(Color.BasicColor.MAGENTA);
        }

        public Fluent cyan() {
            return color(Color.BasicColor.CYAN);
        }

        public Fluent white() {
            return color(Color.BasicColor.WHITE);
        }

        public Fluent color(Color color) {
            return append(color.toAnsiBg());
        }

        public Fluent color(int idx) {
            return color(Color.indexed(idx));
        }

        public Fluent color(int r, int g, int b) {
            return color(Color.rgb(r, g, b));
        }
    }

    public class ScreenCommands {
        public Fluent clear() {
            return append(CSI + SCREEN_ERASE_FULL);
        }

        public Fluent clearToEnd() {
            return append(CSI + SCREEN_ERASE_END);
        }

        public Fluent clearToStart() {
            return append(CSI + SCREEN_ERASE_START);
        }

        public Fluent alternate() {
            return append(CSI + SCREEN_SAVE);
        }

        public Fluent restore() {
            return append(CSI + SCREEN_RESTORE);
        }
    }

    public class LineCommands {
        public Fluent clear() {
            return append(CSI + LINE_ERASE_FULL);
        }

        public Fluent clearToEnd() {
            return append(CSI + LINE_ERASE_END);
        }

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
