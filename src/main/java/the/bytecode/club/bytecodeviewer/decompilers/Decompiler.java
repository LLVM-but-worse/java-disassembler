package the.bytecode.club.bytecodeviewer.decompilers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.bytecodeviewer.BytecodeViewer;
import the.bytecode.club.bytecodeviewer.DecompilerSettings;
import the.bytecode.club.bytecodeviewer.api.ExceptionUI;
import the.bytecode.club.bytecodeviewer.decompilers.bytecode.ClassNodeDecompiler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to represent all of the decompilers/disassemblers BCV contains.
 *
 * @author Konloch
 */

public abstract class Decompiler
{
    private static final Map<String, Decompiler> BY_NAME = new HashMap<>();

    public final static Decompiler BYTECODE = new ClassNodeDecompiler();
    public final static Decompiler FERNFLOWER = new FernFlowerDecompiler();
    public final static Decompiler PROCYON = new ProcyonDecompiler();
    public final static Decompiler CFR = new CFRDecompiler();
    public final static Decompiler HEXCODE = new Decompiler()
    {
        @Override
        public String decompileClassNode(ClassNode cn, byte[] b)
        {
            throw new IllegalArgumentException();
        }

        @Override
        public void decompileToZip(String zipName)
        {
            throw new IllegalArgumentException();
        }

        @Override
        public String getName()
        {
            return "Hexcode";
        }

        @Override
        public DecompilerSettings getSettings()
        {
            throw new IllegalArgumentException();
        }
    };

    public Decompiler()
    {
        BY_NAME.put(getName().toLowerCase().replace(' ', '-'), this);
    }

    protected DecompilerSettings settings = new DecompilerSettings(this);

    public abstract String decompileClassNode(ClassNode cn, byte[] b);

    public abstract void decompileToZip(String zipName);

    public abstract String getName();

    public DecompilerSettings getSettings()
    {
        return settings;
    }

    protected String parseException(Throwable e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        e.printStackTrace();
        String exception = "Java DisAssembler Version: " + BytecodeViewer.version + BytecodeViewer.nl + BytecodeViewer.nl + sw.toString();
        return getName() + " encountered a problem! Send the stacktrace to https://github.com/ecx86/jda/issues" + BytecodeViewer.nl +
                BytecodeViewer.nl +
                "Suggested Fix: Click refresh class, if it fails again try another decompiler." + BytecodeViewer.nl +
                BytecodeViewer.nl +
                exception;
    }

    protected void handleException(Exception e)
    {
        new ExceptionUI(e);
    }

    protected byte[] fixBytes(byte[] in)
    {
        ClassReader reader = new ClassReader(in);
        ClassNode node = new ClassNode();
        reader.accept(node, ClassReader.EXPAND_FRAMES);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    public static void ensureInitted()
    {
        // Just to make sure the classes is loaded so all decompilers are loaded
    }

    public static Decompiler getByName(String name)
    {
        return BY_NAME.get(name.toLowerCase().replace(' ', '-'));
    }

    public static Collection<Decompiler> getAllDecompilers()
    {
        return Collections.unmodifiableCollection(BY_NAME.values());
    }
}
