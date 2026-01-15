package org.codejive.twinkle.core.terminal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.core.util.Size;

public class RobotTerminal implements Terminal {
    private final Deque<Action> actions;
    private final Terminal terminal;
    private final RobotTerminalReader reader;
    private Size sizeOverride;
    private Consumer<Size> resizeCallback;
    private Thread actionThread;

    public RobotTerminal() {
        this(Terminal.getDefault());
    }

    public RobotTerminal(Size size) {
        this(new DummyTerminal(size));
    }

    public RobotTerminal(Terminal terminal) {
        this.actions = new ConcurrentLinkedDeque<>();
        this.terminal = terminal;
        this.reader = new RobotTerminalReader();
        this.resizeCallback = (Size) -> {};
        start();
    }

    interface Action {
        void perform();
    }

    class KeyAction implements Action {
        private final char key;

        KeyAction(char key) {
            this.key = key;
        }

        @Override
        public void perform() {
            reader.push(key);
        }
    }

    static class DelayAction implements Action {
        private final int delay;

        DelayAction(int delay) {
            this.delay = delay;
        }

        @Override
        public void perform() {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class ResizeAction implements Action {
        private final Size newSize;

        ResizeAction(Size newSize) {
            this.newSize = newSize;
        }

        @Override
        public void perform() {
            sizeOverride = newSize;
            resizeCallback.accept(newSize);
        }
    }

    public RobotTerminal key(char c) {
        actions.add(new KeyAction(c));
        return this;
    }

    public RobotTerminal resize(Size newSize) {
        actions.add(new ResizeAction(newSize));
        return this;
    }

    public RobotTerminal delay(int millis) {
        actions.add(new DelayAction(millis));
        return this;
    }

    @Override
    public Size size() {
        return sizeOverride != null ? sizeOverride : terminal.size();
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
        return terminal.writer();
    }

    public void start() {
        if (actionThread != null) {
            return;
        }
        actionThread =
                new Thread(
                        () -> {
                            while (true) {
                                Action action = actions.poll();
                                if (action != null) {
                                    action.perform();
                                } else {
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        break;
                                    }
                                }
                            }
                        });
        actionThread.start();
    }

    public void stop() {
        if (actionThread != null) {
            actionThread.interrupt();
            try {
                actionThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            actionThread = null;
        }
    }

    @Override
    public void close() {
        stop();
        writer().print(Ansi.STYLE_RESET + Ansi.showCursor());
        writer().flush();
    }

    static class RobotTerminalReader extends Reader {
        private final Queue<Character> buffer = new LinkedList<>();
        private boolean closed = false;

        public void push(char c) {
            synchronized (buffer) {
                if (closed) {
                    throw new IllegalStateException("Reader is closed");
                }
                buffer.offer(c);
                buffer.notifyAll();
            }
        }

        public void push(String s) {
            for (char c : s.toCharArray()) {
                push(c);
            }
        }

        @Override
        public boolean ready() throws IOException {
            return !buffer.isEmpty();
        }

        @Override
        public int read() throws IOException {
            synchronized (buffer) {
                if (closed) {
                    return -1;
                }
                while (buffer.isEmpty() && !closed) {
                    try {
                        buffer.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Read interrupted", e);
                    }
                }
                if (closed && buffer.isEmpty()) {
                    return -1;
                }
                return buffer.poll();
            }
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (cbuf == null) {
                throw new NullPointerException();
            }
            if (off < 0 || len < 0 || len > cbuf.length - off) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0) {
                return 0;
            }

            synchronized (buffer) {
                if (closed && buffer.isEmpty()) {
                    return -1;
                }

                int count = 0;
                while (count < len) {
                    if (buffer.isEmpty()) {
                        if (count > 0) {
                            return count;
                        }
                        if (closed) {
                            return -1;
                        }
                        try {
                            buffer.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new IOException("Read interrupted", e);
                        }
                        if (closed && buffer.isEmpty()) {
                            return count > 0 ? count : -1;
                        }
                    } else {
                        cbuf[off + count] = buffer.poll();
                        count++;
                    }
                }
                return count;
            }
        }

        @Override
        public void close() throws IOException {
            synchronized (buffer) {
                closed = true;
                buffer.notifyAll();
            }
        }
    }
}
