package org.codejive.twinkle.tui.styles;

public enum Type {
    color,
    integer,
    length,
    number,
    percentage,
    string,
    AUTO("auto"),
    HIDDEN("hidden"),
    INHERIT("inherit"),
    INITIAL("initial"),
    NONE("none"),
    REVERT("revert"),
    SOLID("solid"),
    UNSET("unset");

    String literal;

    Type() {}

    Type(String literal) {
        this.literal = literal;
    }
}
