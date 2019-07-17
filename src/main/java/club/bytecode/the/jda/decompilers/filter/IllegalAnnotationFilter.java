package club.bytecode.the.jda.decompilers.filter;

import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.api.JDANamespace;
import org.objectweb.asm.tree.ClassNode;

public class IllegalAnnotationFilter implements DecompileFilter {
    @Override
    public void process(ClassNode cn) {
        cn.methods.forEach(methodNode -> {
            if (methodNode.invisibleAnnotations != null)
                methodNode.invisibleAnnotations.removeIf(node -> node.desc.equals("@"));
        });

        if (cn.invisibleAnnotations != null)
            cn.invisibleAnnotations.removeIf(node -> node.desc.equals("@"));
    }

    @Override
    public String getName() {
        return "Kill illegal annotations";
    }

    @Override
    public JDANamespace getNamespace() {
        return JDA.namespace;
    }
}
