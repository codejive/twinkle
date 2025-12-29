package org.codejive.twinkle.tui.ciml.dom;

import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import org.codejive.twinkle.tui.styles.Style;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;

public class ContextElement extends ElementNSImpl {
    private final Style style;

    public static final String CONTEXT_NS = "http://www.codejive.org/ns/2023/03/context";

    protected ContextElement(ContextDocument ownerDocument, String qualifiedName)
            throws DOMException {
        super(ownerDocument, CONTEXT_NS, qualifiedName);
        style = new Style();
    }

    public Style style() {
        return style;
    }

    public ContextElement style(Style style) {
        this.style.and(style);
        return this;
    }

    @Override
    public String getAttribute(String name) {
        if ("style".equalsIgnoreCase(name)) {
            updateStyleAttr(style);
        }
        return super.getAttribute(name);
    }

    @Override
    public String getAttributeNS(String namespaceURI, String name) {
        if (CONTEXT_NS.equals(namespaceURI) && "style".equalsIgnoreCase(name)) {
            updateStyleAttr(style);
        }
        return super.getAttribute(name);
    }

    @Override
    public Attr getAttributeNode(String name) {
        if ("style".equalsIgnoreCase(name)) {
            updateStyleAttr(style);
        }
        return super.getAttributeNode(name);
    }

    @Override
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        if (CONTEXT_NS.equals(namespaceURI) && "style".equalsIgnoreCase(name)) {
            updateStyleAttr(style);
        }
        return super.getAttributeNodeNS(namespaceURI, localName);
    }

    @Override
    public void setAttribute(String name, String value) {
        if ("style".equalsIgnoreCase(name)) {
            updateStyleMap(value);
        }
        super.setAttribute(name, value);
    }

    @Override
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) {
        if (CONTEXT_NS.equals(namespaceURI) && "style".equalsIgnoreCase(name)) {
            updateStyleMap(value);
        }
        super.setAttributeNS(namespaceURI, qualifiedName, value);
    }

    @Override
    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        if ("style".equalsIgnoreCase(name)) {
            updateStyleMap(newAttr.getValue());
        }
        return super.setAttributeNode(newAttr);
    }

    @Override
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        if (CONTEXT_NS.equals(namespaceURI) && "style".equalsIgnoreCase(name)) {
            updateStyleMap(newAttr.getValue());
        }
        return super.setAttributeNodeNS(newAttr);
    }

    private void updateStyleMap(String styles) {
        throw new RuntimeException("NYI");
    }

    private void updateStyleAttr(Style style) {
        throw new RuntimeException("NYI");
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
