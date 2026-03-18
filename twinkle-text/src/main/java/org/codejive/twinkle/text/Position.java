package org.codejive.twinkle.text;

import java.util.Objects;
import org.jspecify.annotations.NonNull;

public class Position {
    private final int x;
    private final int y;

    public static Position ZERO = new Position(0, 0);
    public static Position MAX = new Position(Integer.MAX_VALUE, Integer.MAX_VALUE);

    public static @NonNull Position of(int x, int y) {
        if (x == 0 && y == 0) {
            return ZERO;
        }
        if (x == Integer.MAX_VALUE && y == Integer.MAX_VALUE) {
            return MAX;
        }
        return new Position(x, y);
    }

    protected Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    /**
     * Returns a new position moved by the given amounts in x and y direction.
     *
     * @param dx the distance to move in the X direction
     * @param dy the distance to move in the Y direction
     * @return a new position with the updated coordinates
     */
    public Position move(int dx, int dy) {
        return of(x + dx, y + dy);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Position pos = (Position) o;
        return x == pos.x && y == pos.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
