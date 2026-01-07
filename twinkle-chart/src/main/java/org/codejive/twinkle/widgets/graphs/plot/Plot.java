package org.codejive.twinkle.widgets.graphs.plot;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.text.Buffer;
import org.codejive.twinkle.core.text.Canvas;
import org.codejive.twinkle.core.util.Size;
import org.codejive.twinkle.core.widget.Widget;
import org.jspecify.annotations.NonNull;

public class Plot implements Widget {
    private final Canvas canvas;
    private final int cOrgX;
    private final int cOrgY;
    private final Size plotSize;
    private Style currentStyle = Style.UNSTYLED;

    private static final char BLOCK_FULL = '█';
    private static final char EMPTY = ' ';

    private static final char DOT_LOWER_LEFT = '▖';
    private static final char DOT_LOWER_RIGHT = '▗';
    private static final char DOT_UPPER_LEFT = '▘';
    private static final char DOT_UPPER_RIGHT = '▝';

    private static final char DOTS_TL_BR = '▚';
    private static final char DOTS_BL_TR = '▞';

    private static final char BLOCK_LEFT_HALF = '▌'; // full, left half
    private static final char BLOCK_RIGHT_HALF = '▐'; // full, right half
    private static final char BLOCK_TOP_HALF = '▀'; // full, top half
    private static final char BLOCK_BOTTOM_HALF = '▄'; // full, bottom half

    private static final char HOLE_LOWER_LEFT = '▜';
    private static final char HOLE_LOWER_RIGHT = '▛';
    private static final char HOLE_UPPER_LEFT = '▟';
    private static final char HOLE_UPPER_RIGHT = '▙';

    private static final char[] ALL_DOTS = {
        EMPTY, DOT_LOWER_LEFT, DOT_LOWER_RIGHT, BLOCK_BOTTOM_HALF,
        DOT_UPPER_LEFT, BLOCK_LEFT_HALF, DOTS_TL_BR, HOLE_UPPER_RIGHT,
        DOT_UPPER_RIGHT, DOTS_BL_TR, BLOCK_RIGHT_HALF, HOLE_UPPER_LEFT,
        BLOCK_TOP_HALF, HOLE_LOWER_RIGHT, HOLE_LOWER_LEFT, BLOCK_FULL
    };

    private static final char[] SINGLE_DOTS = {
        DOT_LOWER_LEFT, DOT_LOWER_RIGHT, DOT_UPPER_LEFT, DOT_UPPER_RIGHT
    };

    private static final int dotIndex[] = {1, 2, 4, 7, 6, 13, 14, 8, 9, 11};

    public static Plot of(Size size) {
        return new Plot(Buffer.of(size));
    }

    public static Plot of(Canvas canvas) {
        return new Plot(canvas);
    }

    protected Plot(Canvas canvas) {
        this.canvas = canvas;
        this.cOrgX = 0;
        this.cOrgY = canvas.size().height() - 1;
        this.plotSize = Size.of(canvas.size().width() * 2, canvas.size().height() * 2);
    }

    @Override
    public @NonNull Size size() {
        return canvas.size();
    }

    public @NonNull Size plotSize() {
        return plotSize;
    }

    public @NonNull Style currentStyle() {
        return currentStyle;
    }

    public Plot currentStyle(Style currentStyle) {
        this.currentStyle = currentStyle;
        return this;
    }

    @Override
    public void render(Canvas canvas) {
        this.canvas.copyTo(canvas, 0, 0);
    }

    public Plot plot(int x, int y) {
        return plot(x, y, currentStyle);
    }

    public Plot plot(int x, int y, Style style) {
        int cx = cOrgX + x / 2;
        int cy = cOrgY - y / 2;
        int rx = x % 2;
        int ry = y % 2;
        char newDot = selectDot(rx, ry);
        char existingDot = canvas.charAt(cx, cy);
        char combinedDot = combineDots(existingDot, newDot);
        canvas.setCharAt(cx, cy, style, combinedDot);
        return this;
    }

    public Plot unplot(int x, int y) {
        int cx = cOrgX + x / 2;
        int cy = cOrgY - y / 2;
        int rx = x % 2;
        int ry = y % 2;
        char removeDot = selectDot(rx, ry);
        char existingDot = canvas.charAt(cx, cy);
        char combinedDot = uncombineDots(existingDot, removeDot);
        canvas.setCharAt(cx, cy, currentStyle, combinedDot);
        return this;
    }

    public Plot clear() {
        for (int y = 0; y < size().height(); y++) {
            for (int x = 0; x < size().width(); x++) {
                canvas.setCharAt(x, y, currentStyle, ' ');
            }
        }
        return this;
    }

    private char selectDot(int rx, int ry) {
        int dotIdx = ry * 2 + rx;
        return SINGLE_DOTS[dotIdx];
    }

    private int charToDotIndex(char c) {
        if (c == '\u2584') {
            return 3;
        } else if (c == '\u258c') {
            return 5;
        } else if (c == '\u2590') {
            return 10;
        } else if (c == '\u2580') {
            return 12;
        } else if (c == '\u2588') {
            return 15;
        } else if (c >= '\u2596' && c <= '\u259f') {
            int idx = c - '\u2596';
            return dotIndex[idx];
        }
        return 0;
    }

    private char combineDots(char existing, char added) {
        int existingIdx = charToDotIndex(existing);
        int addedIdx = charToDotIndex(added);
        int combinedIdx = existingIdx | addedIdx;
        return ALL_DOTS[combinedIdx];
    }

    private char uncombineDots(char existing, char removed) {
        int existingIdx = charToDotIndex(existing);
        int removedIdx = charToDotIndex(removed);
        int combinedIdx = existingIdx & (~removedIdx);
        return ALL_DOTS[combinedIdx];
    }
}
