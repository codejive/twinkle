package org.codejive.twinkle.core.terminal;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.function.Consumer;
import org.codejive.twinkle.core.util.Size;

public class DummyTerminal implements Terminal {
    protected Size size;
    protected Consumer<Size> resizeCallback;

    public DummyTerminal() {
        this(Size.of(80, 24));
    }

    public DummyTerminal(Size size) {
        this.size = size;
    }

    @Override
    public Size size() {
        return Size.of(80, 24);
    }

    public void resize(Size newSize) {
        this.size = newSize;
        if (resizeCallback != null) {
            resizeCallback.accept(newSize);
        }
    }

    @Override
    public Terminal onResize(Consumer<Size> resizeCallback) {
        return this;
    }

    @Override
    public Reader reader() {
        return new StringReader("");
    }

    @Override
    public PrintWriter writer() {
        return new PrintWriter(System.out);
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
