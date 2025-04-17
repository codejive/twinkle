package org.codejive.twinkle.core.component;

import static org.codejive.twinkle.core.text.StyledBuffer.REPLACEMENT_CHAR;

import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.text.StyledBuffer;
import org.codejive.twinkle.core.text.StyledCharSequence;
import org.jspecify.annotations.NonNull;

public class StyledBufferPanel implements Panel {
    protected @NonNull Rect rect;
    protected @NonNull StyledBuffer[] lines;

    public StyledBufferPanel(@NonNull Size size) {
        this.rect = Rect.of(size);
        this.lines = new StyledBuffer[size.height()];
        for (int i = 0; i < size.height(); i++) {
            lines[i] = createBuffer(size.width());
        }
    }

    protected StyledBufferPanel(@NonNull Rect rect, @NonNull StyledBuffer[] lines) {
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
    public long styleStateAt(int x, int y) {
        if (outside(x, y, 0)) {
            return Style.F_UNSTYLED;
        }
        return line(y).styleStateAt(applyXOffset(x));
    }

    @Override
    public void setCharAt(int x, int y, @NonNull Style style, char c) {
        setCharAt(x, y, style.state(), c);
    }

    @Override
    public void setCharAt(int x, int y, long styleState, char c) {
        if (outside(x, y, 0)) {
            return;
        }
        line(y).setCharAt(applyXOffset(x), styleState, c);
    }

    @Override
    public void setCharAt(int x, int y, @NonNull Style style, int cp) {
        setCharAt(x, y, style.state(), cp);
    }

    @Override
    public void setCharAt(int x, int y, long styleState, int cp) {
        if (outside(x, y, 0)) {
            return;
        }
        line(y).setCharAt(applyXOffset(x), styleState, cp);
    }

    @Override
    public void setCharAt(int x, int y, @NonNull Style style, @NonNull CharSequence grapheme) {
        setCharAt(x, y, style.state(), grapheme);
    }

    @Override
    public void setCharAt(int x, int y, long styleState, @NonNull CharSequence grapheme) {
        if (outside(x, y, 0)) {
            return;
        }
        line(y).setCharAt(applyXOffset(x), styleState, grapheme);
    }

    @Override
    public int putStringAt(int x, int y, @NonNull Style style, @NonNull CharSequence str) {
        return putStringAt(x, y, style.state(), str);
    }

    @Override
    public int putStringAt(int x, int y, long styleState, @NonNull CharSequence str) {
        if (outside(x, y, str.length())) {
            return str.length();
        }
        return line(y).putStringAt(applyXOffset(x), styleState, str);
    }

    @Override
    public int putStringAt(int x, int y, @NonNull StyledCharSequence str) {
        if (outside(x, y, str.length())) {
            return str.length();
        }
        return line(y).putStringAt(applyXOffset(x), str);
    }

    @Override
    public @NonNull Panel resize(@NonNull Size newSize) {
        if (newSize.equals(size())) {
            return this;
        }
        StyledBuffer[] newLines = new StyledBuffer[newSize.height()];
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

    private @NonNull StyledBuffer createBuffer(int width) {
        return StyledBuffer.of(width);
    }

    @Override
    public @NonNull PanelView view(@NonNull Rect viewRect) {
        return new StyledBufferPanelView(this, viewRect, lines);
    }

    private StyledBuffer line(int y) {
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
    public String toAnsiString() {
        // Assuming only single-width characters for capacity estimation
        // plus 20 extra for escape codes and newline
        int initialCapacity = (size().width() + 20) * size().height();
        StringBuilder sb = new StringBuilder(initialCapacity);
        sb.append(Ansi.STYLE_RESET);
        return toAnsiString(sb, Style.F_UNSTYLED).toString();
    }

    @Override
    public String toAnsiString(long currentStyleState) {
        // Assuming only single-width characters for capacity estimation
        // plus 20 extra for escape codes and newline
        int initialCapacity = (size().width() + 1) * size().height();
        StringBuilder sb = new StringBuilder(initialCapacity);
        return toAnsiString(sb, currentStyleState).toString();
    }

    private @NonNull StringBuilder toAnsiString(StringBuilder sb, long currentStyleState) {
        for (int y = 0; y < size().height(); y++) {
            sb.append(line(y).toAnsiString(currentStyleState));
            currentStyleState = line(y).styleStateAt(size().width() - 1);
            if (y < size().height() - 1) {
                sb.append('\n');
            }
        }
        return sb;
    }

    public static class StyledBufferPanelView extends StyledBufferPanel implements PanelView {
        protected final @NonNull StyledBufferPanel parentPanel;

        protected StyledBufferPanelView(
                @NonNull StyledBufferPanel parentPanel,
                @NonNull Rect rect,
                @NonNull StyledBuffer[] lines) {
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
        public @NonNull Panel resize(@NonNull Size newSize) {
            if (newSize.equals(size())) {
                return this;
            }
            Rect r = rect();
            rect = Rect.of(r.left(), r.top(), newSize);
            return this;
        }

        @Override
        public PanelView moveTo(int x, int y) {
            Rect r = rect();
            rect = Rect.of(x, y, r.size());
            return this;
        }

        @Override
        public PanelView moveBy(int dx, int dy) {
            Rect r = rect();
            rect = Rect.of(r.left() + dx, r.top() + dy, r.size());
            return this;
        }
    }
}
