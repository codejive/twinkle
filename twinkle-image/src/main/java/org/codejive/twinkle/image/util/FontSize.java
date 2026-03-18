package org.codejive.twinkle.image.util;

public class FontSize {

    // Common monospace font size (width x height in pixels)
    private static Resolution defaultFontSize = new Resolution(8, 16);

    public static Resolution defaultFontSize() {
        return defaultFontSize;
    }

    public static void defaultFontSize(Resolution newFontSize) {
        defaultFontSize = newFontSize;
    }

    private FontSize() {
        // Private constructor to prevent instantiation
    }
}
