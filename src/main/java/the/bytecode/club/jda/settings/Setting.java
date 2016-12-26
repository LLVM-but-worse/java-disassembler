package the.bytecode.club.jda.settings;

/**
 * Used to handle loading/saving the GUI (options).
 *
 * @author Konloch
 */
public class Setting {
    public final String node;
    public final String key;
    private String value;

    public Setting(String key, String value) {
        this("settings", key, value);
    }

    public Setting(String node, String key, String value) {
        this.node = node;
        this.key = key;
        this.value = value;
        Settings.ALL_SETTINGS.add(this);
    }

    public String get() {
        return value;
    }

    public boolean getBool() {
        return Boolean.parseBoolean(value);
    }

    public int getInt() {
        return Integer.parseInt(value);
    }

    public void set(Object value) {
        this.value = value.toString();
    }

    public boolean isEmpty() {
        return this.value == null || this.value.isEmpty();
    }
}