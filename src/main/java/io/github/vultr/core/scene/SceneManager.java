package io.github.vultr.core.scene;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import io.github.vultr.core.window.Window;
import lombok.Getter;

public class SceneManager {

    private static Window window;

    @Getter
    private static List<Scene> scenes = new ArrayList<>();

    @Getter
    private static Scene currentScene;
    @Getter
    private static Scene previousScene;

    @Getter
    private static int sceneIndex;

    public static void init(Window window) {
        SceneManager.window = window;
    }

    /**
     * Add a scene to the scene manager.
     * 
     * @param scene
     */
    public static void addScene(Class<? extends Scene> scene) {
        try {
            Scene s = scene.getConstructor(window.getClass()).newInstance(window);
            addScene(s);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void addScene(Scene scene) {
        scenes.add(scene);
    }

    public static void changeScene(Class<? extends Scene> scene) {
        previousScene = currentScene;
        if (currentScene != null)
            currentScene.onSceneExit();
        currentScene = getScene(scene);
        if (window.isWindowReady())
            currentScene.onSceneEnter(previousScene);
        sceneIndex = getSceneIndex(scene);
    }

    public static void changeScene(int index) {
        previousScene = currentScene;
        if (currentScene != null)
            currentScene.onSceneExit();
        currentScene = getScene(index);
        if (window.isWindowReady())
            currentScene.onSceneEnter(previousScene);
        sceneIndex = index;
    }

    /**
     * Returns a scene in the scene list
     * based on the provided scene class.
     * 
     * @param scene
     * @return
     */
    public static Scene getScene(Class<? extends Scene> scene) {

        for (Scene s : scenes) {
            if (s.getClass().equals(scene)) {
                return s;
            }
        }

        return scenes.get(0);
    }

    public static int getSceneIndex(Class<? extends Scene> scene) {
        return scenes.indexOf(getScene(scene));
    }

    public static Scene getScene(int index) {
        return scenes.get(index);
    }

    public static boolean hasScene() {
        return scenes.size() > 0;
    }

}
