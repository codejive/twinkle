package org.codejive.twinkle.terminal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.function.Consumer;
import org.codejive.twinkle.core.terminal.Terminal;
import org.codejive.twinkle.core.terminal.TerminalProvider;
import org.codejive.twinkle.core.util.Size;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal.Signal;
import org.jline.terminal.TerminalBuilder;

public class JLineTerminal implements Terminal {
    protected final org.jline.terminal.Terminal terminal;
    protected final Attributes savedAttributes;
    protected Consumer<Size> resizeCallback;

    public JLineTerminal() throws IOException {
        terminal = TerminalBuilder.builder().build();
        savedAttributes = terminal.enterRawMode();
        terminal.handle(org.jline.terminal.Terminal.Signal.WINCH, this::handleResize);
    }

    @Override
    public Size size() {
        return Size.of(terminal.getSize().getColumns(), terminal.getSize().getRows());
    }

    @Override
    public Terminal onResize(Consumer<Size> resizeCallback) {
        this.resizeCallback = resizeCallback;
        return this;
    }

    @Override
    public Reader reader() {
        return terminal.reader();
    }

    @Override
    public PrintWriter writer() {
        return terminal.writer();
    }

    @Override
    public void close() throws Exception {
        terminal.flush();
        terminal.setAttributes(savedAttributes);
        terminal.close();
    }

    protected void handleResize(Signal signal) {
        if (resizeCallback != null) {
            resizeCallback.accept(size());
        }
    }

    public static class Provider implements TerminalProvider {
        @Override
        public String name() {
            return "jline";
        }

        @Override
        public Terminal get() {
            try {
                return new JLineTerminal();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
