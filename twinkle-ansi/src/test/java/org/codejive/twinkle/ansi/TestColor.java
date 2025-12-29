package org.codejive.twinkle.ansi;

import static org.assertj.core.api.Assertions.*;
import static org.codejive.twinkle.ansi.Ansi.*;

import org.codejive.twinkle.ansi.Color.*;
import org.junit.jupiter.api.Test;

public class TestColor {

    @Test
    public void testDefaultColorCodes() {
        assertThat(Color.DEFAULT.toAnsiFg()).isEqualTo(Ansi.style(Ansi.DEFAULT_FOREGROUND));
        assertThat(Color.DEFAULT.toAnsiBg()).isEqualTo(Ansi.style(Ansi.DEFAULT_BACKGROUND));
    }

    @Test
    public void testBasicColorCodes() {
        // Basic foreground colors
        assertThat(BasicColor.BLACK.toAnsiFg()).isEqualTo(Ansi.style(Ansi.foreground(Ansi.BLACK)));
        assertThat(BasicColor.RED.toAnsiFg()).isEqualTo(Ansi.style(Ansi.foreground(Ansi.RED)));
        assertThat(BasicColor.GREEN.toAnsiFg()).isEqualTo(Ansi.style(Ansi.foreground(Ansi.GREEN)));
        assertThat(BasicColor.YELLOW.toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foreground(Ansi.YELLOW)));
        assertThat(BasicColor.BLUE.toAnsiFg()).isEqualTo(Ansi.style(Ansi.foreground(Ansi.BLUE)));
        assertThat(BasicColor.MAGENTA.toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foreground(Ansi.MAGENTA)));
        assertThat(BasicColor.CYAN.toAnsiFg()).isEqualTo(Ansi.style(Ansi.foreground(Ansi.CYAN)));
        assertThat(BasicColor.WHITE.toAnsiFg()).isEqualTo(Ansi.style(Ansi.foreground(Ansi.WHITE)));

        // Basic foreground colors - dark variants
        assertThat(BasicColor.BLACK.dark().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundDark(BLACK)));
        assertThat(BasicColor.RED.dark().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundDark(Ansi.RED)));
        assertThat(BasicColor.GREEN.dark().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundDark(Ansi.GREEN)));
        assertThat(BasicColor.YELLOW.dark().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundDark(Ansi.YELLOW)));
        assertThat(BasicColor.BLUE.dark().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundDark(Ansi.BLUE)));
        assertThat(BasicColor.MAGENTA.dark().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundDark(Ansi.MAGENTA)));
        assertThat(BasicColor.CYAN.dark().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundDark(Ansi.CYAN)));
        assertThat(BasicColor.WHITE.dark().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundDark(Ansi.WHITE)));

        // Basic foreground colors - bright variants
        assertThat(BasicColor.BLACK.bright().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundBright(BLACK)));
        assertThat(BasicColor.RED.bright().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundBright(Ansi.RED)));
        assertThat(BasicColor.GREEN.bright().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundBright(Ansi.GREEN)));
        assertThat(BasicColor.YELLOW.bright().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundBright(Ansi.YELLOW)));
        assertThat(BasicColor.BLUE.bright().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundBright(Ansi.BLUE)));
        assertThat(BasicColor.MAGENTA.bright().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundBright(Ansi.MAGENTA)));
        assertThat(BasicColor.CYAN.bright().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundBright(Ansi.CYAN)));
        assertThat(BasicColor.WHITE.bright().toAnsiFg())
                .isEqualTo(Ansi.style(Ansi.foregroundBright(Ansi.WHITE)));

        // Basic background colors
        assertThat(BasicColor.BLACK.toAnsiBg()).isEqualTo(Ansi.style(Ansi.background(Ansi.BLACK)));
        assertThat(BasicColor.RED.toAnsiBg()).isEqualTo(Ansi.style(Ansi.background(Ansi.RED)));
        assertThat(BasicColor.GREEN.toAnsiBg()).isEqualTo(Ansi.style(Ansi.background(Ansi.GREEN)));
        assertThat(BasicColor.YELLOW.toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.background(Ansi.YELLOW)));
        assertThat(BasicColor.BLUE.toAnsiBg()).isEqualTo(Ansi.style(Ansi.background(Ansi.BLUE)));
        assertThat(BasicColor.MAGENTA.toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.background(Ansi.MAGENTA)));
        assertThat(BasicColor.CYAN.toAnsiBg()).isEqualTo(Ansi.style(Ansi.background(Ansi.CYAN)));
        assertThat(BasicColor.WHITE.toAnsiBg()).isEqualTo(Ansi.style(Ansi.background(Ansi.WHITE)));

        // Basic background colors - dark variants
        assertThat(BasicColor.BLACK.dark().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundDark(BLACK)));
        assertThat(BasicColor.RED.dark().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundDark(Ansi.RED)));
        assertThat(BasicColor.GREEN.dark().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundDark(Ansi.GREEN)));
        assertThat(BasicColor.YELLOW.dark().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundDark(Ansi.YELLOW)));
        assertThat(BasicColor.BLUE.dark().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundDark(Ansi.BLUE)));
        assertThat(BasicColor.MAGENTA.dark().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundDark(Ansi.MAGENTA)));
        assertThat(BasicColor.CYAN.dark().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundDark(Ansi.CYAN)));
        assertThat(BasicColor.WHITE.dark().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundDark(Ansi.WHITE)));

        // Basic background colors - bright variants
        assertThat(BasicColor.BLACK.bright().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundBright(BLACK)));
        assertThat(BasicColor.RED.bright().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundBright(Ansi.RED)));
        assertThat(BasicColor.GREEN.bright().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundBright(Ansi.GREEN)));
        assertThat(BasicColor.YELLOW.bright().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundBright(Ansi.YELLOW)));
        assertThat(BasicColor.BLUE.bright().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundBright(Ansi.BLUE)));
        assertThat(BasicColor.MAGENTA.bright().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundBright(Ansi.MAGENTA)));
        assertThat(BasicColor.CYAN.bright().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundBright(Ansi.CYAN)));
        assertThat(BasicColor.WHITE.bright().toAnsiBg())
                .isEqualTo(Ansi.style(Ansi.backgroundBright(Ansi.WHITE)));
    }

    @Test
    public void testIndexedColorCodes() {
        IndexedColor color = IndexedColor.of(0);
        assertThat(color.toAnsiFg()).isEqualTo(Ansi.style(Ansi.foregroundIndexed(0)));
        assertThat(color.toAnsiBg()).isEqualTo(Ansi.style(Ansi.backgroundIndexed(0)));
        color = IndexedColor.of(128);
        assertThat(color.toAnsiFg()).isEqualTo(Ansi.style(Ansi.foregroundIndexed(128)));
        assertThat(color.toAnsiBg()).isEqualTo(Ansi.style(Ansi.backgroundIndexed(128)));
        color = IndexedColor.of(255);
        assertThat(color.toAnsiFg()).isEqualTo(Ansi.style(Ansi.foregroundIndexed(255)));
        assertThat(color.toAnsiBg()).isEqualTo(Ansi.style(Ansi.backgroundIndexed(255)));
    }

    @Test
    public void testIndexedColorCodesUnderflow() {
        assertThatThrownBy(
                        () -> {
                            IndexedColor.of(-1);
                        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Color index must be between 0 and 255, got: -1");
    }

    @Test
    public void testIndexedColorCodesOverflow() {
        assertThatThrownBy(
                        () -> {
                            IndexedColor.of(256);
                        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Color index must be between 0 and 255, got: 256");
    }

    @Test
    public void testRgbColorCodes() {
        RgbColor color = RgbColor.of(0, 0, 0);
        assertThat(color.toAnsiFg()).isEqualTo(Ansi.style(Ansi.foregroundRgb(0, 0, 0)));
        assertThat(color.toAnsiBg()).isEqualTo(Ansi.style(Ansi.backgroundRgb(0, 0, 0)));
        color = RgbColor.of(128, 64, 32);
        assertThat(color.toAnsiFg()).isEqualTo(Ansi.style(Ansi.foregroundRgb(128, 64, 32)));
        assertThat(color.toAnsiBg()).isEqualTo(Ansi.style(Ansi.backgroundRgb(128, 64, 32)));
        color = RgbColor.of(255, 255, 255);
        assertThat(color.toAnsiFg()).isEqualTo(Ansi.style(Ansi.foregroundRgb(255, 255, 255)));
        assertThat(color.toAnsiBg()).isEqualTo(Ansi.style(Ansi.backgroundRgb(255, 255, 255)));
    }

    @Test
    public void testRgbColorCodesUnderflow() {
        assertThatThrownBy(
                        () -> {
                            RgbColor.of(-1, 0, 0);
                        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RGB values must be between 0 and 255, got: -1, 0, 0");
        assertThatThrownBy(
                        () -> {
                            RgbColor.of(0, -1, 0);
                        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RGB values must be between 0 and 255, got: 0, -1, 0");
        assertThatThrownBy(
                        () -> {
                            RgbColor.of(0, 0, -1);
                        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RGB values must be between 0 and 255, got: 0, 0, -1");
    }

    @Test
    public void testRgbColorCodesOverflow() {
        assertThatThrownBy(
                        () -> {
                            RgbColor.of(256, 0, 0);
                        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RGB values must be between 0 and 255, got: 256, 0, 0");
        assertThatThrownBy(
                        () -> {
                            RgbColor.of(0, 256, 0);
                        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RGB values must be between 0 and 255, got: 0, 256, 0");
        assertThatThrownBy(
                        () -> {
                            RgbColor.of(0, 0, 256);
                        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RGB values must be between 0 and 255, got: 0, 0, 256");
    }

    @Test
    public void testBasicColorCodesByIndex() {
        assertThat(BasicColor.byIndex(BLACK)).isEqualTo(BasicColor.BLACK);
        assertThat(BasicColor.byIndex(RED)).isEqualTo(BasicColor.RED);
        assertThat(BasicColor.byIndex(GREEN)).isEqualTo(BasicColor.GREEN);
        assertThat(BasicColor.byIndex(YELLOW)).isEqualTo(BasicColor.YELLOW);
        assertThat(BasicColor.byIndex(BLUE)).isEqualTo(BasicColor.BLUE);
        assertThat(BasicColor.byIndex(MAGENTA)).isEqualTo(BasicColor.MAGENTA);
        assertThat(BasicColor.byIndex(CYAN)).isEqualTo(BasicColor.CYAN);
        assertThat(BasicColor.byIndex(WHITE)).isEqualTo(BasicColor.WHITE);
    }

    @Test
    public void testBasicColorCodesByIndexNormal() {
        assertThat(BasicColor.byIndex(BLACK, BasicColor.Intensity.normal))
                .isEqualTo(BasicColor.BLACK);
        assertThat(BasicColor.byIndex(RED, BasicColor.Intensity.normal)).isEqualTo(BasicColor.RED);
        assertThat(BasicColor.byIndex(GREEN, BasicColor.Intensity.normal))
                .isEqualTo(BasicColor.GREEN);
        assertThat(BasicColor.byIndex(YELLOW, BasicColor.Intensity.normal))
                .isEqualTo(BasicColor.YELLOW);
        assertThat(BasicColor.byIndex(BLUE, BasicColor.Intensity.normal))
                .isEqualTo(BasicColor.BLUE);
        assertThat(BasicColor.byIndex(MAGENTA, BasicColor.Intensity.normal))
                .isEqualTo(BasicColor.MAGENTA);
        assertThat(BasicColor.byIndex(CYAN, BasicColor.Intensity.normal))
                .isEqualTo(BasicColor.CYAN);
        assertThat(BasicColor.byIndex(WHITE, BasicColor.Intensity.normal))
                .isEqualTo(BasicColor.WHITE);
    }

    @Test
    public void testBasicColorCodesByIndexDark() {
        assertThat(BasicColor.byIndex(BLACK, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_BLACK);
        assertThat(BasicColor.byIndex(RED, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_RED);
        assertThat(BasicColor.byIndex(GREEN, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_GREEN);
        assertThat(BasicColor.byIndex(YELLOW, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_YELLOW);
        assertThat(BasicColor.byIndex(BLUE, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_BLUE);
        assertThat(BasicColor.byIndex(MAGENTA, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_MAGENTA);
        assertThat(BasicColor.byIndex(CYAN, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_CYAN);
        assertThat(BasicColor.byIndex(WHITE, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_WHITE);
    }

    @Test
    public void testBasicColorCodesByIndexBright() {
        assertThat(BasicColor.byIndex(BLACK, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_BLACK);
        assertThat(BasicColor.byIndex(RED, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_RED);
        assertThat(BasicColor.byIndex(GREEN, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_GREEN);
        assertThat(BasicColor.byIndex(YELLOW, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_YELLOW);
        assertThat(BasicColor.byIndex(BLUE, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_BLUE);
        assertThat(BasicColor.byIndex(MAGENTA, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_MAGENTA);
        assertThat(BasicColor.byIndex(CYAN, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_CYAN);
        assertThat(BasicColor.byIndex(WHITE, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_WHITE);
    }
}
