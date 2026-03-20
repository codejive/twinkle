package org.codejive.twinkle.image.util;

public class FontSize {
    public final int widthInPixels;
    public final int heightInPixels;

    // Common monospace font size (width x height in pixels)
    private static FontSize defaultFontSize = new FontSize(8, 16);

    public FontSize(int widthInPixels, int heightInPixels) {
        this.widthInPixels = widthInPixels;
        this.heightInPixels = heightInPixels;
    }

    public static FontSize defaultFontSize() {
        return defaultFontSize;
    }

    public static void defaultFontSize(FontSize newFontSize) {
        defaultFontSize = newFontSize;
    }
}
