package club.bytecode.the.jda.gui.search;

import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.gui.components.SortedUniqueListModel;
import club.bytecode.the.jda.gui.fileviewer.ViewerFile;
import club.bytecode.the.jda.settings.Settings;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

public class SearchDialog extends JDialog {
    private final List<ViewerFile> searchResults;
    private final JList<ViewerFile> list;
    private final JTextArea searchBox;

    private String oldFilter = "";

    public SearchDialog(String needle, List<ViewerFile> matches) {
        super(new JFrame(), "Search Results", true);
        searchResults = matches;
        Container pane = getContentPane();
        pane.setPreferredSize(new Dimension(850, 400));
        pane.setLayout(new MigLayout("fill"));
        pane.add(new JLabel(needle + " found in:"), "pushx, growx, wrap");
        list = new JList<>(createSortedListModel());
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    openResult(index);
                }
            }
        });

        searchBox = new JTextArea();
        searchBox.setRows(1);
        searchBox.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    list.requestFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        searchBox.getDocument().addDocumentListener(new DocumentListener(){
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) {}
            private void filter() {
                String filter = searchBox.getText();
                updateFilter((SortedUniqueListModel<ViewerFile>) list.getModel(), filter);
                oldFilter = filter;
            }
        });

        list.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') e.consume();
                else focusSearch(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) openResult(list.getSelectedIndex());
                else focusSearch(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) e.consume();
                else focusSearch(e);
            }
        });

        list.setFont(Settings.getCodeFont());
        JScrollPane listScroller = new JScrollPane(list);

        pane.add(listScroller, "grow, push, wrap");
        pane.add(searchBox, "grow");
        pack();
    }

    public void openResult(int index) {
        ViewerFile vf = searchResults.get(index);
        JDA.viewer.navigator.openClassFileToWorkSpace(vf);
    }

    public void focusSearch(KeyEvent e) {
        searchBox.setText("");
        searchBox.requestFocus();
        forwardKeyEvent(searchBox, e);
    }

    private void forwardKeyEvent(Component receiver, KeyEvent e) {
        receiver.dispatchEvent(new KeyEvent(receiver, e.getID(), e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar()));
    }

    private ListModel<ViewerFile> createSortedListModel() {
        SortedUniqueListModel<ViewerFile> model = new SortedUniqueListModel<>();
        model.addAll(searchResults);
        return model;
    }

    public void updateFilter(SortedUniqueListModel<ViewerFile> model, String filter) {
        if (oldFilter.equals(filter))
            return;

        if (filter.isEmpty()) {
            model.deferUpdates();
            model.clear();
            model.addAll(searchResults);
            model.commitUpdates();
            return;
        }

        model.deferUpdates(); // make sure to commit me
        String filterLower = filter.toLowerCase();
        String oldFilterLower = oldFilter.toLowerCase();
        if (oldFilterLower.contains(filterLower)) {
            for (ViewerFile vf : searchResults) {
                String s = vf.toString().toLowerCase();
                if (s.contains(filterLower)) {
                    model.add(vf);
                }
            }
        } else if (filterLower.contains(oldFilterLower)) {
            for (Iterator<ViewerFile> it = model.iterator(); it.hasNext(); ) {
                ViewerFile vf = it.next(); // copy because we remove as we iterate. inefficient
                if (!vf.toString().toLowerCase().contains(filterLower)) {
                    it.remove();
                }
            }
        } else for (ViewerFile vf : searchResults) {
            if (!vf.toString().toLowerCase().contains(filter)) {
                model.removeElement(vf);
            } else {
                model.add(vf);
            }
        }
        model.commitUpdates();
    }
}
