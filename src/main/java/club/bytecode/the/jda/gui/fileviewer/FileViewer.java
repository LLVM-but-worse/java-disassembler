package club.bytecode.the.jda.gui.fileviewer;

import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.api.ExceptionUI;
import com.strobel.annotations.Nullable;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Represents any open non-class file.
 *
 * @author Konloch
 */

public class FileViewer extends Viewer {

    private static final long serialVersionUID = 6103372882168257164L;

    RSyntaxTextArea panelArea = new RSyntaxTextArea();
    SearchPanel searchPanel;
    JPanel panel2 = new JPanel(new BorderLayout());
    public BufferedImage image;

    public void setContents(byte[] contents) {
        String name = getFile().name.toLowerCase();
        panelArea.setCodeFoldingEnabled(true);
        panelArea.setAntiAliasingEnabled(true);
        RTextScrollPane scrollPane = new RTextScrollPane(panelArea);
        searchPanel = new SearchPanel(panelArea);

        String contentsS = new String(contents);

        if (!isPureAscii(contentsS)) {
            if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                    name.endsWith(".gif") || name.endsWith(".tif") || name.endsWith(".bmp")) {
                try {
                    image = ImageIO.read(new ByteArrayInputStream(contents)); //gifs fail cause of this
                    JLabel label = new JLabel("", new ImageIcon(image), JLabel.CENTER);
                    panel2.add(label, BorderLayout.CENTER);
                    panel2.addMouseWheelListener(e -> {
                        int notches = e.getWheelRotation();
                        if (notches < 0) {
                            image = Scalr.resize(image, Scalr.Method.SPEED, image.getWidth() + 10, image.getHeight() + 10);
                        } else {
                            image = Scalr.resize(image, Scalr.Method.SPEED, image.getWidth() - 10, image.getHeight() - 10);
                        }
                        panel2.removeAll();
                        JLabel label1 = new JLabel("", new ImageIcon(image), JLabel.CENTER);
                        panel2.add(label1, BorderLayout.CENTER);
                        panel2.updateUI();
                    });
                    return;
                } catch (Exception e) {
                    new ExceptionUI(e);
                }
            } else {
                // todo: fallback
                return;
            }
        }

        if (name.endsWith(".xml") || contentsS.startsWith("<?xml") || contentsS.startsWith(("<xml"))) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".py") || name.endsWith(".python")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".rb") || name.endsWith(".ruby")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_RUBY);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".java")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".html")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".css")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSS);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".properties") || name.endsWith(".mf") || name.endsWith(".sf")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".php") || contentsS.startsWith("<?php")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PHP);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".js")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".bat")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".sh")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".c")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".cpp")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".scala")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SCALA);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".clojure")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CLOJURE);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".groovy")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".lua")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_LUA);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".sql")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".json")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
            panelArea.setText(contentsS);
        } else if (name.endsWith(".jsp")) {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSP);
            panelArea.setText(contentsS);
        } else {
            panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
            panelArea.setText(contentsS);
        }

        panelArea.setCaretPosition(0);
        scrollPane.setColumnHeaderView(searchPanel);
        panel2.add(scrollPane);
    }

    static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1

    public static boolean isPureAscii(String v) {
        return asciiEncoder.canEncode(v);
    }

    public FileViewer(ViewerFile file) {
        super(file);
        updateName();
        this.setLayout(new BorderLayout());

        this.add(panel2, BorderLayout.CENTER);

        refresh(null);
    }

    @Override
    public void refresh(@Nullable JButton src) {
        panel2.removeAll();
        setContents(JDA.getFileBytes(getFile()));
        panel2.updateUI();
        if (src != null) {
            src.setEnabled(true);
        }
    }
}
