package org.codejive.twinkle.screen.util;

import org.codejive.twinkle.text.Position;
import org.codejive.twinkle.text.Size;
import org.jspecify.annotations.NonNull;

public class Rect implements Sized {
    private final @NonNull Position pos;
    private final @NonNull Size size;

    public static @NonNull Rect of(int width, int height) {
        return of(0, 0, width, height);
    }

    public static @NonNull Rect of(@NonNull Size size) {
        return of(0, 0, size);
    }

    public static @NonNull Rect of(int left, int top, int width, int height) {
        return new Rect(Position.of(left, top), Size.of(width, height));
    }

    public static @NonNull Rect of(int left, int top, @NonNull Size size) {
        return new Rect(Position.of(left, top), size);
    }

    public static @NonNull Rect of(@NonNull Position pos, @NonNull Size size) {
        return new Rect(pos, size);
    }

    public Rect(@NonNull Position pos, @NonNull Size size) {
        this.pos = pos;
        this.size = size;
    }

    public int x() {
        return pos.x();
    }

    public int y() {
        return pos.y();
    }

    public int left() {
        return pos.x();
    }

    public int right() {
        return left() + width() - 1;
    }

    public int top() {
        return pos.y();
    }

    public int bottom() {
        return top() + height() - 1;
    }

    public int width() {
        return size.width();
    }

    public int height() {
        return size.height();
    }

    public @NonNull Position position() {
        return pos;
    }

    @Override
    public @NonNull Size size() {
        return size;
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
        return Rect.of(
                left() - leftAmount,
                top() - topAmount,
                Math.max(width() + leftAmount + rightAmount, 0),
                Math.max(height() + topAmount + bottomAmount, 0));
    }

    public Rect limited(@NonNull Rect availableRect) {
        int l = Math.max(left(), availableRect.left());
        int t = Math.max(top(), availableRect.top());
        int r = Math.min(right(), availableRect.right());
        int b = Math.min(bottom(), availableRect.bottom());
        return Rect.of(l, t, r - l + 1, b - t + 1);
    }

    public Rect appliedTo(@NonNull Rect rect) {
        return of(rect.left() + left(), rect.top() + top(), size);
    }

    @Override
    public String toString() {
        return "Rect{"
                + "left="
                + left()
                + ", top="
                + top()
                + ", width="
                + width()
                + ", height="
                + height()
                + '}';
    }
}
