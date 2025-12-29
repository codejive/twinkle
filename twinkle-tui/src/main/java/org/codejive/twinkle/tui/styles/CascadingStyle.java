package org.codejive.twinkle.tui.styles;

import java.util.HashSet;
import java.util.Set;

public class CascadingStyle extends Style {
    public final CascadingStyle parent;

    public CascadingStyle(CascadingStyle parent) {
        this.parent = parent;
    }

    public Value get(Property p) {
        Value v = super.get(p);
        if (parent != null && (v == null || v.type() == Type.INHERIT)) {
            v = parent.get(p);
        } else if (v != null && v.type() == Type.INITIAL) {
            v = initial(p);
        } else if (v != null && v.type() == Type.UNSET) {
            return parent != null && p.inherited ? parent.get(p) : initial(p);
        }
        return v;
    }

    public Value initial(Property p) {
        if (parent != null) {
            return parent.initial(p);
        } else {
            return super.get(p);
        }
    }

    public Set<Property> properties() {
        return properties(new HashSet<>());
    }

    private Set<Property> properties(Set<Property> props) {
        if (parent != null) {
            parent.properties(props);
        } else {
            props.addAll(super.properties());
        }
        return props;
    }

    /**
     * This flattens the cascading style hierarchy into a single <code>Style</code> object that
     * takes into account cascading, inheritance and initial values.
     *
     * @return A <code>Style</code> object
     */
    public Style specifiedStyle() {
        return specifiedStyle(new Style());
    }

    private Style specifiedStyle(Style s) {
        if (parent == null) {
            s.propVals.putAll(this.propVals);
        } else {
            parent.specifiedStyle(s);
            for (Property p : properties()) {
                s.put(p, get(p));
            }
        }
        return s;
    }
}
