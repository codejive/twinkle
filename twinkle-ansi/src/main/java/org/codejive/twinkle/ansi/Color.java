package org.codejive.twinkle.ansi;

import static org.codejive.twinkle.ansi.Ansi.*;

import java.util.Objects;

public interface Color {

    Color DEFAULT = DefaultColor.instance();

    static BasicColor basic(int index) {
        return BasicColor.byIndex(index);
    }

    static BasicColor basic(int index, BasicColor.Intensity intensity) {
        return BasicColor.byIndex(index, intensity);
    }

    static IndexedColor indexed(int index) {
        return IndexedColor.of(index);
    }

    static RgbColor rgb(int r, int g, int b) {
        return RgbColor.of(r, g, b);
    }

    /**
     * Convert this color to ANSI escape code for setting the foreground color to the color
     * represented by this instance. This is NOT the full CSI sequence, to get the full sequence
     * pass the result to <code>Ansi.style()</code> or use <code>toAnsiFg()</code>.
     *
     * @return Embeddable ANSI escape code string
     */
    String toAnsiFgArgs();

    /**
     * Convert this color to ANSI escape code for setting the foreground color to the color
     * represented by this instance.
     *
     * @return ANSI CSI escape code string
     */
    default String toAnsiFg() {
        return Ansi.style(toAnsiFgArgs());
    }

    /**
     * Convert this color to ANSI escape code for setting the background color to the color
     * represented by this instance. This is NOT the full CSI sequence, to get the full sequence
     * pass the result to <code>Ansi.style()</code> or use <code>toAnsiBg()</code>.
     *
     * @return Embeddable ANSI escape code string
     */
    String toAnsiBgArgs();

    /**
     * Convert this color to ANSI escape code for setting the background color to the color
     * represented by this instance.
     *
     * @return ANSI CSI escape code string
     */
    default String toAnsiBg() {
        return Ansi.style(toAnsiBgArgs());
    }

    class DefaultColor implements Color {
        private static final DefaultColor INSTANCE = new DefaultColor();

        private DefaultColor() {}

        protected static Color instance() {
            return INSTANCE;
        }

        @Override
        public String toAnsiFgArgs() {
            return String.valueOf(DEFAULT_FOREGROUND);
        }

        @Override
        public String toAnsiBgArgs() {
            return String.valueOf(DEFAULT_BACKGROUND);
        }

        @Override
        public String toString() {
            return "default";
        }
    }

    class BasicColor implements Color {
        private final String name;
        private final int index;
        private final Intensity intensity;
        private final String fgAnsi;
        private final String bgAnsi;

        public enum Intensity {
            normal,
            dark,
            bright;
        }

        public static final BasicColor BLACK = BasicColor.of("black", Ansi.BLACK, Intensity.normal);
        public static final BasicColor RED = BasicColor.of("red", Ansi.RED, Intensity.normal);
        public static final BasicColor GREEN = BasicColor.of("green", Ansi.GREEN, Intensity.normal);
        public static final BasicColor YELLOW =
                BasicColor.of("yellow", Ansi.YELLOW, Intensity.normal);
        public static final BasicColor BLUE = BasicColor.of("blue", Ansi.BLUE, Intensity.normal);
        public static final BasicColor MAGENTA =
                BasicColor.of("magenta", Ansi.MAGENTA, Intensity.normal);
        public static final BasicColor CYAN = BasicColor.of("cyan", Ansi.CYAN, Intensity.normal);
        public static final BasicColor WHITE = BasicColor.of("white", Ansi.WHITE, Intensity.normal);

        public static final BasicColor DARK_BLACK =
                BasicColor.of("black", Ansi.BLACK, Intensity.dark);
        public static final BasicColor DARK_RED = BasicColor.of("red", Ansi.RED, Intensity.dark);
        public static final BasicColor DARK_GREEN =
                BasicColor.of("green", Ansi.GREEN, Intensity.dark);
        public static final BasicColor DARK_YELLOW =
                BasicColor.of("yellow", Ansi.YELLOW, Intensity.dark);
        public static final BasicColor DARK_BLUE = BasicColor.of("blue", Ansi.BLUE, Intensity.dark);
        public static final BasicColor DARK_MAGENTA =
                BasicColor.of("magenta", Ansi.MAGENTA, Intensity.dark);
        public static final BasicColor DARK_CYAN = BasicColor.of("cyan", Ansi.CYAN, Intensity.dark);
        public static final BasicColor DARK_WHITE =
                BasicColor.of("white", Ansi.WHITE, Intensity.dark);

        public static final BasicColor BRIGHT_BLACK =
                BasicColor.of("black", Ansi.BLACK, Intensity.bright);
        public static final BasicColor BRIGHT_RED =
                BasicColor.of("red", Ansi.RED, Intensity.bright);
        public static final BasicColor BRIGHT_GREEN =
                BasicColor.of("green", Ansi.GREEN, Intensity.bright);
        public static final BasicColor BRIGHT_YELLOW =
                BasicColor.of("yellow", Ansi.YELLOW, Intensity.bright);
        public static final BasicColor BRIGHT_BLUE =
                BasicColor.of("blue", Ansi.BLUE, Intensity.bright);
        public static final BasicColor BRIGHT_MAGENTA =
                BasicColor.of("magenta", Ansi.MAGENTA, Intensity.bright);
        public static final BasicColor BRIGHT_CYAN =
                BasicColor.of("cyan", Ansi.CYAN, Intensity.bright);
        public static final BasicColor BRIGHT_WHITE =
                BasicColor.of("white", Ansi.WHITE, Intensity.bright);

        private static final BasicColor[] normalColors = {
            BLACK, RED, GREEN, YELLOW,
            BLUE, MAGENTA, CYAN, WHITE
        };

        private static final BasicColor[] darkColors = {
            DARK_BLACK, DARK_RED, DARK_GREEN, DARK_YELLOW,
            DARK_BLUE, DARK_MAGENTA, DARK_CYAN, DARK_WHITE
        };

        private static final BasicColor[] brightColors = {
            BRIGHT_BLACK, BRIGHT_RED, BRIGHT_GREEN, BRIGHT_YELLOW,
            BRIGHT_BLUE, BRIGHT_MAGENTA, BRIGHT_CYAN, BRIGHT_WHITE
        };

        protected static BasicColor of(String name, int index, Intensity intensity) {
            return new BasicColor(name, index, intensity);
        }

        public static BasicColor byIndex(int index) {
            return normalColors[index];
        }

        public static BasicColor byIndex(int index, Intensity intensity) {
            switch (intensity) {
                case dark:
                    return darkColors[index];
                case bright:
                    return brightColors[index];
                default:
                    return normalColors[index];
            }
        }

        private BasicColor(String name, int index, Intensity intensity) {
            if (index < 0 || index > 7) {
                throw new IllegalArgumentException(
                        "Color index must be between 0 and 7, got: " + index);
            }
            this.name = name;
            this.index = index;
            this.intensity = intensity;
            fgAnsi = fgAnsi(index, intensity);
            bgAnsi = bgAnsi(index, intensity);
        }

        public int index() {
            return index;
        }

        public Intensity intensity() {
            return intensity;
        }

        public BasicColor normal() {
            if (intensity == Intensity.normal) {
                return this;
            } else {
                return normalColors[index];
            }
        }

        public BasicColor dark() {
            if (intensity == Intensity.dark) {
                return this;
            } else {
                return darkColors[index];
            }
        }

        public BasicColor bright() {
            if (intensity == Intensity.bright) {
                return this;
            } else {
                return brightColors[index];
            }
        }

        public String toAnsiFgArgs() {
            return fgAnsi;
        }

        public String toAnsiBgArgs() {
            return bgAnsi;
        }

        private static String fgAnsi(int index, Intensity intensity) {
            switch (intensity) {
                case normal:
                    return Ansi.foreground(index);
                case dark:
                    return Ansi.foregroundDark(index);
                case bright:
                    return Ansi.foregroundBright(index);
                default:
                    throw new IllegalArgumentException("Unknown mode: " + intensity);
            }
        }

        private static String bgAnsi(int index, Intensity intensity) {
            switch (intensity) {
                case normal:
                    return Ansi.background(index);
                case dark:
                    return Ansi.backgroundDark(index);
                case bright:
                    return Ansi.backgroundBright(index);
                default:
                    throw new IllegalArgumentException("Unknown mode: " + intensity);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            BasicColor that = (BasicColor) o;
            return index == that.index && intensity == that.intensity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(index, intensity);
        }

        @Override
        public String toString() {
            switch (intensity) {
                case normal:
                    return name;
                case dark:
                    return "dark " + name;
                case bright:
                    return "bright " + name;
                default:
                    throw new IllegalArgumentException("Unknown mode: " + intensity);
            }
        }
    }

    class IndexedColor implements Color {
        private final int index;
        private final String fgAnsi;
        private final String bgAnsi;

        public static IndexedColor of(int index) {
            return new IndexedColor(index);
        }

        private IndexedColor(int index) {
            if (index < 0 || index > 255) {
                throw new IllegalArgumentException(
                        "Color index must be between 0 and 255, got: " + index);
            }
            this.index = index;
            fgAnsi = fgAnsi(index);
            bgAnsi = bgAnsi(index);
        }

        public int index() {
            return index;
        }

        @Override
        public String toAnsiFgArgs() {
            return fgAnsi;
        }

        @Override
        public String toAnsiBgArgs() {
            return bgAnsi;
        }

        private static String fgAnsi(int index) {
            return Ansi.foregroundIndexed(index);
        }

        private static String bgAnsi(int index) {
            return Ansi.backgroundIndexed(index);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            IndexedColor that = (IndexedColor) o;
            return index == that.index;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(index);
        }

        @Override
        public String toString() {
            return "%" + index;
        }
    }

    class RgbColor implements Color {
        private final int r, g, b;
        private final String fgAnsi;
        private final String bgAnsi;

        public static RgbColor of(int r, int g, int b) {
            return new RgbColor(r, g, b);
        }

        private RgbColor(int r, int g, int b) {
            if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
                throw new IllegalArgumentException(
                        "RGB values must be between 0 and 255, got: " + r + ", " + g + ", " + b);
            }
            this.r = r;
            this.g = g;
            this.b = b;
            fgAnsi = fgAnsi(r, g, b);
            bgAnsi = bgAnsi(r, g, b);
        }

        public int r() {
            return r;
        }

        public int g() {
            return g;
        }

        public int b() {
            return b;
        }

        @Override
        public String toAnsiFgArgs() {
            return fgAnsi;
        }

        @Override
        public String toAnsiBgArgs() {
            return bgAnsi;
        }

        private static String fgAnsi(int r, int g, int b) {
            return Ansi.foregroundRgb(r, g, b);
        }

        private static String bgAnsi(int r, int g, int b) {
            return Ansi.backgroundRgb(r, g, b);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            RgbColor rgbColor = (RgbColor) o;
            return r == rgbColor.r && g == rgbColor.g && b == rgbColor.b;
        }

        @Override
        public int hashCode() {
            return Objects.hash(r, g, b);
        }

        @Override
        public String toString() {
            return String.format("#%02x%02x%02x", r, g, b);
        }
    }
}
