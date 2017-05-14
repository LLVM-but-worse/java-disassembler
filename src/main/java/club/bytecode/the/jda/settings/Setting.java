package club.bytecode.the.jda.settings;

import static club.bytecode.the.jda.settings.Setting.SettingType.*;

public class Setting {
    public final String node; //TODO: convert to JSON node or something
    public final String name;
    public final SettingType type;
    private Object value;

    public Setting(String node, String name, Object value, SettingType type) {
        this.node = node;
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public Setting(String node, String name, Object value) {
        this(node, name, value, STRING);
    }

    public Setting(String name, Object value, SettingType type) {
        this("settings", name, value, type);
    }

    public Setting(String name, String value) {
        this(name, (Object)value, STRING);
    }

    public String getString() {
        return value.toString();
    }

    public Object get() {
        return value;
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