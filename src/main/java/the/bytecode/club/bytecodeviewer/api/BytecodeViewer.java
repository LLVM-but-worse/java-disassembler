package the.bytecode.club.bytecodeviewer.api;

import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.bytecodeviewer.JarUtils;
import the.bytecode.club.bytecodeviewer.decompilers.Decompiler;
import the.bytecode.club.bytecodeviewer.plugin.preinstalled.EZInjection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The official API for BCV, this was designed for plugin authors and
 * people utilizing EZ-Injection.
 *
 * @author Konloch
 */

public class BytecodeViewer
{

    private static URLClassLoader cl;

    /**
     * Grab the loader instance
     *
     * @return
     */
    public static ClassNodeLoader getClassNodeLoader()
    {
        return the.bytecode.club.bytecodeviewer.BytecodeViewer.loader;
    }

    /**
     * Returns the URLClassLoader instance
     *
     * @return the URLClassLoader instance
     */
    public static URLClassLoader getClassLoaderInstance()
    {
        return cl;
    }


    /**
     * Re-instances the URLClassLoader and loads a jar to it.
     *
     * @param nodeList The list of ClassNodes to be loaded
     * @return The loaded classes into the new URLClassLoader instance
     * @throws IOException
     * @throws ClassNotFoundException
     * @author Cafebabe
     */
    @SuppressWarnings("deprecation")
    public static List<Class<?>> loadClassesIntoClassLoader(ArrayList<ClassNode> nodeList) throws IOException, ClassNotFoundException
    {
        File f = new File(the.bytecode.club.bytecodeviewer.BytecodeViewer.tempDir, "loaded_temp.jar");
        JarUtils.saveAsJarClassesOnly(nodeList, f.getAbsolutePath());

        JarFile jarFile = new JarFile("" + f.getAbsolutePath());
        Enumeration<JarEntry> e = jarFile.entries();
        cl = URLClassLoader.newInstance(new URL[] { f.toURL() });
        List<Class<?>> ret = new ArrayList<>();

        while (e.hasMoreElements())
        {
            JarEntry je = e.nextElement();
            if (je.isDirectory() || !je.getName().endsWith(".class"))
                continue;
            String className = je.getName().replace("/", ".").replace(".class", "");
            className = className.replace('/', '.');
            ret.add(cl.loadClass(className));

        }
        jarFile.close();

        return ret;

    }


    /**
     * Re-instances the URLClassLoader and loads a jar to it.
     *
     * @return The loaded classes into the new URLClassLoader instance
     * @throws IOException
     * @throws ClassNotFoundException
     * @author Cafebabe
     */
    public static List<Class<?>> loadAllClassesIntoClassLoader() throws ClassNotFoundException, IOException
    {
        return loadClassesIntoClassLoader(getLoadedClasses());
    }

    /**
     * Creates a new instance of the ClassNode loader.
     */
    public static void createNewClassNodeLoaderInstance()
    {
        the.bytecode.club.bytecodeviewer.BytecodeViewer.loader.clear();
        the.bytecode.club.bytecodeviewer.BytecodeViewer.loader = new ClassNodeLoader();
    }

    /**
     * Used to start a plugin from file.
     *
     * @param plugin the file of the plugin
     */
    public static void startPlugin(File plugin)
    {
        the.bytecode.club.bytecodeviewer.BytecodeViewer.startPlugin(plugin);
    }

    /**
     * Used to load classes/jars into BCV.
     *
     * @param files       an array of the files you want loaded.
     * @param recentFiles if it should save to the recent files menu.
     */
    public static void openFiles(File[] files, boolean recentFiles)
    {
        the.bytecode.club.bytecodeviewer.BytecodeViewer.openFiles(files, recentFiles);
    }

    /**
     * Returns the currently opened class node, if nothing is opened it'll return null.
     *
     * @return The opened class node or a null if nothing is opened
     */
    public static ClassNode getCurrentlyOpenedClassNode()
    {
        return the.bytecode.club.bytecodeviewer.BytecodeViewer.getCurrentlyOpenedClassNode();
    }

    /**
     * Used to load a ClassNode.
     *
     * @param name the full name of the ClassNode
     * @return the ClassNode
     */
    public static ClassNode getClassNode(String containerName, String name)
    {
        return the.bytecode.club.bytecodeviewer.BytecodeViewer.getClassNode(containerName, name);
    }

    /**
     * Used to grab the loaded ClassNodes.
     *
     * @return the loaded classes
     */
    public static ArrayList<ClassNode> getLoadedClasses()
    {
        return the.bytecode.club.bytecodeviewer.BytecodeViewer.getLoadedClasses();
    }

    /**
     * Used to insert a Bytecode Hook using EZ-Injection.
     *
     * @param hook
     */
    public static void insertHook(BytecodeHook hook)
    {
        EZInjection.hookArray.add(hook);
    }

    /**
     * This will ask the user if they really want to reset the workspace, then
     * it'll reset the work space.
     *
     * @param ask if it should ask the user about resetting the workspace
     */
    public static void resetWorkSpace(boolean ask)
    {
        the.bytecode.club.bytecodeviewer.BytecodeViewer.resetWorkSpace(ask);
    }

    /**
     * If true, it will display the busy icon, if false it will remove it if
     * it's displayed.
     *
     * @param busy if it should display the busy icon or not
     */
    public static void setBusy(boolean busy)
    {
        the.bytecode.club.bytecodeviewer.BytecodeViewer.viewer.setIcon(busy);
    }

    /**
     * Sends a small window popup with the defined message.
     *
     * @param message the message you want to display
     */
    public static void showMessage(String message)
    {
        the.bytecode.club.bytecodeviewer.BytecodeViewer.showMessage(message);
    }

    /**
     * Returns the wrapped Procyon Decompiler instance.
     *
     * @return The wrapped Procyon Decompiler instance
     */
    public static Decompiler getProcyonDecompiler()
    {
        return Decompiler.PROCYON;
    }

    /**
     * Returns the wrapped CFR Decompiler instance.
     *
     * @return The wrapped CFR Decompiler instance
     */
    public static Decompiler getCFRDecompiler()
    {
        return Decompiler.CFR;
    }

    /**
     * Returns the wrapped FernFlower Decompiler instance.
     *
     * @return The wrapped FernFlower Decompiler instance
     */
    public static Decompiler getFernFlowerDecompiler()
    {
        return Decompiler.FERNFLOWER;
    }
}
