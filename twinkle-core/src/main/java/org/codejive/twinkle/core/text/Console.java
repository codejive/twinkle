package org.codejive.twinkle.core.text;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.widget.Panel;

public final class Console {
    private Console() {}

    // ---- Fluent builder (Span/Line -> Panel -> ANSI) ----

    /** Entry point for building rich console output using a style stack. */
    public static Rich text() {
        return new Rich();
    }

    public static final class Rich {
        private final SpanLinesBuilder out = new SpanLinesBuilder();
        private final Deque<Style> stack = new ArrayDeque<>();
        private static final String NO_LINK = "\0";
        private final Deque<String> linkStack = new ArrayDeque<>();
        private String currentLink;
        private boolean scopeOpen;

        private Rich() {
            stack.push(Style.UNSTYLED);
            currentLink = null;
        }

        // ---- text & emoji ----

        public Rich text(String s) {
            out.append(stack.peek(), currentLink, Objects.toString(s, ""));
            return this;
        }

        /** 
         * Adds a space to the output.
         * @return The current Rich instance.
         */
        public Rich space() {
            out.append(stack.peek(), currentLink, " ");
            return this;
        }

        public Rich sp() { return space(); }

        public Rich nl() {
            out.nl();
            return this;
        }

        public Rich emoji(String name) {
            String repl = Emoji.emoji(name == null ? null : name.toLowerCase(Locale.ROOT));
            out.append(stack.peek(), currentLink, repl != null ? repl : ":" + name + ":");
            return this;
        }

        // ---- style scopes ----

        /**
         * Begins a style scope explicitly (usually you don't need this; calling a style method like
         * {@link #bold()} will auto-begin one).
         */
        public Rich begin() {
            beginScopeIfNeeded(true);
            return this;
        }

        public Rich bold()      { beginScopeIfNeeded(false); return setTop(stack.peek().bold()); }
        public Rich italic()    { beginScopeIfNeeded(false); return setTop(stack.peek().italic()); }
        public Rich underline() { beginScopeIfNeeded(false); return setTop(stack.peek().underlined()); }
        public Rich dim()       { beginScopeIfNeeded(false); return setTop(stack.peek().faint()); }
        public Rich reverse()   { beginScopeIfNeeded(false); return setTop(stack.peek().inverse()); }

        public Rich black()   { beginScopeIfNeeded(false); return setTop(stack.peek().fgColor(Color.BasicColor.BLACK)); }
        public Rich red()     { beginScopeIfNeeded(false); return setTop(stack.peek().fgColor(Color.BasicColor.RED)); }
        public Rich green()   { beginScopeIfNeeded(false); return setTop(stack.peek().fgColor(Color.BasicColor.GREEN)); }
        public Rich yellow()  { beginScopeIfNeeded(false); return setTop(stack.peek().fgColor(Color.BasicColor.YELLOW)); }
        public Rich blue()    { beginScopeIfNeeded(false); return setTop(stack.peek().fgColor(Color.BasicColor.BLUE)); }
        public Rich magenta() { beginScopeIfNeeded(false); return setTop(stack.peek().fgColor(Color.BasicColor.MAGENTA)); }
        public Rich cyan()    { beginScopeIfNeeded(false); return setTop(stack.peek().fgColor(Color.BasicColor.CYAN)); }
        public Rich white()   { beginScopeIfNeeded(false); return setTop(stack.peek().fgColor(Color.BasicColor.WHITE)); }

        public Rich fg(int rgb) {
            beginScopeIfNeeded(false);
            return setTop(stack.peek().fgColor(Color.rgb((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF)));
        }
        public Rich bg(int rgb) {
            beginScopeIfNeeded(false);
            return setTop(stack.peek().bgColor(Color.rgb((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF)));
        }

        // ---- scoped helpers (avoid explicit .end()) ----

        public Rich bold(Consumer<Rich> body)      { return scoped(r -> { r.bold(); body.accept(r); }); }
        public Rich italic(Consumer<Rich> body)    { return scoped(r -> { r.italic(); body.accept(r); }); }
        public Rich underline(Consumer<Rich> body) { return scoped(r -> { r.underline(); body.accept(r); }); }
        public Rich dim(Consumer<Rich> body)       { return scoped(r -> { r.dim(); body.accept(r); }); }
        public Rich reverse(Consumer<Rich> body)   { return scoped(r -> { r.reverse(); body.accept(r); }); }

        public Rich black(Consumer<Rich> body)   { return scoped(r -> { r.black(); body.accept(r); }); }
        public Rich red(Consumer<Rich> body)     { return scoped(r -> { r.red(); body.accept(r); }); }
        public Rich green(Consumer<Rich> body)   { return scoped(r -> { r.green(); body.accept(r); }); }
        public Rich yellow(Consumer<Rich> body)  { return scoped(r -> { r.yellow(); body.accept(r); }); }
        public Rich blue(Consumer<Rich> body)    { return scoped(r -> { r.blue(); body.accept(r); }); }
        public Rich magenta(Consumer<Rich> body) { return scoped(r -> { r.magenta(); body.accept(r); }); }
        public Rich cyan(Consumer<Rich> body)    { return scoped(r -> { r.cyan(); body.accept(r); }); }
        public Rich white(Consumer<Rich> body)   { return scoped(r -> { r.white(); body.accept(r); }); }

        public Rich fg(int rgb, Consumer<Rich> body) { return scoped(r -> { r.fg(rgb); body.accept(r); }); }
        public Rich bg(int rgb, Consumer<Rich> body) { return scoped(r -> { r.bg(rgb); body.accept(r); }); }

        // ---- ultra-compact scoped helpers (no lambdas) ----

        public Rich bold(String s)      { return bold(r -> r.text(s)); }
        public Rich italic(String s)    { return italic(r -> r.text(s)); }
        public Rich underline(String s) { return underline(r -> r.text(s)); }
        public Rich dim(String s)       { return dim(r -> r.text(s)); }
        public Rich reverse(String s)   { return reverse(r -> r.text(s)); }

        public Rich black(String s)   { return black(r -> r.text(s)); }
        public Rich red(String s)     { return red(r -> r.text(s)); }
        public Rich green(String s)   { return green(r -> r.text(s)); }
        public Rich yellow(String s)  { return yellow(r -> r.text(s)); }
        public Rich blue(String s)    { return blue(r -> r.text(s)); }
        public Rich magenta(String s) { return magenta(r -> r.text(s)); }
        public Rich cyan(String s)    { return cyan(r -> r.text(s)); }
        public Rich white(String s)   { return white(r -> r.text(s)); }

        public Rich fg(int rgb, String s) { return fg(rgb, r -> r.text(s)); }
        public Rich bg(int rgb, String s) { return bg(rgb, r -> r.text(s)); }

        public Rich end() {
            if (stack.size() > 1) stack.pop();
            scopeOpen = false;
            return this;
        }

        // ---- links ----

        public Rich link(String url, Consumer<Rich> body) {
            Objects.requireNonNull(url, "url");
            Objects.requireNonNull(body, "body");
            linkStack.push(currentLink == null ? NO_LINK : currentLink);
            currentLink = url;
            try {
                body.accept(this);
            } finally {
                String prev = linkStack.pop();
                currentLink = NO_LINK.equals(prev) ? null : prev;
            }
            return this;
        }

        public Rich link(URI uri, Consumer<Rich> body) {
            return link(Objects.requireNonNull(uri, "uri").toString(), body);
        }

        /**
         * Appends a fragment written in the bracket/emoji markup syntax understood by
         * {@link Console#render(String)}.
         *
         * <p>Note: the fragment is rendered starting from the current fluent style. After it's
         * appended, the current fluent builder style is restored (so you can keep chaining).
         */
        public Rich markup(String markup) {
            appendMarkup(out, Objects.toString(markup, ""), stack.peek());
            return this;
        }

        // ---- internals ----

        private void beginScopeIfNeeded(boolean force) {
            if (!force && scopeOpen) return;
            stack.push(stack.peek());
            scopeOpen = true;
        }

        private Rich setTop(Style s) {
            // Replace the top of the stack with the new style
            stack.pop();
            stack.push(s);
            return this;
        }

        private Rich scoped(Consumer<Rich> body) {
            Objects.requireNonNull(body, "body");
            boolean prevScopeOpen = scopeOpen;
            // Always create a nested scope for the lambda.
            stack.push(stack.peek());
            scopeOpen = true;
            try {
                body.accept(this);
            } finally {
                if (stack.size() > 1) stack.pop();
                scopeOpen = prevScopeOpen;
            }
            return this;
        }

        public Panel build() {
            return out.toPanel();
        }

        public String toAnsiString() {
            return out.toAnsiString();
        }

        public void println() {
            System.out.println(toAnsiString());
        }
    }


    // ---- Style token dictionaries
    private static final Map<String, Color.BasicColor> BASIC_FG;

    static {
        Map<String, Color.BasicColor> tmp = new HashMap<>();
        tmp.put("black", Color.BasicColor.BLACK);
        tmp.put("red", Color.BasicColor.RED);
        tmp.put("green", Color.BasicColor.GREEN);
        tmp.put("yellow", Color.BasicColor.YELLOW);
        tmp.put("blue", Color.BasicColor.BLUE);
        tmp.put("magenta", Color.BasicColor.MAGENTA);
        tmp.put("cyan", Color.BasicColor.CYAN);
        tmp.put("white", Color.BasicColor.WHITE);
        BASIC_FG = java.util.Collections.unmodifiableMap(tmp);
    }

    public static void println(String input) {
        System.out.println(toAnsiString(input));
    }

    public static Panel render(String input) {
        return render(Objects.toString(input, ""), Style.UNSTYLED);
    }

    /**
     * Renders markup into a compact ANSI string (no fixed-width padding).
     *
     * <p>Unlike {@link #render(String)}, this is optimized for streaming to a terminal: each line is
     * rendered at its natural width and concatenated with newlines.
     */
    public static String toAnsiString(String input) {
        SpanLinesBuilder out = new SpanLinesBuilder();
        appendMarkup(out, Objects.toString(input, ""), Style.UNSTYLED);
        return out.toAnsiString();
    }

    /**
     * Renders bracket/emoji markup starting from a given base style.
     *
     * <p>This is useful when you want to splice markup fragments into an existing styled builder
     * and have {@code [/]} close back to the current style (instead of default).
     */
    public static Panel render(String input, Style baseStyle) {
        input = Objects.toString(input, "");
        SpanLinesBuilder out = new SpanLinesBuilder();
        appendMarkup(out, input, Objects.requireNonNull(baseStyle, "baseStyle"));
        return out.toPanel();
    }

    // ---- internals ----

    private static final class State {
        final Style style;
        final String link; // nullable

        State(Style style, String link) {
            this.style = Objects.requireNonNull(style, "style");
            this.link = link;
        }
    }

    private static void appendMarkup(SpanLinesBuilder out, String input, Style baseStyle) {
        Deque<State> stack = new ArrayDeque<>();
        State cur = new State(baseStyle, null);

        int i = 0;
        while (i < input.length()) {
            char ch = input.charAt(i);

            // Escape: \X => literal X
            if (ch == '\\') {
                if (i + 1 < input.length()) {
                    out.append(cur.style, cur.link, String.valueOf(input.charAt(i + 1)));
                    i += 2;
                } else {
                    out.append(cur.style, cur.link, "\\");
                    i++;
                }
                continue;
            }

            // Tag: [ ... ]
            if (ch == '[') {
                int end = findUnescaped(input, ']', i + 1);
                if (end == -1) { out.append(cur.style, cur.link, "["); i++; continue; }

                String raw = input.substring(i + 1, end).trim();

                // Close tag: [/] (Rich-ish) or anything starting with /
                if (raw.equals("/") || raw.startsWith("/")) {
                    if (!stack.isEmpty()) {
                        State prev = stack.pop();
                        cur = prev;
                    }
                    i = end + 1;
                    continue;
                }

                // Opening tag: push current, compute next
                stack.push(cur);
                State next = applyTag(cur, raw);
                cur = next;

                i = end + 1;
                continue;
            }

            // Emoji shortcode: :alias:
            if (ch == ':') {
                int end = input.indexOf(':', i + 1);
                if (end != -1) {
                    String name = input.substring(i + 1, end);
                    if (isEmojiName(name)) {
                        String repl = Emoji.emoji(name.toLowerCase(Locale.ROOT));
                        if (repl != null) {
                            out.append(cur.style, cur.link, repl);
                            i = end + 1;
                            continue;
                        }
                    }
                }
                out.append(cur.style, cur.link, ":");
                i++;
                continue;
            }

            if (ch == '\n') {
                out.nl();
                i++;
                continue;
            }

            // Normal char
            out.append(cur.style, cur.link, String.valueOf(ch));
            i++;
        }

        // Link open/close is handled during ANSI emission.
    }

    private static State applyTag(State base, String raw) {
        Style style = base.style;
        String link = base.link;

        List<String> parts = splitTokens(raw);

        for (int idx = 0; idx < parts.size(); idx++) {
            String tok = parts.get(idx);

            // [#RRGGBB] => foreground truecolor
            if (isHexColor(tok)) {
                style = style.fgColor(parseHexColor(tok));
                continue;
            }

            // on #RRGGBB  => background truecolor
            if (tok.equals("on") && idx + 1 < parts.size() && isHexColor(parts.get(idx + 1))) {
                style = style.bgColor(parseHexColor(parts.get(idx + 1)));
                idx++;
                continue;
            }

            // fg=#RRGGBB / bg=#RRGGBB
            if (tok.startsWith("fg=") && isHexColor(tok.substring(3))) {
                style = style.fgColor(parseHexColor(tok.substring(3)));
                continue;
            }
            if (tok.startsWith("bg=") && isHexColor(tok.substring(3))) {
                style = style.bgColor(parseHexColor(tok.substring(3)));
                continue;
            }

            // link=https://... (OSC-8 hyperlink)
            if (tok.startsWith("link=")) {
                String url = tok.substring("link=".length());
                if (!url.trim().isEmpty()) link = url;
                continue;
            }

            // basic named colors (fg)
            Color.BasicColor fg = BASIC_FG.get(tok);
            if (fg != null) { style = style.fgColor(fg); continue; }

            // background named colors: on:red or bg:red
            if (tok.startsWith("bg:")) {
                String name = tok.substring(3);
                Color.BasicColor bg = BASIC_FG.get(name);
                if (bg != null) style = style.bgColor(bg);
                continue;
            }
            if (tok.startsWith("on:")) {
                String name = tok.substring(3);
                Color.BasicColor bg = BASIC_FG.get(name);
                if (bg != null) style = style.bgColor(bg);
                continue;
            }

            // styles like bold/u/i/dim/reverse/blink
            if (tok.equals("bold") || tok.equals("b")) style = style.bold();
            else if (tok.equals("u") || tok.equals("underline")) style = style.underlined();
            else if (tok.equals("i") || tok.equals("italic")) style = style.italic();
            else if (tok.equals("dim")) style = style.faint();
            else if (tok.equals("reverse") || tok.equals("inverse")) style = style.inverse();
            else if (tok.equals("blink")) style = style.blink();
        }

        return new State(style, link);
    }

    private static int findUnescaped(String s, char target, int from) {
        for (int i = from; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') { i++; continue; }
            if (c == target) return i;
        }
        return -1;
    }

    private static boolean isEmojiName(String name) {
        for (int j = 0; j < name.length(); j++) {
            char c = name.charAt(j);
            boolean ok = (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == '_' || c == '+' || c == '-';
            if (!ok) return false;
        }
        return true;
    }

    private static List<String> splitTokens(String raw) {
        String[] parts = raw.toLowerCase(Locale.ROOT).trim().split("\\s+");
        List<String> out = new ArrayList<>(parts.length);
        for (String p : parts) if (!p.trim().isEmpty()) out.add(p);
        return out;
    }

    private static boolean isHexColor(String s) {
        if (s == null) return false;
        String t = s.startsWith("#") ? s.substring(1) : s;
        if (t.length() != 6) return false;
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            boolean hex = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
            if (!hex) return false;
        }
        return true;
    }

    private static Color parseHexColor(String s) {
        String t = s.startsWith("#") ? s.substring(1) : s;
        int rgb = Integer.parseInt(t, 16) & 0xFFFFFF;
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return Color.rgb(r, g, b);
    }

    /**
     * Internal helper for building a multi-line {@link Panel} from styled text spans.
     */
    private static final class SpanLinesBuilder {
        private static final class Segment {
            final String text;
            final long styleState;
            final String link; // nullable

            Segment(String text, long styleState, String link) {
                this.text = text;
                this.styleState = styleState;
                this.link = link;
            }
        }

        private final List<List<Segment>> lines = new ArrayList<>();
        private List<Segment> currentLine = new ArrayList<>();
        private long pendingStyle = Style.F_UNSTYLED;
        private String pendingLink;
        private final StringBuilder pendingText = new StringBuilder();

        void append(Style style, String link, String text) {
            String s = Objects.toString(text, "");
            if (s.isEmpty()) return;
            long st = Objects.requireNonNull(style, "style").state();
            if (pendingText.length() > 0 && (pendingStyle != st || !Objects.equals(pendingLink, link))) {
                flushPending();
            }
            pendingStyle = st;
            pendingLink = link;
            pendingText.append(s);
        }

        void nl() {
            flushPending();
            lines.add(currentLine);
            currentLine = new ArrayList<>();
        }

        private void flushPending() {
            if (pendingText.length() == 0) return;
            currentLine.add(new Segment(pendingText.toString(), pendingStyle, pendingLink));
            pendingText.setLength(0);
        }

        String toAnsiString() {
            flushPending();
            if (lines.isEmpty()) {
                lines.add(currentLine);
            } else if (currentLine != null && !currentLine.isEmpty()) {
                lines.add(currentLine);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(Ansi.STYLE_RESET);

            long currentStyleState = Style.F_UNSTYLED;
            String currentLink = null;
            for (int y = 0; y < lines.size(); y++) {
                List<Segment> segs = lines.get(y);

                for (Segment seg : segs) {
                    // hyperlink transition
                    if (!Objects.equals(currentLink, seg.link)) {
                        if (currentLink != null) sb.append(Ansi.osc8Close());
                        if (seg.link != null) sb.append(Ansi.osc8Open(seg.link));
                        currentLink = seg.link;
                    }

                    long next = seg.styleState;
                    if (next != currentStyleState) {
                        Style.of(next).toAnsiString(sb, currentStyleState);
                        currentStyleState = next;
                    }
                    sb.append(seg.text);
                }

                if (y < lines.size() - 1) sb.append('\n');
            }

            if (currentLink != null) sb.append(Ansi.osc8Close());
            return sb.toString();
        }

        Panel toPanel() {
            flushPending();
            if (lines.isEmpty()) {
                lines.add(currentLine);
            } else if (currentLine != null && !currentLine.isEmpty()) {
                lines.add(currentLine);
            }

            int height = lines.size();
            int width = 0;
            for (List<Segment> line : lines) {
                int w = 0;
                for (Segment seg : line) {
                    w += seg.text.codePointCount(0, seg.text.length());
                }
                width = Math.max(width, w);
            }

            Panel panel = Panel.of(width, height);
            for (int y = 0; y < height; y++) {
                List<Segment> segs = lines.get(y);
                List<Span> spans = new ArrayList<>();
                for (Segment seg : segs) spans.add(Span.of(seg.text, seg.styleState));
                Line ln = Line.of(spans.toArray(new Span[0]));
                ln.render(panel.view(0, y, width, 1));
            }
            return panel;
        }
    }


   public static void main(String[] args) {
   
    
    Console.println(
        "[bold underline #ffcc00]Twinkle Twinkle[/]\n" +
        "[italic]A dazzling demo of JBang Twinkle by Tako Quintesse![/]\n" +
        ":star2: [b magenta]Run Java scripts with style[/] :sparkles:\n" +
        ":rocket: [u link=https://jbang.dev]Explore more on jbang.dev[/u] :rocket:\n" +
        "Features:\n" +
        "- [green bold]Rich text[/], [blue]colors[/], [underline]hyperlinks[/], and [magenta]emojis :zap::smile:[/]\n" +
        "- [reverse]Console output that shines![/reverse]\n" +
        "- [#00bcd4]Escape tags? Like \\[not a tag\\] or \\:noemoji\\:[/]\n" +
        "[dim italic]Let your scripts shine with [/][ #0dff00ff]J[/][ #ff7f00]B[/][ #ffff00]a[/][ #00ff00]n[/][#0000ff]g[/] [#ffcc00 bold blink]Twinkle[/] :sparkles:[/]"
    );

    URI home = URI.create("https://jbang.dev");
    String codename = "Twinkle";

    Console.text()
            .bold().underline().fg(0xFFCC00).text(codename).sp().text(codename).end()
            .nl().nl()
            .italic().text("A dazzling demo of JBang ").text(codename).text(" by Tako Quintesse!").end()
            .nl().nl()
            .emoji("star2").space()
            .bold(r -> r.magenta(rr -> rr.text("Run Java scripts with style")))
            .space().emoji("sparkles")
            .nl()
            .emoji("rocket").space()
            .underline().link(home, r -> r.text("Explore more on jbang.dev")).end()
            .space().emoji("rocket")
            .nl().nl()
            .text("Features:")
            .nl()
            // mixed styles: left shows explicit .end(), right uses scoped helpers
            .text("- ").green().bold().text("Rich text").end()
            .text(", ").blue(r -> r.text("colors"))
            .text(", ").underline(r -> r.text("hyperlinks"))
            .text(", and ").magenta(r -> r.text("emojis "))
            .emoji("zap").emoji("smile")
            .nl()
            .text("- ").reverse().text("Console output that shines!").end()
            .nl()
            .text("- ").fg(0x00BCD4, r -> r.markup("Escape tags? Like [not a tag] or :noemoji:"))
            .nl().nl()
            .dim().italic().text("Let your scripts shine with ").end()
            .fg(0x0DFF00, "J")
            .fg(0xFF7F00, "B")
            .fg(0xFFFF00, "a")
            .fg(0x00FF00, "n")
            .fg(0x0000FF, "g")
            .space()
            .markup("[bold #ffcc00]" + codename + "[/]")
            .sp().emoji("sparkles")
            .println();
            
}

    
}