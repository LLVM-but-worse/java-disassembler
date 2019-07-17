package club.bytecode.the.jda;

import club.bytecode.the.jda.util.BytecodeUtils;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.util.Map;

/**
 * Represents a file container
 *
 * @author Konloch
 */

public class FileContainer {
    public FileContainer(File f, Map<String, byte[]> files) {
        this.file = f;
        this.name = f.getAbsolutePath();
        this.files = files;
    }

    public final File file;
    public final String name;
    public final Map<String, byte[]> files;


    public ClassNode loadClassFile(String filename) {
        byte[] bytes = files.get(filename);
        if (bytes == null)
            return null;
        return BytecodeUtils.loadClass(bytes);
    }

    public String findClassfile(String className) {
        String candidate = className + ".class";
        if (name.endsWith(".class")) { // this is a single .class file. we need to strip the package path out.
            candidate = JDA.getClassName(candidate);
        }
        if (files.containsKey(candidate))
            return candidate;
        return "";
    }

    public Map<String, byte[]> getFiles() {
        return files;
    }

    @Override
    public String toString() {
        return name;
    }
}
