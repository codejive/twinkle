package org.codejive.twinkle.terminal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.function.Consumer;
import org.aesh.terminal.Attributes;
import org.aesh.terminal.tty.Signal;
import org.aesh.terminal.tty.TerminalConnection;
import org.codejive.twinkle.terminal.io.InputReader;
import org.codejive.twinkle.text.Size;

public class AeshTerminal implements Terminal {
    protected final TerminalConnection connection;
    protected final Attributes savedAttributes;
    protected final PrintWriter outputWriter;
    protected final InputReader inputReader;
    protected final Thread thread;

    protected Consumer<Size> resizeCallback;

    public AeshTerminal() throws IOException {
        connection = new TerminalConnection();
        savedAttributes = connection.enterRawMode();
        inputReader = new InputReader();
        outputWriter = new PrintWriter(new OutputWriter(connection), true);
        connection.setSizeHandler(this::handleResize);
        connection.setStdinHandler(this::handleInput);
        connection.setSignalHandler(this::handleSignal);
        connection.openNonBlocking();
        thread = Thread.currentThread();
    }

    @Override
    public Size size() {
        return Size.of(connection.size().getWidth(), connection.size().getHeight());
    }

    @Override
    public Terminal onResize(Consumer<Size> resizeCallback) {
        this.resizeCallback = resizeCallback;
        return this;
    }

    @Override
    public Reader reader() {
        return inputReader;
    }

    @Override
    public PrintWriter writer() {
        return outputWriter;
    }

    @Override
    public void close() throws Exception {
        connection.setAttributes(savedAttributes);
        connection.close();
    }

    protected void handleResize(org.aesh.terminal.tty.Size size) {
        if (resizeCallback != null) {
            resizeCallback.accept(Size.of(size.getWidth(), size.getHeight()));
        }
    }

    protected void handleInput(int[] codePoints) {
        for (int codePoint : codePoints) {
            inputReader.push(codePoint);
        }
    }

    protected void handleSignal(Signal signal) {
        if (signal == Signal.INT) {
            thread.interrupt();
        }
    }

    public static class Provider implements TerminalProvider {
        @Override
        public String name() {
            return "aesh";
        }

        @Override
        public Terminal get() {
            try {
                return new AeshTerminal();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class OutputWriter extends Writer {
        private final TerminalConnection connection;

        public OutputWriter(TerminalConnection connection) {
            this.connection = connection;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            String str = new String(cbuf, off, len);
            connection.write(str);
        }

        @Override
        public void flush() throws IOException {
            // Not needed
        }

        @Override
        public void close() throws IOException {
            connection.close();
        }
    }
}
