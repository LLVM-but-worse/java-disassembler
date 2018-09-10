package club.bytecode.the.jda.gui.dialogs;

import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.Resources;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;

/**
 * The about frame.
 *
 * @author Konloch
 */
public class AboutWindow extends JFrame {
    private static final long serialVersionUID = -8230501978224923296L;
    private JEditorPane editorPane;

    public AboutWindow() {
        this.setIconImages(Resources.iconList);
        setSize(new Dimension(400, 410));
        setType(Type.UTILITY);
        setTitle("JDA - About");
        getContentPane().setLayout(new CardLayout(0, 0));
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBar(null);

        getContentPane().add(scrollPane);
        editorPane = new JEditorPane();
        editorPane.setEditorKit(new HTMLEditorKit());
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        try {
            String text = IOUtils.toString(Resources.class.getResourceAsStream("/club/bytecode/the/jda/html/about.html"), "UTF-8");
            text = text.replace("$JDA_VERSION$", JDA.version);
            text = text.replace("$JDA_ICON$", Resources.class.getResource("/club/bytecode/the/jda/images/icon.png").toString());
            editorPane.setText(text);
        } catch (IOException e) {
            System.err.println("Couldn't load about html:");
            e.printStackTrace();
        }
        scrollPane.setViewportView(editorPane);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }
}
