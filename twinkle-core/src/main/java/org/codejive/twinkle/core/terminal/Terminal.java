package org.codejive.twinkle.core.terminal;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.function.Consumer;
import org.codejive.twinkle.core.util.Size;

public interface Terminal extends AutoCloseable {
    Size size();

    Terminal onResize(Consumer<Size> callback);

    Reader reader();

    PrintWriter writer();
}
