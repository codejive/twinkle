package org.codejive.twinkle.widgets.graphs.bar;

public class BarConfig {
    private Number minValue;
    private Number maxValue;
    private Direction direction;

    public enum Orientation {
        HORIZONTAL,
        VERTICAL;
    }

    public enum Direction {
        L2R(Orientation.HORIZONTAL, 1, 0),
        R2L(Orientation.HORIZONTAL, -1, 0),
        B2T(Orientation.VERTICAL, 0, -1),
        T2B(Orientation.VERTICAL, 0, 1);

        public final Orientation orientation;
        public final int dx;
        public final int dy;

        Direction(Orientation orientation, int dx, int dy) {
            this.orientation = orientation;
            this.dx = dx;
            this.dy = dy;
        }
    }

    public BarConfig() {
        this.minValue = 0;
        this.maxValue = 100;
        this.direction = Direction.L2R;
    }

    public Number minValue() {
        return minValue;
    }

    public BarConfig minValue(Number minValue) {
        this.minValue = minValue;
        return this;
    }

    public Number maxValue() {
        return maxValue;
    }

    public BarConfig maxValue(Number maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public Direction direction() {
        return direction;
    }

    public BarConfig direction(Direction direction) {
        this.direction = direction;
        return this;
    }

    public BarConfig copy() {
        return copy_(new BarConfig());
    }

    protected BarConfig copy_(BarConfig b) {
        b.minValue = this.minValue;
        b.maxValue = this.maxValue;
        b.direction = this.direction;
        return b;
    }

    public static BarConfig create() {
        return new BarConfig();
    }
}
