package org.codejive.twinkle.ansi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class TestStyle {
    @Test
    public void testStyleCreation() {
        Style style1 = Style.of(Style.F_BOLD);
        Style style2 = Style.BOLD;

        assertThat(style1).isEqualTo(style2);
        assertThat(style1.state()).isEqualTo(style2.state());

        Style style3 = Style.FAINT;

        assertThat(style1).isNotEqualTo(style3);
    }

    @Test
    public void testStyleCombination() {
        Style style =
                Style.UNSTYLED
                        .bold()
                        .faint()
                        .italic()
                        .underlined()
                        .blink()
                        .inverse()
                        .hidden()
                        .strikethrough();
        assertThat(style.isBold()).isTrue();
        assertThat(style.isFaint()).isTrue();
        assertThat(style.isItalic()).isTrue();
        assertThat(style.isUnderlined()).isTrue();
        assertThat(style.isBlink()).isTrue();
        assertThat(style.isInverse()).isTrue();
        assertThat(style.isHidden()).isTrue();
        assertThat(style.isStrikethrough()).isTrue();

        style =
                style.normal()
                        .italicOff()
                        .underlinedOff()
                        .blinkOff()
                        .inverseOff()
                        .hiddenOff()
                        .strikethroughOff();
        assertThat(style).isEqualTo(Style.UNSTYLED);
    }

    @Test
    public void testBasicColorStyles() {
        Style style = Style.UNSTYLED;
        assertThat(style.fgColor()).isEqualTo(Color.DEFAULT);
        assertThat(style.bgColor()).isEqualTo(Color.DEFAULT);

        style = style.fgColor(Color.BasicColor.BLUE);
        assertThat(style.fgColor()).isEqualTo(Color.BasicColor.BLUE);

        style = style.bgColor(Color.BasicColor.RED);
        assertThat(style.bgColor()).isEqualTo(Color.BasicColor.RED);
    }

    @Test
    public void testIndexedColorStyles() {
        for (int i = 0; i < 256; i++) {
            Style style = Style.ofFgColor(Color.indexed(i)).bgColor(Color.indexed(255 - i));
            assertThat(style.fgColor()).isEqualTo(Color.indexed(i));
            assertThat(style.bgColor()).isEqualTo(Color.indexed(255 - i));
        }
    }

    @Test
    public void testRgbColorStyles() {
        Style style =
                Style.UNSTYLED.fgColor(Color.rgb(100, 150, 200)).bgColor(Color.rgb(50, 75, 125));
        assertThat(style.fgColor()).isEqualTo(Color.rgb(100, 150, 200));
        assertThat(style.bgColor()).isEqualTo(Color.rgb(50, 75, 125));
    }

    @Test
    public void testMixedStyles() {
        Style style =
                Style.UNSTYLED
                        .bold()
                        .faint()
                        .italic()
                        .underlined()
                        .blink()
                        .inverse()
                        .hidden()
                        .strikethrough();
        style = style.fgColor(Color.BasicColor.BLUE);
        style = style.bgColor(Color.indexed(128));
        style =
                style.normal()
                        .italicOff()
                        .underlinedOff()
                        .blinkOff()
                        .inverseOff()
                        .hiddenOff()
                        .strikethroughOff();
        style = style.fgColor(Color.DEFAULT);
        style = style.bgColor(Color.DEFAULT);
        assertThat(style).isEqualTo(Style.UNSTYLED);
    }

    @Test
    public void testToAnsiStringUnstyled() {
        Style style = Style.UNSTYLED;
        String ansiCode = style.toAnsiString();
        assertThat(ansiCode).isEqualTo("");
    }

    @Test
    public void testToAnsiStringAllStyles() {
        Style style =
                Style.UNSTYLED
                        .bold()
                        .faint()
                        .italic()
                        .underlined()
                        .blink()
                        .inverse()
                        .hidden()
                        .strikethrough();
        String ansiCode = style.toAnsiString();
        assertThat(ansiCode)
                .isEqualTo(
                        Ansi.style(
                                Ansi.BOLD,
                                Ansi.FAINT,
                                Ansi.ITALICIZED,
                                Ansi.UNDERLINED,
                                Ansi.BLINK,
                                Ansi.INVERSE,
                                Ansi.INVISIBLE,
                                Ansi.CROSSEDOUT));
    }

    @Test
    public void testToAnsiStringAllStylesWithCurrent() {
        Style style =
                Style.UNSTYLED
                        .bold()
                        .faint()
                        .italic()
                        .underlined()
                        .blink()
                        .inverse()
                        .hidden()
                        .strikethrough();
        Style currentStyle = Style.UNSTYLED.bold().underlined();
        String ansiCode = style.toAnsiString(currentStyle);
        assertThat(ansiCode)
                .isEqualTo(
                        Ansi.style(
                                Ansi.NORMAL,
                                Ansi.BOLD,
                                Ansi.FAINT,
                                Ansi.ITALICIZED,
                                Ansi.BLINK,
                                Ansi.INVERSE,
                                Ansi.INVISIBLE,
                                Ansi.CROSSEDOUT));
    }

    @Test
    public void testToAnsiStringAllStylesWithCurrent2() {
        Style style =
                Style.UNSTYLED
                        .bold()
                        .faint()
                        .italic()
                        .underlined()
                        .blink()
                        .inverse()
                        .hidden()
                        .strikethrough();
        Style currentStyle = Style.UNSTYLED.bold().faint().underlined();
        String ansiCode = style.toAnsiString(currentStyle);
        assertThat(ansiCode)
                .isEqualTo(
                        Ansi.style(
                                Ansi.ITALICIZED,
                                Ansi.BLINK,
                                Ansi.INVERSE,
                                Ansi.INVISIBLE,
                                Ansi.CROSSEDOUT));
    }
}
