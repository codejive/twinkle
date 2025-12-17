package org.codejive.twinkle.core.text;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;
import org.jspecify.annotations.NonNull;

public class StyledCodepointBuffer implements StyledBuffer {
    protected int[] cpBuffer;
    protected long[] styleBuffer;

    public StyledCodepointBuffer(int size) {
        cpBuffer = new int[size];
        styleBuffer = new long[size];
    }

    protected StyledCodepointBuffer(int[] cpBuffer, long[] styleBuffer) {
        if (cpBuffer.length != styleBuffer.length) {
            throw new IllegalArgumentException(
                    "Codepoint buffer and style buffer must have the same length");
        }
        this.cpBuffer = cpBuffer;
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
        if (Character.charCount(cpBuffer[index]) == 2) {
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
        int cp;
        if (codepointCount(grapheme) > 1) {
            // TODO log warning about extended Unicode graphemes not being supported
            cp = REPLACEMENT_CHAR;
        } else {
            cp = codepointAt(grapheme, 0);
        }
        cpBuffer[index] = cp;
        styleBuffer[index] = styleState;
    }

    @Override
    public int putStringAt(int index, long styleState, @NonNull CharSequence str) {
        if (outside(index, str.length())) {
            return str.length();
        }
        // TODO this code can be optimized by avoiding calculating codepointCount
        //  and simply looping until the end of the char sequence is reached
        int cpsCount = codepointCount(str);
        int minIndex = 0;
        int maxIndex = cpBuffer.length;
        int startIndex = Math.max(index, minIndex);
        int strStart = Math.max(startIndex - index, 0);
        int endIndex = Math.min(index + cpsCount, maxIndex);
        int len = endIndex - startIndex;
        for (int i = 0; i < len; ) {
            int cp = codepointAt(str, strStart + i);
            setCharAt_(startIndex + i, styleState, cp);
            i += Character.charCount(cp);
        }
        return cpsCount;
    }

    @Override
    public int putStringAt(int index, @NonNull StyledCharSequence str) {
        if (outside(index, str.length())) {
            return str.length();
        }
        int minIndex = 0;
        int maxIndex = cpBuffer.length;
        int startIndex = Math.max(index, minIndex);
        int endIndex = Math.min(index + str.length(), maxIndex);
        int strStart = Math.max(startIndex - index, 0);
        int len = endIndex - startIndex;
        for (int i = 0; i < len; i++) {
            setCharAt_(
                    startIndex + i, str.styleStateAt(strStart + i), str.codepointAt(strStart + i));
        }
        return str.length();
    }

    @Override
    public @NonNull StyledCharSequence subSequence(int start, int end) {
        if (start < 0 || end > length() || start > end) {
            throw new IndexOutOfBoundsException(
                    "Invalid subsequence range: " + start + " to " + end);
        }
        int subLength = end - start;
        int[] subCpBuffer = new int[subLength];
        long[] subStyleBuffer = new long[subLength];
        System.arraycopy(cpBuffer, start, subCpBuffer, 0, subLength);
        System.arraycopy(styleBuffer, start, subStyleBuffer, 0, subLength);
        return new StyledCodepointBuffer(subCpBuffer, subStyleBuffer);
    }

    @Override
    public @NonNull StyledCodepointBuffer resize(int newSize) {
        if (newSize == cpBuffer.length) {
            return this;
        }
        int[] newCpBuffer = new int[newSize];
        long[] newStyleBuffer = new long[newSize];
        int copyLength = Math.min(newSize, length());
        System.arraycopy(cpBuffer, 0, newCpBuffer, 0, copyLength);
        System.arraycopy(styleBuffer, 0, newStyleBuffer, 0, copyLength);
        cpBuffer = newCpBuffer;
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
            int cp = cpBuffer[i];
            if (cp == '\0') {
                cp = ' ';
            }
            sb.appendCodePoint(cp);
        }
        return sb.toString();
    }

    @Override
    public @NonNull String toAnsiString() {
        // Assuming only single-width characters for capacity estimation
        // plus 20 extra for escape codes
        int initialCapacity = length() + 20;
        StringBuilder sb = new StringBuilder(initialCapacity);
        sb.append(Ansi.STYLE_RESET);
        return toAnsiString(sb, Style.UNSTYLED.state()).toString();
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
        return toAnsiString(sb, styleState).toString();
    }

    private @NonNull StringBuilder toAnsiString(StringBuilder sb, long lastStyleState) {
        for (int i = 0; i < length(); i++) {
            if (styleBuffer[i] != lastStyleState) {
                Style style = Style.of(styleBuffer[i]);
                style.toAnsiString(sb);
                lastStyleState = styleBuffer[i];
            }
            int cp = cpBuffer[i];
            if (cp == '\0') {
                cp = ' ';
            }
            sb.appendCodePoint(cp);
        }
        return sb;
    }
}
