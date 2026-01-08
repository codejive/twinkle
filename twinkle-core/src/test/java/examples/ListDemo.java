// java
package examples;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.core.text.Buffer;
import org.codejive.twinkle.core.text.Line;
import org.codejive.twinkle.widgets.Framed;
import org.codejive.twinkle.widgets.list.List;

public class ListDemo {
    public static void main(String[] args) throws InterruptedException {
        List l =
                List.ofStrings(
                        "First Item", "Second Item", "Third Item", "Fourth Item", "Fifth Item");
        Framed f = Framed.of(l).title(Line.of(" Simple List "));
        Buffer buf = Buffer.of(42, 22);

        System.out.print(Ansi.hideCursor());
        try {
            f.render(buf);
            System.out.println(buf.toAnsiString());
        } finally {
            System.out.print(Ansi.showCursor());
        }
    }
}
