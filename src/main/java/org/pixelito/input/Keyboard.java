package org.pixelito.input;

import org.lwjgl.glfw.GLFW;

public class Keyboard {

    private static final boolean[] keys = new boolean[GLFW.GLFW_KEY_LAST];
    private static final boolean[] keysPressed = new boolean[GLFW.GLFW_KEY_LAST];
    private static final boolean[] previousKeys = new boolean[GLFW.GLFW_KEY_LAST];

    public static void setKey(int key, boolean pressed) {
        if (key >= 0 && key < keys.length) {
            // Store the key's previous state before updating
            previousKeys[key] = keys[key];
            keys[key] = pressed;
            
            // A key is "pressed" only on the frame it transitions from released to pressed
            keysPressed[key] = pressed && !previousKeys[key];
        }
    }

    /**
     * Checks if a key is currently held down.
     * 
     * @param key The key code to check
     * @return true if the key is currently held down
     */
    public static boolean isKeyDown(int key) {
        return key >= 0 && key < keys.length && keys[key];
    }
    
    /**
     * Checks if a key was just pressed this frame.
     * Returns true only on the frame when the key transitions from released to pressed.
     * 
     * @param key The key code to check
     * @return true if the key was just pressed
     */
    public static boolean isKeyPressed(int key) {
        return key >= 0 && key < keysPressed.length && keysPressed[key];
    }
    
    /**
     * Called at the end of each frame to reset the "just pressed" state.
     */
    public static void update() {
        for (int i = 0; i < keysPressed.length; i++) {
            keysPressed[i] = false;
        }
    }
}