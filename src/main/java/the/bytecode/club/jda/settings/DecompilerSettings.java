package the.bytecode.club.jda.settings;

import com.eclipsesource.json.JsonObject;
import the.bytecode.club.jda.decompilers.Decompiler;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecompilerSettings {
    private Decompiler decompiler;

    public DecompilerSettings(Decompiler decompiler) {
        this.decompiler = decompiler;
    }

    private Map<SettingsEntry, JCheckBoxMenuItem> menuItems = new HashMap<>();
    private List<SettingsEntry> settingsEntries = new ArrayList<>();

    public void registerSetting(SettingsEntry entry) {
        if (!menuItems.containsKey(entry)) {
            settingsEntries.add(entry);
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(entry.getText());
            if (entry.isDefaultOn()) {
                item.setSelected(true);
            }
            menuItems.put(entry, item);
        }
    }

    public boolean isSelected(SettingsEntry entry) {
        return menuItems.get(entry).isSelected();
    }

    public JCheckBoxMenuItem getMenuItem(SettingsEntry entry) {
        return menuItems.get(entry);
    }

    public int size() {
        return settingsEntries.size();
    }

    public List<SettingsEntry> getEntries() {
        return settingsEntries;
    }

    public void loadFrom(JsonObject rootSettings) {
        if (rootSettings.get("decompilers") != null) {
            JsonObject decompilerSection = rootSettings.get("decompilers").asObject();
            if (decompilerSection.get(decompiler.getName()) != null) {
                JsonObject thisDecompiler = decompilerSection.get(decompiler.getName()).asObject();
                for (Map.Entry<SettingsEntry, JCheckBoxMenuItem> entry : menuItems.entrySet()) {
                    if (thisDecompiler.get(entry.getKey().getParam()) != null) {
                        entry.getValue().setSelected(thisDecompiler.get(entry.getKey().getParam()).asBoolean());
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
        for (Map.Entry<SettingsEntry, JCheckBoxMenuItem> entry : menuItems.entrySet()) {
            thisDecompiler.add(entry.getKey().getParam(), entry.getValue().isSelected());
        }
    }

    public interface SettingsEntry {
        String getText();

        String getParam();

        boolean isDefaultOn();
    }
}
