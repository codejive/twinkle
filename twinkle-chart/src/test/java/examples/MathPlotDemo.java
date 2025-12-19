// java
package examples;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.core.text.Line;
import org.codejive.twinkle.core.widget.Panel;
import org.codejive.twinkle.core.widget.Size;
import org.codejive.twinkle.widgets.Framed;
import org.codejive.twinkle.widgets.graphs.plot.MathPlot;

public class MathPlotDemo {
    public static void main(String[] args) throws InterruptedException {
        MathPlot p = MathPlot.of(Size.of(40, 20)).ranges(-2 * Math.PI, 2 * Math.PI, -2.0, 2.0);
        Framed f = Framed.of(p).title(Line.of(" Interfering Waves "));
        Panel pnl = Panel.of(42, 22);

        System.out.print(Ansi.hideCursor());
        try {
            for (int i = 0; i < 400; i++) {
                if (i > 0) {
                    System.out.print(Ansi.cursorMove(Ansi.CURSOR_PREV_LINE, 22));
                    p.clear();
                }

                // time parameter
                double t = i * 0.08;

                // amplitudes vary slowly
                double a1 = 0.8 + 0.4 * Math.sin(t * 0.7);
                double a2 = 0.6 + 0.3 * Math.cos(t * 0.5);

                // spatial frequencies (strides) vary with time
                double k1 = 1.0 + 0.6 * Math.sin(t * 0.4);
                double k2 = 2.0 + 0.9 * Math.cos(t * 0.3);

                // phases to make waves drift from right to left at differing speeds
                double speed1 = 1.2; // faster wave
                double speed2 = 0.6; // slower wave
                double phase1 = t * speed1;
                double phase2 = t * speed2;

                // plot combined wave
                p.plot(x -> a1 * Math.sin(k1 * x + phase1) + a2 * Math.sin(k2 * x + phase2));

                f.render(pnl);
                System.out.println(pnl);

                Thread.sleep(40);
            }
        } finally {
            System.out.print(Ansi.showCursor());
        }
    }
}
