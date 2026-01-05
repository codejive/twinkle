package examples;

import org.codejive.twinkle.core.widget.Buffer;
import org.codejive.twinkle.core.widget.Canvas;
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
        Buffer buf = Buffer.of(20, 1);
        Bar b = Bar.bar().setValue(42);
        b.render(buf);
        System.out.println(buf);
    }

    private static void printHorizontalBars() {
        Buffer buf = Buffer.of(20, 4);
        FracBarConfig cfg = FracBarConfig.create();
        renderHorizontal(buf, cfg);
        System.out.println(buf);

        cfg.direction(BarConfig.Direction.R2L);
        renderHorizontal(buf, cfg);
        System.out.println(buf);
    }

    private static void renderHorizontal(Buffer buf, FracBarConfig cfg) {
        for (int i = 0; i < buf.size().height(); i++) {
            Canvas v = buf.view(0, i, 20, 1);
            Bar b = new Bar(cfg).setValue(30 + i * 27);
            b.render(v);
        }
    }

    private static void printVerticalBars() {
        Buffer buf = Buffer.of(16, 8);
        FracBarConfig cfg = FracBarConfig.create().direction(BarConfig.Direction.B2T);
        renderVertical(buf, cfg);
        System.out.println(buf);

        cfg.direction(BarConfig.Direction.T2B);
        renderVertical(buf, cfg);
        System.out.println(buf);
    }

    private static void renderVertical(Buffer buf, FracBarConfig cfg) {
        for (int i = 0; i < buf.size().width(); i++) {
            Canvas v = buf.view(i, 0, 1, 8);
            Bar b = new Bar(cfg).setValue(30 + i * 5.4d);
            b.render(v);
        }
    }
}
