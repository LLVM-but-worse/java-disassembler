package the.bytecode.club.jda.api;

import the.bytecode.club.jda.Resources;
import the.bytecode.club.jda.gui.SearchPanel;

import javax.swing.*;
import java.awt.*;

/**
 * A simple console GUI.
 *
 * @author Konloch
 */

public class PluginConsole extends JFrame
{

    JTextArea textArea = new JTextArea();
    SearchPanel search = new SearchPanel(textArea);
    JScrollPane scrollPane = new JScrollPane();

    public PluginConsole(String pluginName)
    {
        this.setIconImages(Resources.iconList);
        setTitle("Java DisAssembler - Plugin Console - " + pluginName);
        setSize(new Dimension(542, 316));

        getContentPane().add(scrollPane, BorderLayout.CENTER);

        scrollPane.setViewportView(textArea);
        scrollPane.setColumnHeaderView(search);
        this.setLocationRelativeTo(null);
    }

    /**
     * Appends \r\n to the end of your string, then it puts it on the top.
     *
     * @param t the string you want to append
     */
    public void appendText(String t)
    {
        textArea.append(t + "\r\n");
        textArea.setCaretPosition(textArea.getText().length());
    }

    /**
     * Sets the text
     *
     * @param t the text you want set
     */
    public void setText(String t)
    {
        textArea.setText(t);
        textArea.setCaretPosition(0);
    }

    private static final long serialVersionUID = -6556940545421437508L;

}
