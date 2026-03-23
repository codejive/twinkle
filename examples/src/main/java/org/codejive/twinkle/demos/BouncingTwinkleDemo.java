/// usr/bin/env jbang "$0" "$@" ; exit $?

// spotless:off
//DEPS org.codejive.twinkle:twinkle-terminal-aesh:1.0-SNAPSHOT
//DEPS org.codejive.twinkle:twinkle-shapes:1.0-SNAPSHOT
//DEPS com.github.lalyos:jfiglet:0.0.9
// spotless:on

package org.codejive.twinkle.demos;

import com.github.lalyos.jfiglet.FigletFont;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.ansi.util.Printable;
import org.codejive.twinkle.fluent.Fluent;
import org.codejive.twinkle.screen.Buffer;
import org.codejive.twinkle.screen.BufferStack;
import org.codejive.twinkle.screen.io.PrintBufferWriter;
import org.codejive.twinkle.screen.util.FrameCounter;
import org.codejive.twinkle.screen.util.Sized;
import org.codejive.twinkle.shapes.Borders;
import org.codejive.twinkle.terminal.Terminal;
import org.codejive.twinkle.text.Position;
import org.codejive.twinkle.text.Size;
import org.codejive.twinkle.text.Sizer;
import org.jspecify.annotations.NonNull;

class BouncingTwinkleDemo {

    private static final BufferStack buffers = BufferStack.create();
    private static final Buffer helpBuffer = Buffer.of(30, 10);
    private static volatile Size size;
    private static volatile Position textPos;
    private static volatile int dx, dy;
    private static volatile long currentSleep = 50;
    private static volatile Borders.LineStyle lineStyle = Borders.LineStyle.ASCII;
    private static volatile Borders.CornerStyle cornerStyle = Borders.CornerStyle.ASCII;
    private static final FrameCounter fps = new FrameCounter();
    private static final FigletShape text = new FigletShape("TWINKLE");

    private static final String URL = "https://github.com/codejive/twinkle";

    private static final Borders.LineStyle[] borders =
            new Borders.LineStyle[] {
                Borders.LineStyle.ASCII, Borders.LineStyle.SINGLE, Borders.LineStyle.DOUBLE
            };

    private static final Borders.CornerStyle[] corners =
            new Borders.CornerStyle[] {
                Borders.CornerStyle.ASCII, Borders.CornerStyle.ROUND, Borders.CornerStyle.SQUARE
            };

    private static final Color.BasicColor[] textPalette = {
        Color.BasicColor.RED,
        Color.BasicColor.YELLOW,
        Color.BasicColor.BLUE,
        Color.BasicColor.MAGENTA,
        Color.BasicColor.CYAN,
        Color.BasicColor.BRIGHT_RED,
        Color.BasicColor.BRIGHT_YELLOW,
        Color.BasicColor.BRIGHT_BLUE,
        Color.BasicColor.BRIGHT_MAGENTA,
        Color.BasicColor.BRIGHT_CYAN,
        Color.BasicColor.BRIGHT_WHITE
    };

    private static Color.BasicColor textColor = textPalette[0];

    public static void main(String[] args) throws Exception {
        try (Terminal terminal = Terminal.getDefault()) {
            size = terminal.size();

            terminal.onResize(BouncingTwinkleDemo::handleResize);

            try {
                // Hide cursor and clear screen
                Fluent.of(terminal.writer()).screen().alternate().hide().screen().clear().done();

                textPos = size.center(text.size());
                dx = 1;
                dy = 1;

                // Reusable buffer for frame rendering
                Buffer buffer = Buffer.of(size);
                PrintBufferWriter writer = buffer.writer();
                buffers.primary(buffer);

                Reader reader = terminal.reader();
                while (handleKeys(reader)) {
                    buffer.resize(size);

                    bounce();
                    colorize();

                    Borders border =
                            new Borders()
                                    .style(Style.ofFgColor(Color.BasicColor.GREEN))
                                    .lineStyle(lineStyle)
                                    .cornerStyle(cornerStyle);
                    border.render(buffer);

                    Fluent f = writer.fluent();
                    f.at(2, 0).markup("{green}[ {white}%s{green} ]", size);
                    f.at(size.width() / 2 - 3, 0)
                            .markup("{green}[ {blue}{ul}{$1}Twinkle{/}{/ul}{green} ]", URL);
                    f.at(size.width() - 12, 0)
                            .markup("{green}[ {+}{white}fps %s{-} ]", Math.round(fps.average()));
                    f.at(textPos).color(textColor).text(text).done();

                    // Write entire frame buffer to connection in one call
                    terminal.writer().write(Ansi.cursorHome() + buffers.toAnsi());

                    fps.update();
                    Thread.sleep(currentSleep);
                }
            } finally {
                // Show cursor and clear screen on exit
                Fluent.of(terminal.writer())
                        .screen()
                        .restore()
                        .show()
                        .reset()
                        .text("\nGoodbye!\n")
                        .done();
            }
        }
    }

    private static boolean handleKeys(Reader reader) throws IOException {
        int ch = reader.ready() ? reader.read() : -1;
        while (ch >= 0) {
            if (ch == 'q' || ch == 'Q') {
                return false;
            } else if (ch == 'h' || ch == 'H') {
                toggleHelp();
            } else if (ch == 'b' || ch == 'B') {
                // Cycle border styles
                lineStyle = cycle(lineStyle, borders);
                cornerStyle = cycle(cornerStyle, corners);
            } else if (ch == 's' || ch == 'S') {
                // Cycle speeds: 0ms, 1ms, 10ms, 50ms, 100ms
                long nextSleep = cycle(currentSleep, new Long[] {0L, 1L, 10L, 50L, 100L});
                currentSleep = nextSleep;
            }
            ch = reader.ready() ? reader.read() : -1;
        }
        return true;
    }

    private static <T> T cycle(T current, T[] values) {
        int idx = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                idx = i;
                break;
            }
        }
        return values[(idx + 1) % values.length];
    }

    private static void toggleHelp() {
        BufferStack.BufferElement helpElement = buffers.contains(helpBuffer);
        if (helpElement == null) {
            helpElement =
                    buffers.add(helpBuffer, size.center(helpBuffer.size()), Integer.MAX_VALUE);
            helpElement.transparancy = "";
        } else {
            helpElement.visible = !helpElement.visible;
        }
        if (helpElement.visible) {
            drawHelp();
        }
    }

    private static void drawHelp() {
        PrintBufferWriter writer = helpBuffer.writer();
        Borders b =
                new Borders()
                        .lineStyle(lineStyle)
                        .cornerStyle(cornerStyle)
                        .style(Style.ofFgColor(Color.BasicColor.BRIGHT_MAGENTA));
        b.render(helpBuffer);
        writer.fluent().at(2, 0).markup("{brightmagenta}{+}[ {white}Help Page{-} ]").done();
        Fluent help =
                Fluent.string()
                        .white()
                        .text("q - quit\n")
                        .text("h - toggle this help\n")
                        .text("b - cycle border style\n")
                        .text("s - cycle speeds");
        writer.fluent().at(3, 2).block(help).done();
    }

    private static void handleResize(Size newSize) {
        size = newSize;
        BufferStack.BufferElement helpElement = buffers.contains(helpBuffer);
        if (helpElement != null) {
            helpElement.pos = size.center(helpBuffer.size());
        }
    }

    private static void bounce() {
        Size bounceSize = size.shrink(text.size()).shrink(1, 1);
        int textX = textPos.x();
        int textY = textPos.y();

        textX += dx;
        if (textX <= 1 || textX >= bounceSize.width()) {
            textX = Math.max(1, Math.min(textX, bounceSize.width()));
            dx = -dx;
        }

        textY += dy;
        if (textY <= 1 || textY >= bounceSize.height()) {
            textY = Math.max(1, Math.min(textY, bounceSize.height()));
            dy = -dy;
        }

        textPos = Position.of(textX, textY);
    }

    private static void colorize() {
        if (fps.frames() > 0 && fps.frames() % 10 == 0) {
            int idx = ThreadLocalRandom.current().nextInt(textPalette.length);
            textColor = textPalette[idx];
        }
    }

    static class FigletShape extends CachingSupplierShape {
        private String text;

        public FigletShape(String initialText) {
            super(null);
            text(initialText);
        }

        public void text(String text) {
            this.text = text;
            update();
        }

        protected String getUpdated() {
            try {
                return Sizer.trim(FigletFont.convertOneLine(text));
            } catch (Exception e) {
                return "???";
            }
        }
    }

    static class CachingSupplierShape extends SupplierShape {
        protected volatile String contents;
        protected volatile Size size;

        public CachingSupplierShape(Supplier<String> supplier) {
            super(supplier);
            this.contents = null;
            this.size = null;
        }

        public String get() {
            if (contents == null) {
                contents = getUpdated();
                size = null;
            }
            return contents;
        }

        protected void update() {
            contents = null;
            size = null;
        }

        @Override
        public @NonNull Size size() {
            if (size == null) {
                size = Sizer.measure(get());
            }
            return size;
        }
    }

    abstract static class SupplierShape implements Supplier<String>, Printable, Shape {
        protected final Supplier<String> supplier;

        public SupplierShape(Supplier<String> supplier) {
            this.supplier = supplier;
        }

        public String get() {
            if (supplier == null) {
                throw new IllegalStateException(
                        "Internal error: either provide a Supplier or override get()");
            }
            return getUpdated();
        }

        protected String getUpdated() {
            return supplier.get();
        }

        @Override
        public @NonNull Appendable toAnsi(
                @NonNull Appendable appendable, @NonNull Style currentStyle) {
            try {
                return appendable.append(get());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    interface Shape extends Sized {

        default @NonNull Size size() {
            return Size.MAX;
        }

        default void resize(@NonNull Size availableSize) {}
    }
}
