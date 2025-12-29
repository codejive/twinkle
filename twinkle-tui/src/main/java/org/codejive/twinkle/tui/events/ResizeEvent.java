package org.codejive.twinkle.tui.events;

import org.codejive.twinkle.core.widget.Size;

public class ResizeEvent<T> implements Event<T> {
    private final T target;
    private final Size size;

    public ResizeEvent(Size size, T target) {
        this.size = size;
        this.target = target;
    }

    public Size size() {
        return size;
    }

    @Override
    public T target() {
        return target;
    }
}
