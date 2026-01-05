package org.codejive.twinkle.ansi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.codejive.twinkle.util.Printable;
import org.jspecify.annotations.NonNull;

public class Style implements Printable {
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

    public static final Style UNSTYLED = new Style(0);
    public static final Style BOLD = UNSTYLED.bold();
    public static final Style FAINT = UNSTYLED.faint();
    public static final Style ITALIC = UNSTYLED.italic();
    public static final Style UNDERLINED = UNSTYLED.underlined();
    public static final Style BLINK = UNSTYLED.blink();
    public static final Style INVERSE = UNSTYLED.inverse();
    public static final Style HIDDEN = UNSTYLED.hidden();
    public static final Style STRIKETHROUGH = UNSTYLED.strikethrough();

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
        long newState = applyFgColor(state, color);
        return of(newState);
    }

    public @NonNull Color bgColor() {
        long bgc = ((state & MASK_BG_COLOR) >> SHIFT_BG_COLOR);
        return decodeColor(bgc);
    }

    public @NonNull Style bgColor(@NonNull Color color) {
        long newState = applyBgColor(state, color);
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

    public static long parse(@NonNull String ansiSequence) {
        return parse(F_UNSTYLED, ansiSequence);
    }

    public static long parse(long currentStyleState, @NonNull String ansiSequence) {
        if (!ansiSequence.startsWith(Ansi.CSI) || !ansiSequence.endsWith("m")) {
            return currentStyleState;
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

        long state = currentStyleState;
        for (int i = 0; i < codes.length; i++) {
            int code = codes[i];
            switch (code) {
                case -1:
                    // Invalid code, ignore
                    break;
                case 0:
                    state = 0;
                    break;
                case 1:
                    state |= F_BOLD;
                    break;
                case 2:
                    state |= F_FAINT;
                    break;
                case 3:
                    state |= F_ITALIC;
                    break;
                case 4:
                    state |= F_UNDERLINED;
                    break;
                case 5:
                    state |= F_BLINK;
                    break;
                case 7:
                    state |= F_INVERSE;
                    break;
                case 8:
                    state |= F_HIDDEN;
                    break;
                case 9:
                    state |= F_STRIKETHROUGH;
                    break;
                case 22:
                    state &= ~(F_BOLD | F_FAINT);
                    break;
                case 23:
                    state &= ~F_ITALIC;
                    break;
                case 24:
                    state &= ~F_UNDERLINED;
                    break;
                case 25:
                    state &= ~F_BLINK;
                    break;
                case 27:
                    state &= ~F_INVERSE;
                    break;
                case 28:
                    state &= ~F_HIDDEN;
                    break;
                case 29:
                    state &= ~F_STRIKETHROUGH;
                    break;
                case 39:
                    state &= ~MASK_FG_COLOR;
                    break;
                case 49:
                    state &= ~MASK_BG_COLOR;
                    break;
                default:
                    if (code >= 30 && code <= 37) {
                        Color c = Color.basic(code - 30, Color.BasicColor.Intensity.normal);
                        state = applyFgColor(state, c);
                    } else if (code >= 90 && code <= 97) {
                        Color c = Color.basic(code - 90, Color.BasicColor.Intensity.bright);
                        state = applyFgColor(state, c);
                    } else if (code >= 40 && code <= 47) {
                        Color c = Color.basic(code - 40, Color.BasicColor.Intensity.normal);
                        state = applyBgColor(state, c);
                    } else if (code >= 100 && code <= 107) {
                        Color c = Color.basic(code - 100, Color.BasicColor.Intensity.bright);
                        state = applyBgColor(state, c);
                    } else if (code == 38 || code == 48) {
                        boolean isFg = (code == 38);
                        if (i + 1 < codes.length) {
                            int type = codes[i + 1];
                            if (type == 5 && i + 2 < codes.length) {
                                Color c = Color.indexed(codes[i + 2]);
                                if (isFg) {
                                    state = applyFgColor(state, c);
                                } else {
                                    state = applyBgColor(state, c);
                                }
                                i += 2;
                            } else if (type == 2 && i + 4 < codes.length) {
                                Color c = Color.rgb(codes[i + 2], codes[i + 3], codes[i + 4]);
                                if (isFg) {
                                    state = applyFgColor(state, c);
                                } else {
                                    state = applyBgColor(state, c);
                                }
                                i += 4;
                            }
                        }
                    }
                    break;
            }
        }
        return state;
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
        if (state == F_UNKNOWN) {
            sb.append("UNKNOWN}");
            return sb.toString();
        }
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

    @Override
    public @NonNull String toAnsiString() {
        return toAnsiString(UNSTYLED);
    }

    @Override
    public @NonNull Appendable toAnsi(Appendable appendable) throws IOException {
        try {
            return toAnsi(appendable, Style.UNSTYLED);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull String toAnsiString(long currentStyleState) {
        try {
            return toAnsi(new StringBuilder(), currentStyleState).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull Appendable toAnsi(Appendable appendable, long currentStyleState)
            throws IOException {
        if (state == F_UNKNOWN) {
            // Do nothing, we keep the current state
            return appendable;
        }
        if (currentStyleState == F_UNKNOWN) {
            appendable.append(Ansi.STYLE_RESET);
            currentStyleState = F_UNSTYLED;
        }
        List<Object> styles = new ArrayList<>();
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
            styles.add(fgColor().toAnsiFgArgs());
        }
        if ((currentStyleState & MASK_BG_COLOR) != (state & MASK_BG_COLOR)) {
            styles.add(bgColor().toAnsiBgArgs());
        }
        return Ansi.style(appendable, styles.toArray());
    }
}
