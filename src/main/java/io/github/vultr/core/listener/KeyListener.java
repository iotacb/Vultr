package io.github.vultr.core.listener;

import static org.lwjgl.glfw.GLFW.*;

public class KeyListener {

    private static KeyListener instance;

    private boolean keyPressed[] = new boolean[GLFW_KEY_LAST];
    private boolean keyPressedLast[] = new boolean[GLFW_KEY_LAST];

    public static void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            get().keyPressed[key] = true;
        } else if (action == GLFW_RELEASE) {
            get().keyPressed[key] = false;
        }
    }

    /**
     * Get the instance of the KeyListener.
     * if the instance is null, create a new one.
     */
    public static KeyListener get() {
        if (instance == null) {
            instance = new KeyListener();
        }
        return instance;
    }

    /**
     * This method is automatically called.
     * When called manually there can be update errors;
     */
    public void update() {
        for (int i = 32; i < GLFW_KEY_LAST; i++) {
            keyPressedLast[i] = keyPressed[i];
        }
    }

    public boolean keysPressed() {
        for (int i = 32; i < GLFW_KEY_LAST; i++) {
            if (keyState(i)) {
                return true;
            }
        }
        return false;
    }

    public boolean keyState(int key) {
        return keyPressed[key];
    }

    public boolean keyPressed(int key) {
        return keyPressed[key] && !keyPressedLast[key];
    }

    public boolean keyReleased(int key) {
        return !keyPressed[key] && keyPressedLast[key];
    }

}
