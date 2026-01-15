package examples;

import org.codejive.twinkle.core.text.Line;
import org.codejive.twinkle.tui.application.App;
import org.codejive.twinkle.tui.widgets.Framed;
import org.codejive.twinkle.tui.widgets.list.List;

public class ListDemo {
    public static void main(String[] args) throws Exception {
        List l =
                List.ofStrings(
                        "First Item", "Second Item", "Third Item", "Fourth Item", "Fifth Item");
        Framed f = Framed.of(l).title(Line.of(" Simple List "));
        App.run(f);
    }
}
