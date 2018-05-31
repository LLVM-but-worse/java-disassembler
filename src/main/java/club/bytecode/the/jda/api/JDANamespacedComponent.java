package club.bytecode.the.jda.api;

public interface JDANamespacedComponent {
    String getName();
    
    JDANamespace getNamespace();
    
    default String getFullName() {
        return getNamespace() + ":" + getName();
    }
}
