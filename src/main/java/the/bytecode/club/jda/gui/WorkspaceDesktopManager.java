package the.bytecode.club.jda.gui;

import javax.swing.*;
import java.awt.*;

public class WorkspaceDesktopManager extends DefaultDesktopManager
{
    @Override
    public void dragFrame(JComponent f, int x, int y)
    {
        if (f instanceof VisibleComponent)
        {
            VisibleComponent frame = (VisibleComponent) f;
            JDesktopPane desk = frame.getDesktopPane();
            Dimension d = desk.getSize();
            if (x < 5)
                x = 0;
            else if (x + frame.getWidth() > d.width - 5)
                x = d.width - frame.getWidth();
            if (y < 5)
                y = 0;
        }
        super.dragFrame(f, x, y);
    }
}