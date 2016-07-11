package the.bytecode.club.jda.settings;

import com.eclipsesource.json.JsonObject;
import the.bytecode.club.jda.JDA;
import the.bytecode.club.jda.decompilers.Decompiler;
import the.bytecode.club.jda.gui.JDAWindow;
import the.bytecode.club.jda.gui.MainViewerGUI;

import java.awt.*;
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

    public static final Setting PATH = new Setting("path", "");
    // todo: I should really refactor this
    public static final Setting SHOW_CONTAINER_NAME = new Setting("showfilename", "false");
    public static final Setting SNAP_TO_EDGES = new Setting("snaptoedges", "false");
    public static final Setting DO_UPDATE_CHECK = new Setting("doupdatecheck", "true");
    public static final Setting REFRESH_ON_VIEW_CHANGE = new Setting("refreshonviewchange", "false");

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

            if (settings.get("windows") == null)
                settings.add("windows", new JsonObject());
            JsonObject windowsSection = settings.get("windows").asObject();
            for (JDAWindow f : MainViewerGUI.windows)
                saveFrame(windowsSection, f);
            saveFrame(windowsSection, JDA.viewer);

            FileOutputStream out = new FileOutputStream(JDA.settingsFile);
            out.write(settings.toString().getBytes("UTF-8"));
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void saveFrame(JsonObject windowsSection, IPersistentWindow f)
    {
        String name = f.getWindowId();
        if (windowsSection.get(name) == null)
            windowsSection.add(name, new JsonObject());

        JsonObject windowSettings = windowsSection.get(name).asObject();
        Point pos = f.getPersistentPosition();
        Dimension size = f.getPersistentSize();
        windowSettings.add("x", pos.x);
        windowSettings.add("y", pos.y);
        windowSettings.add("width", size.width);
        windowSettings.add("height", size.height);
        windowSettings.add("state", f.getState());
    }

    public static void loadGUI()
    {
        try
        {
            JsonObject settings = JsonObject.readFrom(new FileReader(JDA.settingsFile));
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

    public static void loadWindows()
    {
        try
        {
            JsonObject rootSettings = JsonObject.readFrom(new FileReader(JDA.settingsFile));
            if (rootSettings.get("windows") != null)
            {
                JsonObject windowsSection = rootSettings.get("windows").asObject();
                for (JDAWindow f : MainViewerGUI.windows)
                    loadFrame(windowsSection, f);
                loadFrame(windowsSection, JDA.viewer);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void loadFrame(JsonObject windowsSection, IPersistentWindow f)
    {
        if (windowsSection.get(f.getWindowId()) != null)
        {
            JsonObject settings = windowsSection.get(f.getWindowId()).asObject();
            Point pos = new Point(settings.get("x").asInt(), settings.get("y").asInt());
            Dimension size = new Dimension(settings.get("width").asInt(), settings.get("height").asInt());
            f.restoreState(settings.get("state").asInt());
            f.restorePosition(pos);
            f.restoreSize(size);
        }
    }
}