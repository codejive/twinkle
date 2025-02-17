package org.codejive.context.terminal;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import org.codejive.context.events.EventEmitter;
import org.codejive.context.events.EventTarget;
import org.codejive.context.events.ResizeEvent;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

public class Term implements Flushable, Closeable, Resizeable, EventTarget {
    protected final Terminal terminal;
    protected final EventEmitter<ResizeEvent<Term>> resizeEmitter = new EventEmitter<>();
    protected final Attributes savedAttributes;

    public static Term create() throws IOException {
        return new Term();
    }

    private Term() throws IOException {
        terminal = TerminalBuilder.builder().build();
        savedAttributes = terminal.enterRawMode();
        terminal.handle(Terminal.Signal.WINCH, this::handleResize);
    }

    public Size size() {
        return new Size(terminal.getSize().getColumns(), terminal.getSize().getRows());
    }

    public Integer maxColors() {
        return terminal.getNumericCapability(InfoCmp.Capability.max_colors);
    }

    public Screen fullScreen() {
        return sizedScreen(-1, -1);
    }

    public Screen wideScreen(int height) {
        return sizedScreen(-1, height);
    }

    public Screen sizedScreen(int width, int height) {
        return screen(0, 0, width, height);
    }

    public Screen screen(int left, int top, int width, int height) {
        assert left >= 0;
        assert top >= 0;
        return new BufferedScreen(this, 0, 0, width, height);
    }

    public Input input() {
        return new InputImpl(this);
    }

    @Override
    public void flush() {
        terminal.flush();
    }

    @Override
    public void close() throws IOException {
        terminal.setAttributes(savedAttributes);
        terminal.close();
    }

    protected void handleResize(Terminal.Signal signal) {
        onResize(size());
    }

    @Override
    public void onResize(Size newSize) {
        resizeEmitter.dispatch(new ResizeEvent<>(newSize, this));
    }
}
