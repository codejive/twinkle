package org.codejive.twinkle.fluent;

/** An interface for possible markup parser implementations that can be used for the `Fluent` */
public interface MarkupParser {

    void parse(Fluent fluent, String textWithMarkup, Object... args);
}
