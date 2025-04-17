package org.codejive.twinkle.tui.util;

import java.util.Arrays;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

/**
 * Provides an appendable buffer of lines with a fixed size. New lines that get added will push
 * existing lines "upward". Once the buffer is full new lines will push the oldest lines out of the
 * buffer.
 */
public class ScrollBuffer implements Appendable {
    private AttributedString[] history = new AttributedString[0];
    private final AttributedStringBuilder lastLine = new AttributedStringBuilder();
    private int historySize;
    private int maxLineLength;
    private boolean wrap;

    public ScrollBuffer(int historySize, int maxLineLength, boolean wrap) {
        setHistorySize(historySize);
        this.maxLineLength = maxLineLength;
        this.wrap = wrap;
    }

    public int getHistorySize() {
        return historySize;
    }

    public void setHistorySize(int historySize) {
        assert historySize > 0;
        if ((historySize - 1) > history.length) {
            // The buffer only grows, we never shrink it
            int oldlen = history.length;
            history = Arrays.copyOf(history, historySize - 1);
            for (int i = oldlen; i < history.length; i++) {
                history[i] = new AttributedString("");
            }
        }
        this.historySize = historySize;
    }

    public int getMaxLineLength() {
        return maxLineLength;
    }

    public void setMaxLineLength(int maxLineLength) {
        this.maxLineLength = maxLineLength;
    }

    public boolean getWrap() {
        return wrap;
    }

    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }

    public void clear() {
        history = new AttributedString[0];
    }

    public AttributedString[] getLines() {
        int n = getHistorySize();
        AttributedString[] result = new AttributedString[n];
        for (int i = 0; i < n - 1; i++) {
            result[i] = history[n - i - 2];
        }
        result[n - 1] = lastLine.toAttributedString();
        return result;
    }

    protected void addLine(AttributedString line) {
        if (history.length > 0) {
            System.arraycopy(history, 0, history, 1, history.length - 1);
            history[0] = line;
        }
    }

    protected void pushLastLine() {
        addLine(lastLine.toAttributedString());
        lastLine.setLength(0);
    }

    @Override
    public Appendable append(CharSequence csq) {
        if (csq == null) {
            // According to Appendable
            csq = "null";
        }
        for (int i = 0; i < csq.length(); i++) {
            append(csq.charAt(i));
        }
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) {
        if (csq == null) {
            // According to Appendable
            return append(csq);
        }
        for (int i = start; i < end; i++) {
            append(csq.charAt(i));
        }
        return this;
    }

    @Override
    public Appendable append(char c) {
        if (c == '\r') {
            // Ignore CRs
        } else if (c != '\n') {
            if (lastLine.length() >= maxLineLength && wrap) {
                pushLastLine();
            }
            if (maxLineLength == -1 || lastLine.length() < maxLineLength) {
                lastLine.append(c);
            } else {
                // We simply ignore any characters past the max line length
            }
        } else {
            pushLastLine();
        }
        return this;
    }

    public static class ThreadSafe extends ScrollBuffer {

        public ThreadSafe(int historySize, int maxLineLength, boolean wrap) {
            super(historySize, maxLineLength, wrap);
        }

        public synchronized void setHistorySize(int historySize) {
            super.setHistorySize(historySize);
        }

        public synchronized void clear() {
            super.clear();
        }

        public synchronized AttributedString[] getLines() {
            return super.getLines();
        }

        protected void addLine(AttributedString line) {
            super.addLine(line);
        }

        protected synchronized void pushLastLine() {
            super.pushLastLine();
        }

        @Override
        public synchronized Appendable append(char c) {
            return super.append(c);
        }
    }
}
