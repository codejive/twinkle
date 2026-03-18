package org.codejive.twinkle.image.util;

public class Resolution implements Comparable<Resolution> {
    public final int x;
    public final int y;

    public Resolution(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Resolution other) {
        return Integer.compare(this.x * this.y, other.x * other.y);
    }
}
