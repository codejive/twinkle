// java
package examples;

import java.util.Random;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.text.Buffer;
import org.codejive.twinkle.core.text.Line;
import org.codejive.twinkle.core.util.Size;
import org.codejive.twinkle.widgets.Framed;
import org.codejive.twinkle.widgets.graphs.plot.MathPlot;

public class MathPlotColorDemo {
    public static void main(String[] args) throws InterruptedException {
        MathPlot p = MathPlot.of(Size.of(40, 20)).ranges(-2 * Math.PI, 2 * Math.PI, -2.0, 2.0);
        Framed f = Framed.of(p).title(Line.of(" Interfering Waves "));
        Buffer buf = Buffer.of(42, 22);

        System.out.print(Ansi.hideCursor());
        try {
            // random generator and mutable parameter bases/variations
            Random rng = new Random();
            double a1Base, a2Base, a1Var, a2Var;
            double k1Base, k2Base, k1Var, k2Var;
            double speed1, speed2;

            a1Base = 0.5 + rng.nextDouble() * 0.7; // 0.5 .. 1.2
            a1Var = 0.2 + rng.nextDouble() * 0.6; // 0.2 .. 0.8
            a2Base = 0.3 + rng.nextDouble() * 0.6; // 0.3 .. 0.9
            a2Var = 0.1 + rng.nextDouble() * 0.5; // 0.1 .. 0.6

            k1Base = 0.8 + rng.nextDouble() * 1.2; // 0.8 .. 2.0
            k1Var = 0.2 + rng.nextDouble() * 1.05; // 0.2 .. 1.2
            k2Base = 1.5 + rng.nextDouble() * 1.5; // 1.5 .. 3.0
            k2Var = 0.3 + rng.nextDouble() * 1.2; // 0.3 .. 1.5

            speed1 = 0.6 + rng.nextDouble() * 1.6; // 0.6 .. 2.2
            speed2 = 0.3 + rng.nextDouble() * 1.1; // 0.3 .. 1.4
            if (Math.abs(speed1 - speed2) < 0.3) {
                speed2 += 0.4; // ensure noticeable difference
            }

            for (int i = 0; i < 400; i++) {
                if (i > 0) {
                    System.out.print(Ansi.cursorMove(Ansi.CURSOR_PREV_LINE, 22));
                    p.clear();
                }

                // time parameter
                double t = i * 0.08;

                // amplitudes vary slowly around randomized bases
                double a1 = a1Base + a1Var * Math.sin(t * 0.7);
                double a2 = a2Base + a2Var * Math.cos(t * 0.5);

                // spatial frequencies (strides) vary with time around randomized bases
                double k1 = k1Base + k1Var * Math.sin(t * 0.4);
                double k2 = k2Base + k2Var * Math.cos(t * 0.3);

                // phases to make waves drift from right to left at differing speeds (positive)
                double phase1 = t * speed1;
                double phase2 = t * speed2;

                // plot combined wave
                p.plot(
                        x -> a1 * Math.sin(k1 * x + phase1),
                        Style.ofFgColor(Color.BasicColor.GREEN));
                p.plot(x -> a2 * Math.sin(k2 * x + phase2), Style.ofFgColor(Color.BasicColor.RED));

                f.render(buf);
                System.out.println(buf.toAnsiString());

                Thread.sleep(20);
            }
        } finally {
            System.out.print(Ansi.showCursor());
        }
    }
}
