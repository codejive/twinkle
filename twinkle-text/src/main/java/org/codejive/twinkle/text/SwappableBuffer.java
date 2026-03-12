package org.codejive.twinkle.text;

import static org.codejive.twinkle.ansi.Constants.*;

import org.codejive.twinkle.text.util.BufferWriter;
import org.codejive.twinkle.text.util.Size;
import org.jspecify.annotations.NonNull;

/**
 * A buffer that supports swapping between two sets of buffers. This is useful for implementing
 * features like alternate screen buffers.
 */
public class SwappableBuffer extends Buffer {
    private InternalBuffers savedBuffers;
    private InternalBuffers altBuffers;

    public static @NonNull SwappableBuffer of(int width, int height) {
        return of(Size.of(width, height));
    }

    public static @NonNull SwappableBuffer of(@NonNull Size size) {
        return new SwappableBuffer(size);
    }

    protected SwappableBuffer(@NonNull Size size) {
        super(size);
    }

    @Override
    public @NonNull BufferWriter writer() {
        return new BufferWriter(new InternalWriter(this));
    }

    @Override
    public @NonNull SwappableBuffer resize(@NonNull Size newSize) {
        if (savedBuffers == null) {
            buffers = buffers.resize(newSize);
        } else {
            savedBuffers = savedBuffers.resize(newSize);
        }
        if (altBuffers != null) {
            altBuffers = altBuffers.resize(newSize);
        }
        return this;
    }

    /**
     * Saves the current buffer state and switches to the alternate buffer.
     *
     * @return true if the save was successful, false if the aternate buffer is already the active
     *     one (i.e. save was already called without a restore).
     */
    public @NonNull boolean save() {
        if (savedBuffers == null) {
            if (altBuffers == null) {
                altBuffers = new InternalBuffers(size());
            }
            savedBuffers = buffers;
            buffers = altBuffers;
            return true;
        }
        return false;
    }

    /**
     * Restores the previously saved buffer state. If there is no saved state, this method does
     * nothing.
     *
     * @return true if the restore was successful, false if there was no saved state
     */
    public @NonNull boolean restore() {
        if (savedBuffers != null) {
            buffers = savedBuffers;
            savedBuffers = null;
            return true;
        }
        return false;
    }

    private static class InternalWriter extends BufferWriter.InternalWriter {

        public InternalWriter(@NonNull SwappableBuffer buffer) {
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
}
