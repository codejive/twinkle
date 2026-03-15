/// usr/bin/env jbang "$0" "$@" ; exit $?

// spotless:off
//DEPS org.aesh:terminal-tty:3.4-dev
//DEPS org.codejive.twinkle:twinkle-shapes:1.0-SNAPSHOT
//DEPS com.github.lalyos:jfiglet:0.0.9
// spotless:on

package org.codejive.twinkle.demos;

import com.github.lalyos.jfiglet.FigletFont;
import java.util.concurrent.ThreadLocalRandom;
import org.aesh.terminal.tty.Signal;
import org.aesh.terminal.tty.TerminalConnection;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.ansi.util.AnsiTricks;
import org.codejive.twinkle.ansi.util.Fluent;
import org.codejive.twinkle.shapes.Borders;
import org.codejive.twinkle.text.Buffer;
import org.codejive.twinkle.text.io.PrintBufferWriter;
import org.codejive.twinkle.text.util.FrameCounter;
import org.codejive.twinkle.text.util.Size;
import org.codejive.twinkle.text.util.Sizer;

class BouncingTwinkleDemo {

    private static volatile boolean running = true;
    private static volatile Size size;
    private static volatile Size textSize;
    private static volatile int minX, minY, maxX, maxY, textX, textY, dx, dy;
    private static final FrameCounter fps = new FrameCounter();

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
        try (TerminalConnection connection = new TerminalConnection()) {
            connection.enterRawMode();
            size = Size.of(connection.size().getWidth(), connection.size().getHeight());

            String text = AnsiTricks.blockify(Sizer.trim(FigletFont.convertOneLine("TWINKLE")));
            textSize = Sizer.measure(text);

            // Handle Ctrl+C
            connection.setSignalHandler(
                    signal -> {
                        if (signal == Signal.INT) {
                            running = false;
                        }
                    });

            // Handle any key press to exit
            connection.setStdinHandler(
                    input -> {
                        if (input != null && input.length > 0) {
                            running = false;
                        }
                    });

            connection.setSizeHandler(BouncingTwinkleDemo::handleResize);

            // Start input reading in background
            connection.openNonBlocking();

            // Hide cursor and clear screen
            Fluent.of(connection).hide().screen().clear().done();

            minX = 1;
            minY = 1;
            maxX = Math.max(minX, size.width() - textSize.width() - 1);
            maxY = Math.max(minY, size.height() - textSize.height() - 1);
            textX = Math.max(minX, Math.min((size.width() - textSize.width()) / 2, maxX));
            textY = Math.max(minY, Math.min((size.height() - textSize.height()) / 2, maxY));
            dx = 1;
            dy = 1;

            // Reusable buffer for frame rendering
            Buffer buffer = Buffer.of(size);
            PrintBufferWriter writer = buffer.writer();

            while (running) {
                // Clear buffer for new frame
                buffer.resize(size);
                // buffer.clear();

                bounce();
                colorize();

                Borders.ascii().style(Style.ofFgColor(Color.BasicColor.GREEN)).render(buffer);

                Fluent f = writer.fluent();
                f.at(2, 0).green().text("[ ").white().text(size).green().text(" ]");
                f.at(size.width() / 2 - 3, 0)
                        .green()
                        .text("[ ")
                        .blue()
                        .underline()
                        .link("Twinkle", "https://github.com/codejive/twinkle")
                        .not()
                        .underline()
                        .green()
                        .text(" ]");
                f.at(size.width() - 12, 0)
                        .green()
                        .text("[ ")
                        .white()
                        .text("fps %s", Math.round(fps.average()))
                        .green()
                        .text(" ]");
                f.at(textX, textY).color(textColor).text(text).done();

                // Write entire frame buffer to connection in one call
                connection.write(Ansi.cursorHome() + buffer.toAnsi());

                fps.update();
                Thread.sleep(50);
            }
            Fluent.of(connection).show().reset().text("\nGoodbye!\n").done();
        }
    }

    private static void handleResize(org.aesh.terminal.tty.Size newSize) {
        size = Size.of(newSize.getWidth(), newSize.getHeight());
        maxX = Math.max(minX, size.width() - textSize.width() - 1);
        maxY = Math.max(minY, size.height() - textSize.height() - 1);
    }

    private static void bounce() {
        textX = Math.max(minX, Math.min(textX, maxX));
        textY = Math.max(minY, Math.min(textY, maxY));

        if (maxX > minX) {
            textX += dx;
            if (textX <= minX || textX >= maxX) {
                textX = Math.max(minX, Math.min(textX, maxX));
                dx = -dx;
            }
        }

        if (maxY > minY) {
            textY += dy;
            if (textY <= minY || textY >= maxY) {
                textY = Math.max(minY, Math.min(textY, maxY));
                dy = -dy;
            }
        }
    }

    private static void colorize() {
        if (fps.frames() > 0 && fps.frames() % 10 == 0) {
            int idx = ThreadLocalRandom.current().nextInt(textPalette.length);
            textColor = textPalette[idx];
        }
    }
}
