package org.codejive.twinkle.image.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import org.codejive.twinkle.image.ImageEncoder;
import org.codejive.twinkle.image.util.AnsiUtils;
import org.codejive.twinkle.image.util.FontSize;
import org.codejive.twinkle.image.util.ImageUtils;
import org.codejive.twinkle.image.util.Resolution;
import org.jspecify.annotations.NonNull;

/**
 * Implementation of a block-based terminal image encoder using Unicode block characters.
 *
 * <p>This encoder works in any terminal by using Unicode block drawing characters (half-blocks,
 * quadrants, sextants, or octants) to represent sub-pixel resolution within each character cell.
 * Since each cell can only have one foreground and one background color, this implementation uses
 * color clustering to find the best two representative colors for each cell's pixels.
 *
 * <p>This is the most compatible image rendering method as it requires no special terminal support
 * beyond Unicode and ANSI color codes.
 *
 * <p>This encoder is stateful: the image, font size, and block mode are set at construction time
 * and are immutable, while the target size and fit mode can be changed via setters. Expensive
 * transformations like image scaling are performed lazily on the first call to {@link
 * #render(Appendable)} and cached for subsequent calls.
 */
public class BlockEncoder implements ImageEncoder {
    // Immutable state
    private final @NonNull BufferedImage image;
    private final @NonNull BlockMode mode;

    // Mutable state
    private int targetWidth;
    private int targetHeight;
    private boolean fitImage;

    // Cached transformations
    private BufferedImage scaledImage;

    public static BlockEncoder create(
            @NonNull BlockMode mode,
            @NonNull BufferedImage image,
            int targetWidth,
            int targetHeight,
            boolean fitImage) {
        return new BlockEncoder(mode, image, targetWidth, targetHeight, fitImage);
    }

    /**
     * Defines the different block rendering modes for the block-based image encoder.
     *
     * <p>Block modes determine how many sub-pixels are rendered within each terminal character
     * cell, trading off between resolution and compatibility.
     */
    public enum BlockMode {
        /**
         * Full block mode using solid block characters
         *
         * <p>Each cell represents a single pixel (1x1), with no subdivision. This is the simplest,
         * rendering each terminal cell as a solid color. Provides lowest resolution.
         */
        FULL(1, 1),

        /**
         * Half-block mode using upper and lower half block characters
         *
         * <p>Divides each cell into 2 vertical pixels (1x2), providing basic vertical resolution
         * improvement. This is the most compatible mode, supported in virtually all terminals.
         */
        HALF(1, 2),

        /**
         * Quadrant mode using 2x2 block characters
         *
         * <p>Divides each cell into 4 pixels (2x2), providing moderate resolution improvement in
         * both dimensions. Well supported in modern terminals.
         */
        QUADRANT(2, 2),

        /**
         * Sextant mode using 2x3 block characters.
         *
         * <p>Divides each cell into 6 pixels (2x3), providing higher vertical resolution. Requires
         * Unicode support for Symbols for Legacy Computing characters (U+1FB00-U+1FB3B).
         */
        SEXTANT(2, 3),

        /**
         * Octant mode using 2x4 block characters.
         *
         * <p>Divides each cell into 8 pixels (2x4), providing the highest resolution. Requires wide
         * Unicode support.
         */
        OCTANT(2, 4);

        private final int columns;
        private final int rows;

        BlockMode(int columns, int rows) {
            this.columns = columns;
            this.rows = rows;
        }

        /**
         * Gets the number of horizontal sub-pixels per cell.
         *
         * @return the number of horizontal sub-pixels in this block mode (1 or 2)
         */
        public int columns() {
            return columns;
        }

        /**
         * Gets the number of vertical sub-pixels per cell.
         *
         * @return the number of vertical sub-pixels in this block mode (2, 3, or 4)
         */
        public int rows() {
            return rows;
        }

        /**
         * Gets the total number of sub-pixels per cell.
         *
         * @return columns * rows
         */
        public int pixelsPerCell() {
            return columns * rows;
        }
    }

    // Full block characters (1x1)
    private static final String[] FULL_BLOCKS = {
        " ", // 0b0 - empty
        "█" // 0b1 - full block
    };

    // Half-block characters (1x2)
    private static final String[] HALF_BLOCKS = {
        " ", // 0b00 - empty
        "▀", // 0b01 - upper half
        "▄", // 0b10 - lower half
        "█" // 0b11 - full block
    };

    // Quadrant characters (2x2) - indexed by bit pattern: top-left, top-right, bottom-left,
    // bottom-right
    private static final String[] QUADRANT_BLOCKS = {
        " ", // 0b0000
        "▘", // 0b0001 - top-left
        "▝", // 0b0010 - top-right
        "▀", // 0b0011 - top half
        "▖", // 0b0100 - bottom-left
        "▌", // 0b0101 - left half
        "▞", // 0b0110 - diagonal bottom-left to top-right
        "▛", // 0b0111 - top and left
        "▗", // 0b1000 - bottom-right
        "▚", // 0b1001 - diagonal top-left to bottom-right
        "▐", // 0b1010 - right half
        "▜", // 0b1011 - top and right
        "▄", // 0b1100 - bottom half
        "▙", // 0b1101 - bottom and left
        "▟", // 0b1110 - bottom and right
        "█" // 0b1111 - full block
    };

    // Sextant characters (2x3) - Symbols for Legacy Computing block (U+1FB00-U+1FB3B)
    // Bit layout:  bit 0 = top-left (pos 1),    bit 1 = top-right (pos 2)
    //              bit 2 = middle-left (pos 3),  bit 3 = middle-right (pos 4)
    //              bit 4 = bottom-left (pos 5),  bit 5 = bottom-right (pos 6)
    // Two patterns use existing Block Elements characters instead of this range:
    //   pattern 21 (0b010101, positions 1,3,5) = ▌ LEFT HALF BLOCK (U+258C)
    //   pattern 42 (0b101010, positions 2,4,6) = ▐ RIGHT HALF BLOCK (U+2590)
    private static final String[] SEXTANT_BLOCKS = {
        " ", // 0b000000 (0)
        "\uD83E\uDF00", // 0b000001 (1)  - U+1FB00 SEXTANT-1
        "\uD83E\uDF01", // 0b000010 (2)  - U+1FB01 SEXTANT-2
        "\uD83E\uDF02", // 0b000011 (3)  - U+1FB02 SEXTANT-12
        "\uD83E\uDF03", // 0b000100 (4)  - U+1FB03 SEXTANT-3
        "\uD83E\uDF04", // 0b000101 (5)  - U+1FB04 SEXTANT-13
        "\uD83E\uDF05", // 0b000110 (6)  - U+1FB05 SEXTANT-23
        "\uD83E\uDF06", // 0b000111 (7)  - U+1FB06 SEXTANT-123
        "\uD83E\uDF07", // 0b001000 (8)  - U+1FB07 SEXTANT-4
        "\uD83E\uDF08", // 0b001001 (9)  - U+1FB08 SEXTANT-14
        "\uD83E\uDF09", // 0b001010 (10) - U+1FB09 SEXTANT-24
        "\uD83E\uDF0A", // 0b001011 (11) - U+1FB0A SEXTANT-124
        "\uD83E\uDF0B", // 0b001100 (12) - U+1FB0B SEXTANT-34
        "\uD83E\uDF0C", // 0b001101 (13) - U+1FB0C SEXTANT-134
        "\uD83E\uDF0D", // 0b001110 (14) - U+1FB0D SEXTANT-234
        "\uD83E\uDF0E", // 0b001111 (15) - U+1FB0E SEXTANT-1234
        "\uD83E\uDF0F", // 0b010000 (16) - U+1FB0F SEXTANT-5
        "\uD83E\uDF10", // 0b010001 (17) - U+1FB10 SEXTANT-15
        "\uD83E\uDF11", // 0b010010 (18) - U+1FB11 SEXTANT-25
        "\uD83E\uDF12", // 0b010011 (19) - U+1FB12 SEXTANT-125
        "\uD83E\uDF13", // 0b010100 (20) - U+1FB13 SEXTANT-35
        "▌", // 0b010101 (21) - U+258C  LEFT HALF BLOCK (positions 1,3,5)
        "\uD83E\uDF14", // 0b010110 (22) - U+1FB14 SEXTANT-235
        "\uD83E\uDF15", // 0b010111 (23) - U+1FB15 SEXTANT-1235
        "\uD83E\uDF16", // 0b011000 (24) - U+1FB16 SEXTANT-45
        "\uD83E\uDF17", // 0b011001 (25) - U+1FB17 SEXTANT-145
        "\uD83E\uDF18", // 0b011010 (26) - U+1FB18 SEXTANT-245
        "\uD83E\uDF19", // 0b011011 (27) - U+1FB19 SEXTANT-1245
        "\uD83E\uDF1A", // 0b011100 (28) - U+1FB1A SEXTANT-345
        "\uD83E\uDF1B", // 0b011101 (29) - U+1FB1B SEXTANT-1345
        "\uD83E\uDF1C", // 0b011110 (30) - U+1FB1C SEXTANT-2345
        "\uD83E\uDF1D", // 0b011111 (31) - U+1FB1D SEXTANT-12345
        "\uD83E\uDF1E", // 0b100000 (32) - U+1FB1E SEXTANT-6
        "\uD83E\uDF1F", // 0b100001 (33) - U+1FB1F SEXTANT-16
        "\uD83E\uDF20", // 0b100010 (34) - U+1FB20 SEXTANT-26
        "\uD83E\uDF21", // 0b100011 (35) - U+1FB21 SEXTANT-126
        "\uD83E\uDF22", // 0b100100 (36) - U+1FB22 SEXTANT-36
        "\uD83E\uDF23", // 0b100101 (37) - U+1FB23 SEXTANT-136
        "\uD83E\uDF24", // 0b100110 (38) - U+1FB24 SEXTANT-236
        "\uD83E\uDF25", // 0b100111 (39) - U+1FB25 SEXTANT-1236
        "\uD83E\uDF26", // 0b101000 (40) - U+1FB26 SEXTANT-46
        "\uD83E\uDF27", // 0b101001 (41) - U+1FB27 SEXTANT-146
        "▐", // 0b101010 (42) - U+2590  RIGHT HALF BLOCK (positions 2,4,6)
        "\uD83E\uDF28", // 0b101011 (43) - U+1FB28 SEXTANT-1246
        "\uD83E\uDF29", // 0b101100 (44) - U+1FB29 SEXTANT-346
        "\uD83E\uDF2A", // 0b101101 (45) - U+1FB2A SEXTANT-1346
        "\uD83E\uDF2B", // 0b101110 (46) - U+1FB2B SEXTANT-2346
        "\uD83E\uDF2C", // 0b101111 (47) - U+1FB2C SEXTANT-12346
        "\uD83E\uDF2D", // 0b110000 (48) - U+1FB2D SEXTANT-56
        "\uD83E\uDF2E", // 0b110001 (49) - U+1FB2E SEXTANT-156
        "\uD83E\uDF2F", // 0b110010 (50) - U+1FB2F SEXTANT-256
        "\uD83E\uDF30", // 0b110011 (51) - U+1FB30 SEXTANT-1256
        "\uD83E\uDF31", // 0b110100 (52) - U+1FB31 SEXTANT-356
        "\uD83E\uDF32", // 0b110101 (53) - U+1FB32 SEXTANT-1356
        "\uD83E\uDF33", // 0b110110 (54) - U+1FB33 SEXTANT-2356
        "\uD83E\uDF34", // 0b110111 (55) - U+1FB34 SEXTANT-12356
        "\uD83E\uDF35", // 0b111000 (56) - U+1FB35 SEXTANT-456
        "\uD83E\uDF36", // 0b111001 (57) - U+1FB36 SEXTANT-1456
        "\uD83E\uDF37", // 0b111010 (58) - U+1FB37 SEXTANT-2456
        "\uD83E\uDF38", // 0b111011 (59) - U+1FB38 SEXTANT-12456
        "\uD83E\uDF39", // 0b111100 (60) - U+1FB39 SEXTANT-3456
        "\uD83E\uDF3A", // 0b111101 (61) - U+1FB3A SEXTANT-13456
        "\uD83E\uDF3B", // 0b111110 (62) - U+1FB3B SEXTANT-23456
        "█" // 0b111111 (63) - full block
    };

    /**
     * Creates a block encoder with the specified mode, image, and font size.
     *
     * @param mode the block rendering mode
     * @param image the image to encode
     * @param targetWidth the initial target width in terminal columns
     * @param targetHeight the initial target height in terminal rows
     * @param fitImage the initial fit mode
     */
    protected BlockEncoder(
            @NonNull BlockMode mode,
            @NonNull BufferedImage image,
            int targetWidth,
            int targetHeight,
            boolean fitImage) {
        if (mode == null) {
            throw new IllegalArgumentException("Mode cannot be null");
        }
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        if (targetWidth <= 0) {
            throw new IllegalArgumentException("Target width must be positive");
        }
        if (targetHeight <= 0) {
            throw new IllegalArgumentException("Target height must be positive");
        }
        this.mode = mode;
        this.image = image;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.fitImage = fitImage;
    }

    /**
     * Creates a block encoder with half-block mode (most compatible).
     *
     * @param image the image to encode
     * @param targetWidth the initial target width in terminal columns
     * @param targetHeight the initial target height in terminal rows
     * @param fitImage the initial fit mode
     */
    public BlockEncoder(
            @NonNull BufferedImage image, int targetWidth, int targetHeight, boolean fitImage) {
        this(BlockMode.HALF, image, targetWidth, targetHeight, fitImage);
    }

    /**
     * Gets the block rendering mode used by this encoder.
     *
     * @return the block mode (FULL, HALF, QUADRANT, SEXTANT, or OCTANT)
     */
    public @NonNull BlockMode mode() {
        return mode;
    }

    @Override
    public @NonNull ImageEncoder targetSize(int targetWidth, int targetHeight) {
        if (targetWidth <= 0) {
            throw new IllegalArgumentException("Target width must be positive");
        }
        if (targetHeight <= 0) {
            throw new IllegalArgumentException("Target height must be positive");
        }
        if (this.targetWidth != targetWidth || this.targetHeight != targetHeight) {
            this.targetWidth = targetWidth;
            this.targetHeight = targetHeight;
            this.scaledImage = null; // Invalidate cache
        }
        return this;
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
    public @NonNull ImageEncoder fitImage(boolean fitImage) {
        if (this.fitImage != fitImage) {
            this.fitImage = fitImage;
            this.scaledImage = null; // Invalidate cache
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

        // Lazily compute and cache the scaled image
        if (scaledImage == null) {
            // Calculate the physical pixel dimensions of the terminal area
            // This accounts for the actual font size (e.g., 8x16 pixels per cell)
            Resolution fontSize = FontSize.defaultFontSize();
            int physicalWidth = targetWidth * fontSize.x;
            int physicalHeight = targetHeight * fontSize.y;

            // Scale image to match the physical dimensions (preserving aspect ratio or fitting
            // exactly)
            scaledImage = ImageUtils.scaleImage(image, physicalWidth, physicalHeight, fitImage);
        }

        // Calculate how many cells the scaled image actually fills
        // (aspect ratio preservation may leave the image smaller in one dimension)
        Resolution fontSize = FontSize.defaultFontSize();
        int actualCols =
                Math.min(
                        (int) Math.ceil((double) scaledImage.getWidth() / fontSize.x), targetWidth);
        int actualRows =
                Math.min(
                        (int) Math.ceil((double) scaledImage.getHeight() / fontSize.y),
                        targetHeight);

        // Render using only the cells covered by the image
        renderBlocks(scaledImage, actualCols, actualRows, output);
    }

    /**
     * Renders the scaled image using block characters.
     *
     * @param image the scaled image
     * @param targetWidth the target width in terminal columns
     * @param targetHeight the target height in terminal rows
     * @param output the output to write to
     * @throws IOException if an I/O error occurs
     */
    private void renderBlocks(
            @NonNull BufferedImage image,
            int targetWidth,
            int targetHeight,
            @NonNull Appendable output)
            throws IOException {

        int cols = mode.columns();
        int rows = mode.rows();

        // Calculate how many physical pixels each sub-pixel represents
        Resolution fontSize = FontSize.defaultFontSize();
        double pixelsPerSubPixelX = (double) fontSize.x / cols;
        double pixelsPerSubPixelY = (double) fontSize.y / rows;

        for (int cellRow = 0; cellRow < targetHeight; cellRow++) {
            for (int cellCol = 0; cellCol < targetWidth; cellCol++) {
                // Sample pixels for this cell
                int[] pixels =
                        sampleCell(image, cellCol, cellRow, pixelsPerSubPixelX, pixelsPerSubPixelY);

                // Find the two best representative colors
                ColorPair colors = findBestColorPair(pixels);

                // Determine which pixels belong to foreground vs background
                int pattern = determinePattern(pixels, colors);

                // Get the appropriate block character
                String blockChar = getBlockCharacter(pattern);

                // Output the character with colors
                outputCell(output, blockChar, colors);
            }
            // Reset colors at the end of each line to prevent bleeding
            output.append(AnsiUtils.STYLE_RESET);
            if (cellRow < targetHeight - 1) {
                output.append('\n');
            }
        }
    }

    /**
     * Samples the pixels for a single cell.
     *
     * @param image the image to sample from
     * @param cellCol the cell column
     * @param cellRow the cell row
     * @param pixelsPerSubPixelX physical pixels per sub-pixel in X direction
     * @param pixelsPerSubPixelY physical pixels per sub-pixel in Y direction
     * @return array of RGB pixel values
     */
    private int[] sampleCell(
            @NonNull BufferedImage image,
            int cellCol,
            int cellRow,
            double pixelsPerSubPixelX,
            double pixelsPerSubPixelY) {
        int cols = mode.columns();
        int rows = mode.rows();
        int[] pixels = new int[cols * rows];

        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // Calculate sub-pixel coordinates
                int subPixelX = cellCol * cols + col;
                int subPixelY = cellRow * rows + row;

                // Map to physical pixel coordinates
                int x = (int) (subPixelX * pixelsPerSubPixelX);
                int y = (int) (subPixelY * pixelsPerSubPixelY);

                // Clamp coordinates to image bounds
                x = Math.min(x, imgWidth - 1);
                y = Math.min(y, imgHeight - 1);

                pixels[row * cols + col] = image.getRGB(x, y);
            }
        }

        return pixels;
    }

    /**
     * Finds the best two representative colors for the given pixels using color clustering.
     *
     * @param pixels array of RGB pixel values
     * @return the foreground and background colors
     */
    private @NonNull ColorPair findBestColorPair(int[] pixels) {
        // Simple k-means clustering with k=2
        // Initialize with darkest and brightest pixels
        int darkest = 0xFFFFFF;
        int brightest = 0x000000;

        for (int i = 0; i < pixels.length; i++) {
            int rgb = pixels[i];
            int brightness = getBrightness(rgb);

            if (brightness < getBrightness(darkest)) {
                darkest = rgb;
            }
            if (brightness > getBrightness(brightest)) {
                brightest = rgb;
            }
        }

        // Perform a few iterations of k-means
        int color1 = darkest;
        int color2 = brightest;

        for (int iter = 0; iter < 3; iter++) {
            long sumR1 = 0, sumG1 = 0, sumB1 = 0, count1 = 0;
            long sumR2 = 0, sumG2 = 0, sumB2 = 0, count2 = 0;

            for (int pixel : pixels) {
                if (colorDistance(pixel, color1) < colorDistance(pixel, color2)) {
                    sumR1 += (pixel >> 16) & 0xFF;
                    sumG1 += (pixel >> 8) & 0xFF;
                    sumB1 += pixel & 0xFF;
                    count1++;
                } else {
                    sumR2 += (pixel >> 16) & 0xFF;
                    sumG2 += (pixel >> 8) & 0xFF;
                    sumB2 += pixel & 0xFF;
                    count2++;
                }
            }

            if (count1 > 0) {
                color1 =
                        ((int) (sumR1 / count1) << 16)
                                | ((int) (sumG1 / count1) << 8)
                                | (int) (sumB1 / count1);
            }
            if (count2 > 0) {
                color2 =
                        ((int) (sumR2 / count2) << 16)
                                | ((int) (sumG2 / count2) << 8)
                                | (int) (sumB2 / count2);
            }
        }

        return new ColorPair(color1, color2);
    }

    /**
     * Determines the bit pattern for which pixels belong to the foreground color.
     *
     * @param pixels array of RGB pixel values
     * @param colors the foreground and background colors
     * @return bit pattern where 1 = foreground, 0 = background
     */
    private int determinePattern(int[] pixels, @NonNull ColorPair colors) {
        int pattern = 0;
        for (int i = 0; i < pixels.length; i++) {
            if (colorDistance(pixels[i], colors.foreground)
                    < colorDistance(pixels[i], colors.background)) {
                pattern |= (1 << i);
            }
        }
        return pattern;
    }

    /**
     * Gets the appropriate block character for the given pattern.
     *
     * @param pattern the bit pattern
     * @return the Unicode block character
     */
    private @NonNull String getBlockCharacter(int pattern) {
        switch (mode) {
            case FULL:
                return FULL_BLOCKS[pattern & 0x1];
            case HALF:
                return HALF_BLOCKS[pattern & 0x3];
            case QUADRANT:
                return QUADRANT_BLOCKS[pattern & 0xF];
            case SEXTANT:
                return SEXTANT_BLOCKS[pattern & 0x3F];
            case OCTANT:
                // For octant, we'll use quadrants as a fallback for now
                // Full octant support would require additional characters
                return QUADRANT_BLOCKS[pattern & 0xF];
            default:
                return " ";
        }
    }

    /**
     * Outputs a cell with the specified character and colors.
     *
     * @param output the output to write to
     * @param blockChar the block character
     * @param colors the foreground and background colors
     * @throws IOException if an I/O error occurs
     */
    private void outputCell(
            @NonNull Appendable output, @NonNull String blockChar, @NonNull ColorPair colors)
            throws IOException {
        // Set foreground color
        int fgR = (colors.foreground >> 16) & 0xFF;
        int fgG = (colors.foreground >> 8) & 0xFF;
        int fgB = colors.foreground & 0xFF;

        // Set background color
        int bgR = (colors.background >> 16) & 0xFF;
        int bgG = (colors.background >> 8) & 0xFF;
        int bgB = colors.background & 0xFF;

        output.append(AnsiUtils.rgbFg(fgR, fgG, fgB));
        output.append(AnsiUtils.rgbBg(bgR, bgG, bgB));
        output.append(blockChar);
    }

    /**
     * Calculates the brightness of an RGB color.
     *
     * @param rgb the RGB value
     * @return the brightness (0-255)
     */
    private int getBrightness(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        // Use perceived brightness formula
        return (int) (0.299 * r + 0.587 * g + 0.114 * b);
    }

    /**
     * Calculates the distance between two RGB colors.
     *
     * @param rgb1 first RGB value
     * @param rgb2 second RGB value
     * @return the color distance
     */
    private int colorDistance(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;

        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;

        int dr = r1 - r2;
        int dg = g1 - g2;
        int db = b1 - b2;

        return dr * dr + dg * dg + db * db;
    }

    /** Helper class to hold a pair of colors (foreground and background). */
    private static class ColorPair {
        final int foreground;
        final int background;

        ColorPair(int foreground, int background) {
            this.foreground = foreground;
            this.background = background;
        }
    }

    /** Provider for creating BlockEncoder instances. */
    public static class Provider implements ImageEncoder.Provider {
        private final @NonNull BlockMode mode;

        public Provider(@NonNull BlockMode mode) {
            this.mode = mode;
        }

        @Override
        public @NonNull String name() {
            return "block-" + mode.name().toLowerCase();
        }

        @Override
        public @NonNull Resolution resolution() {
            return new Resolution(mode.columns(), mode.rows());
        }

        @Override
        public @NonNull ImageEncoder create(
                @NonNull BufferedImage image, int targetWidth, int targetHeight, boolean fitImage) {
            return new BlockEncoder(mode, image, targetWidth, targetHeight, fitImage);
        }
    }
}
