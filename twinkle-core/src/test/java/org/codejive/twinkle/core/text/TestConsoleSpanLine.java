package org.codejive.twinkle.core.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.widget.Panel;
import org.junit.jupiter.api.Test;

public class TestConsoleSpanLine {
    private static String stripSgr(String s) {
        // Strip CSI ... m sequences (covers our SGR output, including ':' or ';' separators)
        return s.replaceAll("\u001B\\[[0-9;:]*m", "");
    }

    @Test
    public void testLineRendersSpansSequentially() {
        Panel p = Panel.of(2, 1);
        Line.of(Span.of("A", Style.BOLD), Span.of("B", Style.UNDERLINED)).render(p);
        assertThat(p.toString()).isEqualTo("AB");
    }

    @Test
    public void testConsoleToAnsiStringIsCompactPerLine() {
        String ansi = Console.toAnsiString("hi\nthere");
        assertThat(stripSgr(ansi)).isEqualTo("hi\nthere");
    }

    @Test
    public void testConsoleToAnsiStringEmoji() {
        String ansi = Console.toAnsiString(":sparkles:");
        assertThat(stripSgr(ansi)).isEqualTo("✨");
    }

    @Test
    public void testConsoleToAnsiStringOsc8LinkMarkup() {
        String ansi = Console.toAnsiString("[link=https://example.com]hi[/]");
        assertThat(ansi).contains(org.codejive.twinkle.ansi.Ansi.osc8Open("https://example.com"));
        assertThat(ansi).contains(org.codejive.twinkle.ansi.Ansi.osc8Close());
        assertThat(stripSgr(ansi)).contains("hi");
    }
}


