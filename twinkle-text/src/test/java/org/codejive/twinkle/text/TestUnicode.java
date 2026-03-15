package org.codejive.twinkle.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class TestUnicode {

    // -------------------------------------------------------------------------
    // isWide(String) — basic narrow / wide cases
    // -------------------------------------------------------------------------

    @Test
    public void testIsWideStringNull() {
        assertThat(Unicode.isWide(null)).isFalse();
    }

    @Test
    public void testIsWideStringEmpty() {
        assertThat(Unicode.isWide("")).isFalse();
    }

    @Test
    public void testIsWideStringNarrowLatin() {
        // Plain ASCII letter — always narrow
        assertThat(Unicode.isWide("A")).isFalse();
    }

    @Test
    public void testIsWideStringCjk() {
        // CJK Unified Ideograph U+4E2D (中) — wide
        assertThat(Unicode.isWide("中")).isTrue();
    }

    @Test
    public void testIsWideStringEmoji() {
        // 😀 U+1F600 — wide emoji
        assertThat(Unicode.isWide("😀")).isTrue();
    }

    @Test
    public void testIsWideStringFullwidthLatin() {
        // Fullwidth Latin 'Ａ' U+FF21 — wide
        assertThat(Unicode.isWide("Ａ")).isTrue();
    }

    // -------------------------------------------------------------------------
    // isWide(String) — combining / diacritic grapheme clusters (narrow)
    // -------------------------------------------------------------------------

    @Test
    public void testIsWideStringCombiningAccentIsNarrow() {
        // 'e' + combining acute accent (U+0301) — grapheme cluster, but still narrow
        assertThat(Unicode.isWide("é")).isFalse();
    }

    @Test
    public void testIsWideStringHangulWithTrailingJamo() {
        // Hangul L + V + T forms a grapheme cluster; base L codepoint is wide
        // U+1100 (ᄀ) + U+1161 (ᅡ) + U+11A8 (ᆨ)
        assertThat(Unicode.isWide("각")).isTrue();
    }

    // -------------------------------------------------------------------------
    // isWide(String) — variation selectors
    // -------------------------------------------------------------------------

    @Test
    public void testIsWideStringVS16ForcesWide() {
        // ☎ (U+260E) is a narrow symbol by default, but ☎️ (U+260E + U+FE0F) is emoji-style (wide)
        assertThat(Unicode.isWide("\u260E\uFE0F")).isTrue();
    }

    @Test
    public void testIsWideStringVS15ForcesNarrow() {
        // ☎ (U+260E) with VS15 (U+FE0E) is explicitly text presentation (narrow)
        assertThat(Unicode.isWide("\u260E\uFE0E")).isFalse();
    }

    @Test
    public void testIsWideStringEmojiWithoutVariationSelector() {
        // ☎ alone (U+260E) — no variation selector; isWide(int) decides
        // U+260E is NOT in the emoji ranges defined by isEmoji(), and not in East Asian Wide,
        // so it should be narrow without a variation selector
        assertThat(Unicode.isWide("\u260E")).isFalse();
    }

    // -------------------------------------------------------------------------
    // isWide(String) — regional indicator pairs (flag emoji)
    // -------------------------------------------------------------------------

    @Test
    public void testIsWideStringRegionalIndicatorPairIsWide() {
        // 🇺🇸 = U+1F1FA + U+1F1F8 — a flag emoji, always wide
        String flag =
                new String(Character.toChars(0x1F1FA)) + new String(Character.toChars(0x1F1F8));
        assertThat(Unicode.isWide(flag)).isTrue();
    }

    @Test
    public void testIsWideStringSingleRegionalIndicatorIsWide() {
        // A lone regional indicator has no paired partner; falls through to isWide(int).
        // Regional indicators (0x1F1E6–0x1F1FF) are in the emoji range, so isWide(int) returns
        // true.
        String single = new String(Character.toChars(0x1F1FA));
        assertThat(Unicode.isWide(single)).isTrue();
    }

    // -------------------------------------------------------------------------
    // isWide(String) — ZWJ sequence (width driven by base codepoint)
    // -------------------------------------------------------------------------

    @Test
    public void testIsWideStringZwjSequenceIsWide() {
        // 👨‍👩 = man ZWJ woman — base is wide emoji, so the whole cluster is wide
        String man = new String(Character.toChars(0x1F468));
        String woman = new String(Character.toChars(0x1F469));
        String zwj = "\u200D";
        assertThat(Unicode.isWide(man + zwj + woman)).isTrue();
    }
}
