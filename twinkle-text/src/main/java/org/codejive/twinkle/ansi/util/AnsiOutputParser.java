package org.codejive.twinkle.ansi.util;

import static org.codejive.twinkle.ansi.Constants.*;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Hyperlink;
import org.codejive.twinkle.ansi.Style;

public class AnsiOutputParser {
    protected final AnsiSequenceHandler handler;

    public interface AnsiSequenceHandler {
        // CSI
        default boolean onStyle(String sequence) {
            return false;
        }

        default boolean onHome() {
            return false;
        }

        default boolean onCursorPosition(int x, int y) {
            return false;
        }

        default boolean onCursorUp(int num) {
            return false;
        }

        default boolean onCursorDown(int num) {
            return false;
        }

        default boolean onCursorForward(int num) {
            return false;
        }

        default boolean onCursorBackward(int num) {
            return false;
        }

        default boolean onCursorNextLine(int num) {
            return false;
        }

        default boolean onCursorPrevLine(int num) {
            return false;
        }

        default boolean onCursorColumn(int col) {
            return false;
        }

        default boolean onScreenEraseFull() {
            return false;
        }

        default boolean onScreenEraseStart() {
            return false;
        }

        default boolean onScreenEraseEnd() {
            return false;
        }

        default boolean onLineEraseFull() {
            return false;
        }

        default boolean onLineEraseStart() {
            return false;
        }

        default boolean onLineEraseEnd() {
            return false;
        }

        default boolean onAutoWrap(boolean enabled) {
            return false;
        }

        default boolean onScreenSave() {
            return false;
        }

        default boolean onScreenSaveAlt() {
            return false;
        }

        default boolean onScreenRestore() {
            return false;
        }

        default boolean onScreenRestoreAlt() {
            return false;
        }

        // OSC
        default boolean onHyperlink(Hyperlink link) {
            return false;
        }

        // Other
        default boolean onCursorSave() {
            return false;
        }

        default boolean onCursorRestore() {
            return false;
        }
    }

    public static boolean parse(String sequence, AnsiSequenceHandler handler) {
        AnsiOutputParser parser = new AnsiOutputParser(handler);
        return parser.parse(sequence);
    }

    public AnsiOutputParser(AnsiSequenceHandler handler) {
        this.handler = handler;
    }

    public boolean parse(String sequence) {
        if (Style.isStyleSequence(sequence)) {
            return handler.onStyle(sequence);
        } else if (sequence.startsWith(CSI)) {
            // Handle CSI sequences here
            return handleCsiSequence(sequence);
        } else if (sequence.startsWith(OSC)) {
            // Handle OSC sequences here
            return handleOscSequence(sequence);
        } else {
            // Handle any other sequences here
            return handleOtherSequence(sequence);
        }
    }

    protected boolean handleCsiSequence(String sequence) {
        int num;
        int[] nums;

        if (Ansi.cursorHome().equals(sequence)) {
            return handler.onHome();
        } else if ((nums = numsMatch(CURSOR_POSITION_CMD, sequence, null)) != null) {
            return handler.onCursorPosition(nums[1], nums[0]);
        } else if ((num = numMatch(CURSOR_UP_CMD, sequence, 1)) != -1) {
            return handler.onCursorUp(num);
        } else if ((num = numMatch(CURSOR_DOWN_CMD, sequence, 1)) != -1) {
            return handler.onCursorDown(num);
        } else if ((num = numMatch(CURSOR_FORWARD_CMD, sequence, 1)) != -1) {
            return handler.onCursorForward(num);
        } else if ((num = numMatch(CURSOR_BACKWARD_CMD, sequence, 1)) != -1) {
            return handler.onCursorBackward(num);
        } else if ((num = numMatch(CURSOR_NEXT_LINE_CMD, sequence, 1)) != -1) {
            return handler.onCursorNextLine(num);
        } else if ((num = numMatch(CURSOR_PREV_LINE_CMD, sequence, 1)) != -1) {
            return handler.onCursorPrevLine(num);
        } else if ((num = numMatch(CURSOR_COLUMN_CMD, sequence, 1)) != -1) {
            return handler.onCursorColumn(num);
        } else if ((CSI + SCREEN_ERASE_FULL).equals(sequence)) {
            return handler.onScreenEraseFull();
        } else if ((CSI + SCREEN_ERASE_START).equals(sequence)) {
            return handler.onScreenEraseStart();
        } else if ((CSI + SCREEN_ERASE_CMD).equals(sequence)
                || (CSI + SCREEN_ERASE_END).equals(sequence)) {
            return handler.onScreenEraseEnd();
        } else if ((CSI + LINE_ERASE_FULL).equals(sequence)) {
            return handler.onLineEraseFull();
        } else if ((CSI + LINE_ERASE_START).equals(sequence)) {
            return handler.onLineEraseStart();
        } else if ((CSI + LINE_ERASE_CMD).equals(sequence)
                || (CSI + LINE_ERASE_END).equals(sequence)) {
            return handler.onLineEraseEnd();
        } else if (Ansi.autoWrap().equals(sequence)) {
            return handler.onAutoWrap(true);
        } else if (Ansi.autoWrapOff().equals(sequence)) {
            return handler.onAutoWrap(false);
        } else if ((CSI + SCREEN_SAVE).equals(sequence)) {
            return handler.onScreenSave();
        } else if ((CSI + SCREEN_SAVE_ALT).equals(sequence)) {
            return handler.onScreenSaveAlt();
        } else if ((CSI + SCREEN_RESTORE).equals(sequence)) {
            return handler.onScreenRestore();
        } else if ((CSI + SCREEN_RESTORE_ALT).equals(sequence)) {
            return handler.onScreenRestoreAlt();
        }
        return false;
    }

    protected boolean handleOscSequence(String sequence) {
        // Handle hyperlink sequences here
        Hyperlink link = Hyperlink.parse(sequence);
        if (link != null) {
            return handler.onHyperlink(link);
        }
        return false;
    }

    protected boolean handleOtherSequence(String sequence) {
        if (Ansi.cursorSave().equals(sequence)) {
            return handler.onCursorSave();
        } else if (Ansi.cursorRestore().equals(sequence)) {
            return handler.onCursorRestore();
        }
        return false;
    }

    private int numMatch(char cursorCmd, String sequence, int defaultNum) {
        if (sequence.startsWith(CSI) && sequence.endsWith(String.valueOf(cursorCmd))) {
            String numStr = sequence.substring(CSI.length(), sequence.length() - 1);
            if (numStr.isEmpty()) {
                return defaultNum;
            }
            try {
                return Integer.parseInt(numStr);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private int[] numsMatch(char cursorCmd, String sequence, int[] defaultNums) {
        if (sequence.startsWith(CSI) && sequence.endsWith(String.valueOf(cursorCmd))) {
            String numsStr = sequence.substring(CSI.length(), sequence.length() - 1);
            if (numsStr.isEmpty()) {
                return defaultNums;
            }
            String[] parts = numsStr.split(";");
            int nums[] = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                try {
                    nums[i] = Integer.parseInt(parts[i]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return nums;
        }
        return null;
    }
}
