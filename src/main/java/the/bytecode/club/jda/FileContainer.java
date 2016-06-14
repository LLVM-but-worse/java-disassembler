package the.bytecode.club.jda;

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

public class FileContainer
{
    public FileContainer(File f)
    {
        this.file = f;
        this.name = f.getName();
    }

    public File file;
    public String name;

    public HashMap<String, byte[]> files = new HashMap<>();
    private Map<String, ClassNode> classes = new HashMap<>();

    public ClassNode getClassNode(String name)
    {
        if (!classes.containsKey(name))
        {
            byte[] bytes = files.get(name + ".class");
            if (bytes != null)
            {
                ClassReader reader = new ClassReader(bytes);
                ClassNode classNode = new ClassNode();
                reader.accept(classNode, ClassReader.EXPAND_FRAMES);
                classes.put(name, classNode);
            }
        }
        return classes.get(name);
    }

    public Map<String, byte[]> getData()
    {
        return files;
    }

    public boolean remove(ClassNode classNode)
    {
        return classes.remove(classNode.name) != null;
    }

    public void add(ClassNode classNode)
    {
        classes.put(classNode.name, classNode);
    }

    public Collection<ClassNode> values()
    {
        return classes.values();
    }
}
