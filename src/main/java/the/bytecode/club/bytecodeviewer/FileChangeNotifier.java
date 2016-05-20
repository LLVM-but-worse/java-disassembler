package the.bytecode.club.bytecodeviewer;

import org.objectweb.asm.tree.ClassNode;

/**
 * Used to represent whenever a file has been opened
 *
 * @author Konloch
 */

public interface FileChangeNotifier
{
    void openClassFile(String name, String container, ClassNode cn);

    void openFile(String name, String container, byte[] contents);
}