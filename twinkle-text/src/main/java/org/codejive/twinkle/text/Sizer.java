package org.codejive.twinkle.text;

public class Sizer {
    /**
     * Measure how much space the provided text would take up on a terminal.
     *
     * @param text the text to be displayed
     * @return a `Size` object containing the measured dimensions
     */
    public static Size measure(String text) {
        return measure(StyledIterator.of(text));
    }

    /**
     * Measure how much space the text provided by the iterator would take up on a terminal.
     *
     * @param iterator the text to be displayed
     * @return a `Size` object containing the measured dimensions
     */
    public static Size measure(StyledIterator iterator) {
        int maxWidth = 0;
        int maxHeight = iterator.hasNext() ? 1 : 0;
        int width = 0;
        while (iterator.hasNext()) {
            int cp = iterator.next();
            if (cp == '\n') {
                width = 0;
                maxHeight++;
            } else {
                width += iterator.width();
                maxWidth = Math.max(maxWidth, width);
            }
        }
        return Size.of(maxWidth, maxHeight);
    }

    /**
     * This returns the given string, which can contain lines separated by newlines, where any empty
     * lines at the start and the end are removed and all the lines in between are trimmed of any
     * spaces (like the normal `String.trim()`).
     *
     * @param text the text to trim
     * @return the trimmed text
     */
    public static String trim(String text) {
        String[] lines = text.split("\\n", -1);

        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].trim();
        }

        int first = 0;
        while (first < lines.length && lines[first].isEmpty()) {
            first++;
        }

        if (first == lines.length) {
            return "";
        }

        int last = lines.length - 1;
        while (last >= first && lines[last].isEmpty()) {
            last--;
        }

        return String.join("\n", java.util.Arrays.copyOfRange(lines, first, last + 1));
    }
}
