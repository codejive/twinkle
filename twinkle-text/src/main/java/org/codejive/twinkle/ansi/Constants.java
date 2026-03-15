package org.codejive.twinkle.ansi;

public class Constants {
    public static final char ESC = '\u001B';

    public static final String CSI = ESC + "["; // Control Sequence Introducer
    public static final String OSC = ESC + "]"; // Operating System Command
    public static final String OSC_END = "\u0007"; // Bell character
    public static final String OSC_END_ALT = ESC + "\\"; // String Terminator (ST)

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
    public static final int DEFAULT_FOREGROUND = 39; // Default foreground color
    public static final int DEFAULT_BACKGROUND = 49; // Default background color

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

    public static final char CURSOR_UP = 'A';
    public static final char CURSOR_DOWN = 'B';
    public static final char CURSOR_FORWARD = 'C';
    public static final char CURSOR_BACKWARD = 'D';
    public static final char CURSOR_NEXT_LINE = 'E';
    public static final char CURSOR_PREV_LINE = 'F';
    public static final char CURSOR_COLUMN = 'G';
    public static final char CURSOR_POSITION = 'H';
    public static final char CURSOR_POSITION_ALT = 'f';
    public static final char CURSOR_SAVE = '7'; // Note: ESC+7, not CSI+7 !
    public static final char CURSOR_RESTORE = '8'; // Note: ESC+8, not CSI+8 !
    public static final char CURSOR_UP_WITH_SCROLL = 'M'; // Note: ESC+M, not CSI+M !

    public static final String CURSOR_HIDE = "?25l";
    public static final String CURSOR_SHOW = "?25h";

    public static final String SCREEN_ERASE = "J"; // Same as SCREEN_ERASE_END
    public static final String SCREEN_ERASE_END = "0J";
    public static final String SCREEN_ERASE_START = "1J";
    public static final String SCREEN_ERASE_FULL = "2J";
    public static final String SCREEN_ERASE_SAVED_LINES = "3J";

    public static final String SCREEN_SAVE = "?1049h";
    public static final String SCREEN_SAVE_ALT = "?47h";
    public static final String SCREEN_RESTORE = "?1049l";
    public static final String SCREEN_RESTORE_ALT = "?47l";

    public static final String LINE_ERASE = "K"; // Same as LINE_ERASE_END
    public static final String LINE_ERASE_END = "0K";
    public static final String LINE_ERASE_START = "1K";
    public static final String LINE_ERASE_FULL = "2K";

    public static final String LINE_WRAP_ON = "=7h";
    public static final String LINE_WRAP_OFF = "=7l";

    public static final String HYPERLINK = "8;";

    // Mouse tracking modes
    public static final String MOUSE_BUTTON_TRACKING_ENABLE =
            "?1000h"; // Enable basic mouse tracking
    public static final String MOUSE_BUTTON_TRACKING_DISABLE =
            "?1000l"; // Disable basic mouse tracking
    public static final String MOUSE_BUTTON_AND_DRAG_TRACKING_ENABLE =
            "?1002h"; // Enable button event and drag tracking
    public static final String MOUSE_BUTTON_AND_DRAG_TRACKING_DISABLE =
            "?1002l"; // Disable button event and drag tracking
    public static final String MOUSE_ANY_EVENT_TRACKING_ENABLE =
            "?1003h"; // Enable any event tracking
    public static final String MOUSE_ANY_EVENT_TRACKING_DISABLE =
            "?1003l"; // Disable any event tracking
    public static final String MOUSE_SGR_EXT_MODE_ENABLE = "?1006h"; // Enable SGR extended mode
    public static final String MOUSE_SGR_EXT_MODE_DISABLE = "?1006l"; // Disable SGR extended mode

    // Mouse button codes (for SGR mode)
    public static final int MOUSE_BUTTON_LEFT = 0;
    public static final int MOUSE_BUTTON_MIDDLE = 1;
    public static final int MOUSE_BUTTON_RIGHT = 2;
    public static final int MOUSE_BUTTON_RELEASE = 3;
    public static final int MOUSE_SCROLL_UP = 64;
    public static final int MOUSE_SCROLL_DOWN = 65;

    // Mouse modifier flags
    public static final int MOUSE_MODIFIER_SHIFT = 4;
    public static final int MOUSE_MODIFIER_ALT = 8;
    public static final int MOUSE_MODIFIER_CTRL = 16;

    // X10 mouse encoding
    public static final int MOUSE_X10_OFFSET = 32; // Offset for X10 encoding (button, x, y)
}
