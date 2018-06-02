package club.bytecode.the.jda;

import club.bytecode.the.jda.gui.fileviewer.ViewerFile;

/**
 * Used to represent whenever a file has been opened
 *
 * @author Konloch
 */

//todo: DELETE!
public interface FileChangeNotifier {
    void openClassFile(ViewerFile file);

    // todo: move the responsibility of byte[] contents to the file viewer too.
    void openFile(ViewerFile file, byte[] contents);
}
