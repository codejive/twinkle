package org.codejive.twinkle.tui.terminal.impl;

import java.io.IOException;
import org.codejive.twinkle.tui.terminal.Input;
import org.jline.utils.NonBlockingReader;

public class JlineInput implements Input {
    private final JlineTerm term;
    private final NonBlockingReader reader;

    public JlineInput(JlineTerm term) {
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
