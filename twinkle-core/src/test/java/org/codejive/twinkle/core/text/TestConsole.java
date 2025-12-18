package org.codejive.twinkle.core.text;

import java.net.URI;

import org.junit.jupiter.api.Test;

public class TestConsole {

    @Test
    void testRichString() {
        Console.println(
                "[bold underline #ffcc00]Twinkle Twinkle[/]\n"
                        + "[italic]A dazzling demo of JBang Twinkle by Tako Quintesse![/]\n"
                        + ":star2: [b magenta]Run Java scripts with style[/] :sparkles:\n"
                        + ":rocket: [u link=https://jbang.dev]Explore more on jbang.dev[/u] :rocket:\n"
                        + "Features:\n"
                        + "- [green bold]Rich text[/], [blue]colors[/], [underline]hyperlinks[/], and [magenta]emojis :zap::smile:[/]\n"
                        + "- [reverse]Console output that shines![/reverse]\n"
                        + "- [#00bcd4]Escape tags? Like \\[not a tag\\] or \\:noemoji\\:[/]\n"
                        + "[dim italic]Let your scripts shine with [/][ #0dff00ff]J[/][ #ff7f00]B[/][ #ffff00]a[/][ #00ff00]n[/][#0000ff]g[/] [#ffcc00 bold blink]Twinkle[/] :sparkles:[/]");
    }

    @Test
    void testFluentRich() {
        URI home = URI.create("https://jbang.dev");
        String codename = "Twinkle";

        Console.text()
                .bold()
                .underline()
                .fg(0xFFCC00)
                .text(codename)
                .sp()
                .text(codename)
                .end()
                .nl()
                .nl()
                .italic()
                .text("A dazzling demo of JBang ")
                .text(codename)
                .text(" by Tako Quintesse!")
                .end()
                .nl()
                .nl()
                .emoji("star2")
                .space()
                .bold(r -> r.magenta(rr -> rr.text("Run Java scripts with style")))
                .space()
                .emoji("sparkles")
                .nl()
                .emoji("rocket")
                .space()
                .underline()
                .link(home, r -> r.text("Explore more on jbang.dev"))
                .end()
                .space()
                .emoji("rocket")
                .nl()
                .nl()
                .text("Features:")
                .nl()
                // mixed styles: left shows explicit .end(), right uses scoped helpers
                .text("- ")
                .green()
                .bold()
                .text("Rich text")
                .end()
                .text(", ")
                .blue(r -> r.text("colors"))
                .text(", ")
                .underline(r -> r.text("hyperlinks"))
                .text(", and ")
                .magenta(r -> r.text("emojis "))
                .emoji("zap")
                .emoji("smile")
                .nl()
                .text("- ")
                .reverse()
                .text("Console output that shines!")
                .end()
                .nl()
                .text("- ")
                .fg(0x00BCD4, r -> r.markup("Escape tags? Like [not a tag] or :noemoji:"))
                .nl()
                .nl()
                .dim()
                .italic()
                .text("Let your scripts shine with ")
                .end()
                .fg(0x0DFF00, "J")
                .fg(0xFF7F00, "B")
                .fg(0xFFFF00, "a")
                .fg(0x00FF00, "n")
                .fg(0x0000FF, "g")
                .space()
                .markup("[bold #ffcc00]" + codename + "[/]")
                .sp()
                .emoji("sparkles")
                .println();
    }
}