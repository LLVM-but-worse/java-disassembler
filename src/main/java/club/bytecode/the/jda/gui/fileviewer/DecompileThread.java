package club.bytecode.the.jda.gui.fileviewer;

import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.api.ExceptionUI;
import club.bytecode.the.jda.decompilers.JDADecompiler;
import club.bytecode.the.jda.decompilers.bytecode.BytecodeDecompiler;
import club.bytecode.the.jda.decompilers.filter.DecompileFilter;
import club.bytecode.the.jda.settings.Settings;
import com.strobel.annotations.Nullable;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.objectweb.asm.tree.ClassNode;

import javax.swing.*;
import java.awt.*;

/**
 * Updates a pane
 *
 * @author Konloch
 */
public class DecompileThread extends Thread {

    private JDADecompiler decompiler;
    private int paneId;
    private JPanel target;
    private ClassViewer viewer;
    @Nullable private JButton button; // this needs to be refactored into something event-based, not a stupid hack like this! 

    public DecompileThread(ClassViewer viewer, JDADecompiler decompiler, int paneId, JPanel target, @Nullable JButton button) {
        this.decompiler = decompiler;
        this.paneId = paneId;
        this.target = target;
        this.viewer = viewer;
        this.button = button;
        JDA.setBusy(true);
    }

    public void run() {
        try {
            String decompileResult;
            
            ClassNode cn = viewer.getFile().container.loadClass(viewer.getFile().name);
            if (cn == null) {
                decompileResult = "// The file was removed during the reload.";
            } else {
                decompiler.applyFilters(cn);
                decompileResult = decompiler.decompileClassNode(viewer.getFile().container, cn);
            }
            
            RSyntaxTextArea panelArea;
            if (decompiler instanceof BytecodeDecompiler) {
                panelArea = new BytecodeSyntaxArea();
            } else {
                panelArea = new RSyntaxTextArea();
                panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
            }
            panelArea.setCodeFoldingEnabled(true);
            panelArea.setAntiAliasingEnabled(true);
            
            final RTextScrollPane scrollPane = new RTextScrollPane(panelArea);
            panelArea.setText(stripUndisplayableChars(decompileResult));
            panelArea.setCaretPosition(0);
            panelArea.setEditable(viewer.isPaneEditable(paneId));
            StringBuilder topLabelText = new StringBuilder(decompiler.getName());
            for (DecompileFilter filter : decompiler.getSettings().getEnabledFilters()) {
                topLabelText.append(" + ").append(filter.getName());
            }
            scrollPane.setColumnHeaderView(new JLabel(topLabelText.toString()));
            panelArea.setFont(new Font(Settings.FONT_FAMILY.getString(), Settings.FONT_OPTIONS.getInt(), Settings.FONT_SIZE.getInt()));

            SwingUtilities.invokeLater(() -> target.add(scrollPane));
            viewer.updatePane(paneId, panelArea, decompiler);
        } catch (Exception e) {
            new ExceptionUI(e);
        } finally {
            viewer.resetDivider();
            JDA.setBusy(false);
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
                result.append(s, startIdx, idx);
                result.append("\\u").append(Integer.toHexString(c));
                startIdx = idx + 1;
            }
            idx++;
        }
        if (idx > startIdx)
            result.append(s, startIdx, idx);
        return result.toString();
    }

    private boolean isUndisplayable(char c) {
        return c >= 255 || c == 127;
    }
}
