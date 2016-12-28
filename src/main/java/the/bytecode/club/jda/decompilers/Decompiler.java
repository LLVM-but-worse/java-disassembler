package the.bytecode.club.jda.decompilers;

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
        Decompilers.BY_NAME.put(getName(), this);
    }

    protected DecompilerSettings settings = new DecompilerSettings(this);

    public abstract String decompileClassNode(String containerName, ClassNode cn);

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

    public static void ensureInitted() {
        // Just to make sure the classes is loaded so all decompilers are loaded
    }
}
