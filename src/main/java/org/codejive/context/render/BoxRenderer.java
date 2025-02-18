package org.codejive.context.render;

import org.codejive.context.terminal.Canvas;
import org.jline.utils.AttributedString;

public class BoxRenderer {
    private final Canvas canvas;

    public BoxRenderer(Canvas canvas) {
        this.canvas = canvas;
    }

    public void render(Box box) {
        if (box.rect().outside(canvas.rect())) {
            return;
        }
        int x = box.left();
        int y = box.top();
        int i = 0;
        for (AttributedString str : box.content()) {
            canvas.printAt(x, y + i, str);
            i++;
        }
    }
}
