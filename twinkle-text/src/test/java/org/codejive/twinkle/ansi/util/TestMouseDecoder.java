package org.codejive.twinkle.ansi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codejive.twinkle.ansi.Constants.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TestMouseDecoder {

    /** Test helper class to capture mouse events */
    private static class TestMouseEvent implements MouseEvent {
        static class Event {
            final String type;
            final int button;
            final int x;
            final int y;
            final boolean shift;
            final boolean alt;
            final boolean ctrl;

            Event(String type, int button, int x, int y, boolean shift, boolean alt, boolean ctrl) {
                this.type = type;
                this.button = button;
                this.x = x;
                this.y = y;
                this.shift = shift;
                this.alt = alt;
                this.ctrl = ctrl;
            }
        }

        List<Event> events = new ArrayList<>();

        @Override
        public void onButtonPress(
                int button, int x, int y, boolean shift, boolean alt, boolean ctrl) {
            events.add(new Event("press", button, x, y, shift, alt, ctrl));
        }

        @Override
        public void onButtonRelease(
                int button, int x, int y, boolean shift, boolean alt, boolean ctrl) {
            events.add(new Event("release", button, x, y, shift, alt, ctrl));
        }

        @Override
        public void onDrag(int button, int x, int y, boolean shift, boolean alt, boolean ctrl) {
            events.add(new Event("drag", button, x, y, shift, alt, ctrl));
        }

        @Override
        public void onScroll(
                int direction, int x, int y, boolean shift, boolean alt, boolean ctrl) {
            events.add(new Event("scroll", direction, x, y, shift, alt, ctrl));
        }

        @Override
        public void onMove(int x, int y, boolean shift, boolean alt, boolean ctrl) {
            events.add(new Event("move", -1, x, y, shift, alt, ctrl));
        }

        Event last() {
            return events.isEmpty() ? null : events.get(events.size() - 1);
        }
    }

    @Test
    public void testSgrLeftButtonPress() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // ESC[<0;10;20M - Left button press at (9, 19) in 0-based coords
        boolean result = decoder.accept("\u001B[<0;10;20M");

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("press");
        assertThat(event.button).isEqualTo(MOUSE_BUTTON_LEFT);
        assertThat(event.x).isEqualTo(9);
        assertThat(event.y).isEqualTo(19);
        assertThat(event.shift).isFalse();
        assertThat(event.alt).isFalse();
        assertThat(event.ctrl).isFalse();
    }

    @Test
    public void testSgrRightButtonPress() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // ESC[<2;5;10M - Right button press at (4, 9)
        boolean result = decoder.accept("\u001B[<2;5;10M");

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("press");
        assertThat(event.button).isEqualTo(MOUSE_BUTTON_RIGHT);
        assertThat(event.x).isEqualTo(4);
        assertThat(event.y).isEqualTo(9);
    }

    @Test
    public void testSgrMiddleButtonPress() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // ESC[<1;15;25M - Middle button press at (14, 24)
        boolean result = decoder.accept("\u001B[<1;15;25M");

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("press");
        assertThat(event.button).isEqualTo(MOUSE_BUTTON_MIDDLE);
        assertThat(event.x).isEqualTo(14);
        assertThat(event.y).isEqualTo(24);
    }

    @Test
    public void testSgrButtonRelease() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // ESC[<0;10;20m - Button release (lowercase 'm')
        boolean result = decoder.accept("\u001B[<0;10;20m");

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("release");
        assertThat(event.button).isEqualTo(0);
        assertThat(event.x).isEqualTo(9);
        assertThat(event.y).isEqualTo(19);
    }

    @Test
    public void testSgrDragEvent() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // ESC[<32;10;20M - Drag with left button (0 + 32 drag flag)
        boolean result = decoder.accept("\u001B[<32;10;20M");

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("drag");
        assertThat(event.button).isEqualTo(MOUSE_BUTTON_LEFT);
        assertThat(event.x).isEqualTo(9);
        assertThat(event.y).isEqualTo(19);
    }

    @Test
    public void testSgrMoveEvent() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // ESC[<35;10;20M - Move without buttons (35 = 32 drag flag + 3 for move)
        boolean result = decoder.accept("\u001B[<35;10;20M");

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("move");
        assertThat(event.x).isEqualTo(9);
        assertThat(event.y).isEqualTo(19);
    }

    @Test
    public void testSgrScrollUp() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // ESC[<64;10;20M - Scroll up
        boolean result = decoder.accept("\u001B[<64;10;20M");

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("scroll");
        assertThat(event.button).isEqualTo(MOUSE_SCROLL_UP);
        assertThat(event.x).isEqualTo(9);
        assertThat(event.y).isEqualTo(19);
    }

    @Test
    public void testSgrScrollDown() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // ESC[<65;10;20M - Scroll down
        boolean result = decoder.accept("\u001B[<65;10;20M");

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("scroll");
        assertThat(event.button).isEqualTo(MOUSE_SCROLL_DOWN);
    }

    @Test
    public void testSgrWithShiftModifier() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // ESC[<4;10;20M - Left button with Shift (0 + 4 shift flag)
        boolean result = decoder.accept("\u001B[<4;10;20M");

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("press");
        assertThat(event.button).isEqualTo(MOUSE_BUTTON_LEFT);
        assertThat(event.shift).isTrue();
        assertThat(event.alt).isFalse();
        assertThat(event.ctrl).isFalse();
    }

    @Test
    public void testSgrWithAltModifier() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // ESC[<8;10;20M - Left button with Alt (0 + 8 alt flag)
        boolean result = decoder.accept("\u001B[<8;10;20M");

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("press");
        assertThat(event.button).isEqualTo(MOUSE_BUTTON_LEFT);
        assertThat(event.shift).isFalse();
        assertThat(event.alt).isTrue();
        assertThat(event.ctrl).isFalse();
    }

    @Test
    public void testSgrWithCtrlModifier() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // ESC[<16;10;20M - Left button with Ctrl (0 + 16 ctrl flag)
        boolean result = decoder.accept("\u001B[<16;10;20M");

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("press");
        assertThat(event.button).isEqualTo(MOUSE_BUTTON_LEFT);
        assertThat(event.shift).isFalse();
        assertThat(event.alt).isFalse();
        assertThat(event.ctrl).isTrue();
    }

    @Test
    public void testSgrWithMultipleModifiers() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // ESC[<28;10;20M - Left button with Shift+Alt+Ctrl (0 + 4 + 8 + 16 = 28)
        boolean result = decoder.accept("\u001B[<28;10;20M");

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("press");
        assertThat(event.button).isEqualTo(MOUSE_BUTTON_LEFT);
        assertThat(event.shift).isTrue();
        assertThat(event.alt).isTrue();
        assertThat(event.ctrl).isTrue();
    }

    @Test
    public void testX10LeftButtonPress() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // ESC[M followed by 3 bytes: button+32, x+32, y+32
        // Button 0 (left) at position (10, 20)
        String sequence = "\u001B[M" + (char) (0 + 32) + (char) (10 + 32) + (char) (20 + 32);
        boolean result = decoder.accept(sequence);

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("press");
        assertThat(event.button).isEqualTo(MOUSE_BUTTON_LEFT);
        assertThat(event.x).isEqualTo(10);
        assertThat(event.y).isEqualTo(20);
    }

    @Test
    public void testX10RightButtonPress() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // Button 2 (right) at position (5, 15)
        String sequence = "\u001B[M" + (char) (2 + 32) + (char) (5 + 32) + (char) (15 + 32);
        boolean result = decoder.accept(sequence);

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("press");
        assertThat(event.button).isEqualTo(MOUSE_BUTTON_RIGHT);
        assertThat(event.x).isEqualTo(5);
        assertThat(event.y).isEqualTo(15);
    }

    @Test
    public void testX10ButtonRelease() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // Button 3 (release) at position (10, 20)
        String sequence = "\u001B[M" + (char) (3 + 32) + (char) (10 + 32) + (char) (20 + 32);
        boolean result = decoder.accept(sequence);

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("release");
        assertThat(event.x).isEqualTo(10);
        assertThat(event.y).isEqualTo(20);
    }

    @Test
    public void testX10WithModifiers() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // Button 0 with Shift+Ctrl at position (5, 10)
        // Button code: 0 + 4 (shift) + 16 (ctrl) = 20
        String sequence = "\u001B[M" + (char) (20 + 32) + (char) (5 + 32) + (char) (10 + 32);
        boolean result = decoder.accept(sequence);

        assertThat(result).isTrue();
        assertThat(handler.events).hasSize(1);
        TestMouseEvent.Event event = handler.last();
        assertThat(event.type).isEqualTo("press");
        assertThat(event.button).isEqualTo(MOUSE_BUTTON_LEFT);
        assertThat(event.shift).isTrue();
        assertThat(event.alt).isFalse();
        assertThat(event.ctrl).isTrue();
    }

    @Test
    public void testInvalidSequenceTooShort() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        boolean result = decoder.accept("\u001B[<0");

        assertThat(result).isFalse();
        assertThat(handler.events).isEmpty();
    }

    @Test
    public void testInvalidSequenceWrongPrefix() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        boolean result = decoder.accept("\u001B[A"); // Cursor up, not mouse

        assertThat(result).isFalse();
        assertThat(handler.events).isEmpty();
    }

    @Test
    public void testInvalidSgrMissingParameters() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        boolean result = decoder.accept("\u001B[<0;10M"); // Missing y coordinate

        assertThat(result).isFalse();
        assertThat(handler.events).isEmpty();
    }

    @Test
    public void testInvalidSgrNonNumericParameters() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        boolean result = decoder.accept("\u001B[<abc;10;20M");

        assertThat(result).isFalse();
        assertThat(handler.events).isEmpty();
    }

    @Test
    public void testInvalidX10WrongLength() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // X10 must be exactly 6 characters
        boolean result = decoder.accept("\u001B[M" + (char) 32 + (char) 32);

        assertThat(result).isFalse();
        assertThat(handler.events).isEmpty();
    }

    @Test
    public void testNullSequence() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        boolean result = decoder.accept(null);

        assertThat(result).isFalse();
        assertThat(handler.events).isEmpty();
    }

    @Test
    public void testEmptySequence() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        boolean result = decoder.accept("");

        assertThat(result).isFalse();
        assertThat(handler.events).isEmpty();
    }

    @Test
    public void testMultipleEvents() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // Process multiple events
        decoder.accept("\u001B[<0;1;1M"); // Press
        decoder.accept("\u001B[<32;2;2M"); // Drag
        decoder.accept("\u001B[<0;3;3m"); // Release

        assertThat(handler.events).hasSize(3);
        assertThat(handler.events.get(0).type).isEqualTo("press");
        assertThat(handler.events.get(1).type).isEqualTo("drag");
        assertThat(handler.events.get(2).type).isEqualTo("release");
    }

    @Test
    public void testZeroBasedCoordinates() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // SGR uses 1-based coordinates in the protocol, should convert to 0-based
        decoder.accept("\u001B[<0;1;1M");

        TestMouseEvent.Event event = handler.last();
        assertThat(event.x).isEqualTo(0);
        assertThat(event.y).isEqualTo(0);
    }

    @Test
    public void testLargeCoordinates() {
        TestMouseEvent handler = new TestMouseEvent();
        MouseDecoder decoder = new MouseDecoder(handler);

        // SGR can handle large coordinates (beyond X10's 223 limit)
        boolean result = decoder.accept("\u001B[<0;300;500M");

        assertThat(result).isTrue();
        TestMouseEvent.Event event = handler.last();
        assertThat(event.x).isEqualTo(299);
        assertThat(event.y).isEqualTo(499);
    }
}
