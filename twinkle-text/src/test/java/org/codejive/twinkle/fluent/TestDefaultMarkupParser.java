package org.codejive.twinkle.fluent;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.fluent.impl.DefaultMarkupParser;
import org.codejive.twinkle.fluent.impl.FluentImpl;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DefaultMarkupParser}.
 *
 * <p>Each test creates a {@link FluentImpl} backed by a {@link StringBuilder} and passes it to a
 * {@link DefaultMarkupParser} instance. The same {@link StringBuilder} is used as the target
 * appendable for {@code parse()}, so plain text and ANSI escape sequences land in the same buffer
 * in the correct order.
 */
public class TestDefaultMarkupParser {

    /**
     * Creates a fresh FluentImpl + DefaultMarkup pair that both write to the returned
     * StringBuilder.
     */
    private static class Setup {
        final StringBuilder sb = new StringBuilder();
        final FluentImpl fluent = FluentImpl.of(sb, Style.DEFAULT);
        final DefaultMarkupParser markup = new DefaultMarkupParser();

        void parse(String text) {
            markup.parse(fluent, text);
        }

        String result() {
            return sb.toString();
        }
    }

    // -------------------------------------------------------------------------
    // Plain text (no markup)
    // -------------------------------------------------------------------------

    @Test
    public void testPlainText() {
        Setup s = new Setup();
        s.parse("hello world");
        assertThat(s.result()).isEqualTo("hello world");
    }

    @Test
    public void testEmptyString() {
        Setup s = new Setup();
        s.parse("");
        assertThat(s.result()).isEmpty();
    }

    @Test
    public void testTextAroundMarkup() {
        Setup s = new Setup();
        s.parse("before{bold}between{reset}after");
        assertThat(s.result())
                .isEqualTo("before" + Ansi.bold() + "between" + Ansi.reset() + "after");
    }

    // -------------------------------------------------------------------------
    // Style markup
    // -------------------------------------------------------------------------

    @Test
    public void testBoldMarkup() {
        Setup s = new Setup();
        s.parse("{bold}text");
        assertThat(s.result()).isEqualTo(Ansi.bold() + "text");
    }

    @Test
    public void testBoldShorthand() {
        Setup s = new Setup();
        s.parse("{b}text");
        assertThat(s.result()).isEqualTo(Ansi.bold() + "text");
    }

    @Test
    public void testFaintMarkup() {
        Setup s = new Setup();
        s.parse("{faint}text");
        assertThat(s.result()).isEqualTo(Ansi.faint() + "text");
    }

    @Test
    public void testItalicMarkup() {
        Setup s = new Setup();
        s.parse("{italic}text");
        assertThat(s.result()).isEqualTo(Ansi.italic() + "text");
    }

    @Test
    public void testItalicShorthand() {
        Setup s = new Setup();
        s.parse("{i}text");
        assertThat(s.result()).isEqualTo(Ansi.italic() + "text");
    }

    @Test
    public void testUnderlineMarkup() {
        Setup s = new Setup();
        s.parse("{underline}text");
        assertThat(s.result()).isEqualTo(Ansi.underlined() + "text");
    }

    @Test
    public void testUnderlineShorthand() {
        Setup s = new Setup();
        s.parse("{ul}text");
        assertThat(s.result()).isEqualTo(Ansi.underlined() + "text");
    }

    @Test
    public void testBlinkMarkup() {
        Setup s = new Setup();
        s.parse("{blink}text");
        assertThat(s.result()).isEqualTo(Ansi.blink() + "text");
    }

    @Test
    public void testInverseMarkup() {
        Setup s = new Setup();
        s.parse("{inverse}text");
        assertThat(s.result()).isEqualTo(Ansi.inverse() + "text");
    }

    @Test
    public void testInverseShorthand() {
        Setup s = new Setup();
        s.parse("{inv}text");
        assertThat(s.result()).isEqualTo(Ansi.inverse() + "text");
    }

    @Test
    public void testHiddenMarkup() {
        Setup s = new Setup();
        s.parse("{hidden}text");
        assertThat(s.result()).isEqualTo(Ansi.hidden() + "text");
    }

    @Test
    public void testStrikethroughMarkup() {
        Setup s = new Setup();
        s.parse("{strikethrough}text");
        assertThat(s.result()).isEqualTo(Ansi.strikethrough() + "text");
    }

    @Test
    public void testStrikeShorthand() {
        Setup s = new Setup();
        s.parse("{strike}text");
        assertThat(s.result()).isEqualTo(Ansi.strikethrough() + "text");
    }

    @Test
    public void testNormalMarkup() {
        Setup s = new Setup();
        s.parse("{normal}");
        assertThat(s.result()).isEqualTo(Ansi.normal());
    }

    @Test
    public void testResetMarkup() {
        Setup s = new Setup();
        s.parse("{reset}");
        assertThat(s.result()).isEqualTo(Ansi.reset());
    }

    @Test
    public void testBoldOffMarkup() {
        Setup s = new Setup();
        s.parse("{bold}on{/bold}off");
        assertThat(s.result()).isEqualTo(Ansi.bold() + "on" + Ansi.normal() + "off");
    }

    @Test
    public void testBoldOffShorthandMarkup() {
        Setup s = new Setup();
        s.parse("{bold}on{/b}off");
        assertThat(s.result()).isEqualTo(Ansi.bold() + "on" + Ansi.normal() + "off");
    }

    @Test
    public void testItalicOffMarkup() {
        Setup s = new Setup();
        s.parse("{italic}on{/italic}off");
        assertThat(s.result()).isEqualTo(Ansi.italic() + "on" + Ansi.italicOff() + "off");
    }

    @Test
    public void testRedColorMarkup() {
        Setup s = new Setup();
        s.parse("{red}text");
        assertThat(s.result()).isEqualTo(Color.BasicColor.RED.toAnsiFg() + "text");
    }

    @Test
    public void testGreenColorMarkup() {
        Setup s = new Setup();
        s.parse("{green}text");
        assertThat(s.result()).isEqualTo(Color.BasicColor.GREEN.toAnsiFg() + "text");
    }

    @Test
    public void testBlueColorMarkup() {
        Setup s = new Setup();
        s.parse("{blue}text");
        assertThat(s.result()).isEqualTo(Color.BasicColor.BLUE.toAnsiFg() + "text");
    }

    @Test
    public void testBrightRedColorMarkup() {
        Setup s = new Setup();
        s.parse("{brightred}text");
        assertThat(s.result()).isEqualTo(Color.BasicColor.RED.bright().toAnsiFg() + "text");
    }

    @Test
    public void testDarkBlueColorMarkup() {
        Setup s = new Setup();
        s.parse("{darkblue}text");
        assertThat(s.result()).isEqualTo(Color.BasicColor.BLUE.dark().toAnsiFg() + "text");
    }

    @Test
    public void testRgbColorMarkup() {
        Setup s = new Setup();
        s.parse("{#FF8000}text");
        assertThat(s.result()).isEqualTo(Color.rgb(0xFF, 0x80, 0x00).toAnsiFg() + "text");
    }

    @Test
    public void testBackgroundRedMarkup() {
        Setup s = new Setup();
        s.parse("{bg:red}text");
        assertThat(s.result()).isEqualTo(Color.BasicColor.RED.toAnsiBg() + "text");
    }

    @Test
    public void testBackgroundRgbMarkup() {
        Setup s = new Setup();
        s.parse("{bg:#0000FF}text");
        assertThat(s.result()).isEqualTo(Color.rgb(0x00, 0x00, 0xFF).toAnsiBg() + "text");
    }

    @Test
    public void testMultipleMarkupInOneString() {
        Setup s = new Setup();
        s.parse("A{bold}B{i}{red}C{/i}D{/bold}E");
        assertThat(s.result())
                .isEqualTo(
                        "A"
                                + Ansi.bold()
                                + "B"
                                + Ansi.italic()
                                + Color.BasicColor.RED.toAnsiFg()
                                + "C"
                                + Ansi.italicOff()
                                + "D"
                                + Ansi.normal()
                                + "E");
    }

    @Test
    public void testMultipleMarkupInOneString2() {
        Setup s = new Setup();
        s.parse("A{brightmagenta}B{white}C{brightmagenta}D");
        assertThat(s.result())
                .isEqualTo(
                        "A"
                                + Color.BasicColor.MAGENTA.bright().toAnsiFg()
                                + "B"
                                + Color.BasicColor.WHITE.toAnsiFg()
                                + "C"
                                + Color.BasicColor.MAGENTA.bright().toAnsiFg()
                                + "D");
    }

    @Test
    public void testMarkupPushPopInOneString() {
        Setup s = new Setup();
        s.parse("A{brightmagenta}{+}B{white}C{-}D");
        assertThat(s.result())
                .isEqualTo(
                        "A"
                                + Color.BasicColor.MAGENTA.bright().toAnsiFg()
                                + "B"
                                + Color.BasicColor.WHITE.toAnsiFg()
                                + "C"
                                + Color.BasicColor.MAGENTA.bright().toAnsiFg()
                                + "D");
    }

    @Test
    public void testMultipleMarkupInOneStringWithBackgroundPopRestore() {
        Setup s = new Setup();
        s.parse("A{bg:blue}{+}B{bg:white}C{-}D");
        assertThat(s.result())
                .isEqualTo(
                        "A"
                                + Color.BasicColor.BLUE.toAnsiBg()
                                + "B"
                                + Color.BasicColor.WHITE.toAnsiBg()
                                + "C"
                                + Color.BasicColor.BLUE.toAnsiBg()
                                + "D");
    }

    @Test
    public void testOverlappingCloseTags() {
        Setup s = new Setup();
        s.parse("{ul}{i}{/ul}{/i}");
        assertThat(s.result())
                .isEqualTo(
                        Ansi.underlined()
                                + Ansi.italic()
                                + Ansi.underlinedOff()
                                + Ansi.italicOff());
    }

    @Test
    public void testPositionMarkup() {
        Setup s = new Setup();
        s.parse("{~5,3}");
        assertThat(s.result()).isEqualTo(Ansi.cursorPos(5, 3));
    }

    @Test
    public void testColumnMarkup() {
        Setup s = new Setup();
        s.parse("{~10}");
        assertThat(s.result()).isEqualTo(Ansi.cursorToColumn(10));
    }

    @Test
    public void testHomeMarkup() {
        Setup s = new Setup();
        s.parse("{home}");
        assertThat(s.result()).isEqualTo(Ansi.cursorHome());
    }

    @Test
    public void testHomeTildeShorthand() {
        Setup s = new Setup();
        s.parse("{~}");
        assertThat(s.result()).isEqualTo(Ansi.cursorHome());
    }

    @Test
    public void testMarkCursorMarkup() {
        Setup s = new Setup();
        s.parse("{mark}");
        assertThat(s.result()).isEqualTo(Ansi.cursorSave());
    }

    @Test
    public void testMarkCursorAtShorthand() {
        Setup s = new Setup();
        s.parse("{@}");
        assertThat(s.result()).isEqualTo(Ansi.cursorSave());
    }

    @Test
    public void testJumpCursorMarkup() {
        Setup s = new Setup();
        s.parse("{jump}");
        assertThat(s.result()).isEqualTo(Ansi.cursorRestore());
    }

    @Test
    public void testJumpCursorCaretShorthand() {
        Setup s = new Setup();
        s.parse("{^}");
        assertThat(s.result()).isEqualTo(Ansi.cursorRestore());
    }

    @Test
    public void testHyperlinkMarkup() {
        Setup s = new Setup();
        s.parse("{https://example.com}link text{/}");
        assertThat(s.result())
                .isEqualTo(Ansi.link("https://example.com") + "link text" + Ansi.linkEnd());
    }

    @Test
    public void testHyperlinkEndOnlyMarkup() {
        Setup s = new Setup();
        s.parse("{/}");
        assertThat(s.result()).isEqualTo(Ansi.linkEnd());
    }

    @Test
    public void testPushPlusShorthand() {
        Setup s = new Setup();
        // {+} pushes current style, {-} pops it back – state should be restored
        s.fluent.italic();
        Style styleBeforePush = s.fluent.style();
        s.parse("{+}{bold}{-}");
        assertThat(s.fluent.style()).isEqualTo(styleBeforePush);
    }

    @Test
    public void testPushPopKeywords() {
        Setup s = new Setup();
        s.fluent.underline();
        Style styleBeforePush = s.fluent.style();
        s.parse("{push}{italic}{pop}");
        assertThat(s.fluent.style()).isEqualTo(styleBeforePush);
    }

    @Test
    public void testUnknownMarkupIsIgnored() {
        Setup s = new Setup();
        s.parse("before{unknown_tag}after");
        assertThat(s.result()).isEqualTo("beforeafter");
    }

    @Test
    public void testInvalidRgbColorIsIgnored() {
        Setup s = new Setup();
        s.parse("{#ZZZZZZ}text");
        assertThat(s.result()).isEqualTo("text");
    }

    @Test
    public void testInvalidPositionIsIgnored() {
        Setup s = new Setup();
        s.parse("{~abc,def}text");
        assertThat(s.result()).isEqualTo("text");
    }

    @Test
    public void testTextWithFormatting() {
        Setup s = new Setup();
        s.parse("{i}%s{/i}");
        assertThat(s.result()).isEqualTo(Ansi.italic() + "%s" + Ansi.italicOff());
    }
}
