package the.bytecode.club.jda.gui;

import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.jda.*;
import the.bytecode.club.jda.api.ExceptionUI;
import the.bytecode.club.jda.decompilers.Decompiler;
import the.bytecode.club.jda.decompilers.Decompilers;
import the.bytecode.club.jda.gui.dialogs.AboutWindow;
import the.bytecode.club.jda.gui.dialogs.FontOptionsDialog;
import the.bytecode.club.jda.gui.dialogs.IntroWindow;
import the.bytecode.club.jda.gui.dialogs.TabbedPane;
import the.bytecode.club.jda.gui.fileviewer.FileViewerPane;
import the.bytecode.club.jda.gui.fileviewer.Viewer;
import the.bytecode.club.jda.gui.navigation.FileNavigationPane;
import the.bytecode.club.jda.settings.IPersistentWindow;
import the.bytecode.club.jda.settings.Settings;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * The main file for the GUI.n
 *
 * @author Konloch
 */
public class MainViewerGUI extends JFrame implements FileChangeNotifier, IPersistentWindow {
    public static final long serialVersionUID = 1851409230530948543L;
    private static final Color COLOR_DESKTOP_BACKGROUND = new Color(58, 110, 165);

    public final ButtonGroup panelGroup1 = new ButtonGroup();
    public final ButtonGroup panelGroup2 = new ButtonGroup();
    public final ButtonGroup panelGroup3 = new ButtonGroup();

    public JMenuBar menuBar;
    public JMenu viewMenu;
    public JMenu fileMenu;
    public JMenu windowMenu;
    public JMenu settingsMenu;
    public JMenu helpMenu;

    public boolean isMaximized = false;
    public Point unmaximizedPos;
    public Dimension unmaximizedSize;

    public JDesktopPane desktop;
    public FileNavigationPane navigator;
    public FileViewerPane FileViewerPane;
    public static ArrayList<JDAWindow> windows = new ArrayList<>();
    private final ActionListener listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (Settings.REFRESH_ON_VIEW_CHANGE.getBool()) {
                if (FileViewerPane.getCurrentViewer() == null)
                    return;
                FileViewerPane.refreshClass.doClick();
            }
        }
    };
    public AboutWindow aboutWindow = new AboutWindow();
    public IntroWindow introWindow = new IntroWindow();
    public List<ButtonGroup> allPanes = Collections.unmodifiableList(Arrays.asList(panelGroup1, panelGroup2, panelGroup3));
    public Map<ButtonGroup, Map<JRadioButtonMenuItem, Decompiler>> allDecompilers = new HashMap<>();
    public Map<ButtonGroup, Map<Decompiler, JRadioButtonMenuItem>> allDecompilersRev = new HashMap<>();
    public Map<ButtonGroup, Map<Decompiler, JCheckBoxMenuItem>> editButtons = new HashMap<>();
    public JMenu mnRecentFiles = new JMenu("Recent Files");
    private JMenuItem spinnerMenu = new JMenuItem("");
    public FontOptionsDialog fontOptionsDialog = new FontOptionsDialog();

    public MainViewerGUI() {
        initializeWindows();

        Decompiler.ensureInitted();
        allDecompilers.put(panelGroup1, new HashMap<>());
        allDecompilers.put(panelGroup2, new HashMap<>());
        allDecompilers.put(panelGroup3, new HashMap<>());
        allDecompilersRev.put(panelGroup1, new HashMap<>());
        allDecompilersRev.put(panelGroup2, new HashMap<>());
        allDecompilersRev.put(panelGroup3, new HashMap<>());
        editButtons.put(panelGroup1, new HashMap<>());
        editButtons.put(panelGroup2, new HashMap<>());
        editButtons.put(panelGroup3, new HashMap<>());
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new JDAKeybindManager());
        this.addWindowStateListener(new WindowAdapter() {
            @Override
            public void windowStateChanged(WindowEvent evt) {
                int oldState = evt.getOldState();
                int newState = evt.getNewState();

                if ((oldState & Frame.ICONIFIED) == 0 && (newState & Frame.ICONIFIED) != 0) {
                    //System.out.println("Frame was iconized");
                } else if ((oldState & Frame.ICONIFIED) != 0 && (newState & Frame.ICONIFIED) == 0) {
                    //System.out.println("Frame was deiconized");
                }

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

        if (JDA.previewCopy)
            setTitle("JDA v" + JDA.version + " Preview");
        else
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
        settingsMenu = new JMenu("Settings");
        helpMenu = new JMenu("Help");
        setJMenuBar(menuBar);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        menuBar.add(fileMenu);

        JMenuItem mntmLoadJar = new JMenuItem("Add..");
        mntmLoadJar.addActionListener(e -> addFile());
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

        menuBar.add(viewMenu);
        viewMenu.add(generatePane(0));
        viewMenu.add(generatePane(1));
        viewMenu.add(generatePane(2));

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

        menuBar.add(windowMenu);

        refreshOnChange.addItemListener(e -> Settings.REFRESH_ON_VIEW_CHANGE.set(refreshOnChange.isSelected()));
        refreshOnChange.setSelected(Settings.REFRESH_ON_VIEW_CHANGE.getBool());
        settingsMenu.add(refreshOnChange);

        settingsMenu.add(new JSeparator());

        mntmSetOptionalLibrary.addActionListener(e -> setOptionalLibrary());

        settingsMenu.add(mntmSetOptionalLibrary);

        settingsMenu.add(new JSeparator());

        for (Decompiler decompiler : Decompilers.getAllDecompilers()) {
            JMenuItem settingsButton = new JMenuItem(decompiler.getName());
            settingsButton.addActionListener(e -> decompiler.getSettings().displayDialog());
            settingsMenu.add(settingsButton);
        }

        menuBar.add(settingsMenu);

        mntmAbout.addActionListener(arg0 -> aboutWindow.setVisible(true));
        helpMenu.add(mntmAbout);

        mntmIntro.addActionListener(arg0 -> introWindow.setVisible(true));
        helpMenu.add(mntmIntro);

        mntmUpdateCheck.setSelected(Settings.DO_UPDATE_CHECK.getBool());
        mntmUpdateCheck.addActionListener(e -> Settings.DO_UPDATE_CHECK.set(mntmUpdateCheck.isSelected()));
        helpMenu.add(mntmUpdateCheck);
        menuBar.add(helpMenu);

        menuBar.add(spinnerMenu);

        mntmFontSettings.addActionListener(e -> fontOptionsDialog.display());
        viewMenu.add(mntmFontSettings);

        mnShowContainer.setSelected(Settings.SHOW_CONTAINER_NAME.getBool());
        mnShowContainer.addItemListener(e -> {
            JTabbedPane tabs = FileViewerPane.tabs;
            Component[] components = tabs.getComponents();
            for (int i = 0; i < components.length; i++) {
                Component c = components[i];
                if (c instanceof Viewer) {
                    ((Viewer) c).updateName();
                    int idx = tabs.indexOfComponent(c);
                    tabs.setTabComponentAt(idx, new TabbedPane(c.getName(), tabs));
                    FileViewerPane.tabs.setTitleAt(idx, c.getName());
                }
            }
            Settings.SHOW_CONTAINER_NAME.set(mnShowContainer.isSelected());
        });
        viewMenu.add(mnShowContainer);

        panelGroup1.setSelected(allDecompilersRev.get(panelGroup1).get(Decompilers.FERNFLOWER).getModel(), true);
        panelGroup2.setSelected(allDecompilersRev.get(panelGroup2).get(Decompilers.BYTECODE).getModel(), true);
        panelGroup3.setSelected(allDecompilersRev.get(panelGroup3).get(null).getModel(), true);
    }

    public static <T> T getComponent(final Class<T> clazz) {
        for (final JDAWindow vc : windows)
            if (vc.getClass() == clazz)
                return clazz.cast(vc);
        return null;
    }

    private void initializeWindows() {
        navigator = new FileNavigationPane(this);
        FileViewerPane = new FileViewerPane(this);

        desktop = new JDesktopPane();
        setContentPane(desktop);
        desktop.add(navigator);
        desktop.add(FileViewerPane);
        desktop.setDesktopManager(new WorkspaceDesktopManager());
        desktop.setBackground(COLOR_DESKTOP_BACKGROUND);

        windows.add(navigator);
        windows.add(FileViewerPane);
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
        text.setText(Settings.PATH.get());
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

//    private JMenu generateDecompilerMenu(Decompiler decompiler, int panelId) {
//        ButtonGroup group = allPanes.get(panelId);
//        JMenu menu = new JMenu(decompiler.getName());
//        JRadioButtonMenuItem java = new JRadioButtonMenuItem("Java");
//        java.addActionListener(listener);
//        JRadioButtonMenuItem bytecode = new JRadioButtonMenuItem("Bytecode");
//        JCheckBoxMenuItem editable = new JCheckBoxMenuItem("Editable");
//        JSeparator separator = new JSeparator();
//        menu.add(java);
//        group.add(java);
//        allDecompilers.get(group).put(java, decompiler);
//        allDecompilersRev.get(group).put(decompiler, java);
//        menu.add(separator);
//        menu.add(editable);
//        editButtons.get(group).put(decompiler, editable);
//        return menu;
//    }

    private JMenu generatePane(int id) {
        JMenu menu = new JMenu("Pane " + (id + 1));
        ButtonGroup group = allPanes.get(id);

        JRadioButtonMenuItem none = new JRadioButtonMenuItem("None");
        allDecompilers.get(group).put(none, null);
        allDecompilersRev.get(group).put(null, none);
        group.add(none);
        menu.add(none);
        menu.add(new JSeparator());

        for (Decompiler decompiler : Decompilers.getAllDecompilers()) {
            JRadioButtonMenuItem button = new JRadioButtonMenuItem(decompiler.getName());
            allDecompilers.get(group).put(button, decompiler);
            allDecompilersRev.get(group).put(decompiler, button);
            group.add(button);
            menu.add(button);

        }
        return menu;
    }

    public void closeResources() {
        navigator.resetWorkspace();
        FileViewerPane.resetWorkspace();
    }

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

    @Override
    public void openClassFile(final String name, String container, final ClassNode cn) {
        for (final JDAWindow vc : windows)
            vc.openClassFile(name, container, cn);
    }

    @Override
    public void openFile(final String name, String container, byte[] content) {
        for (final JDAWindow vc : windows)
            vc.openFile(name, container, content);
    }

    public void refreshView() {
        FileViewerPane.refreshClass.doClick();
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
            ArrayList<File> reopen = new ArrayList<>();
            for (FileContainer container : JDA.files)
                reopen.add(container.file);

            JDA.files.clear();
            JDA.openFiles(reopen.toArray(new File[reopen.size()]), false);

            refreshView();
        }
    }

    private void addFile() {
        JFileChooser fc = new JFileChooser();
        try {
            File f = new File(JDA.lastDirectory);
            if (f.exists())
                fc.setSelectedFile(f);
        } catch (Exception e2) {

        }
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;

                String extension = MiscUtils.extension(f.getAbsolutePath());
                if (extension != null)
                    if (extension.equals("jar") || extension.equals("zip") || extension.equals("class"))
                        return true;

                return false;
            }

            @Override
            public String getDescription() {
                return "Class Files or Zip/Jar Archives";
            }
        });
        fc.setFileHidingEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showOpenDialog(JDA.viewer);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            JDA.lastDirectory = fc.getSelectedFile().getAbsolutePath();
            try {
                JDA.viewer.setIcon(true);
                JDA.openFiles(new File[]{fc.getSelectedFile()}, true);
                JDA.viewer.setIcon(false);
            } catch (Exception e1) {
                new ExceptionUI(e1);
            }
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
        if (FileViewerPane.getCurrentViewer() == null) {
            JDA.showMessage("First open a class, jar, or zip file.");
            return;
        }
        (new Thread(() -> {
        })).start();
    }

    private void decompileSaveAllClasses() {
        if (JDA.files.isEmpty()) {
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
