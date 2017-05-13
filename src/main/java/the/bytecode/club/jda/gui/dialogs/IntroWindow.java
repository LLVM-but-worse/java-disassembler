package the.bytecode.club.jda.gui.dialogs;

import org.apache.commons.io.IOUtils;
import the.bytecode.club.jda.Resources;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;

public class IntroWindow extends JFrame {
    private static final long serialVersionUID = -8230501978224923296L;
    private JEditorPane editorPane;

    public IntroWindow() {
        this.setIconImages(Resources.iconList);
        setSize(new Dimension(800, 800));
        setType(Type.UTILITY);
        setTitle("JDA - Help");
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
            editorPane.setText(IOUtils.toString(Resources.class.getResourceAsStream("/the/bytecode/club/jda/html/intro.html"), "UTF-8"));
        } catch (IOException e) {
            System.err.println("Couldn't load intro html:");
            e.printStackTrace();
        }
        scrollPane.setViewportView(editorPane);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }
}
