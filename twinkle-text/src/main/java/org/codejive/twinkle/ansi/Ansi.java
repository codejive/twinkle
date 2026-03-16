package org.codejive.twinkle.ansi;

import static org.codejive.twinkle.ansi.Constants.*;

public class Ansi {

    /**
     * Returns the ANSI escape sequence to reset all styles and colors to their defaults.
     *
     * @return the ANSI escape sequence to reset all styles and colors
     */
    public static String reset() {
        return CSI + RESET + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to enable bold text style.
     *
     * @return the ANSI escape sequence to enable bold text style
     */
    public static String bold() {
        return CSI + BOLD + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to enable faint (dim) text style.
     *
     * @return the ANSI escape sequence to enable faint (dim) text style
     */
    public static String faint() {
        return CSI + FAINT + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to disable bold and faint text styles, returning to normal
     *
     * @return the ANSI escape sequence to disable bold and faint text styles
     */
    public static String normal() {
        return CSI + NORMAL + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to enable italic text style.
     *
     * @return the ANSI escape sequence to enable italic text style
     */
    public static String italic() {
        return CSI + ITALICIZED + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to disable italic text style.
     *
     * @return the ANSI escape sequence to disable italic text style
     */
    public static String italicOff() {
        return CSI + NOTITALICIZED + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to enable underlined text style.
     *
     * @return the ANSI escape sequence to enable underlined text style
     */
    public static String underlined() {
        return CSI + UNDERLINED + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to disable underlined text style.
     *
     * @return the ANSI escape sequence to disable underlined text style
     */
    public static String underlinedOff() {
        return CSI + NOTUNDERLINED + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to enable blinking text style.
     *
     * @return the ANSI escape sequence to enable blinking text style
     */
    public static String blink() {
        return CSI + BLINK + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to disable blinking text style.
     *
     * @return the ANSI escape sequence to disable blinking text style
     */
    public static String blinkOff() {
        return CSI + STEADY + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to enable double underlined text style.
     *
     * @return the ANSI escape sequence to enable double underlined text style
     */
    public static String inverse() {
        return CSI + INVERSE + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to disable inverse text style.
     *
     * @return the ANSI escape sequence to disable inverse text style
     */
    public static String inverseOff() {
        return CSI + POSITIVE + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to enable invisible (hidden) text style.
     *
     * @return the ANSI escape sequence to enable invisible (hidden) text style
     */
    public static String hidden() {
        return CSI + INVISIBLE + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to disable invisible (hidden) text style.
     *
     * @return the ANSI escape sequence to disable invisible (hidden) text style
     */
    public static String hiddenOff() {
        return CSI + VISIBLE + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to enable crossed-out (strikethrough) text style.
     *
     * @return the ANSI escape sequence to enable crossed-out (strikethrough) text style
     */
    public static String strikethrough() {
        return CSI + CROSSEDOUT + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to disable crossed-out (strikethrough) text style.
     *
     * @return the ANSI escape sequence to disable crossed-out (strikethrough) text style
     */
    public static String strikethroughOff() {
        return CSI + NOTCROSSEDOUT + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to reset the foreground color to the default.
     *
     * @return the ANSI escape sequence to reset the foreground color to the default
     */
    public static String defaultForeground() {
        return CSI + DEFAULT_FOREGROUND + STYLE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to reset the background color to the default.
     *
     * @return the ANSI escape sequence to reset the background color to the default
     */
    public static String defaultBackground() {
        return CSI + DEFAULT_BACKGROUND + STYLE_CMD;
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
        return CSI + (row + 1) + ";" + (col + 1) + CURSOR_POSITION_CMD;
    }

    public static String cursorHome() {
        return CSI + CURSOR_POSITION_CMD;
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
        return CSI + (col + 1) + CURSOR_COLUMN_CMD;
    }

    /**
     * Returns the ANSI escape sequence for moving the cursor up by the specified amount.
     *
     * @param amount the number of positions to move the cursor
     * @return the ANSI escape sequence for moving the cursor up
     */
    public static String cursorUp(int amount) {
        return cursorMove(CURSOR_UP_CMD, amount);
    }

    /**
     * Returns the ANSI escape sequence for moving the cursor down by the specified amount.
     *
     * @param amount the number of positions to move the cursor
     * @return the ANSI escape sequence for moving the cursor down
     */
    public static String cursorDown(int amount) {
        return cursorMove(CURSOR_DOWN_CMD, amount);
    }

    /**
     * Returns the ANSI escape sequence for moving the cursor forward by the specified amount.
     *
     * @param amount the number of positions to move the cursor
     * @return the ANSI escape sequence for moving the cursor forward
     */
    public static String cursorForward(int amount) {
        return cursorMove(CURSOR_FORWARD_CMD, amount);
    }

    /**
     * Returns the ANSI escape sequence for moving the cursor backward by the specified amount.
     *
     * @param amount the number of positions to move the cursor
     * @return the ANSI escape sequence for moving the cursor backward
     */
    public static String cursorBackward(int amount) {
        return cursorMove(CURSOR_BACKWARD_CMD, amount);
    }

    /**
     * Returns the ANSI escape sequence to hide the cursor.
     *
     * @return the ANSI escape sequence to hide the cursor
     */
    public static String cursorHide() {
        return CSI + CURSOR_HIDE;
    }

    /**
     * Returns the ANSI escape sequence to show the cursor.
     *
     * @return the ANSI escape sequence to show the cursor
     */
    public static String cursorShow() {
        return CSI + CURSOR_SHOW;
    }

    /**
     * Returns the ANSI escape sequence to save the current cursor position.
     *
     * @return the ANSI escape sequence to save the current cursor position
     */
    public static String cursorSave() {
        return "" + ESC + CURSOR_SAVE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to restore the cursor to the last saved position.
     *
     * @return the ANSI escape sequence to restore the cursor
     */
    public static String cursorRestore() {
        return "" + ESC + CURSOR_RESTORE_CMD;
    }

    /**
     * Returns the ANSI escape sequence to clear the entire screen.
     *
     * @return the ANSI escape sequence to clear the entire screen
     */
    public static String clearScreen() {
        return CSI + SCREEN_ERASE_FULL;
    }

    /**
     * Returns the ANSI escape sequence to switch to the alternate screen buffer.
     *
     * @return the ANSI escape sequence to switch to the alternate screen buffer
     */
    public static String screenSave() {
        return CSI + SCREEN_SAVE;
    }

    /**
     * Returns an alternative ANSI escape sequence to switch to the alternate screen buffer.
     *
     * @return the ANSI escape sequence to switch to the alternate screen buffer
     */
    public static String screenSaveAlt() {
        return CSI + SCREEN_SAVE_ALT;
    }

    /**
     * Returns the ANSI escape sequence to switch back to the main screen buffer.
     *
     * @return the ANSI escape sequence to switch back to the main screen buffer
     */
    public static String screenRestore() {
        return CSI + SCREEN_RESTORE;
    }

    /**
     * Returns an alternative ANSI escape sequence to switch back to the main screen buffer.
     *
     * @return the ANSI escape sequence to switch back to the main screen buffer
     */
    public static String screenRestoreAlt() {
        return CSI + SCREEN_RESTORE_ALT;
    }

    /**
     * Returns the ANSI escape sequence to enable automatic line wrapping. When enabled, the cursor
     * will automatically move to the beginning of the next line when it reaches the end of the
     * current line.
     *
     * @return the ANSI escape sequence to enable automatic line wrapping
     */
    public static String autoWrap() {
        return CSI + LINE_WRAP_ON;
    }

    /**
     * Returns the ANSI escape sequence to disable automatic line wrapping. When disabled, the
     * cursor will stay on the same line when it reaches the end.
     *
     * @return the ANSI escape sequence to disable automatic line wrapping
     */
    public static String autoWrapOff() {
        return CSI + LINE_WRAP_OFF;
    }

    /**
     * Returns the ANSI escape sequence to create a hyperlink with the specified URL.
     *
     * @param url the URL for the hyperlink
     * @return the ANSI escape sequence to create a hyperlink
     */
    public static String link(String url) {
        return OSC + HYPERLINK + ";" + url + OSC_END;
    }

    /**
     * Returns the ANSI escape sequence to create a hyperlink with the specified URL and ID.
     *
     * @param url the URL for the hyperlink
     * @param id the ID for the hyperlink
     * @return the ANSI escape sequence to create a hyperlink with an ID
     */
    public static String link(String url, String id) {
        return OSC + HYPERLINK + "id=" + id + ";" + url + OSC_END;
    }

    /**
     * Returns the ANSI escape sequence to end a hyperlink.
     *
     * @return the ANSI escape sequence to end a hyperlink
     */
    public static String linkEnd() {
        return OSC + HYPERLINK + ";" + OSC_END;
    }

    /**
     * Returns the ANSI escape sequence to enable SGR extended mouse mode tracking. This should be
     * combined with one of the mouse tracking modes (e.g., mouseTrackingEnable()). SGR mode
     * provides a more reliable encoding that supports coordinates beyond 223.
     *
     * @return the ANSI escape sequence to enable SGR extended mouse mode
     */
    public static String mouseSgrModeEnable() {
        return CSI + MOUSE_SGR_EXT_MODE_ENABLE;
    }

    /**
     * Returns the ANSI escape sequence to disable SGR extended mouse mode tracking.
     *
     * @return the ANSI escape sequence to disable SGR extended mouse mode
     */
    public static String mouseSgrModeDisable() {
        return CSI + MOUSE_SGR_EXT_MODE_DISABLE;
    }

    /**
     * Returns the ANSI escape sequence to enable basic mouse tracking. This will report button
     * press and release events.
     *
     * @return the ANSI escape sequence to enable basic mouse tracking
     */
    public static String mouseTrackingEnable() {
        return CSI + MOUSE_BUTTON_TRACKING_ENABLE;
    }

    /**
     * Returns the ANSI escape sequence to disable basic mouse tracking.
     *
     * @return the ANSI escape sequence to disable basic mouse tracking
     */
    public static String mouseTrackingDisable() {
        return CSI + MOUSE_BUTTON_TRACKING_DISABLE;
    }

    /**
     * Returns the ANSI escape sequence to enable button event tracking. This will report button
     * press, release, and drag events.
     *
     * @return the ANSI escape sequence to enable button event tracking
     */
    public static String mouseButtonTrackingEnable() {
        return CSI + MOUSE_BUTTON_AND_DRAG_TRACKING_ENABLE;
    }

    /**
     * Returns the ANSI escape sequence to disable button event tracking.
     *
     * @return the ANSI escape sequence to disable button event tracking
     */
    public static String mouseButtonTrackingDisable() {
        return CSI + MOUSE_BUTTON_AND_DRAG_TRACKING_DISABLE;
    }

    /**
     * Returns the ANSI escape sequence to enable any event tracking. This will report all mouse
     * events, including movement without buttons pressed.
     *
     * @return the ANSI escape sequence to enable any event tracking
     */
    public static String mouseAnyEventTrackingEnable() {
        return CSI + MOUSE_ANY_EVENT_TRACKING_ENABLE;
    }

    /**
     * Returns the ANSI escape sequence to disable any event tracking.
     *
     * @return the ANSI escape sequence to disable any event tracking
     */
    public static String mouseAnyEventTrackingDisable() {
        return CSI + MOUSE_ANY_EVENT_TRACKING_DISABLE;
    }

    private Ansi() {}
}
