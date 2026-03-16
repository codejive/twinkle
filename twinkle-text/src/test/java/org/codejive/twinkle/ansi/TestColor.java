package org.codejive.twinkle.ansi;

import static org.assertj.core.api.Assertions.*;

import org.codejive.twinkle.ansi.Color.*;
import org.codejive.twinkle.ansi.util.StyleBuilder;
import org.junit.jupiter.api.Test;

public class TestColor {

    @Test
    public void testDefaultColorCodes() {
        assertThat(Color.DEFAULT.toAnsiFg())
                .isEqualTo(StyleBuilder.styles(Constants.DEFAULT_FOREGROUND));
        assertThat(Color.DEFAULT.toAnsiBg())
                .isEqualTo(StyleBuilder.styles(Constants.DEFAULT_BACKGROUND));
    }

    @Test
    public void testBasicColorCodes() {
        // Basic foreground colors
        assertThat(BasicColor.BLACK.toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundArg(Constants.BLACK)));
        assertThat(BasicColor.RED.toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundArg(Constants.RED)));
        assertThat(BasicColor.GREEN.toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundArg(Constants.GREEN)));
        assertThat(BasicColor.YELLOW.toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundArg(Constants.YELLOW)));
        assertThat(BasicColor.BLUE.toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundArg(Constants.BLUE)));
        assertThat(BasicColor.MAGENTA.toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundArg(Constants.MAGENTA)));
        assertThat(BasicColor.CYAN.toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundArg(Constants.CYAN)));
        assertThat(BasicColor.WHITE.toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundArg(Constants.WHITE)));

        // Basic foreground colors - dark variants
        assertThat(BasicColor.BLACK.dark().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundDarkArg(Constants.BLACK)));
        assertThat(BasicColor.RED.dark().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundDarkArg(Constants.RED)));
        assertThat(BasicColor.GREEN.dark().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundDarkArg(Constants.GREEN)));
        assertThat(BasicColor.YELLOW.dark().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundDarkArg(Constants.YELLOW)));
        assertThat(BasicColor.BLUE.dark().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundDarkArg(Constants.BLUE)));
        assertThat(BasicColor.MAGENTA.dark().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundDarkArg(Constants.MAGENTA)));
        assertThat(BasicColor.CYAN.dark().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundDarkArg(Constants.CYAN)));
        assertThat(BasicColor.WHITE.dark().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundDarkArg(Constants.WHITE)));

        // Basic foreground colors - bright variants
        assertThat(BasicColor.BLACK.bright().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundBrightArg(Constants.BLACK)));
        assertThat(BasicColor.RED.bright().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundBrightArg(Constants.RED)));
        assertThat(BasicColor.GREEN.bright().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundBrightArg(Constants.GREEN)));
        assertThat(BasicColor.YELLOW.bright().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundBrightArg(Constants.YELLOW)));
        assertThat(BasicColor.BLUE.bright().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundBrightArg(Constants.BLUE)));
        assertThat(BasicColor.MAGENTA.bright().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundBrightArg(Constants.MAGENTA)));
        assertThat(BasicColor.CYAN.bright().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundBrightArg(Constants.CYAN)));
        assertThat(BasicColor.WHITE.bright().toAnsiFg())
                .isEqualTo(StyleBuilder.styles(BasicColor.foregroundBrightArg(Constants.WHITE)));

        // Basic background colors
        assertThat(BasicColor.BLACK.toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundArg(Constants.BLACK)));
        assertThat(BasicColor.RED.toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundArg(Constants.RED)));
        assertThat(BasicColor.GREEN.toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundArg(Constants.GREEN)));
        assertThat(BasicColor.YELLOW.toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundArg(Constants.YELLOW)));
        assertThat(BasicColor.BLUE.toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundArg(Constants.BLUE)));
        assertThat(BasicColor.MAGENTA.toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundArg(Constants.MAGENTA)));
        assertThat(BasicColor.CYAN.toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundArg(Constants.CYAN)));
        assertThat(BasicColor.WHITE.toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundArg(Constants.WHITE)));

        // Basic background colors - dark variants
        assertThat(BasicColor.BLACK.dark().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundDarkArg(Constants.BLACK)));
        assertThat(BasicColor.RED.dark().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundDarkArg(Constants.RED)));
        assertThat(BasicColor.GREEN.dark().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundDarkArg(Constants.GREEN)));
        assertThat(BasicColor.YELLOW.dark().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundDarkArg(Constants.YELLOW)));
        assertThat(BasicColor.BLUE.dark().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundDarkArg(Constants.BLUE)));
        assertThat(BasicColor.MAGENTA.dark().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundDarkArg(Constants.MAGENTA)));
        assertThat(BasicColor.CYAN.dark().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundDarkArg(Constants.CYAN)));
        assertThat(BasicColor.WHITE.dark().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundDarkArg(Constants.WHITE)));

        // Basic background colors - bright variants
        assertThat(BasicColor.BLACK.bright().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundBrightArg(Constants.BLACK)));
        assertThat(BasicColor.RED.bright().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundBrightArg(Constants.RED)));
        assertThat(BasicColor.GREEN.bright().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundBrightArg(Constants.GREEN)));
        assertThat(BasicColor.YELLOW.bright().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundBrightArg(Constants.YELLOW)));
        assertThat(BasicColor.BLUE.bright().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundBrightArg(Constants.BLUE)));
        assertThat(BasicColor.MAGENTA.bright().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundBrightArg(Constants.MAGENTA)));
        assertThat(BasicColor.CYAN.bright().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundBrightArg(Constants.CYAN)));
        assertThat(BasicColor.WHITE.bright().toAnsiBg())
                .isEqualTo(StyleBuilder.styles(BasicColor.backgroundBrightArg(Constants.WHITE)));
    }

    @Test
    public void testIndexedColorCodes() {
        IndexedColor color = IndexedColor.of(0);
        assertThat(color.toAnsiFg())
                .isEqualTo(StyleBuilder.styles(IndexedColor.foregroundIndexedArg(0)));
        assertThat(color.toAnsiBg())
                .isEqualTo(StyleBuilder.styles(IndexedColor.backgroundIndexedArg(0)));
        color = IndexedColor.of(128);
        assertThat(color.toAnsiFg())
                .isEqualTo(StyleBuilder.styles(IndexedColor.foregroundIndexedArg(128)));
        assertThat(color.toAnsiBg())
                .isEqualTo(StyleBuilder.styles(IndexedColor.backgroundIndexedArg(128)));
        color = IndexedColor.of(255);
        assertThat(color.toAnsiFg())
                .isEqualTo(StyleBuilder.styles(IndexedColor.foregroundIndexedArg(255)));
        assertThat(color.toAnsiBg())
                .isEqualTo(StyleBuilder.styles(IndexedColor.backgroundIndexedArg(255)));
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
        assertThat(color.toAnsiFg())
                .isEqualTo(StyleBuilder.styles(RgbColor.foregroundRgbArg(0, 0, 0)));
        assertThat(color.toAnsiBg())
                .isEqualTo(StyleBuilder.styles(RgbColor.backgroundRgbArg(0, 0, 0)));
        color = RgbColor.of(128, 64, 32);
        assertThat(color.toAnsiFg())
                .isEqualTo(StyleBuilder.styles(RgbColor.foregroundRgbArg(128, 64, 32)));
        assertThat(color.toAnsiBg())
                .isEqualTo(StyleBuilder.styles(RgbColor.backgroundRgbArg(128, 64, 32)));
        color = RgbColor.of(255, 255, 255);
        assertThat(color.toAnsiFg())
                .isEqualTo(StyleBuilder.styles(RgbColor.foregroundRgbArg(255, 255, 255)));
        assertThat(color.toAnsiBg())
                .isEqualTo(StyleBuilder.styles(RgbColor.backgroundRgbArg(255, 255, 255)));
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
        assertThat(BasicColor.byIndex(Constants.BLACK)).isEqualTo(BasicColor.BLACK);
        assertThat(BasicColor.byIndex(Constants.RED)).isEqualTo(BasicColor.RED);
        assertThat(BasicColor.byIndex(Constants.GREEN)).isEqualTo(BasicColor.GREEN);
        assertThat(BasicColor.byIndex(Constants.YELLOW)).isEqualTo(BasicColor.YELLOW);
        assertThat(BasicColor.byIndex(Constants.BLUE)).isEqualTo(BasicColor.BLUE);
        assertThat(BasicColor.byIndex(Constants.MAGENTA)).isEqualTo(BasicColor.MAGENTA);
        assertThat(BasicColor.byIndex(Constants.CYAN)).isEqualTo(BasicColor.CYAN);
        assertThat(BasicColor.byIndex(Constants.WHITE)).isEqualTo(BasicColor.WHITE);
    }

    @Test
    public void testBasicColorCodesByIndexNormal() {
        assertThat(BasicColor.byIndex(Constants.BLACK, BasicColor.Intensity.normal))
                .isEqualTo(BasicColor.BLACK);
        assertThat(BasicColor.byIndex(Constants.RED, BasicColor.Intensity.normal))
                .isEqualTo(BasicColor.RED);
        assertThat(BasicColor.byIndex(Constants.GREEN, BasicColor.Intensity.normal))
                .isEqualTo(BasicColor.GREEN);
        assertThat(BasicColor.byIndex(Constants.YELLOW, BasicColor.Intensity.normal))
                .isEqualTo(BasicColor.YELLOW);
        assertThat(BasicColor.byIndex(Constants.BLUE, BasicColor.Intensity.normal))
                .isEqualTo(BasicColor.BLUE);
        assertThat(BasicColor.byIndex(Constants.MAGENTA, BasicColor.Intensity.normal))
                .isEqualTo(BasicColor.MAGENTA);
        assertThat(BasicColor.byIndex(Constants.CYAN, BasicColor.Intensity.normal))
                .isEqualTo(BasicColor.CYAN);
        assertThat(BasicColor.byIndex(Constants.WHITE, BasicColor.Intensity.normal))
                .isEqualTo(BasicColor.WHITE);
    }

    @Test
    public void testBasicColorCodesByIndexDark() {
        assertThat(BasicColor.byIndex(Constants.BLACK, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_BLACK);
        assertThat(BasicColor.byIndex(Constants.RED, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_RED);
        assertThat(BasicColor.byIndex(Constants.GREEN, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_GREEN);
        assertThat(BasicColor.byIndex(Constants.YELLOW, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_YELLOW);
        assertThat(BasicColor.byIndex(Constants.BLUE, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_BLUE);
        assertThat(BasicColor.byIndex(Constants.MAGENTA, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_MAGENTA);
        assertThat(BasicColor.byIndex(Constants.CYAN, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_CYAN);
        assertThat(BasicColor.byIndex(Constants.WHITE, BasicColor.Intensity.dark))
                .isEqualTo(BasicColor.DARK_WHITE);
    }

    @Test
    public void testBasicColorCodesByIndexBright() {
        assertThat(BasicColor.byIndex(Constants.BLACK, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_BLACK);
        assertThat(BasicColor.byIndex(Constants.RED, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_RED);
        assertThat(BasicColor.byIndex(Constants.GREEN, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_GREEN);
        assertThat(BasicColor.byIndex(Constants.YELLOW, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_YELLOW);
        assertThat(BasicColor.byIndex(Constants.BLUE, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_BLUE);
        assertThat(BasicColor.byIndex(Constants.MAGENTA, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_MAGENTA);
        assertThat(BasicColor.byIndex(Constants.CYAN, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_CYAN);
        assertThat(BasicColor.byIndex(Constants.WHITE, BasicColor.Intensity.bright))
                .isEqualTo(BasicColor.BRIGHT_WHITE);
    }
}
