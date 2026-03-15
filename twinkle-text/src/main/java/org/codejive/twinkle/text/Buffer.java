package org.codejive.twinkle.text;

import java.io.IOException;
import java.util.Objects;
import org.codejive.twinkle.ansi.Ansi;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.ansi.util.Printable;
import org.codejive.twinkle.text.io.BufferWriter;
import org.codejive.twinkle.text.io.PrintBufferWriter;
import org.codejive.twinkle.text.util.Hyperlink;
import org.codejive.twinkle.text.util.Rect;
import org.codejive.twinkle.text.util.Size;
import org.codejive.twinkle.text.util.StyledIterator;
import org.codejive.twinkle.text.util.Unicode;
import org.jspecify.annotations.NonNull;

public class Buffer implements Printable, RenderTarget {
    protected @NonNull Rect rect;
    protected InternalBuffers buffers;

    public static final char REPLACEMENT_CHAR = '\uFFFD';

    /**
     * Create a new Buffer with the specified width and height.
     *
     * @param width the width of the buffer
     * @param height the height of the buffer
     * @return a new Buffer instance with the specified dimensions
     */
    public static @NonNull Buffer of(int width, int height) {
        return of(Size.of(width, height));
    }

    /**
     * Create a new Buffer with the specified size.
     *
     * @param size the size of the buffer
     * @return a new Buffer instance with the specified dimensions
     */
    public static @NonNull Buffer of(@NonNull Size size) {
        return new Buffer(size);
    }

    protected static class InternalBuffers {
        public final int[][] cpBuffer;
        public final String[][] graphemeBuffer;
        public final long[][] styleBuffer;
        public final Hyperlink[][] linkBuffer;
        public final @NonNull Size size;

        public InternalBuffers(@NonNull Size size) {
            this(
                    new int[size.height()][size.width()],
                    new String[size.height()][size.width()],
                    new long[size.height()][size.width()],
                    new Hyperlink[size.height()][size.width()]);
        }

        public InternalBuffers(
                int[][] cpBuffer,
                String[][] graphemeBuffer,
                long[][] styleBuffer,
                Hyperlink[][] linkBuffer) {
            this.cpBuffer = cpBuffer;
            this.graphemeBuffer = graphemeBuffer;
            this.styleBuffer = styleBuffer;
            this.linkBuffer = linkBuffer;
            this.size = Size.of(styleBuffer[0].length, styleBuffer.length);
        }

        public void copyTo(
                @NonNull InternalBuffers targetBuffers,
                @NonNull Rect sourceRect,
                int targetX,
                int targetY,
                String transparantCharacters) {
            // Calculate the actual region to copy, bounded by both source and target dimensions
            int sourceLeft = Math.max(0, sourceRect.left());
            int sourceTop = Math.max(0, sourceRect.top());
            int sourceRight = Math.min(size.width(), sourceRect.left() + sourceRect.width());
            int sourceBottom = Math.min(size.height(), sourceRect.top() + sourceRect.height());

            int copyWidth = sourceRight - sourceLeft;
            int copyHeight = sourceBottom - sourceTop;

            // Handle negative target positions by adjusting source and target coordinates
            if (targetX < 0) {
                int offset = -targetX;
                sourceLeft += offset;
                copyWidth -= offset;
                targetX = 0;
            }
            if (targetY < 0) {
                int offset = -targetY;
                sourceTop += offset;
                copyHeight -= offset;
                targetY = 0;
            }

            // Adjust copy dimensions if target position would go out of bounds
            copyWidth = Math.min(copyWidth, targetBuffers.size.width() - targetX);
            copyHeight = Math.min(copyHeight, targetBuffers.size.height() - targetY);

            // Only proceed if there's actually something to copy
            if (copyWidth <= 0 || copyHeight <= 0) {
                return;
            }

            if (transparantCharacters == null) {
                copyData(
                        targetBuffers,
                        targetX,
                        targetY,
                        sourceLeft,
                        sourceTop,
                        copyWidth,
                        copyHeight);
            } else {
                overlayData(
                        targetBuffers,
                        targetX,
                        targetY,
                        sourceLeft,
                        sourceTop,
                        copyWidth,
                        copyHeight,
                        transparantCharacters);
            }
        }

        private void copyData(
                InternalBuffers targetBuffers,
                int targetX,
                int targetY,
                int sourceLeft,
                int sourceTop,
                int copyWidth,
                int copyHeight) {
            // Copy the data
            for (int y = 0; y < copyHeight; y++) {
                int sourceY = sourceTop + y;
                int targetYPos = targetY + y;
                System.arraycopy(
                        cpBuffer[sourceY],
                        sourceLeft,
                        targetBuffers.cpBuffer[targetYPos],
                        targetX,
                        copyWidth);
                System.arraycopy(
                        graphemeBuffer[sourceY],
                        sourceLeft,
                        targetBuffers.graphemeBuffer[targetYPos],
                        targetX,
                        copyWidth);
                System.arraycopy(
                        styleBuffer[sourceY],
                        sourceLeft,
                        targetBuffers.styleBuffer[targetYPos],
                        targetX,
                        copyWidth);
                System.arraycopy(
                        linkBuffer[sourceY],
                        sourceLeft,
                        targetBuffers.linkBuffer[targetYPos],
                        targetX,
                        copyWidth);
            }
        }

        private void overlayData(
                InternalBuffers targetBuffers,
                int targetX,
                int targetY,
                int sourceLeft,
                int sourceTop,
                int copyWidth,
                int copyHeight,
                String transparantCharacters) {
            // Copy the data, skipping transparent characters
            for (int y = 0; y < copyHeight; y++) {
                int sourceY = sourceTop + y;
                int targetYPos = targetY + y;
                for (int x = 0; x < copyWidth; x++) {
                    int sourceX = sourceLeft + x;
                    int targetXPos = targetX + x;

                    // Only copy if not transparent
                    int codepoint = cpBuffer[sourceY][sourceX];
                    if (transparantCharacters.indexOf(codepoint) < 0) {
                        targetBuffers.cpBuffer[targetYPos][targetXPos] = cpBuffer[sourceY][sourceX];
                        targetBuffers.graphemeBuffer[targetYPos][targetXPos] =
                                graphemeBuffer[sourceY][sourceX];
                        targetBuffers.styleBuffer[targetYPos][targetXPos] =
                                styleBuffer[sourceY][sourceX];
                        targetBuffers.linkBuffer[targetYPos][targetXPos] =
                                linkBuffer[sourceY][sourceX];
                    }
                }
            }
        }

        public @NonNull InternalBuffers resize(@NonNull Size newSize) {
            if (newSize.equals(size)) {
                return this;
            }

            int newWidth = newSize.width();
            int newHeight = newSize.height();
            int[][] newCpBuffer = new int[newHeight][newWidth];
            String[][] newGraphemeBuffer = new String[newHeight][newWidth];
            long[][] newStyleBuffer = new long[newHeight][newWidth];
            Hyperlink[][] newLinkBuffer = new Hyperlink[newHeight][newWidth];

            InternalBuffers newBuffers =
                    new InternalBuffers(
                            newCpBuffer, newGraphemeBuffer, newStyleBuffer, newLinkBuffer);
            copyTo(newBuffers, Rect.of(newWidth, newHeight), 0, 0, null);

            return newBuffers;
        }
    }

    protected Buffer(@NonNull Size size) {
        this.rect = Rect.of(0, 0, size);
        this.buffers = new InternalBuffers(size);
    }

    /**
     * Get a Writer for this Buffer that can be used to write content to the buffer. The writer will
     * interpret ANSI escape sequences and update the buffer content and styles accordingly.
     *
     * @return a BufferWriter instance for this buffer
     */
    public @NonNull PrintBufferWriter writer() {
        return new PrintBufferWriter(new BufferWriter(this));
    }

    /**
     * Get the size of this Buffer.
     *
     * @return
     */
    @Override
    public @NonNull Size size() {
        return rect.size();
    }

    protected @NonNull Rect rect() {
        return rect;
    }

    protected Rect limitedRect(@NonNull Rect rect) {
        return this.rect().limited(rect.appliedTo(this.rect()));
    }

    /**
     * Get the character at the specified position in the buffer. If the position is out of bounds
     * or the value at the position can not be represented as a single char, a replacement character
     * will be returned.
     *
     * @param x the x-coordinate of the character
     * @param y the y-coordinate of the character
     * @return the character at the specified position, or a replacement character
     */
    public char charAt(int x, int y) {
        if (outside(x, y)) {
            return REPLACEMENT_CHAR;
        }
        if (shouldSkipAt(x, y)) {
            return charAt(x - 1, y);
        }
        if (buffers.graphemeBuffer[y][x] != null
                || Character.charCount(buffers.cpBuffer[y][x]) == 2) {
            return REPLACEMENT_CHAR;
        }
        return (char) buffers.cpBuffer[y][x];
    }

    /**
     * Get the codepoint at the specified position in the buffer. If the position is out of bounds
     * or the value at the position is not a valid codepoint, a replacement character will be
     * returned.
     *
     * @param x the x-coordinate of the codepoint
     * @param y the y-coordinate of the codepoint
     * @return the codepoint at the specified position, or a replacement character
     */
    public int codepointAt(int x, int y) {
        if (outside(x, y)) {
            return REPLACEMENT_CHAR;
        }
        if (shouldSkipAt(x, y)) {
            return codepointAt(x - 1, y);
        }
        return buffers.cpBuffer[y][x];
    }

    /**
     * Get the grapheme at the specified position in the buffer. If the position is out of bounds a
     * replacement character will be returned.
     *
     * @param x the x-coordinate of the grapheme
     * @param y the y-coordinate of the grapheme
     * @return the grapheme at the specified position, or a replacement character
     */
    public @NonNull String graphemeAt(int x, int y) {
        if (outside(x, y)) {
            return String.valueOf(REPLACEMENT_CHAR);
        }
        return graphemeAt_(x, y);
    }

    private @NonNull String graphemeAt_(int x, int y) {
        if (shouldSkipAt(x, y)) {
            return graphemeAt_(x - 1, y);
        }
        if (buffers.graphemeBuffer[y][x] != null) {
            return buffers.graphemeBuffer[y][x];
        }
        return new String(Character.toChars(buffers.cpBuffer[y][x]));
    }

    /**
     * Get the grapheme at the specified position in the buffer and append it to the provided
     * Appendable. If the position is out of bounds, a replacement character will be appended.
     *
     * @param appendable the Appendable to which the grapheme will be appended
     * @param x the x-coordinate of the grapheme
     * @param y the y-coordinate of the grapheme
     */
    public void graphemeAt(@NonNull Appendable appendable, int x, int y) {
        if (outside(x, y)) {
            appendChr(appendable, REPLACEMENT_CHAR);
            return;
        }
        graphemeAt_(appendable, x, y);
    }

    private void graphemeAt_(@NonNull Appendable appendable, int x, int y) {
        if (shouldSkipAt(x, y)) {
            graphemeAt_(appendable, x - 1, y);
        } else if (buffers.graphemeBuffer[y][x] != null) {
            appendStr(appendable, buffers.graphemeBuffer[y][x]);
        } else {
            int cp = buffers.cpBuffer[y][x];
            if (cp == '\0') {
                cp = ' ';
            }
            if (Character.isBmpCodePoint(cp)) {
                appendChr(appendable, (char) cp);
            } else if (Character.isValidCodePoint(cp)) {
                appendChr(appendable, Character.highSurrogate(cp));
                appendChr(appendable, Character.lowSurrogate(cp));
            } else {
                appendChr(appendable, REPLACEMENT_CHAR);
            }
        }
    }

    public @NonNull Style styleAt(int x, int y) {
        if (outside(x, y)) {
            return Style.UNSTYLED;
        }
        return Style.of(buffers.styleBuffer[y][x]);
    }

    @Override
    public void putAt(int x, int y, char c, PrintOption... options) {
        if (outside(x, y)) {
            return;
        }
        if (Character.isSurrogate(c)) {
            c = REPLACEMENT_CHAR;
        }
        Style style = opt(options, StylePrintOption.class, StylePrintOption.UNSTYLED).style();
        Hyperlink link = opt(options, LinkPrintOption.class, LinkPrintOption.NONE).hyperlink();
        setCharAt_(x, y, style.state(), c, null, link);
    }

    @Override
    public void putAt(int x, int y, int cp, PrintOption... options) {
        if (outside(x, y)) {
            return;
        }
        Style style = opt(options, StylePrintOption.class, StylePrintOption.UNSTYLED).style();
        Hyperlink link = opt(options, LinkPrintOption.class, LinkPrintOption.NONE).hyperlink();
        setCharAt_(x, y, style.state(), cp, null, link);
    }

    @Override
    public void putAt(int x, int y, @NonNull CharSequence grapheme, PrintOption... options) {
        if (outside(x, y)) {
            return;
        }
        if (grapheme.length() == 0) {
            return;
        }
        Style style = opt(options, StylePrintOption.class, StylePrintOption.UNSTYLED).style();
        Hyperlink link = opt(options, LinkPrintOption.class, LinkPrintOption.NONE).hyperlink();
        setCharAt_(x, y, style.state(), -1, grapheme.toString(), link);
    }

    public enum SimplePrintOption implements PrintOption {
        NOWRAP
    }

    public static class StylePrintOption implements PrintOption {
        private final Style style;

        public static final StylePrintOption DEFAULT = new StylePrintOption(Style.DEFAULT);
        public static final StylePrintOption UNSTYLED = new StylePrintOption(Style.UNSTYLED);

        public StylePrintOption(Style style) {
            this.style = style;
        }

        public Style style() {
            return style;
        }
    }

    public static @NonNull StylePrintOption styleOpt(Style style) {
        return new StylePrintOption(style);
    }

    public static class LinkPrintOption implements PrintOption {
        private final Hyperlink hyperlink;

        public static final LinkPrintOption NONE = new LinkPrintOption(null);

        public LinkPrintOption(@NonNull String url, String id) {
            this.hyperlink = Hyperlink.of(url, id);
        }

        public LinkPrintOption(Hyperlink hyperlink) {
            this.hyperlink = hyperlink;
        }

        public String url() {
            return hyperlink != null ? hyperlink.url : null;
        }

        public String id() {
            return hyperlink != null ? hyperlink.id : null;
        }

        public Hyperlink hyperlink() {
            return hyperlink;
        }
    }

    public static @NonNull LinkPrintOption linkOpt(@NonNull String url) {
        return new LinkPrintOption(url, null);
    }

    public static @NonNull LinkPrintOption linkOpt(@NonNull String url, String id) {
        return new LinkPrintOption(url, id);
    }

    public static @NonNull LinkPrintOption linkOpt(@NonNull Hyperlink hyperlink) {
        return new LinkPrintOption(hyperlink);
    }

    public static class TransparencyPrintOption implements PrintOption {
        private final String transparentCharacters;

        public static final TransparencyPrintOption NONE = new TransparencyPrintOption("");

        public static final TransparencyPrintOption NUL = new TransparencyPrintOption("\0");

        public static final TransparencyPrintOption NUL_AND_SPACE =
                new TransparencyPrintOption("\0 ");

        public static final TransparencyPrintOption DEFAULT = NUL;

        public TransparencyPrintOption(String transparentCharacters) {
            this.transparentCharacters = transparentCharacters;
        }

        public String chars() {
            return transparentCharacters;
        }
    }

    public static TransparencyPrintOption transparencyOpt(String transparentCharacters) {
        return new TransparencyPrintOption(transparentCharacters);
    }

    @Override
    public void printAt(int x, int y, @NonNull CharSequence str, PrintOption... options) {
        if (outside(x, y, str.length())) {
            return;
        }
        Style style = opt(options, StylePrintOption.class, StylePrintOption.UNSTYLED).style();
        printAt(x, y, StyledIterator.of(str, style), options);
    }

    @Override
    public void printAt(int x, int y, @NonNull StyledIterator iter, PrintOption... options) {
        int curX = x;
        int curY = y;

        boolean shouldWrap = opt(options, SimplePrintOption.NOWRAP.getClass(), null) == null;
        String transparency =
                opt(options, TransparencyPrintOption.class, TransparencyPrintOption.NONE).chars();

        while (iter.hasNext()) {
            int cp = iter.next();
            if (cp == '\n' || (curX > rect.right() && shouldWrap)) {
                curX = 0;
                curY++;
                if (curY > rect.bottom()) {
                    break;
                }
                if (cp == '\n') {
                    continue;
                }
            }
            if (iter.width() == 0) {
                continue;
            }
            Style style = iter.style();
            Hyperlink link = iter.link();
            int width = iter.width();
            if (curX <= rect.right()) {
                if (iter.isComplex()) {
                    setCharAt_(curX, curY, style.state(), -1, iter.sequence(), link);
                } else {
                    if (transparency.indexOf(cp) >= 0) {
                        width = 1;
                    } else {
                        setCharAt_(curX, curY, style.state(), cp, null, link);
                    }
                }
            }
            curX += width;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends PrintOption> T opt(
            PrintOption[] options, Class<T> optionClass, T defaultOpt) {
        for (PrintOption opt : options) {
            if (optionClass.isInstance(opt)) {
                return (T) opt;
            }
        }
        return defaultOpt;
    }

    /**
     * Determine if the cell at the specified position should be skipped. A cell should be skipped
     * if it is part of a wide character that spans multiple cells.
     *
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     * @return true if the cell should be skipped, false otherwise
     */
    public boolean shouldSkipAt(int x, int y) {
        return buffers.cpBuffer[y][x] == -1
                && buffers.graphemeBuffer[y][x] == null
                && buffers.styleBuffer[y][x] == -1;
    }

    private void setSkipAt(int x, int y) {
        if (outside(x, y)) {
            return;
        }
        boolean isWide = (isWideAt(x, y));
        // Set skip state to indicate cell is occupied by wide character from previous cell
        setCellAt(x, y, -1, -1, null, null);
        if (isWide) {
            // Clear the next cell's skip state if this cell contained a wide character
            clearAt(x + 1, y);
        }
    }

    @Override
    public void clearAt(int x, int y) {
        if (outside(x, y)) {
            return;
        }
        boolean isWide = (isWideAt(x, y));
        clearAt_(x, y);
        if (isWide) {
            // Clear the next cell over if this cell contains a wide character
            clearAt(x + 1, y);
        }
    }

    private void clearAt_(int x, int y) {
        setCellAt(x, y, Style.F_UNSTYLED, '\0', null, null);
    }

    /**
     * Determine if the character at the specified position in the buffer is a wide character. A
     * wide character is a character that occupies two cells in the buffer. If the position is out
     * of bounds, false is returned.
     *
     * @param x the x-coordinate of the character
     * @param y the y-coordinate of the character
     * @return true if the character is wide, false otherwise
     */
    public boolean isWideAt(int x, int y) {
        if (outside(x, y)) {
            return false;
        }
        int cp = buffers.cpBuffer[y][x];
        String grapheme = buffers.graphemeBuffer[y][x];
        boolean isWide = (grapheme != null) ? Unicode.isWide(grapheme) : Unicode.isWide(cp);
        return isWide;
    }

    private void setCharAt_(
            int x, int y, long styleState, int cp, String grapheme, Hyperlink link) {
        // Handle wide character overlap to the left of this cell
        if (shouldSkipAt(x, y)) {
            // The previous cell contains a wide character that overlaps this cell
            clearAt(x - 1, y);
        }

        setCellAt(x, y, styleState, cp, grapheme, link);

        // Set a skip cell to the right if this is a wide character
        boolean isWide = (grapheme != null) ? Unicode.isWide(grapheme) : Unicode.isWide(cp);
        if (isWide) {
            setSkipAt(x + 1, y);
        }
    }

    private void setCellAt(int x, int y, long styleState, int cp, String grapheme, Hyperlink link) {
        buffers.cpBuffer[y][x] = cp;
        buffers.graphemeBuffer[y][x] = grapheme;
        buffers.styleBuffer[y][x] = styleState;
        buffers.linkBuffer[y][x] = link;
    }

    /**
     * Clear the entire buffer, setting all cells to the default state.
     *
     * @return a reference to this Buffer, for chaining
     */
    public @NonNull Buffer clear() {
        for (int y = 0; y < rect.height(); y++) {
            for (int x = 0; x < rect.width(); x++) {
                clearAt_(x, y);
            }
        }
        return this;
    }

    /**
     * Clear the area of the buffer defined by the starting position (fromX, fromY) and the ending
     * position (toX, toY). And all FULL lines in between.
     *
     * @return a reference to this Buffer, for chaining
     */
    public @NonNull Buffer clear(int fromX, int fromY, int toX, int toY) {
        for (int x = fromX; x < rect.width(); x++) {
            // Using clearAt instead of clearAt_ to handle wide character overlap
            clearAt(x, fromY);
        }
        for (int y = fromY + 1; y < toY; y++) {
            for (int x = 0; x < rect.width(); x++) {
                // Can use clearAt_() here because we're clearing full lines
                clearAt_(x, y);
            }
        }
        for (int x = 0; x <= toX; x++) {
            // Using clearAt instead of clearAt_ to handle wide character overlap
            clearAt(x, toY);
        }
        return this;
    }

    /**
     * Resize the buffer to the new size. When resizing to a larger size, the new area will be
     * filled with the default state. When resizing to a smaller size, the content will be truncated
     * to fit the new size.
     *
     * @param newSize the new size of the buffer
     * @return a reference to this Buffer, for chaining
     */
    public @NonNull Buffer resize(@NonNull Size newSize) {
        buffers = buffers.resize(newSize);
        rect = Rect.of(newSize);
        return this;
    }

    /**
     * Overlay the content of this buffer onto another target buffer at the specified position. The
     * content of this buffer will be copied to the target buffer, starting at the coordinates
     * (targetX, targetY) in the target buffer. If any part of this buffer extends beyond the bounds
     * of the target buffer, the overlapping content will be truncated. Null ('\0') characters in
     * this buffer will be treated as transparent and will not overwrite the corresponding cells in
     * the target buffer.
     *
     * @param targetBuffer the buffer to overlay onto
     * @param targetX the x-coordinate on the target buffer
     * @param targetY the y-coordinate on the target buffer
     * @return a reference to this Buffer, for chaining
     */
    public @NonNull Buffer overlayOn(@NonNull Buffer targetBuffer, int targetX, int targetY) {
        buffers.copyTo(targetBuffer.buffers, rect, targetX, targetY, "\0");
        return this;
    }

    /**
     * Overlay the content of this buffer onto another target buffer at the specified position. The
     * content of this buffer will be copied to the target buffer, starting at the coordinates
     * (targetX, targetY) in the target buffer. If any part of this buffer extends beyond the bounds
     * of the target buffer, the overlapping content will be truncated. Characters in this buffer
     * that are present in the transparantCharacters string will be treated as transparent and will
     * not overwrite the corresponding cells in the target buffer.
     *
     * @param targetBuffer the buffer to overlay onto
     * @param targetX the x-coordinate on the target buffer
     * @param targetY the y-coordinate on the target buffer
     * @param transparantCharacters the characters to be treated as transparent
     * @return a reference to this Buffer, for chaining
     */
    public @NonNull Buffer overlayOn(
            @NonNull Buffer targetBuffer, int targetX, int targetY, String transparantCharacters) {
        buffers.copyTo(targetBuffer.buffers, rect, targetX, targetY, transparantCharacters);
        return this;
    }

    /**
     * Overlay the content of this buffer onto another target buffer at the specified position. The
     * content of this buffer will be copied to the target buffer, starting at the coordinates
     * (targetX, targetY) in the target buffer. If any part of this buffer extends beyond the bounds
     * of the target buffer, the overlapping content will be truncated. The sourceRect parameter
     * defines the region of this buffer to copy. Characters in this buffer that are present in the
     * transparantCharacters string will be treated as transparent and will not overwrite the
     * corresponding cells in the target buffer.
     *
     * @param targetBuffer the buffer to overlay onto
     * @param sourceRect the area of this buffer to copy
     * @param targetX the x-coordinate on the target buffer
     * @param targetY the y-coordinate on the target buffer
     * @param transparantCharacters the characters to be treated as transparent
     * @return a reference to this Buffer, for chaining
     */
    public @NonNull Buffer overlayOn(
            @NonNull Buffer targetBuffer,
            Rect sourceRect,
            int targetX,
            int targetY,
            String transparantCharacters) {
        buffers.copyTo(targetBuffer.buffers, sourceRect, targetX, targetY, transparantCharacters);
        return this;
    }

    private boolean outside(int x, int y) {
        return outside(x, y, 1);
    }

    private boolean outside(int x, int y, int length) {
        Size sz = size();
        return x + length <= 0 || x >= sz.width() || y < 0 || y >= sz.height();
    }

    @Override
    public String toString() {
        return toString(rect);
    }

    /**
     * Get a string representation of the content of the buffer within the specified rectangle. The
     * output will contain only plain text without any style information or links.
     *
     * @param rect the rectangle defining the area of the buffer to convert to a string
     * @return a string representation of the content within the specified rectangle
     */
    public String toString(@NonNull Rect rect) {
        int initialCapacity = (size().width() + 1) * size().height();
        StringBuilder sb = new StringBuilder(initialCapacity);
        Rect limitedRect = limitedRect(rect);
        for (int y = limitedRect.top(); y <= limitedRect.bottom(); y++) {
            for (int x = limitedRect.left(); x <= limitedRect.right(); x++) {
                if (shouldSkipAt(x, y)) {
                    continue;
                }
                String g = graphemeAt_(x, y);
                if (g.isEmpty() || g.charAt(0) == '\0') {
                    sb.append(' ');
                } else if (x == limitedRect.right() && isWideAt(x, y)) {
                    // Don't attempt to render a wide character if it would overflow the right
                    // edge of the buffer, as the wide character will be truncated and not
                    // display correctly.
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
    public @NonNull String toAnsi(@NonNull Style currentStyle) {
        int initialCapacity = (size().width() + 1) * size().height();
        StringBuilder sb = new StringBuilder(initialCapacity);
        return toAnsi(sb, currentStyle).toString();
    }

    @Override
    public @NonNull Appendable toAnsi(@NonNull Appendable appendable, @NonNull Style currentStyle) {
        return toAnsi(rect, appendable, currentStyle);
    }

    public @NonNull Appendable toAnsi(
            @NonNull Rect rect, @NonNull Appendable appendable, @NonNull Style currentStyle) {
        Hyperlink currentLink = null;
        if (currentStyle == Style.UNKNOWN) {
            currentStyle = Style.DEFAULT;
            appendStr(appendable, Ansi.STYLE_RESET);
        }
        Rect limitedRect = limitedRect(rect);
        for (int y = limitedRect.top(); y <= limitedRect.bottom(); y++) {
            for (int x = limitedRect.left(); x <= limitedRect.right(); x++) {
                if (shouldSkipAt(x, y)) {
                    continue;
                }
                if (buffers.styleBuffer[y][x] != currentStyle.state()) {
                    Style style = Style.of(buffers.styleBuffer[y][x]);
                    style.toAnsi(appendable, currentStyle);
                    currentStyle = Style.of(buffers.styleBuffer[y][x]);
                }
                if (!Objects.equals(buffers.linkBuffer[y][x], currentLink)) {
                    Hyperlink link = buffers.linkBuffer[y][x];
                    if (currentLink != null) {
                        // Emit the end of the current hyperlink
                        Hyperlink.END.toAnsi(appendable);
                    }
                    if (link != null) {
                        link.toAnsi(appendable);
                    }
                    currentLink = link;
                }
                if (x == limitedRect.right() && isWideAt(x, y)) {
                    // Don't attempt to render a wide character if it would overflow the right
                    // edge of the buffer, as the wide character will be truncated and not
                    // display correctly.
                    appendChr(appendable, ' ');
                } else {
                    graphemeAt_(appendable, x, y);
                }
            }
            if (y < limitedRect.bottom()) {
                appendChr(appendable, '\n');
            }
        }
        return appendable;
    }

    private void appendStr(Appendable appendable, String str) {
        try {
            appendable.append(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void appendChr(Appendable appendable, char ch) {
        try {
            appendable.append(ch);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
