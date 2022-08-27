package io.github.vultr.core.window;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import io.github.vultr.core.exceptions.NoSceneFoundException;
import io.github.vultr.core.listener.KeyListener;
import io.github.vultr.core.listener.MouseListener;
import io.github.vultr.core.scene.SceneManager;
import io.github.vultr.utils.time.DeltaTime;
import lombok.Getter;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;

public class Window {

    private static Window instance;

    @Getter
    private int width, height;

    @Getter
    private String title;

    @Getter
    private boolean resizeable;
    @Getter
    private boolean fullscreen;
    @Getter
    private boolean vsync;
    @Getter
    private boolean centerWindowOnStart;

    @Getter
    private boolean windowReady; // returns true when the window is done loading

    @Getter
    private long windowId;

    @Getter
    private float delta;

    protected Window() {
        init(800, 600, "Vultr");
    }

    protected Window(int width, int height, String title) {
        init(width, height, title);
    }

    private void init(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;

        // Initialize the scene manager
        SceneManager.init(this);
    }

    /**
     * Checks if a window has been initialized
     * if not a new instance will be instantiated with the default parameters.
     * 
     * @return the instance of the current window
     */
    public static Window get() {
        if (instance == null) {
            instance = new Window();
        }
        return instance;
    }

    /**
     * Checks if a window has been initialized
     * if not a new instance will be instantiated.
     * 
     * @param width
     * @param height
     * @param title
     * @return the instance of the current window
     */
    public static Window get(int width, int height, String title) {
        if (instance == null) {
            instance = new Window(width, height, title);
        }
        return instance;
    }

    /**
     * Set the window to fullscreen
     * 
     * @return
     */
    public Window fullscreen() {
        this.fullscreen = true;
        return this;
    }

    /**
     * Enable the window to be resizable
     * 
     * @return
     */
    public Window resizeable() {
        this.resizeable = true;
        return this;
    }

    /**
     * Enable vsync
     * Will cap the maximum fps of the window to the refresh rate of the monitor
     * 
     * @return
     */
    public Window vsync() {
        this.vsync = true;
        return this;
    }

    /**
     * Will center the window on the screen when it is opened
     * 
     * @return
     */
    public Window centered() {
        this.centerWindowOnStart = true;
        return this;
    }

    /**
     * Show the window and start the update loop
     */
    public void show() {
        initWindow();
        loop();

        // Free callbacks and destroy window
        glfwFreeCallbacks(windowId);
        glfwDestroyWindow(windowId);

        // Terminate GLFW
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * Initialize the window
     */
    private void initWindow() {
        // Setup the error callback
        // Errors will be printed to the console
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        // If GLFW can't be initialized, exit the application
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Setup default window hints
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, glfwBool(isResizeable()));
        glfwWindowHint(GLFW_MAXIMIZED, glfwBool(isFullscreen()));

        // Create the window
        windowId = glfwCreateWindow(getWidth(), getHeight(), getTitle(), NULL, NULL);
        if (windowId == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        setCallbacks();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(windowId, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window on the screen
            if (isCenterWindowOnStart()) {
                glfwSetWindowPos(
                        windowId,
                        (vidmode.width() - pWidth.get(0)) / 2,
                        (vidmode.height() - pHeight.get(0)) / 2);
            }
        }

        glfwMakeContextCurrent(windowId);

        // Enable vsync if enabled
        glfwSwapInterval(isVsync() ? 1 : 0);

        // Make the window visible
        glfwShowWindow(windowId);

        // Enable LWJGL bindings
        GL.createCapabilities();

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        this.windowReady = true;

        if (SceneManager.hasScene()) {
            SceneManager.changeScene(0);
        }
    }

    /**
     * Start the update loop of the window
     */
    private void loop() {
        while (!glfwWindowShouldClose(windowId)) {
            // Update input and poll events
            Input.update();
            glfwPollEvents();

            // Set the clear color of the window
            glClearColor(1, 1, 1, 1);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH);

            // Draw and update the current scene
            // if a scene is set
            if (!SceneManager.hasScene())
                throw new NoSceneFoundException("No scene has been added to the scene");

            delta = DeltaTime.getDelta();
            if (delta >= 0)
                SceneManager.getCurrentScene().update(delta);
            SceneManager.getCurrentScene().draw(delta);

            // Swap the buffers
            glfwSwapBuffers(windowId);
        }
    }

    private void setCallbacks() {
        glfwSetFramebufferSizeCallback(windowId, Window::framebufferSizeCallback);
        glfwSetCursorPosCallback(windowId, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(windowId, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(windowId, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(windowId, KeyListener::keyCallback);
    }

    private int glfwBool(boolean state) {
        return state ? GLFW_TRUE : GLFW_FALSE;
    }

    protected static void framebufferSizeCallback(long window, int width, int height) {
        Window.get().width = width;
        Window.get().height = height;
    }

}
