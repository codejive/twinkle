package examples;

import org.codejive.twinkle.core.widget.Canvas;
import org.codejive.twinkle.core.widget.Panel;
import org.codejive.twinkle.widgets.graphs.bar.Bar;
import org.codejive.twinkle.widgets.graphs.bar.BarConfig;
import org.codejive.twinkle.widgets.graphs.bar.FracBarConfig;

public class BarDemo {
    public static void main(String[] args) {
        System.out.println("Simple Bar:");
        printSimpleBar();

        System.out.println("Horizontal Bars:");
        printHorizontalBars();

        System.out.println("Vertical Bars:");
        printVerticalBars();
    }

    private static void printSimpleBar() {
        Panel pnl = Panel.of(20, 1);
        Bar b = Bar.bar().setValue(42);
        b.render(pnl);
        System.out.println(pnl);
    }

    private static void printHorizontalBars() {
        Panel pnl = Panel.of(20, 4);
        FracBarConfig cfg = FracBarConfig.create();
        renderHorizontal(pnl, cfg);
        System.out.println(pnl);

        cfg.direction(BarConfig.Direction.R2L);
        renderHorizontal(pnl, cfg);
        System.out.println(pnl);
    }

    private static void renderHorizontal(Panel pnl, FracBarConfig cfg) {
        for (int i = 0; i < pnl.size().height(); i++) {
            Canvas v = pnl.view(0, i, 20, 1);
            Bar b = new Bar(cfg).setValue(30 + i * 27);
            b.render(v);
        }
    }

    private static void printVerticalBars() {
        Panel pnl = Panel.of(16, 8);
        FracBarConfig cfg = FracBarConfig.create().direction(BarConfig.Direction.B2T);
        renderVertical(pnl, cfg);
        System.out.println(pnl);

        cfg.direction(BarConfig.Direction.T2B);
        renderVertical(pnl, cfg);
        System.out.println(pnl);
    }

    private static void renderVertical(Panel pnl, FracBarConfig cfg) {
        for (int i = 0; i < pnl.size().width(); i++) {
            Canvas v = pnl.view(i, 0, 1, 8);
            Bar b = new Bar(cfg).setValue(30 + i * 5.4d);
            b.render(v);
        }
    }
}
