package the.bytecode.club.jda.gui;

import the.bytecode.club.jda.JDA;
import the.bytecode.club.jda.Resources;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * A simple console GUI.
 *
 * @author Konloch
 */

public class SystemErrConsole extends JFrame
{

    JTextArea textArea = new JTextArea();
    SearchPanel search = new SearchPanel(textArea);
    JScrollPane scrollPane = new JScrollPane();
    private PrintStream originalOut;

    public SystemErrConsole(String title)
    {
        this.setIconImages(Resources.iconList);
        setTitle(title);
        setSize(new Dimension(542, 316));

        getContentPane().add(scrollPane, BorderLayout.CENTER);

        scrollPane.setViewportView(textArea);
        scrollPane.setColumnHeaderView(search);
        this.setLocationRelativeTo(null);
        s = new CustomOutputStream(textArea);
        PrintStream printStream = new PrintStream(s);
        originalOut = System.err;
        System.setErr(printStream);
    }

    CustomOutputStream s;

    public void finished()
    {
        if (originalOut != null)
            System.setErr(originalOut);
    }

    public void pretty()
    {
        s.update();
        String[] test = null;
        if (textArea.getText().split("\n").length >= 2)
            test = textArea.getText().split("\n");
        else
            test = textArea.getText().split("\r");

        String replace = "";
        for (String s : test)
        {
            if (s.startsWith("File '"))
            {
                String[] split = s.split("'");
                String start = split[0] + "'" + split[1] + "', ";
                s = s.substring(start.length(), s.length());
            }
            replace += s + JDA.nl;
        }
        setText(replace);
    }

    /**
     * Appends \r\n to the end of your string, then it puts it on the top.
     *
     * @param t the string you want to append
     */
    public void appendText(String t)
    {
        textArea.setText((textArea.getText().isEmpty() ? "" : textArea.getText() + "\r\n") + t);
        textArea.setCaretPosition(0);
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

    class CustomOutputStream extends OutputStream
    {
        private StringBuffer sb = new StringBuffer();
        private JTextArea textArea;

        public CustomOutputStream(JTextArea textArea)
        {
            this.textArea = textArea;
        }

        public void update()
        {
            textArea.append(sb.toString());
        }

        @Override
        public void write(int b) throws IOException
        {
            sb.append(String.valueOf((char) b));
        }
    }

    private static final long serialVersionUID = -6556940545421437508L;

}
