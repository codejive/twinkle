package org.codejive.twinkle.widgets;

import org.codejive.twinkle.core.widget.Canvas;
import org.codejive.twinkle.core.widget.Panel;
import org.codejive.twinkle.core.widget.Size;
import org.codejive.twinkle.core.widget.Widget;
import org.jspecify.annotations.NonNull;

public class Frame implements Widget {
    private final Canvas canvas;
    private final Canvas innerCanvas;

    public static Frame of(Size size) {
        return new Frame(Panel.of(size));
    }

    public static Frame of(Canvas canvas) {
        return new Frame(canvas);
    }

    public Frame(Canvas canvas) {
        this.canvas = canvas;
        if (canvas.size().width() < 2 || canvas.size().height() < 2) {
            this.innerCanvas = canvas;
        } else {
            this.innerCanvas =
                    canvas.view(1, 1, canvas.size().width() - 2, canvas.size().height() - 2);
        }
    }

    @Override
    public @NonNull Size size() {
        return canvas.size();
    }

    @Override
    public void render() {}
}
