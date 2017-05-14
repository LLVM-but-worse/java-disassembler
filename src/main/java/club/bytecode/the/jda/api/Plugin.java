package club.bytecode.the.jda.api;

import club.bytecode.the.jda.FileContainer;

public interface Plugin {
    int onGUILoad();

    int onExit();

    int onAddFile(FileContainer container);
}
