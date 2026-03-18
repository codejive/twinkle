package org.codejive.twinkle.demos;

// spotless:off
//DEPS org.codejive.twinkle:twinkle-terminal-aesh:1.0-SNAPSHOT
//DEPS org.codejive.twinkle:twinkle-image:1.0-SNAPSHOT
// spotless:on

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.codejive.twinkle.image.ImageEncoder;
import org.codejive.twinkle.image.ImageEncoders;
import org.codejive.twinkle.terminal.Terminal;

/**
 * Demo application showing how to use the terminal image encoding framework.
 *
 * <p>This example demonstrates rendering images to the terminal using different encoders (Sixel,
 * Kitty, iTerm2, and block-based Unicode rendering).
 *
 * <p>Usage: {@code ImageEncoderDemo [--encoder=<name>] [--all]}
 *
 * <p>Supported encoder names: sixel, kitty, iterm2, block-full, block-half, block-quadrant,
 * block-sextant, block-octant.
 */
public class ImageEncoderDemo {

    public static void main(String[] args) throws Exception {
        try (Terminal terminal = Terminal.getDefault()) {
            PrintWriter writer = terminal.writer();

            // Create a simple test image
            BufferedImage testImage = createTestImage(200, 150);

            // Define target size in terminal rows/columns
            int targetWidth = 20; // 20 columns wide
            int targetHeight = 10; // 10 rows tall

            writer.println("=== Image Encoder Demo ===");

            boolean fitImage = true;

            String encoderName = getEncoderArg(args);

            if (encoderName != null) {
                // Use a specific encoder requested via --encoder=
                ImageEncoder.Provider provider = findProvider(encoderName);
                if (provider == null) {
                    writer.println("Unknown encoder: " + encoderName);
                    writer.println(
                            "Available: sixel, kitty, iterm2, block-full, block-half,"
                                    + " block-quadrant, block-sextant, block-octant");
                    writer.flush();
                    return;
                }
                writer.println("Using encoder: " + provider.name());
                writer.println();
                writer.println("Rendering with " + provider.name() + " encoder:");
                writer.flush();
                renderImage(
                        provider.create(testImage, targetWidth, targetHeight, fitImage), writer);
                writer.println("\n");
            } else {
                // Detect the best encoder for the current terminal
                ImageEncoder.Provider bestProvider = ImageEncoders.best();
                ImageEncoder detectedEncoder =
                        bestProvider.create(testImage, targetWidth, targetHeight, fitImage);
                writer.println("Detected encoder: " + bestProvider.name());
                writer.println();

                // Try rendering with the detected encoder
                writer.println("Rendering with " + bestProvider.name() + " encoder:");
                writer.flush();
                renderImage(detectedEncoder, writer);
                writer.println("\n");

                // Optionally try all available encoders
                if (shouldTestAllEncoders(args)) {
                    writer.println("\n--- Testing all encoders ---\n");

                    for (ImageEncoder.Provider provider : ImageEncoders.providers()) {
                        testEncoder(
                                provider.name(),
                                provider.create(testImage, targetWidth, targetHeight, fitImage),
                                writer);
                    }
                }
            }

            writer.println("\nDemo complete!");
            writer.flush();
        }
    }

    private static String getEncoderArg(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--encoder=")) {
                return arg.substring("--encoder=".length());
            }
        }
        return null;
    }

    private static ImageEncoder.Provider findProvider(String name) {
        String normalized = name.toLowerCase();
        List<ImageEncoder.Provider> all = ImageEncoders.providers();
        for (ImageEncoder.Provider provider : all) {
            String providerKey = provider.name().toLowerCase();
            if (providerKey.equals(normalized)) {
                return provider;
            }
        }
        return null;
    }

    private static void testEncoder(String name, ImageEncoder encoder, PrintWriter writer)
            throws IOException {
        writer.println(name + " encoder:");
        writer.flush();
        renderImage(encoder, writer);
        writer.println("\n");
    }

    private static void renderImage(ImageEncoder encoder, Appendable output) throws IOException {
        encoder.render(output);
    }

    /**
     * Creates a simple test image with a gradient and some shapes.
     *
     * @param width the image width
     * @param height the image height
     * @return the created test image
     */
    private static BufferedImage createTestImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // Draw gradient background
        for (int y = 0; y < height; y++) {
            float hue = (float) y / height;
            Color color = Color.getHSBColor(hue, 0.8f, 0.9f);
            g.setColor(color);
            g.fillRect(0, y, width, 1);
        }

        // Draw some shapes
        g.setColor(Color.WHITE);
        g.fillOval(width / 4, height / 4, width / 2, height / 2);

        g.setColor(Color.BLACK);
        g.drawString("Test Image", width / 3, height / 2);

        g.dispose();
        return image;
    }

    private static boolean shouldTestAllEncoders(String[] args) {
        for (String arg : args) {
            if ("--all".equals(arg) || "-a".equals(arg)) {
                return true;
            }
        }
        return false;
    }
}
