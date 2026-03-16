package org.codejive.twinkle.terminal.io;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class InputReader extends Reader {
    private final LinkedBlockingQueue<Character> events = new LinkedBlockingQueue<>();

    public static final int EOF = -1;
    public static final int TIMEOUT = -2;

    public void push(char ch) {
        events.offer(ch);
    }

    public void push(int codePoint) {
        if (Character.isBmpCodePoint(codePoint)) {
            push((char) codePoint);
        } else {
            // Split code points outside the BMP into surrogate pairs
            char[] chars = Character.toChars(codePoint);
            for (char c : chars) {
                push(c);
            }
        }
    }

    public void push(CharSequence csq) {
        for (int i = 0; i < csq.length(); i++) {
            push(csq.charAt(i));
        }
    }

    @Override
    public boolean ready() throws IOException {
        return !events.isEmpty();
    }

    public int read(int timeout) throws IOException {
        try {
            Character event = events.poll(timeout, TimeUnit.MILLISECONDS);
            if (event == null) {
                return TIMEOUT;
            }
            return event;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return EOF;
        }
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        try {
            int count = 0;
            while (count < len) {
                Character event = (count == 0) ? events.take() : events.poll();
                if (event == null) {
                    break; // No more events available without blocking
                }
                cbuf[off + count] = event.charValue();
                count++;
            }
            return count > 0 ? count : EOF;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return EOF;
        }
    }

    @Override
    public void close() throws IOException {
        // Nothing to do here
    }
}
