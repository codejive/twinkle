package org.codejive.twinkle.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class TestSequenceIterator {

    @Test
    public void testNormalText() {
        String input = "abc 123";
        SequenceIterator it = SequenceIterator.of(input);

        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo('a');
        assertThat(it.sequence()).isEqualTo("a");
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo('b');
        assertThat(it.sequence()).isEqualTo("b");
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo('c');
        assertThat(it.sequence()).isEqualTo("c");
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo(' ');
        assertThat(it.sequence()).isEqualTo(" ");
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo('1');
        assertThat(it.sequence()).isEqualTo("1");
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo('2');
        assertThat(it.sequence()).isEqualTo("2");
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo('3');
        assertThat(it.sequence()).isEqualTo("3");
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        assertThat(it.hasNext()).isFalse();
    }

    @Test
    public void testSurrogatePairs() {
        // G clef character: U+1D11E (surrogate pair \uD834\uDD1E)
        String input = "\uD834\uDD1E";
        SequenceIterator it = SequenceIterator.of(input);

        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo(0x1D11E);
        assertThat(it.sequence()).isEqualTo("\uD834\uDD1E");
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        assertThat(it.hasNext()).isFalse();
    }

    @Test
    public void testComplexGraphemes() {
        // Family emoji: Man + ZWJ + Woman + ZWJ + Girl + ZWJ + Boy
        // U+1F468 U+200D U+1F469 U+200D U+1F467 U+200D U+1F466
        String familyEmoji = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66";
        // Letter 'a' with acute accent: a + U+0301
        String aAcute = "a\u0301";

        String input = familyEmoji + aAcute;
        SequenceIterator it = SequenceIterator.of(input);

        // Test Family Emoji
        assertThat(it.hasNext()).isTrue();
        // next() returns the first code point of the sequence
        assertThat(it.next()).isEqualTo(0x1F468);
        assertThat(it.sequence()).isEqualTo(familyEmoji);
        assertThat(it.width()).isEqualTo(2);
        assertThat(it.isComplex()).isTrue();

        // Test Combining Character
        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo('a');
        assertThat(it.sequence()).isEqualTo(aAcute);
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isTrue();

        assertThat(it.hasNext()).isFalse();
    }

    @Test
    public void testAnsiEscapeSequences() {
        // CSI: Red color
        String csi = "\u001B[31m";
        // OSC: Window title with BEL terminator
        String oscBel = "\u001B]0;Title\u0007";
        // OSC: Hyperlink with ST (ESC \) terminator
        String oscSt = "\u001B]8;;http://example.com\u001B\\";

        String input = csi + "A" + oscBel + "B" + oscSt;
        SequenceIterator it = SequenceIterator.of(input);

        // CSI
        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo(0x1B);
        assertThat(it.sequence()).isEqualTo(csi);
        assertThat(it.width()).isEqualTo(0);
        assertThat(it.isComplex()).isTrue();

        // Normal char 'A'
        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo('A');
        assertThat(it.sequence()).isEqualTo("A");
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        // OSC BEL
        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo(0x1B);
        assertThat(it.sequence()).isEqualTo(oscBel);
        assertThat(it.width()).isEqualTo(0);
        assertThat(it.isComplex()).isTrue();

        // Normal char 'B'
        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo('B');
        assertThat(it.sequence()).isEqualTo("B");
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        // OSC ST
        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo(0x1B);
        assertThat(it.sequence()).isEqualTo(oscSt);
        assertThat(it.width()).isEqualTo(0);
        assertThat(it.isComplex()).isTrue();

        assertThat(it.hasNext()).isFalse();
    }

    @Test
    public void testNewlines() {
        // \n, \r\n, \r should all be treated as newlines
        String input = "a\nb\r\nc\rd";
        SequenceIterator it = SequenceIterator.of(input);

        // a
        it.next();
        assertThat(it.sequence()).isEqualTo("a");
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        // \n
        assertThat(it.next()).isEqualTo('\n');
        assertThat(it.sequence()).isEqualTo("\n");
        assertThat(it.width()).isEqualTo(0);
        assertThat(it.isComplex()).isFalse();

        // b
        it.next();
        assertThat(it.sequence()).isEqualTo("b");
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        // \r\n -> returns \n code point, sequence is \r\n
        assertThat(it.next()).isEqualTo('\n');
        assertThat(it.sequence()).isEqualTo("\r\n");
        assertThat(it.width()).isEqualTo(0);
        assertThat(it.isComplex()).isTrue();

        // c
        it.next();
        assertThat(it.sequence()).isEqualTo("c");
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        // \r -> returns \n code point, sequence is \r
        assertThat(it.next()).isEqualTo('\n');
        assertThat(it.sequence()).isEqualTo("\r");
        assertThat(it.width()).isEqualTo(0);
        assertThat(it.isComplex()).isFalse();

        // d
        it.next();
        assertThat(it.sequence()).isEqualTo("d");
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        assertThat(it.hasNext()).isFalse();
    }

    @Test
    public void testCombined() {
        // "Hi " + FamilyEmoji + " " + RedColor + "Bold" + Reset
        String familyEmoji = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66";
        String red = "\u001B[31m";
        String reset = "\u001B[0m";

        String input = "Hi " + familyEmoji + " " + red + "Bold" + reset;
        SequenceIterator it = SequenceIterator.of(input);

        // H
        assertThat(it.next()).isEqualTo('H');
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();
        // i
        assertThat(it.next()).isEqualTo('i');
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();
        // space
        assertThat(it.next()).isEqualTo(' ');
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        // Family Emoji
        assertThat(it.next()).isEqualTo(0x1F468);
        assertThat(it.sequence()).isEqualTo(familyEmoji);
        assertThat(it.width()).isEqualTo(2);
        assertThat(it.isComplex()).isTrue();

        // space
        assertThat(it.next()).isEqualTo(' ');
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        // Red ANSI
        assertThat(it.next()).isEqualTo(0x1B);
        assertThat(it.sequence()).isEqualTo(red);
        assertThat(it.width()).isEqualTo(0);
        assertThat(it.isComplex()).isTrue();

        // B
        assertThat(it.next()).isEqualTo('B');
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();
        // o
        assertThat(it.next()).isEqualTo('o');
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();
        // l
        assertThat(it.next()).isEqualTo('l');
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();
        // d
        assertThat(it.next()).isEqualTo('d');
        assertThat(it.width()).isEqualTo(1);
        assertThat(it.isComplex()).isFalse();

        // Reset ANSI
        assertThat(it.next()).isEqualTo(0x1B);
        assertThat(it.sequence()).isEqualTo(reset);
        assertThat(it.width()).isEqualTo(0);
        assertThat(it.isComplex()).isTrue();

        assertThat(it.hasNext()).isFalse();
    }
}
