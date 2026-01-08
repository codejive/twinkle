package org.codejive.twinkle.widgets.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.codejive.twinkle.core.text.Line;
import org.codejive.twinkle.util.Printable;

public class ListModel {
    private final List<ListItem> items;

    public ListModel() {
        this.items = new ArrayList<>();
    }

    public List<ListItem> items() {
        return items;
    }

    public ListModel add(String text) {
        items.add(ListItem.of(Line.of(text)));
        return this;
    }

    public ListModel add(Printable text) {
        items.add(ListItem.of(text));
        return this;
    }

    public static ListModel ofStrings(String... texts) {
        return ofStrings(Arrays.asList(texts));
    }

    public static ListModel ofStrings(List<String> texts) {
        ListModel model = new ListModel();
        for (String text : texts) {
            model.add(text);
        }
        return model;
    }

    public static ListModel ofPrintables(Printable... texts) {
        return ofPrintables(Arrays.asList(texts));
    }

    public static ListModel ofPrintables(List<Printable> texts) {
        ListModel model = new ListModel();
        for (Printable text : texts) {
            model.add(text);
        }
        return model;
    }
}
