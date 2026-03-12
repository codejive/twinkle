package org.codejive.twinkle.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Constants;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.text.util.BufferWriter;
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
            buffer.printAt(i, 0, Style.ITALIC, (char) ('a' + i));
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
            buffer.printAt(i, 0, Style.ITALIC, (char) ('a' + i));
        }
        assertThat(buffer.toString()).isEqualTo("abcdefghij");
    }

    @Test
    public void testBufferPutCharToAnsiString() {
        Buffer buffer = Buffer.of(10, 1);
        for (int i = 0; i < buffer.size().width(); i++) {
            Style style = i < 5 ? Style.ITALIC : Style.UNDERLINED;
            buffer.printAt(i, 0, style, (char) ('a' + i));
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
            buffer.printAt(i, 0, style, (char) ('a' + i));
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
            buffer.printAt(i - 5, 0, style, (char) ('a' + i));
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
    public void testWriterWritesSimpleCharsAndMovesCursor() {
        Buffer buffer = Buffer.of(10, 1);

        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("hello");

            writer.flush(); // Necessary to get up-to-date cursor position
            assertThat(writer.cursorX()).isEqualTo(5);
            assertThat(writer.cursorY()).isEqualTo(0);
        }

        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("h");
        assertThat(buffer.graphemeAt(1, 0)).isEqualTo("e");
        assertThat(buffer.graphemeAt(4, 0)).isEqualTo("o");
        assertThat(buffer.toString()).isEqualTo("hello     ");
    }

    @Test
    public void testWriterHandlesSupplementaryAndGraphemeClusters() {
        Buffer buffer = Buffer.of(5, 1);

        String supplementary = "\uD834\uDD1E"; // MUSICAL SYMBOL G CLEF
        String cluster = "a\u0301"; // 'a' + combining acute accent

        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.printf("%s%sZ", supplementary, cluster);

            writer.flush(); // Necessary to get up-to-date cursor position
            assertThat(writer.cursorX()).isEqualTo(3);
            assertThat(writer.cursorY()).isEqualTo(0);
        }

        assertThat(buffer.graphemeAt(0, 0)).isEqualTo(supplementary);
        assertThat(buffer.graphemeAt(1, 0)).isEqualTo(cluster);
        assertThat(buffer.graphemeAt(2, 0)).isEqualTo("Z");
        assertThat(buffer.toString()).isEqualTo(supplementary + cluster + "Z  ");
    }

    @Test
    public void testWriterRespectsMovedCursorPosition() {
        Buffer buffer = Buffer.of(5, 2);

        BufferWriter writer = buffer.writer();
        writer.style(Style.DEFAULT);
        writer.at(2, 1);
        writer.write("xy");
        writer.flush();

        assertThat(writer.cursorX()).isEqualTo(4);
        assertThat(writer.cursorY()).isEqualTo(1);
        assertThat(buffer.graphemeAt(2, 1)).isEqualTo("x");
        assertThat(buffer.graphemeAt(3, 1)).isEqualTo("y");
        assertThat(buffer.toString()).isEqualTo("     \n  xy ");
    }

    @Test
    public void testWriterAnsiChangesStyleAndDoesNotMoveCursorByItself() {
        Buffer buffer = Buffer.of(5, 1);

        BufferWriter writer = buffer.writer();
        writer.style(Style.DEFAULT);
        writer.write("\u001B[31m");
        writer.flush();

        assertThat(writer.cursorX()).isEqualTo(0);
        assertThat(writer.cursorY()).isEqualTo(0);

        writer.write('R');
        writer.flush();

        assertThat(writer.cursorX()).isEqualTo(1);
        assertThat(writer.cursorY()).isEqualTo(0);
        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("R");
        assertThat(buffer.styleAt(0, 0)).isEqualTo(Style.DEFAULT.fgColor(Color.BasicColor.RED));
    }

    @Test
    public void testWriterPastWidthWrapsWhenLineWrapEnabled() {
        Buffer buffer = Buffer.of(4, 2);

        BufferWriter writer = buffer.writer();
        writer.style(Style.DEFAULT);
        writer.write("abcdef");
        writer.flush();

        assertThat(writer.cursorX()).isEqualTo(2);
        assertThat(writer.cursorY()).isEqualTo(1);
        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("a");
        assertThat(buffer.graphemeAt(3, 0)).isEqualTo("d");
        assertThat(buffer.graphemeAt(0, 1)).isEqualTo("e");
        assertThat(buffer.graphemeAt(1, 1)).isEqualTo("f");
        assertThat(buffer.toString()).isEqualTo("abcd\nef  ");
    }

    @Test
    public void testWriterPastWidthDoesNotWrapWhenLineWrapDisabled() {
        Buffer buffer = Buffer.of(4, 2);

        BufferWriter writer = buffer.writer();
        writer.style(Style.DEFAULT);
        writer.wrap(false);
        writer.write("abcdef");
        writer.flush();

        assertThat(writer.cursorX()).isEqualTo(3);
        assertThat(writer.cursorY()).isEqualTo(0);
        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("a");
        assertThat(buffer.graphemeAt(3, 0)).isEqualTo("d");
        assertThat(buffer.charAt(0, 1)).isEqualTo('\0');
        assertThat(buffer.toString()).isEqualTo("abcd\n    ");
    }

    @Test
    public void testWriterTextWithNewline() {
        Buffer buffer = Buffer.of(4, 2);

        BufferWriter writer = buffer.writer();
        writer.style(Style.DEFAULT);
        writer.at(1, 0);
        writer.write("ab\ncd");
        writer.flush();

        assertThat(writer.cursorX()).isEqualTo(2);
        assertThat(writer.cursorY()).isEqualTo(1);
        assertThat(buffer.graphemeAt(1, 0)).isEqualTo("a");
        assertThat(buffer.graphemeAt(1, 1)).isEqualTo("d");
        assertThat(buffer.toString()).isEqualTo(" ab \ncd  ");
    }

    @Test
    public void testWriterExactWidthCursorPos() {
        Buffer buffer = Buffer.of(4, 2);

        BufferWriter writer = buffer.writer();
        writer.style(Style.DEFAULT);
        writer.write("abcd");
        writer.flush();

        assertThat(writer.cursorX()).isEqualTo(3);
        assertThat(writer.cursorY()).isEqualTo(0);
        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("a");
        assertThat(buffer.graphemeAt(3, 0)).isEqualTo("d");
        assertThat(buffer.toString()).isEqualTo("abcd\n    ");
    }

    @Test
    public void testWriterExactWidthAndNewline() {
        Buffer buffer = Buffer.of(4, 2);

        BufferWriter writer = buffer.writer();
        writer.style(Style.DEFAULT);
        writer.write("abcd\nef");
        writer.flush();

        assertThat(writer.cursorX()).isEqualTo(2);
        assertThat(writer.cursorY()).isEqualTo(1);
        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("a");
        assertThat(buffer.graphemeAt(1, 1)).isEqualTo("f");
        assertThat(buffer.toString()).isEqualTo("abcd\nef  ");
    }

    @Test
    public void testWriterWideCodepointMovesCursorByDisplayWidth() {
        Buffer buffer = Buffer.of(4, 1);

        String wide = "界";
        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write(wide);

            writer.flush(); // Necessary to get up-to-date cursor position
            assertThat(writer.cursorX()).isEqualTo(2);
            assertThat(writer.cursorY()).isEqualTo(0);
        }

        assertThat(buffer.graphemeAt(0, 0)).isEqualTo(wide);
        assertThat(buffer.shouldSkipAt(1, 0)).isTrue(); // Wide character occupies both columns
        assertThat(buffer.toString()).isEqualTo(wide + "  ");
    }

    @Test
    public void testWriterWideCodepointAtBorder() {
        Buffer buffer = Buffer.of(4, 1);

        String wide = "界";
        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.at(3, 0); // Move cursor to last column
            writer.write(wide);

            writer.flush(); // Necessary to get up-to-date cursor position
            assertThat(writer.cursorX()).isEqualTo(3);
            assertThat(writer.cursorY()).isEqualTo(0);
        }

        // The wide character should be returned
        assertThat(buffer.graphemeAt(3, 0)).isEqualTo(wide);
        // But it should not be displayed
        assertThat(buffer.toString()).isEqualTo("    ");
    }

    @Test
    public void testWriterWideCodepointOverwrite() {
        Buffer buffer = Buffer.of(4, 1);

        String wide = "界";
        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write(wide);
            writer.write(wide);
            writer.at(1, 0);
            // This write breaks both other characters!
            writer.write(wide);

            writer.flush(); // Necessary to get up-to-date cursor position
            assertThat(writer.cursorX()).isEqualTo(3);
            assertThat(writer.cursorY()).isEqualTo(0);
        }

        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("\0");
        assertThat(buffer.graphemeAt(1, 0)).isEqualTo(wide);
        assertThat(buffer.isWideAt(1, 0)).isTrue();
        assertThat(buffer.shouldSkipAt(2, 0)).isTrue();
        assertThat(buffer.graphemeAt(3, 0)).isEqualTo("\0");
        assertThat(buffer.toString()).isEqualTo(" " + wide + " ");
    }

    @Test
    public void testWriterWriteToBorder() {
        Buffer buffer = Buffer.of(14, 1);

        try (BufferWriter writer = buffer.writer()) {
            writer.write("this is a test");
            writer.at(0, 0);
        }

        assertThat(buffer.toString()).isEqualTo("this is a test");
    }

    @Test
    public void testWriterTransparantOverwrite() {
        Buffer buffer = Buffer.of(14, 1);

        try (BufferWriter writer = buffer.writer()) {
            writer.write("this is a test");
            writer.at(0, 0);
            writer.write("T\0\0\0\0I\0\0A\0T\0\0\0");
        }

        assertThat(buffer.toString()).isEqualTo("This Is A Test");
    }

    @Test
    public void testWriterCustomTransparantOverwrite() {
        Buffer buffer = Buffer.of(14, 1);

        try (BufferWriter writer = buffer.writer()) {
            writer.write("this is a test");
            writer.at(0, 0);
            writer.transparant(" ");
            writer.write("T    I  A T   ");
        }

        assertThat(buffer.toString()).isEqualTo("This Is A Test");
    }

    @Test
    public void testWriterPrintBlock() {
        Buffer buffer = Buffer.of(7, 5);

        try (BufferWriter writer = buffer.writer()) {
            writer.at(1, 1);
            writer.printBlock("Test1\nTest2\nTest3");
        }

        assertThat(buffer.toString()).isEqualTo("       \n Test1 \n Test2 \n Test3 \n       ");
    }

    @Test
    public void testBufferOverlayCenter() {
        Buffer buffer = Buffer.of(5, 5);
        Buffer buffer2 = Buffer.of(3, 3);

        try (BufferWriter writer = buffer.writer()) {
            writer.write("abcde\nfgehi\njklmn\nopqrs\ntuvwx");
        }

        try (BufferWriter writer = buffer2.writer()) {
            writer.write("123\n4\u00006\n789");
        }

        buffer2.overlayOn(buffer, 1, 1);

        assertThat(buffer.toString()).isEqualTo("abcde\nf123i\nj4l6n\no789s\ntuvwx");
    }

    @Test
    public void testBufferOverlayOffCenter1() {
        Buffer buffer = Buffer.of(5, 5);
        Buffer buffer2 = Buffer.of(3, 3);

        try (BufferWriter writer = buffer.writer()) {
            writer.write("abcde\nfgehi\njklmn\nopqrs\ntuvwx");
        }

        try (BufferWriter writer = buffer2.writer()) {
            writer.write("123\n4\u00006\n789");
        }

        buffer2.overlayOn(buffer, -1, -1);

        assertThat(buffer.toString()).isEqualTo("a6cde\n89ehi\njklmn\nopqrs\ntuvwx");
    }

    @Test
    public void testBufferOverlayOffCenter2() {
        Buffer buffer = Buffer.of(5, 5);
        Buffer buffer2 = Buffer.of(3, 3);

        try (BufferWriter writer = buffer.writer()) {
            writer.write("abcde\nfgehi\njklmn\nopqrs\ntuvwx");
        }

        try (BufferWriter writer = buffer2.writer()) {
            writer.write("123\n4\u00006\n789");
        }

        buffer2.overlayOn(buffer, 3, 3);

        assertThat(buffer.toString()).isEqualTo("abcde\nfgehi\njklmn\nopq12\ntuv4x");
    }

    private Buffer createBuffer() {
        Buffer buffer = Buffer.of(5, 5);
        Size size = buffer.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                buffer.printAt(
                        x,
                        y,
                        Style.ofFgColor(Color.indexed(x)),
                        (char) ('A' + x + y * size.width()));
            }
        }
        return buffer;
    }
}
