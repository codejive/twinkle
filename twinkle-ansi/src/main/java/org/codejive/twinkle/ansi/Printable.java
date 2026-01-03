package org.codejive.twinkle.ansi;

import java.io.IOException;
import org.jspecify.annotations.NonNull;

public interface Printable {
    /**
     * Converts the object to an ANSI string, including ANSI escape codes for styles. This method
     * resets the current style to default at the start of the string.
     *
     * @return The ANSI string representation of the object.
     */
    @NonNull String toAnsiString();

    /**
     * Outputs the object as an ANSI string, including ANSI escape codes for styles. This method
     * resets the current style to default at the start of the output.
     *
     * @param appendable The <code>Appendable</code> to write the ANSI output to.
     * @return The <code>Appendable</code> passed as parameter.
     */
    @NonNull Appendable toAnsi(Appendable appendable) throws IOException;

    /**
     * Converts the object to an ANSI string, including ANSI escape codes for styles. This method
     * takes into account the provided current style to generate a result that is as efficient as
     * possible in terms of ANSI codes.
     *
     * @param currentStyle The current style to start with.
     * @return The ANSI string representation of the object.
     */
    default @NonNull String toAnsiString(Style currentStyle) {
        return toAnsiString(currentStyle.state());
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
    default @NonNull Appendable toAnsi(Appendable appendable, Style currentStyle)
            throws IOException {
        return toAnsi(appendable, currentStyle.state());
    }

    /**
     * Converts the object to an ANSI string, including ANSI escape codes for styles. This method
     * takes into account the provided current style to generate a result that is as efficient as
     * possible in terms of ANSI codes.
     *
     * @param currentStyleState The current style to start with.
     * @return The ANSI string representation of the object.
     */
    @NonNull String toAnsiString(long currentStyleState);

    /**
     * Outputs the object as an ANSI string, including ANSI escape codes for styles. This method
     * takes into account the provided current style to generate a result that is as efficient as
     * possible in terms of ANSI codes.
     *
     * @param appendable The <code>Appendable</code> to write the ANSI output to.
     * @param currentStyleState The current style to start with.
     * @return The <code>Appendable</code> passed as parameter.
     */
    @NonNull Appendable toAnsi(Appendable appendable, long currentStyleState) throws IOException;
}
