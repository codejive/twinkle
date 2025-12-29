package org.codejive.twinkle.tui.terminal.impl;

import java.io.IOException;
import org.codejive.twinkle.core.widget.Size;
import org.codejive.twinkle.tui.events.EventEmitter;
import org.codejive.twinkle.tui.events.EventTarget;
import org.codejive.twinkle.tui.events.ResizeEvent;
import org.codejive.twinkle.tui.terminal.Input;
import org.codejive.twinkle.tui.terminal.Screen;
import org.codejive.twinkle.tui.terminal.Term;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

public class JlineTerm implements EventTarget, Term {
    protected final Terminal terminal;
    protected final EventEmitter<ResizeEvent<JlineTerm>> resizeEmitter = new EventEmitter<>();
    protected final Attributes savedAttributes;

    public JlineTerm() throws IOException {
        terminal = TerminalBuilder.builder().build();
        savedAttributes = terminal.enterRawMode();
        terminal.handle(Terminal.Signal.WINCH, this::handleResize);
    }

    @Override
    public Screen screen(int left, int top, int width, int height) {
        return new BufferedScreen(this, left, top, width, height);
    }

    @Override
    public Size size() {
        return new Size(terminal.getSize().getColumns(), terminal.getSize().getRows());
    }

    @Override
    public Integer maxColors() {
        return terminal.getNumericCapability(InfoCmp.Capability.max_colors);
    }

    @Override
    public Input input() {
        return new JlineInput(this);
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
