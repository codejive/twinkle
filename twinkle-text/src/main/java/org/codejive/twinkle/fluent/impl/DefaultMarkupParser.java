package org.codejive.twinkle.fluent.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Hyperlink;
import org.codejive.twinkle.fluent.Fluent;
import org.codejive.twinkle.fluent.MarkupParser;
import org.codejive.twinkle.fluent.commands.ColorCommands;
import org.codejive.twinkle.fluent.commands.NegatableCommands;

/**
 * A markup parser that allows for special markup of the form `{...}`. Each occurrence of those
 * markup elements will be replaced by the corresponding markup feature. Some are simple and fixed,
 * like `{red}` applying a red color. While others can have parameters, like `{~10,5}` moving the
 * cursor to column 10 and row 5.
 */
public class DefaultMarkupParser implements MarkupParser {
    private static Map<String, Color.BasicColor> colors;

    private static final Pattern markupPattern = Pattern.compile("(?<!\\{)\\{([^{}]+)}");
    private static final Pattern varPattern = Pattern.compile("(?<!\\$)\\$(/?\\d+)");

    @Override
    public void parse(Fluent fluent, String textWithMarkup, Object... args) {
        // Call handleMarkup() for each markup pattern found in the text
        applyPattern(
                fluent::plain,
                markupPattern,
                "{",
                textWithMarkup,
                markup -> handleMarkup(fluent, markup, args));
    }

    protected void handleMarkup(Fluent fluent, String markup, Object... args) {
        markup = applySubstitutions(markup, args);

        if (tryStyles(fluent, markup)) {
            return;
        }

        if (markup.startsWith("/") && markup.length() > 1) {
            String closeMarkup = markup.substring(1);
            if (tryStyles(fluent.not(), closeMarkup)) {
                return;
            }
        }

        switch (markup.toLowerCase()) {
            case "normal":
                fluent.normal();
                return;
            case "reset":
                fluent.reset();
                return;
            case "home":
            case "~":
                fluent.home();
                return;
            case "push":
            case "+":
                fluent.push();
                return;
            case "pop":
            case "-":
                fluent.pop();
                return;
        }

        if (tryPosition(fluent, markup)) {
            return;
        }

        if (tryHyperlink(fluent, markup)) {
            return;
        }

        if (tryColors(fluent, markup)) {
            return;
        }
    }

    private String applySubstitutions(String markup, Object... args) {
        if (markup.contains("$")) {
            StringBuilder sb = new StringBuilder();
            applyPattern(sb::append, varPattern, "$", markup, vn -> applyVar(sb, vn, args));
            return sb.toString();
        }
        return markup;
    }

    protected void applyVar(Appendable appendable, String varName, Object... args) {
        try {
            if (varName.startsWith("/")) {
                appendable.append("/").append(getVar(varName.substring(1), args));
            } else {
                appendable.append(getVar(varName, args));
            }
        } catch (IOException e) {
            // Ignore
        }
    }

    protected String getVar(String varName, Object... args) {
        try {
            int num = Integer.parseInt(varName) - 1;
            if (num >= args.length) {
                throw new IndexOutOfBoundsException(
                        "Variable substitution index out of bounds: "
                                + num
                                + " is larger than the number of provided arguments: "
                                + args.length);
            }
            if (num < 0) {
                throw new IndexOutOfBoundsException(
                        "Variable substitution index must be 1 or higher");
            }
            return String.valueOf(args[num]);
        } catch (NumberFormatException e) {
            // Ignore
        }
        return "";
    }

    private boolean tryStyles(NegatableCommands fluentNeg, String markup) {
        switch (markup.toLowerCase()) {
            case "bold":
            case "b":
                fluentNeg.bold();
                return true;
            case "faint":
                fluentNeg.faint();
                return true;
            case "italic":
            case "i":
                fluentNeg.italic();
                return true;
            case "underline":
            case "ul":
                fluentNeg.underline();
                return true;
            case "blink":
                fluentNeg.blink();
                return true;
            case "inv":
            case "inverse":
                fluentNeg.inverse();
                return true;
            case "hidden":
                fluentNeg.hidden();
                return true;
            case "strikethrough":
            case "strike":
                fluentNeg.strikethrough();
                return true;
        }
        return false;
    }

    private boolean tryPosition(Fluent fluent, String markup) {
        switch (markup.toLowerCase()) {
            case "mark":
            case "@":
                fluent.mark();
                return true;
            case "jump":
            case "^":
                fluent.jump();
                return true;
            case "up":
                fluent.up();
                return true;
            case "dn":
                fluent.down();
                return true;
            case "bw":
                fluent.backward();
                return true;
            case "fw":
                fluent.forward();
                return true;
        }
        if (markup.startsWith("up:")) {
            try {
                int n = parsePos(markup.substring(3), 999);
                fluent.up(n);
                return true;
            } catch (NumberFormatException e) {
                // Invalid position format, ignore
            }
        } else if (markup.startsWith("dn:")) {
            try {
                int n = parsePos(markup.substring(3), 999);
                fluent.down(n);
                return true;
            } catch (NumberFormatException e) {
                // Invalid position format, ignore
            }
        } else if (markup.startsWith("fw:")) {
            try {
                int n = parsePos(markup.substring(3), 999);
                fluent.forward(n);
                return true;
            } catch (NumberFormatException e) {
                // Invalid position format, ignore
            }
        } else if (markup.startsWith("bw:")) {
            try {
                int n = parsePos(markup.substring(3), 999);
                fluent.backward(n);
                return true;
            } catch (NumberFormatException e) {
                // Invalid position format, ignore
            }
        } else if (markup.startsWith("~")) {
            try {
                String[] parts = markup.substring(1).split(",");
                if (parts.length == 2) {
                    int x = parsePos(parts[0], 999); // TODO get real numbers
                    int y = parsePos(parts[1], 999);
                    fluent.at(x, y);
                    return true;
                } else if (parts.length == 1) {
                    int x = parsePos(parts[0], 999); // TODO get real numbers
                    fluent.col(x);
                    return true;
                }
            } catch (NumberFormatException e) {
                // Invalid position format, ignore
            }
        }
        return false;
    }

    private int parsePos(String pos, int max) throws NumberFormatException {
        if (pos.equals("max")) {
            return max;
        }
        return Integer.parseInt(pos.trim());
    }

    private boolean tryHyperlink(Fluent fluent, String markup) {
        if (markup.equals("/")) {
            fluent.lru();
            return true;
        } else if (markup.startsWith("http://") || markup.startsWith("https://")) {
            Hyperlink link = Hyperlink.of(markup);
            fluent.url(link.url, link.id);
            return true;
        }
        return false;
    }

    private boolean tryColors(Fluent fluent, String markup) {
        if (markup.startsWith("bg:")) {
            return tryColorsFgBg(fluent.bg(), markup.substring(3));
        } else {
            return tryColorsFgBg(fluent, markup);
        }
    }

    private boolean tryColorsFgBg(ColorCommands fluentColors, String markup) {
        // Try #RRGGBB format first
        Color color = tryRgbColor(markup);
        if (color == null) {
            // Then try color names, like "red", "blue", etc
            color = tryColorByName(markup);
        }
        if (color != null) {
            fluentColors.color(color);
            return true;
        }
        return false;
    }

    private Color tryRgbColor(String markup) {
        if (markup.length() != 7 || !markup.startsWith("#")) {
            return null; // Invalid RGB color format
        }
        try {
            int r = Integer.parseInt(markup.substring(1, 3), 16);
            int g = Integer.parseInt(markup.substring(3, 5), 16);
            int b = Integer.parseInt(markup.substring(5, 7), 16);
            return Color.rgb(r, g, b);
        } catch (Exception e) {
            // Invalid RGB color format, ignore
            return null;
        }
    }

    private Color tryColorByName(String markup) {
        if (colors == null) {
            Color.BasicColor[] cs = Color.BasicColor.normalColors;
            colors = new HashMap<>();
            for (Color.BasicColor c : cs) {
                colors.put(c.name().toLowerCase(), c);
            }
        }

        String lmarkup = markup.toLowerCase();
        if (lmarkup.startsWith("bright")) {
            String baseColor = lmarkup.substring(6);
            Color.BasicColor c = colors.get(baseColor.toLowerCase());
            if (c != null) {
                return c.bright();
            }
        } else if (lmarkup.startsWith("dark")) {
            String baseColor = lmarkup.substring(4);
            Color.BasicColor c = colors.get(baseColor.toLowerCase());
            if (c != null) {
                return c.dark();
            }
        }
        return colors.get(lmarkup);
    }

    protected void applyPattern(
            Consumer<String> appendable,
            Pattern pattern,
            String token,
            String textWithMarkup,
            Consumer<String> handler) {
        int lastIndex = 0;
        Matcher matcher = pattern.matcher(textWithMarkup);
        while (matcher.find()) {
            // Append text before the markup
            if (matcher.start() > lastIndex) {
                String txt = textWithMarkup.substring(lastIndex, matcher.start());
                txt = txt.replace(token + token, token);
                appendable.accept(txt);
            }
            // Handle the markup content
            String markupContent = matcher.group(1);
            handler.accept(markupContent);
            lastIndex = matcher.end();
        }
        // Append any remaining text after the last markup
        if (lastIndex < textWithMarkup.length()) {
            String txt = textWithMarkup.substring(lastIndex);
            txt = txt.replace(token + token, token);
            appendable.accept(txt);
        }
    }
}
