package org.codejive.twinkle.tui.terminal;

import org.codejive.twinkle.core.component.Size;

public interface Resizeable {
    void onResize(Size newSize);
}
