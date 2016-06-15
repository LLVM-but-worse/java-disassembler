package the.bytecode.club.jda.gui;

import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.jda.FileChangeNotifier;

import javax.swing.*;
import java.awt.*;

/**
 * Used to represent all the panes inside of Bytecode Viewer, this is temp code
 * that was included from porting in J-RET, this needs to be re-written.
 *
 * @author Konloch
 * @author WaterWolf
 */

public abstract class VisibleComponent extends JInternalFrame implements FileChangeNotifier
{
    private static final long serialVersionUID = -6453413772343643526L;

    public VisibleComponent(final String title)
    {
        super(title, false, false, false, false);
        this.setFrameIcon(null);
        setResizable(true);
    }

    @SuppressWarnings("unused")
    private VisibleComponent()
    { // because we want to enforce the title
        // argument

    }

    @Override
    public abstract void openClassFile(final String name, String container, final ClassNode cn);

    @Override
    public abstract void openFile(final String name, String container, byte[] contents);

    protected static Dimension defaultDimensions;
    protected static Point defaultPosition;

    public abstract Dimension getDefaultDimensions();
    public abstract Point getDefaultPosition();
}
