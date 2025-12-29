package org.codejive.twinkle.tui.terminal;

import org.codejive.twinkle.core.widget.Size;

public interface Resizeable {
    void onResize(Size newSize);
}
