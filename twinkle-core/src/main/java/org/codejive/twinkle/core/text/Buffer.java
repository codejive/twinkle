package org.codejive.twinkle.core.text;

import java.io.IOException;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.core.util.Rect;
import org.codejive.twinkle.core.util.Size;
import org.codejive.twinkle.util.Printable;
import org.codejive.twinkle.util.StyledIterator;
import org.jspecify.annotations.NonNull;

public interface Buffer extends Canvas, Printable {

    char REPLACEMENT_CHAR = '\uFFFD';

    @NonNull Buffer resize(@NonNull Size newSize);

    @Override
    default @NonNull View view(int left, int top, int width, int height) {
        return view(Rect.of(left, top, width, height));
    }

    @Override
    @NonNull View view(@NonNull Rect rect);

    static @NonNull Buffer of(int width, int height) {
        return of(Size.of(width, height));
    }

    static @NonNull Buffer of(@NonNull Size size) {
        return new BufferImpl(size);
    }

    Buffer EMPTY =
            new BufferImpl(Size.of(0, 1)) {
                @Override
                public @NonNull Buffer resize(@NonNull Size newSize) {
                    if (!newSize.equals(Size.of(0, 1))) {
                        throw new UnsupportedOperationException("Cannot resize EMPTY");
                    }
                    return this;
                }
            };

    interface View extends Buffer {
        @NonNull View moveTo(int x, int y);

        @NonNull View moveBy(int dx, int dy);
    }
}

abstract class BufferBase implements Buffer {
    public abstract Rect rect();

    public abstract int putStringAt(@NonNull Rect rect, int x, int y, @NonNull StyledIterator iter);

    public abstract void copyTo(@NonNull Rect rect, @NonNull Canvas canvas, int x, int y);

    public abstract String toString(@NonNull Rect rect);

    public abstract @NonNull Appendable toAnsi(
            @NonNull Rect rect, @NonNull Appendable appendable, @NonNull Style currentStyle)
            throws IOException;

    protected Rect limitedRect(@NonNull Rect rect) {
        return this.rect().limited(rect.appliedTo(this.rect()));
    }
}

class BufferImpl extends BufferBase {
    private @NonNull Rect rect;
    private int[] cpBuffer;
    private String[] graphemeBuffer;
    private long[] styleBuffer;

    public BufferImpl(@NonNull Size size) {
        this.rect = Rect.of(0, 0, size);
        int totalSize = size.width() * size.height();
        this.cpBuffer = new int[totalSize];
        this.graphemeBuffer = new String[totalSize];
        this.styleBuffer = new long[totalSize];
    }

    private BufferImpl(
            @NonNull Size size, int[] cpBuffer, String[] graphemeBuffer, long[] styleBuffer) {
        this.rect = Rect.of(0, 0, size);
        this.cpBuffer = cpBuffer;
        this.graphemeBuffer = graphemeBuffer;
        this.styleBuffer = styleBuffer;
    }

    @Override
    public @NonNull Size size() {
        return rect.size();
    }

    @Override
    public @NonNull Rect rect() {
        return rect;
    }

    @Override
    public char charAt(int x, int y) {
        if (outside(x, y, 1)) {
            return REPLACEMENT_CHAR;
        }
        int index = index(x, y);
        if (graphemeBuffer[index] != null || Character.charCount(cpBuffer[index]) == 2) {
            return REPLACEMENT_CHAR;
        }
        return (char) cpBuffer[index];
    }

    @Override
    public int codepointAt(int x, int y) {
        if (outside(x, y, 1)) {
            return REPLACEMENT_CHAR;
        }
        return cpBuffer[index(x, y)];
    }

    @Override
    public @NonNull String graphemeAt(int x, int y) {
        if (outside(x, y, 1)) {
            return String.valueOf(REPLACEMENT_CHAR);
        }
        return graphemeAt_(index(x, y));
    }

    private @NonNull String graphemeAt_(int index) {
        if (graphemeBuffer[index] != null) {
            return graphemeBuffer[index];
        }
        return new String(Character.toChars(cpBuffer[index]));
    }

    public void graphemeAt(@NonNull Appendable appendable, int x, int y) throws IOException {
        if (outside(x, y, 1)) {
            appendable.append(REPLACEMENT_CHAR);
            return;
        }
        graphemeAt_(appendable, index(x, y));
    }

    public void graphemeAt_(@NonNull Appendable appendable, int index) throws IOException {
        if (graphemeBuffer[index] != null) {
            appendable.append(graphemeBuffer[index]);
        } else {
            int cp = cpBuffer[index];
            if (cp == '\0') {
                cp = ' ';
            }
            if (Character.isBmpCodePoint(cp)) {
                appendable.append((char) cp);
            } else if (Character.isValidCodePoint(cp)) {
                appendable.append(Character.highSurrogate(cp));
                appendable.append(Character.lowSurrogate(cp));
            } else {
                appendable.append(REPLACEMENT_CHAR);
            }
        }
    }

    @Override
    public @NonNull Style styleAt(int x, int y) {
        if (outside(x, y, 1)) {
            return Style.UNSTYLED;
        }
        return Style.of(styleBuffer[index(x, y)]);
    }

    @Override
    public void putCharAt(int x, int y, @NonNull Style style, char c) {
        if (outside(x, y, 1)) {
            return;
        }
        if (Character.isSurrogate(c)) {
            c = REPLACEMENT_CHAR;
        }
        setCharAt_(index(x, y), style.state(), c, null);
    }

    @Override
    public void putCharAt(int x, int y, @NonNull Style style, int cp) {
        if (outside(x, y, 1)) {
            return;
        }
        setCharAt_(index(x, y), style.state(), cp, null);
    }

    @Override
    public void putCharAt(int x, int y, @NonNull Style style, @NonNull CharSequence grapheme) {
        if (outside(x, y, 1)) {
            return;
        }
        if (grapheme.length() == 0) {
            return;
        }
        setCharAt_(index(x, y), style.state(), -1, grapheme.toString());
    }

    @Override
    public int putStringAt(int x, int y, @NonNull Style style, @NonNull CharSequence str) {
        if (outside(x, y, str.length())) {
            return str.length();
        }
        return putStringAt(x, y, StyledIterator.of(str, style));
    }

    @Override
    public int putStringAt(int x, int y, @NonNull StyledIterator iter) {
        return putStringAt(rect, x, y, iter);
    }

    @Override
    public int putStringAt(@NonNull Rect rect, int x, int y, @NonNull StyledIterator iter) {
        Rect limitedRect = limitedRect(rect);
        int startIndex = index(x, y);
        int maxLength = limitedRect.right() - x + 1;
        int cnt = 0;

        while (iter.hasNext()) {
            int cp = iter.next();
            if (cp == '\n') {
                break;
            }
            if (iter.width() == 0) {
                continue;
            }
            Style style = iter.style();
            if (iter.width() == 2 && (cnt + 1) >= maxLength) {
                setCharAt_(startIndex + cnt, style.state(), REPLACEMENT_CHAR, null);
                break;
            }
            if (cnt < maxLength) {
                if (iter.isComplex()) {
                    setCharAt_(startIndex + cnt, style.state(), -1, iter.sequence());
                } else {
                    setCharAt_(startIndex + cnt, style.state(), cp, null);
                }
            }
            cnt++;
            if (iter.width() == 2 && cnt < maxLength) {
                setSkipAt(startIndex + cnt);
                cnt++;
            }
        }
        return cnt;
    }

    @Override
    public void drawHLineAt(int x, int y, int x2, @NonNull Style style, char c) {
        for (int i = x; i < x2; i++) {
            putCharAt(i, y, style, c);
        }
    }

    @Override
    public void drawVLineAt(int x, int y, int y2, @NonNull Style style, char c) {
        for (int i = y; i < y2; i++) {
            putCharAt(x, i, style, c);
        }
    }

    @Override
    public void copyTo(@NonNull Canvas canvas, int x, int y) {
        copyTo(rect, canvas, x, y);
    }

    @Override
    public void copyTo(@NonNull Rect rect, @NonNull Canvas canvas, int x, int y) {
        Rect limitedRect = limitedRect(rect);
        for (int j = limitedRect.top(); j <= limitedRect.bottom(); j++) {
            for (int i = limitedRect.left(); i <= limitedRect.right(); i++) {
                int index = index(i, j);
                if (graphemeBuffer[index] == null) {
                    canvas.putCharAt(x + i, y + j, Style.of(styleBuffer[index]), cpBuffer[index]);
                } else {
                    canvas.putCharAt(
                            x + i, y + j, Style.of(styleBuffer[index]), graphemeBuffer[index]);
                }
            }
        }
    }

    private boolean shouldSkipAt(int index) {
        return cpBuffer[index] == -1 && graphemeBuffer[index] == null && styleBuffer[index] == -1;
    }

    private void setSkipAt(int index) {
        setCharAt_(index, -1, -1, null);
    }

    private void setCharAt_(int index, long styleState, int cp, String grapheme) {
        cpBuffer[index] = cp;
        graphemeBuffer[index] = grapheme;
        styleBuffer[index] = styleState;
    }

    @Override
    public @NonNull Buffer resize(@NonNull Size newSize) {
        if (newSize.equals(size())) {
            return this;
        }

        int newTotalSize = newSize.width() * newSize.height();
        int[] newCpBuffer = new int[newTotalSize];
        String[] newGraphemeBuffer = new String[newTotalSize];
        long[] newStyleBuffer = new long[newTotalSize];

        Size oldSize = size();
        int copyHeight = Math.min(newSize.height(), oldSize.height());
        int copyWidth = Math.min(newSize.width(), oldSize.width());

        for (int y = 0; y < copyHeight; y++) {
            int oldIndex = y * oldSize.width();
            int newIndex = y * newSize.width();
            System.arraycopy(cpBuffer, oldIndex, newCpBuffer, newIndex, copyWidth);
            System.arraycopy(graphemeBuffer, oldIndex, newGraphemeBuffer, newIndex, copyWidth);
            System.arraycopy(styleBuffer, oldIndex, newStyleBuffer, newIndex, copyWidth);
        }

        rect = Rect.of(0, 0, newSize);
        cpBuffer = newCpBuffer;
        graphemeBuffer = newGraphemeBuffer;
        styleBuffer = newStyleBuffer;

        return this;
    }

    @Override
    public Buffer.@NonNull View view(@NonNull Rect viewRect) {
        return new BufferViewImpl(this, viewRect);
    }

    private boolean outside(int x, int y, int length) {
        Size sz = size();
        return x + length <= 0 || x >= sz.width() || y < 0 || y >= sz.height();
    }

    private int index(int x, int y) {
        return y * rect.width() + x;
    }

    @Override
    public String toString() {
        return toString(rect);
    }

    public String toString(@NonNull Rect rect) {
        int initialCapacity = (size().width() + 1) * size().height();
        StringBuilder sb = new StringBuilder(initialCapacity);
        Rect limitedRect = limitedRect(rect);
        for (int y = limitedRect.top(); y <= limitedRect.bottom(); y++) {
            for (int x = limitedRect.left(); x <= limitedRect.right(); x++) {
                int index = index(x, y);
                if (shouldSkipAt(index)) {
                    continue;
                }
                String g = graphemeAt_(index);
                if (g.isEmpty() || g.charAt(0) == '\0') {
                    sb.append(' ');
                } else {
                    sb.append(g);
                }
            }
            if (y < limitedRect.bottom()) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    @Override
    public @NonNull String toAnsiString(Style currentStyle) {
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
        return toAnsi(rect, appendable, currentStyle);
    }

    public @NonNull Appendable toAnsi(
            @NonNull Rect rect, @NonNull Appendable appendable, @NonNull Style currentStyle)
            throws IOException {
        if (currentStyle == Style.UNKNOWN) {
            currentStyle = Style.DEFAULT;
            appendable.append(Ansi.STYLE_RESET);
        }
        Rect limitedRect = limitedRect(rect);
        for (int y = limitedRect.top(); y <= limitedRect.bottom(); y++) {
            for (int x = limitedRect.left(); x <= limitedRect.right(); x++) {
                int index = index(x, y);
                if (shouldSkipAt(index)) {
                    continue;
                }
                if (styleBuffer[index] != currentStyle.state()) {
                    Style style = Style.of(styleBuffer[index]);
                    style.toAnsi(appendable, currentStyle);
                    currentStyle = Style.of(styleBuffer[index]);
                }
                graphemeAt_(appendable, index);
            }
            if (y < limitedRect.bottom()) {
                appendable.append('\n');
            }
        }
        return appendable;
    }
}

class BufferViewImpl extends BufferBase implements Buffer.View {
    private final @NonNull BufferBase parent;
    private @NonNull Rect rect;

    protected BufferViewImpl(@NonNull BufferBase parent, @NonNull Rect rect) {
        this.parent = parent;
        this.rect = rect;
    }

    @Override
    public @NonNull Size size() {
        return rect.size();
    }

    @Override
    public @NonNull Rect rect() {
        return rect;
    }

    @Override
    public @NonNull Buffer resize(@NonNull Size newSize) {
        if (newSize.equals(size())) {
            return this;
        }
        rect = Rect.of(rect.left(), rect.top(), newSize);
        return this;
    }

    @Override
    public char charAt(int x, int y) {
        if (outside(x, y, 1)) {
            return REPLACEMENT_CHAR;
        }
        return parent.charAt(adjustX(x), adjustY(y));
    }

    @Override
    public int codepointAt(int x, int y) {
        if (outside(x, y, 1)) {
            return REPLACEMENT_CHAR;
        }
        return parent.codepointAt(adjustX(x), adjustY(y));
    }

    @Override
    public @NonNull String graphemeAt(int x, int y) {
        if (outside(x, y, 1)) {
            return String.valueOf(REPLACEMENT_CHAR);
        }
        return parent.graphemeAt(adjustX(x), adjustY(y));
    }

    @Override
    public @NonNull Style styleAt(int x, int y) {
        if (outside(x, y, 1)) {
            return Style.UNSTYLED;
        }
        return parent.styleAt(adjustX(x), adjustY(y));
    }

    @Override
    public void putCharAt(int x, int y, @NonNull Style style, char c) {
        if (outside(x, y, 1)) {
            return;
        }
        parent.putCharAt(adjustX(x), adjustY(y), style, c);
    }

    @Override
    public void putCharAt(int x, int y, @NonNull Style style, int cp) {
        if (outside(x, y, 1)) {
            return;
        }
        parent.putCharAt(adjustX(x), adjustY(y), style, cp);
    }

    @Override
    public void putCharAt(int x, int y, @NonNull Style style, @NonNull CharSequence grapheme) {
        if (outside(x, y, 1)) {
            return;
        }
        parent.putCharAt(adjustX(x), adjustY(y), style, grapheme);
    }

    @Override
    public int putStringAt(int x, int y, @NonNull Style style, @NonNull CharSequence str) {
        if (outside(x, y, str.length())) {
            return str.length();
        }
        return putStringAt(x, y, StyledIterator.of(str, style));
    }

    @Override
    public int putStringAt(int x, int y, @NonNull StyledIterator iter) {
        if (outside(x, y, 1)) {
            return 0;
        }
        return parent.putStringAt(rect, adjustX(x), adjustY(y), iter);
    }

    @Override
    public int putStringAt(@NonNull Rect rect, int x, int y, @NonNull StyledIterator iter) {
        if (outside(x, y, 1)) {
            return 0;
        }
        return parent.putStringAt(limitedRect(rect), adjustX(x), adjustY(y), iter);
    }

    @Override
    public void drawHLineAt(int x, int y, int x2, @NonNull Style style, char c) {
        int ax = adjustX(x);
        int ay = adjustY(y);
        int ax2 = adjustX(x2);
        parent.drawHLineAt(ax, ay, ax2, style, c);
    }

    @Override
    public void drawVLineAt(int x, int y, int y2, @NonNull Style style, char c) {
        int ax = adjustX(x);
        int ay = adjustY(y);
        int ay2 = adjustY(y2);
        parent.drawVLineAt(ax, ay, ay2, style, c);
    }

    @Override
    public void copyTo(@NonNull Canvas canvas, int x, int y) {
        parent.copyTo(rect, canvas, adjustX(x), adjustY(y));
    }

    @Override
    public void copyTo(@NonNull Rect rect, @NonNull Canvas canvas, int x, int y) {
        parent.copyTo(limitedRect(rect), canvas, adjustX(x), adjustY(y));
    }

    @Override
    public @NonNull View view(@NonNull Rect rect) {
        return new BufferViewImpl(this, rect);
    }

    @Override
    public @NonNull View moveTo(int x, int y) {
        rect = Rect.of(x, y, rect.size());
        return this;
    }

    @Override
    public @NonNull View moveBy(int dx, int dy) {
        rect = Rect.of(rect.left() + dx, rect.top() + dy, rect.size());
        return this;
    }

    @Override
    public String toString() {
        return parent.toString(rect);
    }

    @Override
    public String toString(@NonNull Rect rect) {
        return parent.toString(limitedRect(rect));
    }

    @Override
    public @NonNull Appendable toAnsi(Appendable appendable, Style currentStyle)
            throws IOException {
        return parent.toAnsi(rect, appendable, currentStyle);
    }

    @Override
    public @NonNull Appendable toAnsi(
            @NonNull Rect rect, @NonNull Appendable appendable, @NonNull Style currentStyle)
            throws IOException {
        return parent.toAnsi(limitedRect(rect), appendable, currentStyle);
    }

    private boolean outside(int x, int y, int length) {
        Size sz = size();
        return x + length <= 0 || x >= sz.width() || y < 0 || y >= sz.height();
    }

    private int adjustX(int x) {
        return rect.left() + x;
    }

    private int adjustY(int y) {
        return rect.top() + y;
    }
}
