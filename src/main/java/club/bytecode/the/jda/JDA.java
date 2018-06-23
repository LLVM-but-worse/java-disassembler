package club.bytecode.the.jda;

import club.bytecode.the.jda.api.ExceptionUI;
import club.bytecode.the.jda.api.JDANamespace;
import club.bytecode.the.jda.api.JDAPlugin;
import club.bytecode.the.jda.api.PluginLoader;
import club.bytecode.the.jda.gui.MainViewerGUI;
import club.bytecode.the.jda.gui.fileviewer.BytecodeFoldParser;
import club.bytecode.the.jda.gui.fileviewer.BytecodeTokenizer;
import club.bytecode.the.jda.gui.fileviewer.ViewerFile;
import club.bytecode.the.jda.gui.navigation.FileNavigationPane;
import club.bytecode.the.jda.settings.Settings;
import club.bytecode.the.jda.util.GuiUtils;
import club.bytecode.the.jda.util.MiscUtils;
import org.apache.commons.io.FileUtils;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class JDA {
    /*per version*/
    public static final String version = "1.1.0";
    /* Constants */
    public static final String fs = System.getProperty("file.separator");
    public static final String nl = System.getProperty("line.separator");
    public static final File dataDir = new File(System.getProperty("user.home") + fs + ".jda");
    public static final File pluginsDir = new File(dataDir, "plugins");
    public static final File recentsFile = new File(dataDir, "recentfiles.jda");
    public static final File settingsFile = new File(dataDir, "settings.jda");
    private static final long start = System.currentTimeMillis();
    /*the rest*/
    public static MainViewerGUI viewer = null;
    private static List<FileContainer> files = new ArrayList<>(); //all of BCV's loaded files/classes/etc
    private static int maxRecentFiles = 25;
    private static List<String> recentFiles = new ArrayList<>();
    public static String lastDirectory = "";
    public static List<Process> createdProcesses = new ArrayList<>();
    private static final AtomicInteger jobCount = new AtomicInteger(0);
    public static final JDANamespace namespace = JDADefaultNamespace.INSTANCE; 
    private static List<JDAPlugin> plugins = new ArrayList<>();
    
    public static Supplier<JDAPlugin> injectedPlugin = null; // for testing purposes only.

    /**
     * Main startup
     *
     * @param args files you want to open or CLI
     */
    public static void main(String[] args) {
        try {
            // Fix antialiasing
            System.setProperty("awt.useSystemAAFontSettings", "lcd");
            System.setProperty("swing.aatext", "true");
            GuiUtils.setWmClassName("JDA");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            new ExceptionUI(e);
        }
        try {
            System.out.println("JDA (BCV Fork) v" + version);
            getJDADirectory();

            loadPlugins();

            Settings.loadGUI();
            if (!recentsFile.exists() && !recentsFile.createNewFile())
                throw new RuntimeException("Could not create recent files file");
            recentFiles.addAll(FileUtils.readLines(recentsFile, "UTF-8"));

            viewer = new MainViewerGUI();
            Boot.boot();
            JDA.boot(args);
        } catch (Exception e) {
            new ExceptionUI(e);
        }
    }

    public static void loadPlugin(JDAPlugin plugin) {
        plugins.add(plugin);
        plugin.onLoad();
    }
    
    public static void unloadPlugin(JDAPlugin plugin) {
        plugin.onUnload();
        plugins.remove(plugin);
    }
    
    public static List<JDAPlugin> getLoadedPlugins() {
        return Collections.unmodifiableList(plugins);
    }
    
    private static void loadPlugins() throws MalformedURLException {
        if (injectedPlugin != null) {
            JDAPlugin plugin = injectedPlugin.get();
            System.out.println("Loading dependency-injected plugin " + plugin.getName());
            loadPlugin(injectedPlugin.get());
            System.out.println("Skipping other plugins.");
            return;
        }
        
        if (!pluginsDir.exists())
            if (!pluginsDir.mkdirs())
                throw new RuntimeException("Couldn't create plugins directory");
        else if (!pluginsDir.isDirectory())
            throw new RuntimeException("Plugins location is not a directory");

        for (File pluginFile : pluginsDir.listFiles()) {
            if (!pluginFile.getName().endsWith(".jar")) {
                System.out.println("Skipping non-jar " + pluginFile.getName());
                continue;
            }
            JDAPlugin pluginInstance = PluginLoader.tryLoadPlugin(pluginFile);
            if (pluginInstance != null)
                loadPlugin(pluginInstance);
        }
    }

    public static void onGUILoad() {
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
        atmf.putMapping(BytecodeTokenizer.SYNTAX_STYLE_BYTECODE, BytecodeTokenizer.class.getName());
        FoldParserManager.get().addFoldParserMapping(BytecodeTokenizer.SYNTAX_STYLE_BYTECODE, new BytecodeFoldParser());

        plugins.forEach(JDAPlugin::onGUILoad);
    }

    // todo: rewrite
    /**
     * The version checker thread
     */
    private static final Thread versionChecker = new Thread(() -> {});

    /**
     * Boot after all of the libraries have been loaded
     */
    public static void boot(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(JDA::onExit));

        resetRecentFilesMenu();

        if (Settings.DO_UPDATE_CHECK.getBool())
            versionChecker.start();

        viewer.setVisible(true);

        viewer.calledAfterLoad();

        System.out.println("Start up took " + ((System.currentTimeMillis() - start) / 1000.) + " seconds");

        if (args.length >= 1) {
            for (String s : args) {
                openFiles(new File[]{new File(s)}, true);
            }
        }
    }

    private static void onExit() {
        // unload all plugins
        plugins.forEach(JDAPlugin::onExit);
        while (!plugins.isEmpty())
            unloadPlugin(plugins.get(0));

        for (Process proc : createdProcesses)
            proc.destroy();
        try {
            FileUtils.writeLines(recentsFile, recentFiles);
        } catch (IOException e) {
            new ExceptionUI(e);
        }
        if (!viewer.isMaximized)
            viewer.unmaximizedPos = viewer.getLocation();
        Settings.saveGUI();
    }
    
    /**
     * Waits for all busy-setting tasks to complete.
     */
    public static void waitForTasks() {
        while (jobCount.get() > 0) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the busy state, and toggles the spinner icon
     * Make sure to call me OUTSIDE of your worker thread for busy=true!
     * Then, you must call again busy=false once your worker thread finishes!
     * @param busy whether a background task is running
     */
    public static void setBusy(boolean busy) {
        if (busy)
            jobCount.incrementAndGet();
        else
            jobCount.decrementAndGet();
        assert (jobCount.get() >= 0);
        viewer.setIcon(busy);
    }

    public static byte[] getFileBytes(ViewerFile file) {
        if (file.container != null)
            return file.container.getFiles().get(file.name);
        else
            return null;
    }
    
    public static boolean hasFile(ViewerFile file) {
        if (file.container == null)
            return false;
        return file.container.getFiles().containsKey(file.name);
    }
    
    public static byte[] getClassFileBytes(FileContainer container, String className) {
        byte[] bytes = getFileBytes(new ViewerFile(container, container.findClassfile(className)));
        if (bytes == null)
            return null;
        return bytes;
    }
    
    public static byte[] dumpClassToBytes(ClassNode cn) {
        // we have to do this, or else decompile filters don't work.
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cn.accept(writer);
        return writer.toByteArray();
    }

    public static final String HACK_PREFIX = "\0JDA-hack";

    public static File getClassFileProxy(ClassNode cn) {
        return new File('/' + HACK_PREFIX, cn + ".class");
    }

    public static String extractProxyClassName(String fileName) {
        return getClassFilePath(fileName.substring(fileName.indexOf(HACK_PREFIX) + HACK_PREFIX.length() + 1));
    }

    public static String getClassFilePath(String fileName) {
        return fileName.replace(File.separator, "/").substring(0, fileName.length() - ".class".length());
    }

    public static String getClassName(String fullyQualifiedName) {
        return fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('/') + 1);
    }

    // WTF????
    public static Map<String, byte[]> getLoadedBytes() {
        Map<String, byte[]> data = new HashMap<>();
        for (FileContainer container : files) {
            data.putAll(container.getFiles());
        }
        return data;
    }

    /**
     * Opens a file, optional if it should append to the recent files menu
     *
     * @param files       the file(s) you wish to open
     * @param recentFiles if it should append to the recent files menu
     */
    public static void openFiles(final File[] files, boolean recentFiles) {
        openFiles(files, recentFiles, null);
    }

    public static void openFiles(final File[] files, boolean recentFiles, FileNavigationPane.FileNode parent) {
        JDA.setBusy(true);
        
        if (recentFiles)
            for (File f : files)
                if (f.exists())
                    JDA.addRecentFile(f);

        FileNavigationPane fnp = MainViewerGUI.getComponent(FileNavigationPane.class);

        (new Thread(() -> {
            try {
                for (final File fileToOpen : files) {
                    final String fn = fileToOpen.getName();
                    if (!fileToOpen.exists()) {
                        showMessage("The file " + fileToOpen.getAbsolutePath() + " could not be found.");
                    } else if (fileToOpen.isDirectory()) {
                        FileNavigationPane.FileNode newNode = fnp.addTreeElement(new FileContainer(fileToOpen), parent);
                        openFiles(fileToOpen.listFiles(), false, newNode);
                    } else if (fn.endsWith(".jar") || fn.endsWith(".zip")) {
                        try {
                            FileContainer newContainer = JarUtils.load(fileToOpen);
                            openFile(newContainer);
                            fnp.addTreeElement(newContainer, parent);
                        } catch (final Exception e) {
                            new ExceptionUI(e);
                        }
                    } else {
                        HashMap<String, byte[]> files1 = new HashMap<>();
                        byte[] bytes = JarUtils.getBytes(new FileInputStream(fileToOpen));
                        files1.put(fileToOpen.getName(), bytes);
                        FileContainer container = new FileContainer(fileToOpen);
                        container.files = files1;
                        openFile(container);
                        fnp.addTreeElement(container, parent);
                    }
                }
            } catch (final Exception e) {
                new ExceptionUI(e);
            } finally {
                JDA.setBusy(false);
            }
        })).start();
    }
    
    public static void openFile(FileContainer fc) {
        JDA.files.add(fc);
        plugins.forEach((plugin -> plugin.onOpenFile(fc)));
    }
    
    public static void closeFile(FileContainer fc) {
        JDA.files.remove(fc);
        plugins.forEach(plugin -> plugin.onCloseFile(fc));
    }
    
    public static void clearFiles() {
        while (!JDA.files.isEmpty()) {
            JDA.closeFile(JDA.files.get(0));
        }
    }
    
    public static List<FileContainer> getOpenFiles() {
        return Collections.unmodifiableList(files);
    }

    /**
     * Send a message to alert the user
     *
     * @param message the message you need to send
     */
    public static void showMessage(String message) {
        JOptionPane.showMessageDialog(viewer, message);
    }

    /**
     * Resets the workspace with optional user input required
     *
     * @param ask if should require user input or not
     */
    public static void resetWorkSpace(boolean ask) {
        if (ask) {
            JOptionPane pane = new JOptionPane("Are you sure you want to reset the workspace?\n\rIt will also reset your file navigator and search.");
            Object[] options = new String[]{"Yes", "No"};
            pane.setOptions(options);
            JDialog dialog = pane.createDialog(viewer, "JDA - Reset Workspace");
            dialog.setVisible(true);
            Object obj = pane.getValue();
            for (int k = 0; k < options.length; k++)
                if (options[k].equals(obj) && k != 0)
                    return;
        }

        closeResources(false);
        viewer.resetWindows();
    }

    public static void closeResources(boolean ask) {
        if (ask) {
            JOptionPane pane = new JOptionPane("Are you sure you want to close all resources?");
            Object[] options = new String[]{"Yes", "No"};
            pane.setOptions(options);
            JDialog dialog = pane.createDialog(viewer, "JDA - Close Resources");
            dialog.setVisible(true);
            Object obj = pane.getValue();
            for (int k = 0; k < options.length; k++)
                if (options[k].equals(obj) && k != 0)
                    return;
        }

        JDA.setBusy(true);
        clearFiles();
        viewer.closeResources();
        JDA.setBusy(false);
    }

    private static ArrayList<String> killList = new ArrayList<>();

    /**
     * Add the recent file
     *
     * @param f the recent file
     */
    public static void addRecentFile(File f) {
        for (int i = 0; i < recentFiles.size(); i++) { // remove dead strings
            String s = recentFiles.get(i);
            if (s.isEmpty() || i > maxRecentFiles)
                killList.add(s);
        }
        if (!killList.isEmpty()) {
            for (String s : killList)
                recentFiles.remove(s);
            killList.clear();
        }

        if (recentFiles.contains(f.getAbsolutePath())) // already added on the list
            recentFiles.remove(f.getAbsolutePath());
        if (recentFiles.size() >= maxRecentFiles)
            recentFiles.remove(maxRecentFiles - 1); // zero indexing

        recentFiles.add(0, f.getAbsolutePath());
        resetRecentFilesMenu();
    }

    private static ArrayList<String> killList2 = new ArrayList<>();

    /**
     * resets the recent files menu
     */
    public static void resetRecentFilesMenu() {
        viewer.mnRecentFiles.removeAll();
        for (String s : recentFiles)
            if (!s.isEmpty()) {
                JMenuItem m = new JMenuItem(s);
                m.addActionListener(e -> {
                    JMenuItem m1 = (JMenuItem) e.getSource();
                    openFiles(new File[]{new File(m1.getText())}, true);
                });
                viewer.mnRecentFiles.add(m);
            }
    }

    public static ArrayList<String> createdRandomizedNames = new ArrayList<>();

    /**
     * Ensures it will only return a uniquely generated names, contains a dupe checker to be sure
     *
     * @return the unique randomized name of 25 characters.
     */
    public static String getRandomizedName() {
        boolean generated = false;
        String name = "";
        while (!generated) {
            String randomizedName = MiscUtils.randomString(25);
            if (!createdRandomizedNames.contains(randomizedName)) {
                createdRandomizedNames.add(randomizedName);
                name = randomizedName;
                generated = true;
            }
        }
        return name;
    }

    /**
     * Returns the BCV directory
     *
     * @return the static BCV directory
     */
    public static String getJDADirectory() {
        while (!dataDir.exists())
            dataDir.mkdirs();

        if (!dataDir.isHidden() && isWindows())
            hideFile(dataDir);

        return dataDir.getAbsolutePath();
    }

    /**
     * Checks if the OS contains 'win'
     *
     * @return true if the os.name property contains 'win'
     */
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Runs the windows command to hide files
     *
     * @param f file you want hidden
     */
    private static void hideFile(File f) {
        try {
            // Hide file by running attrib system command (on Windows)
            Runtime.getRuntime().exec("attrib +H " + f.getAbsolutePath());
        } catch (Exception e) {
            new ExceptionUI(e);
        }
    }

    private static boolean isCtrlDown(KeyEvent e) {
        return ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0);
    }

    private static boolean isShiftDown(KeyEvent e) {
        return ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0);
    }

    /**
     * Checks the hotkeys
     *
     * @param e
     */
    public static void checkHotKey(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_O) && isCtrlDown(e)) {
            openFileChooser();
        } else if ((e.getKeyCode() == KeyEvent.VK_N) && isCtrlDown(e)) {
            JDA.resetWorkSpace(true);
        } else if ((e.getKeyCode() == KeyEvent.VK_R) && isCtrlDown(e) && isShiftDown(e)) {
            viewer.reloadResources();
        } else if ((e.getKeyCode() == KeyEvent.VK_R) && isCtrlDown(e)) {
            viewer.refreshView();
        } else if ((e.getKeyCode() == KeyEvent.VK_W) && isCtrlDown(e) && isShiftDown(e)) {
            JDA.closeResources(true);
        } else if ((e.getKeyCode() == KeyEvent.VK_W) && isCtrlDown(e)) {
            if (viewer.fileViewerPane.getCurrentViewer() != null)
                viewer.fileViewerPane.tabs.remove(viewer.fileViewerPane.getCurrentViewer());
        }
    }

    public static void openFileChooser() {
        JFileChooser fc = new JFileChooser();
        try {
            File f = new File(JDA.lastDirectory);
            if (f.exists())
                fc.setSelectedFile(f);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        fc.setFileFilter(new JavaFileFilter());
        fc.setFileHidingEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showOpenDialog(JDA.viewer);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            JDA.lastDirectory = fc.getSelectedFile().getAbsolutePath();
            try {
                JDA.setBusy(true);
                JDA.openFiles(new File[]{fc.getSelectedFile()}, true);
                JDA.setBusy(false);
            } catch (Exception e1) {
                new ExceptionUI(e1);
            }
        }
    }

    public static class JavaFileFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory())
                return true;

            String extension = MiscUtils.extension(f.getAbsolutePath());
            if (extension.equals("jar") || extension.equals("zip") || extension.equals("class"))
                return true;

            return false;
        }

        @Override
        public String getDescription() {
            return "Class Files or Zip/Jar Archives";
        }
    }
}
