package org.codejive.twinkle.core.util;

import org.jspecify.annotations.NonNull;

/**
 * This class defines a rectangle (similar to <code>Rect</code>) but with the ability to have
 * negative width and height which means that the width or height is relative to the available size.
 */
public class FlexRect {
    private final int left, top, width, height;

    public FlexRect(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public @NonNull Rect actualRect(@NonNull Size availableSize) {
        Rect availableRect = new Rect(0, 0, availableSize.width(), availableSize.height());
        int w = width >= 0 ? width : availableSize.width() - left + width + 1;
        int h = height >= 0 ? height : availableSize.height() - top + height + 1;
        return new Rect(left, top, w, h).limited(availableRect);
    }
}
