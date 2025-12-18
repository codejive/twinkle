package org.codejive.twinkle.widgets.graphs.plot;

import java.util.function.Function;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.widget.Canvas;
import org.codejive.twinkle.core.widget.Size;
import org.codejive.twinkle.core.widget.Widget;
import org.jspecify.annotations.NonNull;

public class MathPlot implements Widget {
    private final Plot plot;
    private Origin origin;
    private Number minXValue;
    private Number maxXValue;
    private Number minYValue;
    private Number maxYValue;
    private double originX;
    private double originY;

    public enum Origin {
        N,
        S,
        E,
        W,
        NE,
        NW,
        SE,
        SW,
        CENTER
    }

    public static MathPlot of(Size size) {
        return new MathPlot(Plot.of(size));
    }

    public static MathPlot of(Canvas canvas) {
        return new MathPlot(Plot.of(canvas));
    }

    public static MathPlot of(Plot plot) {
        return new MathPlot(plot);
    }

    protected MathPlot(Plot plot) {
        this.plot = plot;
        ranges(-1.0d, 1.0d, -1.0d, 1.0d);
        origin(Origin.CENTER);
    }

    @Override
    public @NonNull Size size() {
        return plot.size();
    }

    public Origin origin() {
        return origin;
    }

    public MathPlot origin(Origin origin) {
        this.origin = origin;
        int width = plot.plotSize().width();
        int height = plot.plotSize().height();
        switch (origin) {
            case N:
                originX = (width - 1) / 2.0;
                originY = height - 1;
                break;
            case S:
                originX = (width - 1) / 2.0;
                originY = 0.0;
                break;
            case E:
                originX = width - 1;
                originY = (height - 1) / 2.0;
                break;
            case W:
                originX = 0.0;
                originY = (height - 1) / 2.0;
                break;
            case NE:
                originX = width - 1;
                originY = height - 1;
                break;
            case NW:
                originX = 0.0;
                originY = height - 1;
                break;
            case SE:
                originX = width - 1;
                originY = 0.0;
                break;
            case SW:
                originX = 0.0;
                originY = 0.0;
                break;
            case CENTER:
            default:
                originX = (width - 1) / 2.0;
                originY = (height - 1) / 2.0;
                break;
        }
        return this;
    }

    public Number getMinXValue() {
        return minXValue;
    }

    public MathPlot minXValue(Number minXValue) {
        this.minXValue = minXValue;
        return this;
    }

    public Number maxXValue() {
        return maxXValue;
    }

    public MathPlot maxXValue(Number maxXValue) {
        this.maxXValue = maxXValue;
        return this;
    }

    public MathPlot xRange(Number minXValue, Number maxXValue) {
        this.minXValue = minXValue;
        this.maxXValue = maxXValue;
        return this;
    }

    public Number minYValue() {
        return minYValue;
    }

    public MathPlot minYValue(Number minYValue) {
        this.minYValue = minYValue;
        return this;
    }

    public Number maxYValue() {
        return maxYValue;
    }

    public MathPlot maxYValue(Number maxYValue) {
        this.maxYValue = maxYValue;
        return this;
    }

    public MathPlot yRange(Number minYValue, Number maxYValue) {
        this.minYValue = minYValue;
        this.maxYValue = maxYValue;
        return this;
    }

    public MathPlot ranges(Number minXValue, Number maxXValue, Number minYValue, Number maxYValue) {
        this.minXValue = minXValue;
        this.maxXValue = maxXValue;
        this.minYValue = minYValue;
        this.maxYValue = maxYValue;
        return this;
    }

    public @NonNull Style currentStyle() {
        return plot.currentStyle();
    }

    public long currentStyleState() {
        return plot.currentStyleState();
    }

    public MathPlot currentStyle(Style currentStyle) {
        plot.currentStyle(currentStyle);
        return this;
    }

    public MathPlot currentStyleState(long currentStyleState) {
        plot.currentStyleState(currentStyleState);
        return this;
    }

    public MathPlot plot(Function<Double, Double> func) {
        double xRange = maxXValue.doubleValue() - minXValue.doubleValue();
        double yRange = maxYValue.doubleValue() - minYValue.doubleValue();
        Size plotSize = plot.plotSize();
        int width = plotSize.width();
        int height = plotSize.height();
        if (width <= 0 || height <= 0 || xRange <= 0.0 || yRange <= 0.0) {
            return this;
        }

        // Precompute default positions for zero in the numeric range (where numeric 0 maps in
        // default mapping).
        double defaultZeroX = (0.0 - minXValue.doubleValue()) / xRange * (width - 1);
        double defaultZeroY = (0.0 - minYValue.doubleValue()) / yRange * (height - 1);

        double deltaX = originX - defaultZeroX;
        double deltaY = originY - defaultZeroY;

        boolean prevValid = false;
        int prevX = 0;
        int prevY = 0;

        for (int xi = 0; xi < width; xi++) {
            // map pixel x to numeric x in [minX, maxX]
            double tX = (width == 1) ? 0.0 : (double) xi / (width - 1);
            double scaledX = minXValue.doubleValue() + tX * xRange;

            double scaledY = func.apply(scaledX);
            if (Double.isNaN(scaledY) || Double.isInfinite(scaledY)) {
                prevValid = false;
                continue;
            }

            // map numeric y to pixel y in default mapping (minY -> 0, maxY -> height-1)
            double defaultPixelX = (scaledX - minXValue.doubleValue()) / xRange * (width - 1);
            double defaultPixelY = (scaledY - minYValue.doubleValue()) / yRange * (height - 1);

            // apply origin shift so numeric zero lands where requested
            int px = (int) Math.round(defaultPixelX + deltaX);
            int py = (int) Math.round(defaultPixelY + deltaY);

            boolean currValid = !(px < 0 || px >= width || py < 0 || py >= height);

            if (prevValid && currValid) {
                // draw line between prev and current using Bresenham
                int x0 = prevX;
                int y0 = prevY;
                int x1 = px;
                int y1 = py;
                int dx = Math.abs(x1 - x0);
                int sx = x0 < x1 ? 1 : -1;
                int dy = Math.abs(y1 - y0);
                int sy = y0 < y1 ? 1 : -1;
                int err = dx - dy;

                while (true) {
                    if (x0 >= 0 && x0 < width && y0 >= 0 && y0 < height) {
                        plot.plot(x0, y0);
                    }
                    if (x0 == x1 && y0 == y1) {
                        break;
                    }
                    int e2 = 2 * err;
                    if (e2 > -dy) {
                        err -= dy;
                        x0 += sx;
                    }
                    if (e2 < dx) {
                        err += dx;
                        y0 += sy;
                    }
                }
                prevX = px;
                prevY = py;
            } else if (currValid) {
                // start new segment (or single point) and record as previous
                plot.plot(px, py);
                prevX = px;
                prevY = py;
                prevValid = true;
            } else {
                // current invalid -> break continuity
                prevValid = false;
            }
        }
        return this;
    }

    @Override
    public void render(Canvas canvas) {
        plot.render(canvas);
    }
}
