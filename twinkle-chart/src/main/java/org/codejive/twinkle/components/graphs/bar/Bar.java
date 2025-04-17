package org.codejive.twinkle.components.graphs.bar;

import org.codejive.twinkle.core.component.Canvas;
import org.codejive.twinkle.core.component.Renderable;

public class Bar implements Renderable {
    private final FracBarRenderer renderer;
    private Number value = 0.0d;

    /**
     * Returns a fractional horizontal Bar representing values between 0 and 100.
     *
     * @return A Bar instance
     */
    public static Bar bar() {
        return new Bar(FracBarConfig.create());
    }

    public Bar(FracBarConfig config) {
        this.renderer = new FracBarRenderer(config);
    }

    public Bar setValue(Number value) {
        this.value = value;
        return this;
    }

    public void render(Canvas canvas) {
        renderer.render(canvas, value);
    }
}
