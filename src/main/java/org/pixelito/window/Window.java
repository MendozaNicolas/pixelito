package org.pixelito.window;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.pixelito.input.Keyboard;
import org.pixelito.input.Mouse;

public class Window {
    private long windowHandle;
    private final int width;
    private final int height;
    private final String title;
    private boolean resized = false;
    private boolean vSync;

    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWCursorPosCallback cursorPosCallback;
    private GLFWMouseButtonCallback mouseButtonCallback;
    private GLFWScrollCallback scrollCallback;

    public Window(int width, int height, String title, boolean vSync) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.vSync = vSync;
    }

    public void create() {
        // Inicializar GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("No se pudo inicializar GLFW.");
        }

        // Seteo el error callback
        errorCallback = GLFWErrorCallback.createPrint(System.err);
        GLFW.glfwSetErrorCallback(errorCallback);

        // Configuración de la ventana
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE); // ventana oculta hasta que esté lista
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUSED, GLFW.GLFW_TRUE); // Start focused
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUS_ON_SHOW, GLFW.GLFW_TRUE); // Focus when shown

        // Crear ventana
        windowHandle = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (windowHandle == MemoryUtil.NULL) {
            throw new RuntimeException("No se pudo crear la ventana GLFW.");
        }

        // Callback de resize
        GLFW.glfwSetFramebufferSizeCallback(windowHandle, (window, w, h) -> resized = true);

        // Set up keyboard callback
        keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                Keyboard.setKey(key, action != GLFW.GLFW_RELEASE);
            }
        };
        GLFW.glfwSetKeyCallback(windowHandle, keyCallback);
        
        // Set up mouse position callback
        cursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                Mouse.setPosition(xpos, ypos);
            }
        };
        GLFW.glfwSetCursorPosCallback(windowHandle, cursorPosCallback);
        
        // Set up mouse button callback
        mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                Mouse.setButton(button, action != GLFW.GLFW_RELEASE);
            }
        };
        GLFW.glfwSetMouseButtonCallback(windowHandle, mouseButtonCallback);
        
        // Set up scroll callback
        scrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                Mouse.setScroll(xoffset, yoffset);
            }
        };
        GLFW.glfwSetScrollCallback(windowHandle, scrollCallback);
        
        // Initialize mouse with current cursor position
        double[] xPos = new double[1];
        double[] yPos = new double[1];
        GLFW.glfwGetCursorPos(windowHandle, xPos, yPos);
        Mouse.init(xPos[0], yPos[0]);

        // Centrar la ventana
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        if (vidmode != null) {
            GLFW.glfwSetWindowPos(
                    windowHandle,
                    (vidmode.width() - width) / 2,
                    (vidmode.height() - height) / 2
            );
        }

        // Hacer contexto actual
        GLFW.glfwMakeContextCurrent(windowHandle);

        // Sincronización vertical
        if (vSync) {
            GLFW.glfwSwapInterval(1);
        }

        // Mostrar ventana
        GLFW.glfwShowWindow(windowHandle);
        
        // Ensure the window is focused
        GLFW.glfwFocusWindow(windowHandle);

        // Cargar capacidades OpenGL
        GL.createCapabilities();

        // Configuración inicial de OpenGL
        GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        
        // Enable alpha blending for transparent textures
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        System.out.println("Window created successfully with handle: " + windowHandle);
    }

    public void update() {
        GLFW.glfwSwapBuffers(windowHandle);
        GLFW.glfwPollEvents();
        
        // Update input states at the end of the frame
        Keyboard.update();
        Mouse.update();
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(windowHandle);
    }

    public long getId() {
        return windowHandle;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isResized() {
        return resized;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public void destroy() {
        // Free callbacks
        if (keyCallback != null) keyCallback.free();
        if (cursorPosCallback != null) cursorPosCallback.free();
        if (mouseButtonCallback != null) mouseButtonCallback.free();
        if (scrollCallback != null) scrollCallback.free();
        
        GLFW.glfwDestroyWindow(windowHandle);
        GLFW.glfwTerminate();
        if (errorCallback != null) errorCallback.free();
    }
}
