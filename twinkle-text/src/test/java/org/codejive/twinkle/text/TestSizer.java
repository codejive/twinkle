package org.codejive.twinkle.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Constants;
import org.junit.jupiter.api.Test;

public class TestSizer {

    @Test
    public void testMeasureEmptyString() {
        assertThat(Sizer.measure("")).isEqualTo(Size.of(0, 0));
    }

    @Test
    public void testMeasureSingleLineString() {
        assertThat(Sizer.measure("hello")).isEqualTo(Size.of(5, 1));
    }

    @Test
    public void testMeasureWithSeveralLineFeeds() {
        assertThat(Sizer.measure("ab\nc\ndef")).isEqualTo(Size.of(3, 3));
    }

    @Test
    public void testMeasureWithLineFeedsOnEmptyLines() {
        assertThat(Sizer.measure("\nalpha\n\n")).isEqualTo(Size.of(5, 4));
    }

    @Test
    public void testMeasureIgnoresAnsiEscapeSequenceWidth() {
        String red = Constants.CSI + "31m";
        String reset = Ansi.STYLE_RESET;

        assertThat(Sizer.measure(red + "red" + reset)).isEqualTo(Size.of(3, 1));
    }

    @Test
    public void testMeasureIgnoresAnsiEscapeSequenceWidthAcrossLines() {
        String green = Constants.CSI + "32m";
        String reset = Ansi.STYLE_RESET;
        String text = "A" + green + "B" + reset + "\n\n" + green + "CD" + reset;

        assertThat(Sizer.measure(text)).isEqualTo(Size.of(2, 3));
    }

    @Test
    public void testMeasureWithOnlyWhitespaceLines() {
        assertThat(Sizer.trim("   \n\t\n ")).isEqualTo("");
    }

    @Test
    public void testMeasureTrimsEachLine() {
        assertThat(Sizer.trim("  a  \n  b  ")).isEqualTo("a\nb");
    }

    @Test
    public void testMeasurePreservesInteriorEmptyLinesAfterTrimming() {
        assertThat(Sizer.trim("  one\n   \n two  ")).isEqualTo("one\n\ntwo");
    }
}
