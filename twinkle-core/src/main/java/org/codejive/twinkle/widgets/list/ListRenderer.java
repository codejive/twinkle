package org.codejive.twinkle.widgets.list;

import java.util.stream.Collectors;
import org.codejive.twinkle.ansi.Ansi;
import org.jspecify.annotations.NonNull;

public class ListRenderer {
    private final @NonNull ListConfig config;

    protected ListRenderer(@NonNull ListConfig config) {
        this.config = config;
    }

    public CharSequence render(@NonNull ListModel list, int startIndex, int count) {
        int endIndex = Math.min(startIndex + count, list.items().size());
        return list.items().subList(startIndex, endIndex).stream()
                .map(this::renderItem)
                .collect(Collectors.joining("\n"));
    }

    public CharSequence renderItem(@NonNull ListItem item) {
        String content =
                Ansi.STYLE_RESET
                        + config.listbackgroundColor.toAnsiBg()
                        + config.itemStyle.toAnsiString();
        if (item.selected) {
            content += config.selectedItemStyle.toAnsiString();
        }
        if (item.highlighted) {
            content += config.highlightedItemStyle.toAnsiString();
        }
        content += item.content.toAnsiString();
        return content;
    }

    public static @NonNull ListRenderer create() {
        return new ListRenderer(new ListConfig());
    }
}
