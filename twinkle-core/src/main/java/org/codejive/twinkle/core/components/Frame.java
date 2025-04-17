package org.codejive.twinkle.core.components;

import org.codejive.twinkle.core.component.Canvas;
import org.codejive.twinkle.core.component.Component;
import org.codejive.twinkle.core.component.Panel;
import org.codejive.twinkle.core.component.Size;
import org.jspecify.annotations.NonNull;

public class Frame implements Component {
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
