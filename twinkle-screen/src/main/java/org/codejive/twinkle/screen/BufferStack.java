package org.codejive.twinkle.screen;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.codejive.twinkle.ansi.Style;
import org.codejive.twinkle.ansi.util.Printable;
import org.codejive.twinkle.text.Position;
import org.codejive.twinkle.text.Size;
import org.jspecify.annotations.NonNull;

public class BufferStack implements Printable {
    private Buffer primary;
    private final Set<BufferElement> bufferStack;
    private final Buffer combined;

    public static class BufferElement {
        public Buffer buffer;
        public Position pos;
        public int zIndex;
        public boolean visible;
        public String transparancy;

        public BufferElement(Buffer buffer, Position pos, int zIndex) {
            this.buffer = buffer;
            this.pos = pos;
            this.zIndex = zIndex;
            this.visible = true;
            this.transparancy = "\0";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            BufferElement that = (BufferElement) obj;
            return buffer.equals(that.buffer);
        }

        @Override
        public int hashCode() {
            return buffer.hashCode();
        }
    }

    public static BufferStack create() {
        return BufferStack.of((Buffer) null);
    }

    public static BufferStack of(Size size) {
        return BufferStack.of(Buffer.of(size));
    }

    public static BufferStack of(Buffer primary) {
        return new BufferStack(primary);
    }

    protected BufferStack(Buffer primary) {
        this.primary = primary;
        this.bufferStack = new LinkedHashSet<>();
        this.combined = Buffer.of(1, 1);
    }

    public Buffer primary() {
        return primary;
    }

    public void primary(Buffer primary) {
        this.primary = primary;
    }

    public List<BufferElement> list() {
        List<BufferElement> elems = list_();
        elems.add(0, new BufferElement(primary, Position.ZERO, Integer.MIN_VALUE));
        return elems;
    }

    private List<BufferElement> list_() {
        List<BufferElement> elems = new ArrayList<>();
        elems.addAll(bufferStack);
        elems.sort((o1, o2) -> o1.zIndex - o2.zIndex);
        return elems;
    }

    public BufferElement add(Buffer buffer) {
        BufferElement element = new BufferElement(buffer, Position.ZERO, bufferStack.size());
        bufferStack.add(element);
        return element;
    }

    public BufferElement add(Buffer buffer, Position pos, int zIndex) {
        BufferElement element = new BufferElement(buffer, pos, zIndex);
        bufferStack.add(element);
        return element;
    }

    public BufferElement add(Buffer buffer, int xPos, int yPos, int zIndex) {
        BufferElement element = new BufferElement(buffer, Position.of(xPos, yPos), zIndex);
        bufferStack.add(element);
        return element;
    }

    public BufferElement contains(Buffer element) {
        for (BufferElement bufferElement : bufferStack) {
            if (bufferElement.buffer.equals(element)) {
                return bufferElement;
            }
        }
        return null;
    }

    public void remove(BufferElement element) {
        bufferStack.remove(element);
    }

    public Buffer combined() {
        if (primary == null) {
            throw new IllegalStateException("Primary buffer not set");
        }
        if (bufferStack.isEmpty()) {
            return primary;
        }
        combined.resize(primary.size());
        primary.overlayOn(combined, 0, 0, "");
        for (BufferElement element : list_()) {
            if (element.visible) {
                element.buffer.overlayOn(
                        combined, element.pos.x(), element.pos.y(), element.transparancy);
            }
        }
        return combined;
    }

    @Override
    public @NonNull Appendable toAnsi(@NonNull Appendable appendable, @NonNull Style currentStyle) {
        return combined().toAnsi(appendable, currentStyle);
    }
}
