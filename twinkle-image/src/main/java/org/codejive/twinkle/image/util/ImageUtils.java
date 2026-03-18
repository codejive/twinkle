package org.codejive.twinkle.image.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import org.jspecify.annotations.NonNull;

/** Utility class for common image operations used by terminal image encoders. */
public class ImageUtils {

    private ImageUtils() {
        // Utility class, prevent instantiation
    }

    /**
     * Scales an image to fit within the specified dimensions while maintaining aspect ratio.
     *
     * <p>The image will be scaled to fit completely within the target dimensions. If the aspect
     * ratio of the source image differs from the target dimensions, the resulting image will be
     * smaller in one dimension to preserve the aspect ratio.
     *
     * @param source the source image to scale
     * @param targetWidth the maximum target width in pixels
     * @param targetHeight the maximum target height in pixels
     * @return the scaled image with preserved aspect ratio
     * @throws IllegalArgumentException if source is null or dimensions are invalid
     */
    public static @NonNull BufferedImage scaleImage(
            @NonNull BufferedImage source, int targetWidth, int targetHeight) {
        return scaleImage(source, targetWidth, targetHeight, false);
    }

    /**
     * Scales an image to the specified dimensions.
     *
     * @param source the source image to scale
     * @param targetWidth the target width in pixels
     * @param targetHeight the target height in pixels
     * @param fitImage if true, scale the image to fit the target dimensions exactly (stretching if
     *     needed); if false, preserve aspect ratio
     * @return the scaled image
     * @throws IllegalArgumentException if source is null or dimensions are invalid
     */
    public static @NonNull BufferedImage scaleImage(
            @NonNull BufferedImage source, int targetWidth, int targetHeight, boolean fitImage) {

        if (source == null) {
            throw new IllegalArgumentException("Source image cannot be null");
        }
        if (targetWidth <= 0 || targetHeight <= 0) {
            throw new IllegalArgumentException(
                    "Target dimensions must be positive: " + targetWidth + "x" + targetHeight);
        }

        int scaledWidth;
        int scaledHeight;

        if (fitImage) {
            // Fit the image to the exact target dimensions (may stretch)
            scaledWidth = targetWidth;
            scaledHeight = targetHeight;
        } else {
            // Calculate scaling to fit within target dimensions while preserving aspect ratio
            double scaleX = (double) targetWidth / source.getWidth();
            double scaleY = (double) targetHeight / source.getHeight();
            double scale = Math.min(scaleX, scaleY);

            scaledWidth = (int) Math.round(source.getWidth() * scale);
            scaledHeight = (int) Math.round(source.getHeight() * scale);

            // Ensure dimensions are at least 1x1
            scaledWidth = Math.max(1, scaledWidth);
            scaledHeight = Math.max(1, scaledHeight);
        }

        BufferedImage scaled =
                new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.drawImage(
                source.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH),
                0,
                0,
                null);
        g.dispose();

        return scaled;
    }
}
