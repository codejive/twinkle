package org.codejive.twinkle.image.util;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;

/**
 * Utility class for color quantization of images.
 *
 * <p>This class provides methods to reduce the color palette of an image to a specified number of
 * colors using median cut algorithm. This is useful for encoders like Sixel that have a limited
 * color palette (typically 256 colors).
 */
public class ColorQuantizer {

    private ColorQuantizer() {
        // Utility class, prevent instantiation
    }

    /** Result of color quantization containing the palette and indexed pixel data. */
    public static class QuantizedImage {
        private final @NonNull List<Integer> palette;
        private final int @NonNull [][] indexedPixels;

        /**
         * Creates a new quantized image result.
         *
         * @param palette the color palette (list of RGB colors)
         * @param indexedPixels 2D array of palette indices for each pixel
         */
        public QuantizedImage(@NonNull List<Integer> palette, int @NonNull [][] indexedPixels) {
            this.palette = palette;
            this.indexedPixels = indexedPixels;
        }

        /**
         * Gets the color palette.
         *
         * @return the palette
         */
        public @NonNull List<Integer> palette() {
            return palette;
        }

        /**
         * Gets the indexed pixel data.
         *
         * @return the indexed pixels
         */
        public int @NonNull [][] indexedPixels() {
            return indexedPixels;
        }
    }

    /**
     * Quantizes an image to a maximum number of colors using median cut algorithm.
     *
     * @param image the image to quantize
     * @param maxColors the maximum number of colors in the palette (typically 256 for Sixel)
     * @return the quantized image with palette and indexed pixels
     * @throws IllegalArgumentException if image is null or maxColors is invalid
     */
    public static @NonNull QuantizedImage quantize(@NonNull BufferedImage image, int maxColors) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        if (maxColors < 2 || maxColors > 256) {
            throw new IllegalArgumentException(
                    "Max colors must be between 2 and 256, got: " + maxColors);
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // Collect all unique colors from the image
        Map<Integer, Integer> colorCounts = new HashMap<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y) & 0xFFFFFF; // Mask out alpha
                colorCounts.merge(rgb, 1, Integer::sum);
            }
        }

        // If the image already has fewer colors than maxColors, use them directly
        List<Integer> palette;
        if (colorCounts.size() <= maxColors) {
            palette = new ArrayList<>(colorCounts.keySet());
        } else {
            // Use median cut algorithm to reduce colors
            palette = medianCut(new ArrayList<>(colorCounts.keySet()), maxColors);
        }

        // Build index map for fast lookup
        Map<Integer, Integer> colorToIndex = new HashMap<>();
        for (int i = 0; i < palette.size(); i++) {
            colorToIndex.put(palette.get(i), i);
        }

        // Create indexed pixel array
        int[][] indexedPixels = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y) & 0xFFFFFF;
                Integer index = colorToIndex.get(rgb);
                if (index == null) {
                    // Find nearest color in palette
                    index = findNearestColor(rgb, palette);
                }
                indexedPixels[y][x] = index;
            }
        }

        return new QuantizedImage(palette, indexedPixels);
    }

    /**
     * Performs median cut algorithm on a list of colors.
     *
     * @param colors the list of colors to quantize
     * @param maxColors the target number of colors
     * @return the quantized palette
     */
    private static @NonNull List<Integer> medianCut(@NonNull List<Integer> colors, int maxColors) {
        // Start with one bucket containing all colors
        List<ColorBucket> buckets = new ArrayList<>();
        buckets.add(new ColorBucket(colors));

        // Repeatedly split the bucket with the largest range until we have maxColors buckets
        while (buckets.size() < maxColors) {
            // Find bucket with largest range
            ColorBucket largest = null;
            int largestRange = -1;
            for (ColorBucket bucket : buckets) {
                int range = bucket.getRange();
                if (range > largestRange) {
                    largestRange = range;
                    largest = bucket;
                }
            }

            if (largest == null || largestRange == 0) {
                break; // Cannot split further
            }

            // Split the bucket
            buckets.remove(largest);
            ColorBucket[] split = largest.split();
            buckets.add(split[0]);
            buckets.add(split[1]);
        }

        // Get average color from each bucket
        List<Integer> palette = new ArrayList<>();
        for (ColorBucket bucket : buckets) {
            palette.add(bucket.getAverageColor());
        }

        return palette;
    }

    /**
     * Finds the index of the nearest color in the palette.
     *
     * @param rgb the target color
     * @param palette the color palette
     * @return the index of the nearest color
     */
    private static int findNearestColor(int rgb, @NonNull List<Integer> palette) {
        int r1 = (rgb >> 16) & 0xFF;
        int g1 = (rgb >> 8) & 0xFF;
        int b1 = rgb & 0xFF;

        int nearestIndex = 0;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < palette.size(); i++) {
            int paletteColor = palette.get(i);
            int r2 = (paletteColor >> 16) & 0xFF;
            int g2 = (paletteColor >> 8) & 0xFF;
            int b2 = paletteColor & 0xFF;

            // Euclidean distance in RGB space
            int dr = r1 - r2;
            int dg = g1 - g2;
            int db = b1 - b2;
            int distance = dr * dr + dg * dg + db * db;

            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }

        return nearestIndex;
    }

    /** A bucket of colors for the median cut algorithm. */
    private static class ColorBucket {
        private final List<Integer> colors;

        ColorBucket(List<Integer> colors) {
            this.colors = colors;
        }

        /**
         * Gets the range of this bucket (max range across R, G, B channels).
         *
         * @return the range
         */
        int getRange() {
            if (colors.isEmpty()) {
                return 0;
            }

            int minR = 255, maxR = 0;
            int minG = 255, maxG = 0;
            int minB = 255, maxB = 0;

            for (int color : colors) {
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;

                minR = Math.min(minR, r);
                maxR = Math.max(maxR, r);
                minG = Math.min(minG, g);
                maxG = Math.max(maxG, g);
                minB = Math.min(minB, b);
                maxB = Math.max(maxB, b);
            }

            int rangeR = maxR - minR;
            int rangeG = maxG - minG;
            int rangeB = maxB - minB;

            return Math.max(rangeR, Math.max(rangeG, rangeB));
        }

        /**
         * Splits this bucket into two buckets by median cut on the channel with largest range.
         *
         * @return array of two buckets
         */
        ColorBucket[] split() {
            if (colors.size() < 2) {
                return new ColorBucket[] {this, new ColorBucket(new ArrayList<>())};
            }

            // Find channel with largest range
            int minR = 255, maxR = 0;
            int minG = 255, maxG = 0;
            int minB = 255, maxB = 0;

            for (int color : colors) {
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;

                minR = Math.min(minR, r);
                maxR = Math.max(maxR, r);
                minG = Math.min(minG, g);
                maxG = Math.max(maxG, g);
                minB = Math.min(minB, b);
                maxB = Math.max(maxB, b);
            }

            int rangeR = maxR - minR;
            int rangeG = maxG - minG;
            int rangeB = maxB - minB;

            // Determine which channel to split on
            final int channel; // 0=R, 1=G, 2=B
            if (rangeR >= rangeG && rangeR >= rangeB) {
                channel = 0;
            } else if (rangeG >= rangeB) {
                channel = 1;
            } else {
                channel = 2;
            }

            // Sort colors by the selected channel
            colors.sort(
                    (c1, c2) -> {
                        int v1 = (c1 >> (16 - channel * 8)) & 0xFF;
                        int v2 = (c2 >> (16 - channel * 8)) & 0xFF;
                        return Integer.compare(v1, v2);
                    });

            // Split at median
            int median = colors.size() / 2;
            List<Integer> left = new ArrayList<>(colors.subList(0, median));
            List<Integer> right = new ArrayList<>(colors.subList(median, colors.size()));

            return new ColorBucket[] {new ColorBucket(left), new ColorBucket(right)};
        }

        /**
         * Gets the average color of all colors in this bucket.
         *
         * @return the average color as RGB integer
         */
        int getAverageColor() {
            if (colors.isEmpty()) {
                return 0;
            }

            long sumR = 0, sumG = 0, sumB = 0;
            for (int color : colors) {
                sumR += (color >> 16) & 0xFF;
                sumG += (color >> 8) & 0xFF;
                sumB += color & 0xFF;
            }

            int avgR = (int) (sumR / colors.size());
            int avgG = (int) (sumG / colors.size());
            int avgB = (int) (sumB / colors.size());

            return (avgR << 16) | (avgG << 8) | avgB;
        }
    }
}
