package org.codejive.twinkle.core.text;

import org.codejive.twinkle.ansi.Style;
import org.jspecify.annotations.NonNull;

public class StyledStringBuilder implements StyledCharSequence {
    private StyledBuffer buffer;
    private int length;

    private static final int INITIAL_CAPACITY = 16;

    public static StyledStringBuilder create() {
        return new StyledStringBuilder(INITIAL_CAPACITY);
    }

    public static StyledStringBuilder create(int initialCapacity) {
        return new StyledStringBuilder(initialCapacity);
    }

    public static StyledStringBuilder of(Style style, CharSequence str) {
        return of(style.state(), str);
    }

    public static StyledStringBuilder of(long styleState, CharSequence str) {
        StyledStringBuilder builder = new StyledStringBuilder(str.length() + INITIAL_CAPACITY);
        builder.append(styleState, str);
        return builder;
    }

    public static StyledStringBuilder of(StyledCharSequence str) {
        StyledStringBuilder builder = new StyledStringBuilder(str.length() + INITIAL_CAPACITY);
        builder.append(str);
        return builder;
    }

    public StyledStringBuilder(int initialCapacity) {
        this.buffer = StyledBuffer.of(initialCapacity);
        this.length = 0;
    }

    public StyledStringBuilder append(StyledCharSequence str) {
        ensureCapacity(str.length());
        length += buffer.putStringAt(length, str);
        return this;
    }

    public StyledStringBuilder append(Style style, CharSequence str) {
        ensureCapacity(str.length());
        length += buffer.putStringAt(length, style, str);
        return this;
    }

    public StyledStringBuilder append(long styleState, CharSequence str) {
        ensureCapacity(str.length());
        length += buffer.putStringAt(length, styleState, str);
        return this;
    }

    public StyledStringBuilder append(Style style, Object obj) {
        String str = String.valueOf(obj);
        return append(style, str);
    }

    public StyledStringBuilder append(long styleState, Object obj) {
        String str = String.valueOf(obj);
        return append(styleState, str);
    }

    public StyledStringBuilder append(Style style, char ch) {
        String str = String.valueOf(ch);
        return append(style, str);
    }

    public StyledStringBuilder append(long styleState, char ch) {
        String str = String.valueOf(ch);
        return append(styleState, str);
    }

    public StyledStringBuilder append(Style style, long number) {
        String str = String.valueOf(number);
        return append(style, str);
    }

    public StyledStringBuilder append(long styleState, long number) {
        String str = String.valueOf(number);
        return append(styleState, str);
    }

    public StyledStringBuilder append(Style style, int number) {
        String str = String.valueOf(number);
        return append(style, str);
    }

    public StyledStringBuilder append(long styleState, int number) {
        String str = String.valueOf(number);
        return append(styleState, str);
    }

    public StyledStringBuilder append(Style style, double number) {
        String str = String.valueOf(number);
        return append(style, str);
    }

    public StyledStringBuilder append(long styleState, double number) {
        String str = String.valueOf(number);
        return append(styleState, str);
    }

    public StyledStringBuilder append(Style style, float number) {
        String str = String.valueOf(number);
        return append(style, str);
    }

    public StyledStringBuilder append(long styleState, float number) {
        String str = String.valueOf(number);
        return append(styleState, str);
    }

    public StyledStringBuilder append(Style style, boolean bool) {
        String str = String.valueOf(bool);
        return append(style, str);
    }

    public StyledStringBuilder append(long styleState, boolean bool) {
        String str = String.valueOf(bool);
        return append(styleState, str);
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        return buffer.charAt(index);
    }

    @Override
    public int codepointAt(int index) {
        return buffer.codepointAt(index);
    }

    @Override
    public @NonNull String graphemeAt(int index) {
        return buffer.graphemeAt(index);
    }

    @Override
    public long styleStateAt(int index) {
        return buffer.styleStateAt(index);
    }

    @Override
    public @NonNull Style styleAt(int index) {
        return buffer.styleAt(index);
    }

    @Override
    public @NonNull StyledCharSequence subSequence(int start, int end) {
        return buffer.subSequence(start, end);
    }

    private void ensureCapacity(int extraLength) {
        int requiredLength = length + extraLength;
        if (requiredLength > buffer.length()) {
            buffer = buffer.resize(requiredLength + 2 * INITIAL_CAPACITY);
        }
    }
}
