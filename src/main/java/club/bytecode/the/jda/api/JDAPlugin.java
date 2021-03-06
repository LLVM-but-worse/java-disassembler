package club.bytecode.the.jda.api;

import club.bytecode.the.jda.FileContainer;

public interface JDAPlugin extends JDANamespacedComponent{
    
    /**
     * Callback for when the plugin is loaded.
     */
    void onLoad();

    /**
     * Callback for when the plugin is unloaded.
     */
    void onUnload();
    
    /**
     * Callback for when the JDA main gui has loaded.
     * Will always be called after {@link #onLoad() onLoad}.
     */
    void onGUILoad();
    
    /**
     * Callback for when JDA is exiting.
     * Will always be called before {@link #onUnload() onLoad}.
     */
    void onExit();

    /**
     * Callback for when a file has been opened in JDA.
     */
    void onOpenFile(FileContainer container);
    
    /**
     * Callback for when a file has been closed in JDA.
     */
    void onCloseFile(FileContainer container);

    /**
     * Called when the plugin's button in the Edit/Plugins menu is pressed.
     */
    void onPluginButton();

    /**
     * Should return the JDAPluginNamespace for this plugin
     */
    @Override
    JDAPluginNamespace getNamespace();
}
