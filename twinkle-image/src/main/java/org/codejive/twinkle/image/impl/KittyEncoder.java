package org.codejive.twinkle.image.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.codejive.twinkle.image.ImageEncoder;
import org.codejive.twinkle.image.util.AnsiUtils;
import org.codejive.twinkle.image.util.FontSize;
import org.codejive.twinkle.image.util.ImageUtils;
import org.jspecify.annotations.NonNull;

/**
 * Implementation of the Kitty terminal graphics encoding format.
 *
 * <p>The Kitty graphics encoding format is a modern, efficient format developed for the Kitty
 * terminal emulator. It supports direct transmission of PNG images encoded in base64, with various
 * sophisticated features like image IDs, placements, and more.
 *
 * <p>Format: ESC _G<control-data>;base64-data ESC \
 *
 * <p>This encoder is stateful: the image and font size are set at construction time and are
 * immutable, while the target size and fit mode can be changed via setters. Expensive
 * transformations like image scaling and PNG encoding are performed lazily on the first call to
 * {@link #render(Appendable)} and cached for subsequent calls.
 *
 * @see <a href="https://sw.kovidgoyal.net/kitty/graphics-protocol/">Kitty Graphics Protocol</a>
 */
public class KittyEncoder implements ImageEncoder {
    // Immutable state
    private final @NonNull BufferedImage image;

    // Mutable state
    private int targetWidth;
    private int targetHeight;
    private boolean fitImage;

    // Cached transformations
    private BufferedImage scaledImage;
    private String base64Data;

    private static final String APC = AnsiUtils.ESC + "_"; // Application Program Command
    private static final char GRAPHICS_CMD = 'G';

    /**
     * Creates a new Kitty encoder for the given image and font size.
     *
     * @param image the image to encode
     * @param targetWidth the initial target width in terminal columns
     * @param targetHeight the initial target height in terminal rows
     * @param fitImage the initial fit mode
     */
    public KittyEncoder(
            @NonNull BufferedImage image, int targetWidth, int targetHeight, boolean fitImage) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        if (targetWidth <= 0 || targetHeight <= 0) {
            throw new IllegalArgumentException("Target size must be positive");
        }
        this.image = image;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.fitImage = fitImage;
    }

    @Override
    public @NonNull String name() {
        return "kitty";
    }

    @Override
    public int targetWidth() {
        return targetWidth;
    }

    @Override
    public int targetHeight() {
        return targetHeight;
    }

    @Override
    public @NonNull ImageEncoder targetSize(int targetWidth, int targetHeight) {
        if (targetWidth <= 0 || targetHeight <= 0) {
            throw new IllegalArgumentException("Target size must be positive");
        }
        if (this.targetWidth != targetWidth || this.targetHeight != targetHeight) {
            this.targetWidth = targetWidth;
            this.targetHeight = targetHeight;
            invalidateCache();
        }
        return this;
    }

    @Override
    public @NonNull ImageEncoder fitImage(boolean fitImage) {
        if (this.fitImage != fitImage) {
            this.fitImage = fitImage;
            invalidateCache();
        }
        return this;
    }

    @Override
    public boolean fitImage() {
        return fitImage;
    }

    private void invalidateCache() {
        this.scaledImage = null;
        this.base64Data = null;
    }

    @Override
    public void render(@NonNull Appendable output) throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("Output cannot be null");
        }

        // Lazily compute and cache the scaled image and encoded data
        if (base64Data == null) {
            // Calculate target pixel dimensions based on terminal size and font size
            FontSize fontSize = FontSize.defaultFontSize();
            int targetWidthPx = targetWidth * fontSize.widthInPixels;
            int targetHeightPx = targetHeight * fontSize.heightInPixels;

            // Scale the image to fit the target dimensions
            scaledImage = ImageUtils.scaleImage(image, targetWidthPx, targetHeightPx, fitImage);

            // Encode image as PNG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(scaledImage, "png", baos);
            byte[] imageData = baos.toByteArray();

            // Encode to base64
            base64Data = Base64.getEncoder().encodeToString(imageData);
        }

        // Calculate number of rows and columns the image will occupy
        int cols = targetWidth;
        int rows = targetHeight;

        // Build Kitty graphics command
        // Control data format: a=<action>,f=<format>,t=<transmission>,c=<columns>,r=<rows>
        // a=T : transmit and display
        // f=100 : PNG format
        // t=d : direct transmission (inline)
        // c,r : columns and rows
        StringBuilder controlData = new StringBuilder();
        controlData.append("a=T"); // Transmit and display immediately
        controlData.append(",f=100"); // PNG format
        controlData.append(",t=d"); // Direct transmission
        controlData.append(",c=").append(cols); // Width in columns
        controlData.append(",r=").append(rows); // Height in rows

        // Split base64 data into chunks (maximum 4096 bytes per chunk recommended)
        int chunkSize = 4096;
        int dataLength = base64Data.length();

        for (int i = 0; i < dataLength; i += chunkSize) {
            int end = Math.min(i + chunkSize, dataLength);
            String chunk = base64Data.substring(i, end);
            boolean isLastChunk = (end >= dataLength);

            // Start graphics command
            output.append(APC);
            output.append(GRAPHICS_CMD);

            // Add control data only for first chunk
            if (i == 0) {
                output.append(controlData);
            }

            // Add 'm' parameter to indicate chunking
            if (!isLastChunk) {
                if (i == 0) {
                    output.append(",");
                }
                output.append("m=1"); // More chunks coming
            } else {
                if (i > 0) {
                    output.append("m=0"); // Last chunk
                }
            }

            // Add the data
            output.append(";");
            output.append(chunk);

            // End graphics command
            output.append(AnsiUtils.ST);
        }
    }
}
