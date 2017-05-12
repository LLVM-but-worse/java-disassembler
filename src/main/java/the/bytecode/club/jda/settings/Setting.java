package the.bytecode.club.jda.settings;

import static the.bytecode.club.jda.settings.Setting.SettingType.*;

public class Setting {
    public final String node; //TODO: convert to JSON node or something
    public final String key;
    public final SettingType type;
    private Object value;

    public Setting(String node, String key, Object value, SettingType type) {
        this.node = node;
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public Setting(String node, String key, Object value) {
        this(node, key, value, STRING);
    }

    public Setting(String key, Object value, SettingType type) {
        this("settings", key, value, type);
    }

    public Setting(String key, String value) {
        this(key, (Object)value, STRING);
    }

    public String get() {
        return value.toString();
    }

    public boolean getBool() {
        return (boolean) value;
    }

    public int getInt() {
        return (int) value;
    }

    public void set(Object value) {
        switch (type) {
            case BOOLEAN:
                this.value = value instanceof Boolean ? value : Boolean.parseBoolean(value.toString());
                break;
            case INT:
                this.value = value instanceof Integer ? value : Integer.parseInt(value.toString());
                break;
            case OPTIONS:
                throw new UnsupportedOperationException();
            case STRING:
            default:
                this.value = value.toString();
                break;
        }
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