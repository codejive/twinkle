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

    static Terminal getDefault() {
        String prov =
                System.getProperty(
                        "twinkle.terminal.provider", System.getenv("TWINKLE_TERMINAL_PROVIDER"));
        if (prov != null && !prov.isEmpty()) {
            TerminalProvider provider = TerminalProvider.getByName(prov);
            if (provider != null) {
                return provider.get();
            }
        }
        for (TerminalProvider provider : TerminalProvider.getAll()) {
            return provider.get();
        }
        return new DummyTerminal();
    }
}
