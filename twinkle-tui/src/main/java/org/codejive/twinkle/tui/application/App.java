package org.codejive.twinkle.tui.application;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Consumer;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.core.terminal.Terminal;
import org.codejive.twinkle.core.text.Buffer;
import org.codejive.twinkle.core.widget.Renderable;
import org.jspecify.annotations.NonNull;

public class App {
    private boolean fullScreen;
    private boolean mouseSupport;
    private boolean quitOnQ;
    private boolean quitOnEsc;
    private Integer limitFps;
    private @NonNull Consumer<App> onFrame;
    private Renderable renderable;
    private Terminal terminal;

    public static App using(Renderable renderable) {
        return new App().renderable(renderable);
    }

    public static App using(Consumer<App> onFrame) {
        return new App().onFrame(onFrame);
    }

    private App() {
        this.onFrame = this::onFrameHandler;
    }

    public static void run(Renderable renderable) throws Exception {
        using(renderable).quitOnQ(true).quitOnEsc(true).start();
    }

    public boolean fullScreen() {
        return fullScreen;
    }

    public @NonNull App fullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
        return this;
    }

    public boolean mouseSupport() {
        return mouseSupport;
    }

    public @NonNull App mouseSupport(boolean mouseSupport) {
        this.mouseSupport = mouseSupport;
        return this;
    }

    public boolean quitOnQ() {
        return quitOnQ;
    }

    public @NonNull App quitOnQ(boolean quitOnQ) {
        this.quitOnQ = quitOnQ;
        return this;
    }

    public boolean quitOnEsc() {
        return quitOnEsc;
    }

    public @NonNull App quitOnEsc(boolean quitOnEsc) {
        this.quitOnEsc = quitOnEsc;
        return this;
    }

    public Integer limitFps() {
        return limitFps;
    }

    public @NonNull App limitFps(Integer limitFps) {
        this.limitFps = limitFps;
        return this;
    }

    public @NonNull App renderable(Renderable renderable) {
        this.renderable = renderable;
        this.onFrame = this::onFrameHandler;
        return this;
    }

    public @NonNull App onFrame(Consumer<App> onFrame) {
        this.onFrame = onFrame;
        this.renderable = null;
        return this;
    }

    public Terminal terminal() {
        return terminal;
    }

    public void start() throws Exception {
        try (Terminal term = Terminal.getDefault()) {
            this.terminal = term;
            System.out.print(Ansi.hideCursor() + Ansi.autoWrap(false));
            onFrame.accept(this);
        } finally {
            this.terminal = null;
            System.out.print(Ansi.showCursor() + Ansi.autoWrap(true));
        }
    }

    private void onFrameHandler(App app) {
        if (renderable != null) {
            try {
                animate(renderable);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void animate(Renderable renderable) throws IOException, InterruptedException {
        Buffer buf = Buffer.of(terminal.size());
        Reader rdr = terminal.reader();

        long targetFrameTimeNanos = limitFps != null ? 1_000_000_000L / limitFps : 0;

        while (true) {
            long frameStartTime = System.nanoTime();

            while (rdr.ready()) {
                int ch = rdr.read();
                if (quitOnQ && (ch == 'q' || ch == 'Q')) {
                    return;
                }
                if (quitOnEsc && ch == Ansi.ESC) {
                    return;
                }
            }

            drawFrame(buf, renderable);

            if (limitFps != null) {
                long elapsedNanos = System.nanoTime() - frameStartTime;
                long remainingNanos = targetFrameTimeNanos - elapsedNanos;

                if (remainingNanos > 0) {
                    long sleepMillis = remainingNanos / 1_000_000;
                    int sleepNanos = (int) (remainingNanos % 1_000_000);
                    Thread.sleep(sleepMillis, sleepNanos);
                }
            }
        }
    }

    public void drawFrame(Buffer buf, Renderable renderable) {
        buf.resize(terminal.size());
        renderable.render(buf);

        System.out.print(Ansi.hideCursor() + Ansi.autoWrap(false));
        System.out.print(Ansi.cursorHome());
        System.out.print(buf.toAnsiString());
    }
}
