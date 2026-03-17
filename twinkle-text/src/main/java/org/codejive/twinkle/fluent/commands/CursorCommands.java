package org.codejive.twinkle.fluent.commands;

import org.codejive.twinkle.fluent.Fluent;

public interface CursorCommands {
    /**
     * Positions the cursor at the home position (top-left corner, coordinates 0,0).
     *
     * @return this Fluent instance for chaining
     */
    Fluent home();

    /**
     * Positions the cursor at the specified column (x) and row (y). Coordinates are 0-based (the
     * top-left corner is 0,0).
     *
     * @param x the column (0-based, 0 is leftmost)
     * @param y the row (0-based, 0 is topmost)
     * @return this Fluent instance for chaining
     */
    Fluent at(int x, int y);

    /**
     * Moves the cursor up by one row. Alias for {@link #up(int)} with n=1.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent up() {
        return up(1);
    }

    /**
     * Moves the cursor up by the specified number of rows.
     *
     * @param n the number of rows to move up
     * @return this Fluent instance for chaining
     */
    Fluent up(int n);

    /**
     * Moves the cursor down by one row. Alias for {@link #down(int)} with n=1.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent down() {
        return down(1);
    }

    /**
     * Moves the cursor down by the specified number of rows.
     *
     * @param n the number of rows to move down
     * @return this Fluent instance for chaining
     */
    Fluent down(int n);

    /**
     * Moves the cursor right by one column. Alias for {@link #forward()}.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent right() {
        return forward(1);
    }

    /**
     * Moves the cursor right by the specified number of columns.
     *
     * @param n the number of columns to move right
     * @return this Fluent instance for chaining
     */
    default Fluent right(int n) {
        return forward(n);
    }

    /**
     * Moves the cursor forward by one column. Alias for {@link #forward(int)} with n=1.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent forward() {
        return forward(1);
    }

    /**
     * Moves the cursor forward by the specified number of columns.
     *
     * @param n the number of columns to move forward
     * @return this Fluent instance for chaining
     */
    Fluent forward(int n);

    /**
     * Moves the cursor left by one column. Alias for {@link #backward()}.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent left() {
        return backward(1);
    }

    /**
     * Moves the cursor left by the specified number of columns. Alias for {@link #backward(int)}.
     *
     * @param n the number of columns to move left
     * @return this Fluent instance for chaining
     */
    default Fluent left(int n) {
        return backward(n);
    }

    /**
     * Moves the cursor backward by one column. Alias for {@link #backward(int)} with n=1.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent backward() {
        return backward(1);
    }

    /**
     * Moves the cursor backward by the specified number of columns.
     *
     * @param n the number of columns to move backward
     * @return this Fluent instance for chaining
     */
    Fluent backward(int n);

    /**
     * Moves the cursor to the beginning of the next line. Alias for {@link #next(int)} with n=1.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent next() {
        return next(1);
    }

    /**
     * Moves the cursor to the beginning of the n-th line down.
     *
     * @param n the number of lines to move down
     * @return this Fluent instance for chaining
     */
    Fluent next(int n);

    /**
     * Moves the cursor to the beginning of the previous line. Alias for {@link #prev(int)} with
     * n=1.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent prev() {
        return prev(1);
    }

    /**
     * Moves the cursor to the beginning of the n-th line up.
     *
     * @param n the number of lines to move up
     * @return this Fluent instance for chaining
     */
    Fluent prev(int n);

    /**
     * Positions the cursor at the specified column on the current row. The column is 0-based (0 is
     * leftmost). Alias for {@link #column(int)}.
     *
     * @param column the column (0-based, 0 is leftmost)
     * @return this Fluent instance for chaining
     */
    default Fluent col(int column) {
        return column(column);
    }

    /**
     * Positions the cursor at the specified column on the current row. The column is 0-based (0 is
     * leftmost).
     *
     * @param column the column (0-based, 0 is leftmost)
     * @return this Fluent instance for chaining
     */
    Fluent column(int column);

    /**
     * Hides the cursor.
     *
     * @return this Fluent instance for chaining
     */
    Fluent hide();

    /**
     * Shows the cursor.
     *
     * @return this Fluent instance for chaining
     */
    Fluent show();

    /**
     * Marks the current cursor position.
     *
     * @return this Fluent instance for chaining
     */
    Fluent mark();

    /**
     * Jumps to the previously marked cursor position.
     *
     * @return this Fluent instance for chaining
     */
    Fluent jump();
}
