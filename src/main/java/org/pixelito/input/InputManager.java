package org.pixelito.input;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;

public class InputManager {

    public static void setup(long window) {
        // Teclado
        GLFW.glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_PRESS) {
                Keyboard.setKey(key, true);
            } else if (action == GLFW.GLFW_RELEASE) {
                Keyboard.setKey(key, false);
            }
        });

        // Mouse
        GLFW.glfwSetCursorPosCallback(window, (w, xpos, ypos) -> {
            Mouse.setPosition(xpos, ypos);
        });

        // Desactiva cursor visible para mouse-look
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }
}