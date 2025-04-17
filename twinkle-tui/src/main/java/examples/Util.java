package examples;

import org.codejive.twinkle.tui.render.Box;
import org.codejive.twinkle.tui.styles.Property;
import org.codejive.twinkle.tui.styles.Style;
import org.codejive.twinkle.tui.styles.Unit;
import org.codejive.twinkle.tui.styles.Value;

class Util {

    static Style pos(int x, int y) {
        Style s = new Style();
        s.putAsEmInt(Property.left, x);
        s.putAsEmInt(Property.top, y);
        return s;
    }

    static Style size(int w, int h) {
        Style s = new Style();
        s.putAsEmInt(Property.width, w);
        s.putAsEmInt(Property.height, h);
        return s;
    }

    static void setSize(Box b, int w, int h) {
        b.style().and(size(w, h));
    }

    static void setRandomPosSize(Box b, int totalW, int totalH) {
        int minx = -b.width();
        int maxx = totalW + b.width();
        int miny = -b.height();
        int maxy = totalH + b.height();
        int x = (int) (Math.random() * (maxx - minx + 1) + minx);
        int y = (int) (Math.random() * (maxy - miny + 1) + miny);
        setPos(b, x, y);
    }

    static void setPos(Box b, int x, int y) {
        int w = b.width();
        int h = b.height();
        Style s = b.style().and(pos(x, y));
        s.put(Property.bottom, Value.length(y + w - 1, Unit.em));
        s.put(Property.right, Value.length(x + h - 1, Unit.em));
        s.put(Property.width, Value.length(w, Unit.em));
        s.put(Property.height, Value.length(h, Unit.em));
    }

    static void setBorderWidth(Box b, int w) {
        b.style().put(Property.border_width, Value.length(w, Unit.em));
    }
}
