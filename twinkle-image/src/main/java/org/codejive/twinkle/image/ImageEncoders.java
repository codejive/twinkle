package org.codejive.twinkle.image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
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

    public static @NonNull List<ImageEncoder.Provider> providers() {
        return Arrays.asList(
                new SixelEncoder.Provider(),
                new KittyEncoder.Provider(),
                new ITermEncoder.Provider(),
                new BlockEncoder.Provider(BlockMode.FULL),
                new BlockEncoder.Provider(BlockMode.HALF),
                new BlockEncoder.Provider(BlockMode.QUADRANT),
                new BlockEncoder.Provider(BlockMode.SEXTANT),
                new BlockEncoder.Provider(BlockMode.OCTANT));
    }

    /**
     * Attempts to detect which encoder types are supported by the current terminal.
     *
     * <p>This method checks environment variables and terminal capabilities to determine which
     * encoder types are supported. Results are ordered by priority (best protocol first). The
     * detection logic checks for:
     *
     * <ul>
     *   <li><b>Kitty protocol</b>: KITTY_WINDOW_ID, TERM=xterm-kitty, TERM=xterm-ghostty,
     *       WEZTERM_PANE, TERM_PROGRAM=WezTerm, KONSOLE_VERSION
     *   <li><b>iTerm2 protocol</b>: ITERM_SESSION_ID,
     *       TERM_PROGRAM=iTerm.app/mintty/vscode/tabby/hyper, TERM=rio, KONSOLE_VERSION
     *   <li><b>Sixel</b>: KONSOLE_VERSION, WT_SESSION, TERM containing
     *       mlterm/foot/contour/yaft/ctx/darktile, TERM=rio
     *   <li><b>Block encoders</b>: Always included as universal fallback
     * </ul>
     *
     * @return the detected encoder types, or block encoder as a fallback (most compatible)
     */
    public static @NonNull List<ImageEncoder.Provider> supportedProviders() {
        // LinkedHashMap keyed by provider name for deduplication and priority ordering
        LinkedHashMap<String, ImageEncoder.Provider> supported = new LinkedHashMap<>();

        String term = getEnv("TERM");
        String termLower = term != null ? term.toLowerCase() : "";
        String termProgram = getEnv("TERM_PROGRAM");

        // Kitty terminal sets KITTY_WINDOW_ID
        if (getEnv("KITTY_WINDOW_ID") != null) {
            supported.putIfAbsent("Kitty", new KittyEncoder.Provider());
        }

        // Kitty sets TERM=xterm-kitty
        if (termLower.equals("xterm-kitty")) {
            supported.putIfAbsent("Kitty", new KittyEncoder.Provider());
        }

        // Ghostty uses Kitty graphics protocol
        if (termLower.equals("xterm-ghostty")) {
            supported.putIfAbsent("Kitty", new KittyEncoder.Provider());
        }

        // WezTerm supports iTerm2 graphics protocol; detected via WEZTERM_PANE or TERM_PROGRAM
        if (getEnv("WEZTERM_PANE") != null || "WezTerm".equalsIgnoreCase(termProgram)) {
            supported.putIfAbsent("iTerm2", new ITermEncoder.Provider());
        }

        // iTerm2 sets ITERM_SESSION_ID
        if (getEnv("ITERM_SESSION_ID") != null) {
            supported.putIfAbsent("iTerm2", new ITermEncoder.Provider());
        }

        // TERM_PROGRAM=iTerm.app
        if ("iTerm.app".equals(termProgram)) {
            supported.putIfAbsent("iTerm2", new ITermEncoder.Provider());
        }

        // Mintty, VSCode integrated terminal, Tabby, and Hyper support iTerm2 inline images
        if (termProgram != null) {
            String tp = termProgram.toLowerCase();
            if ("mintty".equals(tp)
                    || "vscode".equals(tp)
                    || "tabby".equals(tp)
                    || "hyper".equals(tp)) {
                supported.putIfAbsent("iTerm2", new ITermEncoder.Provider());
            }
        }

        // Rio terminal supports both iTerm2 and Sixel
        if (termLower.equals("rio")) {
            supported.putIfAbsent("iTerm2", new ITermEncoder.Provider());
            supported.putIfAbsent("Sixel", new SixelEncoder.Provider());
        }

        // Konsole supports Kitty, iTerm2, and Sixel protocols
        if (getEnv("KONSOLE_VERSION") != null) {
            supported.putIfAbsent("Sixel", new SixelEncoder.Provider());
            supported.putIfAbsent("iTerm2", new ITermEncoder.Provider());
            supported.putIfAbsent("Kitty", new KittyEncoder.Provider());
        }

        // Windows Terminal supports Sixel (since v1.22)
        if (getEnv("WT_SESSION") != null) {
            supported.putIfAbsent("Sixel", new SixelEncoder.Provider());
        }

        // Terminals known to support Sixel via TERM identification
        if (termLower.contains("mlterm")
                || termLower.contains("foot")
                || termLower.contains("contour")
                || termLower.contains("yaft")
                || termLower.contains("ctx")
                || termLower.contains("darktile")) {
            supported.putIfAbsent("Sixel", new SixelEncoder.Provider());
        }

        // --- Block encoders as universal fallback ---
        // Works in any terminal with Unicode support (virtually all modern terminals)
        supported.put("Block (full)", new BlockEncoder.Provider(BlockMode.FULL));
        supported.put("Block (half)", new BlockEncoder.Provider(BlockMode.HALF));
        supported.put("Block (quadrant)", new BlockEncoder.Provider(BlockMode.QUADRANT));

        return new ArrayList<>(supported.values());
    }

    private static String getEnv(String name) {
        try {
            String value = System.getenv(name);
            return (value != null && !value.isEmpty()) ? value : null;
        } catch (SecurityException e) {
            return null;
        }
    }

    /**
     * Gets the best available encoder provider for the current terminal.
     *
     * @return the best available encoder provider
     */
    public static ImageEncoder.@NonNull Provider best() {
        List<ImageEncoder.Provider> providers = supportedProviders();
        return providers.get(0);
    }

    private ImageEncoders() {
        // Utility class, prevent instantiation
    }
}
