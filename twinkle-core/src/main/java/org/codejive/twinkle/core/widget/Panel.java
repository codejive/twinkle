package org.codejive.twinkle.core.widget;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.text.StyledBuffer;
import org.jspecify.annotations.NonNull;

public interface Panel extends Canvas {

    @NonNull Panel resize(@NonNull Size newSize);

    @Override
    default @NonNull PanelView view(int left, int top, int width, int height) {
        return view(new Rect(left, top, width, height));
    }

    @Override
    @NonNull PanelView view(@NonNull Rect rect);

    String toAnsiString();

    default String toAnsiString(Style currentStyle) {
        return toAnsiString(currentStyle.state());
    }

    String toAnsiString(long currentStyleState);

    static @NonNull Panel of(int width, int height) {
        return of(Size.of(width, height));
    }

    static @NonNull Panel of(@NonNull Size size) {
        return new StyledBufferPanel(size);
    }

    static @NonNull Panel of(@NonNull StyledBuffer buffer) {
        Rect rect = Rect.of(buffer.length(), 1);
        StyledBuffer[] lines = new StyledBuffer[] {buffer};
        return new StyledBufferPanel(rect, lines);
    }
}
