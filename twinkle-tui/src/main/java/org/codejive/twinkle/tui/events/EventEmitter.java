package org.codejive.twinkle.tui.events;

import java.util.ArrayList;
import java.util.List;

public class EventEmitter<T extends Event> {
    private final List<EventListener<T>> listeners = new ArrayList<>();

    public void addListener(EventListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(EventListener<T> listener) {
        listeners.remove(listener);
    }

    public void dispatch(T event) {
        for (EventListener<T> listener : listeners) {
            listener.handleEvent(event);
        }
    }
}
