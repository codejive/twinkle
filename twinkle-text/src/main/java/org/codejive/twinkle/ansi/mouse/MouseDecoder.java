package org.codejive.twinkle.ansi.mouse;

import static org.codejive.twinkle.ansi.Constants.*;

import org.jspecify.annotations.NonNull;

/**
 * A decoder for ANSI mouse event escape sequences. This class supports both SGR (Select Graphic
 * Rendition) extended mouse protocol and X10 mouse protocol events.
 *
 * <p>SGR mouse events have the format:
 *
 * <ul>
 *   <li>{@code ESC[<Cb;Cx;CyM} for button press and drag events
 *   <li>{@code ESC[<Cb;Cx;Cym} for button release events
 * </ul>
 *
 * <p>Where:
 *
 * <ul>
 *   <li>Cb = button code with modifiers (Shift=4, Alt=8, Ctrl=16)
 *   <li>Cx = x coordinate (1-based in protocol, converted to 0-based)
 *   <li>Cy = y coordinate (1-based in protocol, converted to 0-based)
 * </ul>
 *
 * <p>X10 mouse events have the format:
 *
 * <ul>
 *   <li>{@code ESC[M<Cb><Cx><Cy>} where each parameter is a single byte
 * </ul>
 *
 * <p>Where:
 *
 * <ul>
 *   <li>Cb = button code + 32 with modifiers (Shift=4, Alt=8, Ctrl=16)
 *   <li>Cx = x coordinate + 32 (0-based)
 *   <li>Cy = y coordinate + 32 (0-based)
 * </ul>
 */
public class MouseDecoder {
    private final @NonNull MouseEvent handler;

    private static final int DRAG_FLAG = 32;
    private static final int MOVE_FLAG = 35; // Button code for move events

    /**
     * Creates a new MouseDecoder with the specified event handler.
     *
     * @param handler the MouseEvent handler that will receive callbacks for decoded mouse events
     */
    public MouseDecoder(@NonNull MouseEvent handler) {
        this.handler = handler;
    }

    /**
     * Attempts to decode the given escape sequence as a mouse event.
     *
     * <p>If the sequence is a valid SGR or X10 mouse event, this method will call the appropriate
     * callback on the MouseEvent handler and return true. If the sequence is not a mouse event or
     * is malformed, it returns false without calling the handler.
     *
     * @param sequence the complete ANSI escape sequence to analyze
     * @return true if the sequence was a mouse event and was successfully decoded, false otherwise
     */
    public boolean accept(String sequence) {
        if (sequence == null || sequence.length() < 6) {
            return false;
        }

        // Check for SGR format: ESC[<...M or ESC[<...m
        if (sequence.startsWith(CSI + MOUSE_SGR_CMD)) {
            return decodeSgr(sequence);
        }

        // Check for X10 format: ESC[M followed by exactly 3 bytes
        if (sequence.startsWith(CSI + MOUSE_X10_CMD) && sequence.length() == 6) {
            return decodeX10(sequence);
        }

        return false;
    }

    /**
     * Decodes an SGR extended mouse event sequence.
     *
     * @param sequence the SGR mouse sequence
     * @return true if successfully decoded, false otherwise
     */
    private boolean decodeSgr(String sequence) {
        // Check the final character (M for press/drag, m for release)
        char finalChar = sequence.charAt(sequence.length() - 1);
        if (finalChar != 'M' && finalChar != 'm') {
            return false;
        }

        boolean isRelease = (finalChar == 'm');

        // Extract the parameters: button, x, y
        String params = sequence.substring(3, sequence.length() - 1);
        String[] parts = params.split(";");

        if (parts.length != 3) {
            return false;
        }

        try {
            int buttonCode = Integer.parseInt(parts[0]);
            int x = Integer.parseInt(parts[1]) - 1; // Convert from 1-based to 0-based
            int y = Integer.parseInt(parts[2]) - 1; // Convert from 1-based to 0-based

            dispatchMouseEvent(buttonCode, x, y, isRelease);
            return true;
        } catch (NumberFormatException e) {
            // Malformed sequence
            return false;
        }
    }

    /**
     * Decodes an X10 mouse event sequence.
     *
     * @param sequence the X10 mouse sequence (ESC[M followed by 3 bytes)
     * @return true if successfully decoded, false otherwise
     */
    private boolean decodeX10(String sequence) {
        // Extract the three bytes: button, x, y
        int buttonCode = sequence.charAt(3) - MOUSE_X10_OFFSET;
        int x = sequence.charAt(4) - MOUSE_X10_OFFSET;
        int y = sequence.charAt(5) - MOUSE_X10_OFFSET;

        // X10 uses button bits 0-1 to encode which button, with 3 meaning release
        int buttonBits = buttonCode & 3;
        boolean isRelease = (buttonBits == 3);

        dispatchMouseEvent(buttonCode, x, y, isRelease);
        return true;
    }

    /**
     * Dispatches a mouse event to the appropriate handler method based on the button code and
     * release flag.
     *
     * @param buttonCode the raw button code with modifiers
     * @param x the x coordinate (0-based)
     * @param y the y coordinate (0-based)
     * @param isRelease true if this is a button release event
     */
    private void dispatchMouseEvent(int buttonCode, int x, int y, boolean isRelease) {
        // Extract modifiers
        boolean shift = (buttonCode & MOUSE_MODIFIER_SHIFT) != 0;
        boolean alt = (buttonCode & MOUSE_MODIFIER_ALT) != 0;
        boolean ctrl = (buttonCode & MOUSE_MODIFIER_CTRL) != 0;

        // Remove modifiers to get the base button code
        int baseButton =
                buttonCode & ~(MOUSE_MODIFIER_SHIFT | MOUSE_MODIFIER_ALT | MOUSE_MODIFIER_CTRL);

        // Handle different event types
        if (isRelease) {
            // Button release event
            handler.onButtonRelease(baseButton, x, y, shift, alt, ctrl);
        } else {
            // Check if it's a drag/move event (bit 5 set, value 32)
            if ((baseButton & DRAG_FLAG) != 0) {
                int dragButton = baseButton & ~DRAG_FLAG;
                // Check if it's a move event (no button pressed)
                if (dragButton == MOVE_FLAG - DRAG_FLAG) {
                    handler.onMove(x, y, shift, alt, ctrl);
                } else {
                    handler.onDrag(dragButton, x, y, shift, alt, ctrl);
                }
            } else if (baseButton >= MOUSE_SCROLL_UP && baseButton <= MOUSE_SCROLL_DOWN) {
                // Scroll event
                handler.onScroll(baseButton, x, y, shift, alt, ctrl);
            } else {
                // Button press event
                handler.onButtonPress(baseButton, x, y, shift, alt, ctrl);
            }
        }
    }
}
