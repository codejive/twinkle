package examples;

import static examples.Util.pos;
import static examples.Util.size;

import java.io.IOException;
import java.util.List;
import org.codejive.twinkle.tui.ciml.dom.ContextDocument;
import org.codejive.twinkle.tui.ciml.dom.PanelElement;
import org.codejive.twinkle.tui.ciml.dom.ScreenElement;
import org.codejive.twinkle.tui.ciml.layout.DomLayouter;
import org.codejive.twinkle.tui.render.Box;
import org.codejive.twinkle.tui.render.BoxRenderer;
import org.codejive.twinkle.tui.terminal.Screen;
import org.codejive.twinkle.tui.terminal.Term;

public class SimpleDom {
    private final Term term;

    public SimpleDom(Term term) {
        this.term = term;
    }

    public static void main(String... args) throws IOException {
        try (Term terminal = Term.create()) {
            new SimpleDom(terminal).run();
        }
    }

    public int run() throws IOException {
        Screen screen = term.fullScreen();
        int displayWidth = screen.rect().width();
        int displayHeight = screen.rect().height();

        ContextDocument doc = new ContextDocument();
        ScreenElement scr =
                doc.append(doc.createScreenElement(), size(displayWidth, displayHeight));
        PanelElement pan = scr.append(doc.createPanelElement(), pos(5, 5).and(size(20, 10)));

        List<Box> boxes = new DomLayouter().layout(doc);
        BoxRenderer boxr = new BoxRenderer(screen);
        for (Box b : boxes) {
            boxr.render(b);
        }
        screen.update();
        int c = term.input().readChar();

        return 0;
    }
}
