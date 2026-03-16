package org.codejive.twinkle.screen.io;

import org.codejive.twinkle.ansi.util.AnsiOutputParser;
import org.codejive.twinkle.screen.SwappableBuffer;
import org.jspecify.annotations.NonNull;

public class SwappableBufferWriter extends BufferWriter {

    public SwappableBufferWriter(@NonNull SwappableBuffer buffer) {
        super(buffer);
    }

    @Override
    protected void handleAnsiSequence(String sequence) {
        AnsiOutputParser.parse(sequence, new SwappableHandler());
    }

    protected class SwappableHandler extends Handler {
        private SwappableBuffer sbuffer = (SwappableBuffer) SwappableBufferWriter.this.buffer;

        public boolean onScreenSave() {
            if (sbuffer.save()) {
                sbuffer.clear();
            }
            return true;
        }

        public boolean onScreenSaveAlt() {
            sbuffer.save();
            return true;
        }

        public boolean onScreenRestore() {
            return sbuffer.restore();
        }

        public boolean onScreenRestoreAlt() {
            return sbuffer.restore();
        }
    }
}
