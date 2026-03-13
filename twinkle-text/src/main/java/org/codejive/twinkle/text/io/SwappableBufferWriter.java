package org.codejive.twinkle.text.io;

import static org.codejive.twinkle.ansi.Constants.CSI;
import static org.codejive.twinkle.ansi.Constants.SCREEN_RESTORE;
import static org.codejive.twinkle.ansi.Constants.SCREEN_RESTORE_ALT;
import static org.codejive.twinkle.ansi.Constants.SCREEN_SAVE;
import static org.codejive.twinkle.ansi.Constants.SCREEN_SAVE_ALT;

import org.codejive.twinkle.text.SwappableBuffer;
import org.jspecify.annotations.NonNull;

public class SwappableBufferWriter extends BufferWriter {

    public SwappableBufferWriter(@NonNull SwappableBuffer buffer) {
        super(buffer);
    }

    @Override
    protected void handleCsiSequence(String sequence) {
        SwappableBuffer sbuffer = (SwappableBuffer) this.buffer;
        if ((CSI + SCREEN_SAVE).equals(sequence)) {
            if (sbuffer.save()) {
                sbuffer.clear();
            }
        } else if ((CSI + SCREEN_SAVE_ALT).equals(sequence)) {
            sbuffer.save();
        } else if ((CSI + SCREEN_RESTORE).equals(sequence)
                || (CSI + SCREEN_RESTORE_ALT).equals(sequence)) {
            sbuffer.restore();
        } else {
            super.handleCsiSequence(sequence);
        }
    }
}
