package org.codejive.twinkle.core.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.widget.Panel;
import org.codejive.twinkle.util.SequenceIterator;
import org.codejive.twinkle.util.StyledIterator;
import org.junit.jupiter.api.Test;

public class TestLine {

    @Test
    public void testRenderSingleStyledSpan() {
        Panel p = Panel.of(1, 1);
        Line.of("A", Style.BOLD).render(p);

        assertThat(p.toString()).isEqualTo("A");
        assertThat(p.toAnsiString()).isEqualTo(Ansi.STYLE_RESET + Ansi.style(Ansi.BOLD) + "A");
    }

    @Test
    public void testRenderMultipleSpans() {
        Panel p = Panel.of(3, 1);
        Line.of(Span.of("A"), Span.of("B", Style.BOLD), Span.of("C")).render(p);

        assertThat(p.toString()).isEqualTo("ABC");

        String ansi = p.toAnsiString();
        assertThat(ansi)
                .isEqualTo(
                        Ansi.STYLE_RESET
                                + "A"
                                + Ansi.style(Ansi.BOLD)
                                + "B"
                                + Ansi.style(Ansi.NORMAL)
                                + "C");
    }

    @Test
    public void testOfStyledIterator() {
        StyledIterator iter = new StyledIterator(SequenceIterator.of("Line 1"));
        Panel p = Panel.of(6, 1);
        Line.of(iter).render(p);
        assertThat(p.toString()).isEqualTo("Line 1");
    }
}
