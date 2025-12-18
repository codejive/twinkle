package org.codejive.twinkle.ansi;

public class Ansi {
    public static final char ESC = 27;

    public static final String CSI = ESC + "["; // Control Sequence Introducer

    // Style codes
    public static final int RESET = 0; // Reset all attributes
    public static final int BOLD = 1; // Bold text
    public static final int FAINT = 2; // Faint/dim text
    public static final int ITALICIZED = 3; // Italic text
    public static final int UNDERLINED = 4; // Underlined text
    public static final int BLINK = 5; // Blinking text
    public static final int INVERSE = 7; // Reversed foreground
    public static final int INVISIBLE = 8; // Invisible text
    public static final int CROSSEDOUT = 9; // Strike-through
    public static final int DOUBLEUNDERLINE = 21; // Double underline
    public static final int NORMAL = 22; // Normal intensity
    public static final int NOTITALICIZED = 23; // Not italic
    public static final int NOTUNDERLINED = 24; // Not underlined
    public static final int STEADY = 25; // Not blinking
    public static final int POSITIVE = 27; // Positive image
    public static final int VISIBLE = 28; // Visible text
    public static final int NOTCROSSEDOUT = 29; // Not strike-through
    public static final int DEFAULT_FOREGROUND = 39;
    public static final int DEFAULT_BACKGROUND = 49;

    public static final String STYLE_RESET = style(RESET); // Reset all attributes
    public static final String STYLE_DEFAULT_FOREGROUND =
            style(DEFAULT_FOREGROUND); // Reset all attributes
    public static final String STYLE_DEFAULT_BACKGROUND =
            style(DEFAULT_BACKGROUND); // Reset all attributes

    public static final int BLACK = 0;
    public static final int RED = 1;
    public static final int GREEN = 2;
    public static final int YELLOW = 3;
    public static final int BLUE = 4;
    public static final int MAGENTA = 5;
    public static final int CYAN = 6;
    public static final int WHITE = 7;

    public static final int FOREGROUND_BASE = 30;
    public static final int FOREGROUND_DARK_BASE = 60;
    public static final int FOREGROUND_BRIGHT_BASE = 90;
    public static final int BACKGROUND_BASE = 40;
    public static final int BACKGROUND_DARK_BASE = 70;
    public static final int BACKGROUND_BRIGHT_BASE = 100;

    public static final int FOREGROUND_COLORS = 38;
    public static final int BACKGROUND_COLORS = 48;
    public static final int COLORS_RGB = 2;
    public static final int COLORS_INDEXED = 5;

    // OSC 8 hyperlinks (BEL-terminated)
    public static String osc8Open(String url) {
        return "\u001B]8;;" + url + "\u0007";
    }

    public static String osc8Close() {
        return "\u001B]8;;\u0007";
    }

    public static String style(Object... styles) {
        if (styles == null || styles.length == 0) {
            return "";
        }
        return style(new StringBuilder(), styles).toString();
    }

    public static StringBuilder style(StringBuilder sb, Object... styles) {
        if (styles == null || styles.length == 0) {
            return sb;
        }
        sb.append(CSI);
        for (int i = 0; i < styles.length; i++) {
            sb.append(styles[i]);
            if (i < styles.length - 1) {
                sb.append(";");
            }
        }
        sb.append("m");
        return sb;
    }

    public static String foreground(int index) {
        return String.valueOf(FOREGROUND_BASE + index);
    }

    public static String foregroundDark(int index) {
        return String.valueOf(FOREGROUND_DARK_BASE + index);
    }

    public static String foregroundBright(int index) {
        return String.valueOf(FOREGROUND_BRIGHT_BASE + index);
    }

    public static String background(int index) {
        return String.valueOf(BACKGROUND_BASE + index);
    }

    public static String backgroundDark(int index) {
        return String.valueOf(BACKGROUND_DARK_BASE + index);
    }

    public static String backgroundBright(int index) {
        return String.valueOf(BACKGROUND_BRIGHT_BASE + index);
    }

    public static String foregroundIndexed(int index) {
        // Standard SGR: 38;5;<n>
        return FOREGROUND_COLORS + ";" + COLORS_INDEXED + ";" + index;
    }

    public static String foregroundRgb(int r, int g, int b) {
        // Standard SGR truecolor: 38;2;<r>;<g>;<b>
        return FOREGROUND_COLORS + ";" + COLORS_RGB + ";" + r + ";" + g + ";" + b;
    }

    public static String backgroundIndexed(int index) {
        // Standard SGR: 48;5;<n>
        return BACKGROUND_COLORS + ";" + COLORS_INDEXED + ";" + index;
    }

    public static String backgroundRgb(int r, int g, int b) {
        // Standard SGR truecolor: 48;2;<r>;<g>;<b>
        return BACKGROUND_COLORS + ";" + COLORS_RGB + ";" + r + ";" + g + ";" + b;
    }

    private Ansi() {}
}
