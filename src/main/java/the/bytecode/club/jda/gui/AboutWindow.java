package the.bytecode.club.jda.gui;

import org.apache.commons.io.IOUtils;
import the.bytecode.club.jda.Resources;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;

/**
 * The about frame.
 *
 * @author Konloch
 */
public class AboutWindow extends JFrame
{
    private static final long serialVersionUID = -8230501978224923296L;
    private JEditorPane editorPane;

    public AboutWindow()
    {
        this.setIconImages(Resources.iconList);
        setSize(new Dimension(800, 800));
        setType(Type.UTILITY);
        setTitle("Java DisAssembler - About - https://the.bytecode.club");
        getContentPane().setLayout(new CardLayout(0, 0));
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBar(null);

        getContentPane().add(scrollPane, "name_322439757638784");
        editorPane = new JEditorPane();
        editorPane.setEditorKit(new HTMLEditorKit());
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        try
        {
            editorPane.setText(IOUtils.toString(Resources.class.getResourceAsStream("/intro.html"), "UTF-8"));
        }
        catch (IOException e)
        {
            System.err.println("Couldn't load about html:");
            e.printStackTrace();
        }
        scrollPane.setViewportView(editorPane);
    }

    @Override
    public void setVisible(boolean b)
    {
        super.setVisible(b);
    }
}
