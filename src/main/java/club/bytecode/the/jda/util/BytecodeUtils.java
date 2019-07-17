package club.bytecode.the.jda.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class BytecodeUtils {
    public static byte[] dumpClassToBytes(ClassNode cn) {
        // we have to do this, or else decompile filters don't work.
        try {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cn.accept(writer);
            return writer.toByteArray();
        } catch (Exception e) {
            System.err.println("Exception while dumping class " + cn.name + ": ");
            e.printStackTrace();
            return null;
        }
    }

    public static MethodNode applyJsrInlineAdapter(MethodNode mn) {
        final JSRInlinerAdapter adapter = new JSRInlinerAdapter(mn, mn.access, mn.name, mn.desc, mn.signature, mn.exceptions.toArray(new String[0]));
        mn.accept(adapter);
        return adapter;
    }

    public static ClassNode loadClass(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);
        return classNode;
    }
}
