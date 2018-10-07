package club.bytecode.the.jda.gui.fileviewer;

import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.JDA;
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
        ast = JavaParser.parse(text);
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
        class NodeIterator {


            private NodeHandler nodeHandler;

            public NodeIterator(NodeHandler nodeHandler) {
                this.nodeHandler = nodeHandler;
            }

            public void explore(Node node) {
             if(   nodeHandler.handle(node)){
                    for (Node child : node.getChildNodes()) {
                        explore(child);
                    }}
            }
        }

        if (ast != null) {
            int startOff = Math.min(getCaret().getDot(), getCaret().getMark());
            int endOff = Math.max(getCaret().getDot(), getCaret().getMark());
            int caretLine = getCaretLineNumber()+1;
            Position caretPos = new Position(getCaretLineNumber()+1, getCaretOffsetFromLineStart());
            CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
            JavaParser.getStaticConfiguration().setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));
            // combinedTypeSolver.add(new JavaParserTypeSolver(getText()));
            for(FileContainer fc : JDA.getOpenFiles()) {

                try {
                    combinedTypeSolver.add(new JarTypeSolver(fc.file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            combinedTypeSolver.add(new ReflectionTypeSolver());
            try {
                combinedTypeSolver.add(new JarTypeSolver(new File("C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\rt.jar")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            JavaParserFacade solver = JavaParserFacade.get(combinedTypeSolver);
                new NodeIterator(node -> {
                    Range r = node.getRange().orElse(null);
                    if (r == null) return false;
                    if (r.begin.compareTo(caretPos) <= 0 && r.end.compareTo(caretPos) >= 0) {
                        // if (r.begin.line == caretLine && r.end.line == caretLine) {
                        if (node instanceof MethodCallExpr) {
                            System.out.println(solver.solve((MethodCallExpr) node));
                        } else if (node instanceof ClassOrInterfaceType) {
                            System.out.println(JavaParserFactory.getContext(node, solver.getTypeSolver()).solveType(((ClassOrInterfaceType) node).getName().getId(), solver.getTypeSolver()));
                        } else if (node instanceof NameExpr) {
                            System.out.println(JavaParserFactory.getContext(node, solver.getTypeSolver()).solveType(((NameExpr) node).getName().getId(), solver.getTypeSolver()));
                        } else if (node instanceof SimpleName) {
                            System.out.println(JavaParserFactory.getContext(node, solver.getTypeSolver()).solveType(((SimpleName) node).getId(), solver.getTypeSolver()));
                        } else if (node instanceof FieldAccessExpr) {
                            System.out.println(solver.solve((FieldAccessExpr) node));
                        } else if (node instanceof ExplicitConstructorInvocationStmt) {
                            System.out.println(solver.solve((ExplicitConstructorInvocationStmt) node));
                        } else if (node instanceof MethodDeclaration) {
                            System.out.println(solver.getTypeOfThisIn(node));
                        } else if (node instanceof FieldDeclaration) {
                            System.out.println(solver.getTypeOfThisIn(node));
                        } else if (node instanceof ClassOrInterfaceDeclaration) {
                            System.out.println(solver.getTypeOfThisIn(node));
                        } else if (node instanceof ImportDeclaration) {
                            System.out.println(((ImportDeclaration) node).getName());
                        }
                        // }
                        //         System.out.println(node);
                        //         System.out.println();
                        // }
                        return true;
                    }
                    return false;
                }).explore(ast);
            // for (JavaToken tok = tr.getBegin(); tok != null; tok = tok.getNextToken().orElse(null)) {
            // Range r = tok.getRange().orElse(null);
            return;
        }

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
