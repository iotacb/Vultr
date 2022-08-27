package demo;

import io.github.vultr.core.scene.SceneManager;
import io.github.vultr.core.window.Window;

public class Main {

    public static void main(String[] args) {
        Window window = Window.get().centered().vsync();

        SceneManager.addScene(DemoScene.class);

        window.show();
    }

}
