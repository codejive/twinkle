package org.codejive.twinkle.screen.io;

import java.io.Writer;
import org.codejive.twinkle.ansi.Hyperlink;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.ansi.util.AnsiOutputParser;
import org.codejive.twinkle.ansi.util.AnsiOutputParser.AnsiSequenceHandler;
import org.codejive.twinkle.screen.Buffer;
import org.codejive.twinkle.screen.Buffer.LinkPrintOption;
import org.codejive.twinkle.text.Size;
import org.codejive.twinkle.text.UnicodeDecoder;
import org.jspecify.annotations.NonNull;

public class BufferWriter extends Writer {
    protected Buffer buffer;
    protected UnicodeDecoder decoder;
    int cursorX;
    int cursorY;
    private int savedCursorX;
    private int savedCursorY;
    @NonNull Style curStyle;
    Hyperlink currentLink;
    LinkPrintOption linkPrintOption;
    boolean lineWrap;
    @NonNull String transparantCharacters;

    public BufferWriter(@NonNull Buffer buffer) {
        this.buffer = buffer;
        this.decoder = new UnicodeDecoder();
        this.cursorX = 0;
        this.cursorY = 0;
        this.savedCursorX = 0;
        this.savedCursorY = 0;
        this.curStyle = Style.DEFAULT;
        this.currentLink = null;
        this.linkPrintOption = LinkPrintOption.NONE;
        this.lineWrap = true;
        this.transparantCharacters = "\0";
    }

    protected Size size() {
        return buffer.size();
    }

    public @NonNull BufferWriter at(int x, int y) {
        flush();
        cursorX = Math.min(size().width() - 1, Math.max(x, 0));
        cursorY = Math.min(size().height() - 1, Math.max(y, 0));
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
            if (decoder.state() == UnicodeDecoder.ANSI) {
                handleAnsiSequence(decoder.toString());
            } else if (decoder.codepoint() == '\n') {
                cursorX = 0;
                cursorY++;
            } else if (transparantCharacters.indexOf(decoder.codepoint()) >= 0) {
                // Do not write transparant characters to the buffer, but do update the cursor
                // position by one, as they should never be wide.
                cursorX += 1;
            } else {
                if (lineWrap && cursorX >= size().width()) {
                    cursorX = 0;
                    cursorY++;
                }
                buffer.putAt(
                        cursorX,
                        cursorY,
                        decoder.toString(),
                        Buffer.styleOpt(curStyle),
                        linkPrintOption);
                cursorX += decoder.width();
            }
        }
        decoder.reset();
    }

    protected void handleAnsiSequence(String sequence) {
        AnsiOutputParser.parse(sequence, new Handler());
    }

    @Override
    public void close() {
        flush();
        buffer = null;
    }

    protected class Handler implements AnsiSequenceHandler {

        @Override
        public boolean onStyle(String sequence) {
            curStyle = Style.parse(curStyle, sequence);
            return true;
        }

        @Override
        public boolean onHome() {
            cursorX = 0;
            cursorY = 0;
            return true;
        }

        @Override
        public boolean onCursorPosition(int x, int y) {
            cursorY = Math.max(0, y - 1);
            cursorX = Math.max(0, x - 1);
            return true;
        }

        @Override
        public boolean onCursorUp(int num) {
            cursorY = Math.max(0, cursorY - num);
            return true;
        }

        @Override
        public boolean onCursorDown(int num) {

            cursorY = Math.min(size().height() - 1, cursorY + num);
            return true;
        }

        @Override
        public boolean onCursorForward(int num) {
            cursorX = Math.min(size().width() - 1, cursorX + num);
            return true;
        }

        @Override
        public boolean onCursorBackward(int num) {
            cursorX = Math.max(0, cursorX - num);
            return true;
        }

        @Override
        public boolean onCursorNextLine(int num) {
            cursorY = Math.min(size().height() - 1, cursorY + num);
            cursorX = 0;
            return true;
        }

        @Override
        public boolean onCursorPrevLine(int num) {
            cursorY = Math.max(0, cursorY - num);
            cursorX = 0;
            return true;
        }

        @Override
        public boolean onCursorColumn(int num) {
            cursorX = Math.min(size().width() - 1, num - 1);
            return true;
        }

        @Override
        public boolean onScreenEraseFull() {
            buffer.clear();
            return true;
        }

        @Override
        public boolean onScreenEraseStart() {
            buffer.clear(0, 0, cursorX, cursorY);
            return true;
        }

        @Override
        public boolean onScreenEraseEnd() {
            buffer.clear(cursorX, cursorY, size().width() - 1, size().height() - 1);
            return true;
        }

        @Override
        public boolean onLineEraseFull() {
            buffer.clear(0, cursorY, size().width() - 1, cursorY);
            return true;
        }

        @Override
        public boolean onLineEraseStart() {
            buffer.clear(0, cursorY, cursorX, cursorY);
            return true;
        }

        @Override
        public boolean onLineEraseEnd() {
            buffer.clear(cursorX, cursorY, size().width() - 1, cursorY);
            return true;
        }

        @Override
        public boolean onAutoWrap(boolean enabled) {
            lineWrap = enabled;
            return true;
        }

        @Override
        public boolean onHyperlink(Hyperlink link) {
            currentLink = link;
            if (link == Hyperlink.END) {
                linkPrintOption = LinkPrintOption.NONE;
            } else {
                linkPrintOption = new LinkPrintOption(link);
            }
            return true;
        }

        @Override
        public boolean onCursorSave() {
            savedCursorX = cursorX;
            savedCursorY = cursorY;
            return true;
        }

        @Override
        public boolean onCursorRestore() {
            // The limits are in case the screen was resized between save and restore
            cursorX = Math.min(size().width() - 1, Math.max(savedCursorX, 0));
            cursorY = Math.min(size().height() - 1, Math.max(savedCursorY, 0));
            return true;
        }
    }
}
