package org.codejive.twinkle.core.widget;

import org.codejive.twinkle.core.text.Canvas;
import org.codejive.twinkle.util.StyledIterator;

public interface StringWidget extends Widget {

    CharSequence render();

    default void render(Canvas canvas) {
        StyledIterator iter = StyledIterator.of(render());
        int y = 0;
        while (iter.hasNext()) {
            canvas.putStringAt(0, y++, iter);
        }
    }
}
