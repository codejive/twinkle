package org.codejive.twinkle.ansi.util;

import static org.codejive.twinkle.ansi.Constants.*;

import java.io.IOException;

public class StyleBuilder {

    /**
     * Returns the ANSI escape sequence for the given styles. The styles can be any combination of
     * the style constants defined in the Constants class, such as BOLD, UNDERLINED, or the output
     * of the color functions like foregroundArg(). The output is a string that can be used in the
     * console to apply the specified styles to the text that follows.
     *
     * @param styles the style codes to apply
     * @return the ANSI escape sequence for the given styles
     */
    public static String styles(Object... styles) {
        if (styles == null || styles.length == 0) {
            return "";
        }
        return styles(new StringBuilder(), styles).toString();
    }

    /**
     * Appends the ANSI escape sequence for the given styles to the provided Appendable. The styles
     * can be any combination of the style constants defined in the Constants class, such as BOLD,
     * UNDERLINED, or the output of the color functions like foregroundArg(). The output will be
     * passed to the provided Appendable.
     *
     * @param appendable the Appendable to which the ANSI escape sequence will be appended
     * @param styles the style codes to apply
     * @return the provided Appendable with the ANSI escape sequence appended
     */
    public static Appendable styles(Appendable appendable, Object... styles) {
        if (styles == null || styles.length == 0) {
            return appendable;
        }
        try {
            appendable.append(CSI);
            for (int i = 0; i < styles.length; i++) {
                String arg = styles[i].toString();
                appendable.append(arg);
                if (i < styles.length - 1) {
                    appendable.append(";");
                }
            }
            appendable.append(STYLE_CMD);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return appendable;
    }

    public static String compact(Object... styles) {
        if (styles == null || styles.length == 0) {
            return "";
        }
        return compact(new StringBuilder(), styles).toString();
    }

    public static Appendable compact(Appendable appendable, Object... styles) {
        if (styles == null || styles.length == 0) {
            return appendable;
        }
        try {
            appendable.append(CSI);
            for (int i = 0; i < styles.length; i++) {
                String arg = styles[i].toString();
                if (arg.startsWith(CSI) && arg.charAt(arg.length() - 1) == STYLE_CMD) {
                    // Strip off the CSI and trailing 'm' if the style is already a complete ANSI
                    // sequence
                    arg = arg.substring(2, arg.length() - 1);
                }
                appendable.append(arg);
                if (i < styles.length - 1) {
                    appendable.append(";");
                }
            }
            appendable.append(STYLE_CMD);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return appendable;
    }
}
