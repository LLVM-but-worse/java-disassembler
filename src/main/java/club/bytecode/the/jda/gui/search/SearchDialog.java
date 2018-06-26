package club.bytecode.the.jda.gui.search;

import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.gui.fileviewer.ViewerFile;
import club.bytecode.the.jda.settings.Settings;
import net.miginfocom.swing.MigLayout;
import org.mapleir.stdlib.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class SearchDialog extends JDialog {
    public SearchDialog(String needle, List<ViewerFile> matches) {
        super(new JFrame(), "Search Results", true);
        Container pane = getContentPane();
        pane.setPreferredSize(new Dimension(850, 400));
        pane.setLayout(new MigLayout("fill"));
        pane.add(new JLabel(needle + " found in:"), "pushx, growx, wrap");
        JList<Pair<FileContainer, String>> list = new JList(matches.toArray());
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    ViewerFile vf = matches.get(index);
                    JDA.viewer.navigator.openClassFileToWorkSpace(vf);
                }
            }
        });
        list.setFont(Settings.getCodeFont());
        JScrollPane listScroller = new JScrollPane(list);
        pane.add(listScroller, "grow, push");
        pack();
    }
}
