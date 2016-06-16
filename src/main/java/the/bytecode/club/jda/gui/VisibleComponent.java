package the.bytecode.club.jda.gui;

import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.jda.FileChangeNotifier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Used to represent all the panes inside of Bytecode Viewer, this is temp code
 * that was included from porting in J-RET, this needs to be re-written.
 *
 * @author Konloch
 * @author WaterWolf
 */

public abstract class VisibleComponent extends JInternalFrame implements FileChangeNotifier
{
    private String windowId;

    public Point unmaximizedPos;
    public Dimension unmaximizedSize;

    public VisibleComponent(final String id, final String title, final Icon icon)
    {
        super(title, true, true, true, true);
        windowId = id;
        setFrameIcon(icon);

        unmaximizedPos = getLocation();
        unmaximizedSize = getSize();

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                if (!isMaximum())
                    unmaximizedSize = getSize();
                super.componentResized(e);
            }

            @Override
            public void componentMoved(ComponentEvent e)
            {
                if (!isMaximum())
                    unmaximizedPos = getLocation();
                super.componentMoved(e);
            }
        });

        addPropertyChangeListener(evt -> {
            if (!isMaximum() && !isIcon())
            {
                setSize(unmaximizedSize);
                setLocation(unmaximizedPos);
            }
        });
    }

    @Override
    public abstract void openClassFile(final String name, String container, final ClassNode cn);

    @Override
    public abstract void openFile(final String name, String container, byte[] contents);

    protected static Dimension defaultDimensions;
    protected static Point defaultPosition;

    public abstract Dimension getDefaultDimensions();
    public abstract Point getDefaultPosition();

    public String getWindowId()
    {
        return windowId;
    }
}
