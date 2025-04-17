package org.codejive.twinkle.tui.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Reader;
import org.jline.utils.Log;
import org.jline.utils.NonBlockingReader;
import org.jline.utils.Timeout;

public class EventEmittingReader extends NonBlockingReader implements AutoCloseable {
    public static final int READ_EXPIRED = -2;

    private final Reader in;

    private int lastChar = READ_EXPIRED;
    private boolean threadIsReading = false;
    private IOException exception = null;
    private long threadDelay = 60 * 1000;
    private Thread thread;

    public EventEmittingReader(Reader in) {
        this.in = in;
    }

    private synchronized void startReadingThreadIfNeeded() {
        if (thread == null) {
            thread = new Thread(this::run);
            thread.setName("EventEmittingReader non blocking reader thread");
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void close() throws IOException {
        /*
         * The underlying input stream is closed first. This means that if the
         * I/O thread was blocked waiting on input, it will be woken for us.
         */
        in.close();
        if (thread != null) {
            notify();
        }
    }

    @Override
    public synchronized boolean ready() throws IOException {
        return lastChar >= 0 || in.ready();
    }

    @Override
    public int readBuffered(char[] b, int off, int len, long timeout) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || off + len < b.length) {
            throw new IllegalArgumentException();
        } else if (len == 0) {
            return 0;
        } else if (exception != null) {
            assert lastChar == READ_EXPIRED;
            IOException toBeThrown = exception;
            exception = null;
            throw toBeThrown;
        } else if (lastChar >= -1) {
            b[0] = (char) lastChar;
            lastChar = READ_EXPIRED;
            return 1;
        } else if (!threadIsReading && timeout <= 0) {
            return in.read(b, off, len);
        } else {
            // TODO: rework implementation to read as much as possible
            int c = read(timeout, false);
            if (c >= 0) {
                b[off] = (char) c;
                return 1;
            } else {
                return c;
            }
        }
    }

    /**
     * Attempts to read a character from the input stream for a specific period of time.
     *
     * @param timeout The amount of time to wait for the character
     * @return The character read, -1 if EOF is reached, or -2 if the read timed out.
     */
    protected synchronized int read(long timeout, boolean isPeek) throws IOException {
        /*
         * If the thread hit an IOException, we report it.
         */
        if (exception != null) {
            assert lastChar == READ_EXPIRED;
            IOException toBeThrown = exception;
            if (!isPeek) exception = null;
            throw toBeThrown;
        }

        /*
         * If there was a pending character from the thread, then
         * we send it. If the timeout is 0L or the thread was shut down
         * then do a local read.
         */
        if (lastChar >= -1) {
            assert exception == null;
        } else if (!isPeek && timeout <= 0L && !threadIsReading) {
            lastChar = in.read();
        } else {
            /*
             * If the thread isn't reading already, then ask it to do so.
             */
            if (!threadIsReading) {
                threadIsReading = true;
                startReadingThreadIfNeeded();
                notifyAll();
            }

            /*
             * So the thread is currently doing the reading for us. So
             * now we play the waiting game.
             */
            Timeout t = new Timeout(timeout);
            while (!t.elapsed()) {
                try {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    wait(t.timeout());
                } catch (InterruptedException e) {
                    exception = (IOException) new InterruptedIOException().initCause(e);
                }

                if (exception != null) {
                    assert lastChar == READ_EXPIRED;

                    IOException toBeThrown = exception;
                    if (!isPeek) exception = null;
                    throw toBeThrown;
                }

                if (lastChar >= -1) {
                    assert exception == null;
                    break;
                }
            }
        }

        /*
         * ch is the character that was just read. Either we set it because
         * a local read was performed or the read thread set it (or failed to
         * change it).  We will return it's value, but if this was a peek
         * operation, then we leave it in place.
         */
        int ret = lastChar;
        if (!isPeek) {
            lastChar = READ_EXPIRED;
        }
        return ret;
    }

    private void run() {
        Log.debug("NonBlockingReader start");
        boolean needToRead;

        try {
            while (true) {

                /*
                 * Synchronize to grab variables accessed by both this thread
                 * and the accessing thread.
                 */
                synchronized (this) {
                    needToRead = this.threadIsReading;

                    try {
                        /*
                         * Nothing to do? Then wait.
                         */
                        if (!needToRead) {
                            wait(threadDelay);
                        }
                    } catch (InterruptedException e) {
                        /* IGNORED */
                    }

                    needToRead = this.threadIsReading;
                    if (!needToRead) {
                        return;
                    }
                }

                /*
                 * We're not shutting down, but we need to read. This cannot
                 * happen while we are holding the lock (which we aren't now).
                 */
                int charRead = READ_EXPIRED;
                IOException failure = null;
                try {
                    charRead = in.read();
                    //                    if (charRead < 0) {
                    //                        continue;
                    //                    }
                } catch (IOException e) {
                    failure = e;
                    //                    charRead = -1;
                }

                /*
                 * Re-grab the lock to update the state.
                 */
                synchronized (this) {
                    exception = failure;
                    lastChar = charRead;
                    threadIsReading = false;
                    notify();
                }
            }
        } catch (Throwable t) {
            Log.warn("Error in NonBlockingReader thread", t);
        } finally {
            Log.debug("NonBlockingReader shutdown");
            synchronized (this) {
                thread = null;
                threadIsReading = false;
            }
        }
    }
}
