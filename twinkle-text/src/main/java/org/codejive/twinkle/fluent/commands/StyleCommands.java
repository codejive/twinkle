package org.codejive.twinkle.fluent.commands;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.fluent.Fluent;
import org.jspecify.annotations.NonNull;

public interface StyleCommands extends NegatableStyleCommands, ColorCommands {
    /**
     * Applies the given style by calculating the difference between the new style and the current
     * style. This will output only the necessary ANSI codes to transition from the current style to
     * the new style.
     *
     * @param newStyle the new style to apply
     * @return this Fluent instance for chaining
     */
    Fluent style(Style newStyle);

    /**
     * Pushes the current style onto the style stack. This allows you to save the current style
     * before applying a new one, and then later call {@link #pop()} to return to the previous
     * style.
     *
     * @return this Fluent instance for chaining
     */
    Fluent push();

    /**
     * Pushes the current style onto the style stack and applies the given new style. This is a
     * convenience method that combines the functionality of {@link #push()} and {@link
     * #style(Style)}.
     *
     * @param newStyle the new style to apply
     * @return this Fluent instance for chaining
     */
    Fluent push(Style newStyle);

    /**
     * Pops the most recently pushed style from the style stack and applies it. If the style stack
     * is empty, this method does nothing.
     *
     * @return this Fluent instance for chaining
     */
    Fluent pop();

    /**
     * Resets to the default style.
     *
     * @return this Fluent instance for chaining
     */
    Fluent reset();

    /**
     * Applies the normal style.
     *
     * @return this Fluent instance for chaining
     */
    Fluent normal();

    /**
     * Begins a ackground color command chain, allowing you to specify a background color in the
     * same fluent style as the foreground color commands. Alias for {@link #background()}.
     *
     * @return a BackgroundColors instance for chaining background color commands
     */
    default ColorCommands bg() {
        return background();
    }

    /**
     * Begins a ackground color command chain, allowing you to specify a background color in the
     * same fluent style as the foreground color commands.
     *
     * @return a BackgroundColors instance for chaining background color commands
     */
    ColorCommands background();

    /**
     * Begins a hyperlink with the given URL. The link will continue until {@link #lru()} is called.
     *
     * @param url the URL of the hyperlink
     * @return this Fluent instance for chaining
     */
    Fluent url(@NonNull String url);

    /**
     * Begins a hyperlink with the given URL and ID. The link will continue until {@link #lru()} is
     * called. The ID can be used by some terminal emulators to identify the link.
     *
     * @param url the URL of the hyperlink
     * @param id an optional ID for the hyperlink
     * @return this Fluent instance for chaining
     */
    Fluent url(@NonNull String url, String id);

    /**
     * Ends the current hyperlink. This should be called after {@link #url(String)} to properly
     * close the hyperlink.
     *
     * @return this Fluent instance for chaining
     */
    Fluent lru();
}
