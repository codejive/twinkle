package org.codejive.twinkle.terminal;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.function.Consumer;
import org.codejive.twinkle.text.Size;

public class DummyTerminal implements Terminal {
    protected Size size;
    protected Consumer<Size> resizeCallback;
    protected PrintWriter writer;
    protected Reader reader;

    public DummyTerminal() {
        this(Size.of(80, 24));
    }

    public DummyTerminal(Size size) {
        this.size = size;
        this.writer = new PrintWriter(System.out, true);
        this.reader = new StringReader("");
    }

    @Override
    public Size size() {
        return size;
    }

    public void resize(Size newSize) {
        this.size = newSize;
        if (resizeCallback != null) {
            resizeCallback.accept(newSize);
        }
    }

    @Override
    public Terminal onResize(Consumer<Size> resizeCallback) {
        this.resizeCallback = resizeCallback;
        return this;
    }

    @Override
    public Reader reader() {
        return reader;
    }

    @Override
    public PrintWriter writer() {
        return writer;
    }

    @Override
    public void close() throws Exception {
        // No resources to close
    }

    public static class Provider implements TerminalProvider {
        @Override
        public String name() {
            return "dummy";
        }

        @Override
        public Terminal get() {
            return new DummyTerminal();
        }
    }
}
