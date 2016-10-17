package the.bytecode.club.jda;

import org.apache.commons.io.IOUtils;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;

/**
 * @author Konloch
 * @author Bibl (don't ban me pls)
 * @created 19 Jul 2015 04:12:21
 */
public class InitialBootScreen extends JFrame {
    private static final long serialVersionUID = -1098467609722393444L;

    private JProgressBar progressBar = new JProgressBar();

    public InitialBootScreen() throws IOException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setIconImages(Resources.iconList);

        int i = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        if (i >= 840)
            setSize(new Dimension(600, 800));
        else if (i >= 640)
            setSize(new Dimension(500, 600));
        else if (i >= 440)
            setSize(new Dimension(400, 400));
        else
            setSize(Toolkit.getDefaultToolkit().getScreenSize());

        setTitle("JDA - Loading");
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        getContentPane().setLayout(gridBagLayout);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.gridheight = 24;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 0;
        getContentPane().add(scrollPane, gbc_scrollPane);

        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditorKit(new HTMLEditorKit());
        editorPane.setEditable(false);
        editorPane.setText(IOUtils.toString(Resources.class.getResourceAsStream("/intro.html"), "UTF-8"));

        scrollPane.setViewportView(editorPane);

        GridBagConstraints gbc_progressBar = new GridBagConstraints();
        gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
        gbc_progressBar.gridx = 0;
        gbc_progressBar.gridy = 24;
        getContentPane().add(progressBar, gbc_progressBar);
        this.setLocationRelativeTo(null);
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }
}