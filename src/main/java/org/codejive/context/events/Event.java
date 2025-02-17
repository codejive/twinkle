package org.codejive.context.events;

public interface Event<T extends EventTarget> {
    T target();
}
