package the.bytecode.club.jda.gui.navigation;

import javax.swing.*;
import javax.swing.tree.TreePath;

class TreeContextMenu extends JPopupMenu {
    private FileNavigationPane fileNavigationPane;
    JMenuItem expandAll, collapseAll;

    public TreeContextMenu(FileNavigationPane fileNavigationPane){
        this.fileNavigationPane = fileNavigationPane;
        add(expandAll = new JMenuItem("Expand All"));
        add(collapseAll = new JMenuItem("Collapse All"));

        expandAll.addActionListener(e -> {
            if (fileNavigationPane.tree.getSelectionPaths() != null) {
                for (TreePath path : fileNavigationPane.tree.getSelectionPaths()) {
                    fileNavigationPane.treeDfs(path, fileNavigationPane.tree::expandPath, null);
                    fileNavigationPane.tree.expandPath(path);
                }
            }
        });

        collapseAll.addActionListener(e -> {
            if (fileNavigationPane.tree.getSelectionPaths() != null) {
                for (TreePath path : fileNavigationPane.tree.getSelectionPaths()) {
                    fileNavigationPane.treeDfs(path, null, fileNavigationPane.tree::collapsePath);
                }
            }
        });
    }
}
