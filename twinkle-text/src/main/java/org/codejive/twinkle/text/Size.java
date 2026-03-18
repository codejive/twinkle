package org.codejive.twinkle.text;

import java.util.Objects;
import org.jspecify.annotations.NonNull;

public class Size {
    private final int width;
    private final int height;

    public static Size EMPTY = new Size(0, 0);
    public static Size MAX = new Size(Integer.MAX_VALUE, Integer.MAX_VALUE);

    public static @NonNull Size of(int width, int height) {
        if (width == 0 && height == 0) {
            return EMPTY;
        }
        if (width == Integer.MAX_VALUE && height == Integer.MAX_VALUE) {
            return MAX;
        }
        return new Size(width, height);
    }

    protected Size(int width, int height) {
        assert width >= 0;
        assert height >= 0;
        this.width = width;
        this.height = height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /**
     * Grows the size by the given size, returning a new Size instance with the updated dimensions.
     *
     * @param growSize the amount to grow the width
     * @return a new size with the updated dimensions
     */
    public Size grow(Size growSize) {
        return grow(growSize.width(), growSize.height());
    }

    /**
     * Grows the size by the given amounts in width and height, returning a new Size instance with
     * the updated dimensions.
     *
     * @param dw the amount to grow the width
     * @param dh the amount to grow the height
     * @return a new size with the updated dimensions
     */
    public Size grow(int dw, int dh) {
        return of(width + dw, height + dh);
    }

    /**
     * Shrinks the size by the given size, returning a new Size instance with the updated
     * dimensions.
     *
     * @param shrinkSize the amount to shrink the width
     * @return a new size with the updated dimensions
     */
    public Size shrink(Size shrinkSize) {
        return shrink(shrinkSize.width(), shrinkSize.height());
    }

    /**
     * Shrinks the size by the given amounts in width and height, returning a new Size instance with
     * the updated dimensions.
     *
     * @param dw the amount to shrink the width
     * @param dh the amount to shrink the height
     * @return a new size with the updated dimensions
     */
    public Size shrink(int dw, int dh) {
        return of(width - dw, height - dh);
    }

    /**
     * Returns the center position of an area of this size
     *
     * @return the center position
     */
    public Position center() {
        return new Position(width / 2, height / 2);
    }

    /**
     * Returns the position where the other size would be centered, both horizontally as vertically,
     * within this size.
     *
     * @param otherSize the other size to center
     * @return the position where the other size would be centered within this size
     */
    public Position center(Size otherSize) {
        int posX = (width - otherSize.width) / 2;
        int posY = (height - otherSize.height) / 2;
        return new Position(posX, posY);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Size size = (Size) o;
        return width == size.width && height == size.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
