package org.codejive.twinkle.text;

import org.codejive.twinkle.ansi.Constants;
import org.codejive.twinkle.ansi.util.AnsiDecoder;

/**
 * A decoder for character sequences including Unicode codepoints, grapheme clusters, and ANSI
 * escape sequences. Extends {@link AnsiDecoder} to add support for proper Unicode handling
 * including surrogate pairs, grapheme cluster boundaries, and extended grapheme cluster rules.
 *
 * <p>Sequences can be built up by pushing characters into the decoder while its state is
 * INCOMPLETE. Once the decoder has enough information to determine the type of sequence (e.g. a
 * codepoint, a grapheme cluster, or an ANSI escape sequence), the state will change to the
 * corresponding type. The decoder can then be queried for the result. <code>reset()</code> can be
 * used to clear the decoder and start building a new sequence.
 */
public class UnicodeDecoder extends AnsiDecoder {
    // Additional state constants for Unicode handling
    public static final int CODEPOINT = 10;
    public static final int GRAPHEME_CLUSTER = 11;

    private static final int NEWLINE = '\n';
    private static final int CARRIAGE_RETURN = '\r';

    private char pendingHighSurrogate = 0;
    private int riCount = 0;
    private boolean pendingCarriageReturn = false;
    private int firstCodepoint = -1;
    private int lastCodepoint = -1;
    private int codepointCount = 0;

    /**
     * Pushes either a Unicode code point or a UTF-16 code unit encoded as an int.
     *
     * <p>Extends the base implementation to handle UTF-16 surrogate pairs and Unicode codepoint
     * sequences properly.
     */
    @Override
    public void push(int cp) {
        // Handle UTF-16 surrogate pairs
        if (pendingHighSurrogate != 0) {
            if (!canPush(cp)) {
                state = ERROR;
                return;
            }
            char low = (char) cp;
            buffer.append(low);
            int codepoint = Character.toCodePoint(pendingHighSurrogate, low);
            pendingHighSurrogate = 0;
            handleNonAnsi(codepoint);
            return;
        }

        if (cp >= Character.MIN_HIGH_SURROGATE && cp <= Character.MAX_HIGH_SURROGATE) {
            if (!canPush(cp)) {
                state = ERROR;
                return;
            }
            buffer.append((char) cp);
            pendingHighSurrogate = (char) cp;
            state = INCOMPLETE;
            return;
        }

        // Delegate to base class for ANSI handling and standard processing
        super.push(cp);
    }

    /**
     * Returns true if {@code cp} can be consumed as part of the currently decoded sequence.
     *
     * <p>Extends the base implementation to handle UTF-16 surrogate pairs and grapheme cluster
     * boundaries.
     */
    @Override
    public boolean canPush(int cp) {
        if (state == ERROR || state == ANSI) {
            return false;
        }

        // Handle UTF-16 surrogate pairs
        if (pendingHighSurrogate != 0) {
            if (cp < Character.MIN_LOW_SURROGATE || cp > Character.MAX_LOW_SURROGATE) {
                return false;
            }
            int codepoint = Character.toCodePoint(pendingHighSurrogate, (char) cp);
            return canPushNonAnsi(codepoint);
        }

        if (cp >= Character.MIN_HIGH_SURROGATE && cp <= Character.MAX_HIGH_SURROGATE) {
            if (ansiMode != AnsiMode.NONE || buffer.length() == 0) {
                return true;
            }
            return canPushNonAnsi(0x10000);
        }

        if (cp >= Character.MIN_LOW_SURROGATE && cp <= Character.MAX_LOW_SURROGATE) {
            return false;
        }

        // Delegate to base class
        return super.canPush(cp);
    }

    /**
     * Finalizes pending state when no more input is available.
     *
     * <p>Extends the base implementation to resolve incomplete CR line endings and validate
     * surrogate pairs.
     */
    @Override
    public void finish() {
        if (state == ERROR) {
            return;
        }
        if (pendingHighSurrogate != 0) {
            state = ERROR;
            return;
        }
        if (pendingCarriageReturn) {
            pendingCarriageReturn = false;
            state = CODEPOINT;
            return;
        }
        // Delegate to base class for ANSI finalization
        super.finish();
    }

    /** Resets the decoder to its initial state, clearing all accumulated data. */
    @Override
    public void reset() {
        super.reset();
        pendingHighSurrogate = 0;
        pendingCarriageReturn = false;
    }

    /**
     * Returns true if the decoder has completed a sequence and can be emitted as-is.
     *
     * <p>Unlike {@link #isComplete()}, this reports false for tails that are syntactically
     * extendable and usually require continuation (for example trailing ZWJ/virama/prepend).
     */
    public boolean isReady() {
        if (state == ERROR || state == INCOMPLETE) {
            return false;
        }
        if (state == ANSI) {
            return true;
        }
        if (pendingHighSurrogate != 0 || pendingCarriageReturn || codepointCount == 0) {
            return false;
        }

        return !(lastCodepoint == Unicode.ZWJ
                || Unicode.isVirama(lastCodepoint)
                || Unicode.isPrepend(lastCodepoint));
    }

    /**
     * Returns the lead code point for the decoded sequence.
     *
     * <p>For newline sequences this returns {@code '\n'} even when the buffered form is {@code
     * "\r"} or {@code "\r\n"}. Returns -1 while incomplete or in error.
     */
    public int codepoint() {
        if (!isComplete() || state == ERROR) {
            return -1;
        }
        if (state == ANSI) {
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
        if (!isComplete() || state == ERROR) {
            return -1;
        }
        if (state == ANSI) {
            return 0;
        }
        return codepointCount == 0 ? -1 : calculateWidth(firstCodepoint);
    }

    /**
     * Implements the hook for handling non-ANSI characters. This method handles Unicode codepoints
     * including newlines and grapheme cluster formation.
     */
    @Override
    protected void handleNonAnsi(int cp) {
        if (pendingCarriageReturn) {
            pendingCarriageReturn = false;
            state = CODEPOINT;
            return;
        }

        if (cp == CARRIAGE_RETURN) {
            firstCodepoint = NEWLINE;
            lastCodepoint = NEWLINE;
            codepointCount = 1;
            state = INCOMPLETE;
            pendingCarriageReturn = true;
            return;
        }

        if (cp == NEWLINE) {
            firstCodepoint = NEWLINE;
            lastCodepoint = NEWLINE;
            codepointCount = 1;
            state = CODEPOINT;
            return;
        }

        pushCodepoint(cp);
    }

    /** Implements the hook for checking if a non-ANSI character can be pushed. */
    @Override
    protected boolean canPushNonAnsi(int cp) {
        if (pendingCarriageReturn) {
            return cp == NEWLINE;
        }
        return canPushCodepoint(cp);
    }

    /** Implements the hook for resetting Unicode-specific state. */
    @Override
    protected void resetNonAnsi() {
        pendingHighSurrogate = 0;
        pendingCarriageReturn = false;
        riCount = 0;
        firstCodepoint = -1;
        lastCodepoint = -1;
        codepointCount = 0;
    }

    private void pushCodepoint(int cp) {
        if (state == ANSI) {
            state = ERROR;
            return;
        }

        if (codepointCount == 0) {
            firstCodepoint = cp;
            lastCodepoint = cp;
            codepointCount = 1;
            riCount = Unicode.isRegionalIndicator(cp) ? 1 : 0;
            state = CODEPOINT;
            return;
        }

        if (shouldBreak(lastCodepoint, cp, riCount)) {
            state = ERROR;
            return;
        }

        lastCodepoint = cp;
        codepointCount++;
        riCount = Unicode.isRegionalIndicator(cp) ? riCount + 1 : 0;
        state = GRAPHEME_CLUSTER;
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
