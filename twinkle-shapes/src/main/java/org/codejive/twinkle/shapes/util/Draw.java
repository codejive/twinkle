package org.codejive.twinkle.shapes.util;

import org.codejive.twinkle.screen.RenderTarget;
import org.codejive.twinkle.screen.RenderTarget.PrintOption;

public class Draw {

    public static void lineH(
            RenderTarget target, int x, int y, int x2, char c, PrintOption... options) {
        for (int i = x; i < x2; i++) {
            target.putAt(i, y, c, options);
        }
    }

    public static void lineV(
            RenderTarget target, int x, int y, int y2, char c, PrintOption... options) {
        for (int i = y; i < y2; i++) {
            target.putAt(x, i, c, options);
        }
    }
}
