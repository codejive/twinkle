package org.codejive.twinkle.tui.widgets;

import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.decor.SimpleBorderRenderer;
import org.codejive.twinkle.core.text.Canvas;
import org.codejive.twinkle.core.text.Line;
import org.codejive.twinkle.core.util.Rect;
import org.codejive.twinkle.core.util.Size;
import org.codejive.twinkle.core.widget.Renderable;
import org.codejive.twinkle.core.widget.Widget;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Framed implements Widget {
    private @Nullable Widget widget;
    private @Nullable Renderable borderRenderer;
    private @Nullable Line title;

    public static Framed of() {
        return new Framed();
    }

    public static Framed of(Widget widget) {
        return new Framed().widget(widget);
    }

    public Framed widget(@Nullable Widget widget) {
        this.widget = widget;
        return this;
    }

    public Framed borderRenderer(@Nullable Renderable borderRenderer) {
        this.borderRenderer = borderRenderer;
        return this;
    }

    public Line title() {
        return title;
    }

    public Framed title(@Nullable Line title) {
        this.title = title;
        return this;
    }

    protected Framed() {
        this.borderRenderer = new SimpleBorderRenderer();
    }

    @Override
    public @NonNull Size size() {
        if (widget == null) {
            return Size.of(2, 2);
        }
        return widget.size().grow(2, 2);
    }

    @Override
    public void render(Canvas canvas) {
        if (borderRenderer != null) {
            borderRenderer.render(canvas);
        }
        Line t = title();
        if (t != null) {
            Canvas view = canvas.view(2, 0, canvas.size().width() - 4, 1);
            view.putStringAt(0, 0, Style.DEFAULT, t.toAnsiString());
        }
        if (widget != null) {
            widget.render(canvas.view(Rect.of(canvas.size()).grow(-1, -1, -1, -1)));
        }
    }
}
