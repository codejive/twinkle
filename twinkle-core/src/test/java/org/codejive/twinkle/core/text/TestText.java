package org.codejive.twinkle.core.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.util.StyledIterator;
import org.junit.jupiter.api.Test;

public class TestText {

    @Test
    public void testOfSimpleString() {
        Text t = Text.of("Hello World");
        assertThat(t.toAnsiString()).isEqualTo("Hello World");
    }

    @Test
    public void testOfStyledString() {
        Style style = Style.BOLD;
        Text t = Text.of("Hello World", style);
        assertThat(t.toAnsiString()).isEqualTo(style.toAnsiString() + "Hello World");
    }

    @Test
    public void testOfStyleState() {
        Style style = Style.BOLD;
        Text t = Text.of("Hello World", style);
        assertThat(t.toAnsiString()).isEqualTo(style.toAnsiString() + "Hello World");
    }

    @Test
    public void testOfLines() {
        Line line1 = Line.of("Line 1");
        Line line2 = Line.of("Line 2");
        Text t = Text.of(line1, line2);
        assertThat(t.toAnsiString()).isEqualTo("Line 1\nLine 2");
    }

    @Test
    public void testOfStyledIterator() {
        StyledIterator iter = StyledIterator.of("Line 1\nLine 2");
        Text t = Text.of(iter);
        assertThat(t.toAnsiString()).isEqualTo("Line 1\nLine 2");
    }
}
