package club.bytecode.the.jda.gui.fileviewer;

import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.api.ExceptionUI;
import com.strobel.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
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
import java.util.HashMap;
import java.util.Map;

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

        String lex = fileExtTypes.getOrDefault(FilenameUtils.getExtension(name), SyntaxConstants.SYNTAX_STYLE_NONE);
        if (contentsS.startsWith("<?xml") || contentsS.startsWith("<xml"))
            lex = SyntaxConstants.SYNTAX_STYLE_XML;
        if (contentsS.startsWith("<?php"))
            lex = SyntaxConstants.SYNTAX_STYLE_PHP;
        panelArea.setSyntaxEditingStyle(lex);

        panelArea.setText(contentsS);

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

    private static Map<String, String> fileExtTypes = new HashMap<>();

    static {
        fileExtTypes.put("xml", SyntaxConstants.SYNTAX_STYLE_XML);
        fileExtTypes.put("py", SyntaxConstants.SYNTAX_STYLE_PYTHON);
        fileExtTypes.put("python", SyntaxConstants.SYNTAX_STYLE_PYTHON);
        fileExtTypes.put("rb", SyntaxConstants.SYNTAX_STYLE_RUBY);
        fileExtTypes.put("ruby", SyntaxConstants.SYNTAX_STYLE_RUBY);
        fileExtTypes.put("java", SyntaxConstants.SYNTAX_STYLE_JAVA);
        fileExtTypes.put("html", SyntaxConstants.SYNTAX_STYLE_HTML);
        fileExtTypes.put("css", SyntaxConstants.SYNTAX_STYLE_CSS);
        fileExtTypes.put("properties", SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
        fileExtTypes.put("mf", SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
        fileExtTypes.put("sf", SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
        fileExtTypes.put("php", SyntaxConstants.SYNTAX_STYLE_PHP);
        fileExtTypes.put("js", SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        fileExtTypes.put("bat", SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH);
        fileExtTypes.put("sh", SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
        fileExtTypes.put("c", SyntaxConstants.SYNTAX_STYLE_C);
        fileExtTypes.put("cpp", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
        fileExtTypes.put("scala", SyntaxConstants.SYNTAX_STYLE_SCALA);
        fileExtTypes.put("clojure", SyntaxConstants.SYNTAX_STYLE_CLOJURE);
        fileExtTypes.put("groovy", SyntaxConstants.SYNTAX_STYLE_GROOVY);
        fileExtTypes.put("lua", SyntaxConstants.SYNTAX_STYLE_LUA);
        fileExtTypes.put("sql", SyntaxConstants.SYNTAX_STYLE_SQL);
        fileExtTypes.put("json", SyntaxConstants.SYNTAX_STYLE_JSON);
        fileExtTypes.put("jsp", SyntaxConstants.SYNTAX_STYLE_JSP);
    }
}
