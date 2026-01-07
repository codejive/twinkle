package org.codejive.twinkle.core.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TestSpan {

    @Test
    public void testLength() {
        Span s = Span.of("hello");
        assertThat(s.length()).isEqualTo(5);
    }

    @Test
    public void testLengthSurrogatePair() {
        Span s = Span.of("😀"); // U+1F600 is a surrogate-pair in UTF-16
        assertThat(s.length()).isEqualTo(1);
    }

    @Test
    @Disabled("Disabled until proper handling of ZWJ sequences is implemented")
    public void testLengthZwjSequence() {
        String family = "👨‍👩‍👧‍👦"; // family emoji uses ZWJ sequences
        Span s = Span.of(family);
        assertThat(s.length()).isEqualTo(1);
    }

    @Test
    public void testSpansRender() {
        Span s = Span.of("A", Style.BOLD);
        assertThat(s.toAnsiString()).isEqualTo(Ansi.style(Ansi.BOLD) + "A");
    }
}
