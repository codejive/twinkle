package org.codejive.context.terminal;

public class Rect extends Size {
    private final int left, top;

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

    public Size size() {
        return new Size(width(), height());
    }

    public boolean outside(Rect other) {
        return top() > other.bottom()
                || bottom() < other.top()
                || left() > other.right()
                || right() < other.left();
    }

    public boolean inside(Rect other) {
        return top() >= other.top()
                && left() >= other.left()
                && bottom() <= other.bottom()
                && right() <= other.right();
    }

    public boolean overlap(Rect other) {
        return !outside(other) && !inside(other);
    }

    public Rect grow(int leftAmount, int topAmount, int rightAmount, int bottomAmount) {
        return new Rect(
                left - leftAmount,
                top - topAmount,
                Math.max(width() + leftAmount + rightAmount, 0),
                Math.max(height() + topAmount + bottomAmount, 0));
    }

    public Rect limited(Rect availableRect) {
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
