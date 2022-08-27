package io.github.vultr.core.window;

import io.github.vultr.core.listener.KeyListener;
import io.github.vultr.core.listener.MouseListener;

public class Input {

    public static final MouseListener mouseListener = MouseListener.get();
    public static final KeyListener keyListener = KeyListener.get();

    protected static void update() {
        mouseListener.update();
        keyListener.update();
    }

}
