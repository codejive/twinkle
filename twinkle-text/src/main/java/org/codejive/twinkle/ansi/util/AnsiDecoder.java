package org.codejive.twinkle.ansi.util;

import org.codejive.twinkle.ansi.Constants;

/**
 * A base decoder for handling ANSI escape sequences. This class provides the foundation for
 * decoding character sequences, with a focus on identifying and parsing ANSI escape sequences.
 * Subclasses can extend this to add additional sequence handling.
 *
 * <p>Characters are pushed into the decoder while its state is INCOMPLETE. Once enough information
 * is available to determine the sequence type, the state changes accordingly. Use {@code reset()}
 * to clear the decoder and start a new sequence.
 */
public class AnsiDecoder {
    // State constants
    public static final int INCOMPLETE = 0;
    public static final int ANSI = 1;
    public static final int ERROR = 2;

    protected enum AnsiMode {
        NONE,
        PREFIX,
        CSI,
        OSC
    }

    protected final StringBuilder buffer = new StringBuilder();
    protected int state = INCOMPLETE;
    protected AnsiMode ansiMode = AnsiMode.NONE;
    protected boolean oscSeenEsc = false;

    /**
     * Pushes a character value (as an int) into the decoder.
     *
     * <p>Accepts int values to support full Unicode range including supplementary characters. This
     * base implementation handles ANSI escape sequences. Subclasses should override {@link
     * #handleNonAnsi(int)} to provide additional handling for non-ANSI characters.
     *
     * @param c the character value to push
     */
    public void push(int c) {
        if (!canPush(c)) {
            state = ERROR;
            return;
        }

        if (Character.isSupplementaryCodePoint(c)) {
            buffer.append(Character.toChars(c));
        } else {
            buffer.append((char) c);
        }

        if (ansiMode != AnsiMode.NONE) {
            char[] chars = Character.toChars(c);
            for (int i = 0; i < chars.length; i++) {
                pushAnsi(chars[i]);
                if (state == ERROR || state == ANSI) {
                    break;
                }
            }
            return;
        }

        if (c == Constants.ESC) {
            pushAnsi((char) c);
            return;
        }

        handleNonAnsi(c);
    }

    /**
     * Returns true if the given character value can be consumed as part of the currently decoded
     * sequence.
     *
     * <p>This is a non-mutating probe. Callers can use it to detect sequence boundaries without
     * relying on completion heuristics.
     *
     * @param c the character value to check
     * @return true if the character can be pushed
     */
    public boolean canPush(int c) {
        if (state == ERROR || state == ANSI) {
            return false;
        }

        if (!Character.isValidCodePoint(c)) {
            return false;
        }

        if (ansiMode != AnsiMode.NONE) {
            return true;
        }

        if (buffer.length() == 0) {
            return true;
        }

        if (c == Constants.ESC) {
            return false;
        }

        return canPushNonAnsi(c);
    }

    /**
     * Finalizes pending state when no more input is available.
     *
     * <p>This base implementation resolves unterminated ANSI escapes as ANSI sequences. Subclasses
     * should override {@link #finishNonAnsi()} to handle additional finalization logic.
     */
    public void finish() {
        if (state == ERROR) {
            return;
        }
        if (ansiMode != AnsiMode.NONE) {
            ansiMode = AnsiMode.NONE;
            state = ANSI;
            return;
        }
        finishNonAnsi();
    }

    /** Resets the decoder to its initial state, clearing all accumulated data. */
    public void reset() {
        buffer.setLength(0);
        state = INCOMPLETE;
        ansiMode = AnsiMode.NONE;
        oscSeenEsc = false;
        resetNonAnsi();
    }

    /** Returns true if the decoder has completed a sequence. */
    public boolean isComplete() {
        return state() != INCOMPLETE;
    }

    /**
     * Returns the current state of the decoder.
     *
     * @return the current state as an int constant
     */
    public int state() {
        return state;
    }

    /** Returns the buffered sequence as a string. */
    @Override
    public String toString() {
        return buffer.toString();
    }

    /** Handles ANSI escape sequence parsing logic. */
    protected void pushAnsi(char ch) {
        if (state == ANSI) {
            state = ERROR;
            return;
        }

        if (ansiMode == AnsiMode.NONE) {
            if (ch == Constants.ESC) {
                ansiMode = AnsiMode.PREFIX;
                state = INCOMPLETE;
                return;
            }
            state = ERROR;
            return;
        }

        if (ansiMode == AnsiMode.PREFIX) {
            if (ch == '[') {
                ansiMode = AnsiMode.CSI;
                state = INCOMPLETE;
            } else if (ch == ']') {
                ansiMode = AnsiMode.OSC;
                state = INCOMPLETE;
                oscSeenEsc = false;
            } else {
                state = ANSI;
                ansiMode = AnsiMode.NONE;
            }
            return;
        }

        if (ansiMode == AnsiMode.CSI) {
            if (ch >= 0x40 && ch <= 0x7E) {
                state = ANSI;
                ansiMode = AnsiMode.NONE;
            } else {
                state = INCOMPLETE;
            }
            return;
        }

        if (ansiMode == AnsiMode.OSC) {
            if (oscSeenEsc) {
                if (ch == '\\') {
                    state = ANSI;
                    ansiMode = AnsiMode.NONE;
                    oscSeenEsc = false;
                    return;
                }
                oscSeenEsc = (ch == Constants.ESC);
                state = INCOMPLETE;
                return;
            }
            if (ch == 0x07) {
                state = ANSI;
                ansiMode = AnsiMode.NONE;
                return;
            }
            oscSeenEsc = (ch == Constants.ESC);
            state = INCOMPLETE;
        }
    }

    /**
     * Hook for subclasses to handle non-ANSI characters. Base implementation sets state to ERROR.
     *
     * @param c the character value to handle
     */
    protected void handleNonAnsi(int c) {
        state = ERROR;
    }

    /**
     * Hook for subclasses to check if a non-ANSI character can be pushed. Base implementation
     * returns false.
     *
     * @param c the character value to check
     * @return true if the character can be pushed
     */
    protected boolean canPushNonAnsi(int c) {
        return false;
    }

    /** Hook for subclasses to perform finalization of non-ANSI sequences. */
    protected void finishNonAnsi() {
        // Base implementation does nothing
    }

    /** Hook for subclasses to reset non-ANSI state. */
    protected void resetNonAnsi() {
        // Base implementation does nothing
    }
}
