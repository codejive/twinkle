package org.codejive.twinkle.core.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;
import org.junit.jupiter.api.Test;

public class TestStyledBuffer {
    @Test
    public void testStyledBufferCreation() {
        StyledBuffer buffer = StyledBuffer.of(10);
        assertThat(buffer.length()).isEqualTo(10);
    }

    @Test
    public void testStyledBufferPutGetChar() {
        StyledBuffer buffer = StyledBuffer.of(10);
        for (int i = 0; i < buffer.length(); i++) {
            buffer.setCharAt(i, Style.ITALIC.state(), (char) ('a' + i));
        }
        for (int i = 0; i < buffer.length(); i++) {
            assertThat(buffer.charAt(i)).isEqualTo((char) ('a' + i));
            assertThat(buffer.styleStateAt(i)).isEqualTo(Style.ITALIC.state());
        }
    }

    @Test
    public void testStyledBufferPutCharToString() {
        StyledBuffer buffer = StyledBuffer.of(10);
        for (int i = 0; i < buffer.length(); i++) {
            buffer.setCharAt(i, Style.ITALIC.state(), (char) ('a' + i));
        }
        assertThat(buffer.toString()).isEqualTo("abcdefghij");
    }

    @Test
    public void testStyledBufferPutCharToAnsiString() {
        StyledBuffer buffer = StyledBuffer.of(10);
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
        StyledBuffer buffer = StyledBuffer.of(10);
        for (int i = 0; i < buffer.length(); i++) {
            Style style = i < 5 ? Style.ITALIC : Style.UNDERLINED;
            buffer.setCharAt(i, style, (char) ('a' + i));
        }
        assertThat(buffer.toAnsiString(Style.F_ITALIC))
                .isEqualTo("abcde" + Ansi.style(Ansi.NOTITALICIZED, Ansi.UNDERLINED) + "fghij");
    }

    @Test
    public void testStyledBufferPutCharToAnsiStringWithUnderAndOverflow() {
        StyledBuffer buffer = StyledBuffer.of(10);
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
        StyledBuffer buffer = StyledBuffer.of(10);
        buffer.putStringAt(0, Style.ITALIC, "abcdefghij");
        for (int i = 0; i < buffer.length(); i++) {
            assertThat(buffer.charAt(i)).isEqualTo((char) ('a' + i));
            assertThat(buffer.styleStateAt(i)).isEqualTo(Style.ITALIC.state());
        }
    }

    @Test
    public void testStyledBufferPutStyledString() {
        StyledBuffer buffer = StyledBuffer.of(10);
        buffer.putStringAt(0, StyledStringBuilder.of(Style.ITALIC, "abcdefghij"));
        assertThat(buffer.toAnsiString())
                .isEqualTo(Ansi.STYLE_RESET + Ansi.style(Ansi.ITALICIZED) + "abcdefghij");
    }

    @Test
    public void testStyledBufferPutStyledStringWithUnderAndOverflow() {
        StyledBuffer buffer = StyledBuffer.of(10);
        buffer.putStringAt(-5, StyledStringBuilder.of(Style.ITALIC, "xxxxxabcdefghijxxxxx"));
        assertThat(buffer.toAnsiString())
                .isEqualTo(Ansi.STYLE_RESET + Ansi.style(Ansi.ITALICIZED) + "abcdefghij");
    }
}
