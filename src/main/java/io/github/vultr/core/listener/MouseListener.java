package io.github.vultr.core.listener;

import static org.lwjgl.glfw.GLFW.*;

import io.github.vultr.core.window.Window;
import lombok.Getter;

public class MouseListener {

    private static MouseListener instance;

    @Getter
    private double x;
    @Getter
    private double y;

    @Getter
    private double lastX;
    @Getter
    private double lastY;

    @Getter
    private double scrollX;
    @Getter
    private double scrollY;

    @Getter
    private boolean dragging;

    private boolean buttonPressed[] = new boolean[GLFW_MOUSE_BUTTON_LAST];
    private boolean buttonPressedLast[] = new boolean[GLFW_MOUSE_BUTTON_LAST];

    public static void mousePosCallback(long window, double x, double y) {
        get().lastX = get().x;
        get().lastY = get().y;
        get().x = x;
        get().y = Window.get().getHeight() - y;
        get().dragging = get().buttonPressed();
    }

    public static void mouseButtonCallback(long window, int button, int action, int mods) {
        if (action == GLFW_PRESS) {
            get().buttonPressed[button] = true;
        } else if (action == GLFW_RELEASE) {
            get().buttonPressed[button] = false;
        }
    }

    public static void mouseScrollCallback(long window, double x, double y) {
        get().scrollX = x;
        get().scrollY = y;
    }

    /**
     * Get the instance of the MouseListener.
     * if the instance is null, create a new one.
     */
    public static MouseListener get() {
        if (instance == null) {
            instance = new MouseListener();
        }
        return instance;
    }

    /**
     * This method is automatically called.
     * When called manually there can be update errors;
     */
    public void update() {
        for (int i = 0; i < GLFW_MOUSE_BUTTON_LAST; i++) {
            buttonPressedLast[i] = buttonPressed[i];
        }
    }

    public boolean buttonPressed() {
        for (int i = 0; i < GLFW_MOUSE_BUTTON_LAST; i++) {
            if (buttonState(i)) {
                return true;
            }
        }
        return false;
    }

    public boolean buttonState(int button) {
        return buttonPressed[button];
    }

    public boolean buttonPressed(int button) {
        return buttonPressed[button] && !buttonPressedLast[button];
    }

    public boolean buttonReleased(int button) {
        return !buttonPressed[button] && buttonPressedLast[button];
    }

}
