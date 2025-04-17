package org.codejive.twinkle.tui.ciml.dom;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import org.codejive.twinkle.tui.styles.Style;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

public class ContextDocument extends DocumentImpl {
    @Override
    public Element createElement(String tagName) throws DOMException {
        return super.createElement(tagName);
    }

    public ScreenElement createScreenElement() {
        return new ScreenElement(this);
    }

    public PanelElement createPanelElement() {
        return new PanelElement(this);
    }

    public <T extends ContextElement> T append(T elem) {
        appendChild(elem);
        return elem;
    }

    public <T extends ContextElement> T append(T elem, Style style) {
        appendChild(elem.style(style));
        return elem;
    }
}
