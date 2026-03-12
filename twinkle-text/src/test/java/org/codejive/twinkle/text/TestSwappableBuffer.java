package org.codejive.twinkle.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codejive.twinkle.ansi.Constants.*;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.text.util.BufferWriter;
import org.junit.jupiter.api.Test;

public class TestSwappableBuffer {

    @Test
    public void testDirectSaveAndRestore() {
        SwappableBuffer buffer = SwappableBuffer.of(10, 3);

        // Write some content to the buffer
        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("hello");
        }

        // Verify initial content
        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("h");
        assertThat(buffer.graphemeAt(4, 0)).isEqualTo("o");

        // Save should return true on first call
        assertThat(buffer.save()).isTrue();

        // Write different content to the alternate buffer
        try (BufferWriter writer = buffer.writer()) {
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
        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("first");
        }
        assertThat(buffer.save()).isTrue();

        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("second");
        }
        assertThat(buffer.restore()).isTrue();
        assertThat(buffer.toString()).contains("first");

        // Second cycle
        assertThat(buffer.save()).isTrue();
        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("third");
        }
        assertThat(buffer.restore()).isTrue();
        assertThat(buffer.toString()).contains("first");
    }

    @Test
    public void testSaveScreenSequenceClearsBuffer() {
        SwappableBuffer buffer = SwappableBuffer.of(10, 3);

        // Write initial content
        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("hello");
        }

        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("h");

        // Write saveScreen sequence - should save and clear
        try (BufferWriter writer = buffer.writer()) {
            writer.write(Ansi.saveScreen());
        }

        // Buffer should be cleared after saveScreen
        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("\0");
    }

    @Test
    public void testRestoreScreenSequence() {
        SwappableBuffer buffer = SwappableBuffer.of(10, 3);

        // Write initial content
        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("original");
        }

        assertThat(buffer.toString()).contains("original");

        // Save and write new content
        try (BufferWriter writer = buffer.writer()) {
            writer.write(Ansi.saveScreen());
            writer.style(Style.DEFAULT);
            writer.write("alternate");
        }

        assertThat(buffer.toString()).contains("alternate");

        // Restore
        try (BufferWriter writer = buffer.writer()) {
            writer.write(Ansi.restoreScreen());
        }

        // Original content should be restored
        assertThat(buffer.toString()).contains("original");
    }

    @Test
    public void testSaveAndRestorePreservesStyles() {
        SwappableBuffer buffer = SwappableBuffer.of(10, 3);

        // Write styled content
        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.BOLD);
            writer.write("bold");
            writer.style(Style.ITALIC);
            writer.write("ital");
        }

        assertThat(buffer.styleAt(0, 0)).isEqualTo(Style.DEFAULT.bold());
        assertThat(buffer.styleAt(4, 0)).isEqualTo(Style.DEFAULT.italic());

        // Save, modify, and restore
        buffer.save();
        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.UNDERLINED);
            writer.write("underln");
        }

        buffer.restore();

        // Styles should be restored
        assertThat(buffer.styleAt(0, 0)).isEqualTo(Style.DEFAULT.bold());
        assertThat(buffer.styleAt(4, 0)).isEqualTo(Style.DEFAULT.italic());
    }

    @Test
    public void testSaveAndRestoreThroughAnsiSequences() {
        SwappableBuffer buffer = SwappableBuffer.of(20, 5);

        // Write initial content using pure Ansi sequences and text
        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("Main screen content");
        }

        String mainContent = buffer.toString();
        assertThat(mainContent).contains("Main screen");

        // Save screen and write alternate content
        try (BufferWriter writer = buffer.writer()) {
            writer.write(Ansi.saveScreen());
            writer.style(Style.DEFAULT);
            writer.write("Alternate screen");
        }

        String altContent = buffer.toString();
        assertThat(altContent).contains("Alternate");
        assertThat(altContent).doesNotContain("Main screen");

        // Restore screen
        try (BufferWriter writer = buffer.writer()) {
            writer.write(Ansi.restoreScreen());
        }

        String restoredContent = buffer.toString();
        assertThat(restoredContent).contains("Main screen");
    }

    @Test
    public void testAlternateSaveDoesNotClear() {
        SwappableBuffer buffer = SwappableBuffer.of(10, 3);

        // Write initial content
        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("hello");
        }

        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("h");

        // Write alternate save sequence (SCREEN_SAVE_ALT) - should save and swap
        // but the behavior is the same as SCREEN_SAVE except it doesn't explicitly clear
        // However, the alternate buffer starts empty anyway
        try (BufferWriter writer = buffer.writer()) {
            writer.write(CSI + SCREEN_SAVE_ALT);
        }

        // After save, we're on the alternate buffer which is empty
        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("\0");

        // Restore to verify original content was preserved
        buffer.restore();
        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("h");
    }

    @Test
    public void testAlternateRestoreSequence() {
        SwappableBuffer buffer = SwappableBuffer.of(10, 3);

        // Write and save
        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("original");
        }

        assertThat(buffer.toString()).contains("original");

        // Save using alternate sequence
        try (BufferWriter writer = buffer.writer()) {
            writer.write(CSI + SCREEN_SAVE_ALT);
        }

        // Write to alternate buffer
        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("modified");
        }

        assertThat(buffer.toString()).contains("modified");

        // Restore using alternate sequence
        try (BufferWriter writer = buffer.writer()) {
            writer.write(CSI + SCREEN_RESTORE_ALT);
        }

        // Original content should be restored
        assertThat(buffer.toString()).contains("original");
    }

    @Test
    public void testMultipleAnsiSaveRestoreOperations() {
        SwappableBuffer buffer = SwappableBuffer.of(15, 4);

        // Initial state
        try (BufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("state1");
        }

        // Save and go to alternate
        try (BufferWriter writer = buffer.writer()) {
            writer.write(Ansi.saveScreen());
            writer.style(Style.DEFAULT);
            writer.write("state2");
        }

        assertThat(buffer.toString()).contains("state2");

        // Second save should not work (already saved)
        try (BufferWriter writer = buffer.writer()) {
            writer.write(Ansi.saveScreen());
            writer.style(Style.DEFAULT);
            writer.write("state3");
        }

        // Should still be on the same alternate buffer
        assertThat(buffer.toString()).contains("state3");

        // Restore
        try (BufferWriter writer = buffer.writer()) {
            writer.write(Ansi.restoreScreen());
        }

        // Back to original
        assertThat(buffer.toString()).contains("state1");

        // Second restore should not affect anything
        try (BufferWriter writer = buffer.writer()) {
            writer.write(Ansi.restoreScreen());
        }

        assertThat(buffer.toString()).contains("state1");
    }
}
