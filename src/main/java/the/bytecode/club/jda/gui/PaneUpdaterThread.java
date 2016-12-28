package the.bytecode.club.jda.gui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import the.bytecode.club.jda.JDA;
import the.bytecode.club.jda.api.ExceptionUI;
import the.bytecode.club.jda.decompilers.Decompiler;
import the.bytecode.club.jda.settings.Settings;

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
            RSyntaxTextArea panelArea = new RSyntaxTextArea();
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
            panelArea.setCodeFoldingEnabled(true);
            panelArea.setAntiAliasingEnabled(true);
            final RTextScrollPane scrollPane = new RTextScrollPane(panelArea);
            String decompileResult = decompiler.decompileClassNode(viewer.container, viewer.cn);
            panelArea.setText(stripUndisplayableChars(decompileResult));
            panelArea.setCaretPosition(0);
            panelArea.setEditable(viewer.isPaneEditable(paneId));
            scrollPane.setColumnHeaderView(new JLabel(decompiler.getName() + " Decompiler - Editable: " + panelArea.isEditable()));
            panelArea.setFont(new Font(Settings.FONT_FAMILY.get(), Settings.FONT_OPTIONS.getInt(), Settings.FONT_SIZE.getInt()));

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

    private String stripUndisplayableChars(String s) {
        StringBuilder result = new StringBuilder();
        int startIdx = 0, idx = 0;
        while (idx < s.length()) {
            char c = s.charAt(idx);
            if (isUndisplayable(c)) {
                result.append(s.substring(startIdx, idx));
                result.append("\\u").append(Integer.toHexString(c));
                startIdx = idx + 1;
            }
            idx++;
        }
        if (idx > startIdx)
            result.append(s.substring(startIdx, idx));
        return result.toString();
    }

    private boolean isUndisplayable(char c) {
        return c >= 255 || c == 127;
    }
}