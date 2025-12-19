// java
package examples;

import java.util.Random;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.text.Line;
import org.codejive.twinkle.core.widget.Canvas;
import org.codejive.twinkle.core.widget.Panel;
import org.codejive.twinkle.core.widget.Size;
import org.codejive.twinkle.core.widget.Widget;
import org.codejive.twinkle.widgets.Framed;
import org.codejive.twinkle.widgets.graphs.plot.MathPlot;
import org.jspecify.annotations.NonNull;

public class MathPlotFourDemo {
    public static void main(String[] args) throws InterruptedException {
        Panel pnl = Panel.of(60, 40);
        AnimatingMathPlot p1 = new AnimatingMathPlot(Size.of(30, 20), " Interfering Waves 1 ");
        AnimatingMathPlot p2 = new AnimatingMathPlot(Size.of(30, 20), " Interfering Waves 2 ");
        AnimatingMathPlot p3 = new AnimatingMathPlot(Size.of(30, 20), " Interfering Waves 3 ");
        AnimatingMathPlot p4 = new AnimatingMathPlot(Size.of(30, 20), " Interfering Waves 4 ");
        Canvas v1 = pnl.view(0, 0, 30, 20);
        Canvas v2 = pnl.view(30, 0, 30, 20);
        Canvas v3 = pnl.view(0, 20, 30, 20);
        Canvas v4 = pnl.view(30, 20, 30, 20);

        System.out.print(Ansi.hideCursor());
        try {
            for (int i = 0; i < 400; i++) {
                if (i > 0) {
                    System.out.print(Ansi.cursorMove(Ansi.CURSOR_PREV_LINE, pnl.size().height()));
                }

                p1.update();
                p2.update();
                p3.update();
                p4.update();

                p1.render(v1);
                p2.render(v2);
                p3.render(v3);
                p4.render(v4);

                System.out.println(pnl.toAnsiString());

                Thread.sleep(20);
            }
        } finally {
            System.out.print(Ansi.showCursor());
        }
    }
}

class AnimatingMathPlot implements Widget {
    private final MathPlot p;
    private final Framed f;

    // random generator and mutable parameter bases/variations
    private static Random rng = new Random();
    private double a1Base, a2Base, a1Var, a2Var;
    private double k1Base, k2Base, k1Var, k2Var;
    private double speed1, speed2;
    private int frame = 0;

    AnimatingMathPlot(Size size, String title) {
        p = MathPlot.of(size.grow(-2, -2)).ranges(-2 * Math.PI, 2 * Math.PI, -2.0, 2.0);
        f = Framed.of(p).title(Line.of(title));

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
    }

    @Override
    public @NonNull Size size() {
        return f.size();
    }

    public boolean update() {
        // time parameter
        double t = frame++ * 0.08;

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
        p.clear();
        p.plot(x -> a1 * Math.sin(k1 * x + phase1), Style.ofFgColor(Color.BasicColor.GREEN));
        p.plot(x -> a2 * Math.sin(k2 * x + phase2), Style.ofFgColor(Color.BasicColor.RED));

        return true;
    }

    @Override
    public void render(Canvas canvas) {
        f.render(canvas);
    }
}
