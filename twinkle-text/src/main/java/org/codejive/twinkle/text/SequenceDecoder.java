package org.codejive.twinkle.text;

import org.codejive.twinkle.ansi.Constants;

/**
 * A utility class for decoding sequences of characters. Sequences can be built up by pushing
 * characters into the decoder while it's state is INCOMPLETE. Once the decoder has enough
 * information to determine the type of sequence (e.g. a codepoint, a grapheme cluster, or an ANSI
 * escape sequence), the state will change to the corresponding type. The decoder can then be
 * queried for the result. <code>reset()</code> can be used to clear the decoder and start building
 * a new sequence.
 */
public class SequenceDecoder {
    public enum State {
        INCOMPLETE,
        CODEPOINT,
        GRAPHEME_CLUSTER,
        ANSI_ESCAPE_SEQUENCE,
        ERROR
    }

    private enum AnsiMode {
        NONE,
        PREFIX,
        CSI,
        OSC
    }

    private static final int NEWLINE = '\n';
    private static final int CARRIAGE_RETURN = '\r';

    private final StringBuilder buffer = new StringBuilder();

    private State state = State.INCOMPLETE;
    private AnsiMode ansiMode = AnsiMode.NONE;
    private char pendingHighSurrogate = 0;
    private boolean oscSeenEsc = false;
    private int riCount = 0;
    private boolean pendingCarriageReturn = false;
    private int firstCodepoint = -1;
    private int lastCodepoint = -1;
    private int codepointCount = 0;

    /**
     * Pushes either a Unicode code point or a UTF-16 code unit encoded as an int.
     *
     * <p>Values in the surrogate range are treated as UTF-16 code units and paired using internal
     * pending-surrogate state.
     */
    public void push(int cp) {
        if (pendingHighSurrogate != 0) {
            if (!canPush(cp)) {
                state = State.ERROR;
                return;
            }
            char low = (char) cp;
            buffer.append(low);
            int codepoint = Character.toCodePoint(pendingHighSurrogate, low);
            pendingHighSurrogate = 0;
            pushCodepoint(codepoint);
            return;
        }

        if (cp >= Character.MIN_HIGH_SURROGATE && cp <= Character.MAX_HIGH_SURROGATE) {
            if (!canPush(cp)) {
                state = State.ERROR;
                return;
            }
            buffer.append((char) cp);
            pendingHighSurrogate = (char) cp;
            state = State.INCOMPLETE;
            return;
        }

        if (!canPush(cp)) {
            state = State.ERROR;
            return;
        }

        if (Character.isSupplementaryCodePoint(cp)) {
            buffer.append(Character.toChars(cp));
        } else {
            buffer.append((char) cp);
        }

        if (pendingCarriageReturn) {
            pendingCarriageReturn = false;
            state = State.CODEPOINT;
            return;
        }

        if (ansiMode != AnsiMode.NONE) {
            char[] chars = Character.toChars(cp);
            for (int i = 0; i < chars.length; i++) {
                pushAnsi(chars[i]);
                if (state == State.ERROR || state == State.ANSI_ESCAPE_SEQUENCE) {
                    break;
                }
            }
            return;
        }

        if (cp == Constants.ESC) {
            pushAnsi((char) cp);
            return;
        }

        if (cp == CARRIAGE_RETURN) {
            firstCodepoint = NEWLINE;
            lastCodepoint = NEWLINE;
            codepointCount = 1;
            state = State.INCOMPLETE;
            pendingCarriageReturn = true;
            return;
        }

        if (cp == NEWLINE) {
            firstCodepoint = NEWLINE;
            lastCodepoint = NEWLINE;
            codepointCount = 1;
            state = State.CODEPOINT;
            return;
        }

        pushCodepoint(cp);
    }

    /**
     * Returns true if {@code cp} can be consumed as part of the currently decoded sequence.
     *
     * <p>This is a non-mutating probe. Callers can use it to detect sequence boundaries without
     * relying on completion heuristics.
     *
     * <p>Like {@link #push(int)}, this accepts either Unicode code points or UTF-16 code units
     * encoded as ints.
     */
    public boolean canPush(int cp) {
        if (state == State.ERROR || state == State.ANSI_ESCAPE_SEQUENCE) {
            return false;
        }

        if (pendingHighSurrogate != 0) {
            if (cp < Character.MIN_LOW_SURROGATE || cp > Character.MAX_LOW_SURROGATE) {
                return false;
            }
            int codepoint = Character.toCodePoint(pendingHighSurrogate, (char) cp);
            return canPushCodepoint(codepoint);
        }

        if (cp >= Character.MIN_HIGH_SURROGATE && cp <= Character.MAX_HIGH_SURROGATE) {
            if (ansiMode != AnsiMode.NONE || buffer.length() == 0) {
                return true;
            }
            return canPushCodepoint(0x10000);
        }

        if (cp >= Character.MIN_LOW_SURROGATE && cp <= Character.MAX_LOW_SURROGATE) {
            return false;
        }

        if (!Character.isValidCodePoint(cp)) {
            return false;
        }

        if (pendingCarriageReturn) {
            return cp == NEWLINE;
        }

        if (ansiMode != AnsiMode.NONE) {
            return true;
        }

        if (buffer.length() == 0) {
            return true;
        }

        if (cp == Constants.ESC) {
            return false;
        }

        return canPushCodepoint(cp);
    }

    /**
     * Finalizes pending state when no more input is available.
     *
     * <p>This resolves incomplete CR line endings as newline sequences and resolves unterminated
     * ANSI escapes as ANSI sequences, matching iterator semantics at end of input.
     */
    public void finish() {
        if (state == State.ERROR) {
            return;
        }
        if (pendingHighSurrogate != 0) {
            state = State.ERROR;
            return;
        }
        if (pendingCarriageReturn) {
            pendingCarriageReturn = false;
            state = State.CODEPOINT;
            return;
        }
        if (ansiMode != AnsiMode.NONE) {
            ansiMode = AnsiMode.NONE;
            state = State.ANSI_ESCAPE_SEQUENCE;
        }
    }

    public void reset() {
        buffer.setLength(0);
        state = State.INCOMPLETE;
        ansiMode = AnsiMode.NONE;
        pendingHighSurrogate = 0;
        pendingCarriageReturn = false;
        oscSeenEsc = false;
        riCount = 0;
        firstCodepoint = -1;
        lastCodepoint = -1;
        codepointCount = 0;
    }

    public boolean isComplete() {
        return state() != State.INCOMPLETE;
    }

    /**
     * Returns true when the current sequence can be emitted as-is.
     *
     * <p>Unlike {@link #isComplete()}, this reports false for tails that are syntactically
     * extendable and usually require continuation (for example trailing ZWJ/virama/prepend).
     */
    public boolean isReady() {
        if (state == State.ERROR || state == State.INCOMPLETE) {
            return false;
        }
        if (state == State.ANSI_ESCAPE_SEQUENCE) {
            return true;
        }
        if (pendingHighSurrogate != 0 || pendingCarriageReturn || codepointCount == 0) {
            return false;
        }

        return !(lastCodepoint == Unicode.ZWJ
                || Unicode.isVirama(lastCodepoint)
                || Unicode.isPrepend(lastCodepoint));
    }

    public State state() {
        return state;
    }

    /**
     * Returns the lead code point for the decoded sequence.
     *
     * <p>For newline sequences this returns {@code '\n'} even when the buffered form is {@code
     * "\r"} or {@code "\r\n"}. Returns -1 while incomplete or in error.
     */
    public int codepoint() {
        if (!isComplete() || state == State.ERROR) {
            return -1;
        }
        if (state == State.ANSI_ESCAPE_SEQUENCE) {
            return Constants.ESC;
        }
        return codepointCount == 0 ? -1 : firstCodepoint;
    }

    /**
     * Returns the display width of the sequence. This is not necessarily the same as the length of
     * the sequence, as some characters may have a display width of 1 while others a width of 2
     * (e.g. CJK characters). And ANSI escape sequences always have a display width of 0. Will
     * return -1 if the decoder isn't ready yet (e.g. if the sequence is incomplete).
     *
     * @return the visual column width, or -1 while incomplete or in error state
     */
    public int width() {
        if (!isComplete() || state == State.ERROR) {
            return -1;
        }
        if (state == State.ANSI_ESCAPE_SEQUENCE) {
            return 0;
        }
        return codepointCount == 0 ? -1 : calculateWidth(firstCodepoint);
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    private void pushAnsi(char ch) {
        if (state == State.ANSI_ESCAPE_SEQUENCE) {
            state = State.ERROR;
            return;
        }

        if (ansiMode == AnsiMode.NONE) {
            if (ch == Constants.ESC) {
                ansiMode = AnsiMode.PREFIX;
                state = State.INCOMPLETE;
                return;
            }
            state = State.ERROR;
            return;
        }

        if (ansiMode == AnsiMode.PREFIX) {
            if (ch == '[') {
                ansiMode = AnsiMode.CSI;
                state = State.INCOMPLETE;
            } else if (ch == ']') {
                ansiMode = AnsiMode.OSC;
                state = State.INCOMPLETE;
                oscSeenEsc = false;
            } else {
                state = State.ANSI_ESCAPE_SEQUENCE;
                ansiMode = AnsiMode.NONE;
            }
            return;
        }

        if (ansiMode == AnsiMode.CSI) {
            if (ch >= 0x40 && ch <= 0x7E) {
                state = State.ANSI_ESCAPE_SEQUENCE;
                ansiMode = AnsiMode.NONE;
            } else {
                state = State.INCOMPLETE;
            }
            return;
        }

        if (ansiMode == AnsiMode.OSC) {
            if (oscSeenEsc) {
                if (ch == '\\') {
                    state = State.ANSI_ESCAPE_SEQUENCE;
                    ansiMode = AnsiMode.NONE;
                    oscSeenEsc = false;
                    return;
                }
                oscSeenEsc = (ch == Constants.ESC);
                state = State.INCOMPLETE;
                return;
            }
            if (ch == 0x07) {
                state = State.ANSI_ESCAPE_SEQUENCE;
                ansiMode = AnsiMode.NONE;
                return;
            }
            oscSeenEsc = (ch == Constants.ESC);
            state = State.INCOMPLETE;
        }
    }

    private void pushCodepoint(int cp) {
        if (state == State.ANSI_ESCAPE_SEQUENCE) {
            state = State.ERROR;
            return;
        }

        if (codepointCount == 0) {
            firstCodepoint = cp;
            lastCodepoint = cp;
            codepointCount = 1;
            riCount = Unicode.isRegionalIndicator(cp) ? 1 : 0;
            state = State.CODEPOINT;
            return;
        }

        if (shouldBreak(lastCodepoint, cp, riCount)) {
            state = State.ERROR;
            return;
        }

        lastCodepoint = cp;
        codepointCount++;
        riCount = Unicode.isRegionalIndicator(cp) ? riCount + 1 : 0;
        state = State.GRAPHEME_CLUSTER;
    }

    private boolean canPushCodepoint(int cp) {
        if (codepointCount == 0) {
            return true;
        }
        if (cp == CARRIAGE_RETURN || cp == NEWLINE || cp == Constants.ESC) {
            return false;
        }
        return !shouldBreak(lastCodepoint, cp, riCount);
    }

    private static boolean shouldBreak(int prev, int curr, int riCount) {
        if (Unicode.isL(prev)
                && (Unicode.isL(curr)
                        || Unicode.isV(curr)
                        || Unicode.isLV(curr)
                        || Unicode.isLVT(curr))) return false;
        if ((Unicode.isLV(prev) || Unicode.isV(prev)) && (Unicode.isV(curr) || Unicode.isT(curr)))
            return false;
        if ((Unicode.isLVT(prev) || Unicode.isT(prev)) && Unicode.isT(curr)) return false;
        // Variation selectors only attach to specific base characters
        // This must be checked before the general NON_SPACING_MARK check since
        // variation selectors have type NON_SPACING_MARK but need special handling
        if (Unicode.isVariationSelector(curr)) {
            return !Unicode.canHaveVariationSelector(prev);
        }
        int type = Character.getType(curr);
        if (type == Character.NON_SPACING_MARK
                || type == Character.COMBINING_SPACING_MARK
                || curr == Unicode.ZWJ
                || prev == Unicode.ZWJ) return false;
        if (Unicode.isRegionalIndicator(prev) && Unicode.isRegionalIndicator(curr)) {
            return (riCount % 2 == 0);
        }
        return !(Unicode.isPrepend(prev) || Unicode.isVirama(prev));
    }

    private static int calculateWidth(int cp) {
        int type = Character.getType(cp);
        if (type == Character.CONTROL || type == Character.FORMAT || type == Character.UNASSIGNED) {
            return 0;
        }
        return Unicode.isWide(cp) ? 2 : 1;
    }
}
