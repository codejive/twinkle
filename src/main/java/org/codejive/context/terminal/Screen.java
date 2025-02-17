package org.codejive.context.terminal;

import org.codejive.context.events.EventTarget;
import org.jline.utils.AttributedString;

public interface Screen extends Rectangular, EventTarget, Resizeable {
    void printAt(int x, int y, AttributedString str);

    void clear();

    void update();
}
