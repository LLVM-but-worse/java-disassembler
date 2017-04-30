package the.bytecode.club.jda.api;

import the.bytecode.club.jda.FileContainer;

public interface Plugin {
    int onGUILoad();

    int onExit();

    int onAddFile(FileContainer container);
}
