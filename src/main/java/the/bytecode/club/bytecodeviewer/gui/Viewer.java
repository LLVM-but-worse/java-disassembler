package the.bytecode.club.bytecodeviewer.gui;

import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.bytecodeviewer.BytecodeViewer;

import javax.swing.*;

public abstract class Viewer extends JPanel
{

    public ClassNode cn;
    public String name;
    public String container;

    private static final long serialVersionUID = -2965538493489119191L;

    public void updateName()
    {
        this.setName(name + (BytecodeViewer.viewer.mnShowContainer.isSelected() ? "(" + container + ")" : ""));
    }
}
