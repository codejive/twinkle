package org.codejive.context.events;

import org.codejive.context.terminal.Size;

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
