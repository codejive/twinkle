package org.codejive.context.terminal;

import org.jline.utils.AttributedString;

public interface Canvas extends Rectangular {
    void printAt(int x, int y, AttributedString str);

    void clear();
}
