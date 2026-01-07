package org.codejive.twinkle.util;

import java.io.IOException;
import org.codejive.twinkle.ansi.Style;
import org.jspecify.annotations.NonNull;

public interface Printable {
    /**
     * Converts the object to an ANSI string, including ANSI escape codes for styles. This method
     * resets the current style to default at the start of the string.
     *
     * @return The ANSI string representation of the object.
     */
    default @NonNull String toAnsiString() {
        return toAnsiString(Style.of(Style.F_UNKNOWN));
    }

    /**
     * Outputs the object as an ANSI string, including ANSI escape codes for styles. This method
     * resets the current style to default at the start of the output.
     *
     * @param appendable The <code>Appendable</code> to write the ANSI output to.
     * @return The <code>Appendable</code> passed as parameter.
     */
    default @NonNull Appendable toAnsi(Appendable appendable) throws IOException {
        return toAnsi(appendable, Style.of(Style.F_UNKNOWN));
    }

    /**
     * Converts the object to an ANSI string, including ANSI escape codes for styles. This method
     * takes into account the provided current style to generate a result that is as efficient as
     * possible in terms of ANSI codes.
     *
     * @param currentStyle The current style to start with.
     * @return The ANSI string representation of the object.
     */
    default @NonNull String toAnsiString(Style currentStyle) {
        try {
            return toAnsi(new StringBuilder(), currentStyle).toString();
        } catch (IOException e) {
            // This should never happen since we're not actually doing any I/O here.
            throw new RuntimeException(e);
        }
    }

    /**
     * Outputs the object as an ANSI string, including ANSI escape codes for styles. This method
     * takes into account the provided current style to generate a result that is as efficient as
     * possible in terms of ANSI codes.
     *
     * @param appendable The <code>Appendable</code> to write the ANSI output to.
     * @param currentStyle The current style to start with.
     * @return The <code>Appendable</code> passed as parameter.
     */
    @NonNull Appendable toAnsi(Appendable appendable, Style currentStyle) throws IOException;
}
