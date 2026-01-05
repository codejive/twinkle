package org.codejive.twinkle.core.util;

import org.jspecify.annotations.NonNull;

public class Rect extends Size implements Sized {
    private final int left, top;

    public static @NonNull Rect of(int width, int height) {
        return new Rect(0, 0, width, height);
    }

    public static @NonNull Rect of(@NonNull Size size) {
        return new Rect(0, 0, size.width(), size.height());
    }

    public static @NonNull Rect of(int left, int top, int width, int height) {
        return new Rect(left, top, width, height);
    }

    public static @NonNull Rect of(int left, int top, @NonNull Size size) {
        return new Rect(left, top, size.width(), size.height());
    }

    public Rect(int left, int top, int width, int height) {
        super(width, height);
        this.left = left;
        this.top = top;
    }

    public int left() {
        return left;
    }

    public int right() {
        return left + width() - 1;
    }

    public int top() {
        return top;
    }

    public int bottom() {
        return top + height() - 1;
    }

    @Override
    public @NonNull Size size() {
        return this;
    }

    public boolean outside(@NonNull Rect other) {
        return top() > other.bottom()
                || bottom() < other.top()
                || left() > other.right()
                || right() < other.left();
    }

    public boolean inside(@NonNull Rect other) {
        return top() >= other.top()
                && left() >= other.left()
                && bottom() <= other.bottom()
                && right() <= other.right();
    }

    public boolean overlap(@NonNull Rect other) {
        return !outside(other) && !inside(other);
    }

    public Rect grow(int leftAmount, int topAmount, int rightAmount, int bottomAmount) {
        return new Rect(
                left - leftAmount,
                top - topAmount,
                Math.max(width() + leftAmount + rightAmount, 0),
                Math.max(height() + topAmount + bottomAmount, 0));
    }

    public Rect limited(@NonNull Rect availableRect) {
        int l = Math.max(left, availableRect.left());
        int t = Math.max(top, availableRect.top());
        int r = Math.min(right(), availableRect.right());
        int b = Math.min(bottom(), availableRect.bottom());
        return new Rect(l, t, r - l + 1, b - t + 1);
    }

    @Override
    public String toString() {
        return "Rect{"
                + "left="
                + left
                + ", top="
                + top
                + ", width="
                + width()
                + ", height="
                + height()
                + '}';
    }
}
