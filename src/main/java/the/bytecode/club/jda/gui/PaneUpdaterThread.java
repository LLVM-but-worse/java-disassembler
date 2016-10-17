package the.bytecode.club.jda.gui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import the.bytecode.club.jda.JDA;
import the.bytecode.club.jda.api.ExceptionUI;
import the.bytecode.club.jda.decompilers.Decompiler;

import javax.swing.*;
import java.awt.*;

/**
 * Updates a pane
 *
 * @author Konloch
 */
public class PaneUpdaterThread extends Thread {

    private Decompiler decompiler;
    private int paneId;
    private JPanel target;
    private ClassViewer viewer;
    private JButton button;

    public PaneUpdaterThread(ClassViewer viewer, Decompiler decompiler, int paneId, JPanel target, JButton button) {
        this.decompiler = decompiler;
        this.paneId = paneId;
        this.target = target;
        this.viewer = viewer;
        this.button = button;
    }

    public void run() {
        try {
            final byte[] b = JDA.getClassBytes(viewer.container, viewer.cn.name + ".class");
            RSyntaxTextArea panelArea = new RSyntaxTextArea();
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
            panelArea.setCodeFoldingEnabled(true);
            panelArea.setAntiAliasingEnabled(true);
            final RTextScrollPane scrollPane = new RTextScrollPane(panelArea);
            panelArea.setText(decompiler.decompileClassNode(viewer.cn, b));
            panelArea.setCaretPosition(0);
            panelArea.setEditable(viewer.isPaneEditable(paneId));
            scrollPane.setColumnHeaderView(new JLabel(decompiler.getName() + " Decompiler - Editable: " + panelArea.isEditable()));
            panelArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, (int) JDA.viewer.fontSpinner.getValue()));

            SwingUtilities.invokeLater(() -> target.add(scrollPane));
            viewer.updatePane(paneId, panelArea, decompiler);
        } catch (Exception e) {
            new ExceptionUI(e);
        } finally {
            viewer.resetDivider();
            JDA.viewer.setIcon(false);
            if (button != null)
                button.setEnabled(true);
        }
    }
}