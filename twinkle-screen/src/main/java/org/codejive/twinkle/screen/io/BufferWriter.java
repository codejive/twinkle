package org.codejive.twinkle.screen.io;

import static org.codejive.twinkle.ansi.Constants.*;

import java.io.Writer;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.screen.Buffer;
import org.codejive.twinkle.screen.Buffer.LinkPrintOption;
import org.codejive.twinkle.text.Hyperlink;
import org.codejive.twinkle.text.SequenceDecoder;
import org.codejive.twinkle.text.Size;
import org.jspecify.annotations.NonNull;

public class BufferWriter extends Writer {
    protected Buffer buffer;
    protected SequenceDecoder decoder;
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
        this.decoder = new SequenceDecoder();
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

    @Override
    public void close() {
        flush();
        buffer = null;
    }

    protected void handleEscapeSequence(String sequence) {
        if (Style.isStyleSequence(sequence)) {
            curStyle = Style.parse(curStyle, sequence);
        } else if (sequence.startsWith(CSI)) {
            // Handle CSI sequences here
            handleCsiSequence(sequence);
        } else if (sequence.startsWith(OSC)) {
            // Handle OSC sequences here
            handleOscSequence(sequence);
        } else {
            // Handle any other sequences here
            handleOtherSequence(sequence);
        }
    }

    protected void handleCsiSequence(String sequence) {
        int num;
        int[] nums;

        if (Ansi.cursorHome().equals(sequence)) {
            cursorX = 0;
            cursorY = 0;
        } else if ((nums = numsMatch(CURSOR_POSITION, sequence, null)) != null) {
            cursorY = Math.max(0, nums[0] - 1);
            cursorX = Math.max(0, nums[1] - 1);
        } else if ((num = numMatch(CURSOR_UP, sequence, 1)) != -1) {
            cursorY = Math.max(0, cursorY - num);
        } else if ((num = numMatch(CURSOR_DOWN, sequence, 1)) != -1) {
            cursorY = Math.min(size().height() - 1, cursorY + num);
        } else if ((num = numMatch(CURSOR_FORWARD, sequence, 1)) != -1) {
            cursorX = Math.min(size().width() - 1, cursorX + num);
        } else if ((num = numMatch(CURSOR_BACKWARD, sequence, 1)) != -1) {
            cursorX = Math.max(0, cursorX - num);
        } else if ((num = numMatch(CURSOR_NEXT_LINE, sequence, 1)) != -1) {
            cursorY = Math.min(size().height() - 1, cursorY + num);
            cursorX = 0;
        } else if ((num = numMatch(CURSOR_PREV_LINE, sequence, 1)) != -1) {
            cursorY = Math.max(0, cursorY - num);
            cursorX = 0;
        } else if ((num = numMatch(CURSOR_COLUMN, sequence, 1)) != -1) {
            cursorX = Math.min(size().width() - 1, num - 1);
        } else if ((CSI + SCREEN_ERASE_FULL).equals(sequence)) {
            buffer.clear();
        } else if ((CSI + SCREEN_ERASE_START).equals(sequence)) {
            buffer.clear(0, 0, cursorX, cursorY);
        } else if ((CSI + SCREEN_ERASE).equals(sequence)
                || (CSI + SCREEN_ERASE_END).equals(sequence)) {
            buffer.clear(cursorX, cursorY, size().width() - 1, size().height() - 1);
        } else if ((CSI + LINE_ERASE_FULL).equals(sequence)) {
            buffer.clear(0, cursorY, size().width() - 1, cursorY);
        } else if ((CSI + LINE_ERASE_START).equals(sequence)) {
            buffer.clear(0, cursorY, cursorX, cursorY);
        } else if ((CSI + LINE_ERASE).equals(sequence) || (CSI + LINE_ERASE_END).equals(sequence)) {
            buffer.clear(cursorX, cursorY, size().width() - 1, cursorY);
        } else if (Ansi.autoWrap(true).equals(sequence)) {
            lineWrap = true;
        } else if (Ansi.autoWrap(false).equals(sequence)) {
            lineWrap = false;
        }
    }

    protected void handleOscSequence(String sequence) {
        // Handle hyperlink sequences here
        Hyperlink link = Hyperlink.parse(sequence);
        if (link != null) {
            currentLink = link;
            if (link == Hyperlink.END) {
                linkPrintOption = LinkPrintOption.NONE;
            } else {
                linkPrintOption = new LinkPrintOption(link);
            }
        }
    }

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
        if (sequence.startsWith(CSI) && sequence.endsWith(String.valueOf(cursorCmd))) {
            String numStr = sequence.substring(CSI.length(), sequence.length() - 1);
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

    private int[] numsMatch(char cursorCmd, String sequence, int[] defaultNums) {
        if (sequence.startsWith(CSI) && sequence.endsWith(String.valueOf(cursorCmd))) {
            String numsStr = sequence.substring(CSI.length(), sequence.length() - 1);
            if (numsStr.isEmpty()) {
                return defaultNums;
            }
            String[] parts = numsStr.split(";");
            int nums[] = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                try {
                    nums[i] = Integer.parseInt(parts[i]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return nums;
        }
        return null;
    }
}
