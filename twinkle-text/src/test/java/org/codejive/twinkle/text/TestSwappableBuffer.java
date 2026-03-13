package org.codejive.twinkle.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codejive.twinkle.ansi.Constants.*;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.text.io.PrintBufferWriter;
import org.junit.jupiter.api.Test;

public class TestSwappableBuffer {

    @Test
    public void testDirectSaveAndRestore() {
        SwappableBuffer buffer = SwappableBuffer.of(10, 3);

        // Write some content to the buffer
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("hello");
        }

        // Verify initial content
        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("h");
        assertThat(buffer.graphemeAt(4, 0)).isEqualTo("o");

        // Save should return true on first call
        assertThat(buffer.save()).isTrue();

        // Write different content to the alternate buffer
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("world");
        }

        // Verify alternate content
        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("w");
        assertThat(buffer.graphemeAt(4, 0)).isEqualTo("d");

        // Restore should return true
        assertThat(buffer.restore()).isTrue();

        // Verify original content is restored
        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("h");
        assertThat(buffer.graphemeAt(4, 0)).isEqualTo("o");
    }

    @Test
    public void testSaveReturnsFalseWhenAlreadySaved() {
        SwappableBuffer buffer = SwappableBuffer.of(10, 3);

        // First save should succeed
        assertThat(buffer.save()).isTrue();

        // Second save should return false (already saved)
        assertThat(buffer.save()).isFalse();
    }

    @Test
    public void testRestoreReturnsFalseWhenNotSaved() {
        SwappableBuffer buffer = SwappableBuffer.of(10, 3);

        // Restore without save should return false
        assertThat(buffer.restore()).isFalse();
    }

    @Test
    public void testMultipleSaveRestoreCycles() {
        SwappableBuffer buffer = SwappableBuffer.of(10, 3);

        // First cycle
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("first");
        }
        assertThat(buffer.save()).isTrue();

        try (PrintBufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("second");
        }
        assertThat(buffer.restore()).isTrue();
        assertThat(buffer.toString()).contains("first");

        // Second cycle
        assertThat(buffer.save()).isTrue();
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("third");
        }
        assertThat(buffer.restore()).isTrue();
        assertThat(buffer.toString()).contains("first");
    }

    @Test
    public void testSaveAndRestorePreservesStyles() {
        SwappableBuffer buffer = SwappableBuffer.of(10, 3);

        // Write styled content
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.style(Style.BOLD);
            writer.write("bold");
            writer.style(Style.ITALIC);
            writer.write("ital");
        }

        assertThat(buffer.styleAt(0, 0)).isEqualTo(Style.DEFAULT.bold());
        assertThat(buffer.styleAt(4, 0)).isEqualTo(Style.DEFAULT.italic());

        // Save, modify, and restore
        buffer.save();
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.style(Style.UNDERLINED);
            writer.write("underln");
        }

        buffer.restore();

        // Styles should be restored
        assertThat(buffer.styleAt(0, 0)).isEqualTo(Style.DEFAULT.bold());
        assertThat(buffer.styleAt(4, 0)).isEqualTo(Style.DEFAULT.italic());
    }

    @Test
    public void testAlternateSaveDoesNotClear() {
        SwappableBuffer buffer = SwappableBuffer.of(10, 3);

        // Write initial content
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("hello");
        }

        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("h");

        // Write alternate save sequence (SCREEN_SAVE_ALT) - should save and swap
        // but the behavior is the same as SCREEN_SAVE except it doesn't explicitly clear
        // However, the alternate buffer starts empty anyway
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write(CSI + SCREEN_SAVE_ALT);
        }

        // After save, we're on the alternate buffer which is empty
        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("\0");

        // Restore to verify original content was preserved
        buffer.restore();
        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("h");
    }
}
