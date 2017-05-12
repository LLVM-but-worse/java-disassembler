package the.bytecode.club.jda.decompilers.bytecode;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import the.bytecode.club.jda.JDA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Konloch
 * @author Bibl
 */

public class MethodNodeDecompiler {
    private final BytecodeDecompiler parent;
    protected final PrefixedStringBuilder sb;
    protected final MethodNode mn;
    protected final ClassNode cn;

    public MethodNodeDecompiler(BytecodeDecompiler parent, PrefixedStringBuilder sb, MethodNode mn, ClassNode cn) {
        this.parent = parent;
        this.sb = sb;
        this.mn = mn;
        this.cn = cn;
    }

    @SuppressWarnings("unused")
    public PrefixedStringBuilder decompile() {
        String package_ = null;
        String class_ = null;
        if (cn.name.contains("/")) {
            package_ = cn.name.substring(0, cn.name.lastIndexOf("/"));
            class_ = cn.name.substring(cn.name.lastIndexOf("/") + 1);
        } else {
            class_ = cn.name;
        }

        // Descriptor
        if (createDescriptors()) {
            sb.append("     // ");
            sb.append(mn.owner.name);
            sb.append(".");
            sb.append(mn.name);
            sb.append(mn.desc);
            sb.append(JDA.nl);
        }

        // Access
        String access = getAccessString(mn.access);
        sb.append("     ");
        sb.append(access);
        if (access.length() > 0 && !mn.name.equals("<clinit>"))
            sb.append(" ");

        // Return type
        if (mn.name.charAt(0) != '<' && !mn.name.endsWith("init>")) {
            Type returnType = Type.getReturnType(mn.desc);
            sb.append(returnType.getClassName());
            sb.append(" ");
        }

        // Method name
        if (mn.name.equals("<init>")) {
            sb.append(class_);
        } else if (mn.name.equals("<clinit>")) {
        } else {
            sb.append(mn.name);
        }

        // Arguments
        TypeAndName[] args = new TypeAndName[0];
        if (!mn.name.equals("<clinit>")) {
            sb.append("(");

            final Type[] argTypes = Type.getArgumentTypes(mn.desc);
            args = new TypeAndName[argTypes.length];

            for (int i = 0; i < argTypes.length; i++) {
                final Type type = argTypes[i];

                final TypeAndName tan = new TypeAndName();
                final String argName = "arg" + i;

                tan.name = argName;
                tan.type = type;

                args[i] = tan;

                sb.append(type.getClassName() + " " + argName + (i < argTypes.length - 1 ? ", " : ""));
            }

            sb.append(")");
        }

        // Throws
        int amountOfThrows = mn.exceptions.size();
        if (amountOfThrows > 0) {
            sb.append(" throws ");
            sb.append(mn.exceptions.get(0));// exceptions is list<string>
            for (int i = 1; i < amountOfThrows; i++) {
                sb.append(", ");
                sb.append(mn.exceptions.get(i));
            }
        }

        // End method name
        if (access.contains("abstract")) {
            sb.append(";");
        } else {
            sb.append(" {");

            if (createComments() && !createDescriptors()) {
                if (mn.name.equals("<clinit>"))
                    sb.append(" // <clinit>");
                else if (mn.name.equals("<init>"))
                    sb.append(" // <init>");
            }
        }
        sb.append(JDA.nl);

        // Code
        if (!access.contains("abstract")) {
            if (mn.signature != null) {
                sb.append("         <sig:").append(mn.signature).append(">");
            }

            if (mn.annotationDefault != null) {
                sb.append(mn.annotationDefault);
                sb.append("\n");
            }

            InstructionPrinter insnPrinter = getInstructionPrinter(mn, args);

            addAttrList(mn.attrs, "attr", sb, insnPrinter);
            addAttrList(mn.invisibleAnnotations, "invisAnno", sb, insnPrinter);
            addAttrList(mn.invisibleAnnotations, "invisLocalVarAnno", sb, insnPrinter);
            addAttrList(mn.invisibleTypeAnnotations, "invisTypeAnno", sb, insnPrinter);
            addAttrList(mn.localVariables, "localVar", sb, insnPrinter);
            addAttrList(mn.visibleAnnotations, "visAnno", sb, insnPrinter);
            addAttrList(mn.visibleLocalVariableAnnotations, "visLocalVarAnno", sb, insnPrinter);
            addAttrList(mn.visibleTypeAnnotations, "visTypeAnno", sb, insnPrinter);

            // Exception table
            for (Object o : mn.tryCatchBlocks) {
                TryCatchBlockNode tcbn = (TryCatchBlockNode) o;
                sb.append("         ");
                sb.append("TryCatch: L");
                sb.append(insnPrinter.resolveLabel(tcbn.start));
                sb.append(" to L");
                sb.append(insnPrinter.resolveLabel(tcbn.end));
                sb.append(" handled by L");
                sb.append(insnPrinter.resolveLabel(tcbn.handler));
                sb.append(": ");
                if (tcbn.type != null)
                    sb.append(tcbn.type);
                else
                    sb.append("Type is null.");
                sb.append(JDA.nl);
            }

            // Instructions
            for (String insn : insnPrinter.createPrint()) {
                sb.append("         ");
                sb.append(insn);
                sb.append(JDA.nl);
            }

            // Closing brace
            sb.append("     }" + JDA.nl);
        }

        return sb;
    }

    protected InstructionPrinter getInstructionPrinter(MethodNode m, TypeAndName[] args) {
        return new InstructionPrinter(this, m, args);
    }

    protected static void addAttrList(List<?> list, String name, PrefixedStringBuilder sb, InstructionPrinter insnPrinter) {
        if (list == null)
            return;
        if (list.size() > 0) {
            for (Object o : list) {
                sb.append("         <");
                sb.append(name);
                sb.append(":");
                sb.append(printAttr(o, insnPrinter));
                sb.append(">");
                sb.append("\n");
            }
            sb.append("\n");
        }
    }

    protected static String printAttr(Object o, InstructionPrinter insnPrinter) {
        if (o instanceof LocalVariableNode) {
            LocalVariableNode lvn = (LocalVariableNode) o;
            return "index=" + lvn.index + " , name=" + lvn.name + " , desc=" + lvn.desc + ", sig=" + lvn.signature + ", start=L" + insnPrinter.resolveLabel(lvn.start) + ", end=L" + insnPrinter.resolveLabel(lvn.end);
        } else if (o instanceof AnnotationNode) {
            AnnotationNode an = (AnnotationNode) o;
            StringBuilder sb = new StringBuilder();
            sb.append("desc = ");
            sb.append(an.desc);
            sb.append(" , values = ");
            if (an.values != null) {
                sb.append(Arrays.toString(an.values.toArray()));
            } else {
                sb.append("[]");
            }
            return sb.toString();
        }
        if (o == null)
            return "";
        return o.toString();
    }

    protected static String getAccessString(int access) {
        // public, protected, private, abstract, static,
        // final, synchronized, native & strictfp are permitted
        List<String> tokens = new ArrayList<>();
        if ((access & Opcodes.ACC_PUBLIC) != 0)
            tokens.add("public");
        if ((access & Opcodes.ACC_PRIVATE) != 0)
            tokens.add("private");
        if ((access & Opcodes.ACC_PROTECTED) != 0)
            tokens.add("protected");
        if ((access & Opcodes.ACC_STATIC) != 0)
            tokens.add("static");
        if ((access & Opcodes.ACC_ABSTRACT) != 0)
            tokens.add("abstract");
        if ((access & Opcodes.ACC_FINAL) != 0)
            tokens.add("final");
        if ((access & Opcodes.ACC_SYNCHRONIZED) != 0)
            tokens.add("synchronized");
        if ((access & Opcodes.ACC_NATIVE) != 0)
            tokens.add("native");
        if ((access & Opcodes.ACC_STRICT) != 0)
            tokens.add("strictfp");
        if ((access & Opcodes.ACC_BRIDGE) != 0)
            tokens.add("bridge");
        if ((access & Opcodes.ACC_VARARGS) != 0)
            tokens.add("varargs");
        if (tokens.size() == 0)
            return "";
        // hackery delimeters
        StringBuilder sb = new StringBuilder(tokens.get(0));
        for (int i = 1; i < tokens.size(); i++) {
            sb.append(" ");
            sb.append(tokens.get(i));
        }
        return sb.toString();
    }

    boolean createComments() {
        return parent.getSettings().getEntry("debug-helpers").getBool();
    }

    boolean createLabelBrackets() {
        return parent.getSettings().getEntry("append-brackets-to-labels").getBool();
    }

    boolean createDescriptors() {
        return parent.getSettings().getEntry("show-method-descriptors").getBool();
    }
}