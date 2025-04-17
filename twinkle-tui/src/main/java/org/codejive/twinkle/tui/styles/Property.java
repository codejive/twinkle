package org.codejive.twinkle.tui.styles;

import static org.codejive.twinkle.tui.styles.Type.*;

public enum Property {
    left(false, length, percentage, INHERIT, INITIAL, REVERT, UNSET),
    right(false, length, percentage, INHERIT, INITIAL, REVERT, UNSET),
    top(false, length, percentage, INHERIT, INITIAL, REVERT, UNSET),
    bottom(false, length, percentage, INHERIT, INITIAL, REVERT, UNSET),
    width(false, length, percentage, AUTO, INHERIT, INITIAL, REVERT, UNSET),
    height(false, length, percentage, AUTO, INHERIT, INITIAL, REVERT, UNSET),
    border_width(false, length, INHERIT, INITIAL, REVERT, UNSET),
    border_style(false, SOLID, NONE, HIDDEN, INHERIT, INITIAL, REVERT, UNSET),
    border_color(false, string, INHERIT, INITIAL, REVERT, UNSET),
    border_top_width(false, length, INHERIT, INITIAL, REVERT, UNSET),
    border_top_style(false, SOLID, NONE, HIDDEN, INHERIT, INITIAL, REVERT, UNSET),
    border_top_color(false, string, INHERIT, INITIAL, REVERT, UNSET),
    border_bottom_width(false, length, INHERIT, INITIAL, REVERT, UNSET),
    border_bottom_style(false, SOLID, NONE, HIDDEN, INHERIT, INITIAL, REVERT, UNSET),
    border_bottom_color(false, string, INHERIT, INITIAL, REVERT, UNSET),
    border_left_width(false, length, INHERIT, INITIAL, REVERT, UNSET),
    border_left_style(false, SOLID, NONE, HIDDEN, INHERIT, INITIAL, REVERT, UNSET),
    border_left_color(false, string, INHERIT, INITIAL, REVERT, UNSET),
    border_right_width(false, length, INHERIT, INITIAL, REVERT, UNSET),
    border_right_style(false, SOLID, NONE, HIDDEN, INHERIT, INITIAL, REVERT, UNSET),
    border_right_color(false, string, INHERIT, INITIAL, REVERT, UNSET),
    ;

    public final Type[] types;
    public final boolean inherited;

    Property(boolean inherited, Type... types) {
        this.inherited = inherited;
        this.types = types;
    }
}
