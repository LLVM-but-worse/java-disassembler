package club.bytecode.the.jda.decompilers.bytecode;

import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.api.JDANamespace;
import club.bytecode.the.jda.decompilers.JDADecompiler;
import club.bytecode.the.jda.gui.fileviewer.BytecodeTokenizer;
import club.bytecode.the.jda.settings.JDADecompilerSettings.SettingsEntry;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Konloch
 * @author Bibl
 */

public class BytecodeDecompiler extends JDADecompiler {

    public BytecodeDecompiler() {
        settings.registerSetting(new SettingsEntry("debug-helpers", "Debug helpers", true));
        settings.registerSetting(new SettingsEntry("show-method-descriptors", "Show method descriptors", true));
        settings.registerSetting(new SettingsEntry("decompile-inner-classes", "Decompile inner classes", true));
        settings.registerSetting(new SettingsEntry("append-handler-comments", "Append handler comments", true));
    }

    @Override
    public String getName() {
        return "Bytecode";
    }
    
    @Override
    public JDANamespace getNamespace() {
        return JDA.namespace;
    }

    public String decompileClassNode(FileContainer container, ClassNode cn) {
        return decompile(new PrefixedStringBuilder(), new ArrayList<>(), container, cn).toString();
    }

    protected PrefixedStringBuilder decompile(PrefixedStringBuilder sb, ArrayList<String> decompiledClasses, FileContainer container, ClassNode cn) {
        ArrayList<String> unableToDecompile = new ArrayList<>();
        decompiledClasses.add(cn.name);
        sb.append(getAccessString(cn.access));
        sb.append(" ");
        sb.append(cn.name);
        if (cn.superName != null && !cn.superName.equals("java/lang/Object")) {
            sb.append(" extends ");
            sb.append(cn.superName);
        }

        int amountOfInterfaces = cn.interfaces.size();
        if (amountOfInterfaces > 0) {
            sb.append(" implements ");
            sb.append(cn.interfaces.get(0));
            if (amountOfInterfaces > 1) {
                // sb.append(",");
            }
            for (int i = 1; i < amountOfInterfaces; i++) {
                sb.append(", ");
                sb.append(cn.interfaces.get(i));
            }
        }
        sb.append(" {");
        sb.append(JDA.nl);

        for (Iterator<FieldNode> it = cn.fields.iterator(); it.hasNext(); ) {
            sb.append("     ");
            getFieldNodeDecompiler(sb, it).decompile();
            sb.append(JDA.nl);
            if (!it.hasNext())
                sb.append(JDA.nl);
        }

        for (Iterator<MethodNode> it = cn.methods.iterator(); it.hasNext(); ) {
            getMethodNodeDecompiler(sb, cn, it).decompile();
            if (it.hasNext())
                sb.append(JDA.nl);
        }

        if (settings.getEntry("decompile-inner-classes").getBool())
            for (InnerClassNode innerClassNode : cn.innerClasses) {
                String innerClassName = innerClassNode.name;
                if ((innerClassName != null) && !decompiledClasses.contains(innerClassName)) {
                    decompiledClasses.add(innerClassName);
                    ClassNode cn1 = container.loadClassFile(container.findClassfile(innerClassName));
                    applyFilters(cn1);
                    if (cn1 != null) {
                        sb.appendPrefix("     ");
                        sb.append(JDA.nl + JDA.nl);
                        sb = decompile(sb, decompiledClasses, container, cn1);
                        sb.trimPrefix(5);
                        sb.append(JDA.nl);
                    } else {
                        unableToDecompile.add(innerClassName);
                    }
                }
            }

        if (!unableToDecompile.isEmpty()) {
            sb.append("// The following inner classes couldn't be decompiled: ");
            for (String s : unableToDecompile) {
                sb.append(s);
                sb.append(" ");
            }
            sb.append(JDA.nl);
        }

        sb.append("}");
        // System.out.println("Wrote end for " + cn.name +
        // " with prefix length: " + sb.prefix.length());
        return sb;
    }

    protected FieldNodeDecompiler getFieldNodeDecompiler(PrefixedStringBuilder sb, Iterator<FieldNode> it) {
        return new FieldNodeDecompiler(sb, it.next());
    }

    protected MethodNodeDecompiler getMethodNodeDecompiler(PrefixedStringBuilder sb, ClassNode cn, Iterator<MethodNode> it) {
        return new MethodNodeDecompiler(this, sb, it.next(), cn);
    }

    public static String getAccessString(int access) {
        List<String> tokens = new ArrayList<>();
        if ((access & Opcodes.ACC_PUBLIC) != 0)
            tokens.add("public");
        if ((access & Opcodes.ACC_PRIVATE) != 0)
            tokens.add("private");
        if ((access & Opcodes.ACC_PROTECTED) != 0)
            tokens.add("protected");
        if ((access & Opcodes.ACC_FINAL) != 0)
            tokens.add("final");
        if ((access & Opcodes.ACC_SYNTHETIC) != 0)
            tokens.add("synthetic");
        // if ((access & Opcodes.ACC_SUPER) != 0)
        // tokens.add("super"); implied by invokespecial insn
        if ((access & Opcodes.ACC_ABSTRACT) != 0)
            tokens.add("abstract");
        if ((access & Opcodes.ACC_INTERFACE) != 0)
            tokens.add("interface");
        if ((access & Opcodes.ACC_ENUM) != 0)
            tokens.add("enum");
        if ((access & Opcodes.ACC_ANNOTATION) != 0)
            tokens.add("annotation");
        if (!tokens.contains("interface") && !tokens.contains("enum") && !tokens.contains("annotation"))
            tokens.add("class");
        if (tokens.size() == 0)
            return "[Error parsing]";

        // hackery delimeters
        StringBuilder sb = new StringBuilder(tokens.get(0));
        for (int i = 1; i < tokens.size(); i++) {
            sb.append(" ");
            sb.append(tokens.get(i));
        }
        return sb.toString();
    }

    @Override
    public String getTarget() {
        return BytecodeTokenizer.SYNTAX_STYLE_BYTECODE;
    }
}
