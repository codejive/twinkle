package org.codejive.twinkle.terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public interface TerminalProvider extends Supplier<Terminal> {
    String name();

    static List<TerminalProvider> getAll() {
        ServiceLoader<TerminalProvider> loader = ServiceLoader.load(TerminalProvider.class);
        List<TerminalProvider> providers = new ArrayList<>();
        for (TerminalProvider provider : loader) {
            providers.add(provider);
        }
        return providers;
    }

    static TerminalProvider getByName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Terminal provider name cannot be null or empty");
        }

        String[] names = name.split(",");
        for (String nm : names) {
            nm = nm.trim();
            if ("dummy".equals(nm)) {
                return new DummyTerminal.Provider();
            }
            for (TerminalProvider provider : getAll()) {
                if (nm.equals(provider.name())) {
                    return provider;
                }
            }
        }

        return null;
    }
}
