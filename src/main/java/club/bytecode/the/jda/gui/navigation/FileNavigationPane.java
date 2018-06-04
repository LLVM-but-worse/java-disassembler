package club.bytecode.the.jda.gui.navigation;

import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.FileDrop;
import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.Resources;
import club.bytecode.the.jda.gui.JDAWindow;
import club.bytecode.the.jda.gui.fileviewer.ViewerFile;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.function.Consumer;

/**
 * The file navigation pane.
 *
 * @author Konloch
 * @author WaterWolf
 * @author afffsdd
 */

@SuppressWarnings("serial")
public class FileNavigationPane extends JDAWindow {
    private static final String quickSearchText = "File search";

    JCheckBox matchCase = new JCheckBox("Match case");

    FileNode treeRoot = new FileNode("Loaded Files:");
    FileTree tree = new FileTree(treeRoot);
    final JTextField quickSearch = new JTextField(quickSearchText);

    public transient KeyAdapter search = new KeyAdapter() {
        @Override
        public void keyPressed(final KeyEvent ke) {
            if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                final String qt = quickSearch.getText();
                quickSearch.setText("");
                quickSearch(qt);
            } else if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                tree.grabFocus();
            }

            JDA.checkHotKey(ke);
        }
    };

    private void quickSearch(String qt) {
        if (!matchCase.isSelected())
            qt = qt.toLowerCase();
        String[] path = qt.split("\\.");
        String searchFilename = path[path.length - 1];

        FileNode curNode = treeRoot;
        @SuppressWarnings("unchecked")
        Enumeration<FileNode> enums = curNode.depthFirstEnumeration();
        while (enums != null && enums.hasMoreElements()) {
            FileNode node = enums.nextElement();
            if (!node.isLeaf()) {
                continue;
            }

            // Check filename
            String leafFilename = (String) (node.getUserObject());
            if (!matchCase.isSelected())
                leafFilename = leafFilename.toLowerCase();
            if (!leafFilename.contains(searchFilename)) {
                continue;
            }

            // Check for path match
            TreeNode pathArray[] = node.getPath();
            int k = 0;
            StringBuilder fullPath = new StringBuilder();
            while (pathArray != null && k < pathArray.length) {
                FileNode n = (FileNode) pathArray[k];
                String s = (n.getUserObject()).toString();
                fullPath.append(s);
                if (k++ != pathArray.length - 1) {
                    fullPath.append(".");
                }
            }
            String fullPathString = fullPath.toString();
            if (!matchCase.isSelected())
                fullPathString = fullPathString.toLowerCase();

            if (fullPathString.contains(qt)) { // Match found
                final TreePath pathn = new TreePath(node.getPath());
                tree.setSelectionPath(pathn.getParentPath());
                tree.setSelectionPath(pathn);
                tree.makeVisible(pathn);
                tree.scrollPathToVisible(pathn);
                break;
            }
        }
    }

    public FileNavigationPane() {
        super("ClassNavigation", "File Navigator", Resources.fileNavigatorIcon);

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        quickSearch.setForeground(Color.gray);
        setMinimumSize(new Dimension(200, 50));

        this.tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                openTreePath(tree.getPathForLocation(e.getX(), e.getY()));
            }
        });
        tree.setComponentPopupMenu(new TreeContextMenu(this));
        tree.setInheritsPopupMenu(true);

        this.tree.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                JDA.checkHotKey(e);
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (arg0.getSource() instanceof FileTree) {
                        FileTree tree = (FileTree) arg0.getSource();
                        openTreePath(tree.getSelectionPath());
                    }
                } else if (arg0.getKeyCode() == KeyEvent.VK_F && arg0.isControlDown()) {
                    quickSearch.grabFocus();
                }
            }
        });

        quickSearch.addKeyListener(search);
        quickSearch.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent arg0) {
                if (quickSearch.getText().equals(quickSearchText)) {
                    quickSearch.setText(null);
                    quickSearch.setForeground(Color.black);
                }
            }

            @Override
            public void focusLost(final FocusEvent arg0) {
                if (quickSearch.getText().isEmpty()) {
                    quickSearch.setText(quickSearchText);
                    quickSearch.setForeground(Color.gray);
                }
            }
        });

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);

        JPanel p2 = new JPanel();
        p2.setLayout(new BorderLayout());
        p2.add(quickSearch, BorderLayout.NORTH);
        JPanel p3 = new JPanel(new BorderLayout());
        matchCase.setSelected(true);
        p3.add(matchCase, BorderLayout.WEST);
        JPanel p4 = new JPanel(new BorderLayout());
        p3.add(p4, BorderLayout.EAST);
        p2.add(p3, BorderLayout.SOUTH);

        getContentPane().add(p2, BorderLayout.SOUTH);

        new FileDrop(this, files -> {
            if (files.length < 1)
                return;
            JDA.openFiles(files, true);
        });
    }

    public static Dimension defaultDimension = new Dimension(350, -35);
    public static Point defaultPosition = new Point(0, 0);

    @Override
    public Dimension getDefaultSize() {
        return defaultDimension;
    }

    @Override
    public Point getDefaultPosition() {
        return defaultPosition;
    }

    public void openClassFileToWorkSpace(ViewerFile file) {
        JDA.viewer.openClassFile(file);
    }

    public void openFileToWorkSpace(ViewerFile file) {
        JDA.viewer.openFile(file);
    }

    /**
     * Add tree element.
     * If parent is null, it will be added to the root node.
     */
    public FileNode addTreeElement(FileContainer container, FileNode parent) {
        if (parent == null)
            parent = treeRoot;

        FileNode root = new FileNode(container, isJava(container));
        parent.add(root);
        JDATreeCellRenderer renderer = new JDATreeCellRenderer();
        tree.setCellRenderer(renderer);

        if (container.files.size() > 1) {
            for (final Entry<String, byte[]> entry : container.files.entrySet()) {
                String name = entry.getKey();
                final String[] spl = name.split("/");
                FileNode parentNode = root;
                if (spl.length <= 1) {
                    root.add(new FileNode(name, parentNode.isJava));
                } else {
                    for (final String s : spl) {
                        FileNode child = null;
                        for (int i = 0; i < parentNode.getChildCount(); i++) {
                            if (((FileNode) parentNode.getChildAt(i)).getUserObject().equals(s)) {
                                child = (FileNode) parentNode.getChildAt(i);
                                break;
                            }
                        }
                        if (child == null) {
                            child = new FileNode(s, parentNode.isJava);
                            parentNode.add(child);
                        }
                        parentNode = child;
                    }
                }
            }
        }

        parent.sort();
        tree.expandPath(new TreePath(tree.getModel().getRoot()));
        tree.updateUI();

        return root;
    }

    private boolean isJava(FileContainer container) {
        return container.name.endsWith(".java") || container.name.endsWith(".jar") || container.name.endsWith(".class");
    }

    @SuppressWarnings("rawtypes")
    void treeDfs(final TreePath parent, Consumer<TreePath> onPreOrder, Consumer<TreePath> onPostOrder) {
        if (onPreOrder != null)
            onPreOrder.accept(parent);

        final TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (final Enumeration e = node.children(); e.hasMoreElements(); ) {
                final TreeNode n = (TreeNode) e.nextElement();
                final TreePath path = parent.pathByAddingChild(n);
                treeDfs(path, onPreOrder, onPostOrder);
            }
        }

        if (onPostOrder != null)
            onPostOrder.accept(parent);
    }

    public class FileTree extends JTree {
        private static final long serialVersionUID = -2355167326094772096L;
        DefaultMutableTreeNode treeRoot;

        public FileTree(final DefaultMutableTreeNode treeRoot) {
            super(treeRoot);
            this.treeRoot = treeRoot;
        }

        StringMetrics m = null;

        @Override
        public void paint(final Graphics g) {
            try {
                super.paint(g);
                if (m == null) {
                    m = new StringMetrics((Graphics2D) g);
                }
                if (treeRoot.getChildCount() < 1) {
                    g.setColor(new Color(0, 0, 0, 100));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(Color.white);
                    String s = "Drag class/jar/zip here";
                    g.drawString(s, ((int) ((getWidth() / 2) - (m.getWidth(s) / 2))), getHeight() / 2);
                }
            } catch (java.lang.InternalError | java.lang.NullPointerException e) {

            }
        }
    }

    public class FileNode extends DefaultMutableTreeNode {

        public final boolean isJava;

        private static final long serialVersionUID = -8817777566176729571L;

        public FileNode(final Object o, boolean isJava) {
            super(o);
            this.isJava = isJava;
        }

        public FileNode(final Object o) {
            this(o, false);
        }

        @Override
        public void insert(final MutableTreeNode newChild, final int childIndex) {
            super.insert(newChild, childIndex);
        }

        public void sort() {
            recursiveSort(this);
        }

        @SuppressWarnings("unchecked")
        private void recursiveSort(final FileNode node) {
            Collections.sort(node.children, nodeComparator);
            for (FileNode nextNode : (Iterable<FileNode>) node.children) {
                if (nextNode.getChildCount() > 0) {
                    recursiveSort(nextNode);
                }
            }
        }

        protected Comparator<FileNode> nodeComparator = (a, b) ->
        {
            // Ensure nodes with children are always on top
            final boolean aEmpty = a.getChildCount() > 0;
            final boolean bEmpty = b.getChildCount() > 0;
            if (aEmpty && !bEmpty)
                return -1;
            else if (!aEmpty && bEmpty)
                return 1;

            // Try insensitive first, but if they are the same insensitively do it case sensitively
            int compare = a.toString().compareToIgnoreCase(b.toString());
            if (compare != 0)
                return compare;
            else
                return a.toString().compareTo(b.toString());
        };
    }

    /**
     * @author http://stackoverflow.com/a/18450804
     */
    class StringMetrics {

        Font font;
        FontRenderContext context;

        public StringMetrics(Graphics2D g2) {

            font = g2.getFont();
            context = g2.getFontRenderContext();
        }

        Rectangle2D getBounds(String message) {

            return font.getStringBounds(message, context);
        }

        double getWidth(String message) {

            Rectangle2D bounds = getBounds(message);
            return bounds.getWidth();
        }

        double getHeight(String message) {

            Rectangle2D bounds = getBounds(message);
            return bounds.getHeight();
        }

    }

    public void resetWorkspace() {
        treeRoot.removeAllChildren();
        tree.repaint();
        tree.updateUI();
    }

    public void openTreePath(TreePath path) {
        if (path == null)
            return;

        FileContainer container = null;
        int containerLevel;
        for (containerLevel = path.getPathCount() - 1; containerLevel > 0; containerLevel--) {
            Object o = ((FileNode) path.getPathComponent(containerLevel)).getUserObject();
            if (o != null && o instanceof FileContainer) {
                container = (FileContainer) o;
                break;
            }
        }
        if (container == null) {
            System.err.println(path);
            throw new IllegalStateException("Path isn't parented to a container?");
        }

        final StringBuilder nameBuffer = new StringBuilder();
        for (int i = containerLevel + 1; i < path.getPathCount(); i++) {
            nameBuffer.append(path.getPathComponent(i));
            if (i < path.getPathCount() - 1) {
                nameBuffer.append("/");
            }
        }

        // single-file thang
        if (container.files.size() == 1 && nameBuffer.length() == 0) {
            nameBuffer.append(container.files.keySet().iterator().next());
        }

        ViewerFile file = new ViewerFile(container, nameBuffer.toString());
        if (!JDA.hasFile(file)) { // if it's null, it's a directory or some non-leaf tree node
            tree.expandPath(path);
        } else if (file.name.endsWith(".class")) {
            openClassFileToWorkSpace(file);
        } else {
            openFileToWorkSpace(file);
        }
    }

}
