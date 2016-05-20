package the.bytecode.club.bytecodeviewer;

/**
 * Used to handle loading/saving the GUI (options).
 *
 * @author Konloch
 */
public class Setting
{
    private String key;
    private String value;

    public Setting(String key, String value)
    {
        this.key = key;
        this.value = value;
        Settings.ALL_SETTINGS.put(this.key, this);
    }

    public String get()
    {
        return this.value;
    }

    public boolean getBool()
    {
        return Boolean.parseBoolean(this.value);
    }

    public void set(Object value)
    {
        this.value = value.toString();
    }

    public boolean isEmpty()
    {
        return this.value == null || this.value.isEmpty();
    }
}