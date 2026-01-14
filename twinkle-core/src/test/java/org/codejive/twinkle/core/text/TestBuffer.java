package org.codejive.twinkle.core.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Color;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.util.Size;
import org.junit.jupiter.api.Test;

public class TestBuffer {

    @Test
    public void testBufferCreation() {
        Buffer buffer = Buffer.of(10, 5);
        Size size = buffer.size();
        assertThat(size.width()).isEqualTo(10);
        assertThat(size.height()).isEqualTo(5);
    }

    public void testStyledBufferPutGetChar() {
        Buffer buffer = Buffer.of(10, 1);
        for (int i = 0; i < buffer.size().width(); i++) {
            buffer.putCharAt(i, 0, Style.ITALIC, (char) ('a' + i));
        }
        for (int i = 0; i < buffer.size().width(); i++) {
            assertThat(buffer.charAt(i, 0)).isEqualTo((char) ('a' + i));
            assertThat(buffer.styleAt(i, 0)).isEqualTo(Style.DEFAULT.italic());
        }
    }

    @Test
    public void testStyledBufferPutCharToString() {
        Buffer buffer = Buffer.of(10, 1);
        for (int i = 0; i < buffer.size().width(); i++) {
            buffer.putCharAt(i, 0, Style.ITALIC, (char) ('a' + i));
        }
        assertThat(buffer.toString()).isEqualTo("abcdefghij");
    }

    @Test
    public void testStyledBufferPutCharToAnsiString() {
        Buffer buffer = Buffer.of(10, 1);
        for (int i = 0; i < buffer.size().width(); i++) {
            Style style = i < 5 ? Style.ITALIC : Style.UNDERLINED;
            buffer.putCharAt(i, 0, style, (char) ('a' + i));
        }
        assertThat(buffer.toAnsiString())
                .isEqualTo(
                        Ansi.STYLE_RESET
                                + Ansi.style(Ansi.ITALICIZED)
                                + "abcde"
                                + Ansi.style(Ansi.NOTITALICIZED, Ansi.UNDERLINED)
                                + "fghij");
    }

    @Test
    public void testStyledBufferPutCharToAnsiStringWithCurrentStyle() {
        Buffer buffer = Buffer.of(10, 1);
        for (int i = 0; i < buffer.size().width(); i++) {
            Style style = i < 5 ? Style.ITALIC : Style.UNDERLINED;
            buffer.putCharAt(i, 0, style, (char) ('a' + i));
        }
        assertThat(buffer.toAnsiString(Style.DEFAULT.italic()))
                .isEqualTo("abcde" + Ansi.style(Ansi.NOTITALICIZED, Ansi.UNDERLINED) + "fghij");
    }

    @Test
    public void testStyledBufferPutCharToAnsiStringWithUnderAndOverflow() {
        Buffer buffer = Buffer.of(10, 1);
        for (int i = 0; i < buffer.size().width() + 10; i++) {
            Style style = i < 10 ? Style.ITALIC : Style.UNDERLINED;
            buffer.putCharAt(i - 5, 0, style, (char) ('a' + i));
        }
        assertThat(buffer.toAnsiString())
                .isEqualTo(
                        Ansi.STYLE_RESET
                                + Ansi.style(Ansi.ITALICIZED)
                                + "fghij"
                                + Ansi.style(Ansi.NOTITALICIZED, Ansi.UNDERLINED)
                                + "klmno");
    }

    @Test
    public void testStyledBufferPutStringGetChar() {
        Buffer buffer = Buffer.of(10, 1);
        buffer.putStringAt(0, 0, Style.DEFAULT.italic(), "abcdefghij");
        for (int i = 0; i < buffer.size().width(); i++) {
            assertThat(buffer.charAt(i, 0)).isEqualTo((char) ('a' + i));
            assertThat(buffer.styleAt(i, 0)).isEqualTo(Style.DEFAULT.italic());
        }
    }

    @Test
    public void testBufferDefaultInnerContent() {
        Buffer buffer = Buffer.of(10, 5);
        Size size = buffer.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                assertThat(buffer.charAt(x, y)).isEqualTo('\0');
                assertThat(buffer.styleAt(x, y)).isEqualTo(Style.DEFAULT);
            }
        }
    }

    @Test
    public void testBufferDefaultOuterContent() {
        Buffer buffer = Buffer.of(10, 5);
        Size size = buffer.size();
        for (int y = -5; y < size.height() + 5; y++) {
            for (int x = -5; x < size.width() + 5; x++) {
                if (x >= 0 && x < size.width() && y >= 0 && y < size.height()) {
                    continue; // Skip inner content
                }
                assertThat(buffer.charAt(x, y)).isEqualTo(Buffer.REPLACEMENT_CHAR);
                assertThat(buffer.styleAt(x, y)).isEqualTo(Style.UNSTYLED);
            }
        }
    }

    @Test
    public void testBufferNewContents() {
        Buffer buffer = createBuffer();
        Size size = buffer.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                assertThat(buffer.charAt(x, y)).isEqualTo((char) ('A' + x + y * size.width()));
                assertThat(buffer.styleAt(x, y)).isEqualTo(Style.DEFAULT.fgColor(Color.indexed(x)));
            }
        }
    }

    @Test
    public void testBufferView() {
        Buffer buffer = createBuffer();
        Buffer view = buffer.view(1, 1, 3, 3);
        Size size = view.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                assertThat(view.charAt(x, y))
                        .isEqualTo((char) ('G' + x + y * buffer.size().width()));
                assertThat(view.styleAt(x, y))
                        .isEqualTo(Style.DEFAULT.fgColor(Color.indexed(x + 1)));
            }
        }
    }

    @Test
    public void testBufferViewPutString() {
        Buffer buffer = createBuffer();
        Buffer view = buffer.view(1, 1, 3, 3);
        view.putStringAt(1, 1, Style.DEFAULT.fgColor(Color.BasicColor.RED), "XYZ");
        assertThat(buffer.toString())
                .isEqualTo("ABCDE\n" + "FGHIJ\n" + "KLXYO\n" + "PQRST\n" + "UVWXY");
    }

    @Test
    public void testBufferViewOutside() {
        Buffer buffer = createBuffer();
        Buffer view = buffer.view(1, 1, 3, 3);
        Size size = view.size();
        for (int y = -2; y < size.height() + 2; y++) {
            for (int x = -2; x < size.width() + 2; x++) {
                if (x >= 0 && x < size.width() && y >= 0 && y < size.height()) {
                    continue; // Skip inner content
                }
                assertThat(view.charAt(x, y)).isEqualTo(Buffer.REPLACEMENT_CHAR);
                assertThat(view.styleAt(x, y)).isEqualTo(Style.UNSTYLED);
            }
        }
    }

    @Test
    public void testBufferNestedView() {
        Buffer buffer = createBuffer();
        Buffer view1 = buffer.view(1, 1, 3, 3);
        Buffer view2 = view1.view(1, 1, 2, 2);
        Size size = view2.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                assertThat(view2.charAt(x, y))
                        .isEqualTo((char) ('M' + x + y * buffer.size().width()));
                assertThat(view2.styleAt(x, y))
                        .isEqualTo(Style.DEFAULT.fgColor(Color.indexed(x + 2)));
            }
        }
        assertThat(view2.toString()).isEqualTo("MN\nRS");
        assertThat(view2.toAnsiString())
                .isEqualTo(
                        Ansi.CSI
                                + "0m"
                                + Ansi.CSI
                                + "38;5;2mM"
                                + Ansi.CSI
                                + "38;5;3mN\n"
                                + Ansi.CSI
                                + "38;5;2mR"
                                + Ansi.CSI
                                + "38;5;3mS");
    }

    @Test
    public void testBufferNestedViewPutString() {
        Buffer buffer = createBuffer();
        Buffer view1 = buffer.view(1, 1, 3, 3);
        Buffer view2 = view1.view(1, 1, 2, 2);
        view2.putStringAt(0, 1, Style.DEFAULT.fgColor(Color.BasicColor.RED), "XYZ");
        assertThat(buffer.toString())
                .isEqualTo("ABCDE\n" + "FGHIJ\n" + "KLMNO\n" + "PQXYT\n" + "UVWXY");
    }

    @Test
    public void testBufferNestedViewOutside() {
        Buffer buffer = createBuffer();
        Buffer view1 = buffer.view(1, 1, 3, 3);
        Buffer view2 = view1.view(1, 1, 2, 2);
        Size size = view2.size();
        for (int y = -2; y < size.height() + 2; y++) {
            for (int x = -2; x < size.width() + 2; x++) {
                if (x >= 0 && x < size.width() && y >= 0 && y < size.height()) {
                    continue; // Skip inner content
                }
                assertThat(view2.charAt(x, y)).isEqualTo(Buffer.REPLACEMENT_CHAR);
                assertThat(view2.styleAt(x, y)).isEqualTo(Style.UNSTYLED);
            }
        }
    }

    @Test
    public void testBufferNestedViewOutsideInside() {
        Buffer buffer = createBuffer();
        Buffer view1 = buffer.view(10, 10, 3, 3);
        Buffer view2 = view1.view(-10, -10, 2, 2);

        Size size = view2.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                assertThat(view2.charAt(x, y)).isEqualTo(Buffer.REPLACEMENT_CHAR);
                assertThat(view2.styleAt(x, y)).isEqualTo(Style.UNSTYLED);
            }
        }
    }

    @Test
    public void testBufferNestedViewMoved() {
        Buffer buffer = createBuffer();
        Buffer.View view1 = buffer.view(1, 1, 3, 3);
        Buffer view2 = view1.view(1, 1, 2, 2);

        view1.moveBy(1, 1);

        Size size = view2.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                assertThat(view2.charAt(x, y))
                        .isEqualTo((char) ('S' + x + y * buffer.size().width()));
                assertThat(view2.styleAt(x, y))
                        .isEqualTo(Style.DEFAULT.fgColor(Color.indexed(x + 3)));
            }
        }
    }

    @Test
    public void testBufferNestedViewMovedFullyOutside() {
        Buffer buffer = createBuffer();
        Buffer.View view1 = buffer.view(1, 1, 3, 3);
        Buffer view2 = view1.view(1, 1, 2, 2);

        view1.moveBy(10, 10);

        Size size = view2.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                assertThat(view2.charAt(x, y)).isEqualTo(Buffer.REPLACEMENT_CHAR);
                assertThat(view2.styleAt(x, y)).isEqualTo(Style.UNSTYLED);
            }
        }
    }

    @Test
    public void testBufferNestedViewMovedPartiallyOutside() {
        Buffer buffer = createBuffer();
        Buffer.View view1 = buffer.view(1, 1, 3, 3);
        Buffer view2 = view1.view(1, 1, 2, 2);

        view1.moveTo(3, 3);

        Size size = view2.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                if (y == 0 && x == 0) {
                    assertThat(view2.charAt(x, y)).isEqualTo('Y');
                    assertThat(view2.styleAt(x, y))
                            .isEqualTo(Style.DEFAULT.fgColor(Color.indexed(x + 4)));
                } else {
                    assertThat(view2.charAt(x, y)).isEqualTo(Buffer.REPLACEMENT_CHAR);
                    assertThat(view2.styleAt(x, y)).isEqualTo(Style.UNSTYLED);
                }
            }
        }
    }

    private Buffer createBuffer() {
        Buffer buffer = Buffer.of(5, 5);
        Size size = buffer.size();
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                buffer.putCharAt(
                        x,
                        y,
                        Style.ofFgColor(Color.indexed(x)),
                        (char) ('A' + x + y * size.width()));
            }
        }
        return buffer;
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
