package org.codejive.twinkle.core.component;

import java.util.Objects;
import org.jspecify.annotations.NonNull;

public class Size {
    private final int width;
    private final int height;

    public static @NonNull Size of(int width, int height) {
        return new Size(width, height);
    }

    public Size(int width, int height) {
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
