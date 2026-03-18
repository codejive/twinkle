package org.codejive.twinkle.fluent.commands;

import org.codejive.twinkle.ansi.util.Printable;
import org.codejive.twinkle.fluent.Fluent;
import org.jspecify.annotations.NonNull;

public interface TextualCommands {
    /**
     * Like {@link #text(Object, Object...)}, but for Printables. This will write the ANSI
     * representation of the Printable as text.
     *
     * @param prt the Printable to write as text
     * @return this Fluent instance for chaining
     */
    default Fluent text(@NonNull Printable prt) {
        return block(prt);
    }

    /**
     * Writes the string representation of the given object, applying any active styles. The string
     * can include format specifiers, which will be replaced by the corresponding arguments. See
     * {@link String#format(String, Object...)} for details on the supported format.
     *
     * @param obj the text or format string
     * @param args the arguments for the format string
     * @return this Fluent instance for chaining
     */
    Fluent text(@NonNull Object obj, Object... args);

    /**
     * Writes the given string
     *
     * @param text the text to output
     * @return this Fluent instance for chaining
     */
    Fluent plain(@NonNull String text);

    Fluent markup(@NonNull Object obj, Object... args);

    /**
     * Like {@link #block(Object, Object...)}, but for Printables. This will write the ANSI
     * representation of the Printable as a block.
     *
     * @param prt the Printable to write as a block
     * @return this Fluent instance for chaining
     */
    Fluent block(@NonNull Printable prt);

    /**
     * Writes the string representation of the given object as a block, meaning that any newlines
     * encountered in the text will return the the original column the cursor was at when the block
     * command was called. This is useful for writing multi-line text while maintaining the same
     * indentation. Any active styles will be applied to the entire text block. The string can
     * include format specifiers, which will be replaced by the corresponding arguments. See {@link
     * String#format(String, Object...)} for details on the supported format.
     *
     * @param obj the text or format string
     * @param args the arguments for the format string
     * @return this Fluent instance for chaining
     */
    Fluent block(@NonNull Object obj, Object... args);

    /**
     * Appends a linefeed (newline)
     *
     * @return this Fluent instance for chaining
     */
    Fluent lf();

    /**
     * Writes a hyperlink with the given URL and link text. This is the equivalent of calling {@code
     * url(url).text(text).lru()}.
     *
     * @param text the text to display for the hyperlink
     * @param url the URL of the hyperlink
     * @return this Fluent instance for chaining
     */
    Fluent link(@NonNull String text, @NonNull String url);

    /**
     * Writes a hyperlink with the given URL and link text. This is the equivalent of calling {@code
     * url(url, id).text(text).lru()}.
     *
     * @param text the text to display for the hyperlink
     * @param url the URL of the hyperlink
     * @param id the ID for the hyperlink
     * @return this Fluent instance for chaining
     */
    Fluent link(@NonNull String text, @NonNull String url, String id);
}
