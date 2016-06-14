package the.bytecode.club.jda.gui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.jda.BytecodeViewer;
import the.bytecode.club.jda.decompilers.Decompiler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This represents the opened classfile.
 *
 * @author Konloch
 * @author WaterWolf
 */

public class ClassViewer extends Viewer
{
    private static final long serialVersionUID = -8650495368920680024L;
    private List<Thread> decompileThreads = new ArrayList<>();

    public void setPanes()
    {
        for (int i = 0; i < BytecodeViewer.viewer.allPanes.size(); i++)
        {
            ButtonGroup group = BytecodeViewer.viewer.allPanes.get(i);
            for (Map.Entry<JRadioButtonMenuItem, Decompiler> entry : BytecodeViewer.viewer.allDecompilers.get(group).entrySet())
            {
                if (group.isSelected(entry.getKey().getModel()))
                {
                    decompilers.set(i, entry.getValue());
                }
            }
        }
    }

    public boolean isPaneEditable(int pane)
    {
        setPanes();
        ButtonGroup buttonGroup = BytecodeViewer.viewer.allPanes.get(pane);
        Decompiler selected = decompilers.get(pane);
        if (buttonGroup != null && BytecodeViewer.viewer.editButtons.get(buttonGroup) != null && BytecodeViewer.viewer.editButtons.get(buttonGroup).get(selected) != null && BytecodeViewer.viewer.editButtons.get(buttonGroup).get(selected).isSelected())
        {
            return true;
        }
        return false;
    }

    public void updatePane(int pane, RSyntaxTextArea text, Decompiler decompiler)
    {
        javas.set(pane, text);
        SearchPanel search = new SearchPanel(text);
        searches.set(pane, search);
        if (decompilers.get(pane) != Decompiler.HEXCODE)
            panels.get(pane).add(search, BorderLayout.NORTH);
    }

    /**
     * Whoever wrote this function, THANK YOU!
     *
     * @param splitter
     * @param proportion
     * @return
     */
    public static JSplitPane setDividerLocation(final JSplitPane splitter, final double proportion)
    {
        if (splitter.isShowing())
        {
            if (splitter.getWidth() > 0 && splitter.getHeight() > 0)
            {
                splitter.setDividerLocation(proportion);
            }
            else
            {
                splitter.addComponentListener(new ComponentAdapter()
                {
                    @Override
                    public void componentResized(ComponentEvent ce)
                    {
                        splitter.removeComponentListener(this);
                        setDividerLocation(splitter, proportion);
                    }
                });
            }
        }
        else
        {
            splitter.addHierarchyListener(new HierarchyListener()
            {
                @Override
                public void hierarchyChanged(HierarchyEvent e)
                {
                    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && splitter.isShowing())
                    {
                        splitter.removeHierarchyListener(this);
                        setDividerLocation(splitter, proportion);
                    }
                }
            });
        }
        return splitter;
    }

    JSplitPane sp;
    JSplitPane sp2;
    // todo: fix this dumb hack
    public List<Decompiler> decompilers = Arrays.asList(null, null, null);
    public List<JPanel> panels = Arrays.asList(new JPanel(new BorderLayout()), new JPanel(new BorderLayout()), new JPanel(new BorderLayout()));
    public List<RSyntaxTextArea> javas = Arrays.asList(null, null, null);
    public List<SearchPanel> searches = Arrays.asList(null, null, null);

    public ClassViewer(final String name, final String container, final ClassNode cn)
    {
        this.name = name;
        this.container = container;
        this.cn = cn;
        updateName();
        this.setLayout(new BorderLayout());

        this.sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panels.get(0), panels.get(1));
        this.sp2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, panels.get(2));
        this.add(sp2, BorderLayout.CENTER);

        BytecodeViewer.viewer.setIcon(true);
        startPaneUpdater(null);
        this.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                resetDivider();
            }
        });
    }

    public void resetDivider()
    {
        sp.setResizeWeight(0.5);
        if (decompilers.get(1) != null && decompilers.get(0) != null)
            sp = setDividerLocation(sp, 0.5);
        else if (decompilers.get(0) != null)
            sp = setDividerLocation(sp, 1);
        else if (decompilers.get(1) != null)
        {
            sp.setResizeWeight(1);
            sp = setDividerLocation(sp, 0);
        }
        else
            sp = setDividerLocation(sp, 0);
        if (decompilers.get(2) != null)
        {
            sp2.setResizeWeight(0.7);
            sp2 = setDividerLocation(sp2, 0.7);
            if ((decompilers.get(1) == null && decompilers.get(0) != null) || (decompilers.get(0) == null && decompilers.get(1) != null))
                sp2 = setDividerLocation(sp2, 0.5);
            else if (decompilers.get(0) == null && decompilers.get(1) == null)
                sp2 = setDividerLocation(sp2, 0);
        }
        else
        {
            sp.setResizeWeight(1);
            sp2.setResizeWeight(0);
            sp2 = setDividerLocation(sp2, 1);
        }
    }

    public void startPaneUpdater(final JButton button)
    {
        this.cn = BytecodeViewer.getClassNode(container, cn.name); //update the classnode
        setPanes();

        for (JPanel jpanel : panels)
        {
            jpanel.removeAll();
        }
        for (int i = 0; i < javas.size(); i++)
        {
            javas.set(i, null);
        }
        if (this.cn == null)
        {
            for (JPanel jpanel : panels)
            {
                jpanel.add(new JLabel("This file has been removed from the reload."));
            }
            return;
        }

        for (int i = 0; i < decompilers.size(); i++)
        {
            if (decompilers.get(i) != null)
            {
                PaneUpdaterThread t = new PaneUpdaterThread(this, decompilers.get(i), i, panels.get(i), button);
                decompileThreads.add(t);
                t.start();
            }
        }
    }

    public Object[] getJava()
    {
        for (int i = 0; i < javas.size(); i++)
        {
            RSyntaxTextArea text = javas.get(i);
            if (text != null)
            {
                return new Object[] { cn, text.getText() };
            }
        }
        return null;
    }

    public void reset()
    {
        for (Thread t : decompileThreads)
        {
            t.stop();
        }
    }
}
