package the.bytecode.club.jda.decompilers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.jda.JDA;
import the.bytecode.club.jda.api.ExceptionUI;
import the.bytecode.club.jda.settings.DecompilerSettings;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Used to represent all of the decompilers/disassemblers BCV contains.
 *
 * @author Konloch
 */

public abstract class Decompiler {
    public Decompiler() {
        Decompilers.BY_NAME.add(this);
    }

    protected DecompilerSettings settings = new DecompilerSettings(this);

    public abstract String decompileClassNode(ClassNode cn, byte[] b);

    public abstract void decompileToZip(String zipName);

    public abstract String getName();

    public DecompilerSettings getSettings() {
        return settings;
    }

    protected String parseException(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        e.printStackTrace();
        String exception = "JDA v" + JDA.version + JDA.nl + JDA.nl + sw.toString();
        return getName() + " encountered a problem! Send the stacktrace to https://github.com/ecx86/jda/issues" + JDA.nl +
                JDA.nl +
                "Suggested Fix: Click refresh class, if it fails again try another decompiler." + JDA.nl +
                JDA.nl +
                exception;
    }

    protected void handleException(Exception e) {
        new ExceptionUI(e);
    }

    protected byte[] fixBytes(byte[] in) {
        ClassReader reader = new ClassReader(in);
        ClassNode node = new ClassNode();
        reader.accept(node, ClassReader.EXPAND_FRAMES);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    public static void ensureInitted() {
        // Just to make sure the classes is loaded so all decompilers are loaded
    }
}
