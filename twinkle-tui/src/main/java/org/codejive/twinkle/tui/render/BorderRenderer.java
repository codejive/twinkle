package org.codejive.twinkle.tui.render;

import org.codejive.twinkle.core.widget.Canvas;
import org.codejive.twinkle.core.widget.Rect;
import org.codejive.twinkle.tui.util.Util;
import org.jline.utils.AttributedString;

public class BorderRenderer {
    private final Canvas canvas;

    public BorderRenderer(Canvas canvas) {
        this.canvas = canvas;
    }

    public void render(Box box) {
        int lw = box.border_left_width();
        int tw = box.border_top_width();
        int rw = box.border_right_width();
        int bw = box.border_bottom_width();
        if (lw == 0 && tw == 0 && rw == 0 && bw == 0) {
            // Nothing to do
            return;
        }
        // Calculate the rectangle to draw the border in
        Rect r = box.rect().grow(lw, tw, rw, bw);
        if (r.outside(canvas.rect())) {
            return;
        }

        int x = r.left();
        int y = r.top();
        int w = r.width();
        int h = r.height();
        AttributedString tbs = new AttributedString(String.format("+%s+", Util.repeat("-", w - 2)));
        AttributedString ins = new AttributedString(String.format("|%s|", Util.repeat(" ", w - 2)));
        canvas.printAt(x, y, tbs);
        for (int i = 1; i < h - 1; i++) {
            canvas.printAt(x, y + i, ins);
        }
        canvas.printAt(x, y + h - 1, tbs);
    }
}
