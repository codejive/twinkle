package org.codejive.twinkle.fluent.commands;

import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.fluent.Fluent;

public interface ColorCommands {
    /**
     * Applies a black background color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.BLACK}.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent black() {
        return color(Color.BasicColor.BLACK);
    }

    /**
     * Applies a red background color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.RED}.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent red() {
        return color(Color.BasicColor.RED);
    }

    /**
     * Applies a green background color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.GREEN}.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent green() {
        return color(Color.BasicColor.GREEN);
    }

    /**
     * Applies a yellow background color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.YELLOW}.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent yellow() {
        return color(Color.BasicColor.YELLOW);
    }

    /**
     * Applies a blue background color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.BLUE}.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent blue() {
        return color(Color.BasicColor.BLUE);
    }

    /**
     * Applies a magenta background color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.MAGENTA}.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent magenta() {
        return color(Color.BasicColor.MAGENTA);
    }

    /**
     * Applies a cyan background color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.CYAN}.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent cyan() {
        return color(Color.BasicColor.CYAN);
    }

    /**
     * Applies a white background color. Alias for {@link #color(Color)} with {@code
     * Color.BasicColor.WHITE}.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent white() {
        return color(Color.BasicColor.WHITE);
    }

    /**
     * Applies the given background color.
     *
     * @param color the color to apply
     * @return this Fluent instance for chaining
     */
    Fluent color(Color color);

    /**
     * Applies the given indexed background color.
     *
     * @param idx the index of the color to apply (0-255)
     * @return this Fluent instance for chaining
     */
    Fluent color(int idx);

    /**
     * Applies the given RGB background color.
     *
     * @param r the red component (0-255)
     * @param g the green component (0-255)
     * @param b the blue component (0-255)
     * @return this Fluent instance for chaining
     */
    Fluent color(int r, int g, int b);
}
