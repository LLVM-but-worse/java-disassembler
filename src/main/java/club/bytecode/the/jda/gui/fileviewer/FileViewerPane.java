package club.bytecode.the.jda.gui.fileviewer;

import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.Resources;
import club.bytecode.the.jda.gui.JDAWindow;
import club.bytecode.the.jda.gui.components.TabbedPane;
import club.bytecode.the.jda.gui.navigation.FileNavigationPane;
import org.mapleir.stdlib.util.IndexedList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * The pane that contains all of the classes as tabs.
 *
 * @author Konloch
 * @author WaterWolf
 */

public class FileViewerPane extends JDAWindow {

    private static final long serialVersionUID = 6542337997679487946L;

    public JTabbedPane tabs;

    JPanel buttonPanel;
    public JButton refreshClass;

    List<ViewerFile> workingOn = new IndexedList<>();

    public FileViewerPane() {
        super("WorkPanel", "Work Space", Resources.fileNavigatorIcon);

        this.tabs = new JTabbedPane();

        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(tabs, BorderLayout.CENTER);

        buttonPanel = new JPanel(new FlowLayout());

        refreshClass = new JButton("Refresh");
        refreshClass.addActionListener(e -> (new Thread(() -> {
            final Component tabComp = tabs.getSelectedComponent();
            if (tabComp != null) {
                assert(tabComp instanceof Viewer);
                Viewer viewer = (Viewer) tabComp;
                JDA.setBusy(true);
                viewer.refresh(refreshClass);
                JDA.setBusy(false);
            }
        })).start());

        buttonPanel.add(refreshClass);

        buttonPanel.setVisible(false);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        tabs.addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(final ContainerEvent e) {
            }

            @Override
            public void componentRemoved(final ContainerEvent e) {
                final Component c = e.getChild();
                if (c instanceof Viewer) {
                    Viewer v = (Viewer) c;
                    workingOn.remove(v.getFile());
                }
            }

        });
        tabs.addChangeListener(arg0 -> buttonPanel.setVisible(tabs.getSelectedIndex() != -1));

        this.setVisible(true);

    }
    
    public static Dimension defaultDimension = new Dimension(-FileNavigationPane.defaultDimension.width, -35);
    public static Point defaultPosition = new Point(FileNavigationPane.defaultDimension.width, 0);

    @Override
    public Dimension getDefaultSize() {
        return defaultDimension;
    }

    @Override
    public Point getDefaultPosition() {
        return defaultPosition;
    }

    private void openFile(ViewerFile file, Supplier<Viewer> viewerFactory) {
        if (!workingOn.contains(file)) {
            final JPanel tabComp = viewerFactory.get();
            tabs.add(tabComp);
            final int tabCount = tabs.indexOfComponent(tabComp);
            workingOn.add(tabCount, file);
            tabs.setTabComponentAt(tabCount, new TabbedPane(file.name, tabs));
            tabs.setSelectedIndex(tabCount);
        } else {
            tabs.setSelectedIndex(workingOn.indexOf(file));
        }
    }

    public void openFile(ViewerFile file) {
        openFile(file, () -> new FileViewer(file));
    }

    public void openClassFile(ViewerFile file) {
        openFile(file, () -> new ClassViewer(file));
    }

    public Viewer getCurrentViewer() {
        return (Viewer) tabs.getSelectedComponent();
    }
    
    public List<Viewer> getLoadedViewers() {
        ArrayList<Viewer> result = new ArrayList<>();
        for (Component c : tabs.getComponents()) {
            if (c instanceof Viewer)
                result.add((Viewer) c);
        }
        return result;
    }

    /**
     * @return a copy of the files currently open
     */
    public List<ViewerFile> getOpenFiles() {
        return Collections.unmodifiableList(workingOn);
    }
    
    public void resetWorkspace() {
        for (Component component : tabs.getComponents()) {
            if (component instanceof ClassViewer)
                ((ClassViewer) component).reset();
        }
        tabs.removeAll();
        tabs.updateUI();
    }

}
