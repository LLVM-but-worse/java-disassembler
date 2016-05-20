package the.bytecode.club.bytecodeviewer;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.bytecodeviewer.api.ClassNodeLoader;
import the.bytecode.club.bytecodeviewer.api.ExceptionUI;
import the.bytecode.club.bytecodeviewer.gui.FileNavigationPane;
import the.bytecode.club.bytecodeviewer.gui.MainViewerGUI;
import the.bytecode.club.bytecodeviewer.gui.WorkPane;
import the.bytecode.club.bytecodeviewer.plugin.PluginManager;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BytecodeViewer
{

    /*per version*/
    public static final String version = "0.0.0";
    public static final boolean previewCopy = false;
    /* Constants */
    public static final String fs = System.getProperty("file.separator");
    public static final String nl = System.getProperty("line.separator");
    public static final File dataDir = new File(System.getProperty("user.home") + fs + ".jda");
    public static final File filesFile = new File(dataDir, "recentfiles.jda");
    public static final File pluginsFile = new File(dataDir, "recentplugins.jda");
    public static final File settingsFile = new File(dataDir, "settings.jda");
    @Deprecated public static final File tempDir = new File(dataDir, "jda_temp");
    private static final long start = System.currentTimeMillis();
    /*the rest*/
    public static MainViewerGUI viewer = null;
    public static ClassNodeLoader loader = new ClassNodeLoader(); // TODO MAKE SECURE BECAUSE THIS IS INSECURE
    public static SecurityMan sm = new SecurityMan(); // TODO MAKE SECURE BECAUSE THIS IS INSECURE
    public static ArrayList<FileContainer> files = new ArrayList<>(); //all of BCV's loaded files/classes/etc
    private static int maxRecentFiles = 25;
    private static List<String> recentFiles = new ArrayList<>();
    private static List<String> recentPlugins = new ArrayList<>();
    public static boolean runningObfuscation = false;
    public static String lastDirectory = "";
    public static ArrayList<Process> createdProcesses = new ArrayList<>();
    public static boolean deleteForiegnLibraries = true;

    /**
     * Main startup
     *
     * @param args files you want to open or CLI
     */
    public static void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            new ExceptionUI(e);
        }
        try
        {
            System.setSecurityManager(sm);
            System.out.println("Java DisAssembler (BCV Fork) " + version);
            CommandLineInput input = new CommandLineInput(args);
            if (previewCopy && !input.containsCommand())
                showMessage("WARNING: This is a preview/dev copy, you WON'T be alerted when " + version + " is actually out if you use this." + nl +
                        "Make sure to watch the repo: https://github.com/ecx86/jda for " + version + "'s release");
            getJDADirectory();
            if (!filesFile.exists() && !filesFile.createNewFile())
            {
                throw new RuntimeException("Could not create recent files file");
            }
            if (!pluginsFile.exists() && !pluginsFile.createNewFile())
            {
                throw new RuntimeException("Could not create recent plugins file");
            }
            recentFiles.addAll(FileUtils.readLines(filesFile, "UTF-8"));
            recentPlugins.addAll(FileUtils.readLines(pluginsFile, "UTF-8"));
            int CLI = input.parseCommandLine();
            if (CLI == CommandLineInput.STOP)
                return;
            if (CLI == CommandLineInput.OPEN_FILE)
            {
                Settings.loadGUI();
                viewer = new MainViewerGUI();
                Boot.boot();
                BytecodeViewer.BOOT(args, false);
            }
            else
            {
                BytecodeViewer.BOOT(args, true);
                input.executeCommandLine();
            }
        }
        catch (Exception e)
        {
            new the.bytecode.club.bytecodeviewer.api.ExceptionUI(e);
        }
    }

    /**
     * The version checker thread
     */
    private static final Thread versionChecker = new Thread()
    {
        // todo: rewrite
        @Override
        public void run()
        {
        }
    };

    /**
     * Boot after all of the libraries have been loaded
     *
     * @param cli is it running CLI mode or not
     */
    public static void BOOT(String[] args, boolean cli)
    {
        cleanup();
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                for (Process proc : createdProcesses)
                    proc.destroy();
                try
                {
                    FileUtils.writeLines(filesFile, recentFiles);
                    FileUtils.writeLines(pluginsFile, recentPlugins);
                }
                catch (IOException e)
                {
                    new ExceptionUI(e);
                }
                Settings.saveGUI();
                cleanup();
            }
        });

        viewer.calledAfterLoad();
        resetRecentFilesMenu();

        if (viewer.mntmUpdateCheck.isSelected())
            versionChecker.start();

        if (!cli)
            viewer.setVisible(true);

        System.out.println("Start up took " + ((System.currentTimeMillis() - start) / 1000) + " seconds");

        if (!cli)
            if (args.length >= 1)
                for (String s : args)
                {
                    openFiles(new File[] { new File(s) }, true);
                }
    }

    /**
     * Returns the currently opened ClassNode
     *
     * @return the currently opened ClassNode
     */
    public static ClassNode getCurrentlyOpenedClassNode()
    {
        return viewer.workPane.getCurrentViewer().cn;
    }

    /**
     * Returns the ClassNode by the specified name
     *
     * @param containerName name of the FileContainer that this class is in
     * @param name          the class name
     * @return the ClassNode instance
     */
    public static ClassNode getClassNode(String containerName, String name)
    {
        for (FileContainer container : files)
        {
            if (container.name.equals(containerName) && container.getData().containsKey(name + ".class"))
            {
                return container.getClassNode(name);
            }
        }
        return null;
    }

    public static byte[] getClassBytes(String containerName, String name)
    {
        for (FileContainer container : files)
        {
            if (container.name.equals(containerName) && container.getData().containsKey(name))
            {
                return container.getData().get(name);
            }
        }
        return null;
    }

    /**
     * Grabs the file contents of the loaded resources.
     *
     * @param name the file name
     * @return the file contents as a byte[]
     */
    public static byte[] getFileContents(String name)
    {
        for (FileContainer container : files)
        {
            HashMap<String, byte[]> files = container.files;
            if (files.containsKey(name))
                return files.get(name);
        }

        return null;
    }

    /**
     * Replaces an old node with a new instance
     *
     * @param oldNode the old instance
     * @param newNode the new instance
     */
    public static void updateNode(ClassNode oldNode, ClassNode newNode)
    {
        for (FileContainer container : files)
        {
            if (container.remove(oldNode))
                container.add(newNode);
        }
    }

    /**
     * Gets all of the loaded classes as an array list
     *
     * @return the loaded classes as an array list
     */
    public static ArrayList<ClassNode> getLoadedClasses()
    {
        ArrayList<ClassNode> a = new ArrayList<>();

        for (FileContainer container : files)
            for (ClassNode c : container.values())
                if (!a.contains(c))
                    a.add(c);

        return a;
    }

    public static ArrayList<ClassNode> loadAllClasses()
    {
        ArrayList<ClassNode> a = new ArrayList<>();
        for (FileContainer container : files)
        {
            for (String s : container.files.keySet())
            {
                if (!s.endsWith(".class"))
                    continue;
                ClassNode loaded = container.getClassNode(s.substring(0, s.length() - 6));
                if (loaded != null)
                {
                    a.add(loaded);
                }
            }
        }

        return a;
    }

    public static Map<String, byte[]> getLoadedBytes()
    {
        Map<String, byte[]> data = new HashMap<>();
        for (FileContainer container : files)
        {
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
    public static void openFiles(final File[] files, boolean recentFiles)
    {
        if (recentFiles)
            for (File f : files)
                if (f.exists())
                    BytecodeViewer.addRecentFile(f);

        BytecodeViewer.viewer.setIcon(true);
        update = true;

        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    for (final File f : files)
                    {
                        final String fn = f.getName();
                        if (!f.exists())
                        {
                            update = false;
                            showMessage("The file " + f.getAbsolutePath() + " could not be found.");
                        }
                        else
                        {
                            if (f.isDirectory())
                            {
                                FileContainer container = new FileContainer(f);
                                HashMap<String, byte[]> files = new HashMap<>();
                                boolean finished = false;
                                ArrayList<File> totalFiles = new ArrayList<>();
                                totalFiles.add(f);
                                String dir = f.getAbsolutePath();//f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-f.getName().length());

                                while (!finished)
                                {
                                    boolean added = false;
                                    for (int i = 0; i < totalFiles.size(); i++)
                                    {
                                        File child = totalFiles.get(i);
                                        if (child.listFiles() != null)
                                            for (File rocket : child.listFiles())
                                                if (!totalFiles.contains(rocket))
                                                {
                                                    totalFiles.add(rocket);
                                                    added = true;
                                                }
                                    }

                                    if (!added)
                                    {
                                        for (File child : totalFiles)
                                            if (child.isFile())
                                            {
                                                String fileName = child.getAbsolutePath().substring(dir.length() + 1, child.getAbsolutePath().length()).replaceAll("\\\\", "\\/");


                                                files.put(fileName, Files.readAllBytes(Paths.get(child.getAbsolutePath())));
                                            }
                                        finished = true;
                                    }
                                }
                                container.files = files;
                                BytecodeViewer.files.add(container);
                            }
                            else
                            {
                                if (fn.endsWith(".jar") || fn.endsWith(".zip"))
                                {
                                    try
                                    {
                                        JarUtils.put(f);
                                    }
                                    catch (final Exception e)
                                    {
                                        new the.bytecode.club.bytecodeviewer.api.ExceptionUI(e);
                                        update = false;
                                    }

                                }
                                else if (fn.endsWith(".class"))
                                {
                                    try
                                    {
                                        byte[] bytes = JarUtils.getBytes(new FileInputStream(f));
                                        String cafebabe = String.format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]);
                                        if (cafebabe.toLowerCase().equals("cafebabe"))
                                        {
                                            final ClassNode cn = JarUtils.getNode(bytes);

                                            FileContainer container = new FileContainer(f);
                                            container.files.put(cn.name + ".class", bytes);
                                            container.add(cn);
                                            BytecodeViewer.files.add(container);
                                        }
                                        else
                                        {
                                            showMessage(fn + ": Header does not start with CAFEBABE, ignoring.");
                                            update = false;
                                        }
                                    }
                                    catch (final Exception e)
                                    {
                                        new the.bytecode.club.bytecodeviewer.api.ExceptionUI(e);
                                        update = false;
                                    }
                                }
                                else
                                {
                                    HashMap<String, byte[]> files = new HashMap<>();
                                    byte[] bytes = JarUtils.getBytes(new FileInputStream(f));
                                    files.put(f.getName(), bytes);


                                    FileContainer container = new FileContainer(f);
                                    container.files = files;
                                    BytecodeViewer.files.add(container);
                                }
                            }
                        }
                    }
                }
                catch (final Exception e)
                {
                    new the.bytecode.club.bytecodeviewer.api.ExceptionUI(e);
                }
                finally
                {
                    BytecodeViewer.viewer.setIcon(false);
                    if (update)
                        try
                        {
                            MainViewerGUI.getComponent(FileNavigationPane.class).updateTree();
                        }
                        catch (java.lang.NullPointerException e)
                        {
                        }
                }
            }
        };
        t.start();
    }

    /**
     * Starts the specified plugin
     *
     * @param file the file of the plugin
     */
    public static void startPlugin(File file)
    {
        if (!file.exists())
            return;

        try
        {
            PluginManager.runPlugin(file);
        }
        catch (Throwable e)
        {
            new the.bytecode.club.bytecodeviewer.api.ExceptionUI(e);
        }
        addRecentPlugin(file);
    }

    /**
     * Send a message to alert the user
     *
     * @param message the message you need to send
     */
    public static void showMessage(String message)
    {
        JOptionPane.showMessageDialog(viewer, message);
    }

    /**
     * Resets the workspace with optional user input required
     *
     * @param ask if should require user input or not
     */
    public static void resetWorkSpace(boolean ask)
    {
        if (!ask)
        {
            files.clear();
            MainViewerGUI.getComponent(FileNavigationPane.class).resetWorkspace();
            MainViewerGUI.getComponent(WorkPane.class).resetWorkspace();
            the.bytecode.club.bytecodeviewer.api.BytecodeViewer.getClassNodeLoader().clear();
        }
        else
        {
            JOptionPane pane = new JOptionPane("Are you sure you want to reset the workspace?\n\rIt will also reset your file navigator and search.");
            Object[] options = new String[] { "Yes", "No" };
            pane.setOptions(options);
            JDialog dialog = pane.createDialog(viewer, "Java DisAssembler - Reset Workspace");
            dialog.setVisible(true);
            Object obj = pane.getValue();
            int result = -1;
            for (int k = 0; k < options.length; k++)
                if (options[k].equals(obj))
                    result = k;

            if (result == 0)
            {
                files.clear();
                MainViewerGUI.getComponent(FileNavigationPane.class).resetWorkspace();
                MainViewerGUI.getComponent(WorkPane.class).resetWorkspace();
                the.bytecode.club.bytecodeviewer.api.BytecodeViewer.getClassNodeLoader().clear();
            }
        }
    }

    private static ArrayList<String> killList = new ArrayList<>();

    /**
     * Add the recent file
     *
     * @param f the recent file
     */
    public static void addRecentFile(File f)
    {
        for (int i = 0; i < recentFiles.size(); i++)
        { // remove dead strings
            String s = recentFiles.get(i);
            if (s.isEmpty() || i > maxRecentFiles)
                killList.add(s);
        }
        if (!killList.isEmpty())
        {
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
     * Add to the recent plugin list
     *
     * @param f the plugin file
     */
    public static void addRecentPlugin(File f)
    {
        for (int i = 0; i < recentPlugins.size(); i++)
        { // remove dead strings
            String s = recentPlugins.get(i);
            if (s.isEmpty() || i > maxRecentFiles)
                killList2.add(s);
        }
        if (!killList2.isEmpty())
        {
            for (String s : killList2)
                recentPlugins.remove(s);
            killList2.clear();
        }

        if (recentPlugins.contains(f.getAbsolutePath())) // already added on the list
            recentPlugins.remove(f.getAbsolutePath());
        if (recentPlugins.size() >= maxRecentFiles)
            recentPlugins.remove(maxRecentFiles - 1); // zero indexing

        recentPlugins.add(0, f.getAbsolutePath());
        resetRecentFilesMenu();
    }

    /**
     * resets the recent files menu
     */
    public static void resetRecentFilesMenu()
    {
        viewer.mnRecentFiles.removeAll();
        for (String s : recentFiles)
            if (!s.isEmpty())
            {
                JMenuItem m = new JMenuItem(s);
                m.addActionListener(e -> {
                    JMenuItem m1 = (JMenuItem) e.getSource();
                    openFiles(new File[] { new File(m1.getText()) }, true);
                });
                viewer.mnRecentFiles.add(m);
            }
        viewer.mnRecentPlugins.removeAll();
        for (String s : recentPlugins)
            if (!s.isEmpty())
            {
                JMenuItem m = new JMenuItem(s);
                m.addActionListener(e -> {
                    JMenuItem m1 = (JMenuItem) e.getSource();
                    startPlugin(new File(m1.getText()));
                });
                viewer.mnRecentPlugins.add(m);
            }
    }

    /**
     * Clears the temp directory
     */
    public static void cleanup()
    {
        try
        {
            FileUtils.cleanDirectory(tempDir);
        }
        catch (Exception e)
        {
        }
    }

    public static ArrayList<String> createdRandomizedNames = new ArrayList<>();

    /**
     * Ensures it will only return a uniquely generated names, contains a dupe checker to be sure
     *
     * @return the unique randomized name of 25 characters.
     */
    public static String getRandomizedName()
    {
        boolean generated = false;
        String name = "";
        while (!generated)
        {
            String randomizedName = MiscUtils.randomString(25);
            if (!createdRandomizedNames.contains(randomizedName))
            {
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
    public static String getJDADirectory()
    {
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
    private static boolean isWindows()
    {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Runs the windows command to hide files
     *
     * @param f file you want hidden
     */
    private static void hideFile(File f)
    {
        sm.stopBlocking();
        try
        {
            // Hide file by running attrib system command (on Windows)
            Runtime.getRuntime().exec("attrib +H " + f.getAbsolutePath());
        }
        catch (Exception e)
        {
            new the.bytecode.club.bytecodeviewer.api.ExceptionUI(e);
        }
        sm.setBlocking();
    }

    private static long last = System.currentTimeMillis();

    /**
     * Checks the hotkeys
     *
     * @param e
     */
    public static void checkHotKey(KeyEvent e)
    {
        if (System.currentTimeMillis() - last <= (4000))
            return;

        if ((e.getKeyCode() == KeyEvent.VK_O) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
        {
            last = System.currentTimeMillis();
            JFileChooser fc = new JFileChooser();
            try
            {
                fc.setSelectedFile(new File(BytecodeViewer.lastDirectory));
            }
            catch (Exception e2)
            {

            }
            fc.setFileFilter(new FileFilter()
            {
                @Override
                public boolean accept(File f)
                {
                    if (f.isDirectory())
                        return true;

                    String extension = MiscUtils.extension(f.getAbsolutePath());
                    if (extension != null)
                        if (extension.equals("jar") || extension.equals("zip") || extension.equals("class"))
                            return true;

                    return false;
                }

                @Override
                public String getDescription()
                {
                    return "Class Files or Zip/Jar Archives";
                }
            });
            fc.setFileHidingEnabled(false);
            fc.setAcceptAllFileFilterUsed(false);
            int returnVal = fc.showOpenDialog(BytecodeViewer.viewer);

            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                BytecodeViewer.lastDirectory = fc.getSelectedFile().getAbsolutePath();
                try
                {
                    BytecodeViewer.viewer.setIcon(true);
                    BytecodeViewer.openFiles(new File[] { fc.getSelectedFile() }, true);
                    BytecodeViewer.viewer.setIcon(false);
                }
                catch (Exception e1)
                {
                    new the.bytecode.club.bytecodeviewer.api.ExceptionUI(e1);
                }
            }
        }
        else if ((e.getKeyCode() == KeyEvent.VK_N) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
        {
            last = System.currentTimeMillis();
            BytecodeViewer.resetWorkSpace(true);
        }
        else if ((e.getKeyCode() == KeyEvent.VK_R) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
        {
            last = System.currentTimeMillis();
            viewer.reloadResources();
        }
        else if ((e.getKeyCode() == KeyEvent.VK_S) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
        {
            last = System.currentTimeMillis();

            if (BytecodeViewer.getLoadedClasses().isEmpty())
            {
                BytecodeViewer.showMessage("First open a class, jar, or zip file.");
                return;
            }

            Thread t = new Thread()
            {
                public void run()
                {
                    JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new FileFilter()
                    {
                        @Override
                        public boolean accept(File f)
                        {
                            return f.isDirectory() || MiscUtils.extension(f.getAbsolutePath()).equals("zip");
                        }

                        @Override
                        public String getDescription()
                        {
                            return "Zip Archives";
                        }
                    });
                    fc.setFileHidingEnabled(false);
                    fc.setAcceptAllFileFilterUsed(false);
                    int returnVal = fc.showSaveDialog(viewer);
                    if (returnVal == JFileChooser.APPROVE_OPTION)
                    {
                        File file = fc.getSelectedFile();
                        if (!file.getAbsolutePath().endsWith(".zip"))
                            file = new File(file.getAbsolutePath() + ".zip");

                        if (file.exists())
                        {
                            JOptionPane pane = new JOptionPane("Are you sure you wish to overwrite this existing file?");
                            Object[] options = new String[] { "Yes", "No" };
                            pane.setOptions(options);
                            JDialog dialog = pane.createDialog(BytecodeViewer.viewer, "Java DisAssembler - Overwrite File");
                            dialog.setVisible(true);
                            Object obj = pane.getValue();
                            int result = -1;
                            for (int k = 0; k < options.length; k++)
                                if (options[k].equals(obj))
                                    result = k;

                            if (result == 0)
                            {
                                file.delete();
                            }
                            else
                            {
                                return;
                            }
                        }

                        final File file2 = file;

                        BytecodeViewer.viewer.setIcon(true);
                        Thread t = new Thread()
                        {
                            @Override
                            public void run()
                            {
                                JarUtils.saveAsJar(BytecodeViewer.getLoadedBytes(), file2.getAbsolutePath());
                                BytecodeViewer.viewer.setIcon(false);
                            }
                        };
                        t.start();
                    }
                }
            };
            t.start();
        }
        else if ((e.getKeyCode() == KeyEvent.VK_W) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
        {
            last = System.currentTimeMillis();
            if (viewer.workPane.getCurrentViewer() != null)
                viewer.workPane.tabs.remove(viewer.workPane.getCurrentViewer());
        }
    }
}
