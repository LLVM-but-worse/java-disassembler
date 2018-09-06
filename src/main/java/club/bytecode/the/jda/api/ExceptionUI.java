package club.bytecode.the.jda.api;

import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.Resources;
import club.bytecode.the.jda.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A simple class designed to show exceptions in the UI.
 *
 * @author Konloch
 */
public class ExceptionUI extends JFrame {

    private static final long serialVersionUID = -5230501978224926296L;

    /**
     * @param e The exception to be shown
     * @param context What JDA was doing when the exception occurred. Should be a gerund and lowercase
     */
    public ExceptionUI(Throwable e, String context) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        e.printStackTrace();
        setup(sw.toString(), context);
    }

    /**
     * @param e The exception to be shown
     * @param context What JDA was doing when the exception occurred. Should be a gerund and lowercase
     */
    public ExceptionUI(String e, String context) {
        setup(e, context);
    }

    private void setup(String e, String context) {
        System.err.println("Error while " + context + ":");
        System.err.println(e);

        if (Settings.DONT_SHOW_EXCEPTIONS.getBool())
            return;

        this.setIconImages(Resources.iconList);
        setSize(new Dimension(800, 400));
        setTitle("JDA v" + JDA.version + " - Stack Trace - Send this to " + JDA.ISSUE_TRACKER_URL);
        getContentPane().setLayout(new CardLayout(0, 0));

        JTextArea txtrBytecodeViewerIs = new JTextArea();
        txtrBytecodeViewerIs.setDisabledTextColor(Color.BLACK);
        txtrBytecodeViewerIs.setWrapStyleWord(true);
        getContentPane().add(new JScrollPane(txtrBytecodeViewerIs), "name_140466576080695");
        txtrBytecodeViewerIs.setFont(Settings.getCodeFont());
        txtrBytecodeViewerIs.setText("Error while " + context + ":\n" + e);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
