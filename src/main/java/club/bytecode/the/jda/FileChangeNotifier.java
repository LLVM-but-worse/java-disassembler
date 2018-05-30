package club.bytecode.the.jda;

import club.bytecode.the.jda.gui.fileviewer.ViewerFile;
import org.objectweb.asm.tree.ClassNode;

/**
 * Used to represent whenever a file has been opened
 *
 * @author Konloch
 */

public interface FileChangeNotifier {
    void openClassFile(ViewerFile file, ClassNode cn);

    void openFile(ViewerFile file, byte[] contents);
}
