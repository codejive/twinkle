package org.codejive.twinkle.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.ansi.util.StyleBuilder;
import org.codejive.twinkle.screen.Buffer;
import org.codejive.twinkle.screen.io.PrintBufferWriter;
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
            buffer.putAt(i, 0, (char) ('a' + i), Buffer.styleOpt(Style.ITALIC));
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
            buffer.putAt(i, 0, (char) ('a' + i), Buffer.styleOpt(Style.ITALIC));
        }
        assertThat(buffer.toString()).isEqualTo("abcdefghij");
    }

    @Test
    public void testBufferPutCharToAnsiString() {
        Buffer buffer = Buffer.of(10, 1);
        for (int i = 0; i < buffer.size().width(); i++) {
            Style style = i < 5 ? Style.ITALIC : Style.UNDERLINED;
            buffer.putAt(i, 0, (char) ('a' + i), Buffer.styleOpt(style));
        }
        assertThat(buffer.toAnsi())
                .isEqualTo(
                        Ansi.reset()
                                + Ansi.italic()
                                + "abcde"
                                + StyleBuilder.compact(Ansi.italicOff(), Ansi.underlined())
                                + "fghij");
    }

    @Test
    public void testBufferPutCharToAnsiStringWithCurrentStyle() {
        Buffer buffer = Buffer.of(10, 1);
        for (int i = 0; i < buffer.size().width(); i++) {
            Style style = i < 5 ? Style.ITALIC : Style.UNDERLINED;
            buffer.putAt(i, 0, (char) ('a' + i), Buffer.styleOpt(style));
        }
        assertThat(buffer.toAnsi(Style.DEFAULT.italic()))
                .isEqualTo(
                        "abcde"
                                + StyleBuilder.compact(Ansi.italicOff(), Ansi.underlined())
                                + "fghij");
    }

    @Test
    public void testBufferPutCharToAnsiStringWithUnderAndOverflow() {
        Buffer buffer = Buffer.of(10, 1);
        for (int i = 0; i < buffer.size().width() + 10; i++) {
            Style style = i < 10 ? Style.ITALIC : Style.UNDERLINED;
            buffer.putAt(i - 5, 0, (char) ('a' + i), Buffer.styleOpt(style));
        }
        assertThat(buffer.toAnsi())
                .isEqualTo(
                        Ansi.reset()
                                + Ansi.italic()
                                + "fghij"
                                + StyleBuilder.compact(Ansi.italicOff(), Ansi.underlined())
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
        buffer.printAt(2, 1, "hello", Buffer.StylePrintOption.DEFAULT);

        assertThat(buffer.graphemeAt(2, 1)).isEqualTo("h");
        assertThat(buffer.graphemeAt(3, 1)).isEqualTo("e");
        assertThat(buffer.graphemeAt(6, 1)).isEqualTo("o");
        assertThat(buffer.toString()).isEqualTo("          \n  hello   \n          ");
    }

    @Test
    public void testPrintAtWithStyle() {
        Buffer buffer = Buffer.of(10, 3);
        buffer.printAt(2, 1, "test", Buffer.styleOpt(Style.BOLD));

        assertThat(buffer.graphemeAt(2, 1)).isEqualTo("t");
        assertThat(buffer.styleAt(2, 1)).isEqualTo(Style.DEFAULT.bold());
        assertThat(buffer.styleAt(3, 1)).isEqualTo(Style.DEFAULT.bold());
        assertThat(buffer.styleAt(5, 1)).isEqualTo(Style.DEFAULT.bold());
    }

    @Test
    public void testPrintAtWithNewline() {
        Buffer buffer = Buffer.of(10, 3);
        buffer.printAt(2, 0, "ab\ncd", Buffer.StylePrintOption.DEFAULT);

        assertThat(buffer.graphemeAt(2, 0)).isEqualTo("a");
        assertThat(buffer.graphemeAt(3, 0)).isEqualTo("b");
        assertThat(buffer.graphemeAt(0, 1)).isEqualTo("c");
        assertThat(buffer.graphemeAt(1, 1)).isEqualTo("d");
        assertThat(buffer.toString()).isEqualTo("  ab      \ncd        \n          ");
    }

    @Test
    public void testPrintAtAtOrigin() {
        Buffer buffer = Buffer.of(5, 2);
        buffer.printAt(0, 0, "test", Buffer.StylePrintOption.DEFAULT);

        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("t");
        assertThat(buffer.graphemeAt(3, 0)).isEqualTo("t");
        assertThat(buffer.toString()).isEqualTo("test \n     ");
    }

    @Test
    public void testPrintAtPartiallyOutOfBounds() {
        Buffer buffer = Buffer.of(5, 2);
        buffer.printAt(3, 0, "testing", Buffer.StylePrintOption.DEFAULT);

        // Only first 2 characters fit at positions 3 and 4
        assertThat(buffer.graphemeAt(3, 0)).isEqualTo("t");
        assertThat(buffer.graphemeAt(4, 0)).isEqualTo("e");
        assertThat(buffer.graphemeAt(0, 1)).isEqualTo("s");
        assertThat(buffer.toString()).isEqualTo("   te\nsting");
    }

    @Test
    public void testPrintAtPartiallyOutOfBoundsNoWrap() {
        Buffer buffer = Buffer.of(5, 2);
        buffer.printAt(
                3, 0, "testing", Buffer.SimplePrintOption.NOWRAP, Buffer.StylePrintOption.DEFAULT);

        // Only first 2 characters fit at positions 3 and 4
        assertThat(buffer.graphemeAt(3, 0)).isEqualTo("t");
        assertThat(buffer.graphemeAt(4, 0)).isEqualTo("e");
        assertThat(buffer.graphemeAt(0, 1)).isEqualTo("\0");
        assertThat(buffer.toString()).isEqualTo("   te\n     ");
    }

    @Test
    public void testPrintAtFullyOutOfBounds() {
        Buffer buffer = Buffer.of(5, 2);
        buffer.printAt(10, 0, "test", Buffer.StylePrintOption.DEFAULT);

        assertThat(buffer.toString()).isEqualTo("     \n     ");
    }

    @Test
    public void testPrintAtMultipleNewlines() {
        Buffer buffer = Buffer.of(5, 4);
        buffer.printAt(1, 0, "a\nb\nc", Buffer.StylePrintOption.DEFAULT);

        assertThat(buffer.graphemeAt(1, 0)).isEqualTo("a");
        assertThat(buffer.graphemeAt(0, 1)).isEqualTo("b");
        assertThat(buffer.graphemeAt(0, 2)).isEqualTo("c");
        assertThat(buffer.toString()).isEqualTo(" a   \nb    \nc    \n     ");
    }

    @Test
    public void testPrintAtOverwritesExistingContent() {
        Buffer buffer = Buffer.of(10, 2);
        buffer.printAt(0, 0, "original", Buffer.StylePrintOption.DEFAULT);
        buffer.printAt(2, 0, "new", Buffer.StylePrintOption.DEFAULT);

        // "original" -> o-r-i-g-i-n-a-l at positions 0-7
        // "new" overwrites positions 2-4 -> o-r-n-e-w-n-a-l
        assertThat(buffer.toString()).isEqualTo("ornewnal  \n          ");
    }

    @Test
    public void testPutAtWithHyperlink() {
        Buffer buffer = Buffer.of(4, 1);
        buffer.putAt(0, 0, 'a', Buffer.linkOpt("https://example.com"));
        buffer.putAt(1, 0, 'b', Buffer.linkOpt("https://example.com"));
        buffer.putAt(2, 0, 'c');
        assertThat(buffer.toAnsi(Style.DEFAULT))
                .isEqualTo(Ansi.link("https://example.com") + "ab" + Ansi.linkEnd() + "c ");
    }

    @Test
    public void testPutAtWithHyperlinkAndId() {
        Buffer buffer = Buffer.of(4, 1);
        buffer.putAt(0, 0, 'a', Buffer.linkOpt("https://example.com", "myid"));
        buffer.putAt(1, 0, 'b', Buffer.linkOpt("https://example.com", "myid"));
        assertThat(buffer.toAnsi(Style.DEFAULT))
                .isEqualTo(Ansi.link("https://example.com", "myid") + "ab" + Ansi.linkEnd() + "  ");
    }

    @Test
    public void testPrintAtWithHyperlink() {
        Buffer buffer = Buffer.of(16, 1);
        String linkStart = Ansi.link("https://example.com");
        buffer.printAt(0, 0, linkStart + "link text" + Ansi.linkEnd() + "normal");
        assertThat(buffer.toAnsi(Style.DEFAULT))
                .isEqualTo(linkStart + "link text" + Ansi.linkEnd() + "normal ");
    }

    @Test
    public void testMultipleHyperlinksInBuffer() {
        Buffer buffer = Buffer.of(14, 1);
        String link1Start = Ansi.link("https://first.com");
        String linkEnd = Ansi.linkEnd();
        String link2Start = Ansi.link("https://second.com");

        buffer.printAt(
                0, 0, link1Start + "first" + linkEnd + " " + link2Start + "second" + linkEnd);

        assertThat(buffer.toAnsi(Style.DEFAULT))
                .isEqualTo(
                        link1Start
                                + "first"
                                + linkEnd
                                + " "
                                + link2Start
                                + "second"
                                + linkEnd
                                + "  ");
    }

    @Test
    public void testHyperlinkWithStyle() {
        Buffer buffer = Buffer.of(8, 1);
        String linkStart = Ansi.link("https://example.com");
        Color red = Color.BasicColor.RED;
        String redStyle = red.toAnsiFg();

        buffer.printAt(0, 0, linkStart + redStyle + "styled" + Ansi.linkEnd());

        assertThat(buffer.toAnsi(Style.DEFAULT))
                .isEqualTo(
                        redStyle
                                + linkStart
                                + "styled"
                                + Ansi.defaultForeground()
                                + Ansi.linkEnd()
                                + "  ");
    }

    @Test
    public void testHyperlinkOverwrite() {
        Buffer buffer = Buffer.of(10, 1);
        // First write with link
        buffer.putAt(0, 0, 'a', Buffer.linkOpt("https://first.com"));
        buffer.putAt(1, 0, 'b', Buffer.linkOpt("https://first.com"));

        // Overwrite with different link
        buffer.putAt(0, 0, 'x', Buffer.linkOpt("https://second.com"));

        String ansi = buffer.toAnsi(Style.DEFAULT);
        // First character should have second link, second character should have first link
        assertThat(ansi).contains("https://second.com");
        assertThat(ansi).contains("https://first.com");
        assertThat(ansi).startsWith(Ansi.link("https://second.com"));
    }

    @Test
    public void testClearRemovesHyperlink() {
        Buffer buffer = Buffer.of(5, 1);
        buffer.putAt(0, 0, 'a', Buffer.linkOpt("https://example.com"));
        buffer.putAt(1, 0, 'b', Buffer.linkOpt("https://example.com"));

        buffer.clearAt(0, 0);

        String ansi = buffer.toAnsi(Style.DEFAULT);
        // Only the second character should still have the link
        assertThat(ansi).contains("https://example.com");
        // But not at the start (since first char was cleared)
        assertThat(ansi).startsWith(" ");
    }

    @Test
    public void testResizeLargerPreservesContentAndInitializesNewArea() {
        Buffer buffer = createBuffer();

        buffer.resize(Size.of(7, 6));

        assertThat(buffer.size()).isEqualTo(Size.of(7, 6));

        // Existing 5x5 region should remain unchanged.
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                assertThat(buffer.charAt(x, y)).isEqualTo((char) ('A' + x + y * 5));
                assertThat(buffer.styleAt(x, y)).isEqualTo(Style.DEFAULT.fgColor(Color.indexed(x)));
            }
        }

        // Newly added area should be in default state.
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 7; x++) {
                if (x < 5 && y < 5) {
                    continue;
                }
                assertThat(buffer.charAt(x, y)).isEqualTo('\0');
                assertThat(buffer.styleAt(x, y)).isEqualTo(Style.DEFAULT);
            }
        }
    }

    @Test
    public void testResizeSmallerTruncatesAndKeepsVisibleRegion() {
        Buffer buffer = createBuffer();

        buffer.resize(Size.of(3, 2));

        assertThat(buffer.size()).isEqualTo(Size.of(3, 2));

        // Top-left region that still fits should remain unchanged.
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 3; x++) {
                assertThat(buffer.charAt(x, y)).isEqualTo((char) ('A' + x + y * 5));
                assertThat(buffer.styleAt(x, y)).isEqualTo(Style.DEFAULT.fgColor(Color.indexed(x)));
            }
        }

        // Data outside the new bounds should no longer be addressable.
        assertThat(buffer.charAt(3, 0)).isEqualTo(Buffer.REPLACEMENT_CHAR);
        assertThat(buffer.charAt(0, 2)).isEqualTo(Buffer.REPLACEMENT_CHAR);
        assertThat(buffer.styleAt(3, 0)).isEqualTo(Style.UNSTYLED);
        assertThat(buffer.styleAt(0, 2)).isEqualTo(Style.UNSTYLED);
    }

    @Test
    public void testResizeBoundaryWritesUseNewDimensions() {
        Buffer buffer = Buffer.of(2, 2);

        buffer.resize(Size.of(4, 3));

        // New bottom-right coordinate should be writable after growing.
        buffer.putAt(3, 2, 'Z', Buffer.styleOpt(Style.BOLD));
        assertThat(buffer.charAt(3, 2)).isEqualTo('Z');
        assertThat(buffer.styleAt(3, 2)).isEqualTo(Style.DEFAULT.bold());

        // Out-of-bounds writes must still be ignored.
        buffer.putAt(4, 2, 'X');
        buffer.putAt(3, 3, 'Y');
        assertThat(buffer.charAt(3, 2)).isEqualTo('Z');

        buffer.resize(Size.of(2, 1));

        // New bottom-right coordinate after shrinking should be writable.
        buffer.putAt(1, 0, 'Q');
        assertThat(buffer.charAt(1, 0)).isEqualTo('Q');

        // Writes past shrunken bounds should be ignored.
        buffer.putAt(2, 0, 'R');
        buffer.putAt(1, 1, 'S');
        assertThat(buffer.charAt(1, 0)).isEqualTo('Q');
    }

    private Buffer createBuffer() {
        Buffer buffer = Buffer.of(5, 5);
        Size size = buffer.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                buffer.putAt(
                        x,
                        y,
                        (char) ('A' + x + y * size.width()),
                        Buffer.styleOpt(Style.ofFgColor(Color.indexed(x))));
            }
        }
        return buffer;
    }
}
