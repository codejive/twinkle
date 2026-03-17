package org.codejive.twinkle.fluent;

/** An interface for possible markup parser implementations that can be used for the `Fluent` */
public interface Markup {

    default String parse(String textWithMarkup) {
        StringBuilder sb = new StringBuilder();
        parse(sb, textWithMarkup);
        return sb.toString();
    }

    void parse(Appendable appendable, String textWithMarkup);
}
