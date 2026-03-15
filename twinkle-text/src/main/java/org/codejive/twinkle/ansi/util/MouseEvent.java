package org.codejive.twinkle.ansi.util;

/**
 * Interface for handling mouse events decoded from ANSI escape sequences. Implementations can be
 * passed to {@link MouseDecoder} to receive callbacks for different types of mouse events.
 */
public interface MouseEvent {
    /**
     * Called when a mouse button is pressed.
     *
     * @param button the button code (e.g., MOUSE_BUTTON_LEFT, MOUSE_BUTTON_RIGHT,
     *     MOUSE_BUTTON_MIDDLE)
     * @param x the x coordinate (0-based)
     * @param y the y coordinate (0-based)
     * @param shift true if the Shift key was held during the event
     * @param alt true if the Alt key was held during the event
     * @param ctrl true if the Ctrl key was held during the event
     */
    void onButtonPress(int button, int x, int y, boolean shift, boolean alt, boolean ctrl);

    /**
     * Called when a mouse button is released.
     *
     * @param button the button code (e.g., MOUSE_BUTTON_LEFT, MOUSE_BUTTON_RIGHT,
     *     MOUSE_BUTTON_MIDDLE)
     * @param x the x coordinate (0-based)
     * @param y the y coordinate (0-based)
     * @param shift true if the Shift key was held during the event
     * @param alt true if the Alt key was held during the event
     * @param ctrl true if the Ctrl key was held during the event
     */
    void onButtonRelease(int button, int x, int y, boolean shift, boolean alt, boolean ctrl);

    /**
     * Called when the mouse is moved while a button is held down (drag event).
     *
     * @param button the button code (e.g., MOUSE_BUTTON_LEFT, MOUSE_BUTTON_RIGHT,
     *     MOUSE_BUTTON_MIDDLE)
     * @param x the x coordinate (0-based)
     * @param y the y coordinate (0-based)
     * @param shift true if the Shift key was held during the event
     * @param alt true if the Alt key was held during the event
     * @param ctrl true if the Ctrl key was held during the event
     */
    void onDrag(int button, int x, int y, boolean shift, boolean alt, boolean ctrl);

    /**
     * Called when the mouse wheel is scrolled.
     *
     * @param direction the scroll direction (MOUSE_SCROLL_UP or MOUSE_SCROLL_DOWN)
     * @param x the x coordinate (0-based)
     * @param y the y coordinate (0-based)
     * @param shift true if the Shift key was held during the event
     * @param alt true if the Alt key was held during the event
     * @param ctrl true if the Ctrl key was held during the event
     */
    void onScroll(int direction, int x, int y, boolean shift, boolean alt, boolean ctrl);

    /**
     * Called when the mouse is moved without any buttons held down (requires any event tracking).
     *
     * @param x the x coordinate (0-based)
     * @param y the y coordinate (0-based)
     * @param shift true if the Shift key was held during the event
     * @param alt true if the Alt key was held during the event
     * @param ctrl true if the Ctrl key was held during the event
     */
    void onMove(int x, int y, boolean shift, boolean alt, boolean ctrl);
}
