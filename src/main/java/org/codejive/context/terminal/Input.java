package org.codejive.context.terminal;

import java.io.IOException;

public interface Input {
    int readChar() throws IOException;

    int readChar(long timeout) throws IOException;
}
