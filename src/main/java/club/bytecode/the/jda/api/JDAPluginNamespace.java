package club.bytecode.the.jda.api;

public class JDAPluginNamespace implements JDANamespace {
    private final JDAPlugin plugin;
    
    public JDAPluginNamespace(JDAPlugin plugin) {
        this.plugin = plugin;
    }

    public JDAPlugin getPlugin() {
            return plugin;
        }
    
    @Override
    public String getName() {
        return plugin.getName();
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
