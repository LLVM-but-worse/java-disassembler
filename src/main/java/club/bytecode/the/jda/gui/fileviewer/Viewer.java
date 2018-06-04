package club.bytecode.the.jda.gui.fileviewer;

import club.bytecode.the.jda.settings.Settings;
import com.strobel.annotations.Nullable;

import javax.swing.*;

public abstract class Viewer extends JPanel {
    private ViewerFile file;

    private static final long serialVersionUID = -2965538493489119191L;

    protected Viewer(ViewerFile file) {
        this.setFile(file);
    }
    
    public ViewerFile getFile() {
        return file;
    }
    
    public void setFile(ViewerFile file) {
        this.file = file;
    }

    public void updateName() {
        this.setName(getFile().name + (Settings.SHOW_CONTAINER_NAME.getBool() ? "(" + getFile().container + ")" : ""));
    }

    public abstract void refresh(@Nullable JButton button);
}
