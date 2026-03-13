package org.codejive.twinkle.text.util;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.NoSuchElementException;
import org.codejive.twinkle.ansi.Constants;

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
        if (Unicode.isL(prev)
                && (Unicode.isL(curr)
                        || Unicode.isV(curr)
                        || Unicode.isLV(curr)
                        || Unicode.isLVT(curr))) return false;
        if ((Unicode.isLV(prev) || Unicode.isV(prev)) && (Unicode.isV(curr) || Unicode.isT(curr)))
            return false;
        if ((Unicode.isLVT(prev) || Unicode.isT(prev)) && Unicode.isT(curr)) return false;
        int type = Character.getType(curr);
        if (type == Character.NON_SPACING_MARK
                || type == Character.COMBINING_SPACING_MARK
                || curr == Unicode.ZWJ
                || prev == Unicode.ZWJ) return false;
        if (Unicode.isRegionalIndicator(prev) && Unicode.isRegionalIndicator(curr))
            return (riCount % 2 == 0);
        return !(Unicode.isPrepend(prev) || Unicode.isVirama(prev));
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

        if (Unicode.isWide(cp)) {
            return 2;
        }

        return 1;
    }
}

/**
 * A high-performance implementation of SequenceIterator that operates directly on a CharSequence
 * without using Streams or Readers.
 */
class CharSequenceSequenceIterator extends BaseSequenceIterator {
    private final CharSequence text;
    private final int length;
    private final SequenceDecoder decoder = new SequenceDecoder();

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
        decoder.reset();

        while (cursor < length) {
            int cp = Character.codePointAt(text, cursor);
            int cpChars = Character.charCount(cp);

            if (!decoder.canPush(cp)) {
                if (cursor == sequenceStart) {
                    // Defensive fallback for invalid stand-alone UTF-16 code units.
                    nextLeadCodePoint = cp;
                    currentWidth = calculateWidth(cp);
                    cursor += cpChars;
                    sequenceEnd = cursor;
                    primed = true;
                    return;
                }
                break;
            }

            decoder.push(cp);
            cursor += cpChars;
        }

        if (cursor >= length || decoder.state() == SequenceDecoder.State.INCOMPLETE) {
            decoder.finish();
        }

        sequenceEnd = cursor;
        if (decoder.state() == SequenceDecoder.State.ANSI_ESCAPE_SEQUENCE) {
            nextLeadCodePoint = Constants.ESC;
            currentWidth = 0;
        } else {
            int decodedLead = decoder.codepoint();
            nextLeadCodePoint =
                    decodedLead != -1 ? decodedLead : Character.codePointAt(text, sequenceStart);
            int width = decoder.width();
            currentWidth = width >= 0 ? width : calculateWidth(nextLeadCodePoint);
        }

        primed = true;
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
    private final SequenceDecoder decoder = new SequenceDecoder();
    private int nextLeadCodePoint = -1;
    private boolean primed = false;
    private boolean exhausted = false;
    private int position = 0;
    private int sequenceStart = 0;

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
        nextLeadCodePoint = -1;

        try {
            int cp = readCodePoint();
            if (cp == -1) {
                exhausted = true;
                return;
            }

            decoder.reset();
            while (cp != -1) {
                if (!decoder.canPush(cp)) {
                    if (currentSequence.length() == 0) {
                        // Defensive fallback for invalid stand-alone UTF-16 code units.
                        currentSequence.append(Character.toChars(cp));
                        nextLeadCodePoint = cp;
                        currentWidth = calculateWidth(cp);
                    } else {
                        unreadCodePoint(cp);
                    }
                    break;
                }

                decoder.push(cp);
                currentSequence.append(Character.toChars(cp));
                cp = readCodePoint();
            }

            if (cp == -1 && currentSequence.length() > 0) {
                decoder.finish();
            }

            if (currentSequence.length() > 0
                    && (cp == -1 || decoder.state() == SequenceDecoder.State.INCOMPLETE)) {
                decoder.finish();
            }

            if (currentSequence.length() > 0 && nextLeadCodePoint == -1) {
                if (decoder.state() == SequenceDecoder.State.ANSI_ESCAPE_SEQUENCE) {
                    nextLeadCodePoint = Constants.ESC;
                    currentWidth = 0;
                } else {
                    int decodedLead = decoder.codepoint();
                    nextLeadCodePoint =
                            decodedLead != -1
                                    ? decodedLead
                                    : Character.codePointAt(currentSequence, 0);
                    int width = decoder.width();
                    currentWidth = width >= 0 ? width : calculateWidth(nextLeadCodePoint);
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
