package org.codejive.twinkle.fluent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Constants;
import org.codejive.twinkle.ansi.Style;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestFluent {

    private StringBuilder sb;
    private Fluent fluent;

    @BeforeEach
    public void setUp() {
        sb = new StringBuilder();
        fluent = Fluent.of(sb);
    }

    // -------------------------------------------------------------------------
    // Text output
    // -------------------------------------------------------------------------

    @Test
    public void testText() {
        fluent.text("Hello, World!");
        assertThat(sb.toString()).isEqualTo("Hello, World!");
    }

    @Test
    public void testTextWithFormatArguments() {
        fluent.text("Hello, %s! Count: %d", "World", 42);
        assertThat(sb.toString()).isEqualTo("Hello, World! Count: 42");
    }

    @Test
    public void testLinefeed() {
        fluent.lf();
        assertThat(sb.toString()).isEqualTo("\n");
    }

    @Test
    public void testTextCombinedWithStyle() {
        fluent.bold().text("important").not().bold();
        assertThat(sb.toString()).isEqualTo(Ansi.bold() + "important" + Ansi.normal());
    }

    // -------------------------------------------------------------------------
    // Style application
    // -------------------------------------------------------------------------

    @Test
    public void testBold() {
        fluent.bold();
        assertThat(sb.toString()).isEqualTo(Ansi.bold());
        assertThat(fluent.style().isBold()).isTrue();
    }

    @Test
    public void testFaint() {
        fluent.faint();
        assertThat(sb.toString()).isEqualTo(Ansi.faint());
        assertThat(fluent.style().isFaint()).isTrue();
    }

    @Test
    public void testNormal() {
        fluent.bold();
        sb.setLength(0);
        fluent.normal();
        assertThat(sb.toString()).isEqualTo(Ansi.normal());
        assertThat(fluent.style().isBold()).isFalse();
        assertThat(fluent.style().isFaint()).isFalse();
    }

    @Test
    public void testItalic() {
        fluent.italic();
        assertThat(sb.toString()).isEqualTo(Ansi.italic());
        assertThat(fluent.style().isItalic()).isTrue();
    }

    @Test
    public void testUnderline() {
        fluent.underline();
        assertThat(sb.toString()).isEqualTo(Ansi.underlined());
        assertThat(fluent.style().isUnderlined()).isTrue();
    }

    @Test
    public void testBlink() {
        fluent.blink();
        assertThat(sb.toString()).isEqualTo(Ansi.blink());
        assertThat(fluent.style().isBlink()).isTrue();
    }

    @Test
    public void testInverse() {
        fluent.inverse();
        assertThat(sb.toString()).isEqualTo(Ansi.inverse());
        assertThat(fluent.style().isInverse()).isTrue();
    }

    @Test
    public void testHidden() {
        fluent.hidden();
        assertThat(sb.toString()).isEqualTo(Ansi.hidden());
        assertThat(fluent.style().isHidden()).isTrue();
    }

    @Test
    public void testStrikethrough() {
        fluent.strikethrough();
        assertThat(sb.toString()).isEqualTo(Ansi.strikethrough());
        assertThat(fluent.style().isStrikethrough()).isTrue();
    }

    @Test
    public void testReset() {
        fluent.bold().italic();
        sb.setLength(0);
        fluent.reset();
        assertThat(sb.toString()).isEqualTo(Ansi.reset());
        assertThat(fluent.style()).isEqualTo(Style.DEFAULT);
    }

    @Test
    public void testMultipleStylesCombined() {
        fluent.bold().italic().underline();
        assertThat(sb.toString()).isEqualTo(Ansi.bold() + Ansi.italic() + Ansi.underlined());
        assertThat(fluent.style().isBold()).isTrue();
        assertThat(fluent.style().isItalic()).isTrue();
        assertThat(fluent.style().isUnderlined()).isTrue();
    }

    // -------------------------------------------------------------------------
    // Style negation
    // -------------------------------------------------------------------------

    @Test
    public void testNotBold() {
        fluent.not().bold();
        assertThat(sb.toString()).isEqualTo(Ansi.normal());
    }

    @Test
    public void testFaintNotBold() {
        fluent.faint().not().bold();
        assertThat(sb.toString()).isEqualTo(Ansi.faint() + Ansi.normal() + Ansi.faint());
    }

    @Test
    public void testNotFaint() {
        fluent.not().faint();
        assertThat(sb.toString()).isEqualTo(Ansi.normal());
    }

    @Test
    public void testBoldNotFaint() {
        fluent.bold().not().faint();
        assertThat(sb.toString()).isEqualTo(Ansi.bold() + Ansi.normal() + Ansi.bold());
    }

    @Test
    public void testNotItalic() {
        fluent.italic();
        sb.setLength(0);
        fluent.not().italic();
        assertThat(sb.toString()).isEqualTo(Ansi.italicOff());
        assertThat(fluent.style().isItalic()).isFalse();
    }

    @Test
    public void testNotUnderline() {
        fluent.underline();
        sb.setLength(0);
        fluent.not().underline();
        assertThat(sb.toString()).isEqualTo(Ansi.underlinedOff());
        assertThat(fluent.style().isUnderlined()).isFalse();
    }

    @Test
    public void testNotBlink() {
        fluent.blink();
        sb.setLength(0);
        fluent.not().blink();
        assertThat(sb.toString()).isEqualTo(Ansi.blinkOff());
        assertThat(fluent.style().isBlink()).isFalse();
    }

    @Test
    public void testNotInverse() {
        fluent.inverse();
        sb.setLength(0);
        fluent.not().inverse();
        assertThat(sb.toString()).isEqualTo(Ansi.inverseOff());
        assertThat(fluent.style().isInverse()).isFalse();
    }

    @Test
    public void testNotHidden() {
        fluent.hidden();
        sb.setLength(0);
        fluent.not().hidden();
        assertThat(sb.toString()).isEqualTo(Ansi.hiddenOff());
        assertThat(fluent.style().isHidden()).isFalse();
    }

    @Test
    public void testNotStrikethrough() {
        fluent.strikethrough();
        sb.setLength(0);
        fluent.not().strikethrough();
        assertThat(sb.toString()).isEqualTo(Ansi.strikethroughOff());
        assertThat(fluent.style().isStrikethrough()).isFalse();
    }

    // -------------------------------------------------------------------------
    // Colors
    // -------------------------------------------------------------------------

    @Test
    public void testForegroundColorByObject() {
        fluent.color(Color.BasicColor.RED);
        assertThat(sb.toString()).isEqualTo(Color.BasicColor.RED.toAnsiFg());
    }

    @Test
    public void testForegroundColorByRgb() {
        fluent.color(255, 128, 0);
        assertThat(sb.toString()).isEqualTo(Color.rgb(255, 128, 0).toAnsiFg());
    }

    @Test
    public void testForegroundColorByIndex() {
        fluent.color(42);
        assertThat(sb.toString()).isEqualTo(Color.indexed(42).toAnsiFg());
    }

    @Test
    public void testBackgroundColorByObject() {
        fluent.background().color(Color.BasicColor.BLUE);
        assertThat(sb.toString()).isEqualTo(Color.BasicColor.BLUE.toAnsiBg());
    }

    @Test
    public void testBackgroundColorByRgb() {
        fluent.background().color(0, 128, 255);
        assertThat(sb.toString()).isEqualTo(Color.rgb(0, 128, 255).toAnsiBg());
    }

    @Test
    public void testBackgroundColorByIndex() {
        fluent.background().color(100);
        assertThat(sb.toString()).isEqualTo(Color.indexed(100).toAnsiBg());
    }

    @Test
    public void testBgAliasForBackground() {
        // bg() is an alias for background()
        fluent.bg().color(Color.BasicColor.GREEN);
        assertThat(sb.toString()).isEqualTo(Color.BasicColor.GREEN.toAnsiBg());
    }

    // -------------------------------------------------------------------------
    // Cursor movement
    // -------------------------------------------------------------------------

    @Test
    public void testCursorHome() {
        fluent.home();
        assertThat(sb.toString()).isEqualTo(Ansi.cursorHome());
    }

    @Test
    public void testCursorAt() {
        fluent.at(5, 3);
        assertThat(sb.toString()).isEqualTo(Ansi.cursorPos(5, 3));
    }

    @Test
    public void testCursorUp() {
        fluent.up(2);
        assertThat(sb.toString()).isEqualTo(Ansi.cursorUp(2));
    }

    @Test
    public void testCursorDown() {
        fluent.down(3);
        assertThat(sb.toString()).isEqualTo(Ansi.cursorDown(3));
    }

    @Test
    public void testCursorForward() {
        fluent.forward(4);
        assertThat(sb.toString()).isEqualTo(Ansi.cursorForward(4));
    }

    @Test
    public void testCursorBackward() {
        fluent.backward(2);
        assertThat(sb.toString()).isEqualTo(Ansi.cursorBackward(2));
    }

    @Test
    public void testCursorColumn() {
        fluent.column(10);
        assertThat(sb.toString()).isEqualTo(Ansi.cursorToColumn(10));
    }

    @Test
    public void testCursorNext() {
        fluent.next(1);
        assertThat(sb.toString()).isEqualTo(Constants.CSI + 1 + Constants.CURSOR_NEXT_LINE_CMD);
    }

    @Test
    public void testCursorPrev() {
        fluent.prev(1);
        assertThat(sb.toString()).isEqualTo(Constants.CSI + 1 + Constants.CURSOR_PREV_LINE_CMD);
    }

    @Test
    public void testCursorHide() {
        fluent.hide();
        assertThat(sb.toString()).isEqualTo(Ansi.cursorHide());
    }

    @Test
    public void testCursorShow() {
        fluent.show();
        assertThat(sb.toString()).isEqualTo(Ansi.cursorShow());
    }

    @Test
    public void testCursorMark() {
        fluent.mark();
        assertThat(sb.toString()).isEqualTo(Ansi.cursorSave());
    }

    @Test
    public void testCursorJump() {
        fluent.jump();
        assertThat(sb.toString()).isEqualTo(Ansi.cursorRestore());
    }

    // -------------------------------------------------------------------------
    // Screen commands
    // -------------------------------------------------------------------------

    @Test
    public void testScreenClear() {
        fluent.screen().clear();
        assertThat(sb.toString()).isEqualTo(Constants.CSI + Constants.SCREEN_ERASE_FULL);
    }

    @Test
    public void testScreenClearToEnd() {
        fluent.screen().clearToEnd();
        assertThat(sb.toString()).isEqualTo(Constants.CSI + Constants.SCREEN_ERASE_END);
    }

    @Test
    public void testScreenClearToStart() {
        fluent.screen().clearToStart();
        assertThat(sb.toString()).isEqualTo(Constants.CSI + Constants.SCREEN_ERASE_START);
    }

    @Test
    public void testScreenAlternate() {
        fluent.screen().alt();
        assertThat(sb.toString()).isEqualTo(Constants.CSI + Constants.SCREEN_SAVE);
    }

    @Test
    public void testScreenRestore() {
        fluent.screen().restore();
        assertThat(sb.toString()).isEqualTo(Constants.CSI + Constants.SCREEN_RESTORE);
    }

    // -------------------------------------------------------------------------
    // Line commands
    // -------------------------------------------------------------------------

    @Test
    public void testLineClear() {
        fluent.line().clear();
        assertThat(sb.toString()).isEqualTo(Constants.CSI + Constants.LINE_ERASE_FULL);
    }

    @Test
    public void testLineClearToEnd() {
        fluent.line().clearToEnd();
        assertThat(sb.toString()).isEqualTo(Constants.CSI + Constants.LINE_ERASE_END);
    }

    @Test
    public void testLineClearToStart() {
        fluent.line().clearToStart();
        assertThat(sb.toString()).isEqualTo(Constants.CSI + Constants.LINE_ERASE_START);
    }

    // -------------------------------------------------------------------------
    // Hyperlinks
    // -------------------------------------------------------------------------

    @Test
    public void testUrl() {
        fluent.url("https://example.com");
        assertThat(sb.toString()).isEqualTo(Ansi.link("https://example.com"));
    }

    @Test
    public void testUrlWithId() {
        fluent.url("https://example.com", "myid");
        assertThat(sb.toString()).isEqualTo(Ansi.link("https://example.com", "myid"));
    }

    @Test
    public void testLru() {
        fluent.lru();
        assertThat(sb.toString()).isEqualTo(Ansi.linkEnd());
    }

    @Test
    public void testLink() {
        fluent.link("click here", "https://example.com");
        assertThat(sb.toString())
                .isEqualTo(Ansi.link("https://example.com") + "click here" + Ansi.linkEnd());
    }

    @Test
    public void testLinkWithId() {
        fluent.link("click here", "https://example.com", "link-1");
        assertThat(sb.toString())
                .isEqualTo(
                        Ansi.link("https://example.com", "link-1") + "click here" + Ansi.linkEnd());
    }

    // -------------------------------------------------------------------------
    // Style state management
    // -------------------------------------------------------------------------

    @Test
    public void testInitialStyle() {
        assertThat(fluent.style()).isEqualTo(Style.DEFAULT);
    }

    @Test
    public void testStyleTrackingAfterBold() {
        fluent.bold();
        assertThat(fluent.style().isBold()).isTrue();
        assertThat(fluent.style().isItalic()).isFalse();
    }

    @Test
    public void testPushPop() {
        fluent.bold();
        Style styleBeforePush = fluent.style();
        fluent.push();
        fluent.italic();
        assertThat(fluent.style()).isNotEqualTo(styleBeforePush);
        fluent.pop();
        assertThat(fluent.style()).isEqualTo(styleBeforePush);
    }

    @Test
    public void testPushPopRestoresForegroundColor() {
        Color baseColor = Color.BasicColor.MAGENTA.bright();
        fluent.color(baseColor);

        fluent.push();
        fluent.color(Color.BasicColor.WHITE);
        fluent.pop();

        assertThat(sb.toString())
                .isEqualTo(
                        baseColor.toAnsiFg()
                                + Color.BasicColor.WHITE.toAnsiFg()
                                + baseColor.toAnsiFg());
        assertThat(fluent.style().affectsFgColor()).isTrue();
        assertThat(fluent.style().fgColor()).isEqualTo(baseColor);
    }

    @Test
    public void testPushPopRestoresBackgroundColor() {
        Color baseColor = Color.BasicColor.BLUE;
        fluent.background().color(baseColor);

        fluent.push();
        fluent.background().color(Color.BasicColor.RED);
        fluent.pop();

        assertThat(sb.toString())
                .isEqualTo(
                        baseColor.toAnsiBg()
                                + Color.BasicColor.RED.toAnsiBg()
                                + baseColor.toAnsiBg());
        assertThat(fluent.style().affectsBgColor()).isTrue();
        assertThat(fluent.style().bgColor()).isEqualTo(baseColor);
    }

    @Test
    public void testPushWithStyle() {
        Style newStyle = Style.UNSTYLED.italic();
        fluent.push(newStyle);
        assertThat(fluent.style().isItalic()).isTrue();
        fluent.pop();
        assertThat(fluent.style()).isEqualTo(Style.DEFAULT);
    }

    @Test
    public void testPushWithStyleEmitsAnsiTransition() {
        Style newStyle = Style.UNSTYLED.italic();
        StringBuilder expectedPush = new StringBuilder();
        Style.DEFAULT.diff(newStyle).toAnsi(expectedPush, Style.DEFAULT);

        fluent.push(newStyle);
        assertThat(sb.toString()).isEqualTo(expectedPush.toString());

        sb.setLength(0);
        StringBuilder expectedPop = new StringBuilder();
        newStyle.diff(Style.DEFAULT).toAnsi(expectedPop, newStyle);
        fluent.pop();
        assertThat(sb.toString()).isEqualTo(expectedPop.toString());
    }

    @Test
    public void testPopOnEmptyStackDoesNothing() {
        String before = sb.toString();
        fluent.pop();
        assertThat(sb.toString()).isEqualTo(before);
    }

    @Test
    public void testRestoreReturnsToDefaultStyle() {
        fluent.bold().italic();
        assertThat(fluent.style().isBold()).isTrue();
        assertThat(fluent.style().isItalic()).isTrue();
        fluent.restore();
        assertThat(fluent.style()).isEqualTo(Style.DEFAULT);
    }

    @Test
    public void testRestoreWithCustomStartingStyle() {
        Style startingStyle = Style.UNSTYLED.bold();
        Fluent customFluent = Fluent.of(new StringBuilder(), startingStyle);
        customFluent.italic();
        assertThat(customFluent.style().isItalic()).isTrue();
        customFluent.restore();
        assertThat(customFluent.style()).isEqualTo(startingStyle);
    }

    @Test
    public void testStringFluent() {
        Fluent stringFluent = Fluent.string();
        stringFluent.bold().text("Hello").not().bold();
        assertThat(stringFluent.toString()).isEqualTo(Ansi.bold() + "Hello" + Ansi.normal());
    }

    @Test
    public void testFormatMarkupOrdering() {
        Fluent stringFluent = Fluent.string();
        // Markup is applied first, then formatting, so markup is invalid and should be ignored
        stringFluent.markup("{%s}", "bold");
        assertThat(stringFluent.toString()).isEqualTo("");
    }

    @Test
    public void testMarkupAppendable() {
        RecordingAppendable recapp = new RecordingAppendable();
        Fluent fluent = Fluent.of(recapp);
        String txt = "this could be a very large string";
        fluent.markup("{i}%s{/i}", txt);
        assertThat(recapp.calls).hasSize(3);
        assertThat(recapp.calls.get(0)).isEqualTo(Ansi.italic());
        assertThat(recapp.calls.get(1)).isEqualTo(txt);
        assertThat(recapp.calls.get(2)).isEqualTo(Ansi.italicOff());
    }

    public static class RecordingAppendable implements Appendable {
        public ArrayList<CharSequence> calls = new ArrayList<>();

        @Override
        public Appendable append(CharSequence csq) {
            calls.add(csq);
            return this;
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) {
            calls.add(csq.subSequence(start, end));
            return this;
        }

        @Override
        public Appendable append(char c) {
            calls.add("" + c);
            return this;
        }
    }
}
