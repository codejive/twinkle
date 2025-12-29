package org.codejive.twinkle.tui.terminal;

import java.io.Flushable;
import java.io.IOException;
import org.codejive.twinkle.core.widget.Size;
import org.codejive.twinkle.tui.terminal.impl.JlineTerm;

public interface Term extends Flushable, AutoCloseable, Resizeable {
    static Term create() throws IOException {
        return new JlineTerm();
    }

    default Screen fullScreen() {
        return sizedScreen(-1, -1);
    }

    default Screen wideScreen(int height) {
        return sizedScreen(-1, height);
    }

    default Screen sizedScreen(int width, int height) {
        return screen(0, 0, width, height);
    }

    Screen screen(int left, int top, int width, int height);

    Size size();

    Integer maxColors();

    Input input();

    @Override
    void flush();

    @Override
    void close() throws IOException;

    @Override
    void onResize(Size newSize);
}
