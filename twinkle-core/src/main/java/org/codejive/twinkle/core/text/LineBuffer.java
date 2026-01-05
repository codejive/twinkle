package org.codejive.twinkle.core.text;

import java.io.IOException;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.util.Printable;
import org.codejive.twinkle.util.StyledIterator;
import org.jspecify.annotations.NonNull;

public interface LineBuffer extends Printable {

    char REPLACEMENT_CHAR = '\uFFFD';

    int length();

    char charAt(int index);

    int codepointAt(int i);

    @NonNull String graphemeAt(int i);

    long styleStateAt(int i);

    @NonNull Style styleAt(int i);

    default void setCharAt(int index, @NonNull Style style, char c) {
        setCharAt(index, style.state(), c);
    }

    void setCharAt(int index, long styleState, char c);

    default void setCharAt(int index, @NonNull Style style, int cp) {
        setCharAt(index, style.state(), cp);
    }

    void setCharAt(int index, long styleState, int cp);

    default void setCharAt(int index, @NonNull Style style, @NonNull CharSequence grapheme) {
        setCharAt(index, style.state(), grapheme);
    }

    void setCharAt(int index, long styleState, @NonNull CharSequence grapheme);

    default int putStringAt(int index, @NonNull Style style, @NonNull CharSequence str) {
        return putStringAt(index, style.state(), str);
    }

    int putStringAt(int index, long styleState, @NonNull CharSequence str);

    int putStringAt(int index, @NonNull StyledIterator iter);

    @NonNull LineBuffer subSequence(int start, int end);

    @NonNull LineBuffer resize(int newSize);

    LineBuffer EMPTY =
            new CodepointBuffer(0) {
                @Override
                public @NonNull CodepointBuffer resize(int newSize) {
                    if (newSize != 0) {
                        throw new UnsupportedOperationException("Cannot resize EMPTY");
                    }
                    return this;
                }
            };

    static @NonNull LineBuffer of(int width) {
        return new CodepointBuffer(width);
    }
}

class CodepointBuffer implements LineBuffer {
    protected int[] cpBuffer;
    protected String[] graphemeBuffer;
    protected long[] styleBuffer;

    public CodepointBuffer(int size) {
        cpBuffer = new int[size];
        graphemeBuffer = new String[size];
        styleBuffer = new long[size];
    }

    protected CodepointBuffer(int[] cpBuffer, String[] graphemeBuffer, long[] styleBuffer) {
        if (cpBuffer.length != styleBuffer.length || cpBuffer.length != graphemeBuffer.length) {
            throw new IllegalArgumentException(
                    "Codepoint, grapheme and style buffers must have the same length");
        }
        this.cpBuffer = cpBuffer;
        this.graphemeBuffer = graphemeBuffer;
        this.styleBuffer = styleBuffer;
    }

    @Override
    public int length() {
        return cpBuffer.length;
    }

    @Override
    public char charAt(int index) {
        if (invalidIndex(index)) {
            return REPLACEMENT_CHAR;
        }
        if (graphemeBuffer[index] != null || Character.charCount(cpBuffer[index]) == 2) {
            // TODO log warning about extended Unicode characters not being supported
            return REPLACEMENT_CHAR;
        }
        return (char) cpBuffer[index];
    }

    @Override
    public int codepointAt(int index) {
        if (invalidIndex(index)) {
            return REPLACEMENT_CHAR;
        }
        return cpBuffer[index];
    }

    @Override
    public @NonNull String graphemeAt(int index) {
        if (invalidIndex(index)) {
            return String.valueOf(REPLACEMENT_CHAR);
        }
        if (graphemeBuffer[index] != null) {
            return graphemeBuffer[index];
        }
        return new String(Character.toChars(cpBuffer[index]));
    }

    @Override
    public long styleStateAt(int index) {
        if (invalidIndex(index)) {
            return Style.F_UNSTYLED;
        }
        return styleBuffer[index];
    }

    @Override
    public @NonNull Style styleAt(int index) {
        if (invalidIndex(index)) {
            return Style.UNSTYLED;
        }
        return Style.of(styleBuffer[index]);
    }

    @Override
    public void setCharAt(int index, long styleState, char ch) {
        if (invalidIndex(index)) {
            return;
        }
        if (Character.isSurrogate(ch)) {
            // TODO log warning about surrogate characters not being supported
            ch = REPLACEMENT_CHAR;
        }
        setCharAt_(index, styleState, ch);
    }

    private void setCharAt_(int index, long styleState, char ch) {
        if (Character.isSurrogate(ch)) {
            // TODO log warning about surrogate characters not being supported
            ch = REPLACEMENT_CHAR;
        }
        cpBuffer[index] = ch;
        graphemeBuffer[index] = null;
        styleBuffer[index] = styleState;
    }

    @Override
    public void setCharAt(int index, long styleState, int cp) {
        if (invalidIndex(index)) {
            return;
        }
        setCharAt_(index, styleState, cp);
    }

    private void setCharAt_(int index, long styleState, int cp) {
        cpBuffer[index] = cp;
        graphemeBuffer[index] = null;
        styleBuffer[index] = styleState;
    }

    @Override
    public void setCharAt(int index, long styleState, @NonNull CharSequence grapheme) {
        if (invalidIndex(index)) {
            return;
        }
        setCharAt_(index, styleState, grapheme);
    }

    private void setCharAt_(int index, long styleState, @NonNull CharSequence grapheme) {
        if (grapheme.length() == 0) {
            return;
        }
        cpBuffer[index] = -1;
        graphemeBuffer[index] = grapheme.toString();
        styleBuffer[index] = styleState;
    }

    @Override
    public int putStringAt(int index, long styleState, @NonNull CharSequence str) {
        return putStringAt(index, StyledIterator.of(str, styleState));
    }

    @Override
    public int putStringAt(int index, @NonNull StyledIterator iter) {
        int minIndex = 0;
        int maxIndex = cpBuffer.length;
        int startIndex = Math.max(index, minIndex);
        int len = maxIndex - startIndex;
        int cnt = 0;
        while (iter.hasNext()) {
            long style = iter.next();
            int cp = iter.next();
            if (cp == '\n') {
                // We only deal with single lines here, so stop at newline
                break;
            }
            if (iter.width() == 0) {
                // We shouldn't be getting any of these from a StyledIterator, but just in case...
                continue;
            }
            if (cnt < len) {
                if (iter.isComplex()) {
                    setCharAt_(startIndex + cnt, style, iter.sequence());
                } else {
                    setCharAt_(startIndex + cnt, style, cp);
                }
            }
            cnt++;
            if (iter.width() == 2 && cnt < len) {
                // We're dealing with a wide character, so we need to mark the next cell as skipped
                setSkipAt(startIndex + cnt);
                cnt++;
            }
        }
        return cnt;
    }

    private void setSkipAt(int index) {
        cpBuffer[index] = -1;
        graphemeBuffer[index] = null;
        styleBuffer[index] = -1;
    }

    @Override
    public @NonNull CodepointBuffer subSequence(int start, int end) {
        if (start < 0 || end > length() || start > end) {
            throw new IndexOutOfBoundsException(
                    "Invalid subsequence range: " + start + " to " + end);
        }
        int subLength = end - start;
        int[] subCpBuffer = new int[subLength];
        String[] subGraphemeBuffer = new String[subLength];
        long[] subStyleBuffer = new long[subLength];
        System.arraycopy(cpBuffer, start, subCpBuffer, 0, subLength);
        System.arraycopy(graphemeBuffer, start, subGraphemeBuffer, 0, subLength);
        System.arraycopy(styleBuffer, start, subStyleBuffer, 0, subLength);
        return new CodepointBuffer(subCpBuffer, subGraphemeBuffer, subStyleBuffer);
    }

    @Override
    public @NonNull CodepointBuffer resize(int newSize) {
        if (newSize == cpBuffer.length) {
            return this;
        }
        int[] newCpBuffer = new int[newSize];
        String[] newGraphemeBuffer = new String[newSize];
        long[] newStyleBuffer = new long[newSize];
        int copyLength = Math.min(newSize, length());
        System.arraycopy(cpBuffer, 0, newCpBuffer, 0, copyLength);
        System.arraycopy(graphemeBuffer, 0, newGraphemeBuffer, 0, copyLength);
        System.arraycopy(styleBuffer, 0, newStyleBuffer, 0, copyLength);
        cpBuffer = newCpBuffer;
        graphemeBuffer = newGraphemeBuffer;
        styleBuffer = newStyleBuffer;
        return this;
    }

    private static int codepointCount(@NonNull CharSequence str) {
        int count = 0;
        for (int i = 0; i < str.length(); ) {
            int cp = codepointAt(str, i);
            count++;
            i += Character.charCount(cp);
        }
        return count;
    }

    private static int codepointAt(@NonNull CharSequence str, int index) {
        if (index < 0 || index >= str.length()) {
            return REPLACEMENT_CHAR;
        }
        char ch = str.charAt(index);
        if (Character.isHighSurrogate(ch) && (index + 1) < str.length()) {
            char low = str.charAt(index + 1);
            if (Character.isLowSurrogate(low)) {
                return Character.toCodePoint(ch, low);
            }
        } else if (Character.isLowSurrogate(ch) && index > 0) {
            char high = str.charAt(index - 1);
            if (Character.isHighSurrogate(high)) {
                return Character.toCodePoint(high, ch);
            }
        }
        return ch;
    }

    private boolean invalidIndex(int index) {
        return index < 0 || index >= cpBuffer.length;
    }

    private boolean outside(int index, int length) {
        return (index + length) <= 0 || index >= cpBuffer.length;
    }

    @Override
    public @NonNull String toString() {
        // Assuming only single-width characters for capacity estimation
        int initialCapacity = length();
        StringBuilder sb = new StringBuilder(initialCapacity);
        for (int i = 0; i < length(); i++) {
            if (graphemeBuffer[i] != null) {
                sb.append(graphemeBuffer[i]);
            } else {
                int cp = cpBuffer[i];
                if (cp == '\0') {
                    cp = ' ';
                }
                sb.appendCodePoint(cp);
            }
        }
        return sb.toString();
    }

    @Override
    public @NonNull String toAnsiString() {
        // Assuming only single-width characters for capacity estimation
        // plus 20 extra for escape codes
        int initialCapacity = length() + 20;
        StringBuilder sb = new StringBuilder(initialCapacity);
        try {
            return toAnsi(sb).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull Appendable toAnsi(Appendable appendable) throws IOException {
        appendable.append(Ansi.STYLE_RESET);
        return toAnsi(appendable, Style.UNSTYLED.state());
    }

    /**
     * Converts the buffer to an ANSI string, including ANSI escape codes for styles and colors.
     * This method takes into account the provided current style to generate a result that is as
     * efficient as possible in terms of ANSI codes.
     *
     * <p>A system property "twinkle.styledbuffer.toAnsi" can be set to "fast" or "short" to choose
     * between two strategies for generating the ANSI string. The "fast" strategy generates the ANSI
     * string in a single pass, while the "short" strategy generates two ANSI strings and returns
     * the shorter one. The default is "short".
     *
     * @param styleState The current style to start with.
     * @return The ANSI string representation of the styled buffer.
     */
    @Override
    public @NonNull String toAnsiString(long styleState) {
        // Assuming only single-width characters for capacity estimation
        // plus 20 extra for escape codes
        int initialCapacity = length() + 20;
        StringBuilder sb = new StringBuilder(initialCapacity);
        try {
            return toAnsi(sb, styleState).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull Appendable toAnsi(Appendable appendable, long lastStyleState)
            throws IOException {
        for (int i = 0; i < length(); i++) {
            if (styleBuffer[i] != lastStyleState) {
                Style style = Style.of(styleBuffer[i]);
                style.toAnsi(appendable, lastStyleState);
                lastStyleState = styleBuffer[i];
            }
            if (graphemeBuffer[i] != null) {
                appendable.append(graphemeBuffer[i]);
            } else {
                int cp = cpBuffer[i];
                if (cp == '\0') {
                    cp = ' ';
                }
                if (Character.isBmpCodePoint(cp)) {
                    appendable.append((char) cp);
                } else if (Character.isValidCodePoint(cp)) {
                    appendable.append(Character.lowSurrogate(cp));
                    appendable.append(Character.highSurrogate(cp));
                } else {
                    throw new IllegalArgumentException(
                            String.format("Not a valid Unicode code point: 0x%X", cp));
                }
            }
        }
        return appendable;
    }
}
