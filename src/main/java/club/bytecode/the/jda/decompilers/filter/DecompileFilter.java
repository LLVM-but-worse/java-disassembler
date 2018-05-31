package club.bytecode.the.jda.decompilers.filter;

import club.bytecode.the.jda.api.JDANamespacedComponent;
import org.objectweb.asm.tree.ClassNode;

public interface DecompileFilter extends JDANamespacedComponent {
    void process(ClassNode cn);
}
