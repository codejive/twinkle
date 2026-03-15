package org.codejive.twinkle.text.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Constants;
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
        assertThat(it.style()).isEqualTo(Style.UNKNOWN);

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
        String red = Constants.CSI + "31m";
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
        String up = Ansi.cursorMove(Constants.CURSOR_UP);
        SequenceIterator seqIter = SequenceIterator.of(up + "a");
        StyledIterator it = new StyledIterator(seqIter);

        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo('a');
        // Style should NOT change for non-SGR sequences
        assertThat(it.style()).isEqualTo(Style.UNKNOWN);

        assertThat(it.hasNext()).isFalse();
    }

    @Test
    public void testMixedSequences() {
        String red = Constants.CSI + "31m";
        String reset = Ansi.STYLE_RESET;
        SequenceIterator seqIter = SequenceIterator.of("a" + red + "b" + reset + "c");
        StyledIterator it = new StyledIterator(seqIter);

        // 'a'
        assertThat(it.next()).isEqualTo('a');
        assertThat(it.style()).isEqualTo(Style.UNKNOWN);

        // 'b' (RED consumed)
        assertThat(it.next()).isEqualTo('b');
        assertThat(it.style()).isNotEqualTo(Style.UNSTYLED);

        // 'c' (RESET consumed)
        assertThat(it.next()).isEqualTo('c');
        assertThat(it.style()).isEqualTo(Style.UNSTYLED);
    }

    @Test
    public void testBasicHyperlink() {
        String linkStart = Constants.OSC + "8;;https://example.com" + Constants.OSC_END;
        String linkEnd = Constants.OSC + "8;;" + Constants.OSC_END;
        SequenceIterator seqIter = SequenceIterator.of(linkStart + "text" + linkEnd);
        StyledIterator it = new StyledIterator(seqIter);

        // 't' - hyperlink should be active
        assertThat(it.next()).isEqualTo('t');
        assertThat(it.link()).isNotNull();
        assertThat(it.link().url).isEqualTo("https://example.com");
        assertThat(it.link().id).isNull();

        // 'e'
        assertThat(it.next()).isEqualTo('e');
        assertThat(it.link()).isNotNull();

        // 'x'
        assertThat(it.next()).isEqualTo('x');
        assertThat(it.link()).isNotNull();

        // 't' - still has hyperlink
        assertThat(it.next()).isEqualTo('t');
        assertThat(it.link()).isNotNull();

        // After consuming all characters, hyperlink end should be processed
        assertThat(it.hasNext()).isFalse();
        assertThat(it.link()).isNull();
    }

    @Test
    public void testHyperlinkWithId() {
        String linkStart = Constants.OSC + "8;id=myid;https://example.com" + Constants.OSC_END;
        String linkEnd = Constants.OSC + "8;;" + Constants.OSC_END;
        SequenceIterator seqIter = SequenceIterator.of(linkStart + "a" + linkEnd);
        StyledIterator it = new StyledIterator(seqIter);

        assertThat(it.next()).isEqualTo('a');
        assertThat(it.link()).isNotNull();
        assertThat(it.link().url).isEqualTo("https://example.com");
        assertThat(it.link().id).isEqualTo("myid");

        // After consuming, link end is processed
        assertThat(it.hasNext()).isFalse();
        assertThat(it.link()).isNull();
    }

    @Test
    public void testHyperlinkWithAlternativeTerminator() {
        String linkStart = Constants.OSC + "8;;https://example.com" + Constants.OSC_END_ALT;
        String linkEnd = Constants.OSC + "8;;" + Constants.OSC_END_ALT;
        SequenceIterator seqIter = SequenceIterator.of(linkStart + "a" + linkEnd);
        StyledIterator it = new StyledIterator(seqIter);

        assertThat(it.next()).isEqualTo('a');
        assertThat(it.link()).isNotNull();
        assertThat(it.link().url).isEqualTo("https://example.com");

        // After consuming, link end is processed
        assertThat(it.hasNext()).isFalse();
        assertThat(it.link()).isNull();
    }

    @Test
    public void testMultipleHyperlinks() {
        String link1Start = Constants.OSC + "8;;https://first.com" + Constants.OSC_END;
        String linkEnd = Constants.OSC + "8;;" + Constants.OSC_END;
        String link2Start = Constants.OSC + "8;;https://second.com" + Constants.OSC_END;

        SequenceIterator seqIter =
                SequenceIterator.of(link1Start + "a" + linkEnd + "b" + link2Start + "c" + linkEnd);
        StyledIterator it = new StyledIterator(seqIter);

        // 'a' - first link active
        assertThat(it.next()).isEqualTo('a');
        assertThat(it.link()).isNotNull();
        assertThat(it.link().url).isEqualTo("https://first.com");

        // 'b' - no link active (link end was consumed when priming for 'b')
        assertThat(it.next()).isEqualTo('b');
        assertThat(it.link()).isNull();

        // 'c' - second link active (link2Start was consumed when priming for 'c')
        assertThat(it.next()).isEqualTo('c');
        assertThat(it.link()).isNotNull();
        assertThat(it.link().url).isEqualTo("https://second.com");

        // After consuming all, final link end is processed
        assertThat(it.hasNext()).isFalse();
        assertThat(it.link()).isNull();
    }

    @Test
    public void testHyperlinkWithStyleSequences() {
        String linkStart = Constants.OSC + "8;;https://example.com" + Constants.OSC_END;
        String red = Constants.CSI + "31m";
        String linkEnd = Constants.OSC + "8;;" + Constants.OSC_END;
        String reset = Ansi.STYLE_RESET;

        SequenceIterator seqIter =
                SequenceIterator.of(linkStart + red + "a" + linkEnd + reset + "b");
        StyledIterator it = new StyledIterator(seqIter);

        // 'a' - both link and style active
        assertThat(it.next()).isEqualTo('a');
        assertThat(it.link()).isNotNull();
        assertThat(it.link().url).isEqualTo("https://example.com");
        assertThat(it.style()).isNotEqualTo(Style.UNSTYLED);

        // 'b' - no link, reset style
        assertThat(it.next()).isEqualTo('b');
        assertThat(it.link()).isNull();
        assertThat(it.style()).isEqualTo(Style.UNSTYLED);

        assertThat(it.hasNext()).isFalse();
    }

    @Test
    public void testNonHyperlinkAnsiDoesNotAffectLink() {
        String linkStart = Constants.OSC + "8;;https://example.com" + Constants.OSC_END;
        String up = Ansi.cursorMove(Constants.CURSOR_UP);
        String linkEnd = Constants.OSC + "8;;" + Constants.OSC_END;

        SequenceIterator seqIter = SequenceIterator.of(linkStart + up + "a" + linkEnd);
        StyledIterator it = new StyledIterator(seqIter);

        assertThat(it.next()).isEqualTo('a');
        assertThat(it.link()).isNotNull();
        assertThat(it.link().url).isEqualTo("https://example.com");

        // After consuming, link end is processed
        assertThat(it.hasNext()).isFalse();
        assertThat(it.link()).isNull();
    }

    @Test
    public void testLinkEndOnly() {
        String linkEnd = Constants.OSC + "8;;" + Constants.OSC_END;
        SequenceIterator seqIter = SequenceIterator.of("ab" + linkEnd + "c");
        StyledIterator it = new StyledIterator(seqIter);

        assertThat(it.next()).isEqualTo('a');
        assertThat(it.link()).isNull();

        assertThat(it.next()).isEqualTo('b');
        assertThat(it.link()).isNull();

        assertThat(it.next()).isEqualTo('c');
        assertThat(it.link()).isNull();
    }
}
