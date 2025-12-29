package org.codejive.twinkle.tui.terminal;

import org.codejive.twinkle.core.widget.Canvas;
import org.codejive.twinkle.tui.events.EventTarget;

public interface Screen extends Canvas, EventTarget, Resizeable {
    void update();
}
