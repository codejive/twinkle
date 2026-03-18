package org.codejive.twinkle.fluent;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.fluent.commands.ColorCommands;
import org.codejive.twinkle.fluent.commands.CursorCommands;
import org.codejive.twinkle.fluent.commands.LineCommands;
import org.codejive.twinkle.fluent.commands.NegatableCommands;
import org.codejive.twinkle.fluent.commands.ScreenCommands;
import org.codejive.twinkle.fluent.commands.StyleCommands;
import org.codejive.twinkle.fluent.commands.TextualCommands;
import org.codejive.twinkle.fluent.impl.FluentImpl;

public interface Fluent
        extends NegatableCommands, ColorCommands, CursorCommands, StyleCommands, TextualCommands {

    /**
     * Creates a new String-based Fluent instance
     *
     * @return a new Fluent instance
     */
    static Fluent string() {
        return string(Style.DEFAULT);
    }

    /**
     * Creates a new String-based Fluent instance with the given starting style
     *
     * @param startingStyle the style that is currently active
     * @return a new Fluent instance
     */
    static Fluent string(Style startingStyle) {
        return new FluentImpl.StringFluent(startingStyle);
    }

    /**
     * Creates a new Fluent instance that writes to the given Appendable
     *
     * @param appendable the Appendable to write to
     * @return a new Fluent instance
     */
    static Fluent of(Appendable appendable) {
        return of(appendable, Style.DEFAULT);
    }

    /**
     * Creates a new Fluent instance that writes to the given Appendable and starts with the given
     * style
     *
     * @param appendable the Appendable to write to
     * @param startingStyle the style that is currently active
     * @return a new Fluent instance
     */
    static Fluent of(Appendable appendable, Style startingStyle) {
        return FluentImpl.of(appendable, startingStyle);
    }

    /**
     * Sets the Markup parser to use for parsing markup patterns in text. This is useful for
     * allowing users to define their own custom markup syntax.
     *
     * @param markupParser the Markup parser to use
     * @return this Fluent instance for chaining
     */
    Fluent markupParser(MarkupParser markupParser);

    /**
     * Returns the current style combining the styles that have been applied since the creation of
     * this Fluent instance.
     *
     * @return the current style
     */
    Style style();

    /**
     * Begins a negation command, allowing you to specify turn off styles. Examples would be {@code
     * fluent.not().italic()} to turn off italic or {@code fluent.not().underline().not().blink()}
     * to turn off underline and then turn off blink.
     *
     * @return a NegatableCommands instance for chaining a negatable command
     */
    NegatableCommands not();

    /**
     * Restores the style to what it was when this Fluent instance was created.
     *
     * @return this Fluent instance for chaining
     */
    Fluent restore();

    /**
     * The same as {@link #restore()} but meant to signal the end of a fluent chain and therefore
     * returns the underlying Appendable instead of the Fluent instance.
     *
     * @return the underlying Appendable
     */
    Appendable done();

    /**
     * Switches to screen commands, which are used for things like clearing the screen or switching
     * to the alternate buffer.
     *
     * @return a ScreenCommands instance for chaining screen-related commands
     */
    ScreenCommands screen();

    /**
     * Switches to line commands, used for clearing parts of the current line.
     *
     * @return a LineCommands instance for chaining line-related commands
     */
    LineCommands line();
}
