package org.pixelito.input;

import org.lwjgl.glfw.GLFW;

/**
 * Handles mouse input including position, movement, and button states.
 * Supports mouse look functionality for camera control.
 */
public class Mouse {
    // Current and previous mouse positions
    private static double xPos, yPos;
    private static double lastX, lastY;
    
    // Mouse movement since last frame
    private static double deltaX, deltaY;
    
    // Mouse buttons state
    private static final boolean[] buttons = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];
    private static final boolean[] buttonsPressed = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];
    private static final boolean[] previousButtons = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];
    
    // Scroll wheel
    private static double scrollX, scrollY;
    
    // Whether mouse is captured (for first-person camera)
    private static boolean captured = false;
    
    // First mouse movement flag to avoid large initial jump
    private static boolean firstMouse = true;

    /**
     * Initialize mouse position
     */
    public static void init(double x, double y) {
        xPos = x;
        yPos = y;
        lastX = x;
        lastY = y;
        deltaX = 0;
        deltaY = 0;
        scrollX = 0;
        scrollY = 0;
        firstMouse = true;
    }
    
    /**
     * Sets the position of the mouse cursor
     */
    public static void setPosition(double x, double y) {
        // Handle first mouse input to avoid large initial jump
        if (firstMouse) {
            lastX = x;
            lastY = y;
            xPos = x;
            yPos = y;
            deltaX = 0;
            deltaY = 0;
            firstMouse = false;
            return; // Skip calculating deltas on first update
        }
        
        // Update current position
        xPos = x;
        yPos = y;
        
        // Calculate movement delta
        deltaX += (xPos - lastX);
        deltaY += (lastY - yPos); // Reversed since y-coordinates go from bottom to top
        
        // Update last position for next frame
        lastX = xPos;
        lastY = yPos;
    }
    
    /**
     * Sets the state of a mouse button
     */
    public static void setButton(int button, boolean pressed) {
        if (button >= 0 && button < buttons.length) {
            // Store previous state
            previousButtons[button] = buttons[button];
            buttons[button] = pressed;
            
            // A button is "pressed" only on the frame it transitions from released to pressed
            buttonsPressed[button] = pressed && !previousButtons[button];
        }
    }
    
    /**
     * Sets the scroll wheel offset
     */
    public static void setScroll(double xOffset, double yOffset) {
        scrollX = xOffset;
        scrollY = yOffset;
    }
    
    /**
     * Checks if a mouse button is currently held down
     */
    public static boolean isButtonDown(int button) {
        return button >= 0 && button < buttons.length && buttons[button];
    }
    
    /**
     * Checks if a mouse button was just pressed this frame
     */
    public static boolean isButtonPressed(int button) {
        return button >= 0 && button < buttonsPressed.length && buttonsPressed[button];
    }
    
    /**
     * Gets the X position of the cursor
     */
    public static double getX() {
        return xPos;
    }
    
    /**
     * Gets the Y position of the cursor
     */
    public static double getY() {
        return yPos;
    }
    
    /**
     * Gets the X movement since last frame and resets it
     */
    public static double getDeltaX() {
        double temp = deltaX;
        deltaX = 0;  // Reset after reading
        return temp;
    }
    
    /**
     * Gets the Y movement since last frame and resets it
     */
    public static double getDeltaY() {
        double temp = deltaY;
        deltaY = 0;  // Reset after reading
        return temp;
    }
    
    /**
     * Gets the X scroll offset
     */
    public static double getScrollX() {
        return scrollX;
    }
    
    /**
     * Gets the Y scroll offset
     */
    public static double getScrollY() {
        return scrollY;
    }
    
    /**
     * Set whether the mouse is captured (hidden and locked to window center)
     */
    public static void setCaptured(boolean capture, long windowHandle) {
        captured = capture;
        
        if (capture) {
            // Hide cursor and capture it
            GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            
            // Reset first mouse flag to avoid jumps when recapturing
            firstMouse = true;
            
            // Center cursor to the middle of the window
            int[] width = new int[1];
            int[] height = new int[1];
            GLFW.glfwGetWindowSize(windowHandle, width, height);
            GLFW.glfwSetCursorPos(windowHandle, width[0] / 2, height[0] / 2);
        } else {
            // Show cursor and release it
            GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        }
    }
    
    /**
     * Check if mouse is currently captured
     */
    public static boolean isCaptured() {
        return captured;
    }
    
    /**
     * Reset scroll wheel offsets and button pressed states (call at end of frame)
     */
    public static void update() {
        // Reset scroll offsets
        scrollX = 0;
        scrollY = 0;
        
        // Reset button pressed states
        for (int i = 0; i < buttonsPressed.length; i++) {
            buttonsPressed[i] = false;
        }
        
        // Note: deltaX and deltaY are now reset when they're read via getDeltaX() and getDeltaY()
    }
}
