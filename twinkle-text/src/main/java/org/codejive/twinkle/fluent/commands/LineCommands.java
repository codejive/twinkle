package org.codejive.twinkle.fluent.commands;

import org.codejive.twinkle.fluent.Fluent;

public interface LineCommands {
    /**
     * Clears the entire current line.
     *
     * @return this Fluent instance for chaining
     */
    Fluent clear();

    /**
     * Clears from the cursor position to the end of the line.
     *
     * @return this Fluent instance for chaining
     */
    Fluent clearToEnd();

    /**
     * Clears from the cursor position to the start of the line.
     *
     * @return this Fluent instance for chaining
     */
    Fluent clearToStart();
}
