package examples;

import static examples.Util.setBorderWidth;
import static examples.Util.setRandomPosSize;
import static examples.Util.setSize;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import org.codejive.twinkle.tui.render.BorderRenderer;
import org.codejive.twinkle.tui.render.Box;
import org.codejive.twinkle.tui.render.BoxRenderer;
import org.codejive.twinkle.tui.styles.Style;
import org.codejive.twinkle.tui.terminal.Screen;
import org.codejive.twinkle.tui.terminal.Term;
import org.codejive.twinkle.tui.util.ScrollBuffer;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

public class Boxes {
    private final Term term;

    public Boxes(Term term) {
        this.term = term;
    }

    public static void main(String... args) throws IOException {
        try (Term terminal = Term.create()) {
            new Boxes(terminal).run();
        }
    }

    public int run() throws IOException {
        Screen screen = term.fullScreen();
        int displayWidth = screen.rect().width();
        int displayHeight = screen.rect().height();

        Box b1 = null, b2 = null, b3 = null, b4 = null;
        ScrollBuffer sb4 = new ScrollBuffer(5, 30, true);
        boolean refresh = true;
        int cnt = 0;
        out:
        while (true) {
            if (refresh) {
                screen.clear();
                b1 = createColoredBox();
                setRandomPosSize(b1, displayWidth, displayHeight);
                b2 = createTimerBox(new Style());
                setRandomPosSize(b2, displayWidth, displayHeight);
                b3 = createColoredBox();
                setRandomPosSize(b3, displayWidth, displayHeight);
                b4 = createScrollBox(new Style(), sb4);
                setRandomPosSize(b4, displayWidth, displayHeight);
                refresh = false;
            }
            BorderRenderer br = new BorderRenderer(screen);
            BoxRenderer boxr = new BoxRenderer(screen);
            for (Box b : Arrays.asList(b1, b2, b3, b4)) {
                br.render(b);
                boxr.render(b);
            }
            screen.update();
            int c = term.input().readChar(100);
            if (c == 'q') {
                break out;
            } else if (c != -2) {
                refresh = true;
            } else {
                b2 = createTimerBox(b2.style());
                if (cnt % 5 == 0) {
                    sb4.append("Hello World! ");
                    b4 = createScrollBox(b4.style(), sb4);
                }
            }
            cnt++;
        }

        term.flush();
        return 0;
    }

    private Box createColoredBox() {
        AttributedStringBuilder cb = new AttributedStringBuilder();

        Integer cols = term.maxColors();
        if (cols != null) {
            int col = (int) (Math.random() * (cols + 1));
            cb.styled(cb.style().foreground(col), "012345678901234");
        } else {
            cb.append("012345678901234");
        }

        AttributedString c = cb.toAttributedString();
        Box b = new Box(Arrays.asList(c, c, c, c, c));
        setSize(b, 15, 5);
        setBorderWidth(b, 1);
        return b;
    }

    private Box createTimerBox(Style s) {
        AttributedStringBuilder cb = new AttributedStringBuilder();
        cb.append(Instant.now().toString());
        AttributedString c = cb.toAttributedString();
        Box b = new Box(Arrays.asList(c), s);
        setSize(b, 15, 1);
        setBorderWidth(b, 1);
        return b;
    }

    private Box createScrollBox(Style s, ScrollBuffer sb) {
        Box b = new Box(Arrays.asList(sb.getLines()), s);
        setSize(b, 30, 5);
        setBorderWidth(b, 1);
        return b;
    }
}
