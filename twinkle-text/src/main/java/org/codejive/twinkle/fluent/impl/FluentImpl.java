package org.codejive.twinkle.fluent.impl;

import static org.codejive.twinkle.ansi.Constants.*;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Formatter;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.ansi.util.AnsiTricks;
import org.codejive.twinkle.ansi.util.Printable;
import org.codejive.twinkle.fluent.Fluent;
import org.codejive.twinkle.fluent.MarkupParser;
import org.codejive.twinkle.fluent.commands.ColorCommands;
import org.codejive.twinkle.fluent.commands.LineCommands;
import org.codejive.twinkle.fluent.commands.NegatableCommands;
import org.codejive.twinkle.fluent.commands.ScreenCommands;
import org.jspecify.annotations.NonNull;

/**
 * A utility class that provides a fluent API for building ANSI strings. This class is designed to
 * be used in a chainable manner, allowing for easy composition of ANSI styles and commands.
 */
public class FluentImpl implements Fluent {
    protected final Appendable appendable;
    protected final NegatableCommands negatableCommands;
    protected final BackgroundColorCommands backgroundColorCommands;
    protected final ScreenCommands screenCommands;
    protected final LineCommands lineCommands;
    protected final Style startingStyle;
    protected Style currentStyle;
    protected Deque<Style> styleStack;
    protected MarkupParser markupParser;

    public static FluentImpl of(Appendable appendable, Style startingStyle) {
        return new FluentImpl(appendable, startingStyle);
    }

    protected FluentImpl(Appendable appendable, Style startingStyle) {
        this.appendable = appendable;
        this.negatableCommands = new NegatableCommandsImpl();
        this.backgroundColorCommands = new BackgroundColorCommands();
        this.screenCommands = new ScreenCommandsImpl();
        this.lineCommands = new LineCommandsImpl();
        this.startingStyle = startingStyle;
        this.currentStyle = startingStyle;
        this.styleStack = new ArrayDeque<>();
        this.markupParser = new DefaultMarkupParser();
    }

    @Override
    public Fluent markupParser(MarkupParser markupParser) {
        this.markupParser = markupParser;
        return this;
    }

    @Override
    public Style style() {
        return currentStyle;
    }

    @Override
    public Fluent style(Style newStyle) {
        Style diff = currentStyle.diff(newStyle);
        append(diff.toAnsi(currentStyle));
        currentStyle = newStyle;
        return this;
    }

    @Override
    public Fluent push() {
        styleStack.push(currentStyle);
        return this;
    }

    @Override
    public Fluent push(Style newStyle) {
        styleStack.push(currentStyle);
        style(newStyle);
        return this;
    }

    @Override
    public Fluent pop() {
        if (styleStack.isEmpty()) {
            return this;
        }
        Style popped = styleStack.pop();
        Style diff = currentStyle.diff(popped);
        append(diff.toAnsi(currentStyle));
        currentStyle = popped;
        return this;
    }

    @Override
    public FluentImpl text(@NonNull Object obj, Object... args) {
        return append(String.format(String.valueOf(obj), args));
    }

    @Override
    public FluentImpl plain(@NonNull String text) {
        return append(text);
    }

    @Override
    public Fluent markup(@NonNull Object obj, Object... args) {
        StringBuilder sb = new StringBuilder();
        FluentImpl f = new FluentImpl(sb, currentStyle);
        f.markupParser.parse(f, obj.toString());

        Formatter fmt = new Formatter(appendable);
        fmt.format(sb.toString(), args);
        return this;
    }

    @Override
    public Fluent block(@NonNull Printable prt) {
        return block(prt.toAnsi(currentStyle));
    }

    @Override
    public Fluent block(@NonNull Object obj, Object... args) {
        return append(AnsiTricks.blockify(String.format(String.valueOf(obj), args)));
    }

    @Override
    public FluentImpl lf() {
        return append("\n");
    }

    @Override
    public Fluent link(@NonNull String text, @NonNull String url) {
        return url(url).append(text).lru();
    }

    @Override
    public Fluent link(@NonNull String text, @NonNull String url, String id) {
        return url(url, id).append(text).lru();
    }

    @Override
    public FluentImpl url(@NonNull String url) {
        return append(Ansi.link(url));
    }

    @Override
    public FluentImpl url(@NonNull String url, String id) {
        return append(Ansi.link(url, id));
    }

    @Override
    public Fluent lru() {
        return append(Ansi.linkEnd());
    }

    @Override
    public FluentImpl reset() {
        append(Ansi.reset());
        currentStyle = Style.DEFAULT;
        return this;
    }

    @Override
    public Fluent bold() {
        append(Ansi.bold());
        currentStyle = currentStyle.bold();
        return this;
    }

    @Override
    public Fluent faint() {
        append(Ansi.faint());
        currentStyle = currentStyle.faint();
        return this;
    }

    @Override
    public Fluent normal() {
        append(Ansi.normal());
        currentStyle = currentStyle.normal();
        return this;
    }

    @Override
    public Fluent italic() {
        append(Ansi.italic());
        currentStyle = currentStyle.italic();
        return this;
    }

    @Override
    public Fluent underline() {
        append(Ansi.underlined());
        currentStyle = currentStyle.underlined();
        return this;
    }

    @Override
    public Fluent blink() {
        append(Ansi.blink());
        currentStyle = currentStyle.blink();
        return this;
    }

    @Override
    public Fluent inverse() {
        append(Ansi.inverse());
        currentStyle = currentStyle.inverse();
        return this;
    }

    @Override
    public Fluent hidden() {
        append(Ansi.hidden());
        currentStyle = currentStyle.hidden();
        return this;
    }

    @Override
    public Fluent strikethrough() {
        append(Ansi.strikethrough());
        currentStyle = currentStyle.strikethrough();
        return this;
    }

    @Override
    public NegatableCommands not() {
        return negatableCommands;
    }

    @Override
    public FluentImpl color(Color color) {
        append(color.toAnsiFg());
        currentStyle = currentStyle.fgColor(color);
        return this;
    }

    @Override
    public Fluent color(int idx) {
        return color(Color.indexed(idx));
    }

    @Override
    public Fluent color(int r, int g, int b) {
        return color(Color.rgb(r, g, b));
    }

    @Override
    public ColorCommands background() {
        return backgroundColorCommands;
    }

    @Override
    public Fluent home() {
        return append(Ansi.cursorHome());
    }

    @Override
    public FluentImpl at(int x, int y) {
        return append(Ansi.cursorPos(x, y));
    }

    @Override
    public Fluent up(int n) {
        return append(Ansi.cursorUp(n));
    }

    @Override
    public Fluent down(int n) {
        return append(Ansi.cursorDown(n));
    }

    @Override
    public Fluent forward(int n) {
        return append(Ansi.cursorForward(n));
    }

    @Override
    public Fluent backward(int n) {
        return append(Ansi.cursorBackward(n));
    }

    @Override
    public Fluent next(int n) {
        return append(CSI + n + CURSOR_NEXT_LINE_CMD);
    }

    @Override
    public Fluent prev(int n) {
        return append(CSI + n + CURSOR_PREV_LINE_CMD);
    }

    @Override
    public Fluent column(int column) {
        return append(Ansi.cursorToColumn(column));
    }

    @Override
    public Fluent hide() {
        return append(Ansi.cursorHide());
    }

    @Override
    public FluentImpl show() {
        return append(Ansi.cursorShow());
    }

    @Override
    public Fluent mark() {
        return append(Ansi.cursorSave());
    }

    @Override
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
        return append(Ansi.autoWrap());
    }

    @Override
    public FluentImpl restore() {
        Style diff = currentStyle.diff(startingStyle);
        append(diff.toAnsi(currentStyle));
        currentStyle = startingStyle;
        return this;
    }

    @Override
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

    @Override
    public ScreenCommands screen() {
        return screenCommands;
    }

    @Override
    public LineCommands line() {
        return lineCommands;
    }

    public class NegatableCommandsImpl implements NegatableCommands {
        @Override
        public Fluent bold() {
            boolean faint = currentStyle.isFaint();
            append(Ansi.normal());
            if (faint) {
                // We need to do this because "normal" also turns off faint, so we need to turn it
                // back on
                append(Ansi.faint());
                currentStyle.faint();
            }
            return FluentImpl.this;
        }

        @Override
        public Fluent faint() {
            boolean bold = currentStyle.isBold();
            append(Ansi.normal());
            if (bold) {
                // We need to do this because "normal" also turns off bold, so we need to turn it
                // back on
                append(Ansi.bold());
                currentStyle.bold();
            }
            return FluentImpl.this;
        }

        @Override
        public Fluent italic() {
            append(Ansi.italicOff());
            currentStyle = currentStyle.italicOff();
            return FluentImpl.this;
        }

        @Override
        public Fluent underline() {
            append(Ansi.underlinedOff());
            currentStyle = currentStyle.underlinedOff();
            return FluentImpl.this;
        }

        @Override
        public Fluent blink() {
            append(Ansi.blinkOff());
            currentStyle = currentStyle.blinkOff();
            return FluentImpl.this;
        }

        @Override
        public Fluent inverse() {
            append(Ansi.inverseOff());
            currentStyle = currentStyle.inverseOff();
            return FluentImpl.this;
        }

        @Override
        public Fluent hidden() {
            append(Ansi.hiddenOff());
            currentStyle = currentStyle.hiddenOff();
            return FluentImpl.this;
        }

        @Override
        public Fluent strikethrough() {
            append(Ansi.strikethroughOff());
            currentStyle = currentStyle.strikethroughOff();
            return FluentImpl.this;
        }

        @Override
        public Fluent wrap() {
            return append(Ansi.autoWrapOff());
        }
    }

    public class BackgroundColorCommands implements ColorCommands {

        @Override
        public FluentImpl color(Color color) {
            append(color.toAnsiBg());
            currentStyle = currentStyle.bgColor(color);
            return FluentImpl.this;
        }

        @Override
        public Fluent color(int idx) {
            return color(Color.indexed(idx));
        }

        @Override
        public Fluent color(int r, int g, int b) {
            return color(Color.rgb(r, g, b));
        }
    }

    public class ScreenCommandsImpl implements ScreenCommands {
        @Override
        public Fluent clear() {
            return append(CSI + SCREEN_ERASE_FULL);
        }

        @Override
        public Fluent clearToEnd() {
            return append(CSI + SCREEN_ERASE_END);
        }

        @Override
        public Fluent clearToStart() {
            return append(CSI + SCREEN_ERASE_START);
        }

        @Override
        public Fluent alt() {
            return alternate();
        }

        @Override
        public FluentImpl alternate() {
            return append(CSI + SCREEN_SAVE);
        }

        @Override
        public FluentImpl restore() {
            return append(CSI + SCREEN_RESTORE);
        }
    }

    public class LineCommandsImpl implements LineCommands {
        @Override
        public Fluent clear() {
            return append(CSI + LINE_ERASE_FULL);
        }

        @Override
        public Fluent clearToEnd() {
            return append(CSI + LINE_ERASE_END);
        }

        @Override
        public Fluent clearToStart() {
            return append(CSI + LINE_ERASE_START);
        }
    }

    public static class StringFluent extends FluentImpl {
        public StringFluent(Style startingStyle) {
            super(new StringBuilder(), startingStyle);
        }

        @Override
        public String toString() {
            return ((StringBuilder) appendable).toString();
        }
    }

    protected FluentImpl append(String str) {
        try {
            appendable.append(str);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }
}
