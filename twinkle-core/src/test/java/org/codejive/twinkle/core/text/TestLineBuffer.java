package org.codejive.twinkle.core.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;
import org.junit.jupiter.api.Test;

public class TestLineBuffer {
    @Test
    public void testStyledBufferCreation() {
        LineBuffer buffer = LineBuffer.of(10);
        assertThat(buffer.length()).isEqualTo(10);
    }

    @Test
    public void testStyledBufferPutGetChar() {
        LineBuffer buffer = LineBuffer.of(10);
        for (int i = 0; i < buffer.length(); i++) {
            buffer.setCharAt(i, Style.ITALIC, (char) ('a' + i));
        }
        for (int i = 0; i < buffer.length(); i++) {
            assertThat(buffer.charAt(i)).isEqualTo((char) ('a' + i));
            assertThat(buffer.styleAt(i)).isEqualTo(Style.DEFAULT.italic());
        }
    }

    @Test
    public void testStyledBufferPutCharToString() {
        LineBuffer buffer = LineBuffer.of(10);
        for (int i = 0; i < buffer.length(); i++) {
            buffer.setCharAt(i, Style.ITALIC, (char) ('a' + i));
        }
        assertThat(buffer.toString()).isEqualTo("abcdefghij");
    }

    @Test
    public void testStyledBufferPutCharToAnsiString() {
        LineBuffer buffer = LineBuffer.of(10);
        for (int i = 0; i < buffer.length(); i++) {
            Style style = i < 5 ? Style.ITALIC : Style.UNDERLINED;
            buffer.setCharAt(i, style, (char) ('a' + i));
        }
        assertThat(buffer.toAnsiString())
                .isEqualTo(
                        Ansi.STYLE_RESET
                                + Ansi.style(Ansi.ITALICIZED)
                                + "abcde"
                                + Ansi.style(Ansi.NOTITALICIZED, Ansi.UNDERLINED)
                                + "fghij");
    }

    @Test
    public void testStyledBufferPutCharToAnsiStringWithCurrentStyle() {
        LineBuffer buffer = LineBuffer.of(10);
        for (int i = 0; i < buffer.length(); i++) {
            Style style = i < 5 ? Style.ITALIC : Style.UNDERLINED;
            buffer.setCharAt(i, style, (char) ('a' + i));
        }
        assertThat(buffer.toAnsiString(Style.DEFAULT.italic()))
                .isEqualTo("abcde" + Ansi.style(Ansi.NOTITALICIZED, Ansi.UNDERLINED) + "fghij");
    }

    @Test
    public void testStyledBufferPutCharToAnsiStringWithUnderAndOverflow() {
        LineBuffer buffer = LineBuffer.of(10);
        for (int i = 0; i < buffer.length() + 10; i++) {
            Style style = i < 10 ? Style.ITALIC : Style.UNDERLINED;
            buffer.setCharAt(i - 5, style, (char) ('a' + i));
        }
        assertThat(buffer.toAnsiString())
                .isEqualTo(
                        Ansi.STYLE_RESET
                                + Ansi.style(Ansi.ITALICIZED)
                                + "fghij"
                                + Ansi.style(Ansi.NOTITALICIZED, Ansi.UNDERLINED)
                                + "klmno");
    }

    @Test
    public void testStyledBufferPutStringGetChar() {
        LineBuffer buffer = LineBuffer.of(10);
        buffer.putStringAt(0, Style.DEFAULT.italic(), "abcdefghij");
        for (int i = 0; i < buffer.length(); i++) {
            assertThat(buffer.charAt(i)).isEqualTo((char) ('a' + i));
            assertThat(buffer.styleAt(i)).isEqualTo(Style.DEFAULT.italic());
        }
    }
}
