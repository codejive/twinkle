package examples;

import org.codejive.twinkle.components.graphs.plot.MathPlot;
import org.codejive.twinkle.core.component.Panel;

public class MathPlotDemo {
    public static void main(String[] args) {
        Panel pnl = Panel.of(40, 20);
        MathPlot p = MathPlot.of(pnl).ranges(-2 * Math.PI, 2 * Math.PI, -1.0, 1.0);
        // plot a sine wave
        p.plot(Math::sin);
        System.out.println(pnl.toString());
    }
}
