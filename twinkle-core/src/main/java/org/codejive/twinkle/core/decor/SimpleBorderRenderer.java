package org.codejive.twinkle.core.decor;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.text.Canvas;
import org.codejive.twinkle.core.widget.Renderable;

public class SimpleBorderRenderer implements Renderable {
    private LineStyle leftLineStyle;
    private LineStyle rightLineStyle;
    private LineStyle topLineStyle;
    private LineStyle bottomLineStyle;
    private CornerStyle cornerStyle;
    private long styleState;

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
        ROUNDED('╭', '╮', '╰', '╯'),
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

    public SimpleBorderRenderer() {
        this.leftLineStyle = LineStyle.SINGLE;
        this.rightLineStyle = LineStyle.SINGLE;
        this.topLineStyle = LineStyle.SINGLE;
        this.bottomLineStyle = LineStyle.SINGLE;
        this.cornerStyle = CornerStyle.ROUNDED;
        this.styleState = Style.F_UNSTYLED;
    }

    public SimpleBorderRenderer lineStyle(LineStyle lineStyle) {
        this.leftLineStyle = lineStyle;
        this.rightLineStyle = lineStyle;
        this.topLineStyle = lineStyle;
        this.bottomLineStyle = lineStyle;
        return this;
    }

    public SimpleBorderRenderer leftLineStyle(LineStyle lineStyle) {
        this.leftLineStyle = lineStyle;
        return this;
    }

    public SimpleBorderRenderer rightLineStyle(LineStyle lineStyle) {
        this.rightLineStyle = lineStyle;
        return this;
    }

    public SimpleBorderRenderer topLineStyle(LineStyle lineStyle) {
        this.topLineStyle = lineStyle;
        return this;
    }

    public SimpleBorderRenderer bottomLineStyle(LineStyle lineStyle) {
        this.bottomLineStyle = lineStyle;
        return this;
    }

    public SimpleBorderRenderer cornerStyle(CornerStyle cornerStyle) {
        this.cornerStyle = cornerStyle;
        return this;
    }

    public SimpleBorderRenderer style(Style style) {
        return style(style.state());
    }

    public SimpleBorderRenderer style(long styleState) {
        this.styleState = styleState;
        return this;
    }

    @Override
    public void render(Canvas canvas) {
        canvas.setCharAt(
                0, 0, styleState, corner(cornerStyle.topLeftChar, leftLineStyle, topLineStyle));
        canvas.drawHLineAt(
                1, 0, canvas.size().width() - 1, styleState, topLineStyle.horizontalChar);
        canvas.setCharAt(
                canvas.size().width() - 1,
                0,
                styleState,
                corner(cornerStyle.topRightChar, rightLineStyle, topLineStyle));
        canvas.drawVLineAt(
                0, 1, canvas.size().height() - 1, styleState, leftLineStyle.verticalChar);
        canvas.setCharAt(
                0,
                canvas.size().height() - 1,
                styleState,
                corner(cornerStyle.bottomLeftChar, leftLineStyle, bottomLineStyle));
        canvas.drawHLineAt(
                1,
                canvas.size().height() - 1,
                canvas.size().width() - 1,
                styleState,
                bottomLineStyle.horizontalChar);
        canvas.setCharAt(
                canvas.size().width() - 1,
                canvas.size().height() - 1,
                styleState,
                corner(cornerStyle.bottomRightChar, rightLineStyle, bottomLineStyle));
        canvas.drawVLineAt(
                canvas.size().width() - 1,
                1,
                canvas.size().height() - 1,
                styleState,
                rightLineStyle.verticalChar);
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
}
