package org.codejive.context.terminal;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import org.codejive.context.terminal.impl.JlineTerm;

public interface Term extends Flushable, Closeable, Resizeable {
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
