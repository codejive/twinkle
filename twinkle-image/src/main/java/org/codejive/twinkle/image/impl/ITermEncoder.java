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
 * Implementation of the iTerm2 inline image encoding format.
 *
 * <p>The iTerm2 inline image encoding format allows displaying images directly in the terminal. It
 * uses OSC (Operating System Command) escape sequences with base64-encoded image data.
 *
 * <p>Format: ESC ]1337;File=[arguments]:base64-data ^G
 *
 * <p>This encoder is stateful: the image and font size are set at construction time and are
 * immutable, while the target size and fit mode can be changed via setters. Expensive
 * transformations like image scaling and PNG encoding are performed lazily on the first call to
 * {@link #render(Appendable)} and cached for subsequent calls.
 *
 * @see <a href="https://iterm2.com/documentation-images.html">iTerm2 Inline Images Protocol</a>
 */
public class ITermEncoder implements ImageEncoder {
    // Immutable state
    private final @NonNull BufferedImage image;

    // Mutable state
    private int targetWidth;
    private int targetHeight;
    private boolean fitImage;

    // Cached transformations
    private BufferedImage scaledImage;
    private String base64Data;
    private int encodedDataLength;

    private static final String ITERM_FILE_CMD = "1337;File=";

    /**
     * Creates a new iTerm2 encoder for the given image and font size.
     *
     * @param image the image to encode
     * @param targetWidth the initial target width in terminal columns
     * @param targetHeight the initial target height in terminal rows
     * @param fitImage the initial fit mode
     */
    public ITermEncoder(
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
        return "iterm";
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
            encodedDataLength = imageData.length;

            // Encode to base64
            base64Data = Base64.getEncoder().encodeToString(imageData);
        }

        // Build iTerm2 inline image command
        // Format: ESC ]1337;File=[arguments]:base64-data ^G
        // Arguments:
        //   - name: optional filename (base64 encoded)
        //   - size: size in bytes
        //   - width: width in columns or pixels
        //   - height: height in rows or pixels
        //   - preserveAspectRatio: 0 or 1
        //   - inline: 1 to display inline

        output.append(AnsiUtils.OSC);
        output.append(ITERM_FILE_CMD);

        // Add arguments
        StringBuilder args = new StringBuilder();
        args.append("size=").append(encodedDataLength);
        args.append(";width=").append(targetWidth); // Width in columns
        args.append(";height=").append(targetHeight); // Height in rows
        args.append(";preserveAspectRatio=1"); // Preserve aspect ratio
        args.append(";inline=1"); // Display inline

        output.append(args);
        output.append(":");
        output.append(base64Data);
        output.append(AnsiUtils.BEL);
    }
}
