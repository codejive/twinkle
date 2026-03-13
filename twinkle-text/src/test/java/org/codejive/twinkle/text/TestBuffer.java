package org.codejive.twinkle.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Constants;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.text.io.PrintBufferWriter;
import org.codejive.twinkle.text.util.Size;
import org.junit.jupiter.api.Test;

public class TestBuffer {

    @Test
    public void testBufferCreation() {
        Buffer buffer = Buffer.of(10, 5);
        Size size = buffer.size();
        assertThat(size.width()).isEqualTo(10);
        assertThat(size.height()).isEqualTo(5);
    }

    @Test
    public void testBufferPutGetChar() {
        Buffer buffer = Buffer.of(10, 1);
        for (int i = 0; i < buffer.size().width(); i++) {
            buffer.putAt(i, 0, Style.ITALIC, (char) ('a' + i));
        }
        for (int i = 0; i < buffer.size().width(); i++) {
            assertThat(buffer.charAt(i, 0)).isEqualTo((char) ('a' + i));
            assertThat(buffer.styleAt(i, 0)).isEqualTo(Style.DEFAULT.italic());
        }
    }

    @Test
    public void testBufferPutCharToString() {
        Buffer buffer = Buffer.of(10, 1);
        for (int i = 0; i < buffer.size().width(); i++) {
            buffer.putAt(i, 0, Style.ITALIC, (char) ('a' + i));
        }
        assertThat(buffer.toString()).isEqualTo("abcdefghij");
    }

    @Test
    public void testBufferPutCharToAnsiString() {
        Buffer buffer = Buffer.of(10, 1);
        for (int i = 0; i < buffer.size().width(); i++) {
            Style style = i < 5 ? Style.ITALIC : Style.UNDERLINED;
            buffer.putAt(i, 0, style, (char) ('a' + i));
        }
        assertThat(buffer.toAnsi())
                .isEqualTo(
                        Ansi.STYLE_RESET
                                + Ansi.styles(Constants.ITALICIZED)
                                + "abcde"
                                + Ansi.styles(Constants.NOTITALICIZED, Constants.UNDERLINED)
                                + "fghij");
    }

    @Test
    public void testBufferPutCharToAnsiStringWithCurrentStyle() {
        Buffer buffer = Buffer.of(10, 1);
        for (int i = 0; i < buffer.size().width(); i++) {
            Style style = i < 5 ? Style.ITALIC : Style.UNDERLINED;
            buffer.putAt(i, 0, style, (char) ('a' + i));
        }
        assertThat(buffer.toAnsi(Style.DEFAULT.italic()))
                .isEqualTo(
                        "abcde"
                                + Ansi.styles(Constants.NOTITALICIZED, Constants.UNDERLINED)
                                + "fghij");
    }

    @Test
    public void testBufferPutCharToAnsiStringWithUnderAndOverflow() {
        Buffer buffer = Buffer.of(10, 1);
        for (int i = 0; i < buffer.size().width() + 10; i++) {
            Style style = i < 10 ? Style.ITALIC : Style.UNDERLINED;
            buffer.putAt(i - 5, 0, style, (char) ('a' + i));
        }
        assertThat(buffer.toAnsi())
                .isEqualTo(
                        Ansi.STYLE_RESET
                                + Ansi.styles(Constants.ITALICIZED)
                                + "fghij"
                                + Ansi.styles(Constants.NOTITALICIZED, Constants.UNDERLINED)
                                + "klmno");
    }

    @Test
    public void testBufferDefaultInnerContent() {
        Buffer buffer = Buffer.of(10, 5);
        Size size = buffer.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                assertThat(buffer.charAt(x, y)).isEqualTo('\0');
                assertThat(buffer.styleAt(x, y)).isEqualTo(Style.DEFAULT);
            }
        }
    }

    @Test
    public void testBufferDefaultOuterContent() {
        Buffer buffer = Buffer.of(10, 5);
        Size size = buffer.size();
        for (int y = -5; y < size.height() + 5; y++) {
            for (int x = -5; x < size.width() + 5; x++) {
                if (x >= 0 && x < size.width() && y >= 0 && y < size.height()) {
                    continue; // Skip inner content
                }
                assertThat(buffer.charAt(x, y)).isEqualTo(Buffer.REPLACEMENT_CHAR);
                assertThat(buffer.styleAt(x, y)).isEqualTo(Style.UNSTYLED);
            }
        }
    }

    @Test
    public void testBufferNewContents() {
        Buffer buffer = createBuffer();
        Size size = buffer.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                assertThat(buffer.charAt(x, y)).isEqualTo((char) ('A' + x + y * size.width()));
                assertThat(buffer.styleAt(x, y)).isEqualTo(Style.DEFAULT.fgColor(Color.indexed(x)));
            }
        }
    }

    @Test
    public void testBufferOverlayCenter() {
        Buffer buffer = Buffer.of(5, 5);
        Buffer buffer2 = Buffer.of(3, 3);

        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write("abcde\nfgehi\njklmn\nopqrs\ntuvwx");
        }

        try (PrintBufferWriter writer = buffer2.writer()) {
            writer.write("123\n4\u00006\n789");
        }

        buffer2.overlayOn(buffer, 1, 1);

        assertThat(buffer.toString()).isEqualTo("abcde\nf123i\nj4l6n\no789s\ntuvwx");
    }

    @Test
    public void testBufferOverlayOffCenter1() {
        Buffer buffer = Buffer.of(5, 5);
        Buffer buffer2 = Buffer.of(3, 3);

        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write("abcde\nfgehi\njklmn\nopqrs\ntuvwx");
        }

        try (PrintBufferWriter writer = buffer2.writer()) {
            writer.write("123\n4\u00006\n789");
        }

        buffer2.overlayOn(buffer, -1, -1);

        assertThat(buffer.toString()).isEqualTo("a6cde\n89ehi\njklmn\nopqrs\ntuvwx");
    }

    @Test
    public void testBufferOverlayOffCenter2() {
        Buffer buffer = Buffer.of(5, 5);
        Buffer buffer2 = Buffer.of(3, 3);

        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write("abcde\nfgehi\njklmn\nopqrs\ntuvwx");
        }

        try (PrintBufferWriter writer = buffer2.writer()) {
            writer.write("123\n4\u00006\n789");
        }

        buffer2.overlayOn(buffer, 3, 3);

        assertThat(buffer.toString()).isEqualTo("abcde\nfgehi\njklmn\nopq12\ntuv4x");
    }

    @Test
    public void testPrintAtBasic() {
        Buffer buffer = Buffer.of(10, 3);
        buffer.printAt(2, 1, Style.DEFAULT, "hello");

        assertThat(buffer.graphemeAt(2, 1)).isEqualTo("h");
        assertThat(buffer.graphemeAt(3, 1)).isEqualTo("e");
        assertThat(buffer.graphemeAt(6, 1)).isEqualTo("o");
        assertThat(buffer.toString()).isEqualTo("          \n  hello   \n          ");
    }

    @Test
    public void testPrintAtWithStyle() {
        Buffer buffer = Buffer.of(10, 3);
        buffer.printAt(2, 1, Style.BOLD, "test");

        assertThat(buffer.graphemeAt(2, 1)).isEqualTo("t");
        assertThat(buffer.styleAt(2, 1)).isEqualTo(Style.DEFAULT.bold());
        assertThat(buffer.styleAt(3, 1)).isEqualTo(Style.DEFAULT.bold());
        assertThat(buffer.styleAt(5, 1)).isEqualTo(Style.DEFAULT.bold());
    }

    @Test
    public void testPrintAtWithNewline() {
        Buffer buffer = Buffer.of(10, 3);
        buffer.printAt(2, 0, Style.DEFAULT, "ab\ncd");

        assertThat(buffer.graphemeAt(2, 0)).isEqualTo("a");
        assertThat(buffer.graphemeAt(3, 0)).isEqualTo("b");
        assertThat(buffer.graphemeAt(0, 1)).isEqualTo("c");
        assertThat(buffer.graphemeAt(1, 1)).isEqualTo("d");
        assertThat(buffer.toString()).isEqualTo("  ab      \ncd        \n          ");
    }

    @Test
    public void testPrintAtAtOrigin() {
        Buffer buffer = Buffer.of(5, 2);
        buffer.printAt(0, 0, Style.DEFAULT, "test");

        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("t");
        assertThat(buffer.graphemeAt(3, 0)).isEqualTo("t");
        assertThat(buffer.toString()).isEqualTo("test \n     ");
    }

    @Test
    public void testPrintAtPartiallyOutOfBounds() {
        Buffer buffer = Buffer.of(5, 2);
        buffer.printAt(3, 0, Style.DEFAULT, "testing");

        // Only first 2 characters fit at positions 3 and 4
        assertThat(buffer.graphemeAt(3, 0)).isEqualTo("t");
        assertThat(buffer.graphemeAt(4, 0)).isEqualTo("e");
        assertThat(buffer.graphemeAt(0, 1)).isEqualTo("s");
        assertThat(buffer.toString()).isEqualTo("   te\nsting");
    }

    @Test
    public void testPrintAtPartiallyOutOfBoundsNoWrap() {
        Buffer buffer = Buffer.of(5, 2);
        buffer.printAt(3, 0, Style.DEFAULT, "testing", Buffer.SimplePrintOption.NOWRAP);

        // Only first 2 characters fit at positions 3 and 4
        assertThat(buffer.graphemeAt(3, 0)).isEqualTo("t");
        assertThat(buffer.graphemeAt(4, 0)).isEqualTo("e");
        assertThat(buffer.graphemeAt(0, 1)).isEqualTo("\0");
        assertThat(buffer.toString()).isEqualTo("   te\n     ");
    }

    @Test
    public void testPrintAtFullyOutOfBounds() {
        Buffer buffer = Buffer.of(5, 2);
        buffer.printAt(10, 0, Style.DEFAULT, "test");

        assertThat(buffer.toString()).isEqualTo("     \n     ");
    }

    @Test
    public void testPrintAtMultipleNewlines() {
        Buffer buffer = Buffer.of(5, 4);
        buffer.printAt(1, 0, Style.DEFAULT, "a\nb\nc");

        assertThat(buffer.graphemeAt(1, 0)).isEqualTo("a");
        assertThat(buffer.graphemeAt(0, 1)).isEqualTo("b");
        assertThat(buffer.graphemeAt(0, 2)).isEqualTo("c");
        assertThat(buffer.toString()).isEqualTo(" a   \nb    \nc    \n     ");
    }

    @Test
    public void testPrintAtOverwritesExistingContent() {
        Buffer buffer = Buffer.of(10, 2);
        buffer.printAt(0, 0, Style.DEFAULT, "original");
        buffer.printAt(2, 0, Style.DEFAULT, "new");

        // "original" -> o-r-i-g-i-n-a-l at positions 0-7
        // "new" overwrites positions 2-4 -> o-r-n-e-w-n-a-l
        assertThat(buffer.toString()).isEqualTo("ornewnal  \n          ");
    }

    private Buffer createBuffer() {
        Buffer buffer = Buffer.of(5, 5);
        Size size = buffer.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                buffer.putAt(
                        x,
                        y,
                        Style.ofFgColor(Color.indexed(x)),
                        (char) ('A' + x + y * size.width()));
            }
        }
        return buffer;
    }
}
