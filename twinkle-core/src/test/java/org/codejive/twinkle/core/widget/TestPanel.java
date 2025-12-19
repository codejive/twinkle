package org.codejive.twinkle.core.widget;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.text.StyledBuffer;
import org.junit.jupiter.api.Test;

public class TestPanel {

    @Test
    public void testPanelCreation() {
        Panel panel = Panel.of(10, 5);
        Size size = panel.size();
        assertThat(size.width()).isEqualTo(10);
        assertThat(size.height()).isEqualTo(5);
    }

    @Test
    public void testPanelDefaultInnerContent() {
        Panel panel = Panel.of(10, 5);
        Size size = panel.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                assertThat(panel.charAt(x, y)).isEqualTo('\0');
                assertThat(panel.styleAt(x, y)).isEqualTo(Style.UNSTYLED);
            }
        }
    }

    @Test
    public void testPanelDefaultOuterContent() {
        Panel panel = Panel.of(10, 5);
        Size size = panel.size();
        for (int y = -5; y < size.height() + 5; y++) {
            for (int x = -5; x < size.width() + 5; x++) {
                if (x >= 0 && x < size.width() && y >= 0 && y < size.height()) {
                    continue; // Skip inner content
                }
                assertThat(panel.charAt(x, y)).isEqualTo(StyledBuffer.REPLACEMENT_CHAR);
                assertThat(panel.styleAt(x, y)).isEqualTo(Style.UNSTYLED);
            }
        }
    }

    @Test
    public void testPanelNewContents() {
        Panel panel = createPanel();
        Size size = panel.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                assertThat(panel.charAt(x, y)).isEqualTo((char) ('A' + x + y * size.width()));
                assertThat(panel.styleAt(x, y)).isEqualTo(Style.ofFgColor(Color.indexed(x)));
            }
        }
    }

    @Test
    public void testPanelView() {
        Panel panel = createPanel();
        Canvas view = panel.view(1, 1, 3, 3);
        Size size = view.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                assertThat(view.charAt(x, y))
                        .isEqualTo((char) ('G' + x + y * panel.size().width()));
                assertThat(view.styleAt(x, y)).isEqualTo(Style.ofFgColor(Color.indexed(x + 1)));
            }
        }
    }

    @Test
    public void testPanelViewOutside() {
        Panel panel = createPanel();
        Canvas view = panel.view(1, 1, 3, 3);
        Size size = view.size();
        for (int y = -2; y < size.height() + 2; y++) {
            for (int x = -2; x < size.width() + 2; x++) {
                if (x >= 0 && x < size.width() && y >= 0 && y < size.height()) {
                    continue; // Skip inner content
                }
                assertThat(view.charAt(x, y)).isEqualTo(StyledBuffer.REPLACEMENT_CHAR);
                assertThat(view.styleAt(x, y)).isEqualTo(Style.UNSTYLED);
            }
        }
    }

    @Test
    public void testPanelNestedView() {
        Panel panel = createPanel();
        Panel view1 = panel.view(1, 1, 3, 3);
        Panel view2 = view1.view(1, 1, 2, 2);
        Size size = view2.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                assertThat(view2.charAt(x, y))
                        .isEqualTo((char) ('M' + x + y * panel.size().width()));
                assertThat(view2.styleAt(x, y)).isEqualTo(Style.ofFgColor(Color.indexed(x + 2)));
            }
        }
    }

    @Test
    public void testPanelNestedViewOutside() {
        Panel panel = createPanel();
        Panel view1 = panel.view(1, 1, 3, 3);
        Panel view2 = view1.view(1, 1, 2, 2);
        Size size = view2.size();
        for (int y = -2; y < size.height() + 2; y++) {
            for (int x = -2; x < size.width() + 2; x++) {
                if (x >= 0 && x < size.width() && y >= 0 && y < size.height()) {
                    continue; // Skip inner content
                }
                assertThat(view2.charAt(x, y)).isEqualTo(StyledBuffer.REPLACEMENT_CHAR);
                assertThat(view2.styleAt(x, y)).isEqualTo(Style.UNSTYLED);
            }
        }
    }

    @Test
    public void testPanelNestedViewMoved() {
        Panel panel = createPanel();
        PanelView view1 = panel.view(1, 1, 3, 3);
        Panel view2 = view1.view(1, 1, 2, 2);

        view1.moveBy(1, 1);

        Size size = view2.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                assertThat(view2.charAt(x, y))
                        .isEqualTo((char) ('S' + x + y * panel.size().width()));
                assertThat(view2.styleAt(x, y)).isEqualTo(Style.ofFgColor(Color.indexed(x + 3)));
            }
        }
    }

    @Test
    public void testPanelNestedViewMovedFullyOutside() {
        Panel panel = createPanel();
        PanelView view1 = panel.view(1, 1, 3, 3);
        Panel view2 = view1.view(1, 1, 2, 2);

        view1.moveBy(10, 10);

        Size size = view2.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                assertThat(view2.charAt(x, y)).isEqualTo(StyledBuffer.REPLACEMENT_CHAR);
                assertThat(view2.styleAt(x, y)).isEqualTo(Style.UNSTYLED);
            }
        }
    }

    @Test
    public void testPanelNestedViewMovedPartiallyOutside() {
        Panel panel = createPanel();
        PanelView view1 = panel.view(1, 1, 3, 3);
        Panel view2 = view1.view(1, 1, 2, 2);

        view1.moveTo(3, 3);

        Size size = view2.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                if (y == 0 && x == 0) {
                    assertThat(view2.charAt(x, y)).isEqualTo('Y');
                    assertThat(view2.styleAt(x, y))
                            .isEqualTo(Style.ofFgColor(Color.indexed(x + 4)));
                } else {
                    assertThat(view2.charAt(x, y)).isEqualTo(StyledBuffer.REPLACEMENT_CHAR);
                    assertThat(view2.styleAt(x, y)).isEqualTo(Style.UNSTYLED);
                }
            }
        }
    }

    private Panel createPanel() {
        Panel panel = Panel.of(5, 5);
        Size size = panel.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                panel.setCharAt(
                        x,
                        y,
                        Style.ofFgColor(Color.indexed(x)),
                        (char) ('A' + x + y * size.width()));
            }
        }
        return panel;
    }

    private void printCanvas(Canvas canvas) {
        Size size = canvas.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                System.out.print(canvas.charAt(x, y));
            }
            System.out.println();
        }
    }
}
