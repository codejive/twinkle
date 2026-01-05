package org.codejive.twinkle.core.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.widget.Panel;
import org.codejive.twinkle.util.SequenceIterator;
import org.codejive.twinkle.util.StyledIterator;
import org.junit.jupiter.api.Test;

public class TestText {

    @Test
    public void testOfSimpleString() {
        Panel pnl = Panel.of(11, 1);
        Text.of("Hello World").render(pnl);
        assertThat(pnl.toString()).isEqualTo("Hello World");
    }

    @Test
    public void testOfStyledString() {
        Style style = Style.BOLD;
        Panel pnl = Panel.of(11, 1);
        Text.of("Hello World", style).render(pnl);
        assertThat(pnl.toAnsiString(Style.F_UNSTYLED))
                .isEqualTo(style.toAnsiString() + "Hello World");
    }

    @Test
    public void testOfStyleState() {
        Style style = Style.BOLD;
        Panel pnl = Panel.of(11, 1);
        Text.of("Hello World", style.state()).render(pnl);
        assertThat(pnl.toAnsiString(Style.F_UNSTYLED))
                .isEqualTo(style.toAnsiString() + "Hello World");
    }

    @Test
    public void testOfLines() {
        Line line1 = Line.of("Line 1");
        Line line2 = Line.of("Line 2");
        Panel pnl = Panel.of(6, 2);
        Text.of(line1, line2).render(pnl);
        assertThat(pnl.toString()).isEqualTo("Line 1\nLine 2");
    }

    @Test
    public void testOfStyledIterator() {
        StyledIterator iter = new StyledIterator(SequenceIterator.of("Line 1\nLine 2"));
        Panel pnl = Panel.of(6, 2);
        Text.of(iter).render(pnl);
        assertThat(pnl.toString()).isEqualTo("Line 1\nLine 2");
    }
}
