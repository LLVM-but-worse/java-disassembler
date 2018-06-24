package club.bytecode.the.jda.gui.fileviewer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

public abstract class NonRepeatKeyListener implements KeyListener {
    Set<Integer> pressedKeys = new HashSet<>();

    @Override
    public void keyPressed(KeyEvent e) {
        if (pressedKeys.add(e.getKeyCode())) {
            onDown(e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
        onUp(e);
    }

    protected abstract void onDown(KeyEvent e);
    protected abstract void onUp(KeyEvent e);
}
