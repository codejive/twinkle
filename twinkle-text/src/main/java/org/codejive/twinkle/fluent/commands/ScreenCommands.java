package org.codejive.twinkle.fluent.commands;

import org.codejive.twinkle.fluent.Fluent;

public interface ScreenCommands {
    /**
     * Clears the entire screen.
     *
     * @return this Fluent instance for chaining
     */
    Fluent clear();

    /**
     * Clears from the cursor position to the end of the screen.
     *
     * @return this Fluent instance for chaining
     */
    Fluent clearToEnd();

    /**
     * Clears from the cursor position to the start of the screen.
     *
     * @return this Fluent instance for chaining
     */
    Fluent clearToStart();

    /**
     * Switches to the alternate screen buffer. Alias for {@link #alternate()}.
     *
     * @return this Fluent instance for chaining
     */
    Fluent alt();

    /**
     * Switches to the alternate screen buffer.
     *
     * @return this Fluent instance for chaining
     */
    Fluent alternate();

    /**
     * Restores the primary screen buffer.
     *
     * @return this Fluent instance for chaining
     */
    Fluent restore();
}
