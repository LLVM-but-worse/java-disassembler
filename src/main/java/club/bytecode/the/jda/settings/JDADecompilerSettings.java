package club.bytecode.the.jda.settings;

import club.bytecode.the.jda.decompilers.JDADecompiler;
import club.bytecode.the.jda.decompilers.filter.DecompileFilter;
import club.bytecode.the.jda.decompilers.filter.DecompileFilters;
import club.bytecode.the.jda.gui.components.CheckboxList;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * This is a really nasty hack, and must be rewritten to split up the actual settings code, and GUI code
 */
public class JDADecompilerSettings {
    private final JDADecompiler decompiler;

    private final JScrollPane dialogPane;
    private final JPanel dialog;

    /**
     * Stores all of the individual settings. Should not be modified after initialization.
     */
    private final Map<String, SettingsEntry> entries = new LinkedHashMap<>();

    private Map<SettingsEntry, JCheckBox> booleanSettings = new HashMap<>();
    private Map<SettingsEntry, JTextArea> stringSettings = new HashMap<>();
    private Map<SettingsEntry, JSpinner> intSettings = new HashMap<>();
    
    private static final String PIPELINE_KEY = "_pipeline";
    private Set<DecompileFilter> enabledFilters = new HashSet<>();
    private Map<DecompileFilter, JCheckBox> filterCheckboxes = new HashMap<>();
    private JPanel pipelinePanel;
    private JScrollPane pipelineListbox;
    private CheckboxList availFilters;

    public JDADecompilerSettings(JDADecompiler decompiler) {
        this.decompiler = decompiler;
        dialog = new JPanel();
        dialog.setLayout(new MigLayout("gap rel 0", "grow"));
        dialogPane = new JScrollPane(dialog);
        dialogPane.setBorder(BorderFactory.createEmptyBorder());
        dialogPane.setPreferredSize(new Dimension(400, 375));
    }

    public void displayDialog() {
        Dimension oldSize = dialogPane.getPreferredSize();
        if (oldSize.height > dialog.getPreferredSize().height)
            dialogPane.setPreferredSize(new Dimension(oldSize.width, dialog.getPreferredSize().height));
        if (oldSize.width > dialog.getPreferredSize().width)
            dialogPane.setPreferredSize(new Dimension(dialog.getPreferredSize().width + 50, dialogPane.getPreferredSize().height));
        if (JOptionPane.showConfirmDialog(null, dialogPane, decompiler.getName() + " Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            for (Map.Entry<SettingsEntry, JCheckBox> entry : booleanSettings.entrySet()) {
                entry.getKey().set(entry.getValue().isSelected());
            }

            for (Map.Entry<SettingsEntry, JTextArea> entry : stringSettings.entrySet()) {
                entry.getKey().set(entry.getValue().getText());
            }

            for (Map.Entry<SettingsEntry, JSpinner> entry : intSettings.entrySet()) {
                entry.getKey().set(entry.getValue().getValue());
            }
        }
        dialogPane.setPreferredSize(oldSize);
    }

    public SettingsEntry getEntry(String key) {
        return entries.get(key);
    }

    /**
     * @return a copy of the settings entries.
     */
    public Set<SettingsEntry> getEntries() {
        return new HashSet<>(entries.values());
    }

    public void registerSetting(SettingsEntry entry) {
        entries.put(entry.key, entry);

        JComponent item;
        switch(entry.getType()) {
            case BOOLEAN:
                JCheckBox checkbox = new JCheckBox();
                checkbox.setSelected(entry.getBool());
                booleanSettings.put(entry, checkbox);
                item = checkbox;
                break;
            case STRING:
                JTextArea textArea = new JTextArea(entry.getString());
                textArea.setMaximumSize(new Dimension(42, textArea.getMaximumSize().height));
                stringSettings.put(entry, textArea);
                item = textArea;
                break;
            case INT:
                JSpinner spinner = new JSpinner();
                spinner.setPreferredSize(new Dimension(42, 20));
                spinner.setModel(new SpinnerNumberModel(entry.getInt(), 0, null, 1));
                intSettings.put(entry, spinner);
                item = spinner;
                break;
            default:
                throw new IllegalArgumentException();
        }

        dialog.add(item, "align right");
        dialog.add(new JLabel(entry.name), "wrap");
    }

    /**
     * This must be called OUTSIDE the constructor, because otherwise it will get called BEFORE any plugins load.
     */
    private void initPipelineGui() {
        pipelinePanel = new JPanel(new MigLayout("gap rel 0", "grow"));
        pipelinePanel.add(new JLabel("Preprocessing Pipeline"), "wrap");
        availFilters = new CheckboxList();
        for (DecompileFilter filter : DecompileFilters.getAllFilters()) {
            JCheckBox checkbox = new JCheckBox(filter.getFullName());
            checkbox.addItemListener((e) -> {
                if (checkbox.isSelected())
                    enabledFilters.add(filter);
                else
                    enabledFilters.remove(filter);
            });
            filterCheckboxes.put(filter, checkbox);
            availFilters.addCheckbox(checkbox);
        }
        pipelineListbox = new JScrollPane(availFilters);
        pipelinePanel.add(pipelineListbox, "spanx, grow");
        dialog.add(pipelinePanel, "align center, spanx, grow, wrap");
    }
    
    public Collection<DecompileFilter> getEnabledFilters() {
        return Collections.unmodifiableCollection(enabledFilters);
    }

    public void loadFrom(JsonObject rootSettings) {
        initPipelineGui();
        
        if (rootSettings.get("decompilers") != null) {
            JsonObject decompilerSection = rootSettings.get("decompilers").asObject();
            if (decompilerSection.get(decompiler.getName()) != null) {
                JsonObject thisDecompiler = decompilerSection.get(decompiler.getName()).asObject();

                for (Map.Entry<SettingsEntry, JCheckBox> entry : booleanSettings.entrySet()) {
                    if (thisDecompiler.get(entry.getKey().key) != null) {
                        entry.getValue().setSelected(thisDecompiler.get(entry.getKey().key).asBoolean());
                    }
                }

                for (Map.Entry<SettingsEntry, JTextArea> entry : stringSettings.entrySet()) {
                    if (thisDecompiler.get(entry.getKey().key) != null) {
                        entry.getValue().setText(thisDecompiler.get(entry.getKey().key).asString());
                    }
                }

                for (Map.Entry<SettingsEntry, JSpinner> entry : intSettings.entrySet()) {
                    if (thisDecompiler.get(entry.getKey().key) != null) {
                        entry.getValue().setValue(thisDecompiler.get(entry.getKey().key).asInt());
                    }
                }
                
                if (thisDecompiler.get(PIPELINE_KEY) != null) {
                    JsonArray array = thisDecompiler.get(PIPELINE_KEY).asArray();
                    for (JsonValue value : array) {
                        if (!value.isString())
                            continue;
                        DecompileFilter filter = DecompileFilters.getByName(value.asString());
                        if (filter != null) {
                            enabledFilters.add(filter);
                            filterCheckboxes.get(filter).setSelected(true);
                        }
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
            thisDecompiler.add(entry.getKey().key, entry.getValue().isSelected());
        }
        for (Map.Entry<SettingsEntry, JTextArea> entry : stringSettings.entrySet()) {
            thisDecompiler.add(entry.getKey().key, entry.getValue().getText());
        }
        for (Map.Entry<SettingsEntry, JSpinner> entry : intSettings.entrySet()) {
            thisDecompiler.add(entry.getKey().key, (Integer)entry.getValue().getValue());
        }
        
        JsonArray pipelineArray = new JsonArray();
        for (DecompileFilter filter : enabledFilters) {
            pipelineArray.add(filter.getFullName());
        }
        thisDecompiler.add(PIPELINE_KEY, pipelineArray);
    }

    // TODO: Refactor to have a default entry class for each type of entry, etc.
    public static class SettingsEntry extends Setting {
        public final String key;

        public SettingsEntry(String key, String name, Object value, SettingType type) {
            super(null, name, value, type);
            this.key = key;
        }

        public SettingsEntry(String key, String name, Object value) {
            this(key, name, value, SettingType.BOOLEAN);
        }
    }
}
