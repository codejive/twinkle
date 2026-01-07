package org.codejive.twinkle.core.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.util.StyledIterator;
import org.junit.jupiter.api.Test;

public class TestLine {

    @Test
    public void testRenderSingleStyledSpan() {
        Line l = Line.of("A", Style.BOLD);
        assertThat(l.toAnsiString()).isEqualTo(Ansi.style(Ansi.BOLD) + "A");
    }

    @Test
    public void testRenderMultipleSpans() {
        Line l = Line.of(Span.of("A"), Span.of("B", Style.BOLD), Span.of("C"));

        String ansi = l.toAnsiString();
        assertThat(ansi)
                .isEqualTo("A" + Ansi.style(Ansi.BOLD) + "B" + Ansi.style(Ansi.NORMAL) + "C");
    }

    @Test
    public void testOfStyledIterator() {
        StyledIterator iter = StyledIterator.of("Line 1");
        Line l = Line.of(iter);
        assertThat(l.toAnsiString()).isEqualTo("Line 1");
    }
}
