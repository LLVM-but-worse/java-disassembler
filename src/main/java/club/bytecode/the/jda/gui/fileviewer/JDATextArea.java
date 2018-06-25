package club.bytecode.the.jda.gui.fileviewer;

import club.bytecode.the.jda.settings.Settings;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class JDATextArea extends RSyntaxTextArea {
    private List<String> lines;
    private Map<Integer, String> comments;

    private TokenWrapper currentlySelectedToken;

    public JDATextArea(String text) {
        comments = new HashMap<>();

        setSyntaxEditingStyle(JDAJavaTokenizer.SYNTAX_STYLE_JDA_JAVA);
        setCodeFoldingEnabled(true);
        setAntiAliasingEnabled(true);

        setText(text);
        setCaretPosition(0);
        setFont(new Font(Settings.FONT_FAMILY.getString(), Settings.FONT_OPTIONS.getInt(), Settings.FONT_SIZE.getInt()));

        setEditable(false);
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                getCaret().setVisible(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });
        addKeyListener(new JDATextAreaKeyListener());

        addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                try {
                    currentlySelectedToken = null;
                    int cursorPos = e.getDot();
                    int line = getLineOfOffset(cursorPos);
                    for (Token t = getTokenListForLine(line); t != null; t = t.getNextToken()) {
                        if (t.getOffset() <= cursorPos && t.getEndOffset() > cursorPos) {
                            currentlySelectedToken = new TokenWrapper(t);
                            break;
                        }
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                repaint();
            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    @Override
    public Color getBackgroundForToken(Token token) {
        if (token.isWhitespace())
            return super.getBackgroundForToken(token);
        if (token.getType() == TokenTypes.SEPARATOR)
            return super.getBackgroundForToken(token);
        // System.out.println("" + token.getLexeme() + " @ " + token.getOffset());
        if (currentlySelectedToken != null) {
            if (currentlySelectedToken.overlaps(token)) {
                return new Color(255, 255, 0);
            }
            if (token.getLexeme().equals(currentlySelectedToken.getLexeme())) {
                return new Color(255, 255, 128);
            }
        }
        return super.getBackgroundForToken(token);
    }

    @Override
    public void setText(String text) {
        lines = Collections.unmodifiableList(Arrays.asList(text.split("\n")));
        super.setText(text);
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
        if (line > lines.size())
            return;
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
            if (e.getKeyChar() != 0xFFFF)
                e.consume();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            if (e.getKeyChar() != 0xFFFF)
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

    class TokenWrapper {
        private final int offset, length, type;

        public TokenWrapper(Token t) {
            offset = t.getOffset();
            length = t.length();
            type = t.getType();
        }

        public boolean overlaps(Token t) {
            return t.getOffset() >= offset && t.getOffset() <= offset + length;
        }

        public boolean equivalent(Token t) {
            return t.getOffset() == offset && t.length() == length && t.getType() == type;
        }

        public String getLexeme() {
            return getText().substring(offset, offset + length);
        }

        public int getOffset() {
            return offset;
        }

        public int length() {
            return length;
        }

        public int getType() {
            return type;
        }
    }
}
