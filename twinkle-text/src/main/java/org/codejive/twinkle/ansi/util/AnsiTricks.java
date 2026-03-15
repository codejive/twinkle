package org.codejive.twinkle.ansi.util;

import java.io.IOException;
import org.codejive.twinkle.ansi.Ansi;
import org.jspecify.annotations.NonNull;

public class AnsiTricks {

    /**
     * Appends the given text to the appendable. When the text consists of multiple lines, the
     * cursor will each time be moved down from the starting point of the previous line, so that the
     * text will be printed in a block.
     *
     * @param text the text to be blockified
     */
    public static String blockify(@NonNull String text) {
        StringBuilder sb = new StringBuilder();
        blockify(sb, text);
        return sb.toString();
    }

    /**
     * Appends the given text to the appendable. When the text consists of multiple lines, the
     * cursor will each time be moved down from the starting point of the previous line, so that the
     * text will be printed in a block.
     *
     * @param appendable the Appendable to which the text will be appended
     * @param text the text to be blockified
     */
    public static void blockify(@NonNull Appendable appendable, @NonNull String text) {
        try {
            String[] parts = text.split("(\r)?\n");
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    // We keep the linefeed for two reasons:
                    // - we might need the screen to scroll up, something
                    //    cursorDown won't do
                    // - the caller might still want to know how many lines
                    //   there are in the text by splitting on newlines
                    appendable.append('\n');
                    appendable.append(Ansi.cursorRestore());
                    appendable.append(Ansi.cursorDown(1));
                }
                if (i < parts.length - 1) {
                    appendable.append(Ansi.cursorSave());
                }
                appendable.append(parts[i]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
