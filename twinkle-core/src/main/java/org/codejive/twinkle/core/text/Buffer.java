package org.codejive.twinkle.core.text;

import static org.codejive.twinkle.core.text.LineBuffer.REPLACEMENT_CHAR;

import java.io.IOException;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.util.Rect;
import org.codejive.twinkle.core.util.Size;
import org.codejive.twinkle.util.Printable;
import org.codejive.twinkle.util.StyledIterator;
import org.jspecify.annotations.NonNull;

public interface Buffer extends Canvas, Printable {

    @NonNull Buffer resize(@NonNull Size newSize);

    @Override
    default @NonNull View view(int left, int top, int width, int height) {
        return view(new Rect(left, top, width, height));
    }

    @Override
    @NonNull View view(@NonNull Rect rect);

    static @NonNull Buffer of(int width, int height) {
        return of(Size.of(width, height));
    }

    static @NonNull Buffer of(@NonNull Size size) {
        return new BufferImpl(size);
    }

    static @NonNull Buffer of(@NonNull LineBuffer buffer) {
        Rect rect = Rect.of(buffer.length(), 1);
        LineBuffer[] lines = new LineBuffer[] {buffer};
        return new BufferImpl(rect, lines);
    }

    interface View extends Buffer {
        View moveTo(int x, int y);

        View moveBy(int dx, int dy);
    }
}

class BufferImpl implements Buffer {
    protected @NonNull Rect rect;
    protected @NonNull LineBuffer[] lines;

    public BufferImpl(@NonNull Size size) {
        this.rect = Rect.of(size);
        this.lines = new LineBuffer[size.height()];
        for (int i = 0; i < size.height(); i++) {
            lines[i] = createBuffer(size.width());
        }
    }

    protected BufferImpl(@NonNull Rect rect, @NonNull LineBuffer[] lines) {
        this.rect = rect;
        this.lines = lines;
    }

    @Override
    public @NonNull Size size() {
        return rect.size();
    }

    protected @NonNull Rect rect() {
        return rect;
    }

    @Override
    public char charAt(int x, int y) {
        if (outside(x, y, 0)) {
            return REPLACEMENT_CHAR;
        }
        return line(y).charAt(applyXOffset(x));
    }

    @Override
    public int codepointAt(int x, int y) {
        if (outside(x, y, 0)) {
            return REPLACEMENT_CHAR;
        }
        return line(y).codepointAt(applyXOffset(x));
    }

    @Override
    public @NonNull String graphemeAt(int x, int y) {
        if (outside(x, y, 0)) {
            return String.valueOf(REPLACEMENT_CHAR);
        }
        return line(y).graphemeAt(applyXOffset(x));
    }

    @Override
    public @NonNull Style styleAt(int x, int y) {
        if (outside(x, y, 0)) {
            return Style.UNSTYLED;
        }
        return line(y).styleAt(applyXOffset(x));
    }

    @Override
    public void setCharAt(int x, int y, @NonNull Style style, char c) {
        if (outside(x, y, 0)) {
            return;
        }
        line(y).setCharAt(applyXOffset(x), style, c);
    }

    @Override
    public void setCharAt(int x, int y, @NonNull Style style, int cp) {
        if (outside(x, y, 0)) {
            return;
        }
        line(y).setCharAt(applyXOffset(x), style, cp);
    }

    @Override
    public void setCharAt(int x, int y, @NonNull Style style, @NonNull CharSequence grapheme) {
        if (outside(x, y, 0)) {
            return;
        }
        line(y).setCharAt(applyXOffset(x), style, grapheme);
    }

    @Override
    public int putStringAt(int x, int y, @NonNull Style style, @NonNull CharSequence str) {
        if (outside(x, y, str.length())) {
            return str.length();
        }
        return line(y).putStringAt(applyXOffset(x), style, str);
    }

    @Override
    public int putStringAt(int x, int y, @NonNull StyledIterator iter) {
        return line(y).putStringAt(applyXOffset(x), iter);
    }

    @Override
    public void drawHLineAt(int x, int y, int x2, @NonNull Style style, char c) {
        for (int i = x; i < x2; i++) {
            setCharAt(i, y, style, c);
        }
    }

    @Override
    public void drawVLineAt(int x, int y, int y2, @NonNull Style style, char c) {
        for (int i = y; i < y2; i++) {
            setCharAt(x, i, style, c);
        }
    }

    @Override
    public void copyTo(Canvas canvas, int x, int y) {
        for (int i = 0; i < lines.length; i++) {
            for (int j = 0; j < lines[i].length(); j++) {
                canvas.setCharAt(x + j, y + i, styleAt(j, i), charAt(j, i));
            }
        }
    }

    @Override
    public @NonNull Buffer resize(@NonNull Size newSize) {
        if (newSize.equals(size())) {
            return this;
        }
        LineBuffer[] newLines = new LineBuffer[newSize.height()];
        for (int i = 0; i < newSize.height(); i++) {
            if (i < lines.length) {
                newLines[i] = lines[i].resize(newSize.width());
            } else {
                newLines[i] = createBuffer(newSize.width());
            }
        }
        lines = newLines;
        Rect r = rect();
        rect = Rect.of(r.left(), r.top(), newSize);
        return this;
    }

    private @NonNull LineBuffer createBuffer(int width) {
        return LineBuffer.of(width);
    }

    @Override
    public Buffer.@NonNull View view(@NonNull Rect viewRect) {
        return new BufferViewImpl(this, viewRect, lines);
    }

    private LineBuffer line(int y) {
        y = applyYOffset(y);
        return lines[y];
    }

    private int applyXOffset(int x) {
        return x + rect().left();
    }

    private int applyYOffset(int y) {
        return y + rect().top();
    }

    private boolean outside(int x, int y, int length) {
        int xAdjusted = applyXOffset(x);
        Rect r = rect();
        return (xAdjusted + length) < r.left() || xAdjusted > r.right() || invalidYOffset(y);
    }

    private boolean invalidYOffset(int y) {
        int yAdjusted = applyYOffset(y);
        Rect r = rect();
        return yAdjusted < r.top() || yAdjusted > r.bottom() || y < 0 || y >= lines.length;
    }

    @Override
    public String toString() {
        // Assuming only single-width characters for capacity estimation
        // plus one extra for newline
        int initialCapacity = (size().width() + 1) * size().height();
        StringBuilder sb = new StringBuilder(initialCapacity);
        for (int y = 0; y < size().height(); y++) {
            sb.append(line(y).toString());
            if (y < size().height() - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    @Override
    public @NonNull String toAnsiString(Style currentStyle) {
        // Assuming only single-width characters for capacity estimation
        // plus 20 extra for escape codes and newline
        int initialCapacity = (size().width() + 1) * size().height();
        StringBuilder sb = new StringBuilder(initialCapacity);
        try {
            return toAnsi(sb, currentStyle).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull Appendable toAnsi(Appendable appendable, Style currentStyle)
            throws IOException {
        if (currentStyle == Style.UNKNOWN) {
            currentStyle = Style.DEFAULT;
            appendable.append(Ansi.STYLE_RESET);
        }
        for (int y = 0; y < size().height(); y++) {
            line(y).toAnsi(appendable, currentStyle);
            currentStyle = line(y).styleAt(size().width() - 1);
            if (y < size().height() - 1) {
                appendable.append('\n');
            }
        }
        return appendable;
    }
}

class BufferViewImpl extends BufferImpl implements Buffer.View {
    protected final @NonNull BufferImpl parentPanel;

    protected BufferViewImpl(
            @NonNull BufferImpl parentPanel, @NonNull Rect rect, @NonNull LineBuffer[] lines) {
        super(rect, lines);
        this.parentPanel = parentPanel;
    }

    @Override
    protected @NonNull Rect rect() {
        Rect pr = parentPanel.rect();
        return Rect.of(
                this.rect.left() + pr.left(),
                this.rect.top() + pr.top(),
                Math.min(
                        this.rect.size().width(),
                        Math.max(0, pr.size().width() - this.rect.left())),
                Math.min(
                        this.rect.size().height(),
                        Math.max(0, pr.size().height() - this.rect.top())));
    }

    @Override
    public @NonNull Buffer resize(@NonNull Size newSize) {
        if (newSize.equals(size())) {
            return this;
        }
        Rect r = rect();
        rect = Rect.of(r.left(), r.top(), newSize);
        return this;
    }

    @Override
    public View moveTo(int x, int y) {
        Rect r = rect();
        rect = Rect.of(x, y, r.size());
        return this;
    }

    @Override
    public View moveBy(int dx, int dy) {
        Rect r = rect();
        rect = Rect.of(r.left() + dx, r.top() + dy, r.size());
        return this;
    }
}
