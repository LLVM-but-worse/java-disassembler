package the.bytecode.club.jda;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;
import the.bytecode.club.jda.decompilers.Decompiler;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to handle loading/saving the GUI (options).
 *
 * @author Konloch
 */
public class Settings
{
    static final Map<String, Setting> ALL_SETTINGS = new HashMap<>();

    public static final Setting JAVA_LOCATION = new Setting("javalocation", "");
    public static final Setting PATH = new Setting("path", "");
    public static final Setting SHOW_CONTAINER_NAME = new Setting("showfilename", "false");
    public static final Setting DO_UPDATE_CHECK = new Setting("doupdatecheck", "true");

    public static void saveGUI()
    {
        try
        {
            JsonObject settings = new JsonObject();
            Decompiler.CFR.getSettings().saveTo(settings);
            Decompiler.FERNFLOWER.getSettings().saveTo(settings);
            Decompiler.PROCYON.getSettings().saveTo(settings);
            Decompiler.BYTECODE.getSettings().saveTo(settings);
            if (settings.get("settings") == null)
            {
                settings.add("settings", new JsonObject());
            }
            JsonObject rootSettings = settings.get("settings").asObject();
            for (Map.Entry<String, Setting> setting : Settings.ALL_SETTINGS.entrySet())
            {
                if (setting.getValue().get() != null)
                {
                    rootSettings.add(setting.getKey(), setting.getValue().get());
                }
            }
            FileOutputStream out = new FileOutputStream(JDA.settingsFile);
            out.write(settings.toString().getBytes("UTF-8"));
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void loadGUI()
    {
        try
        {
            JsonObject settings = new JsonObject();
            try
            {
                settings = JsonObject.readFrom(new FileReader(JDA.settingsFile));
            }
            catch (ParseException | UnsupportedOperationException e)
            {
            }
            Decompiler.CFR.getSettings().loadFrom(settings);
            Decompiler.FERNFLOWER.getSettings().loadFrom(settings);
            Decompiler.PROCYON.getSettings().loadFrom(settings);
            Decompiler.BYTECODE.getSettings().loadFrom(settings);
            if (settings.get("settings") != null)
            {
                JsonObject rootSettings = settings.get("settings").asObject();
                for (Map.Entry<String, Setting> setting : Settings.ALL_SETTINGS.entrySet())
                {
                    if (rootSettings.get(setting.getKey()) != null)
                    {
                        setting.getValue().set(rootSettings.get(setting.getKey()).asString());
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}