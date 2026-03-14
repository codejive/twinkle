package org.codejive.twinkle.shapes.util;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.text.RenderTarget;
import org.jspecify.annotations.NonNull;

public class Draw {

    public static void lineH(
            RenderTarget target, int x, int y, int x2, @NonNull Style style, char c) {
        for (int i = x; i < x2; i++) {
            target.putAt(i, y, style, c);
        }
    }

    public static void lineV(
            RenderTarget target, int x, int y, int y2, @NonNull Style style, char c) {
        for (int i = y; i < y2; i++) {
            target.putAt(x, i, style, c);
        }
    }
}
