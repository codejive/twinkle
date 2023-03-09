package org.codejive.twinkle.text.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class TestSequenceDecoder {

    @Test
    public void testSimpleCodepoint() {
        SequenceDecoder decoder = new SequenceDecoder();
        assertThat(decoder.isComplete()).isFalse();
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.INCOMPLETE);

        decoder.push('A');

        assertThat(decoder.isComplete()).isTrue();
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.CODEPOINT);
        assertThat(decoder.width()).isEqualTo(1);
        assertThat(decoder.toString()).isEqualTo("A");
    }

    @Test
    public void testSurrogatePair() {
        SequenceDecoder decoder = new SequenceDecoder();

        String clef = "\uD834\uDD1E";
        decoder.push(clef.charAt(0));
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.INCOMPLETE);

        decoder.push(clef.charAt(1));

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.CODEPOINT);
        assertThat(decoder.toString()).isEqualTo(clef);
    }

    @Test
    public void testCombiningMarkUpgradesToGraphemeCluster() {
        SequenceDecoder decoder = new SequenceDecoder();

        decoder.push('a');
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.CODEPOINT);
        assertThat(decoder.canPush('\u0301')).isTrue();
        assertThat(decoder.canPush('b')).isFalse();

        decoder.push('\u0301');

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.GRAPHEME_CLUSTER);
        assertThat(decoder.width()).isEqualTo(1);
        assertThat(decoder.toString()).isEqualTo("a\u0301");
    }

    @Test
    public void testAnsiCsiSequence() {
        SequenceDecoder decoder = new SequenceDecoder();
        String csi = "\u001B[31m";

        for (int i = 0; i < csi.length() - 1; i++) {
            decoder.push(csi.charAt(i));
            assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.INCOMPLETE);
        }

        decoder.push(csi.charAt(csi.length() - 1));

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.ANSI_ESCAPE_SEQUENCE);
        assertThat(decoder.width()).isEqualTo(0);
        assertThat(decoder.toString()).isEqualTo(csi);
    }

    @Test
    public void testAnsiOscSequenceWithStTerminator() {
        SequenceDecoder decoder = new SequenceDecoder();
        String osc = "\u001B]8;;http://example.com\u001B\\";

        for (int i = 0; i < osc.length() - 1; i++) {
            decoder.push(osc.charAt(i));
        }
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.INCOMPLETE);

        decoder.push(osc.charAt(osc.length() - 1));

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.ANSI_ESCAPE_SEQUENCE);
        assertThat(decoder.toString()).isEqualTo(osc);
    }

    @Test
    public void testInvalidSurrogateGoesToError() {
        SequenceDecoder decoder = new SequenceDecoder();

        decoder.push('\uD834');
        decoder.push('x');

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.ERROR);
        assertThat(decoder.width()).isEqualTo(-1);
    }

    @Test
    public void testResetClearsState() {
        SequenceDecoder decoder = new SequenceDecoder();

        decoder.push('A');
        assertThat(decoder.isComplete()).isTrue();

        decoder.reset();

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.INCOMPLETE);
        assertThat(decoder.width()).isEqualTo(-1);
        assertThat(decoder.toString()).isEmpty();
    }

    @Test
    public void testCanPushDetectsBoundaryCharacters() {
        SequenceDecoder decoder = new SequenceDecoder();

        assertThat(decoder.canPush('x')).isTrue();
        decoder.push('x');

        assertThat(decoder.canPush('y')).isFalse();
        assertThat(decoder.canPush('\n')).isFalse();
        assertThat(decoder.canPush('\r')).isFalse();
        assertThat(decoder.canPush('\u001B')).isFalse();

        // Probe does not mutate state.
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.CODEPOINT);
    }

    @Test
    public void testCanPushDuringAnsiAndAfterCompletion() {
        SequenceDecoder decoder = new SequenceDecoder();

        assertThat(decoder.canPush('\u001B')).isTrue();
        decoder.push('\u001B');
        assertThat(decoder.canPush('[')).isTrue();
        decoder.push('[');
        assertThat(decoder.canPush('3')).isTrue();
        decoder.push('3');
        assertThat(decoder.canPush('1')).isTrue();
        decoder.push('1');
        assertThat(decoder.canPush('m')).isTrue();
        decoder.push('m');

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.ANSI_ESCAPE_SEQUENCE);
        assertThat(decoder.canPush('x')).isFalse();
    }

    @Test
    public void testFamilySequenceReadinessAndCanPushBoundaries() {
        SequenceDecoder decoder = new SequenceDecoder();

        String man = "\uD83D\uDC68";
        String woman = "\uD83D\uDC69";
        String girl = "\uD83D\uDC67";
        String boy = "\uD83D\uDC66";
        char joiner = '\u200D';
        char extraEmoji = '\u263A';

        pushStringAssertingCanPush(decoder, man);
        assertThat(decoder.isReady()).isTrue();

        assertThat(decoder.canPush(extraEmoji)).isFalse();
        assertThat(decoder.canPush(joiner)).isTrue();
        decoder.push(joiner);
        assertThat(decoder.isReady()).isFalse();

        pushStringAssertingCanPush(decoder, woman);
        assertThat(decoder.isReady()).isTrue();

        assertThat(decoder.canPush(extraEmoji)).isFalse();
        assertThat(decoder.canPush(joiner)).isTrue();
        decoder.push(joiner);
        assertThat(decoder.isReady()).isFalse();

        pushStringAssertingCanPush(decoder, girl);
        assertThat(decoder.isReady()).isTrue();

        assertThat(decoder.canPush(extraEmoji)).isFalse();
        assertThat(decoder.canPush(joiner)).isTrue();
        decoder.push(joiner);
        assertThat(decoder.isReady()).isFalse();

        pushStringAssertingCanPush(decoder, boy);

        assertThat(decoder.isReady()).isTrue();
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.GRAPHEME_CLUSTER);
        assertThat(decoder.toString())
                .isEqualTo(man + joiner + woman + joiner + girl + joiner + boy);
    }

    @Test
    public void testPushCodepointOverloadWithSupplementary() {
        SequenceDecoder decoder = new SequenceDecoder();

        int man = 0x1F468;
        assertThat(decoder.canPush(man)).isTrue();
        decoder.push(man);

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.CODEPOINT);
        assertThat(decoder.toString()).isEqualTo(new String(Character.toChars(man)));
        assertThat(decoder.isReady()).isTrue();
    }

    @Test
    public void testCanPushCodepointOverloadForFamilyJoin() {
        SequenceDecoder decoder = new SequenceDecoder();

        decoder.push(0x1F468);
        assertThat(decoder.canPush(0x1F469)).isFalse();
        assertThat(decoder.canPush(0x200D)).isTrue();

        decoder.push(0x200D);
        assertThat(decoder.isReady()).isFalse();
        assertThat(decoder.canPush(0x1F469)).isTrue();

        decoder.push(0x1F469);
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.GRAPHEME_CLUSTER);
    }

    @Test
    public void testInvalidCodepointOverloadInput() {
        SequenceDecoder decoder = new SequenceDecoder();

        assertThat(decoder.canPush(-1)).isFalse();
        assertThat(decoder.canPush(0x110000)).isFalse();
        assertThat(decoder.canPush(0xD800)).isTrue();
        assertThat(decoder.canPush(0xDC00)).isFalse();

        decoder.push(0x110000);
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.ERROR);
    }

    private static void pushStringAssertingCanPush(SequenceDecoder decoder, String value) {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            assertThat(decoder.canPush(ch)).isTrue();
            decoder.push(ch);
        }
    }

    @Test
    public void testFinishLoneCrAtEofYieldsNewline() {
        SequenceDecoder decoder = new SequenceDecoder();

        decoder.push('\r');
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.INCOMPLETE);
        assertThat(decoder.isReady()).isFalse();

        decoder.finish();

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.CODEPOINT);
        assertThat(decoder.toString()).isEqualTo("\r");
        assertThat(decoder.codepoint()).isEqualTo('\n');
        assertThat(decoder.width()).isEqualTo(0);
    }

    @Test
    public void testFinishAfterCrLfHasNoEffect() {
        SequenceDecoder decoder = new SequenceDecoder();

        // CR+LF is already complete — finish() should leave state unchanged.
        decoder.push('\r');
        decoder.push('\n');
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.CODEPOINT);

        decoder.finish();

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.CODEPOINT);
        assertThat(decoder.toString()).isEqualTo("\r\n");
        assertThat(decoder.codepoint()).isEqualTo('\n');
    }

    @Test
    public void testFinishUnterminatedCsiYieldsAnsiSequence() {
        SequenceDecoder decoder = new SequenceDecoder();

        // Push ESC [ 3 1 — missing final byte 'm'.
        decoder.push('\u001B');
        decoder.push('[');
        decoder.push('3');
        decoder.push('1');
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.INCOMPLETE);

        decoder.finish();

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.ANSI_ESCAPE_SEQUENCE);
        assertThat(decoder.toString()).isEqualTo("\u001B[31");
        assertThat(decoder.width()).isEqualTo(0);
    }

    @Test
    public void testFinishUnterminatedOscYieldsAnsiSequence() {
        SequenceDecoder decoder = new SequenceDecoder();

        // Push ESC ] 0 ; T i t l e — no BEL or ST terminator.
        String osc = "\u001B]0;Title";
        for (int i = 0; i < osc.length(); i++) {
            decoder.push(osc.charAt(i));
        }
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.INCOMPLETE);

        decoder.finish();

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.ANSI_ESCAPE_SEQUENCE);
        assertThat(decoder.toString()).isEqualTo(osc);
        assertThat(decoder.width()).isEqualTo(0);
    }

    @Test
    public void testFinishPendingHighSurrogateYieldsError() {
        SequenceDecoder decoder = new SequenceDecoder();

        decoder.push('\uD834'); // high surrogate, low surrogate never arrives
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.INCOMPLETE);

        decoder.finish();

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.ERROR);
    }

    @Test
    public void testFinishOnCompleteStateIsNoOp() {
        SequenceDecoder decoder = new SequenceDecoder();

        decoder.push('A');
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.CODEPOINT);

        decoder.finish();

        // Already complete — finish() must not change state or codepoints.
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.CODEPOINT);
        assertThat(decoder.toString()).isEqualTo("A");
    }

    @Test
    public void testFinishOnErrorStateIsNoOp() {
        SequenceDecoder decoder = new SequenceDecoder();

        decoder.push(0x110000); // invalid code point → ERROR
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.ERROR);

        decoder.finish();

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.ERROR);
    }

    @Test
    public void testFinishOnEmptyDecoderIsNoOp() {
        SequenceDecoder decoder = new SequenceDecoder();

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.INCOMPLETE);

        decoder.finish();

        // Nothing was pushed — finish() on an empty decoder should leave it INCOMPLETE.
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.INCOMPLETE);
    }

    // -------------------------------------------------------------------------
    // shouldBreak consistency — variation selectors stay attached
    // (tested indirectly via SequenceDecoder)
    // -------------------------------------------------------------------------

    @Test
    public void testVS16StaysAttachedToBase() {
        // ☎ + VS16: the variation selector must NOT cause a break — the decoder
        // should yield a single GRAPHEME_CLUSTER, not split into two CODEPOINTs.
        SequenceDecoder decoder = new SequenceDecoder();
        decoder.push('\u260E');
        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.CODEPOINT);
        assertThat(decoder.canPush('\uFE0F')).isTrue();

        decoder.push('\uFE0F');

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.GRAPHEME_CLUSTER);
        assertThat(decoder.toString()).isEqualTo("\u260E\uFE0F");
        // Width should be wide because of VS16
        assertThat(Unicode.isWide(decoder.toString())).isTrue();
    }

    @Test
    public void testVS15StaysAttachedToBase() {
        // ☎ + VS15: variation selector must not cause a break either
        SequenceDecoder decoder = new SequenceDecoder();
        decoder.push('\u260E');
        assertThat(decoder.canPush('\uFE0E')).isTrue();

        decoder.push('\uFE0E');

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.GRAPHEME_CLUSTER);
        assertThat(decoder.toString()).isEqualTo("\u260E\uFE0E");
        // Width should be narrow because of VS15
        assertThat(Unicode.isWide(decoder.toString())).isFalse();
    }

    @Test
    public void testVS16DoesNotAttachToSecondCodepoint() {
        // After a complete codepoint, a VS on a *different* base must not attach to the first
        SequenceDecoder decoder = new SequenceDecoder();
        decoder.push('A');
        assertThat(decoder.canPush('\uFE0F')).isFalse();
    }

    // -------------------------------------------------------------------------
    // shouldBreak consistency — regional indicator pairing
    // -------------------------------------------------------------------------

    @Test
    public void testTwoRegionalIndicatorsFormSingleCluster() {
        SequenceDecoder decoder = new SequenceDecoder();
        pushString(decoder, new String(Character.toChars(0x1F1FA))); // 🇺
        assertThat(decoder.isReady()).isTrue();
        assertThat(decoder.canPush(0x1F1F8)).isTrue(); // 🇸 can attach

        decoder.push(0x1F1F8);

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.GRAPHEME_CLUSTER);
        assertThat(decoder.isReady()).isTrue();
        // A third regional indicator must NOT attach (would start a new flag)
        assertThat(decoder.canPush(0x1F1FA)).isFalse();
        assertThat(Unicode.isWide(decoder.toString())).isTrue();
    }

    @Test
    public void testThirdRegionalIndicatorDoesNotAttach() {
        // Verify riCount logic: after 2 RI, a 3rd must break
        SequenceDecoder decoder = new SequenceDecoder();
        decoder.push(0x1F1FA);
        decoder.push(0x1F1F8);
        assertThat(decoder.canPush(0x1F1FA)).isFalse();
    }

    // -------------------------------------------------------------------------
    // shouldBreak consistency — ZWJ keeps emoji joined
    // -------------------------------------------------------------------------

    @Test
    public void testZwjPreventsBreak() {
        SequenceDecoder decoder = new SequenceDecoder();
        decoder.push(0x1F468); // 👨
        assertThat(decoder.canPush(0x200D)).isTrue();
        decoder.push(0x200D);
        assertThat(decoder.isReady()).isFalse(); // ZWJ tail — not yet ready

        assertThat(decoder.canPush(0x1F469)).isTrue(); // 👩 can follow
        decoder.push(0x1F469);

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.GRAPHEME_CLUSTER);
        assertThat(decoder.isReady()).isTrue();
    }

    // -------------------------------------------------------------------------
    // shouldBreak consistency — combining marks stay attached
    // -------------------------------------------------------------------------

    @Test
    public void testNonSpacingMarkStaysAttached() {
        // Combining grave accent (U+0300) is NON_SPACING_MARK — must not break
        SequenceDecoder decoder = new SequenceDecoder();
        decoder.push('a');
        assertThat(decoder.canPush('\u0300')).isTrue();
        decoder.push('\u0300');

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.GRAPHEME_CLUSTER);
        assertThat(decoder.toString()).isEqualTo("a\u0300");
        assertThat(Unicode.isWide(decoder.toString())).isFalse();
    }

    @Test
    public void testCombiningSpacingMarkStaysAttached() {
        // Devanagari vowel sign AA (U+093E) is COMBINING_SPACING_MARK
        SequenceDecoder decoder = new SequenceDecoder();
        decoder.push('\u0915'); // क
        assertThat(decoder.canPush('\u093E')).isTrue();
        decoder.push('\u093E'); // ा

        assertThat(decoder.state()).isEqualTo(SequenceDecoder.State.GRAPHEME_CLUSTER);
        assertThat(decoder.toString()).isEqualTo("\u0915\u093E");
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static void pushString(SequenceDecoder decoder, String s) {
        for (int i = 0; i < s.length(); i++) {
            decoder.push(s.charAt(i));
        }
    }
}
