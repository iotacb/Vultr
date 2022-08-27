package demo;

import io.github.vultr.core.scene.Scene;
import io.github.vultr.core.window.Window;
import io.github.vultr.utils.render.Shader;

public class DemoScene extends Scene {

    Shader shader = new Shader("./assets/shaders/vertex.glsl", "./assets/shaders/fragment.glsl");

    public DemoScene(Window window) {
        super(window);
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void draw(float delta) {
        shader.drawShader();
    }

    @Override
    public void init() {
        shader.compileShader();
    }

}
