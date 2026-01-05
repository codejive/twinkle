package org.codejive.twinkle.util;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.NoSuchElementException;
import org.codejive.twinkle.ansi.Ansi;

/**
 * An iterator that reads from a text source and yields Unicode grapheme clusters, ANSI escape
 * sequences and simple codepoints while handling line endings as single units (even when in reality
 * they might be 2 characters).
 */
public interface SequenceIterator {
    /** Returns true if there is still input to read. */
    boolean hasNext();

    /**
     * Returns the next lead codepoint of the next sequence. Call {@link #sequence()} to get the
     * full sequence. In case of ANSI escape sequences, this returns ESC (0x1B). In case of line
     * endings, this returns NEWLINE (0x0A).
     *
     * @throws NoSuchElementException if there is no more input.
     */
    int next();

    /**
     * Returns true if the last returned sequence from {@link #next()} is complex, i.e., consists of
     * multiple codepoints or is an escape sequence.
     */
    boolean isComplex();

    /**
     * Returns the visual width of the current sequence in columns. Escape sequences return 0, most
     * characters return 1, and Full-width/Wide characters return 2.
     */
    int width();

    /** Returns the full sequence of the last returned codepoint from {@link #next()}. */
    String sequence();

    /** Returns the start index of the current sequence in characters. */
    int begin();

    /** Returns the end index of the current sequence in characters. */
    int end();

    static SequenceIterator of(CharSequence text) {
        return new CharSequenceSequenceIterator(text);
    }

    static SequenceIterator of(Reader input) {
        return new ReaderSequenceIterator(input);
    }
}

abstract class BaseSequenceIterator implements SequenceIterator {
    protected int currentWidth = 0;

    protected boolean shouldBreak(int prev, int curr, int riCount) {
        if (isL(prev) && (isL(curr) || isV(curr) || isLV(curr) || isLVT(curr))) return false;
        if ((isLV(prev) || isV(prev)) && (isV(curr) || isT(curr))) return false;
        if ((isLVT(prev) || isT(prev)) && isT(curr)) return false;
        int type = Character.getType(curr);
        if (type == Character.NON_SPACING_MARK
                || type == Character.COMBINING_SPACING_MARK
                || curr == 0x200D
                || prev == 0x200D) return false;
        if (isRegionalIndicator(prev) && isRegionalIndicator(curr)) return (riCount % 2 == 0);
        return !(isPrepend(prev) || isVirama(prev));
    }

    protected static boolean isRegionalIndicator(int cp) {
        return cp >= 0x1F1E6 && cp <= 0x1F1FF;
    }

    protected static boolean isL(int cp) {
        return (cp >= 0x1100 && cp <= 0x115F);
    }

    protected static boolean isV(int cp) {
        return (cp >= 0x1160 && cp <= 0x11A7);
    }

    protected static boolean isT(int cp) {
        return (cp >= 0x11A8 && cp <= 0x11FF);
    }

    protected static boolean isLV(int cp) {
        return (cp >= 0xAC00 && cp <= 0xD7A3 && (cp - 0xAC00) % 28 == 0);
    }

    protected static boolean isLVT(int cp) {
        return (cp >= 0xAC00 && cp <= 0xD7A3 && (cp - 0xAC00) % 28 != 0);
    }

    protected static boolean isVirama(int cp) {
        return (cp >= 0x094D && cp <= 0x0D4D && (cp & 0xFF) == 0x4D) || cp == 0x0D4D;
    }

    protected static boolean isPrepend(int cp) {
        return cp == 0x0600
                || cp == 0x0601
                || cp == 0x0602
                || cp == 0x0603
                || cp == 0x0604
                || cp == 0x0605
                || cp == 0x06DD
                || cp == 0x070F;
    }

    @Override
    public int width() {
        return currentWidth;
    }

    protected int calculateWidth(int cp) {
        int type = Character.getType(cp);
        if (type == Character.CONTROL // Cc: Control characters (like \n, \t)
                || type == Character.FORMAT // Cf: Format (like Zero Width Joiner)
                || type == Character.UNASSIGNED) { // Cn: Unassigned (reserved)
            return 0;
        }

        if (isWide(cp)) {
            return 2;
        }

        return 1;
    }

    private boolean isWide(int cp) {
        // East Asian Wide (W) and Fullwidth (F)
        if ((cp >= 0x1100 && cp <= 0x115F)
                || // Hangul Jamo
                (cp >= 0x2E80 && cp <= 0xA4CF && cp != 0x303F)
                || // CJK Radicals, Symbols, Han
                (cp >= 0xAC00 && cp <= 0xD7A3)
                || // Hangul Syllables
                (cp >= 0xF900 && cp <= 0xFAFF)
                || // CJK Compatibility Ideographs
                (cp >= 0xFE10 && cp <= 0xFE19)
                || // Vertical forms
                (cp >= 0xFE30 && cp <= 0xFE6F)
                || // CJK Compatibility Forms
                (cp >= 0xFF00 && cp <= 0xFF60)
                || // Fullwidth Forms
                (cp >= 0xFFE0 && cp <= 0xFFE6)) {
            return true;
        }

        // Plane 2 and 3 (SIP/TIP) are almost entirely CJK Ideographs (Wide)
        if (cp >= 0x20000 && cp <= 0x3FFFD) {
            return true;
        }

        // Common Emoji Presentation ranges (Simplified)
        // Includes Miscellaneous Symbols and Pictographs, Emoticons, Transport, etc.
        if ((cp >= 0x1F300 && cp <= 0x1F64F)
                || (cp >= 0x1F680 && cp <= 0x1F6FF)
                || (cp >= 0x1F900 && cp <= 0x1F9FF)
                || (cp >= 0x1F200 && cp <= 0x1F2FF)) {
            return true;
        }

        return false;
    }
}

/**
 * A high-performance implementation of SequenceIterator that operates directly on a CharSequence
 * without using Streams or Readers.
 */
class CharSequenceSequenceIterator extends BaseSequenceIterator {
    private final CharSequence text;
    private final int length;

    private int cursor = 0;
    private int sequenceStart = 0;
    private int sequenceEnd = 0;
    private int nextLeadCodePoint = -1;
    private boolean primed = false;

    public CharSequenceSequenceIterator(CharSequence text) {
        this.text = text;
        length = text.length();
    }

    @Override
    public boolean hasNext() {
        if (!primed) {
            primeNext();
        }
        return cursor < length || primed && nextLeadCodePoint != -1;
    }

    @Override
    public int next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        primed = false;
        return nextLeadCodePoint;
    }

    @Override
    public boolean isComplex() {
        return (sequenceEnd - sequenceStart) > Character.charCount(nextLeadCodePoint);
    }

    @Override
    public String sequence() {
        return text.subSequence(sequenceStart, sequenceEnd).toString();
    }

    @Override
    public int begin() {
        return sequenceStart;
    }

    @Override
    public int end() {
        return sequenceEnd;
    }

    private void primeNext() {
        if (cursor >= length) {
            nextLeadCodePoint = -1;
            return;
        }

        sequenceStart = cursor;
        int cp = Character.codePointAt(text, cursor);
        nextLeadCodePoint = cp;
        currentWidth = calculateWidth(cp);
        cursor += Character.charCount(cp);

        if (cp == Ansi.ESC) {
            consumeAnsi();
        } else if (cp == '\r' || cp == '\n') {
            if (cp == '\r' && cursor < length && text.charAt(cursor) == '\n') {
                cursor++; // Consume the \n of \r\n
            }
            nextLeadCodePoint = '\n';
        } else {
            int riCount = isRegionalIndicator(cp) ? 1 : 0;
            int prevCp = cp;

            while (cursor < length) {
                int curr = Character.codePointAt(text, cursor);

                if (curr == '\r'
                        || curr == '\n'
                        || curr == Ansi.ESC
                        || shouldBreak(prevCp, curr, riCount)) {
                    break;
                }

                cursor += Character.charCount(curr);
                riCount = isRegionalIndicator(curr) ? riCount + 1 : 0;
                prevCp = curr;
            }
        }

        sequenceEnd = cursor;
        primed = true;
    }

    private void consumeAnsi() {
        if (cursor >= length) return;
        char c = text.charAt(cursor++);

        if (c == '[') {
            // --- CSI (Control Sequence Introducer) ---
            // Format: ESC [ (Parameters) (Intermediate bytes) (Final Byte)
            // The Final Byte is always in the range 0x40 to 0x7E.
            while (cursor < length) {
                char n = text.charAt(cursor++);
                if (n >= 0x40 && n <= 0x7E) break;
            }
        } else if (c == ']') {
            // --- OSC (Operating System Command) ---
            // Format: ESC ] (Command string) (Terminator)
            // Terminator is usually BEL (0x07) or ST (ESC \)
            while (cursor < length) {
                char n = text.charAt(cursor++);
                if (n == 0x07) break; // BEL
                if (n == Ansi.ESC && cursor < length && text.charAt(cursor) == '\\') {
                    cursor++; // Consume '\'
                    break;
                }
            }
        }
    }
}

/**
 * An iterator that reads from a Reader and yields Unicode grapheme clusters, ANSI escape sequences
 * and simple codepoints while handling line endings as single units (even when in reality they
 * might be 2 characters).
 */
class ReaderSequenceIterator extends BaseSequenceIterator {
    private final PushbackReader reader;
    private final StringBuilder currentSequence = new StringBuilder();
    private int nextLeadCodePoint = -1;
    private boolean primed = false;
    private boolean exhausted = false;
    private int position = 0;
    private int sequenceStart = 0;

    private static final int NEWLINE = '\n';

    /** Creates a SequenceIterator that reads from the given Reader. */
    ReaderSequenceIterator(Reader reader) {
        this.reader = new PushbackReader(reader, 4);
    }

    @Override
    public boolean hasNext() {
        if (!primed) {
            primeNext();
        }
        return !exhausted;
    }

    @Override
    public int next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        primed = false;
        return nextLeadCodePoint;
    }

    @Override
    public boolean isComplex() {
        return sequence().length() > Character.charCount(nextLeadCodePoint);
    }

    @Override
    public String sequence() {
        return currentSequence.toString();
    }

    /** Returns the start index of the current sequence in characters. */
    @Override
    public int begin() {
        return sequenceStart;
    }

    /** Returns the end index of the current sequence in characters. */
    @Override
    public int end() {
        return sequenceStart + currentSequence.length();
    }

    private void primeNext() {
        currentSequence.setLength(0);
        sequenceStart = position;
        try {
            int cp = readCodePoint();
            if (cp == -1) {
                exhausted = true;
                nextLeadCodePoint = -1;
                return;
            }

            nextLeadCodePoint = cp;
            currentWidth = calculateWidth(cp);
            currentSequence.append(Character.toChars(cp));

            if (cp == Ansi.ESC) {
                consumeAnsi(currentSequence);
            } else if (cp == '\r' || cp == '\n') {
                if (cp == '\r') {
                    int next = read();
                    if (next == '\n') {
                        currentSequence.append('\n');
                    } else if (next != -1) {
                        unread(next);
                    }
                }
                nextLeadCodePoint = NEWLINE;
            } else {
                int riCount = isRegionalIndicator(cp) ? 1 : 0;
                int prevCp = cp;
                while (true) {
                    int curr = readCodePoint();
                    if (curr == -1) break;
                    if (curr == '\r'
                            || curr == '\n'
                            || curr == Ansi.ESC
                            || shouldBreak(prevCp, curr, riCount)) {
                        unreadCodePoint(curr);
                        break;
                    }
                    currentSequence.append(Character.toChars(curr));
                    riCount = isRegionalIndicator(curr) ? riCount + 1 : 0;
                    prevCp = curr;
                }
            }
        } catch (IOException e) {
            exhausted = true;
        }
        primed = true;
    }

    private int readCodePoint() throws IOException {
        int c1 = read();
        if (c1 == -1) return -1;
        if (Character.isHighSurrogate((char) c1)) {
            int c2 = read();
            if (c2 != -1) return Character.toCodePoint((char) c1, (char) c2);
        }
        return c1;
    }

    private void unreadCodePoint(int cp) throws IOException {
        char[] chars = Character.toChars(cp);
        for (int i = chars.length - 1; i >= 0; i--) unread(chars[i]);
    }

    private void consumeAnsi(StringBuilder sb) throws IOException {
        int c = read();
        if (c == -1) return;
        sb.append((char) c);

        if (c == '[') {
            // --- CSI (Control Sequence Introducer) ---
            // Format: ESC [ (Parameters) (Intermediate bytes) (Final Byte)
            // The Final Byte is always in the range 0x40 to 0x7E.
            while (true) {
                int n = read();
                if (n == -1) break;
                sb.append((char) n);
                if (n >= 0x40 && n <= 0x7E)
                    break; // Reached the 'Final Byte' (e.g., 'm' in ESC[31m)
            }
        } else if (c == ']') {
            // --- OSC (Operating System Command) ---
            // Format: ESC ] (Command string) (Terminator)
            // Terminator is usually BEL (0x07) or ST (ESC \)
            while (true) {
                int n = read();
                if (n == -1) break;

                if (n == 0x07) { // Standard BEL terminator
                    sb.append((char) n);
                    break;
                }
                if (n == Ansi.ESC) { // Check for ST terminator (ESC \)
                    int n2 = read();
                    if (n2 == '\\') {
                        sb.append((char) n).append((char) n2);
                        break;
                    }
                    if (n2 != -1) unread(n2);
                }
                sb.append((char) n);
            }
        }
    }

    private int read() throws IOException {
        int c = reader.read();
        if (c != -1) {
            position++;
        }
        return c;
    }

    private void unread(int c) throws IOException {
        reader.unread(c);
        position--;
    }
}
