package club.bytecode.the.jda.gui.fileviewer;

import club.bytecode.the.jda.settings.Settings;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public class JDATextArea extends RSyntaxTextArea {
    private List<String> lines;
    private Map<Integer, String> comments;

    public JDATextArea(String text) {
        lines = Collections.unmodifiableList(Arrays.asList(text.split("\n")));
        comments = new HashMap<>();

        setCodeFoldingEnabled(true);
        setAntiAliasingEnabled(true);

        setText(text);
        setCaretPosition(0);
        setFont(new Font(Settings.FONT_FAMILY.getString(), Settings.FONT_OPTIONS.getInt(), Settings.FONT_SIZE.getInt()));

        setEditable(true);
        addKeyListener(new JDATextAreaKeyListener());
    }

    private void addCommentDialog() {
        setComment(getCaretLineNumber(), JOptionPane.showInputDialog("Enter a comment"));
    }

    private void setComment(int line, String comment) {
        if (comment == null || comment.isEmpty())
            comments.remove(line);
        else
            comments.put(line, comment);
        resetLine(line);
    }

    private void resetLine(int line) {
        String lineText = lines.get(line);
        if (comments.containsKey(line)) {
            lineText += " // " + comments.get(line);
        }
        try {
            replaceRange(lineText, getLineStartOffset(line), getLineEndOffset(line) - 1);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public class JDATextAreaKeyListener extends NonRepeatKeyListener {
        public JDATextAreaKeyListener() {
        }

        @Override
        public void keyTyped(KeyEvent e) {
            e.consume();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            e.consume();
        }

        @Override
        public void keyReleased(KeyEvent e) {
            super.keyReleased(e);
        }

        @Override
        protected void onDown(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_SLASH:
                    addCommentDialog();
            }
        }

        @Override
        protected void onUp(KeyEvent e) {

        }
    }
}
