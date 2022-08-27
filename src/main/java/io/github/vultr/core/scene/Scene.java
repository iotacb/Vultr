package io.github.vultr.core.scene;

import io.github.vultr.core.window.Window;
import lombok.Getter;

public abstract class Scene {

    @Getter
    private Window window;

    public Scene(Window window) {
        this.window = window;
    }

    public abstract void init();

    public abstract void update(float delta);

    public abstract void draw(float delta);

    public void onSceneEnter(Scene previousScene) {
        init();
    }

    public void onSceneExit() {
    }

}
