package org.codejive.twinkle.text.io;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.text.Buffer;
import org.junit.jupiter.api.Test;

public class TestBufferWriter {

    @Test
    public void testWriterWritesSimpleCharsAndMovesCursor() {
        Buffer buffer = Buffer.of(10, 1);

        try (PrintBufferWriter writer = buffer.writer()) {
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

        try (PrintBufferWriter writer = buffer.writer()) {
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

        PrintBufferWriter writer = buffer.writer();
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

        PrintBufferWriter writer = buffer.writer();
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

        PrintBufferWriter writer = buffer.writer();
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

        PrintBufferWriter writer = buffer.writer();
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

        PrintBufferWriter writer = buffer.writer();
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

        PrintBufferWriter writer = buffer.writer();
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

        PrintBufferWriter writer = buffer.writer();
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
        try (PrintBufferWriter writer = buffer.writer()) {
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
        try (PrintBufferWriter writer = buffer.writer()) {
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
        try (PrintBufferWriter writer = buffer.writer()) {
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

        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write("this is a test");
            writer.at(0, 0);
        }

        assertThat(buffer.toString()).isEqualTo("this is a test");
    }

    @Test
    public void testWriterTransparantOverwrite() {
        Buffer buffer = Buffer.of(14, 1);

        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write("this is a test");
            writer.at(0, 0);
            writer.write("T\0\0\0\0I\0\0A\0T\0\0\0");
        }

        assertThat(buffer.toString()).isEqualTo("This Is A Test");
    }

    @Test
    public void testWriterCustomTransparantOverwrite() {
        Buffer buffer = Buffer.of(14, 1);

        try (PrintBufferWriter writer = buffer.writer()) {
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

        try (PrintBufferWriter writer = buffer.writer()) {
            writer.at(1, 1);
            writer.printBlock("Test1\nTest2\nTest3");
        }

        assertThat(buffer.toString()).isEqualTo("       \n Test1 \n Test2 \n Test3 \n       ");
    }
}
