package the.bytecode.club.jda.settings;

import com.eclipsesource.json.JsonObject;
import net.miginfocom.swing.MigLayout;
import the.bytecode.club.jda.decompilers.Decompiler;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DecompilerSettings {
    private final Decompiler decompiler;
    private final JPanel dialog;

    private Map<SettingsEntry, JCheckBox> booleanSettings = new HashMap<>();
    private Map<SettingsEntry, JTextArea> stringSettings = new HashMap<>();
    private Map<SettingsEntry, JSpinner> intSettings = new HashMap<>();

    public DecompilerSettings(Decompiler decompiler) {
        this.decompiler = decompiler;
        this.dialog = new JPanel(new MigLayout("gap rel 0", "grow"));
    }

    public void displayDialog() {
        if (JOptionPane.showConfirmDialog(null, dialog, decompiler.getName() + " Settings", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            // todo: cancel handling
        }
    }

    public void registerSetting(SettingsEntry entry) {
        JComponent item;
        switch(entry.getType()) {
            case BOOLEAN:
                JCheckBox checkbox = new JCheckBox();
                checkbox.setSelected(Boolean.parseBoolean(entry.getDefaultValue()));
                booleanSettings.put(entry, checkbox);
                item = checkbox;
                break;
            case STRING:
                JTextArea textArea = new JTextArea(entry.getDefaultValue());
                textArea.setMaximumSize(new Dimension(42, textArea.getMaximumSize().height));
                stringSettings.put(entry, textArea);
                item = textArea;
                break;
            case INT:
                JSpinner spinner = new JSpinner();
                spinner.setPreferredSize(new Dimension(42, 20));
                spinner.setModel(new SpinnerNumberModel(Integer.parseInt(entry.getDefaultValue()), 0, null, 1));
                intSettings.put(entry, spinner);
                item = spinner;
                break;
            default:
                throw new IllegalArgumentException();
        }

        dialog.add(item, "align right");
        dialog.add(new JLabel(entry.getText()), "wrap");
    }

    public boolean getBoolean(SettingsEntry entry) {
        if (entry.getType() != SettingsEntry.SettingType.BOOLEAN)
            throw new IllegalArgumentException("Setting is not a boolean");
        return booleanSettings.get(entry).isSelected();
    }

    public String getString(SettingsEntry entry) {
        if (entry.getType() != SettingsEntry.SettingType.STRING)
            throw new IllegalArgumentException("Setting is not a string");
        return stringSettings.get(entry).getText();
    }

    public int getInt(SettingsEntry entry) {
        if (entry.getType() != SettingsEntry.SettingType.INT)
            throw new IllegalArgumentException("Setting is not a int");
        return (int) intSettings.get(entry).getValue();
    }

    public Object getValue(SettingsEntry setting) {
        switch(setting.getType()) {
            case BOOLEAN:
                return getBoolean(setting);
            case STRING:
                return getString(setting);
            case INT:
                return getInt(setting);
            default:
                throw new IllegalArgumentException();
        }
    }

    public int size() {
        return booleanSettings.size() + stringSettings.size() + intSettings.size();
    }

    public void loadFrom(JsonObject rootSettings) {
        if (rootSettings.get("decompilers") != null) {
            JsonObject decompilerSection = rootSettings.get("decompilers").asObject();
            if (decompilerSection.get(decompiler.getName()) != null) {
                JsonObject thisDecompiler = decompilerSection.get(decompiler.getName()).asObject();

                for (Map.Entry<SettingsEntry, JCheckBox> entry : booleanSettings.entrySet()) {
                    if (thisDecompiler.get(entry.getKey().getParam()) != null) {
                        entry.getValue().setSelected(thisDecompiler.get(entry.getKey().getParam()).asBoolean());
                    }
                }

                for (Map.Entry<SettingsEntry, JTextArea> entry : stringSettings.entrySet()) {
                    if (thisDecompiler.get(entry.getKey().getParam()) != null) {
                        entry.getValue().setText(thisDecompiler.get(entry.getKey().getParam()).asString());
                    }
                }

                for (Map.Entry<SettingsEntry, JSpinner> entry : intSettings.entrySet()) {
                    if (thisDecompiler.get(entry.getKey().getParam()) != null) {
                        entry.getValue().setValue(thisDecompiler.get(entry.getKey().getParam()).asInt());
                    }
                }
            }
        }
    }

    public void saveTo(JsonObject rootSettings) {
        if (rootSettings.get("decompilers") == null) {
            rootSettings.add("decompilers", new JsonObject());
        }
        JsonObject decompilerSection = rootSettings.get("decompilers").asObject();
        if (decompilerSection.get(decompiler.getName()) == null) {
            decompilerSection.add(decompiler.getName(), new JsonObject());
        }
        JsonObject thisDecompiler = decompilerSection.get(decompiler.getName()).asObject();
        for (Map.Entry<SettingsEntry, JCheckBox> entry : booleanSettings.entrySet()) {
            thisDecompiler.add(entry.getKey().getParam(), entry.getValue().isSelected());
        }
        for (Map.Entry<SettingsEntry, JTextArea> entry : stringSettings.entrySet()) {
            thisDecompiler.add(entry.getKey().getParam(), entry.getValue().getText());
        }
        for (Map.Entry<SettingsEntry, JSpinner> entry : intSettings.entrySet()) {
            thisDecompiler.add(entry.getKey().getParam(), (Integer)entry.getValue().getValue());
        }
    }

    // TODO: Refactor to have a default entry class for each type of entry, etc.
    public interface SettingsEntry {
        String getText();

        String getParam();

        String getDefaultValue();

        SettingType getType();

        enum SettingType {
            BOOLEAN, STRING, INT, OPTIONS
        }
    }
}
