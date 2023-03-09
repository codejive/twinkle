package org.codejive.twinkle.ansi;

import static org.codejive.twinkle.ansi.Constants.*;

import java.io.IOException;

public class Ansi {
    public static final String STYLE_RESET = styles(RESET); // Reset all attributes
    public static final String STYLE_DEFAULT_FOREGROUND =
            styles(DEFAULT_FOREGROUND); // Reset foreground color to default
    public static final String STYLE_DEFAULT_BACKGROUND =
            styles(DEFAULT_BACKGROUND); // Reset background color

    public static String styles(Object... styles) {
        if (styles == null || styles.length == 0) {
            return "";
        }
        return styles(new StringBuilder(), styles).toString();
    }

    public static Appendable styles(Appendable appendable, Object... styles) {
        if (styles == null || styles.length == 0) {
            return appendable;
        }
        try {
            appendable.append(CSI);
            for (int i = 0; i < styles.length; i++) {
                appendable.append(styles[i].toString());
                if (i < styles.length - 1) {
                    appendable.append(";");
                }
            }
            appendable.append("m");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return appendable;
    }

    public static String foreground(int index) {
        return styles(FOREGROUND_BASE + index);
    }

    public static Appendable foreground(Appendable appendable, int index) {
        return styles(appendable, FOREGROUND_BASE + index);
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
        return FOREGROUND_COLORS + ";" + COLORS_INDEXED + ";" + index;
    }

    public static String foregroundRgb(int r, int g, int b) {
        return FOREGROUND_COLORS + ";" + COLORS_RGB + ";" + r + ";" + g + ";" + b;
    }

    public static String backgroundIndexed(int index) {
        return BACKGROUND_COLORS + ";" + COLORS_INDEXED + ";" + index;
    }

    public static String backgroundRgb(int r, int g, int b) {
        return BACKGROUND_COLORS + ";" + COLORS_RGB + ";" + r + ";" + g + ";" + b;
    }

    public static String linkStart(String url) {
        return OSC + "8;;" + url + OSC_END;
    }

    public static String linkEnd() {
        return OSC + "8;;" + OSC_END;
    }

    public static String cursorMove(char command) {
        return cursorMove(command, 1);
    }

    public static String cursorMove(char command, int amount) {
        return CSI + amount + command;
    }

    public static String cursorPos(int row, int col) {
        return CSI + row + ";" + col + "H";
    }

    public static String cursorHome() {
        return CSI + "H";
    }

    public static String hideCursor() {
        return CSI + "?25l";
    }

    public static String showCursor() {
        return CSI + "?25h";
    }

    public static String clearScreen() {
        return CSI + "2J";
    }

    public static String autoWrap(boolean enabled) {
        return CSI + (enabled ? "?7h" : "?7l");
    }

    private Ansi() {}
}
