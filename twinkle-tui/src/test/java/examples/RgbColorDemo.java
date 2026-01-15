// The code for this demo was largely copied from the tamboui project:
package examples;

import java.util.ArrayList;
import java.util.List;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.text.Canvas;
import org.codejive.twinkle.core.text.Line;
import org.codejive.twinkle.core.util.Size;
import org.codejive.twinkle.core.widget.Widget;
import org.codejive.twinkle.tui.application.App;
import org.codejive.twinkle.tui.widgets.Framed;
import org.jspecify.annotations.NonNull;

public class RgbColorDemo {
    public static void main(String[] args) throws Exception {
        RgbColorWidget w = new RgbColorWidget();
        Widget f = new FpsFrameWidget().widget(w);
        App.run(f);
        //App.using(f).quitOnQ(true).limitFps(30).start();
    }

    static class RgbColorWidget implements Widget {
        private final List<List<Color>> colors = new ArrayList<>();
        private final Size size;
        private int frameCount = 0;

        public RgbColorWidget() {
            this.size = Size.of(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        @Override
        public @NonNull Size size() {
            return size;
        }

        @Override
        public void render(Canvas canvas) {
            Size sz = canvas.size();
            setupColors(sz);
            int width = sz.width();
            int height = sz.height();
            int areaX = 0;
            int areaY = 0;

            // Pre-calculate the symbol string to avoid repeated string creation
            String halfBlock = "▀";

            // Pre-calculate modulo for animation
            int frameMod = frameCount % width;

            // Optimize: cache color lists to avoid repeated get() calls
            // Reorder loops to cache row lookups
            for (int y = 0; y < height; y++) {
                int colorY = y * 2;
                List<Color> fgRow = colors.get(colorY);
                List<Color> bgRow = colors.get(colorY + 1);

                for (int x = 0; x < width; x++) {
                    // Animate the colors by shifting the x index by the frame number
                    int xi = (x + frameMod) % width;

                    // Render a half block character using the foreground and background colors
                    Color fg = fgRow.get(xi);
                    Color bg = bgRow.get(xi);
                    Style style = Style.UNSTYLED.fgColor(fg).bgColor(bg);
                    canvas.putCharAt(areaX + x, areaY + y, style, halfBlock);
                }
            }
            frameCount++;
        }

        private void setupColors(Size size) {
            int width = size.width();
            int height = size.height() * 2;

            // Only update the colors if the size has changed
            if (colors.size() == height && !colors.isEmpty() && colors.get(0).size() == width) {
                return;
            }

            colors.clear();
            for (int y = 0; y < height; y++) {
                List<Color> row = new ArrayList<>();
                for (int x = 0; x < width; x++) {
                    // Generate colors using HSV color space
                    // Hue varies from 0 to 360 across the width
                    // Value (brightness) varies from 1.0 to 0.0 from top to bottom
                    // Saturation is always maximum (1.0)
                    float hue = x * 360.0f / width;
                    float value = (height - y) / (float) height;
                    float saturation = 1.0f;

                    // Convert HSV to RGB
                    Color rgb = hsvToRgb(hue, saturation, value);
                    row.add(rgb);
                }
                colors.add(row);
            }
        }

        /**
         * Converts HSV color to RGB.
         *
         * @param h hue in degrees (0-360)
         * @param s saturation (0-1)
         * @param v value/brightness (0-1)
         * @return RGB color
         */
        private Color hsvToRgb(float h, float s, float v) {
            float c = v * s;
            float hPrime = h / 60.0f;
            float x = c * (1 - Math.abs((hPrime % 2) - 1));
            float m = v - c;

            float r, g, b;
            if (hPrime < 1) {
                r = c;
                g = x;
                b = 0;
            } else if (hPrime < 2) {
                r = x;
                g = c;
                b = 0;
            } else if (hPrime < 3) {
                r = 0;
                g = c;
                b = x;
            } else if (hPrime < 4) {
                r = 0;
                g = x;
                b = c;
            } else if (hPrime < 5) {
                r = x;
                g = 0;
                b = c;
            } else {
                r = c;
                g = 0;
                b = x;
            }

            int red = Math.round((r + m) * 255);
            int green = Math.round((g + m) * 255);
            int blue = Math.round((b + m) * 255);

            // Clamp to valid range
            red = Math.max(0, Math.min(255, red));
            green = Math.max(0, Math.min(255, green));
            blue = Math.max(0, Math.min(255, blue));

            return Color.rgb(red, green, blue);
        }
    }

    private static class FpsFrameWidget extends Framed {
        private final Size size = Size.of(10, 1);
        private int frameCount = 0;
        private long lastTime = System.currentTimeMillis();
        private Float fps = null;

        @Override
        public @NonNull Size size() {
            return size;
        }

        public Line title() {
            String text = " RGB Color Demo ";
            if (fps != null) {
                text += String.format("[%.1f fps] ", fps);
            }
            return Line.of(text);
        }

        public void render(Canvas canvas) {
            calculateFps();
            super.render(canvas);
        }

        private void calculateFps() {
            frameCount++;
            long now = System.currentTimeMillis();
            long elapsed = now - lastTime;
            if (elapsed > 1000 && frameCount > 2) {
                fps = (float) frameCount / (elapsed / 1000.0f);
                frameCount = 0;
                lastTime = now;
            }
        }
    }
}
