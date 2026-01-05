package org.codejive.twinkle.util;

public class Unicode {
    public static int visibleWidth(CharSequence text) {
        int width = 0;
        SequenceIterator si = SequenceIterator.of(text);
        while (si.hasNext()) {
            si.next();
            width += si.width();
        }
        return width;
    }
}
