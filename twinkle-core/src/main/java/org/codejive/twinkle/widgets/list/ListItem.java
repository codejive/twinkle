package org.codejive.twinkle.widgets.list;

import org.codejive.twinkle.util.Printable;

public class ListItem {
    public Printable content;
    public boolean selected;
    public boolean highlighted;

    protected ListItem(Printable content, boolean selected, boolean highlighted) {
        this.content = content;
        this.selected = selected;
        this.highlighted = highlighted;
    }

    public static ListItem of(Printable content) {
        return new ListItem(content, false, false);
    }

    public static ListItem of(Printable content, boolean selected, boolean highlighted) {
        return new ListItem(content, selected, highlighted);
    }
}
