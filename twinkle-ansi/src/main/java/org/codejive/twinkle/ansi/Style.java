package org.codejive.twinkle.ansi;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;

public class Style {
    private final long state;

    private static final long IDX_BOLD = 0;
    private static final long IDX_FAINT = 1;
    private static final long IDX_ITALICIZED = 2;
    private static final long IDX_UNDERLINED = 3;
    private static final long IDX_BLINK = 4;
    private static final long IDX_INVERSE = 5;
    private static final long IDX_INVISIBLE = 6;
    private static final long IDX_CROSSEDOUT = 7;
    // private static final long DOUBLEUNDERLINE = 8;

    public static final long F_UNSTYLED = 0L;
    public static final long F_BOLD = 1 << IDX_BOLD;
    public static final long F_FAINT = 1 << IDX_FAINT;
    public static final long F_ITALIC = 1 << IDX_ITALICIZED;
    public static final long F_UNDERLINED = 1 << IDX_UNDERLINED;
    public static final long F_BLINK = 1 << IDX_BLINK;
    public static final long F_INVERSE = 1 << IDX_INVERSE;
    public static final long F_HIDDEN = 1 << IDX_INVISIBLE;
    public static final long F_STRIKETHROUGH = 1 << IDX_CROSSEDOUT;

    public static final Style UNSTYLED = new Style(0);
    public static final Style BOLD = UNSTYLED.bold();
    public static final Style FAINT = UNSTYLED.faint();
    public static final Style ITALIC = UNSTYLED.italic();
    public static final Style UNDERLINED = UNSTYLED.underlined();
    public static final Style BLINK = UNSTYLED.blink();
    public static final Style INVERSE = UNSTYLED.inverse();
    public static final Style HIDDEN = UNSTYLED.hidden();
    public static final Style STRIKETHROUGH = UNSTYLED.strikethrough();

    public static final String PROP_TOANSI = "twinkle.styledbuffer.toAnsi";

    public static @NonNull Style ofFgColor(@NonNull Color color) {
        return UNSTYLED.fgColor(color);
    }

    public static @NonNull Style ofBgColor(@NonNull Color color) {
        return UNSTYLED.bgColor(color);
    }

    /*
     * State bit encoding is as follows (64 bits):
     * Bit 0: Bold
     * Bit 1: Faint
     * Bit 2: Italicized
     * Bit 3: Underlined
     * Bit 4: Blink
     * Bit 5: Inverse
     * Bit 6: Invisible
     * Bit 7: Crossed-out
     * Bits 8-13: Reserved
     * Bits 14-38: Foreground color (see below)
     * Bits 39-63: Background color (see below)
     *
     * Foreground/Background color encoding (25 bits):
     * Bit 0: Color mode (0 = basic/indexed, 1 = RGB)
     *
     * If color mode is basic/indexed:
     * Bit 1: Palette type (0 = basic, 1 = indexed)
     *
     * If palette type is basic:
     * Bits 0-1: Intensity (0 = default, 1 = normal, 2 = dark, 3 = bright)
     * Bits 2-4: Color index
     *
     * If palette type is indexed:
     * Bits 1-8: Color index
     *
     * If color mode is RGB:
     * Bits 1-8: Red component
     * Bits 9-16: Green component
     * Bits 17-24: Blue component
     */

    private static final int SHIFT_FG_COLOR = 14;
    private static final int SHIFT_BG_COLOR = 39;
    private static final int SHIFT_PALETTE_TYPE = 1;
    private static final int SHIFT_COLOR_BASIC_INTENSITY = 2;
    private static final int SHIFT_COLOR_BASIC_INDEX = 5;
    private static final int SHIFT_COLOR_INDEXED_INDEX = 2;
    private static final int SHIFT_COLOR_R = 1;
    private static final int SHIFT_COLOR_G = 9;
    private static final int SHIFT_COLOR_B = 17;

    private static final long MASK_COLOR = 0x01ffffffL;
    private static final long MASK_FG_COLOR = MASK_COLOR << SHIFT_FG_COLOR;
    private static final long MASK_BG_COLOR = MASK_COLOR << SHIFT_BG_COLOR;
    private static final long MASK_COLOR_MODE = 0x01L;
    private static final long MASK_PALETTE_TYPE = 0x01L;
    private static final long MASK_COLOR_BASIC_INTENSITY = 0x03L;
    private static final long MASK_COLOR_BASIC_INDEX = 0x07L;
    private static final long MASK_COLOR_PART = 0xffL;

    private static final long CM_INDEXED = 0;
    private static final long CM_RGB = 1;

    private static final long PALETTE_BASIC = 0;
    private static final long PALETTE_INDEXED = 1;

    // Not really an intensity, but a flag to indicate default color,
    // but we're (ab)using the intensity bits to store it
    private static final long INTENSITY_DEFAULT = 0;

    private static final long INTENSITY_NORMAL = 1;
    private static final long INTENSITY_DARK = 2;
    private static final long INTENSITY_BRIGHT = 3;

    public static @NonNull Style of(long state) {
        if (state == 0) {
            return UNSTYLED;
        }
        return new Style(state);
    }

    private Style(long state) {
        this.state = state;
    }

    public long state() {
        return state;
    }

    public @NonNull Style unstyled() {
        return UNSTYLED;
    }

    public @NonNull Style normal() {
        return of(state & ~(F_BOLD | F_FAINT));
    }

    public boolean isBold() {
        return (state & F_BOLD) != 0;
    }

    public @NonNull Style bold() {
        return of(state | F_BOLD);
    }

    public boolean isFaint() {
        return (state & F_FAINT) != 0;
    }

    public @NonNull Style faint() {
        return of(state | F_FAINT);
    }

    public boolean isItalic() {
        return (state & F_ITALIC) != 0;
    }

    public @NonNull Style italic() {
        return of(state | F_ITALIC);
    }

    public @NonNull Style italicOff() {
        return of(state & ~F_ITALIC);
    }

    public boolean isUnderlined() {
        return (state & F_UNDERLINED) != 0;
    }

    public @NonNull Style underlined() {
        return of(state | F_UNDERLINED);
    }

    public @NonNull Style underlinedOff() {
        return of(state & ~F_UNDERLINED);
    }

    public boolean isBlink() {
        return (state & F_BLINK) != 0;
    }

    public @NonNull Style blink() {
        return of(state | F_BLINK);
    }

    public @NonNull Style blinkOff() {
        return of(state & ~F_BLINK);
    }

    public boolean isInverse() {
        return (state & F_INVERSE) != 0;
    }

    public @NonNull Style inverse() {
        return of(state | F_INVERSE);
    }

    public @NonNull Style inverseOff() {
        return of(state & ~F_INVERSE);
    }

    public boolean isHidden() {
        return (state & F_HIDDEN) != 0;
    }

    public @NonNull Style hidden() {
        return of(state | F_HIDDEN);
    }

    public @NonNull Style hiddenOff() {
        return of(state & ~F_HIDDEN);
    }

    public boolean isStrikethrough() {
        return (state & F_STRIKETHROUGH) != 0;
    }

    public @NonNull Style strikethrough() {
        return of(state | F_STRIKETHROUGH);
    }

    public @NonNull Style strikethroughOff() {
        return of(state & ~F_STRIKETHROUGH);
    }

    public @NonNull Color fgColor() {
        long fgc = ((state & MASK_FG_COLOR) >> SHIFT_FG_COLOR);
        return decodeColor(fgc);
    }

    public @NonNull Style fgColor(@NonNull Color color) {
        long newState = (state & ~MASK_FG_COLOR) | (encodeColor(color) << SHIFT_FG_COLOR);
        return of(newState);
    }

    public @NonNull Color bgColor() {
        long bgc = ((state & MASK_BG_COLOR) >> SHIFT_BG_COLOR);
        return decodeColor(bgc);
    }

    public @NonNull Style bgColor(@NonNull Color color) {
        long newState = (state & ~MASK_BG_COLOR) | (encodeColor(color) << SHIFT_BG_COLOR);
        return of(newState);
    }

    private static long encodeColor(@NonNull Color color) {
        long result = 0;
        if (color instanceof Color.RgbColor) {
            Color.RgbColor rgbColor = (Color.RgbColor) color;
            result |= CM_RGB;
            result |= ((long) rgbColor.r() & MASK_COLOR_PART) << SHIFT_COLOR_R;
            result |= ((long) rgbColor.g() & MASK_COLOR_PART) << SHIFT_COLOR_G;
            result |= ((long) rgbColor.b() & MASK_COLOR_PART) << SHIFT_COLOR_B;
        } else if (color instanceof Color.IndexedColor) {
            Color.IndexedColor idxColor = (Color.IndexedColor) color;
            result |= PALETTE_INDEXED << SHIFT_PALETTE_TYPE;
            result |= ((long) idxColor.index() & MASK_COLOR_PART) << SHIFT_COLOR_INDEXED_INDEX;
        } else if (color instanceof Color.BasicColor) {
            Color.BasicColor basicColor = (Color.BasicColor) color;
            int intensity;
            switch (basicColor.intensity()) {
                case normal:
                    intensity = 1;
                    break;
                case dark:
                    intensity = 2;
                    break;
                case bright:
                    intensity = 3;
                    break;
                default:
                    intensity = 0;
                    break;
            }
            ;
            result |=
                    ((long) intensity & MASK_COLOR_BASIC_INTENSITY) << SHIFT_COLOR_BASIC_INTENSITY;
            result |=
                    ((long) basicColor.index() & MASK_COLOR_BASIC_INDEX) << SHIFT_COLOR_BASIC_INDEX;
        }
        return result;
    }

    private static @NonNull Color decodeColor(long color) {
        Color result = Color.DEFAULT;
        long mode = color & MASK_COLOR_MODE;
        if (mode == CM_INDEXED) {
            long paletteType = (color >> SHIFT_PALETTE_TYPE) & MASK_PALETTE_TYPE;
            if (paletteType == PALETTE_BASIC) {
                int intensity =
                        (int) ((color >> SHIFT_COLOR_BASIC_INTENSITY) & MASK_COLOR_BASIC_INTENSITY);
                int colorIndex =
                        (int) ((color >> SHIFT_COLOR_BASIC_INDEX) & MASK_COLOR_BASIC_INDEX);
                switch (intensity) {
                    case 1:
                        result = Color.basic(colorIndex, Color.BasicColor.Intensity.normal);
                        break;
                    case 2:
                        result = Color.basic(colorIndex, Color.BasicColor.Intensity.dark);
                        break;
                    case 3:
                        result = Color.basic(colorIndex, Color.BasicColor.Intensity.bright);
                        break;
                }
            } else { // paletteType == F_PALETTE_INDEXED
                int colorIndex = (int) ((color >> SHIFT_COLOR_INDEXED_INDEX) & MASK_COLOR_PART);
                result = Color.indexed(colorIndex);
            }
        } else { // mode == F_CM_RGB
            int r = (int) ((color >> SHIFT_COLOR_R) & MASK_COLOR_PART);
            int g = (int) ((color >> SHIFT_COLOR_G) & MASK_COLOR_PART);
            int b = (int) ((color >> SHIFT_COLOR_B) & MASK_COLOR_PART);
            result = Color.rgb(r, g, b);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Style)) return false;
        Style other = (Style) o;
        return this.state == other.state;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(state);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("StyleImpl{");
        if (isBold()) sb.append("bold, ");
        if (isFaint()) sb.append("faint, ");
        if (isItalic()) sb.append("italic, ");
        if (isUnderlined()) sb.append("underlined, ");
        if (isBlink()) sb.append("blink, ");
        if (isInverse()) sb.append("inverse, ");
        if (isHidden()) sb.append("hidden, ");
        if (isStrikethrough()) sb.append("strikethrough, ");
        if (fgColor() != Color.DEFAULT) sb.append("fgColor=").append(fgColor()).append(", ");
        if (bgColor() != Color.DEFAULT) sb.append("bgColor=").append(bgColor());
        if (sb.charAt(sb.length() - 2) == ',')
            sb.setLength(sb.length() - 2); // Remove trailing comma
        sb.append('}');
        return sb.toString();
    }

    static boolean fastOverShort = System.getProperty(PROP_TOANSI, "fast").equals("fast");

    public String toAnsiString() {
        return toAnsiString(UNSTYLED);
    }

    public String toAnsiString(Style currentStyle) {
        return toAnsiString(currentStyle.state());
    }

    public String toAnsiString(long currentStyleState) {
        if (fastOverShort) {
            List<Object> styles = new ArrayList<>();
            return toAnsiString(styles, currentStyleState);
        } else {
            List<Object> styles1 = new ArrayList<>();
            List<Object> styles2 = new ArrayList<>();
            styles2.add(Ansi.RESET);
            String ansi1 = toAnsiString(styles1, currentStyleState);
            String ansi2 = toAnsiString(styles2, F_UNSTYLED);
            return (ansi1.length() <= ansi2.length()) ? ansi1 : ansi2;
        }
    }

    private String toAnsiString(List<Object> styles, long currentStyleState) {
        if ((currentStyleState & (F_BOLD | F_FAINT)) != (state & (F_BOLD | F_FAINT))) {
            // First we switch to NORMAL to clear both BOLD and FAINT
            if ((currentStyleState & (F_BOLD | F_FAINT)) != 0) {
                styles.add(Ansi.NORMAL);
            }
            // Now we set the needed styles
            if (isBold()) styles.add(Ansi.BOLD);
            if (isFaint()) styles.add(Ansi.FAINT);
        }
        if ((currentStyleState & F_ITALIC) != (state & F_ITALIC)) {
            if (isItalic()) {
                styles.add(Ansi.ITALICIZED);
            } else {
                styles.add(Ansi.NOTITALICIZED);
            }
        }
        if ((currentStyleState & F_UNDERLINED) != (state & F_UNDERLINED)) {
            if (isUnderlined()) {
                styles.add(Ansi.UNDERLINED);
            } else {
                styles.add(Ansi.NOTUNDERLINED);
            }
        }
        if ((currentStyleState & F_BLINK) != (state & F_BLINK)) {
            if (isBlink()) {
                styles.add(Ansi.BLINK);
            } else {
                styles.add(Ansi.STEADY);
            }
        }
        if ((currentStyleState & F_INVERSE) != (state & F_INVERSE)) {
            if (isInverse()) {
                styles.add(Ansi.INVERSE);
            } else {
                styles.add(Ansi.POSITIVE);
            }
        }
        if ((currentStyleState & F_HIDDEN) != (state & F_HIDDEN)) {
            if (isHidden()) {
                styles.add(Ansi.INVISIBLE);
            } else {
                styles.add(Ansi.VISIBLE);
            }
        }
        if ((currentStyleState & F_STRIKETHROUGH) != (state & F_STRIKETHROUGH)) {
            if (isStrikethrough()) {
                styles.add(Ansi.CROSSEDOUT);
            } else {
                styles.add(Ansi.NOTCROSSEDOUT);
            }
        }
        if ((currentStyleState & MASK_FG_COLOR) != (state & MASK_FG_COLOR)) {
            styles.add(fgColor().toAnsiFg());
        }
        if ((currentStyleState & MASK_BG_COLOR) != (state & MASK_BG_COLOR)) {
            styles.add(bgColor().toAnsiBg());
        }
        return Ansi.style(styles.toArray());
    }
}
