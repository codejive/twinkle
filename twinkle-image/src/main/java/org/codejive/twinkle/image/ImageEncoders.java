package org.codejive.twinkle.image;

import java.awt.image.BufferedImage;
import org.codejive.twinkle.image.impl.*;
import org.codejive.twinkle.image.impl.BlockEncoder.*;
import org.jspecify.annotations.NonNull;

/**
 * Factory for creating image encoder instances.
 *
 * <p>This factory provides convenient methods to create image encoder implementations. Encoders are
 * stateful objects that encapsulate an image and rendering parameters.
 */
public class ImageEncoders {

    private ImageEncoders() {
        // Utility class, prevent instantiation
    }

    /**
     * Creates a new Sixel encoder instance.
     *
     * @param image the image to encode
     * @param targetWidth the initial target width in terminal columns
     * @param targetHeight the initial target height in terminal rows
     * @param fitImage the initial fit mode
     * @return a new Sixel encoder
     */
    public static @NonNull ImageEncoder sixel(
            @NonNull BufferedImage image, int targetWidth, int targetHeight, boolean fitImage) {
        return new SixelEncoder(image, targetWidth, targetHeight, fitImage);
    }

    /**
     * Creates a new Kitty encoder instance.
     *
     * @param image the image to encode
     * @param targetWidth the initial target width in terminal columns
     * @param targetHeight the initial target height in terminal rows
     * @param fitImage the initial fit mode
     * @return a new Kitty encoder
     */
    public static @NonNull ImageEncoder kitty(
            @NonNull BufferedImage image, int targetWidth, int targetHeight, boolean fitImage) {
        return new KittyEncoder(image, targetWidth, targetHeight, fitImage);
    }

    /**
     * Creates a new iTerm encoder instance.
     *
     * @param image the image to encode
     * @param targetWidth the initial target width in terminal columns
     * @param targetHeight the initial target height in terminal rows
     * @param fitImage the initial fit mode
     * @return a new iTerm encoder
     */
    public static @NonNull ImageEncoder iterm(
            @NonNull BufferedImage image, int targetWidth, int targetHeight, boolean fitImage) {
        return new ITermEncoder(image, targetWidth, targetHeight, fitImage);
    }

    /**
     * Creates a new block encoder instance with the specified mode.
     *
     * @param mode the block rendering mode
     * @param image the image to encode
     * @param targetWidth the initial target width in terminal columns
     * @param targetHeight the initial target height in terminal rows
     * @param fitImage the initial fit mode
     * @return a new block encoder
     */
    public static @NonNull ImageEncoder block(
            @NonNull BlockMode mode,
            @NonNull BufferedImage image,
            int targetWidth,
            int targetHeight,
            boolean fitImage) {
        return new BlockEncoder(mode, image, targetWidth, targetHeight, fitImage);
    }

    /**
     * Creates a new full-block encoder instance.
     *
     * @param image the image to encode
     * @param targetWidth the initial target width in terminal columns
     * @param targetHeight the initial target height in terminal rows
     * @param fitImage the initial fit mode
     * @return a new full-block encoder
     */
    public static @NonNull ImageEncoder blockFull(
            @NonNull BufferedImage image, int targetWidth, int targetHeight, boolean fitImage) {
        return block(BlockMode.FULL, image, targetWidth, targetHeight, fitImage);
    }

    /**
     * Creates a new half-block encoder instance (most compatible block mode).
     *
     * @param image the image to encode
     * @param targetWidth the initial target width in terminal columns
     * @param targetHeight the initial target height in terminal rows
     * @param fitImage the initial fit mode
     * @return a new half-block encoder
     */
    public static @NonNull ImageEncoder blockHalf(
            @NonNull BufferedImage image, int targetWidth, int targetHeight, boolean fitImage) {
        return block(BlockMode.HALF, image, targetWidth, targetHeight, fitImage);
    }

    /**
     * Creates a new quadrant-block encoder instance.
     *
     * @param image the image to encode
     * @param targetWidth the initial target width in terminal columns
     * @param targetHeight the initial target height in terminal rows
     * @param fitImage the initial fit mode
     * @return a new quadrant-block encoder
     */
    public static @NonNull ImageEncoder blockQuadrant(
            @NonNull BufferedImage image, int targetWidth, int targetHeight, boolean fitImage) {
        return block(BlockMode.QUADRANT, image, targetWidth, targetHeight, fitImage);
    }

    /**
     * Creates a new sextant-block encoder instance.
     *
     * @param image the image to encode
     * @param targetWidth the initial target width in terminal columns
     * @param targetHeight the initial target height in terminal rows
     * @param fitImage the initial fit mode
     * @return a new sextant-block encoder
     */
    public static @NonNull ImageEncoder blockSextant(
            @NonNull BufferedImage image, int targetWidth, int targetHeight, boolean fitImage) {
        return block(BlockMode.SEXTANT, image, targetWidth, targetHeight, fitImage);
    }

    /**
     * Creates a new octant-block encoder instance.
     *
     * @param image the image to encode
     * @param targetWidth the initial target width in terminal columns
     * @param targetHeight the initial target height in terminal rows
     * @param fitImage the initial fit mode
     * @return a new octant-block encoder
     */
    public static @NonNull ImageEncoder blockOctant(
            @NonNull BufferedImage image, int targetWidth, int targetHeight, boolean fitImage) {
        return block(BlockMode.OCTANT, image, targetWidth, targetHeight, fitImage);
    }

    /** Encoder type enumeration for terminal encoder detection. */
    public enum EncoderType {
        SIXEL,
        KITTY,
        ITERM,
        BLOCK_QUADRANT
    }

    /**
     * Attempts to detect which encoder type is supported by the current terminal.
     *
     * <p>This method checks environment variables and terminal capabilities to determine the best
     * encoder type to use. The detection logic looks for:
     *
     * <ul>
     *   <li>TERM_PROGRAM environment variable (e.g., "iTerm.app" for iTerm2)
     *   <li>KITTY_WINDOW_ID environment variable (for Kitty terminal)
     *   <li>Terminal type indicators for Sixel support
     * </ul>
     *
     * @return the detected encoder type, or block encoder as a fallback (most compatible)
     */
    public static @NonNull EncoderType detectEncoderType() {
        // Check for iTerm2
        String termProgram = System.getenv("TERM_PROGRAM");
        if ("iTerm.app".equals(termProgram)) {
            return EncoderType.ITERM;
        }

        // Check for Kitty
        String kittyWindowId = System.getenv("KITTY_WINDOW_ID");
        if (kittyWindowId != null && !kittyWindowId.isEmpty()) {
            return EncoderType.KITTY;
        }

        // Check for WezTerm (supports Kitty and iTerm encoding formats)
        if ("WezTerm".equals(termProgram)) {
            return EncoderType.KITTY; // WezTerm supports Kitty encoding format
        }

        // Check TERM variable for Sixel support
        String term = System.getenv("TERM");
        if (term != null
                && (term.contains("xterm") || term.contains("mlterm") || term.contains("vt340"))) {
            // Many terminals support Sixel with proper configuration
            // Default to Sixel as it's the most widely supported legacy format
            return EncoderType.SIXEL;
        }

        // Default to block encoder as the most compatible fallback
        // Works in any terminal with Unicode support (virtually all modern terminals)
        return EncoderType.BLOCK_QUADRANT;
    }

    /**
     * Creates an encoder instance of the detected type for the given image and parameters.
     *
     * @param image the image to encode
     * @param targetWidth the initial target width in terminal columns
     * @param targetHeight the initial target height in terminal rows
     * @param fitImage the initial fit mode
     * @return a new encoder instance of the detected type
     */
    public static @NonNull ImageEncoder detectAndCreate(
            @NonNull BufferedImage image, int targetWidth, int targetHeight, boolean fitImage) {
        EncoderType type = detectEncoderType();
        switch (type) {
            case SIXEL:
                return sixel(image, targetWidth, targetHeight, fitImage);
            case KITTY:
                return kitty(image, targetWidth, targetHeight, fitImage);
            case ITERM:
                return iterm(image, targetWidth, targetHeight, fitImage);
            case BLOCK_QUADRANT:
            default:
                return blockQuadrant(image, targetWidth, targetHeight, fitImage);
        }
    }
}
