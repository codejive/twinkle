package org.codejive.twinkle.util;

import java.io.Reader;
import java.util.NoSuchElementException;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;

/**
 * An iterator that wraps a SequenceIterator and tracks the current style state based on ANSI escape
 * sequences encountered in the input.
 */
public class StyledIterator implements SequenceIterator {
    private final SequenceIterator delegate;
    private Style currentStyle;
    private int nextCodePoint = -1;
    private boolean primed = false;
    private boolean exhausted = false;

    /**
     * Creates a StyledIterator that wraps the given SequenceIterator and starts with an unknown
     * initial style state.
     */
    protected StyledIterator(SequenceIterator delegate) {
        this(delegate, Style.of(Style.F_UNKNOWN));
    }

    /**
     * Creates a StyledIterator that wraps the given SequenceIterator and starts with the given
     * initial style state.
     */
    public StyledIterator(SequenceIterator delegate, Style currentStyle) {
        this.delegate = delegate;
        this.currentStyle = currentStyle;
    }

    /** Returns true if there is still input to read. */
    @Override
    public boolean hasNext() {
        if (!primed) {
            primeNext();
        }
        return !exhausted;
    }

    /**
     * Returns the next lead codepoint of the next sequence, skipping over any ANSI escape sequences
     * and updating the current style state. Call {@link #sequence()} to get the full sequence and
     * {@link #style()} to get the current style. In case of line endings, this returns NEWLINE
     * (0x0A).
     *
     * @throws NoSuchElementException if there is no more input.
     */
    @Override
    public int next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        primed = false;
        return nextCodePoint;
    }

    @Override
    public boolean isComplex() {
        return delegate.isComplex();
    }

    @Override
    public int width() {
        return delegate.width();
    }

    @Override
    public int begin() {
        return delegate.begin();
    }

    @Override
    public int end() {
        return delegate.end();
    }

    @Override
    public String sequence() {
        return delegate.sequence();
    }

    /** Returns the current style based on the ANSI escape sequences encountered so far. */
    public Style style() {
        return currentStyle;
    }

    private void primeNext() {
        while (delegate.hasNext()) {
            int cp = delegate.next();
            if (cp == Ansi.ESC) {
                String ansiSequence = delegate.sequence();
                if (ansiSequence.startsWith(Ansi.CSI) && ansiSequence.endsWith("m")) {
                    currentStyle = Style.of(Style.parse(currentStyle.state(), ansiSequence));
                }
            } else {
                nextCodePoint = cp;
                primed = true;
                return;
            }
        }
        exhausted = true;
        primed = true;
    }

    public static StyledIterator of(CharSequence text) {
        return of(text, Style.of(Style.F_UNKNOWN));
    }

    public static StyledIterator of(Reader input) {
        return of(input, Style.of(Style.F_UNKNOWN));
    }

    public static StyledIterator of(SequenceIterator iter) {
        return of(iter, Style.of(Style.F_UNKNOWN));
    }

    public static StyledIterator of(CharSequence text, Style currentStyle) {
        return of(SequenceIterator.of(text), currentStyle);
    }

    public static StyledIterator of(Reader input, Style currentStyle) {
        return of(SequenceIterator.of(input), currentStyle);
    }

    public static StyledIterator of(SequenceIterator iter, Style currentStyle) {
        return new StyledIterator(iter, currentStyle);
    }
}
