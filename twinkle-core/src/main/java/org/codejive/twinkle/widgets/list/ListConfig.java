package org.codejive.twinkle.widgets.list;

import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Style;
import org.jspecify.annotations.NonNull;

public class ListConfig {
    public @NonNull Color listbackgroundColor = Color.DEFAULT;
    public @NonNull Style itemStyle = Style.DEFAULT;
    public @NonNull Style selectedItemStyle = Style.INVERSE;
    public @NonNull Style highlightedItemStyle = Style.FAINT;
}
