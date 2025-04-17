package org.codejive.twinkle.components.graphs.bar;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.components.graphs.bar.BarConfig.*;
import org.codejive.twinkle.components.graphs.bar.FracBarConfig.*;
import org.codejive.twinkle.core.component.Canvas;
import org.codejive.twinkle.core.component.Size;

public class FracBarRenderer {
    private final FracBarConfig config;

    private static final char[] TEXT_BLOCK = {'#'};
    private static final char[] COLOR_BLOCK = {' '};
    private static final char[] BLOCK_FULL = {'\u2588'};
    private static final char[] BLOCK_HALF_L2R = {'\u2588', '\u258c'}; // full, left half
    private static final char[] BLOCK_HALF_R2L = {'\u2588', '\u2590'}; // full, right half
    private static final char[] BLOCK_HALF_T2B = {'\u2588', '\u2580'}; // full, top half
    private static final char[] BLOCK_HALF_B2T = {'\u2588', '\u2584'}; // full, bottom half
    private static final char[] BLOCK_FRAC_L2R = {
        '\u2588', '\u258f', '\u258e', '\u258d', '\u258c', '\u258b', '\u258a', '\u2589'
    }; // full, left 1/8 .. 7/8
    private static final char[] BLOCK_FRAC_B2T = {
        '\u2588', '\u2581', '\u2582', '\u2583', '\u2584', '\u2585', '\u2586', '\u2587'
    }; // full, lower 1/8 .. 7/8
    private static final char BLOCK_OVERFLOW = '\u2593'; // dark shade block

    public FracBarRenderer() {
        this(FracBarConfig.create());
    }

    public FracBarRenderer(FracBarConfig config) {
        this.config = config;
    }

    public void render(Canvas canvas, Number value) {
        FracBarConfig.Design activeDesign = config.design();
        if (activeDesign == Design.FRACTIONAL_BLOCK
                && (config.direction() == Direction.R2L || config.direction() == Direction.T2B)) {
            // Fallback to half block design for unsupported directions
            activeDesign = Design.HALF_BLOCK;
        }

        double dx = value.doubleValue();
        double dmin = config.minValue().doubleValue();
        double dmax = config.maxValue().doubleValue();

        if (dmin > dmax) {
            throw new IllegalArgumentException("Minimum value greater than maximum value");
        }
        // Clamp value to minimum
        dx = Math.max(dmin, dx);

        Direction direction = config.direction();

        char[] blocks;
        switch (activeDesign) {
            case TEXT_BLOCK:
                blocks = TEXT_BLOCK;
                break;
            case COLOR_BLOCK:
                blocks = COLOR_BLOCK;
                break;
            case FULL_BLOCK:
                blocks = BLOCK_FULL;
                break;
            case HALF_BLOCK:
                switch (direction) {
                    case L2R:
                        blocks = BLOCK_HALF_L2R;
                        break;
                    case R2L:
                        blocks = BLOCK_HALF_R2L;
                        break;
                    case B2T:
                        blocks = BLOCK_HALF_B2T;
                        break;
                    case T2B:
                        blocks = BLOCK_HALF_T2B;
                        break;
                    default:
                        throw new IllegalStateException("Unknown direction: " + direction);
                }
                break;
            case FRACTIONAL_BLOCK:
                switch (direction) {
                    case L2R:
                        blocks = BLOCK_FRAC_L2R;
                        break;
                    case B2T:
                        blocks = BLOCK_FRAC_B2T;
                        break;
                    case R2L:
                    case T2B:
                        // We shouldn't get here because we fall back to half blocks in these cases
                        throw new IllegalStateException(
                                "Unsupported direction: "
                                        + direction
                                        + " for design: "
                                        + activeDesign);
                    default:
                        throw new IllegalStateException("Unknown direction: " + direction);
                }
                break;
            default:
                throw new IllegalStateException("Unknown design: " + activeDesign);
        }

        Size size = canvas.size();
        boolean reversed = direction == Direction.R2L || direction == Direction.T2B;
        boolean horizontal = direction.orientation == Orientation.HORIZONTAL;
        int maxSize = horizontal ? size.width() : size.height();
        int nroBlocksPerChar = blocks.length;
        int maxSizeInFractions = maxSize * nroBlocksPerChar;
        double interval = dmax - dmin;
        int barWidthInFractions = (int) (((dx - dmin) / interval) * maxSizeInFractions);
        boolean overflow = barWidthInFractions > maxSizeInFractions;
        int fullChunks = overflow ? maxSize - 1 : barWidthInFractions / nroBlocksPerChar;
        int remainder = overflow ? 0 : barWidthInFractions % nroBlocksPerChar;

        int x = !reversed && horizontal ? 0 : size.width() - 1;
        int y = reversed || horizontal ? 0 : size.height() - 1;
        // Place full blocks first
        for (int i = 0; i < fullChunks; i++) {
            canvas.setCharAt(x, y, Style.F_UNSTYLED, blocks[0]);
            x += direction.dx;
            y += direction.dy;
        }
        // Append remainder partial block if any
        if (remainder > 0) {
            canvas.setCharAt(x, y, Style.F_UNSTYLED, blocks[remainder]);
            x += direction.dx;
            y += direction.dy;
        } else if (overflow) { // Or an overflow block
            canvas.setCharAt(x, y, Style.F_UNSTYLED, BLOCK_OVERFLOW);
            x += direction.dx;
            y += direction.dy;
        }
        // Fill the rest with spaces
        int sizeLeft = maxSize - fullChunks - (overflow || remainder > 0 ? 1 : 0);
        for (int i = 0; i < sizeLeft; i++) {
            canvas.setCharAt(x, y, Style.F_UNSTYLED, ' ');
            x += direction.dx;
            y += direction.dy;
        }
    }
}
