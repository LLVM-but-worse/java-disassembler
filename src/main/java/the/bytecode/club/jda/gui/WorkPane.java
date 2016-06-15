package the.bytecode.club.jda.gui;

import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.jda.FileChangeNotifier;
import the.bytecode.club.jda.JDA;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.HashMap;

/**
 * The pane that contains all of the classes as tabs.
 *
 * @author Konloch
 * @author WaterWolf
 */

public class WorkPane extends VisibleComponent implements ActionListener
{

    private static final long serialVersionUID = 6542337997679487946L;

    FileChangeNotifier fcn;
    public JTabbedPane tabs;

    JPanel buttonPanel;
    JButton refreshClass;

    HashMap<String, Integer> workingOn = new HashMap<>();

    public static int SyntaxFontHeight = 12;

    public WorkPane(final FileChangeNotifier fcn)
    {
        super("WorkPanel");
        setTitle("Work Space");

        this.tabs = new JTabbedPane();
        this.fcn = fcn;

        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(tabs, BorderLayout.CENTER);

        buttonPanel = new JPanel(new FlowLayout());

        refreshClass = new JButton("Refresh");
        refreshClass.addActionListener(this);

        buttonPanel.add(refreshClass);

        buttonPanel.setVisible(false);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        tabs.addContainerListener(new ContainerListener()
        {

            @Override
            public void componentAdded(final ContainerEvent e)
            {
            }

            @Override
            public void componentRemoved(final ContainerEvent e)
            {
                final Component c = e.getChild();
                if (c instanceof ClassViewer)
                {
                    ClassViewer cv = (ClassViewer) c;
                    workingOn.remove(cv.container + "$" + cv.name);
                }
                if (c instanceof FileViewer)
                {
                    FileViewer fv = (FileViewer) c;
                    workingOn.remove(fv.container + "$" + fv.name);
                }
            }

        });
        tabs.addChangeListener(arg0 -> buttonPanel.setVisible(tabs.getSelectedIndex() != -1));

        this.setVisible(true);

    }

    int tabCount = 0;

    public void addWorkingFile(final String name, String container, final ClassNode cn)
    {
        String key = container + "$" + name;
        if (!workingOn.containsKey(key))
        {
            final JPanel tabComp = new ClassViewer(name, container, cn);
            tabs.add(tabComp);
            final int tabCount = tabs.indexOfComponent(tabComp);
            workingOn.put(key, tabCount);
            tabs.setTabComponentAt(tabCount, new TabbedPane(name, tabs));
            tabs.setSelectedIndex(tabCount);
        }
        else
        {
            tabs.setSelectedIndex(workingOn.get(key));
        }
    }

    public void addFile(final String name, String container, byte[] contents)
    {
        if (contents == null) //a directory
            return;

        String key = container + "$" + name;
        if (!workingOn.containsKey(key))
        {
            final Component tabComp = new FileViewer(name, container, contents);
            tabs.add(tabComp);
            final int tabCount = tabs.indexOfComponent(tabComp);
            workingOn.put(key, tabCount);
            tabs.setTabComponentAt(tabCount, new TabbedPane(name, tabs));
            tabs.setSelectedIndex(tabCount);
        }
        else
        {
            tabs.setSelectedIndex(workingOn.get(key));
        }
    }

    @Override
    public void openClassFile(final String name, String container, final ClassNode cn)
    {
        addWorkingFile(name, container, cn);
    }

    @Override
    public void openFile(final String name, String container, byte[] content)
    {
        addFile(name, container, content);
    }

    public Viewer getCurrentViewer()
    {
        return (Viewer) tabs.getSelectedComponent();
    }

    public java.awt.Component[] getLoadedViewers()
    {
        return tabs.getComponents();
    }

    @Override
    public void actionPerformed(final ActionEvent arg0)
    {
        Thread t = new Thread()
        {
            public void run()
            {
                final JButton src = (JButton) arg0.getSource();
                if (src == refreshClass)
                {
                    final Component tabComp = tabs.getSelectedComponent();
                    if (tabComp != null)
                    {
                        if (tabComp instanceof ClassViewer)
                        {
                            JDA.viewer.setIcon(true);
                            ((ClassViewer) tabComp).startPaneUpdater(src);
                            JDA.viewer.setIcon(false);
                        }
                        else if (tabComp instanceof FileViewer)
                        {
                            src.setEnabled(false);
                            JDA.viewer.setIcon(true);
                            ((FileViewer) tabComp).refresh(src);
                            JDA.viewer.setIcon(false);
                        }
                    }
                }
            }
        };
        t.start();
    }

    public void resetWorkspace()
    {
        for (Component component : tabs.getComponents())
        {
            if (component instanceof ClassViewer)
                ((ClassViewer) component).reset();
        }
        tabs.removeAll();
        tabs.updateUI();
    }

}
