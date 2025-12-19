package org.codejive.twinkle.core.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.widget.Panel;
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
        Panel p = Panel.of(1, 1);
        Span.of("A", Style.BOLD).render(p);
        assertThat(p.toString()).isEqualTo("A");
        assertThat(p.toAnsiString()).isEqualTo(Ansi.STYLE_RESET + Ansi.style(Ansi.BOLD) + "A");
    }
}
