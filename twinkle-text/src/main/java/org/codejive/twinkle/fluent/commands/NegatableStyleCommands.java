package org.codejive.twinkle.fluent.commands;

import org.codejive.twinkle.fluent.Fluent;

public interface NegatableStyleCommands {
    /**
     * Turns bold style on/off. Alias for {@link #bold()}.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent b() {
        return bold();
    }

    /**
     * Turns bold style on/off.
     *
     * @return this Fluent instance for chaining
     */
    Fluent bold();

    /**
     * Turns faint style on/off.
     *
     * @return this Fluent instance for chaining
     */
    Fluent faint();

    /**
     * Turns italic style on/off. Alias for {@link #italic()}.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent i() {
        return italic();
    }

    /**
     * Turns italic style on/off.
     *
     * @return this Fluent instance for chaining
     */
    Fluent italic();

    /**
     * Turns underline style on/off. Alias for {@link #underline()}.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent ul() {
        return underline();
    }

    /**
     * Turns underline style on/off.
     *
     * @return this Fluent instance for chaining
     */
    Fluent underline();

    /**
     * Turns blink style on/off.
     *
     * @return this Fluent instance for chaining
     */
    Fluent blink();

    /**
     * Turns inverse style on/off.
     *
     * @return this Fluent instance for chaining
     */
    Fluent inverse();

    /**
     * Turns invisible style on/off.
     *
     * @return this Fluent instance for chaining
     */
    Fluent hidden();

    /**
     * Turns strikethrough style on/off.
     *
     * @return this Fluent instance for chaining
     */
    default Fluent strike() {
        return strikethrough();
    }

    /**
     * Turns strikethrough style on/off.
     *
     * @return this Fluent instance for chaining
     */
    Fluent strikethrough();
}
