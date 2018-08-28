package club.bytecode.the.jda.gui;

import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.Resources;
import club.bytecode.the.jda.api.JDAPlugin;
import club.bytecode.the.jda.decompilers.Decompilers;
import club.bytecode.the.jda.decompilers.JDADecompiler;
import club.bytecode.the.jda.gui.components.TabbedPane;
import club.bytecode.the.jda.gui.dialogs.AboutWindow;
import club.bytecode.the.jda.gui.dialogs.FontOptionsDialog;
import club.bytecode.the.jda.gui.dialogs.IntroWindow;
import club.bytecode.the.jda.gui.fileviewer.FileViewerPane;
import club.bytecode.the.jda.gui.fileviewer.Viewer;
import club.bytecode.the.jda.gui.fileviewer.ViewerFile;
import club.bytecode.the.jda.gui.navigation.FileNavigationPane;
import club.bytecode.the.jda.gui.search.SearchDialog;
import club.bytecode.the.jda.settings.IPersistentWindow;
import club.bytecode.the.jda.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * The main file for the GUI.
 *
 * @author ecx86
 * @author Konloch
 */
public class MainViewerGUI extends JFrame implements IPersistentWindow {
    public static final long serialVersionUID = 1851409230530948543L;
    private static final Color COLOR_DESKTOP_BACKGROUND = new Color(58, 110, 165);

    public static final int NUM_PANEL_GROUPS = 3;
    public final ButtonGroup[] panelGroups = new ButtonGroup[NUM_PANEL_GROUPS];

    public JMenuBar menuBar;
    public JMenu viewMenu;
    public JMenu fileMenu;
    public JMenu windowMenu;
    public JMenu editMenu;
    public JMenu optionsMenu;
    public JMenu helpMenu;
    public JMenu pluginsMenu;

    public boolean isMaximized = false;
    public Point unmaximizedPos;
    public Dimension unmaximizedSize;

    public JDesktopPane desktop;
    public FileNavigationPane navigator;
    public FileViewerPane fileViewerPane;
    public static ArrayList<JDAWindow> windows = new ArrayList<>();

    public AboutWindow aboutWindow = new AboutWindow();
    public IntroWindow introWindow = new IntroWindow();
    public List<ButtonGroup> allPanes = Collections.unmodifiableList(Arrays.asList(panelGroups));
    public Map<ButtonGroup, Map<JRadioButtonMenuItem, JDADecompiler>> allDecompilers = new HashMap<>();
    public Map<ButtonGroup, Map<JDADecompiler, JRadioButtonMenuItem>> allDecompilersRev = new HashMap<>();
    public JMenu mnRecentFiles = new JMenu("Recent Files");
    private JMenuItem spinnerMenu = new JMenuItem("");
    public FontOptionsDialog fontOptionsDialog = new FontOptionsDialog();

    public MainViewerGUI() {
        initializeWindows();

        for (int i = 0; i < panelGroups.length; i++) {
            ButtonGroup panelGroup = new ButtonGroup();
            allDecompilers.put(panelGroup, new HashMap<>());
            allDecompilersRev.put(panelGroup, new HashMap<>());
            panelGroups[i] = panelGroup;
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new JDAKeybindManager());
        this.addWindowStateListener(new WindowAdapter() {
            @Override
            public void windowStateChanged(WindowEvent evt) {
                int oldState = evt.getOldState();
                int newState = evt.getNewState();

//                if ((oldState & Frame.ICONIFIED) == 0 && (newState & Frame.ICONIFIED) != 0) {
//                    System.out.println("Frame was iconized");
//                } else if ((oldState & Frame.ICONIFIED) != 0 && (newState & Frame.ICONIFIED) == 0) {
//                    System.out.println("Frame was deiconized");
//                }

                if ((oldState & Frame.MAXIMIZED_BOTH) == 0 && (newState & Frame.MAXIMIZED_BOTH) != 0) {
                    isMaximized = true;
                    for (JDAWindow window : windows)
                        window.onJDAMaximized();
                } else if ((oldState & Frame.MAXIMIZED_BOTH) != 0 && (newState & Frame.MAXIMIZED_BOTH) == 0) {
                    setSize(unmaximizedSize);
                    setLocation(unmaximizedPos);
                    isMaximized = false;
                }
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if ((getExtendedState() & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH)
                    unmaximizedSize = getSize();
                for (JDAWindow window : windows)
                    window.onJDAResized();
                super.componentResized(e);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                if ((getExtendedState() & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH)
                    unmaximizedPos = getLocation();
                super.componentMoved(e);
            }
        });

        this.setIconImages(Resources.iconList);

        initializeMenubar();
        initializePanelGroup();

        setTitle("JDA v" + JDA.version);

        Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
        windowSize = new Dimension(windowSize.width * 3 / 4, windowSize.height * 2 / 3);
        setPreferredSize(windowSize);
        pack();
        unmaximizedSize = getSize();
        unmaximizedPos = getLocation();

        this.setLocationRelativeTo(null);

        JDA.onGUILoad();
    }

    private void initializeMenubar() {
        final JCheckBoxMenuItem refreshOnChange = new JCheckBoxMenuItem("Refresh On View Change");
        final JMenuItem mntmNewWorkspace = new JMenuItem("New Workspace");
        final JMenuItem mntmReloadResources = new JMenuItem("Reload Resources");
        final JMenuItem mntmCloseResources = new JMenuItem("Close Resources");
        final JMenuItem mntmDecompileSaveAllClasses = new JMenuItem("Decompile & Save All Classes..");
        final JMenuItem mntmAbout = new JMenuItem("About");
        final JMenuItem mntmIntro = new JMenuItem("Help");
        final JMenuItem mntmSaveAsRunnableJar = new JMenuItem("Save As Runnable Jar..");
        final JCheckBoxMenuItem mntmUpdateCheck = new JCheckBoxMenuItem("Update Check");
        final JMenuItem mntmDecompileSaveOpenedClasses = new JMenuItem("Decompile & Save Opened Class..");
        final JCheckBox mnShowContainer = new JCheckBox("Show Containing File's Name");
        final JCheckBox mnSnapToEdges = new JCheckBox("Snap Windows to Edges");
        final JMenuItem mntmSetOptionalLibrary = new JMenuItem("Set Optional Library Folder");
        final JMenuItem mntmFontSettings = new JMenuItem("Font...");

        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        viewMenu = new JMenu("View");
        windowMenu = new JMenu("Window");
        editMenu = new JMenu("Edit");
        optionsMenu = new JMenu("Settings");
        helpMenu = new JMenu("Help");
        setJMenuBar(menuBar);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // ===========================================================================================
        // File menu
        // ===========================================================================================
        menuBar.add(fileMenu);

        JMenuItem mntmLoadJar = new JMenuItem("Add..");
        mntmLoadJar.addActionListener(e -> JDA.openFileChooser());
        fileMenu.add(mntmLoadJar);

        fileMenu.add(new JSeparator());

        mntmNewWorkspace.addActionListener(arg0 -> JDA.resetWorkSpace(true));
        fileMenu.add(mntmNewWorkspace);

        mntmReloadResources.addActionListener(arg0 -> reloadResources());
        fileMenu.add(mntmReloadResources);

        mntmCloseResources.addActionListener(arg0 -> JDA.closeResources(true));
        fileMenu.add(mntmCloseResources);

        fileMenu.add(new JSeparator());

        JMenuItem mntmSaveAsZip = new JMenuItem("Save As Zip..");
        mntmSaveAsZip.setActionCommand("");
        mntmSaveAsZip.addActionListener(arg0 -> saveAsZip());
        mntmSaveAsZip.setEnabled(false);

        mntmSaveAsRunnableJar.addActionListener(e -> saveAsRunnableJar());
        mntmSaveAsRunnableJar.setEnabled(false);
        fileMenu.add(mntmSaveAsRunnableJar);

        fileMenu.add(mntmSaveAsZip);

        mntmDecompileSaveOpenedClasses.addActionListener(arg0 -> decompileSaveOpenedClasses());
        mntmDecompileSaveOpenedClasses.setEnabled(false);
        fileMenu.add(mntmDecompileSaveOpenedClasses);

        mntmDecompileSaveAllClasses.addActionListener(arg0 -> decompileSaveAllClasses());
        mntmDecompileSaveAllClasses.setEnabled(false);
        mntmDecompileSaveOpenedClasses.setEnabled(false);
        fileMenu.add(mntmDecompileSaveAllClasses);

        fileMenu.add(new JSeparator());
        fileMenu.add(mnRecentFiles);
        fileMenu.add(new JSeparator());

        JMenuItem mntmExit = new JMenuItem("Exit");
        mntmExit.addActionListener(arg0 -> exitPrompt());

        fileMenu.add(mntmExit);

        // ===========================================================================================
        // Edit menu
        // ===========================================================================================
        menuBar.add(editMenu);

        // -------------------------------------------------------------------------------------------
        // Plugins menu
        pluginsMenu = new JMenu("Plugins");
        editMenu.add(pluginsMenu);
        for (JDAPlugin plugin : JDA.getLoadedPlugins()) {
            JMenuItem button = new JMenuItem(plugin.getName());
            button.addActionListener((e) -> plugin.onPluginButton());
            pluginsMenu.add(button);
        }

        // ===========================================================================================
        // Search menu
        // ===========================================================================================
        JMenu searchMenu = new JMenu("Search");
        editMenu.add(searchMenu);
        JMenuItem constantButton = new JMenuItem("Raw constant");
        constantButton.addActionListener((e) -> doSearchDialog());
        searchMenu.add(constantButton);
        menuBar.add(searchMenu);

        
        // ===========================================================================================
        // View menu
        // ===========================================================================================
        menuBar.add(viewMenu);
        for (int i = 0; i < NUM_PANEL_GROUPS; i++)
            viewMenu.add(generatePane(i));
        
        mnShowContainer.setSelected(Settings.SHOW_CONTAINER_NAME.getBool());
        mnShowContainer.addActionListener(e -> {
            Settings.SHOW_CONTAINER_NAME.set(mnShowContainer.isSelected());
            JTabbedPane tabs = fileViewerPane.tabs;
            Component[] components = tabs.getComponents();
            for (Component c : components) {
                if (c instanceof Viewer) {
                    ((Viewer) c).updateName();
                    int idx = tabs.indexOfComponent(c);
                    tabs.setTabComponentAt(idx, new TabbedPane(c.getName(), tabs));
                    fileViewerPane.tabs.setTitleAt(idx, c.getName());
                }
            }
        });
        viewMenu.add(mnShowContainer);
        
        // ===========================================================================================
        // Windows menu
        // ===========================================================================================
        menuBar.add(windowMenu);
        for (JDAWindow frame : windows) {
            JMenuItem button = new JMenuItem(frame.getName());
            button.addActionListener(e -> {
                try {
                    frame.setIcon(false);
                    frame.setVisible(true);
                } catch (PropertyVetoException e1) {
                }
            });
            windowMenu.add(button);
        }
        windowMenu.add(new JSeparator());

        mnSnapToEdges.setSelected(Settings.SNAP_TO_EDGES.getBool());
        mnSnapToEdges.addItemListener(e -> Settings.SNAP_TO_EDGES.set(mnSnapToEdges.isSelected()));
        windowMenu.add(mnSnapToEdges);

        // ===========================================================================================
        // Options menu
        // ===========================================================================================
        optionsMenu = new JMenu("Options");
        editMenu.add(optionsMenu);

        refreshOnChange.addItemListener(e -> Settings.REFRESH_ON_VIEW_CHANGE.set(refreshOnChange.isSelected()));
        refreshOnChange.setSelected(Settings.REFRESH_ON_VIEW_CHANGE.getBool());
        optionsMenu.add(refreshOnChange);

        mntmFontSettings.addActionListener(e -> fontOptionsDialog.display());
        optionsMenu.add(mntmFontSettings);

        optionsMenu.add(new JSeparator());

        mntmSetOptionalLibrary.addActionListener(e -> setOptionalLibrary());
        optionsMenu.add(mntmSetOptionalLibrary);

        optionsMenu.add(new JSeparator());

        for (JDADecompiler decompiler : Decompilers.getAllDecompilers()) {
            JMenuItem settingsButton = new JMenuItem(decompiler.getName());
            settingsButton.addActionListener(e -> decompiler.getSettings().displayDialog());
            optionsMenu.add(settingsButton);
        }
        menuBar.add(optionsMenu);

        // ===========================================================================================
        // Help menu
        // ===========================================================================================
        menuBar.add(helpMenu);
        mntmAbout.addActionListener(arg0 -> aboutWindow.setVisible(true));
        helpMenu.add(mntmAbout);

        mntmIntro.addActionListener(arg0 -> introWindow.setVisible(true));
        helpMenu.add(mntmIntro);

        mntmUpdateCheck.setSelected(false);
        mntmUpdateCheck.setEnabled(false);
        mntmUpdateCheck.addActionListener(e -> Settings.DO_UPDATE_CHECK.set(mntmUpdateCheck.isSelected()));
        helpMenu.add(mntmUpdateCheck);
        
        // ===========================================================================================
        // Spinner (must go last)
        // ===========================================================================================
        menuBar.add(spinnerMenu);
    }

    public void doSearchDialog() {
        String constant = JOptionPane.showInputDialog("Enter a constant...");
        if (constant != null && !constant.isEmpty())
            new SearchDialog(constant, JDA.search(constant)).setVisible(true);
    }

    private void initializePanelGroup() {
        for (int i = 0; i < panelGroups.length; i++) {
            String decompilerName = Settings.PANE_DECOMPILERS[i].getString();
            panelGroups[i].setSelected(allDecompilersRev.get(panelGroups[i]).get(Decompilers.getByName(decompilerName)).getModel(), true);
        }
    }

    public static <T> T getComponent(final Class<T> clazz) {
        for (final JDAWindow vc : windows)
            if (vc.getClass() == clazz)
                return clazz.cast(vc);
        return null;
    }

    private void initializeWindows() {
        navigator = new FileNavigationPane();
        fileViewerPane = new FileViewerPane();

        desktop = new JDesktopPane();
        setContentPane(desktop);
        desktop.add(navigator);
        desktop.add(fileViewerPane);
        desktop.setDesktopManager(new WorkspaceDesktopManager());
        desktop.setBackground(COLOR_DESKTOP_BACKGROUND);

        windows.add(navigator);
        windows.add(fileViewerPane);
    }

    public void resetWindows() {
        Dimension clientSize = desktop.getSize();

        for (JDAWindow f : windows) {
            Dimension size = f.getDefaultSize();
            if (size.width < 0 || size.height < 0)
                size = new Dimension(
                        size.width < 0 ? clientSize.width + size.width : size.width,
                        size.height < 0 ? clientSize.height + size.height : size.height);
            unmaximizedSize = size;
            f.restoreState(JDAWindow.VISIBLE);
            f.restoreSize(size);
            Point pos = f.getDefaultPosition();
            f.restorePosition(pos);
            desktop.getDesktopManager().resizeFrame(f, pos.x, pos.y, size.width, size.height);
        }
    }

    public void setOptionalLibrary() {
        final JTextField text = new JTextField();
        text.setText(Settings.PATH.getString());
        final JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.add(text);
        dialog.setSize(500, 100);
        dialog.setLocationRelativeTo(JDA.viewer);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Settings.PATH.set(text.getText());
            }
        });
        dialog.setVisible(true);
    }

    private JMenu generatePane(int id) {
        JMenu menu = new JMenu("Pane " + (id + 1));
        ButtonGroup group = allPanes.get(id);

        JRadioButtonMenuItem none = new JRadioButtonMenuItem("None");
        none.addItemListener((e) -> {
            if (none.isSelected())
                Settings.PANE_DECOMPILERS[id].set("None");
        });
        allDecompilers.get(group).put(none, null);
        allDecompilersRev.get(group).put(null, none);
        group.add(none);
        menu.add(none);
        menu.add(new JSeparator());

        for (JDADecompiler decompiler : Decompilers.getAllDecompilers()) {
            JRadioButtonMenuItem button = new JRadioButtonMenuItem(decompiler.getName());
            button.addActionListener((e) -> {
                if (button.isSelected())
                    Settings.PANE_DECOMPILERS[id].set(decompiler.getFullName());
            });
            allDecompilers.get(group).put(button, decompiler);
            allDecompilersRev.get(group).put(decompiler, button);
            group.add(button);
            menu.add(button);

        }
        return menu;
    }

    public void closeResources() {
        navigator.resetWorkspace();
        fileViewerPane.resetWorkspace();
    }

    /**
     * Toggles the spinner icon on and off.
     * DON'T CALL ME DIRECTLY. CALL JDA.setBusy INSTEAD!!!!
     * @param busy whether to show the busy spinner icon or not
     */
    public void setIcon(final boolean busy) {
        SwingUtilities.invokeLater(() -> {
            if (busy) {
                try {
                    spinnerMenu.setIcon(Resources.busyIcon);
                } catch (NullPointerException e) {
                    spinnerMenu.setIcon(Resources.busyB64Icon);
                }
            } else
                spinnerMenu.setIcon(null);
            spinnerMenu.updateUI();
        });
    }

    public void calledAfterLoad() {
        resetWindows();
        Settings.loadWindows();
    }

    public void openClassFile(ViewerFile file) {
        fileViewerPane.openClassFile(file);
    }

    public void openFile(ViewerFile file) {
        fileViewerPane.openFile(file);
    }

    public void refreshView() {
        fileViewerPane.refreshClass.doClick();
    }

    public void reloadResources() {
        JOptionPane pane = new JOptionPane("Are you sure you wish to reload the resources?");
        Object[] options = new String[]{"Yes", "No"};
        pane.setOptions(options);
        JDialog dialog = pane.createDialog(JDA.viewer, "JDA - Reload Resources");
        dialog.setVisible(true);
        Object obj = pane.getValue();
        int result = -1;
        for (int k = 0; k < options.length; k++)
            if (options[k].equals(obj))
                result = k;

        if (result == 0) {
            List<File> reopenContainers = new ArrayList<>();
            for (FileContainer container : JDA.getOpenFiles())
                reopenContainers.add(container.file);

            JDA.waitForTasks();
            JDA.clearFiles();
            navigator.resetWorkspace();

            JDA.openFiles(reopenContainers.toArray(new File[reopenContainers.size()]), false);
            JDA.waitForTasks(); // this is not really ideal, but whatever.
            assert(JDA.getOpenFiles().size() > 0);
            for (Viewer v : fileViewerPane.getLoadedViewers()) {
                for (FileContainer newContainer : JDA.getOpenFiles()) {
                    if (newContainer.file.equals(v.getFile().container.file)) {
                        v.setFile(new ViewerFile(newContainer, v.getFile().name));
                        v.refresh(null);
                        break;
                    }
                }
            }
            refreshView();
        }
    }

    private void saveAsZip() {
        if (JDA.getLoadedBytes().isEmpty()) {
            JDA.showMessage("First open a class, jar, or zip file.");
            return;
        }
        (new Thread(() -> {
        })).start();
    }

    private void saveAsRunnableJar() {
        if (JDA.getLoadedBytes().isEmpty()) {
            JDA.showMessage("First open a class, jar, or zip file.");
            return;
        }
        (new Thread(() -> {
        })).start();
    }

    private void decompileSaveOpenedClasses() {
        if (fileViewerPane.getCurrentViewer() == null) {
            JDA.showMessage("First open a class, jar, or zip file.");
            return;
        }
        (new Thread(() -> {
        })).start();
    }

    private void decompileSaveAllClasses() {
        if (JDA.getOpenFiles().isEmpty()) {
            JDA.showMessage("First open a class, jar, or zip file.");
            return;
        }
        (new Thread(() -> {
        })).start();
    }

    private void exitPrompt() {
        JOptionPane pane = new JOptionPane("Are you sure you want to exit?");
        Object[] options = new String[]{"Yes", "No"};
        pane.setOptions(options);
        JDialog dialog = pane.createDialog(JDA.viewer, "JDA - Exit");
        dialog.setVisible(true);
        Object obj = pane.getValue();
        int result = -1;
        for (int k = 0; k < options.length; k++)
            if (options[k].equals(obj))
                result = k;

        if (result == 0) {
            System.exit(0);
        }
    }

    @Override
    public String getWindowId() {
        return "JDA";
    }

    @Override
    public int getState() {
        return getExtendedState();
    }

    @Override
    public void restoreState(int state) {
        setExtendedState(state);
    }

    @Override
    public Point getPersistentPosition() {
        return unmaximizedPos;
    }

    @Override
    public void restorePosition(Point pos) {
        unmaximizedPos = pos;
        if (isNormalState())
            setLocation(pos);
    }

    @Override
    public Dimension getPersistentSize() {
        return unmaximizedSize;
    }

    @Override
    public void restoreSize(Dimension size) {
        unmaximizedSize = size;
        if (isNormalState()) {
            setPreferredSize(size);
            pack();
        }
    }

    @Override
    public boolean isNormalState() {
        return (getExtendedState() & MAXIMIZED_BOTH) != MAXIMIZED_BOTH && (getExtendedState() & ICONIFIED) != ICONIFIED;
    }

    public class JDAKeybindManager implements java.awt.KeyEventDispatcher {
        private final HashMap<Integer, Boolean> keyStates = new HashMap<>();
        private long lastEventTime = System.currentTimeMillis();

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (!e.isControlDown())
                return false;

            long deltaTime = System.currentTimeMillis() - lastEventTime;
            lastEventTime = System.currentTimeMillis();
            if (deltaTime <= 5) // hack to fix repeated key events, thanks Java
                return false;

            int key = e.getKeyCode();
            synchronized (keyStates) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (!keyStates.containsKey(key) || !keyStates.get(key)) {
                        keyStates.put(key, true);
                        JDA.checkHotKey(e);
                    }
                    return false;
                } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                    keyStates.put(key, false);
                }
                return false;
            }
        }
    }
}
