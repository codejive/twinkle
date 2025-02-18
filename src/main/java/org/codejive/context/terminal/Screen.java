package org.codejive.context.terminal;

import org.codejive.context.events.EventTarget;

public interface Screen extends Canvas, EventTarget, Resizeable {
    void update();
}
