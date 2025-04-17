package org.codejive.twinkle.components.graphs.bar;

public class FracBarConfig extends BarConfig {
    private Design design;

    public enum Design {
        TEXT_BLOCK,
        COLOR_BLOCK,
        FULL_BLOCK,
        HALF_BLOCK,
        FRACTIONAL_BLOCK;
    }

    public FracBarConfig() {
        this.design = Design.FRACTIONAL_BLOCK;
    }

    @Override
    public FracBarConfig minValue(Number minValue) {
        return (FracBarConfig) super.minValue(minValue);
    }

    @Override
    public FracBarConfig maxValue(Number maxValue) {
        return (FracBarConfig) super.maxValue(maxValue);
    }

    @Override
    public FracBarConfig direction(Direction direction) {
        return (FracBarConfig) super.direction(direction);
    }

    public Design design() {
        return design;
    }

    public FracBarConfig design(Design design) {
        this.design = design;
        return this;
    }

    public FracBarConfig copy() {
        return copy_(new FracBarConfig());
    }

    protected FracBarConfig copy_(FracBarConfig b) {
        super.copy_(b);
        b.design = this.design;
        return b;
    }

    public static FracBarConfig create() {
        return new FracBarConfig();
    }
}
