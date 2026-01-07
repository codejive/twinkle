package org.codejive.twinkle.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Style;
import org.junit.jupiter.api.Test;

public class TestStyledIterator {

    @Test
    public void testPlainSequence() {
        SequenceIterator seqIter = SequenceIterator.of("abc");
        StyledIterator it = new StyledIterator(seqIter);

        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo('a');
        assertThat(it.sequence()).isEqualTo("a");
        assertThat(it.style()).isEqualTo(Style.of(Style.F_UNKNOWN));

        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo('b');
        assertThat(it.sequence()).isEqualTo("b");

        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo('c');
        assertThat(it.sequence()).isEqualTo("c");

        assertThat(it.hasNext()).isFalse();
    }

    @Test
    public void testStyledSequence() {
        String red = "\u001B[31m";
        SequenceIterator seqIter = SequenceIterator.of(red + "a");
        StyledIterator it = new StyledIterator(seqIter);

        assertThat(it.hasNext()).isTrue();
        // The iterator should consume the ESC sequence internally and advance to 'a'
        assertThat(it.next()).isEqualTo('a');
        assertThat(it.sequence()).isEqualTo("a");

        // Verify style state changed
        assertThat(it.style()).isNotEqualTo(Style.UNSTYLED);

        assertThat(it.hasNext()).isFalse();
    }

    @Test
    public void testSkipNonStyleAnsi() {
        String up = "\u001B[1A"; // Cursor Up
        SequenceIterator seqIter = SequenceIterator.of(up + "a");
        StyledIterator it = new StyledIterator(seqIter);

        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo('a');
        // Style should NOT change for non-SGR sequences
        assertThat(it.style()).isEqualTo(Style.of(Style.F_UNKNOWN));

        assertThat(it.hasNext()).isFalse();
    }

    @Test
    public void testMixedSequences() {
        String red = "\u001B[31m";
        String reset = "\u001B[0m";
        SequenceIterator seqIter = SequenceIterator.of("a" + red + "b" + reset + "c");
        StyledIterator it = new StyledIterator(seqIter);

        // 'a'
        assertThat(it.next()).isEqualTo('a');
        assertThat(it.style()).isEqualTo(Style.of(Style.F_UNKNOWN));

        // 'b' (RED consumed)
        assertThat(it.next()).isEqualTo('b');
        assertThat(it.style()).isNotEqualTo(Style.UNSTYLED);

        // 'c' (RESET consumed)
        assertThat(it.next()).isEqualTo('c');
        assertThat(it.style()).isEqualTo(Style.UNSTYLED);
    }
}
