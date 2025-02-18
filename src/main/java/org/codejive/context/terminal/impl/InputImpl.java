package org.codejive.context.terminal.impl;

import org.codejive.context.terminal.Input;
import org.codejive.context.terminal.Term;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;

public class InputImpl implements Input {
    private final Term term;
    private final NonBlockingReader reader;

    public InputImpl(Term term) {
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
