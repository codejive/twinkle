package org.codejive.twinkle.ansi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.codejive.twinkle.util.Printable;
import org.jspecify.annotations.NonNull;

public class Style implements Printable {
    private final long state;
    private final long mask;

    private static final long IDX_BOLD = 0;
    private static final long IDX_FAINT = 1;
    private static final long IDX_ITALICIZED = 2;
    private static final long IDX_UNDERLINED = 3;
    private static final long IDX_BLINK = 4;
    private static final long IDX_INVERSE = 5;
    private static final long IDX_INVISIBLE = 6;
    private static final long IDX_CROSSEDOUT = 7;
    // private static final long DOUBLEUNDERLINE = 8;

    public static final long F_UNKNOWN = -1L;
    public static final long F_UNSTYLED = 0L;

    public static final long F_BOLD = 1 << IDX_BOLD;
    public static final long F_FAINT = 1 << IDX_FAINT;
    public static final long F_ITALIC = 1 << IDX_ITALICIZED;
    public static final long F_UNDERLINED = 1 << IDX_UNDERLINED;
    public static final long F_BLINK = 1 << IDX_BLINK;
    public static final long F_INVERSE = 1 << IDX_INVERSE;
    public static final long F_HIDDEN = 1 << IDX_INVISIBLE;
    public static final long F_STRIKETHROUGH = 1 << IDX_CROSSEDOUT;

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
     * Bits 1-8: Red widget
     * Bits 9-16: Green widget
     * Bits 17-24: Blue widget
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
    private static final long MASK_STYLES =
            F_BOLD
                    | F_FAINT
                    | F_ITALIC
                    | F_UNDERLINED
                    | F_BLINK
                    | F_INVERSE
                    | F_HIDDEN
                    | F_STRIKETHROUGH;
    private static final long MASK_ALL = MASK_FG_COLOR | MASK_BG_COLOR | MASK_STYLES;

    public static final Style UNKNOWN = new Style(F_UNKNOWN, 0);
    public static final Style UNSTYLED = new Style(F_UNSTYLED, 0);
    public static final Style DEFAULT = new Style(F_UNSTYLED, MASK_ALL);
    public static final Style BOLD = UNSTYLED.bold();
    public static final Style FAINT = UNSTYLED.faint();
    public static final Style ITALIC = UNSTYLED.italic();
    public static final Style UNDERLINED = UNSTYLED.underlined();
    public static final Style BLINK = UNSTYLED.blink();
    public static final Style INVERSE = UNSTYLED.inverse();
    public static final Style HIDDEN = UNSTYLED.hidden();
    public static final Style STRIKETHROUGH = UNSTYLED.strikethrough();

    private static final long CM_INDEXED = 0;
    private static final long CM_RGB = 1;

    private static final long PALETTE_BASIC = 0;
    private static final long PALETTE_INDEXED = 1;

    // Not really an intensity, but a flag to indicate default color,
    // but we're (ab)using the intensity bits to store it
    private static final int INTENSITY_DEFAULT = 0;

    private static final int INTENSITY_NORMAL = 1;
    private static final int INTENSITY_DARK = 2;
    private static final int INTENSITY_BRIGHT = 3;

    public static @NonNull Style of(long state) {
        if (state == F_UNKNOWN) {
            return UNKNOWN;
        }
        if (state == F_UNSTYLED) {
            return DEFAULT;
        }
        return new Style(state, MASK_ALL);
    }

    public static @NonNull Style of(long state, long mask) {
        if (state == F_UNKNOWN) {
            return UNKNOWN;
        }
        if (state == F_UNSTYLED) {
            if ((mask & MASK_ALL) == 0) {
                return UNSTYLED;
            } else if ((mask & MASK_ALL) == MASK_ALL) {
                return DEFAULT;
            }
        }
        return new Style(state, mask);
    }

    private Style(long state, long mask) {
        this.state = state;
        this.mask = mask;
    }

    public long state() {
        return state;
    }

    public long mask() {
        return mask;
    }

    /**
     * Combines this style with another style, giving precedence to the other style's values
     * wherever it has an effect.
     *
     * @param other The other style to combine with.
     * @return A new Style instance representing the combined style.
     */
    public Style and(@NonNull Style other) {
        if (this.equals(UNKNOWN)) {
            return other;
        }
        if (other.equals(UNKNOWN)) {
            return this;
        }

        long newState = (this.state & ~other.mask) | (other.state & other.mask);
        long newMask = this.mask | other.mask;
        return of(newState, newMask);
    }

    /**
     * Computes the difference between this style and another style, producing a new style that
     * represents the changes needed to transform this style into the other style.
     *
     * @param other The other style to compare with.
     * @return A new Style instance representing the difference.
     */
    public Style diff(@NonNull Style other) {
        if (this.equals(UNKNOWN)) {
            return other;
        }
        if (other.equals(UNKNOWN)) {
            return this;
        }

        long newMask = this.mask | other.mask;
        long newState = other.state & newMask;
        return of(newState, newMask);
    }

    /**
     * Returns a new style that represents the style that would result from applying the other style
     * on top of this one. Styles that are changed to their unset or default values in the resulting
     * style will be marked as unaffected.
     *
     * @param other The other style to apply.
     * @return A new Style instance representing the resulting style.
     */
    public Style apply(@NonNull Style other) {
        if (this.equals(UNKNOWN)) {
            return other;
        }
        if (other.equals(UNKNOWN)) {
            return this;
        }

        long newState = (this.state & ~other.mask) | (other.state & other.mask);
        long newMask = this.mask | other.mask;

        // now mark unset styles as unaffected
        long unaffectedMask = ~(other.mask & ~other.state & MASK_STYLES);
        newMask &= unaffectedMask;

        if (other.affectsFgColor() && other.fgColor().equals(Color.DEFAULT)) {
            newState &= ~MASK_FG_COLOR;
            newMask &= ~MASK_FG_COLOR;
        }

        if (other.affectsBgColor() && other.bgColor().equals(Color.DEFAULT)) {
            newState &= ~MASK_BG_COLOR;
            newMask &= ~MASK_BG_COLOR;
        }

        return of(newState, newMask);
    }

    public boolean is(long flag) {
        return (state & flag) != 0;
    }

    public boolean isBold() {
        return is(F_BOLD);
    }

    public boolean isFaint() {
        return is(F_FAINT);
    }

    public boolean isItalic() {
        return is(F_ITALIC);
    }

    public boolean isUnderlined() {
        return is(F_UNDERLINED);
    }

    public boolean isBlink() {
        return is(F_BLINK);
    }

    public boolean isInverse() {
        return is(F_INVERSE);
    }

    public boolean isHidden() {
        return is(F_HIDDEN);
    }

    public boolean isStrikethrough() {
        return is(F_STRIKETHROUGH);
    }

    public @NonNull Color fgColor() {
        long fgc = ((state & MASK_FG_COLOR) >> SHIFT_FG_COLOR);
        return decodeColor(fgc);
    }

    public @NonNull Color bgColor() {
        long bgc = ((state & MASK_BG_COLOR) >> SHIFT_BG_COLOR);
        return decodeColor(bgc);
    }

    public boolean affects(long flag) {
        return (mask & flag) != 0;
    }

    public boolean affectsBold() {
        return affects(F_BOLD);
    }

    public boolean affectsFaint() {
        return affects(F_FAINT);
    }

    public boolean affectsItalic() {
        return affects(F_ITALIC);
    }

    public boolean affectsUnderlined() {
        return affects(F_UNDERLINED);
    }

    public boolean affectsBlink() {
        return affects(F_BLINK);
    }

    public boolean affectsInverse() {
        return affects(F_INVERSE);
    }

    public boolean affectsHidden() {
        return affects(F_HIDDEN);
    }

    public boolean affectsStrikethrough() {
        return affects(F_STRIKETHROUGH);
    }

    public boolean affectsFgColor() {
        return affects(MASK_FG_COLOR);
    }

    public boolean affectsBgColor() {
        return affects(MASK_BG_COLOR);
    }

    public @NonNull Style reset() {
        return DEFAULT;
    }

    public @NonNull Style bold() {
        return of(state | F_BOLD, mask | F_BOLD);
    }

    public @NonNull Style faint() {
        return of(state | F_FAINT, mask | F_FAINT);
    }

    public @NonNull Style normal() {
        return of(state & ~(F_BOLD | F_FAINT), mask | F_BOLD | F_FAINT);
    }

    public @NonNull Style italic() {
        return of(state | F_ITALIC, mask | F_ITALIC);
    }

    public @NonNull Style italicOff() {
        return of(state & ~F_ITALIC, mask | F_ITALIC);
    }

    public @NonNull Style underlined() {
        return of(state | F_UNDERLINED, mask | F_UNDERLINED);
    }

    public @NonNull Style underlinedOff() {
        return of(state & ~F_UNDERLINED, mask | F_UNDERLINED);
    }

    public @NonNull Style blink() {
        return of(state | F_BLINK, mask | F_BLINK);
    }

    public @NonNull Style blinkOff() {
        return of(state & ~F_BLINK, mask | F_BLINK);
    }

    public @NonNull Style inverse() {
        return of(state | F_INVERSE, mask | F_INVERSE);
    }

    public @NonNull Style inverseOff() {
        return of(state & ~F_INVERSE, mask | F_INVERSE);
    }

    public @NonNull Style hidden() {
        return of(state | F_HIDDEN, mask | F_HIDDEN);
    }

    public @NonNull Style hiddenOff() {
        return of(state & ~F_HIDDEN, mask | F_HIDDEN);
    }

    public @NonNull Style strikethrough() {
        return of(state | F_STRIKETHROUGH, mask | F_STRIKETHROUGH);
    }

    public @NonNull Style strikethroughOff() {
        return of(state & ~F_STRIKETHROUGH, mask | F_STRIKETHROUGH);
    }

    public @NonNull Style fgColor(@NonNull Color color) {
        long newState = applyFgColor(state, color);
        return of(newState, mask | MASK_FG_COLOR);
    }

    public @NonNull Style bgColor(@NonNull Color color) {
        long newState = applyBgColor(state, color);
        return of(newState, mask | MASK_BG_COLOR);
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
        Color result;
        long mode = color & MASK_COLOR_MODE;
        if (mode == CM_INDEXED) {
            long paletteType = (color >> SHIFT_PALETTE_TYPE) & MASK_PALETTE_TYPE;
            if (paletteType == PALETTE_BASIC) {
                int intensity =
                        (int) ((color >> SHIFT_COLOR_BASIC_INTENSITY) & MASK_COLOR_BASIC_INTENSITY);
                int colorIndex =
                        (int) ((color >> SHIFT_COLOR_BASIC_INDEX) & MASK_COLOR_BASIC_INDEX);
                switch (intensity) {
                    case INTENSITY_NORMAL:
                        result = Color.basic(colorIndex, Color.BasicColor.Intensity.normal);
                        break;
                    case INTENSITY_DARK:
                        result = Color.basic(colorIndex, Color.BasicColor.Intensity.dark);
                        break;
                    case INTENSITY_BRIGHT:
                        result = Color.basic(colorIndex, Color.BasicColor.Intensity.bright);
                        break;
                    case INTENSITY_DEFAULT:
                    default:
                        result = Color.DEFAULT;
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

    public static Style parse(@NonNull String ansiSequence) {
        if (!ansiSequence.startsWith(Ansi.CSI) || !ansiSequence.endsWith("m")) {
            return UNSTYLED;
        }

        String content = ansiSequence.substring(2, ansiSequence.length() - 1);
        String[] parts = content.split("[;:]", -1);
        int[] codes = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                // Empty parameters are assumed to be 0 otherwise parse as integer
                codes[i] = parts[i].isEmpty() ? 0 : Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                codes[i] = -1; // Invalid code, will be ignored
            }
        }

        Style style = UNSTYLED;
        for (int i = 0; i < codes.length; i++) {
            int code = codes[i];
            switch (code) {
                case -1:
                    // Invalid code, ignore
                    break;
                case 0:
                    style = style.reset();
                    break;
                case 1:
                    style = style.bold();
                    break;
                case 2:
                    style = style.faint();
                    break;
                case 3:
                    style = style.italic();
                    break;
                case 4:
                    style = style.underlined();
                    break;
                case 5:
                    style = style.blink();
                    break;
                case 7:
                    style = style.inverse();
                    break;
                case 8:
                    style = style.hidden();
                    break;
                case 9:
                    style = style.strikethrough();
                    break;
                case 22:
                    style = style.normal();
                    break;
                case 23:
                    style = style.italicOff();
                    break;
                case 24:
                    style = style.underlinedOff();
                    break;
                case 25:
                    style = style.blinkOff();
                    break;
                case 27:
                    style = style.inverseOff();
                    break;
                case 28:
                    style = style.hiddenOff();
                    break;
                case 29:
                    style = style.strikethroughOff();
                    break;
                case 39:
                    style = style.fgColor(Color.DEFAULT);
                    break;
                case 49:
                    style = style.bgColor(Color.DEFAULT);
                    break;
                default:
                    if (code >= 30 && code <= 37) {
                        Color c = Color.basic(code - 30, Color.BasicColor.Intensity.normal);
                        style = style.fgColor(c);
                    } else if (code >= 90 && code <= 97) {
                        Color c = Color.basic(code - 90, Color.BasicColor.Intensity.bright);
                        style = style.fgColor(c);
                    } else if (code >= 40 && code <= 47) {
                        Color c = Color.basic(code - 40, Color.BasicColor.Intensity.normal);
                        style = style.bgColor(c);
                    } else if (code >= 100 && code <= 107) {
                        Color c = Color.basic(code - 100, Color.BasicColor.Intensity.bright);
                        style = style.bgColor(c);
                    } else if (code == 38 || code == 48) {
                        boolean isFg = (code == 38);
                        if (i + 1 < codes.length) {
                            int type = codes[i + 1];
                            if (type == 5 && i + 2 < codes.length) {
                                Color c = Color.indexed(codes[i + 2]);
                                if (isFg) {
                                    style = style.fgColor(c);
                                } else {
                                    style = style.bgColor(c);
                                }
                                i += 2;
                            } else if (type == 2 && i + 4 < codes.length) {
                                Color c = Color.rgb(codes[i + 2], codes[i + 3], codes[i + 4]);
                                if (isFg) {
                                    style = style.fgColor(c);
                                } else {
                                    style = style.bgColor(c);
                                }
                                i += 4;
                            }
                        }
                    }
                    break;
            }
        }
        return style;
    }

    private static long applyFgColor(long state, Color color) {
        long encoded = encodeColor(color);
        return (state & ~MASK_FG_COLOR) | (encoded << SHIFT_FG_COLOR);
    }

    private static long applyBgColor(long state, Color color) {
        long encoded = encodeColor(color);
        return (state & ~MASK_BG_COLOR) | (encoded << SHIFT_BG_COLOR);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Style)) return false;
        Style other = (Style) o;
        return this.state == other.state && this.mask == other.mask;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(state) * 31 + Long.hashCode(mask);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("StyleImpl{");
        if (state == F_UNKNOWN) {
            sb.append("UNKNOWN}");
            return sb.toString();
        }
        if (this.equals(DEFAULT)) {
            sb.append("DEFAULT}");
            return sb.toString();
        }
        if (affectsBold() && affectsFaint() && !isBold() && !isFaint()) {
            sb.append("normal, ");
        } else {
            if (affectsBold()) sb.append(isBold() ? "bold, " : "-bold, ");
            if (affectsFaint()) sb.append(isFaint() ? "faint, " : "-faint, ");
        }
        if (affectsItalic()) sb.append(isItalic() ? "italic, " : "-italic, ");
        if (affectsUnderlined()) sb.append(isUnderlined() ? "underlined, " : "-underlined, ");
        if (affectsBlink()) sb.append(isBlink() ? "blink, " : "-blink, ");
        if (affectsInverse()) sb.append(isInverse() ? "inverse, " : "-inverse, ");
        if (affectsHidden()) sb.append(isHidden() ? "hidden, " : "-hidden, ");
        if (affectsStrikethrough())
            sb.append(isStrikethrough() ? "strikethrough, " : "-strikethrough, ");
        if (affectsFgColor()) sb.append("fgColor=").append(fgColor()).append(", ");
        if (affectsBgColor()) sb.append("bgColor=").append(bgColor());
        if (sb.charAt(sb.length() - 2) == ',')
            sb.setLength(sb.length() - 2); // Remove trailing comma
        sb.append('}');
        return sb.toString();
    }

    public @NonNull Appendable toAnsi(Appendable appendable, Style currentStyle)
            throws IOException {
        if (this.equals(UNKNOWN)) {
            // Do nothing, we keep the current state
            return appendable;
        }
        List<Object> styles = new ArrayList<>();
        if (shouldApply(currentStyle, F_BOLD) || shouldApply(currentStyle, F_FAINT)) {
            boolean normal = false;
            if (!currentStyle.equals(UNKNOWN)
                    && ((!isBold() && currentStyle.isBold())
                            || (!isFaint() && currentStyle.isFaint()))) {
                // First we switch to NORMAL to clear both BOLD and FAINT
                styles.add(Ansi.NORMAL);
                normal = true;
            }
            // Now we set the needed styles
            if (isBold() && (normal || !currentStyle.affectsBold() || !currentStyle.isBold()))
                styles.add(Ansi.BOLD);
            if (isFaint() && (normal || !currentStyle.affectsFaint() || !currentStyle.isFaint()))
                styles.add(Ansi.FAINT);
        }
        if (shouldApply(currentStyle, F_ITALIC)) {
            if (isItalic()) {
                styles.add(Ansi.ITALICIZED);
            } else {
                styles.add(Ansi.NOTITALICIZED);
            }
        }
        if (shouldApply(currentStyle, F_UNDERLINED)) {
            if (isUnderlined()) {
                styles.add(Ansi.UNDERLINED);
            } else {
                styles.add(Ansi.NOTUNDERLINED);
            }
        }
        if (shouldApply(currentStyle, F_BLINK)) {
            if (isBlink()) {
                styles.add(Ansi.BLINK);
            } else {
                styles.add(Ansi.STEADY);
            }
        }
        if (shouldApply(currentStyle, F_INVERSE)) {
            if (isInverse()) {
                styles.add(Ansi.INVERSE);
            } else {
                styles.add(Ansi.POSITIVE);
            }
        }
        if (shouldApply(currentStyle, F_HIDDEN)) {
            if (isHidden()) {
                styles.add(Ansi.INVISIBLE);
            } else {
                styles.add(Ansi.VISIBLE);
            }
        }
        if (shouldApply(currentStyle, F_STRIKETHROUGH)) {
            if (isStrikethrough()) {
                styles.add(Ansi.CROSSEDOUT);
            } else {
                styles.add(Ansi.NOTCROSSEDOUT);
            }
        }
        if (affectsFgColor()
                && (!currentStyle.affectsFgColor() || !fgColor().equals(currentStyle.fgColor()))) {
            styles.add(fgColor().toAnsiFgArgs());
        }
        if (affectsBgColor()
                && (!currentStyle.affectsBgColor() || !bgColor().equals(currentStyle.bgColor()))) {
            styles.add(bgColor().toAnsiBgArgs());
        }
        return Ansi.style(appendable, styles.toArray());
    }

    private boolean shouldApply(Style otherStyle, long flag) {
        return affects(flag) && (!otherStyle.affects(flag) || is(flag) != otherStyle.is(flag));
    }
}
