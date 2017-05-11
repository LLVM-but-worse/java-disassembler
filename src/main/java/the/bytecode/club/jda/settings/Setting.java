package the.bytecode.club.jda.settings;

public class Setting {
    public final String node; //TODO: convert to JSON node or something
    public final String key;
    public final SettingType type;
    private Object value;

    public Setting(String key, String value) {
        this("settings", key, value, SettingType.STRING);
    }

    public Setting(String node, String key, Object value) {
        this(node, key, value, SettingType.STRING);
    }

    public Setting(String node, String key, Object value, SettingType type) {
        this.node = node;
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public String get() {
        return value.toString();
    }

    public boolean getBool() {
        return Boolean.parseBoolean(get());
    }

    public int getInt() {
        return Integer.parseInt(get());
    }

    public void set(Object value) {
        this.value = value.toString();
    }

    public boolean isEmpty() {
        return this.value == null || this.value.toString().isEmpty();
    }

    SettingType getType() {
        return type;
    }

    public enum SettingType {
        BOOLEAN, STRING, INT, OPTIONS
    }
}