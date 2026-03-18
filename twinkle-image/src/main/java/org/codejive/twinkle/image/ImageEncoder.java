package org.codejive.twinkle.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import org.codejive.twinkle.image.util.Resolution;
import org.jspecify.annotations.NonNull;

/**
 * Base interface for terminal image encoding formats.
 *
 * <p>Implementations of this interface handle rendering images to terminals using various image
 * encoding formats such as Sixel, Kitty, and iTerm2.
 *
 * <p>ImageEncoders are stateful objects that are configured with an image and font size at
 * construction time. The target size and fit mode can be adjusted using setters, and expensive
 * transformations (like image scaling) are performed lazily on the first call to {@link
 * #render(Appendable)} and cached for subsequent calls.
 */
public interface ImageEncoder {

    /**
     * Gets the target width in terminal columns that the image should occupy.
     *
     * @return the target width in terminal columns
     */
    int targetWidth();

    /**
     * Gets the target height in terminal rows that the image should occupy.
     *
     * @return the target height in terminal rows
     */
    int targetHeight();

    /**
     * Sets the target size in terminal columns and rows that the image should occupy.
     *
     * <p>Changing this value invalidates any cached transformations.
     *
     * @param targetWidth the target width in terminal columns
     * @param targetHeight the target height in terminal rows
     * @return this encoder for method chaining
     */
    @NonNull ImageEncoder targetSize(int targetWidth, int targetHeight);

    /**
     * Gets whether the image should be fitted exactly to the target size.
     *
     * @return true if the image is fitted exactly, false if aspect ratio is preserved
     */
    boolean fitImage();

    /**
     * Sets whether the image should be fitted exactly to the target size (stretching if needed) or
     * preserve aspect ratio.
     *
     * <p>Changing this value invalidates any cached transformations.
     *
     * @param fitImage if true, scale the image to fit the targetSize exactly (stretching if
     *     needed); if false, preserve aspect ratio
     * @return this encoder for method chaining
     */
    @NonNull ImageEncoder fitImage(boolean fitImage);

    /**
     * Renders the image to the terminal using the specific encoding format's escape sequences.
     *
     * <p>This method performs expensive transformations (such as image scaling) lazily on the first
     * call and caches the results for subsequent calls. If the target size or fit mode is changed
     * via setters, the cache is invalidated and transformations are re-performed on the next
     * render.
     *
     * @param output the Appendable to write the escape sequences to
     * @throws IOException if an I/O error occurs while writing to the output
     */
    void render(@NonNull Appendable output) throws IOException;

    interface Provider {
        /**
         * Gets the name of the encoder type (e.g., "sixel", "kitty", "iterm2").
         *
         * @return the name of the encoder type
         */
        @NonNull String name();

        /**
         * Gets the resolution of the encoder. This indicates how many pixels correspond to one
         * terminal cell for this encoding format.
         *
         * @return the resolution of the encoder
         */
        @NonNull Resolution resolution();

        /**
         * Creates a new encoder instance for the given image and parameters.
         *
         * @param image the image to encode
         * @param targetWidth the initial target width in terminal columns
         * @param targetHeight the initial target height in terminal rows
         * @param fitImage the initial fit mode
         * @return a new encoder instance
         */
        @NonNull ImageEncoder create(
                @NonNull BufferedImage image, int targetWidth, int targetHeight, boolean fitImage);
    }
}
