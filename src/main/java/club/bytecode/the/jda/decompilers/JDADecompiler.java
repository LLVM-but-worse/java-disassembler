package club.bytecode.the.jda.decompilers;

import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.api.JDANamespacedComponent;
import club.bytecode.the.jda.decompilers.filter.DecompileFilter;
import club.bytecode.the.jda.settings.JDADecompilerSettings;
import org.objectweb.asm.tree.ClassNode;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Used to represent all of the decompilers/disassemblers BCV contains.
 *
 * @author Konloch
 */

public abstract class JDADecompiler implements JDANamespacedComponent {
    protected JDADecompilerSettings settings = new JDADecompilerSettings(this);

    public abstract String decompileClassNode(FileContainer container, ClassNode cn);

    public JDADecompilerSettings getSettings() {
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
    
    public void applyFilters(ClassNode cn) {
        for (DecompileFilter filter : getSettings().getEnabledFilters()) {
            filter.process(cn);
        }
    }
}
