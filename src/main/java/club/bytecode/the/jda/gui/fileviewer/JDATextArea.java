package club.bytecode.the.jda.gui.fileviewer;

import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.decompilers.FernflowerDecompiler;
import club.bytecode.the.jda.gui.search.SearchDialog;
import club.bytecode.the.jda.settings.Settings;
import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparser.Navigator;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFactory;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.util.TextRange;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class JDATextArea extends RSyntaxTextArea {
    private List<String> lines;
    private Map<Integer, String> comments;
    private CompilationUnit ast = null;

    private TokenWrapper currentlySelectedToken;

    public JDATextArea(String text) {
        this(text, JDAJavaTokenizer.SYNTAX_STYLE_JDA_JAVA);
        try {
            ast = JavaParser.parse(text);
        } catch(ParseProblemException e) {
            ast = null;
        }
    }

    public JDATextArea(String text, String language) {
        comments = new HashMap<>();

        setSyntaxEditingStyle(language);
        setCodeFoldingEnabled(true);
        setAntiAliasingEnabled(true);

        setText(text);
        setCaretPosition(0);
        setFont(Settings.getCodeFont());

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
                    for (Map.Entry<TextRange, String> wtf : FernflowerDecompiler.dank.entrySet()) {
                        if (wtf.getKey().contains(cursorPos)) {
                            System.out.println(wtf.getValue());
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

    private boolean isIdentifierSelected() {
        return currentlySelectedToken != null && currentlySelectedToken.getType() == TokenTypes.IDENTIFIER;
    }

    private boolean isStringSelected() {
        return currentlySelectedToken != null && currentlySelectedToken.getType() == TokenTypes.LITERAL_STRING_DOUBLE_QUOTE;
    }

        interface NodeHandler {
                        boolean handle(Node node);
                    }
    private void doXrefDialog() {
        String tokenName;
        if (getSelectedText() != null) {
            tokenName = getSelectedText();
        } else if (isIdentifierSelected()) {
            tokenName = currentlySelectedToken.getLexeme();
        } else if (isStringSelected()) {
            tokenName = currentlySelectedToken.getLexeme();
            tokenName = tokenName.substring(1, tokenName.length() - 1);
            new SearchDialog(tokenName, JDA.constantSearchCallback.apply(tokenName)).setVisible(true);
            return;
        } else {
            return;
        }

        new SearchDialog(tokenName, JDA.constantSearchCallback.apply(tokenName)).setVisible(true);
    }

    private void doRenameDialog() {
        if (!isIdentifierSelected())
            return;
        String oldName = currentlySelectedToken.getLexeme();
        String newName = JOptionPane.showInputDialog("Choose a new name", oldName);
        JOptionPane.showMessageDialog(this, "Not implemented");
    }

    public class JDATextAreaKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_SLASH:
                    addCommentDialog();
                    break;
                case KeyEvent.VK_X:
                    doXrefDialog();
                    break;
                case KeyEvent.VK_N:
                    doRenameDialog();
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
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
