package org.codejive.twinkle.text.util;

import java.io.PrintWriter;
import java.io.Writer;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.text.Buffer;
import org.jspecify.annotations.NonNull;

public class BufferWriter extends PrintWriter {
    protected InternalWriter writer;

    public BufferWriter(@NonNull Buffer buffer) {
        super(new InternalWriter(buffer));
        writer = (InternalWriter) super.out;
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

    private static class InternalWriter extends Writer {
        protected Buffer buffer;
        protected SequenceDecoder decoder;
        private int cursorX;
        private int cursorY;
        private @NonNull Style curStyle;
        private boolean lineWrap;
        private String transparantCharacters;

        private InternalWriter(@NonNull Buffer buffer) {
            this.buffer = buffer;
            this.decoder = new SequenceDecoder();
            this.cursorX = 0;
            this.cursorY = 0;
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
            }
        }
    }
}
