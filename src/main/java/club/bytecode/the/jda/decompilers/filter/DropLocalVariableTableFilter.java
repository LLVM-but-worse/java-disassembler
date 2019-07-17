package club.bytecode.the.jda.decompilers.filter;

import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.api.JDANamespace;
import org.objectweb.asm.tree.ClassNode;

public class DropLocalVariableTableFilter implements DecompileFilter {
    @Override
    public void process(ClassNode cn) {
        cn.methods.forEach(methodNode -> methodNode.localVariables.clear());
    }

    @Override
    public String getName() {
        return "Drop local variable table";
    }

    @Override
    public JDANamespace getNamespace() {
        return JDA.namespace;
    }
}
