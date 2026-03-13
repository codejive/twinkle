package org.codejive.twinkle.text.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codejive.twinkle.ansi.Constants.CSI;
import static org.codejive.twinkle.ansi.Constants.SCREEN_RESTORE_ALT;
import static org.codejive.twinkle.ansi.Constants.SCREEN_SAVE_ALT;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.text.SwappableBuffer;
import org.junit.jupiter.api.Test;

public class TestSwappableBufferWriter {

    @Test
    public void testSaveScreenSequenceClearsBuffer() {
        SwappableBuffer buffer = SwappableBuffer.of(10, 3);

        // Write initial content
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("hello");
        }

        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("h");

        // Write saveScreen sequence - should save and clear
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write(Ansi.saveScreen());
        }

        // Buffer should be cleared after saveScreen
        assertThat(buffer.graphemeAt(0, 0)).isEqualTo("\0");
    }

    @Test
    public void testRestoreScreenSequence() {
        SwappableBuffer buffer = SwappableBuffer.of(10, 3);

        // Write initial content
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("original");
        }

        assertThat(buffer.toString()).contains("original");

        // Save and write new content
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write(Ansi.saveScreen());
            writer.style(Style.DEFAULT);
            writer.write("alternate");
        }

        assertThat(buffer.toString()).contains("alternate");

        // Restore
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write(Ansi.restoreScreen());
        }

        // Original content should be restored
        assertThat(buffer.toString()).contains("original");
    }

    @Test
    public void testSaveAndRestoreThroughAnsiSequences() {
        SwappableBuffer buffer = SwappableBuffer.of(20, 5);

        // Write initial content using pure Ansi sequences and text
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("Main screen content");
        }

        String mainContent = buffer.toString();
        assertThat(mainContent).contains("Main screen");

        // Save screen and write alternate content
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write(Ansi.saveScreen());
            writer.style(Style.DEFAULT);
            writer.write("Alternate screen");
        }

        String altContent = buffer.toString();
        assertThat(altContent).contains("Alternate");
        assertThat(altContent).doesNotContain("Main screen");

        // Restore screen
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write(Ansi.restoreScreen());
        }

        String restoredContent = buffer.toString();
        assertThat(restoredContent).contains("Main screen");
    }

    @Test
    public void testAlternateRestoreSequence() {
        SwappableBuffer buffer = SwappableBuffer.of(10, 3);

        // Write and save
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("original");
        }

        assertThat(buffer.toString()).contains("original");

        // Save using alternate sequence
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write(CSI + SCREEN_SAVE_ALT);
        }

        // Write to alternate buffer
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("modified");
        }

        assertThat(buffer.toString()).contains("modified");

        // Restore using alternate sequence
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write(CSI + SCREEN_RESTORE_ALT);
        }

        // Original content should be restored
        assertThat(buffer.toString()).contains("original");
    }

    @Test
    public void testMultipleAnsiSaveRestoreOperations() {
        SwappableBuffer buffer = SwappableBuffer.of(15, 4);

        // Initial state
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.style(Style.DEFAULT);
            writer.write("state1");
        }

        // Save and go to alternate
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write(Ansi.saveScreen());
            writer.style(Style.DEFAULT);
            writer.write("state2");
        }

        assertThat(buffer.toString()).contains("state2");

        // Second save should not work (already saved)
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write(Ansi.saveScreen());
            writer.style(Style.DEFAULT);
            writer.write("state3");
        }

        // Should still be on the same alternate buffer
        assertThat(buffer.toString()).contains("state3");

        // Restore
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write(Ansi.restoreScreen());
        }

        // Back to original
        assertThat(buffer.toString()).contains("state1");

        // Second restore should not affect anything
        try (PrintBufferWriter writer = buffer.writer()) {
            writer.write(Ansi.restoreScreen());
        }

        assertThat(buffer.toString()).contains("state1");
    }
}
