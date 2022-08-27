package io.github.vultr.utils.time;

import org.lwjgl.glfw.GLFW;

public class DeltaTime {

    private static float lastTime;

    public static float getLastTime() {
        return lastTime;
    }

    public static float getDelta() {
        float time = (float) GLFW.glfwGetTime();
        float delta = time - lastTime;
        lastTime = time;
        return delta;
    }

}
