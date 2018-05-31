package club.bytecode.the.jda;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a file container
 *
 * @author Konloch
 */

public class FileContainer {
    public FileContainer(File f) {
        this.file = f;
        this.name = f.getAbsolutePath();
    }

    public final File file;
    public final String name;

    public HashMap<String, byte[]> files = new HashMap<>();
    private Map<String, ClassNode> classes = new HashMap<>();

    public ClassNode getClassNode(String name) {
        if (classes.containsKey(name))
            return classes.get(name);
        ClassNode cn = loadClass(findClassfile(name));
        if (cn != null)
            classes.put(name, cn);
        return classes.get(name);
    }
    
    public void uncacheClassNode(String name) {
        classes.remove(name);
    }

    public ClassNode loadClass(String name) {
        byte[] bytes = files.get(name);
        if (bytes == null)
            return null;
        ClassReader reader = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);
        return classNode;
    }

    public String findClassfile(String className) {
        String candidate = className + ".class";
        if (files.containsKey(candidate))
            return candidate;
        return "";
    }

    public Map<String, byte[]> getFiles() {
        return files;
    }

    public boolean remove(ClassNode classNode) {
        return classes.remove(classNode.name) != null;
    }

    public void add(ClassNode classNode) {
        classes.put(classNode.name, classNode);
    }

    public Collection<ClassNode> getClasses() {
        return classes.values();
    }

    @Override
    public String toString() {
        return name;
    }
}
