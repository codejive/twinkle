package org.codejive.twinkle.ansi;

import static org.codejive.twinkle.ansi.Constants.*;

import java.io.IOException;

public class Ansi {
    public static final String STYLE_RESET = styles(RESET); // Reset all attributes
    public static final String STYLE_DEFAULT_FOREGROUND =
            styles(DEFAULT_FOREGROUND); // Reset foreground color to default
    public static final String STYLE_DEFAULT_BACKGROUND =
            styles(DEFAULT_BACKGROUND); // Reset background color

    /**
     * Returns the ANSI escape sequence for the given styles. The styles can be any combination of
     * the style constants defined in the Constants class, such as BOLD, UNDERLINED, or the output
     * of the color functions like foregroundArg(). The output is a string that can be used in the
     * console to apply the specified styles to the text that follows.
     *
     * @param styles the style codes to apply
     * @return the ANSI escape sequence for the given styles
     */
    public static String styles(Object... styles) {
        if (styles == null || styles.length == 0) {
            return "";
        }
        return styles(new StringBuilder(), styles).toString();
    }

    /**
     * Appends the ANSI escape sequence for the given styles to the provided Appendable. The styles
     * can be any combination of the style constants defined in the Constants class, such as BOLD,
     * UNDERLINED, or the output of the color functions like foregroundArg(). The output will be
     * passed to the provided Appendable.
     *
     * @param appendable the Appendable to which the ANSI escape sequence will be appended
     * @param styles the style codes to apply
     * @return the provided Appendable with the ANSI escape sequence appended
     */
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

    /**
     * Returns the ANSI code for the given basic color. The index should be between 0-7. The output
     * is a string that can be used in the styles() method to create the final ANSI escape sequence.
     *
     * @param index the color index (0-7)
     * @return the ANSI code for the given basic color
     */
    public static String foregroundArg(int index) {
        return String.valueOf(FOREGROUND_BASE + index);
    }

    /**
     * Returns the ANSI code for the given dark color. The index should be between 0-7. The output
     * is a string that can be used in the styles() method to create the final ANSI escape sequence.
     *
     * @param index the color index (0-7)
     * @return the ANSI code for the given dark color
     */
    public static String foregroundDarkArg(int index) {
        return String.valueOf(FOREGROUND_DARK_BASE + index);
    }

    /**
     * Returns the ANSI code for the given bright color. The index should be between 0-7. The output
     * is a string that can be used in the styles() method to create the final ANSI escape sequence.
     *
     * @param index the color index (0-7)
     * @return the ANSI code for the given bright color
     */
    public static String foregroundBrightArg(int index) {
        return String.valueOf(FOREGROUND_BRIGHT_BASE + index);
    }

    /**
     * Returns the ANSI code for the given basic background color. The index should be between 0-7.
     * The output is a string that can be used in the styles() method to create the final ANSI
     * escape sequence.
     *
     * @param index the color index (0-7)
     * @return the ANSI code for the given basic background color
     */
    public static String backgroundArg(int index) {
        return String.valueOf(BACKGROUND_BASE + index);
    }

    /**
     * Returns the ANSI code for the given dark background color. The index should be between 0-7.
     * The output is a string that can be used in the styles() method to create the final ANSI
     * escape sequence.
     *
     * @param index the color index (0-7)
     * @return the ANSI code for the given dark background color
     */
    public static String backgroundDarkArg(int index) {
        return String.valueOf(BACKGROUND_DARK_BASE + index);
    }

    /**
     * Returns the ANSI code for the given bright background color. The index should be between 0-7.
     * The output is a string that can be used in the styles() method to create the final ANSI
     * escape sequence.
     *
     * @param index the color index (0-7)
     * @return the ANSI code for the given bright background color
     */
    public static String backgroundBrightArg(int index) {
        return String.valueOf(BACKGROUND_BRIGHT_BASE + index);
    }

    /**
     * Returns the ANSI code for the given indexed foreground color. The index should be between
     * 0-255. The indexes 0-7 are the standard colors, 8-15 are the bright versions of the standard
     * colors, 16-231 are a 6x6x6 color cube, and 232-255 are a grayscale ramp. The output is a
     * string that can be used in the styles() method to create the final ANSI escape sequence.
     *
     * @param index the color index (0-255)
     * @return the ANSI code for the given indexed foreground color
     */
    public static String foregroundIndexedArg(int index) {
        return FOREGROUND_COLORS + ";" + COLORS_INDEXED + ";" + index;
    }

    /**
     * Returns the ANSI code for the given indexed background color. The index should be between
     * 0-255. The indexes 0-7 are the standard colors, 8-15 are the bright versions of the standard
     * colors, 16-231 are a 6x6x6 color cube, and 232-255 are a grayscale ramp. The output is a
     * string that can be used in the styles() method to create the final ANSI escape sequence.
     *
     * @param index the color index (0-255)
     * @return the ANSI code for the given indexed background color
     */
    public static String backgroundIndexedArg(int index) {
        return BACKGROUND_COLORS + ";" + COLORS_INDEXED + ";" + index;
    }

    /**
     * Returns the ANSI code for the given RGB foreground color. The r, g, and b values should be
     * between 0-255. The output is a string that can be used in the styles() method to create the
     * final ANSI escape sequence.
     *
     * @param r the red component (0-255)
     * @param g the green component (0-255)
     * @param b the blue component (0-255)
     * @return the ANSI code for the given RGB foreground color
     */
    public static String foregroundRgbArg(int r, int g, int b) {
        return FOREGROUND_COLORS + ";" + COLORS_RGB + ";" + r + ";" + g + ";" + b;
    }

    /**
     * Returns the ANSI code for the given RGB background color. The r, g, and b values should be
     * between 0-255. The output is a string that can be used in the styles() method to create the
     * final ANSI escape sequence.
     *
     * @param r the red component (0-255)
     * @param g the green component (0-255)
     * @param b the blue component (0-255)
     * @return the ANSI code for the given RGB background color
     */
    public static String backgroundRgbArg(int r, int g, int b) {
        return BACKGROUND_COLORS + ";" + COLORS_RGB + ";" + r + ";" + g + ";" + b;
    }

    /**
     * Returns the ANSI escape sequence for moving the cursor in the specified direction by 1. The
     * direction is determined by the command parameter, which can be one of the cursor movement
     * commands defined in the Constants class, such as CURSOR_UP, CURSOR_DOWN, etc.
     *
     * @param command the cursor movement command (e.g. CURSOR_UP, CURSOR_DOWN, etc.)
     * @return the ANSI escape sequence for moving the cursor
     */
    public static String cursorMove(char command) {
        return cursorMove(command, 1);
    }

    /**
     * Returns the ANSI escape sequence for moving the cursor in the specified direction by the
     * specified amount. The direction is determined by the command parameter, which can be one of
     * the cursor movement commands defined in the Constants class, such as CURSOR_UP, CURSOR_DOWN,
     * etc. The amount parameter specifies how many positions to move the cursor.
     *
     * @param command the cursor movement command (e.g. CURSOR_UP, CURSOR_DOWN, etc.)
     * @param amount the number of positions to move the cursor
     * @return the ANSI escape sequence for moving the cursor
     */
    public static String cursorMove(char command, int amount) {
        return CSI + amount + command;
    }

    /**
     * Returns the ANSI escape sequence for positioning the cursor at the specified column and row.
     * Coordinates are 0-based (the top-left corner is 0,0). The ANSI sequence will use 1-based
     * coordinates internally as per the ANSI standard.
     *
     * @param col the column (0-based, 0 is leftmost)
     * @param row the row (0-based, 0 is topmost)
     * @return the ANSI escape sequence for positioning the cursor
     */
    public static String cursorPos(int col, int row) {
        return CSI + (row + 1) + ";" + (col + 1) + CURSOR_POSITION;
    }

    public static String cursorHome() {
        return CSI + CURSOR_POSITION;
    }

    /**
     * Returns the ANSI escape sequence for positioning the cursor at the specified column. The
     * column is 0-based (0 is leftmost). The ANSI sequence will use 1-based coordinates internally
     * as per the ANSI standard.
     *
     * @param col the column (0-based, 0 is leftmost)
     * @return the ANSI escape sequence for positioning the cursor
     */
    public static String cursorToColumn(int col) {
        return CSI + (col + 1) + CURSOR_COLUMN;
    }

    public static String cursorUp(int amount) {
        return cursorMove(CURSOR_UP, amount);
    }

    public static String cursorDown(int amount) {
        return cursorMove(CURSOR_DOWN, amount);
    }

    public static String cursorForward(int amount) {
        return cursorMove(CURSOR_FORWARD, amount);
    }

    public static String cursorBackward(int amount) {
        return cursorMove(CURSOR_BACKWARD, amount);
    }

    public static String cursorHide() {
        return CSI + CURSOR_HIDE;
    }

    public static String cursorShow() {
        return CSI + CURSOR_SHOW;
    }

    public static String cursorSave() {
        return "" + ESC + CURSOR_SAVE;
    }

    public static String cursorRestore() {
        return "" + ESC + CURSOR_RESTORE;
    }

    public static String clearScreen() {
        return CSI + SCREEN_ERASE_FULL;
    }

    public static String screenSave() {
        return CSI + SCREEN_SAVE;
    }

    public static String screenRestore() {
        return CSI + SCREEN_RESTORE;
    }

    public static String autoWrap(boolean enabled) {
        return CSI + (enabled ? LINE_WRAP_ON : LINE_WRAP_OFF);
    }

    public static String linkStart(String url) {
        return OSC + "8;;" + url + OSC_END;
    }

    public static String linkStart(String url, String id) {
        return OSC + "8;id=" + id + ";" + url + OSC_END;
    }

    public static String linkEnd() {
        return OSC + "8;;" + OSC_END;
    }

    private Ansi() {}
}
