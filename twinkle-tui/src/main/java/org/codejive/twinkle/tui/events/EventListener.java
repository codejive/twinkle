package org.codejive.twinkle.tui.events;

public interface EventListener<T extends Event> {
    void handleEvent(T event);
}
