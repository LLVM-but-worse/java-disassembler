package club.bytecode.the.jda;

import club.bytecode.the.jda.api.ExceptionUI;
import club.bytecode.the.jda.api.Plugin;
import club.bytecode.the.jda.api.PluginLoader;
import club.bytecode.the.jda.gui.MainViewerGUI;
import club.bytecode.the.jda.gui.fileviewer.BytecodeFoldParser;
import club.bytecode.the.jda.gui.fileviewer.BytecodeTokenizer;
import club.bytecode.the.jda.gui.navigation.FileNavigationPane;
import club.bytecode.the.jda.settings.Settings;
import org.apache.commons.io.FileUtils;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class JDA {
    /*per version*/
    public static final String version = "0.0.7";
    public static final boolean previewCopy = false;
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
    public static List<FileContainer> files = new ArrayList<>(); //all of BCV's loaded files/classes/etc
    private static int maxRecentFiles = 25;
    private static List<String> recentFiles = new ArrayList<>();
    public static String lastDirectory = "";
    public static List<Process> createdProcesses = new ArrayList<>();
    public static List<Plugin> plugins = new ArrayList<>();

    /**
     * Main startup
     *
     * @param args files you want to open or CLI
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            new ExceptionUI(e);
        }
        try {
            System.out.println("JDA (BCV Fork) v" + version);
            if (previewCopy)
                showMessage("WARNING: This is a preview/dev copy, you WON'T be alerted when " + version + " is actually out if you use this." + nl +
                        "Make sure to watch the repo: https://github.com/ecx86/jda for " + version + "'s release");
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

    private static void loadPlugins() throws MalformedURLException {
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
            Plugin pluginInstance = PluginLoader.tryLoadPlugin(pluginFile);
            if (pluginInstance != null)
                plugins.add(pluginInstance);
        }
    }

    public static void onGUILoad() {
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
        atmf.putMapping(BytecodeTokenizer.SYNTAX_STYLE_BYTECODE, BytecodeTokenizer.class.getName());
        FoldParserManager.get().addFoldParserMapping(BytecodeTokenizer.SYNTAX_STYLE_BYTECODE, new BytecodeFoldParser());

        plugins.forEach(Plugin::onGUILoad);
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

        if (args.length >= 1)
            for (String s : args) {
                openFiles(new File[]{new File(s)}, true);
            }
    }

    private static void onExit() {
        plugins.forEach(Plugin::onExit);

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
     * Returns the currently opened ClassNode
     *
     * @return the currently opened ClassNode
     */
    public static ClassNode getCurrentlyOpenedClassNode() {
        return viewer.FileViewerPane.getCurrentViewer().cn;
    }

    public static byte[] getFileBytes(FileContainer container, String name) {
        if (container != null)
            return container.getData().get(name);
        else
            return null;
    }

    public static byte[] getClassBytes(FileContainer container, ClassNode cn) {
        byte[] bytes = getFileBytes(container, getClassfileName(cn));
        if (bytes == null)
            return null;
        if (cn.version < 49)
            bytes = fixBytes(bytes); // this is inefficient!
        return bytes;
    }

    public static final String HACK_PREFIX = "\0JDA-hack";

    public static File getClassFileProxy(ClassNode cn) {
        return new File('/' + HACK_PREFIX, getClassfileName(cn));
    }

    public static String getClassfileName(ClassNode cn) {
        return cn.name + ".class";
    }

    public static String extractProxyClassName(String fileName) {
        return extractClassName(fileName.substring(fileName.indexOf(HACK_PREFIX) + HACK_PREFIX.length() + 1));
    }

    public static String extractClassName(String fileName) {
        return fileName.replace(File.separator, "/").substring(0, fileName.length() - 6);
    }

    protected static byte[] fixBytes(byte[] in) {
        ClassReader reader = new ClassReader(in);
        ClassNode node = new ClassNode();
        reader.accept(node, ClassReader.EXPAND_FRAMES);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    /**
     * Replaces an old node with a new instance
     *
     * @param oldNode the old instance
     * @param newNode the new instance
     */
    public static void updateNode(ClassNode oldNode, ClassNode newNode) {
        for (FileContainer container : files) {
            if (container.remove(oldNode))
                container.add(newNode);
        }
    }

    /**
     * Gets all of the loaded classes as an array list
     *
     * @return the loaded classes as an array list
     */
    public static ArrayList<ClassNode> getLoadedClasses() {
        ArrayList<ClassNode> a = new ArrayList<>();

        for (FileContainer container : files)
            for (ClassNode c : container.getClasses())
                if (!a.contains(c))
                    a.add(c);

        return a;
    }

    public static ArrayList<ClassNode> loadAllClasses() {
        ArrayList<ClassNode> a = new ArrayList<>();
        for (FileContainer container : files) {
            for (String s : container.files.keySet()) {
                if (!s.endsWith(".class"))
                    continue;
                ClassNode loaded = container.getClassNode(s);
                if (loaded != null) {
                    a.add(loaded);
                }
            }
        }

        return a;
    }

    public static Map<String, byte[]> getLoadedBytes() {
        Map<String, byte[]> data = new HashMap<>();
        for (FileContainer container : files) {
            data.putAll(container.getData());
        }
        return data;
    }

    private static boolean update = true;

    /**
     * Opens a file, optional if it should append to the recent files menu
     *
     * @param files       the file(s) you wish to open
     * @param recentFiles if it should append to the recent files menu
     */
    public static void openFiles(final File[] files, boolean recentFiles) {
        if (recentFiles)
            for (File f : files)
                if (f.exists())
                    JDA.addRecentFile(f);

        JDA.viewer.setIcon(true);
        update = true;

        (new Thread(() -> {
            try {
                for (final File fileToOpen : files) {
                    final String fn = fileToOpen.getName();
                    if (!fileToOpen.exists()) {
                        update = false;
                        showMessage("The file " + fileToOpen.getAbsolutePath() + " could not be found.");
                    } else if (fileToOpen.isDirectory()) {
                        FileContainer container = new FileContainer(fileToOpen);
                        HashMap<String, byte[]> openedFiles = new HashMap<>();
                        Set<File> totalFiles = new HashSet<>();
                        Deque<File> queue = new ArrayDeque<>();
                        queue.add(fileToOpen);
                        String dir = fileToOpen.getAbsolutePath();//f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-f.getName().length());

                        while (!queue.isEmpty()) {
                            File file = queue.remove();
                            if (!totalFiles.add(file))
                                continue;
                            if (file.listFiles() != null) { // is directory
                                for (File child : file.listFiles()) {
                                    if (!totalFiles.contains(child)) {
                                        queue.add(child);
                                    }
                                }
                            }
                        }

                        for (File file : totalFiles) {
                            if (file.isFile()) {
                                String fileName = file.getAbsolutePath().substring(dir.length() + 1, file.getAbsolutePath().length()).replaceAll("\\\\", "\\/");
                                openedFiles.put(fileName, Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                            }
                        }

                        container.files = openedFiles;
                        addFile(container);
                    } else if (fn.endsWith(".jar") || fn.endsWith(".zip")) {
                        try {
                            JarUtils.put(fileToOpen);
                        } catch (final Exception e) {
                            new ExceptionUI(e);
                            update = false;
                        }
                    } else if (fn.endsWith(".class")) {
                        try {
                            byte[] bytes = JarUtils.getBytes(new FileInputStream(fileToOpen));
                            String cafebabe = String.format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]);
                            if (cafebabe.toLowerCase().equals("cafebabe")) {
                                final ClassNode cn = JarUtils.getNode(bytes);

                                FileContainer container = new FileContainer(fileToOpen);
                                container.files.put(getClassfileName(cn), bytes);
                                container.add(cn);
                                addFile(container);
                            } else {
                                showMessage(fn + ": Header does not start with CAFEBABE, ignoring.");
                                update = false;
                            }
                        } catch (final Exception e) {
                            new ExceptionUI(e);
                            update = false;
                        }
                    } else {
                        HashMap<String, byte[]> files1 = new HashMap<>();
                        byte[] bytes = JarUtils.getBytes(new FileInputStream(fileToOpen));
                        files1.put(fileToOpen.getName(), bytes);


                        FileContainer container = new FileContainer(fileToOpen);
                        container.files = files1;
                        addFile(container);
                    }
                }
            } catch (final Exception e) {
                new ExceptionUI(e);
            } finally {
                JDA.viewer.setIcon(false);
                if (update) {
                    try {
                        MainViewerGUI.getComponent(FileNavigationPane.class).updateTree();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        })).start();
    }

    public static void addFile(FileContainer fc) {
        JDA.files.add(fc);
        plugins.forEach((plugin -> plugin.onAddFile(fc)));
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

        files.clear();
        viewer.closeResources();
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
        } else if ((e.getKeyCode() == KeyEvent.VK_S) && isCtrlDown(e)) {
            if (JDA.getLoadedClasses().isEmpty()) {
                JDA.showMessage("First open a class, jar, or zip file.");
                return;
            }

            Thread t = new Thread() {
                public void run() {
                    JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            return f.isDirectory() || MiscUtils.extension(f.getAbsolutePath()).equals("zip");
                        }

                        @Override
                        public String getDescription() {
                            return "Zip Archives";
                        }
                    });
                    fc.setFileHidingEnabled(false);
                    fc.setAcceptAllFileFilterUsed(false);
                    int returnVal = fc.showSaveDialog(viewer);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        if (!file.getAbsolutePath().endsWith(".zip"))
                            file = new File(file.getAbsolutePath() + ".zip");

                        if (file.exists()) {
                            JOptionPane pane = new JOptionPane("Are you sure you wish to overwrite this existing file?");
                            Object[] options = new String[]{"Yes", "No"};
                            pane.setOptions(options);
                            JDialog dialog = pane.createDialog(JDA.viewer, "JDA - Overwrite File");
                            dialog.setVisible(true);
                            Object obj = pane.getValue();
                            int result = -1;
                            for (int k = 0; k < options.length; k++)
                                if (options[k].equals(obj))
                                    result = k;

                            if (result == 0) {
                                file.delete();
                            } else {
                                return;
                            }
                        }

                        final File file2 = file;

                        JDA.viewer.setIcon(true);
                        Thread t = new Thread() {
                            @Override
                            public void run() {
                                JarUtils.saveAsJar(JDA.getLoadedBytes(), file2.getAbsolutePath());
                                JDA.viewer.setIcon(false);
                            }
                        };
                        t.start();
                    }
                }
            };
            t.start();
        } else if ((e.getKeyCode() == KeyEvent.VK_W) && isCtrlDown(e)) {
            if (viewer.FileViewerPane.getCurrentViewer() != null)
                viewer.FileViewerPane.tabs.remove(viewer.FileViewerPane.getCurrentViewer());
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
                JDA.viewer.setIcon(true);
                JDA.openFiles(new File[]{fc.getSelectedFile()}, true);
                JDA.viewer.setIcon(false);
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
