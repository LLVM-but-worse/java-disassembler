package the.bytecode.club.jda.gui;

import the.bytecode.club.jda.JDA;
import the.bytecode.club.jda.Resources;
import the.bytecode.club.jda.Settings;

import javax.swing.*;
import java.awt.*;

/**
 * The about frame.
 *
 * @author Konloch
 */
public class AboutWindow extends JFrame
{
    private static final long serialVersionUID = -8230501978224923296L;
    private JTextArea textArea = new JTextArea();

    public AboutWindow()
    {
        this.setIconImages(Resources.iconList);
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setType(Type.UTILITY);
        setTitle("Java DisAssembler - About - https://the.bytecode.club");
        getContentPane().setLayout(new CardLayout(0, 0));
        JScrollPane scrollPane = new JScrollPane();
        getContentPane().add(scrollPane, "name_322439757638784");
        textArea.setWrapStyleWord(true);
        textArea.setEnabled(false);
        textArea.setDisabledTextColor(Color.BLACK);
        scrollPane.setViewportView(textArea);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
    }

    @Override
    public void setVisible(boolean b)
    {
        super.setVisible(b);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, (int) JDA.viewer.fontSpinner.getValue()));
        textArea.setText("Java DisAssembler " + JDA.version + " is a fork of Bytecode Viewer." + JDA.nl +
                JDA.nl +
                "Settings:" + JDA.nl +
                "	Preview Copy: " + JDA.previewCopy + JDA.nl +
                "	Java: " + Settings.JAVA_LOCATION.get() + JDA.nl +
                "	JDA Dir: " + JDA.getJDADirectory() + JDA.nl +
                "	Optional Lib: " + Settings.PATH.get() + JDA.nl +
                "Command Line Input:" + JDA.nl +
                "	-help                         Displays the help menu" + JDA.nl +
                "	-list                         Displays the available decompilers" + JDA.nl +
                "	-decompiler <decompiler>      Selects the decompiler, procyon by default" + JDA.nl +
                "	-i <input file>               Selects the input file (Jar, Class, ZIP, all work automatically)" + JDA.nl +
                "	-o <output file>              Selects the output file (Java or Java-Bytecode)" + JDA.nl +
                "	-t <target classname>         Must either be the fully qualified classname or \"all\" to decompile all as zip" + JDA.nl +
                "	-nowait                       Doesn't wait for the user to read the CLI messages" + JDA.nl + JDA.nl +
                "Keybinds:" + JDA.nl +
                "	CTRL + O: Open/add new jar/class/apk" + JDA.nl +
                "	CTLR + N: Reset the workspace" + JDA.nl +
                "	CTRL + W: Closes the currently opened tab" + JDA.nl +
                "	CTRL + S: Save classes as zip" + JDA.nl +
                "	CTRL + R: Run (EZ-Inject) - dynamically load the classes and invoke a main class" + JDA.nl +
                JDA.nl +
                "Code from various projects has been used, including but not limited to:" + JDA.nl +
                "	J-RET by WaterWolf" + JDA.nl +
                "	RSyntaxPane by Robert Futrell" + JDA.nl +
                "	Commons IO by Apache" + JDA.nl +
                "	ASM by OW2" + JDA.nl +
                "	FernFlower by Stiver" + JDA.nl +
                "	Procyon by Mstrobel" + JDA.nl +
                "	CFR by Lee Benfield" + JDA.nl +
                "	CFIDE by Bibl" + JDA.nl +
                JDA.nl +
                "If you're interested in Java Reverse Engineering, join The Bytecode Club - https://the.bytecode.club");
    }
}
