package org.codejive.twinkle.widgets.list;

import org.codejive.twinkle.core.util.Size;
import org.codejive.twinkle.core.widget.StringWidget;
import org.codejive.twinkle.util.Printable;
import org.jspecify.annotations.NonNull;

public class List implements StringWidget {
    private @NonNull ListModel list;
    private @NonNull ListRenderer renderer;

    protected List(@NonNull ListModel list, @NonNull ListRenderer renderer) {
        this.list = list;
        this.renderer = renderer;
    }

    @Override
    public @NonNull Size size() {
        return null;
    }

    @Override
    public CharSequence render() {
        return renderer.render(list, 0, list.items().size());
    }

    public static @NonNull List create() {
        return new List(ListModel.ofStrings(), ListRenderer.create());
    }

    public static @NonNull List ofStrings(String... texts) {
        return new List(ListModel.ofStrings(texts), ListRenderer.create());
    }

    public static @NonNull List ofPrintables(Printable... texts) {
        return new List(ListModel.ofPrintables(texts), ListRenderer.create());
    }
}
