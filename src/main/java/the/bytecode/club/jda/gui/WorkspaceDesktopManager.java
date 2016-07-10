package the.bytecode.club.jda.gui;

import the.bytecode.club.jda.settings.Settings;

import javax.swing.*;
import java.awt.*;

public class WorkspaceDesktopManager extends DefaultDesktopManager
{
    private static final int SNAP_THRESHOLD = 5;

    @Override
    public void dragFrame(JComponent f, int x, int y)
    {
        if (Settings.SNAP_TO_EDGES.getBool() && f instanceof VisibleComponent)
        {
            VisibleComponent frame = (VisibleComponent) f;
            JDesktopPane desk = frame.getDesktopPane();
            Dimension d = desk.getSize();
            if (x < SNAP_THRESHOLD)
                x = 0;
            else if (x + frame.getWidth() > d.width - SNAP_THRESHOLD)
                x = Math.max(0, d.width - frame.getWidth());
            if (y < SNAP_THRESHOLD)
                y = 0;
            else if (y + frame.getHeight() > d.height - SNAP_THRESHOLD)
                y = Math.max(0, d.height - frame.getHeight());
        }
        super.dragFrame(f, x, y);
    }

    @Override
    public void resizeFrame(JComponent f, int x, int y, int w, int h)
    {
        if (Settings.SNAP_TO_EDGES.getBool())
        {
            VisibleComponent frame = (VisibleComponent) f;
            JDesktopPane desk = frame.getDesktopPane();
            Dimension d = desk.getSize();
            if (x < SNAP_THRESHOLD)
                x = 0;
            else if (d.width - x - w < SNAP_THRESHOLD)
                w = Math.max(0, d.width - x);
            if (y < SNAP_THRESHOLD)
                y = 0;
            if (d.height - y - h < SNAP_THRESHOLD)
                h = Math.max(0, d.height - y);
        }
        super.resizeFrame(f, x, y, w, h);
    }
}