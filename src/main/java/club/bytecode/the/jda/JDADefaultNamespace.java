package club.bytecode.the.jda;

import club.bytecode.the.jda.api.JDANamespace;

public class JDADefaultNamespace implements JDANamespace {
    public static final JDADefaultNamespace INSTANCE = new JDADefaultNamespace();
    
    private JDADefaultNamespace() {
    }
    
    @Override
    public String getName() {
        return "JDA";
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
