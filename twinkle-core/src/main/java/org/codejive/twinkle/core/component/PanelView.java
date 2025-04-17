package org.codejive.twinkle.core.component;

public interface PanelView extends Panel {
    PanelView moveTo(int x, int y);

    PanelView moveBy(int dx, int dy);
}
