package the.bytecode.club.bytecodeviewer.searching;

import org.objectweb.asm.tree.ClassNode;

import javax.swing.*;

/**
 * Search type details
 *
 * @author WaterWolf
 *
 */

public interface SearchTypeDetails
{
    public JPanel getPanel();

    public void search(ClassNode node, SearchResultNotifier srn, boolean exact);
}
