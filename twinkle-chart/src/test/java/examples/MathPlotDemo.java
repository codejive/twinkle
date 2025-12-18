package examples;

import org.codejive.twinkle.core.text.Line;
import org.codejive.twinkle.core.widget.Panel;
import org.codejive.twinkle.core.widget.Size;
import org.codejive.twinkle.widgets.Framed;
import org.codejive.twinkle.widgets.graphs.plot.MathPlot;

public class MathPlotDemo {
    public static void main(String[] args) {
        MathPlot p = MathPlot.of(Size.of(40, 20)).ranges(-2 * Math.PI, 2 * Math.PI, -1.0, 1.0);
        // plot a sine wave
        p.plot(Math::sin);
        Framed f = Framed.of(p).title(Line.of(" Sine Wave "));
        Panel pnl = Panel.of(42, 22);
        f.render(pnl);
        System.out.println(pnl);
    }
}
