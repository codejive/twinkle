package org.codejive.twinkle.image.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import org.codejive.twinkle.image.ImageEncoder;
import org.codejive.twinkle.image.util.AnsiUtils;
import org.codejive.twinkle.image.util.ColorQuantizer;
import org.codejive.twinkle.image.util.FontSize;
import org.codejive.twinkle.image.util.ImageUtils;
import org.codejive.twinkle.image.util.Resolution;
import org.jspecify.annotations.NonNull;

/**
 * Implementation of the Sixel image encoding format.
 *
 * <p>Sixel is a bitmap graphics format originally developed by Digital Equipment Corporation (DEC).
 * It's supported by various terminal emulators including xterm (with -ti vt340 option), mlterm, and
 * others.
 *
 * <p>The Sixel encoding format encodes images as a series of six-pixel-high strips, which are then
 * transmitted as printable ASCII characters.
 *
 * <p>This encoder is stateful: the image is set at construction time and is immutable, while the
 * target size and fit mode can be changed via setters. The expensive encoding process (scaling,
 * color quantization, and Sixel encoding) is performed lazily on the first call to {@link
 * #render(Appendable)} and the result is cached for subsequent calls.
 */
public class SixelEncoder implements ImageEncoder {
    // Immutable state
    private final @NonNull BufferedImage image;

    // Mutable state
    private int targetWidth;
    private int targetHeight;
    private boolean fitImage;

    // Cached encoded result
    private String cachedSixelData;

    private static final String DCS = AnsiUtils.ESC + "P"; // Device Control String
    private static final String SIXEL_INTRO = "q"; // Sixel introducer

    public static @NonNull SixelEncoder sixel(
            @NonNull BufferedImage image, int targetWidth, int targetHeight, boolean fitImage) {
        return new SixelEncoder(image, targetWidth, targetHeight, fitImage);
    }

    /**
     * Creates a new Sixel encoder for the given image and font size.
     *
     * @param image the image to encode
     * @param targetWidth the initial target width in terminal columns
     * @param targetHeight the initial target height in terminal rows
     * @param fitImage the initial fit mode
     */
    protected SixelEncoder(
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
            this.cachedSixelData = null; // Invalidate cache
        }
        return this;
    }

    @Override
    public @NonNull ImageEncoder fitImage(boolean fitImage) {
        if (this.fitImage != fitImage) {
            this.fitImage = fitImage;
            this.cachedSixelData = null; // Invalidate cache
        }
        return this;
    }

    @Override
    public boolean fitImage() {
        return fitImage;
    }

    @Override
    public void render(@NonNull Appendable output) throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("Output cannot be null");
        }

        // Lazily compute and cache the encoded Sixel data
        if (cachedSixelData == null) {
            // Scale the image to target dimensions
            Resolution fontSize = FontSize.defaultFontSize();
            int targetWidthPx = targetWidth * fontSize.x;
            int targetHeightPx = targetHeight * fontSize.y;
            BufferedImage scaledImage =
                    ImageUtils.scaleImage(image, targetWidthPx, targetHeightPx, fitImage);

            // Encode to Sixel format and cache the result
            StringBuilder sixelData = new StringBuilder();
            sixelData.append(DCS);
            sixelData.append("0;1"); // P1=0 (default aspect), P2=1 (transparent background)
            sixelData.append(SIXEL_INTRO);
            encodeSixelData(scaledImage, sixelData);
            sixelData.append(AnsiUtils.ST);
            cachedSixelData = sixelData.toString();
        }

        // Output the cached Sixel data
        output.append(cachedSixelData);
    }

    /**
     * Encodes the image data in Sixel format.
     *
     * @param image the image to encode
     * @param output the output to write to
     * @throws IOException if an I/O error occurs
     */
    private void encodeSixelData(@NonNull BufferedImage image, @NonNull Appendable output)
            throws IOException {

        int width = image.getWidth();
        int height = image.getHeight();

        // Set raster attributes: aspect ratio (1:1) and explicit image dimensions
        output.append("\"1;1;");
        output.append(Integer.toString(width));
        output.append(';');
        output.append(Integer.toString(height));

        // Quantize image to max 256 colors for Sixel
        ColorQuantizer.QuantizedImage quantized =
                ColorQuantizer.quantize(image, Math.min(256, width * height));

        // Define color palette
        List<Integer> palette = quantized.palette();
        for (int i = 0; i < palette.size(); i++) {
            int rgb = palette.get(i);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            // Define color using RGB percentages (0-100)
            output.append('#');
            output.append(Integer.toString(i));
            output.append(";2;");
            output.append(Integer.toString(r * 100 / 255));
            output.append(';');
            output.append(Integer.toString(g * 100 / 255));
            output.append(';');
            output.append(Integer.toString(b * 100 / 255));
        }

        // Encode image data in six-pixel strips
        int[][] indexedPixels = quantized.indexedPixels();

        // Process image in strips of 6 pixels high
        for (int stripY = 0; stripY < height; stripY += 6) {
            // For each color, encode all pixels of that color in this strip
            for (int colorIndex = 0; colorIndex < palette.size(); colorIndex++) {
                boolean colorUsedInStrip = false;
                StringBuilder stripData = new StringBuilder();

                // Check each column
                for (int x = 0; x < width; x++) {
                    // Build sixel value for this column (6 pixels)
                    int sixelValue = 0;
                    for (int dy = 0; dy < 6 && stripY + dy < height; dy++) {
                        if (indexedPixels[stripY + dy][x] == colorIndex) {
                            sixelValue |= (1 << dy);
                        }
                    }

                    if (sixelValue > 0) {
                        colorUsedInStrip = true;
                    }
                    // Always output a character for every column to preserve
                    // correct x-positioning (RLE will compress '?' runs)
                    stripData.append((char) ('?' + sixelValue));
                }

                // Only output if this color was used in this strip
                if (colorUsedInStrip) {
                    // Select color
                    output.append('#');
                    output.append(Integer.toString(colorIndex));

                    // Compress repeated characters
                    compressAndAppend(stripData.toString(), output);

                    // Return to start of line
                    output.append('$');
                }
            }

            // Move to next strip (unless this is the last strip)
            if (stripY + 6 < height) {
                output.append('-');
            }
        }
    }

    /**
     * Compresses repeated characters using Sixel repeat sequences and appends to output.
     *
     * @param data the data to compress
     * @param output the output to write to
     * @throws IOException if an I/O error occurs
     */
    private void compressAndAppend(@NonNull String data, @NonNull Appendable output)
            throws IOException {
        if (data.isEmpty()) {
            return;
        }

        int i = 0;
        while (i < data.length()) {
            char ch = data.charAt(i);
            int count = 1;

            // Count consecutive identical characters
            while (i + count < data.length() && data.charAt(i + count) == ch) {
                count++;
            }

            // Use repeat sequence if count >= 3 (saves space)
            if (count >= 3) {
                output.append('!');
                output.append(Integer.toString(count));
                output.append(ch);
            } else {
                // Output characters directly
                for (int j = 0; j < count; j++) {
                    output.append(ch);
                }
            }

            i += count;
        }
    }

    /** Provider for creating SixelEncoder instances. */
    public static class Provider implements ImageEncoder.Provider {
        @Override
        public @NonNull String name() {
            return "sixel";
        }

        @Override
        public @NonNull Resolution resolution() {
            return FontSize.defaultFontSize();
        }

        @Override
        public @NonNull ImageEncoder create(
                @NonNull BufferedImage image, int targetWidth, int targetHeight, boolean fitImage) {
            return new SixelEncoder(image, targetWidth, targetHeight, fitImage);
        }
    }
}
