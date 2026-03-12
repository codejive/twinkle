package org.codejive.twinkle.text.util;

import static org.codejive.twinkle.ansi.Constants.*;

import java.io.PrintWriter;
import java.io.Writer;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Constants;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.ansi.util.AnsiTricks;
import org.codejive.twinkle.text.Buffer;
import org.jspecify.annotations.NonNull;

public class BufferWriter extends PrintWriter {
    protected InternalWriter writer;

    public BufferWriter(@NonNull InternalWriter writer) {
        super(writer);
        this.writer = (InternalWriter) super.out;
    }

    protected InternalWriter internalWriter(@NonNull Buffer buffer) {
        return new InternalWriter(buffer);
    }

    protected Rect rect() {
        return writer.buffer.rect();
    }

    public int cursorX() {
        return writer.cursorX >= rect().width() ? rect().width() - 1 : writer.cursorX;
    }

    public int cursorY() {
        return writer.cursorY >= rect().height() ? rect().height() - 1 : writer.cursorY;
    }

    public @NonNull BufferWriter at(int x, int y) {
        writer.at(x, y);
        return this;
    }

    public boolean wrap() {
        return writer.lineWrap;
    }

    public @NonNull BufferWriter wrap(boolean lineWrap) {
        writer.lineWrap = lineWrap;
        return this;
    }

    public @NonNull Style style() {
        return writer.curStyle;
    }

    public @NonNull BufferWriter style(Style style) {
        writer.curStyle = style;
        return this;
    }

    public @NonNull String transparant() {
        return writer.transparantCharacters;
    }

    public @NonNull BufferWriter transparant(String transparantCharacters) {
        writer.transparantCharacters = transparantCharacters;
        return this;
    }

    /**
     * Prints the given text to the buffer. When the text consists of multiple lines, the cursor
     * will each time be moved down from the starting point of the previous line, so that the text
     * will be printed in a block.
     *
     * @param text the text to print
     * @return a reference to this BufferWriter, for chaining
     */
    public @NonNull Buffer printBlock(String text) {
        AnsiTricks.blockify(this, text);
        return writer.buffer;
    }

    public static class InternalWriter extends Writer {
        protected Buffer buffer;
        protected SequenceDecoder decoder;
        private int cursorX;
        private int cursorY;
        private int savedCursorX;
        private int savedCursorY;
        private @NonNull Style curStyle;
        private boolean lineWrap;
        private String transparantCharacters;

        public InternalWriter(@NonNull Buffer buffer) {
            this.buffer = buffer;
            this.decoder = new SequenceDecoder();
            this.cursorX = 0;
            this.cursorY = 0;
            this.savedCursorX = 0;
            this.savedCursorY = 0;
            this.curStyle = Style.DEFAULT;
            this.lineWrap = true;
            this.transparantCharacters = "\0";
        }

        protected Rect rect() {
            return buffer.rect();
        }

        public @NonNull InternalWriter at(int x, int y) {
            flush();
            cursorX = Math.min(rect().width() - 1, Math.max(x, 0));
            cursorY = Math.min(rect().height() - 1, Math.max(y, 0));
            return this;
        }

        @Override
        public void write(char[] cbuf, int off, int len) {
            if (buffer == null) {
                throw new IllegalStateException("BufferWriter is closed");
            }
            for (int i = 0; i < len; i++) {
                char ch = cbuf[off + i];
                if (!decoder.canPush(ch)) {
                    // If the value cannot be pushed to the decoder we need
                    // to flush what's currently in the decoder before we can
                    // append the new value.
                    flush();
                }
                decoder.push(ch);
            }
        }

        @Override
        public void flush() {
            if (buffer == null) {
                throw new IllegalStateException("BufferWriter is closed");
            }
            decoder.finish();
            if (decoder.isReady()) {
                if (decoder.state() == SequenceDecoder.State.ANSI_ESCAPE_SEQUENCE) {
                    handleEscapeSequence(decoder.toString());
                } else if (decoder.codepoint() == '\n') {
                    cursorX = 0;
                    cursorY++;
                } else if (transparantCharacters.indexOf(decoder.codepoint()) >= 0) {
                    // Do not write transparant characters to the buffer, but do update the cursor
                    // position by one, as they should never be wide.
                    cursorX += 1;
                } else {
                    if (lineWrap && cursorX >= rect().width()) {
                        cursorX = 0;
                        cursorY++;
                    }
                    buffer.putCharAt(cursorX, cursorY, curStyle, decoder.toString());
                    cursorX += decoder.width();
                }
            }
            decoder.reset();
        }

        @Override
        public void close() {
            flush();
            buffer = null;
        }

        protected void handleEscapeSequence(String sequence) {
            if (Style.isStyleSequence(sequence)) {
                curStyle = Style.parse(curStyle, sequence);
            } else if (sequence.startsWith(Constants.CSI)) {
                // Handle CSI sequences here
                handleCsiSequence(sequence);
            } else if (sequence.startsWith(Constants.OSC)) {
                // Handle OSC sequences here
                handleOscSequence(sequence);
            } else {
                // Handle any other sequences here
                handleOtherSequence(sequence);
            }
        }

        protected void handleCsiSequence(String sequence) {
            int num;
            if (Ansi.cursorHome().equals(sequence)) {
                cursorX = 0;
                cursorY = 0;
            } else if ((num = numMatch(CURSOR_UP, sequence, 1)) != -1) {
                cursorY = Math.max(0, cursorY - num);
            } else if ((num = numMatch(CURSOR_DOWN, sequence, 1)) != -1) {
                cursorY = Math.min(rect().height() - 1, cursorY + num);
            } else if ((num = numMatch(CURSOR_FORWARD, sequence, 1)) != -1) {
                cursorX = Math.min(rect().width() - 1, cursorX + num);
            } else if ((num = numMatch(CURSOR_BACKWARD, sequence, 1)) != -1) {
                cursorX = Math.max(0, cursorX - num);
            } else if ((CSI + SCREEN_ERASE_FULL).equals(sequence)) {
                buffer.clear();
            } else if ((CSI + SCREEN_ERASE_START).equals(sequence)) {
                buffer.clear(0, 0, cursorX, cursorY);
            } else if ((CSI + SCREEN_ERASE).equals(sequence)
                    || (CSI + SCREEN_ERASE_END).equals(sequence)) {
                buffer.clear(cursorX, cursorY, rect().width() - 1, rect().height() - 1);
            } else if ((CSI + LINE_ERASE_FULL).equals(sequence)) {
                buffer.clear(0, cursorY, rect().width() - 1, cursorY);
            } else if ((CSI + LINE_ERASE_START).equals(sequence)) {
                buffer.clear(0, cursorY, cursorX, cursorY);
            } else if ((CSI + LINE_ERASE).equals(sequence)
                    || (CSI + LINE_ERASE_END).equals(sequence)) {
                buffer.clear(cursorX, cursorY, rect().width() - 1, cursorY);
            } else if (Ansi.autoWrap(true).equals(sequence)) {
                lineWrap = true;
            } else if (Ansi.autoWrap(false).equals(sequence)) {
                lineWrap = false;
            }
        }

        protected void handleOscSequence(String sequence) {}

        protected void handleOtherSequence(String sequence) {
            if (Ansi.cursorSave().equals(sequence)) {
                savedCursorX = cursorX;
                savedCursorY = cursorY;
            } else if (Ansi.cursorRestore().equals(sequence)) {
                cursorX = savedCursorX;
                cursorY = savedCursorY;
            }
        }

        private int numMatch(char cursorCmd, String sequence, int defaultNum) {
            if (sequence.startsWith(Constants.CSI)
                    && sequence.endsWith(String.valueOf(cursorCmd))) {
                String numStr = sequence.substring(Constants.CSI.length(), sequence.length() - 1);
                if (numStr.isEmpty()) {
                    return defaultNum;
                }
                try {
                    return Integer.parseInt(numStr);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
            return -1;
        }
    }
}
