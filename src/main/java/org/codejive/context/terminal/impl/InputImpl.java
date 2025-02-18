package org.codejive.context.terminal.impl;

import java.io.IOException;
import org.codejive.context.terminal.Input;
import org.jline.utils.NonBlockingReader;

public class InputImpl implements Input {
    private final JlineTerm term;
    private final NonBlockingReader reader;

    public InputImpl(JlineTerm term) {
        this.term = term;
        this.reader = term.terminal.reader();
    }

    @Override
    public int readChar() throws IOException {
        return readChar(0);
    }

    @Override
    public int readChar(long timeout) throws IOException {
        return reader.read(timeout);
    }
}
