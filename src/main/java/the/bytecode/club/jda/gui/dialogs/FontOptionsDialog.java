package the.bytecode.club.jda.gui.dialogs;

import net.miginfocom.swing.MigLayout;
import the.bytecode.club.jda.settings.Settings;

import javax.swing.*;
import java.awt.*;

public class FontOptionsDialog {
    private final JPanel dialog;
    private final JSpinner sizeSpinner;
    private final JComboBox<String> fontBox;
    private final JCheckBox boldBox, italicsBox;

    public FontOptionsDialog() {
        dialog = new JPanel(new MigLayout());

        dialog.add(new JLabel("Font Family:"));
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] array = ge.getAvailableFontFamilyNames();
        dialog.add(fontBox = new JComboBox<>(array), "span, wrap");
        fontBox.setEditable(true);
        fontBox.setSelectedItem(Settings.FONT_FAMILY.get());

        dialog.add(new JLabel("Font Size:"));
        dialog.add(sizeSpinner = new JSpinner());
        sizeSpinner.setPreferredSize(new Dimension(42, 20));
        sizeSpinner.setModel(new SpinnerNumberModel(Settings.FONT_SIZE.getInt(), 1, null, 1));
        dialog.add(new JLabel("pt"), "wrap");

        dialog.add(new JLabel("Font Options:"));
        dialog.add(boldBox = new JCheckBox("Bold"));
        boldBox.setSelected((Settings.FONT_OPTIONS.getInt() & Font.BOLD) != 0);
        dialog.add(italicsBox = new JCheckBox("Italic"), "wrap");
        italicsBox.setSelected((Settings.FONT_OPTIONS.getInt() & Font.ITALIC) != 0);
    }

    public void display() {
        if (JOptionPane.showConfirmDialog(null, dialog, "Font Options", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Settings.FONT_FAMILY.set(fontBox.getSelectedItem());
            Settings.FONT_SIZE.set(sizeSpinner.getValue());

            int fontOptions = Font.PLAIN;
            if (boldBox.isSelected())
                fontOptions |= Font.BOLD;
            if (italicsBox.isSelected())
                fontOptions |= Font.ITALIC;
            Settings.FONT_OPTIONS.set(String.valueOf(fontOptions));
        }
    }
}
