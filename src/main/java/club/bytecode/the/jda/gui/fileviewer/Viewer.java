package club.bytecode.the.jda.gui.fileviewer;

import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.settings.Settings;

import javax.swing.*;

public abstract class Viewer extends JPanel {
    protected String pathInContainer; // path within the container
    public String name;
    public FileContainer container;

    private static final long serialVersionUID = -2965538493489119191L;

    public void updateName() {
        this.setName(name + (Settings.SHOW_CONTAINER_NAME.getBool() ? "(" + container + ")" : ""));
    }
}
