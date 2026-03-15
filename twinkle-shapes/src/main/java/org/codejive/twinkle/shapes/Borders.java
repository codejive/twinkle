package org.codejive.twinkle.shapes;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.ansi.util.Printable;
import org.codejive.twinkle.screen.Buffer;
import org.codejive.twinkle.screen.Buffer.StylePrintOption;
import org.codejive.twinkle.screen.RenderTarget;
import org.codejive.twinkle.shapes.util.Draw;
import org.jspecify.annotations.NonNull;

public class Borders implements Printable {
    private LineStyle leftLineStyle;
    private LineStyle rightLineStyle;
    private LineStyle topLineStyle;
    private LineStyle bottomLineStyle;
    private CornerStyle cornerStyle;
    private Style style;

    public static Borders ascii() {
        return new Borders();
    }

    public static Borders singleSquare() {
        return new Borders().lineStyle(LineStyle.SINGLE).cornerStyle(CornerStyle.SQUARE);
    }

    public static Borders singleRound() {
        return new Borders().lineStyle(LineStyle.SINGLE).cornerStyle(CornerStyle.ROUND);
    }

    public static Borders doubleSquare() {
        return new Borders().lineStyle(LineStyle.DOUBLE).cornerStyle(CornerStyle.SQUARE);
    }

    public static Borders doubleRound() {
        return new Borders().lineStyle(LineStyle.DOUBLE).cornerStyle(CornerStyle.ROUND);
    }

    public enum LineStyle {
        SINGLE('─', '│'),
        DOUBLE('═', '║'),
        ASCII('-', '|');

        public final char horizontalChar;
        public final char verticalChar;

        LineStyle(char horizontalChar, char verticalChar) {
            this.horizontalChar = horizontalChar;
            this.verticalChar = verticalChar;
        }
    }

    public enum CornerStyle {
        ROUND('╭', '╮', '╰', '╯'),
        SQUARE('┌', '┐', '└', '┘'),
        ASCII('+', '+', '+', '+');

        public final char topLeftChar;
        public final char topRightChar;
        public final char bottomLeftChar;
        public final char bottomRightChar;

        CornerStyle(
                char topLeftChar, char topRightChar, char bottomLeftChar, char bottomRightChar) {
            this.topLeftChar = topLeftChar;
            this.topRightChar = topRightChar;
            this.bottomLeftChar = bottomLeftChar;
            this.bottomRightChar = bottomRightChar;
        }
    }

    public Borders() {
        this.leftLineStyle = LineStyle.ASCII;
        this.rightLineStyle = LineStyle.ASCII;
        this.topLineStyle = LineStyle.ASCII;
        this.bottomLineStyle = LineStyle.ASCII;
        this.cornerStyle = CornerStyle.ASCII;
        this.style = Style.UNSTYLED;
    }

    public Borders lineStyle(LineStyle lineStyle) {
        this.leftLineStyle = lineStyle;
        this.rightLineStyle = lineStyle;
        this.topLineStyle = lineStyle;
        this.bottomLineStyle = lineStyle;
        return this;
    }

    public Borders leftLineStyle(LineStyle lineStyle) {
        this.leftLineStyle = lineStyle;
        return this;
    }

    public Borders rightLineStyle(LineStyle lineStyle) {
        this.rightLineStyle = lineStyle;
        return this;
    }

    public Borders topLineStyle(LineStyle lineStyle) {
        this.topLineStyle = lineStyle;
        return this;
    }

    public Borders bottomLineStyle(LineStyle lineStyle) {
        this.bottomLineStyle = lineStyle;
        return this;
    }

    public Borders cornerStyle(CornerStyle cornerStyle) {
        this.cornerStyle = cornerStyle;
        return this;
    }

    public Borders style(Style style) {
        this.style = style;
        return this;
    }

    private char corner(char corner, LineStyle lineStyle1, LineStyle lineStyle2) {
        if (cornerStyle != CornerStyle.SQUARE) {
            return corner;
        }
        if (lineStyle1 == LineStyle.DOUBLE && lineStyle2 == LineStyle.DOUBLE) {
            switch (corner) {
                case '┌':
                    return '╔';
                case '┐':
                    return '╗';
                case '└':
                    return '╚';
                case '┘':
                    return '╝';
            }
        } else if (lineStyle1 == LineStyle.DOUBLE && lineStyle2 == LineStyle.SINGLE) {
            switch (corner) {
                case '┌':
                    return '╓';
                case '┐':
                    return '╖';
                case '└':
                    return '╙';
                case '┘':
                    return '╜';
            }
        } else if (lineStyle1 == LineStyle.SINGLE && lineStyle2 == LineStyle.DOUBLE) {
            switch (corner) {
                case '┌':
                    return '╒';
                case '┐':
                    return '╕';
                case '└':
                    return '╘';
                case '┘':
                    return '╛';
            }
        }
        return corner;
    }

    public void render(RenderTarget target) {
        StylePrintOption styleOpt = Buffer.styleOpt(style);
        target.putAt(0, 0, corner(cornerStyle.topLeftChar, leftLineStyle, topLineStyle), styleOpt);
        Draw.lineH(target, 1, 0, target.size().width() - 1, topLineStyle.horizontalChar, styleOpt);
        target.putAt(
                target.size().width() - 1,
                0,
                corner(cornerStyle.topRightChar, rightLineStyle, topLineStyle),
                styleOpt);
        Draw.lineV(target, 0, 1, target.size().height() - 1, leftLineStyle.verticalChar, styleOpt);
        target.putAt(
                0,
                target.size().height() - 1,
                corner(cornerStyle.bottomLeftChar, leftLineStyle, bottomLineStyle),
                styleOpt);
        Draw.lineH(
                target,
                1,
                target.size().height() - 1,
                target.size().width() - 1,
                bottomLineStyle.horizontalChar,
                styleOpt);
        target.putAt(
                target.size().width() - 1,
                target.size().height() - 1,
                corner(cornerStyle.bottomRightChar, rightLineStyle, bottomLineStyle),
                styleOpt);
        Draw.lineV(
                target,
                target.size().width() - 1,
                1,
                target.size().height() - 1,
                rightLineStyle.verticalChar,
                styleOpt);
    }

    @Override
    public @NonNull Appendable toAnsi(@NonNull Appendable appendable, @NonNull Style currentStyle) {
        return appendable;
    }
}
