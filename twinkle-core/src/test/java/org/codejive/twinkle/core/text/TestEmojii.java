package org.codejive.twinkle.core.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class TestEmojii {
    @Test
    public void testEmojiLookup() {
        assertThat(Emoji.emoji("1st_place_medal")).isEqualTo("🥇");
        assertThat(Emoji.emoji("sparkles")).isEqualTo("✨");
    }

    @Test
    public void testUnknownEmoji() {
        assertThat(Emoji.emoji("definitely_not_an_emoji")).isNull();
    }
}


