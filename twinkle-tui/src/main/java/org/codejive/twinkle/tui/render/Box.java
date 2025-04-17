package org.codejive.twinkle.tui.render;

import java.util.List;
import org.codejive.twinkle.core.component.Rect;
import org.codejive.twinkle.core.component.Rectangular;
import org.codejive.twinkle.tui.styles.Property;
import org.codejive.twinkle.tui.styles.Style;
import org.jline.utils.AttributedString;

public class Box implements Rectangular {
    private final List<AttributedString> content;
    private final Style style;

    public Box(List<AttributedString> content) {
        this(content, new Style());
    }

    public Box(List<AttributedString> content, Style style) {
        this.content = content;
        this.style = style;
    }

    public List<AttributedString> content() {
        return content;
    }

    public Style style() {
        return style;
    }

    /**
     * Returns top position as a quantity of <code>Unit.em</code> rounded to the nearest integer.
     *
     * @return top position in em
     */
    public int top() {
        return style().getAsEmInt(Property.top);
    }

    /**
     * Returns bottom position as a quantity of <code>Unit.em</code> rounded to the nearest integer.
     *
     * @return bottom position in em
     */
    public int bottom() {
        return style().getAsEmInt(Property.bottom);
    }

    /**
     * Returns left position as a quantity of <code>Unit.em</code> rounded to the nearest integer.
     *
     * @return left position in em
     */
    public int left() {
        return style().getAsEmInt(Property.left);
    }

    /**
     * Returns right position as a quantity of <code>Unit.em</code> rounded to the nearest integer.
     *
     * @return right position in em
     */
    public int right() {
        return style().getAsEmInt(Property.right);
    }

    /**
     * Returns width as a quantity of <code>Unit.em</code> rounded to the nearest integer.
     *
     * @return width in em
     */
    public int width() {
        return style().getAsEmPosInt(Property.width);
    }

    /**
     * Returns height as a quantity of <code>Unit.em</code> rounded to the nearest integer.
     *
     * @return height in em
     */
    public int height() {
        return style().getAsEmPosInt(Property.height);
    }

    @Override
    public Rect rect() {
        return new Rect(left(), top(), width(), height());
    }

    /**
     * Returns border width for the top as a positive quantity of <code>Unit.em</code> rounded to
     * the nearest integer and max value of 1.
     *
     * @return width in em
     */
    public int border_top_width() {
        return style().asEmPosInt(style().get(Property.border_top_width, Property.border_width));
    }

    /**
     * Returns border width for the bottom as a positive quantity of <code>Unit.em</code> rounded to
     * the nearest integer and max value of 1.
     *
     * @return width in em
     */
    public int border_bottom_width() {
        return style().asEmPosInt(style().get(Property.border_bottom_width, Property.border_width));
    }

    /**
     * Returns border width for the left as a positive quantity of <code>Unit.em</code> rounded to
     * the nearest integer and max value of 1.
     *
     * @return width in em
     */
    public int border_left_width() {
        return style().asEmPosInt(style().get(Property.border_left_width, Property.border_width));
    }

    /**
     * Returns border width for the right as a positive quantity of <code>Unit.em</code> rounded to
     * the nearest integer and max value of 1.
     *
     * @return width in em
     */
    public int border_right_width() {
        return style().asEmPosInt(style().get(Property.border_right_width, Property.border_width));
    }
}
