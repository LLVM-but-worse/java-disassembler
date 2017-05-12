package the.bytecode.club.jda.settings;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import the.bytecode.club.jda.JDA;
import the.bytecode.club.jda.decompilers.Decompiler;
import the.bytecode.club.jda.decompilers.Decompilers;
import the.bytecode.club.jda.gui.JDAWindow;
import the.bytecode.club.jda.gui.MainViewerGUI;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static the.bytecode.club.jda.settings.Setting.SettingType.*;

/**
 * Used to handle loading/saving the GUI (options).
 *
 * @author Konloch
 */
public class Settings {
    static final List<Setting> ALL_SETTINGS = new ArrayList<>();

    // todo: I should really refactor this
    public static final Setting PATH = new Setting("path", "");
    public static final Setting SHOW_CONTAINER_NAME = new Setting("showfilename", false, BOOLEAN);
    public static final Setting SNAP_TO_EDGES = new Setting("snaptoedges", false, BOOLEAN);
    public static final Setting DO_UPDATE_CHECK = new Setting("doupdatecheck", true, BOOLEAN);
    public static final Setting REFRESH_ON_VIEW_CHANGE = new Setting("refreshonviewchange", false, BOOLEAN);

    public static final Setting FONT_SIZE = new Setting("font", "fontsize", 12, INT);
    public static final Setting FONT_FAMILY = new Setting("font", "fontfamily", Font.MONOSPACED);
    public static final Setting FONT_OPTIONS = new Setting("font", "fontoptions", Font.PLAIN, INT);

    static
    {
        ALL_SETTINGS.add(PATH);
        ALL_SETTINGS.add(SHOW_CONTAINER_NAME);
        ALL_SETTINGS.add(SNAP_TO_EDGES);
        ALL_SETTINGS.add(DO_UPDATE_CHECK);
        ALL_SETTINGS.add(REFRESH_ON_VIEW_CHANGE);

        ALL_SETTINGS.add(FONT_SIZE);
        ALL_SETTINGS.add(FONT_FAMILY);
        ALL_SETTINGS.add(FONT_OPTIONS);
    }

    public static void saveGUI() {
        try {
            JsonObject settings = new JsonObject();
            for (Decompiler decompiler : Decompilers.getAllDecompilers())
                decompiler.getSettings().saveTo(settings);

            for (Setting setting : Settings.ALL_SETTINGS) {
                getNode(settings, setting.node).add(setting.key, setting.get());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsonObject getNode(JsonObject parent, String nodeId) {
        final JsonValue nodeValue = parent.get(nodeId);
        if (nodeValue != null)
            return nodeValue.asObject();
        final JsonObject node = new JsonObject();
        parent.add(nodeId, node);
        return node;
    }

    public static void saveFrame(JsonObject windowsSection, IPersistentWindow f) {
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

    public static void loadGUI() {
        try {
            JsonObject settings = JsonObject.readFrom(new FileReader(JDA.settingsFile));
            for (Decompiler decompiler : Decompilers.getAllDecompilers())
                decompiler.getSettings().loadFrom(settings);
            for (Setting setting : Settings.ALL_SETTINGS) {
                String nodeId = setting.node;
                JsonValue nodeValue = settings.get(nodeId);
                if (nodeValue != null) {
                    if ((nodeValue = nodeValue.asObject().get(setting.key)) != null)
                        setting.set(nodeValue.asString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadWindows() {
        try {
            JsonObject rootSettings = JsonObject.readFrom(new FileReader(JDA.settingsFile));
            if (rootSettings.get("windows") != null) {
                JsonObject windowsSection = rootSettings.get("windows").asObject();
                for (JDAWindow f : MainViewerGUI.windows)
                    loadFrame(windowsSection, f);
                loadFrame(windowsSection, JDA.viewer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadFrame(JsonObject windowsSection, IPersistentWindow f) {
        if (windowsSection.get(f.getWindowId()) != null) {
            JsonObject settings = windowsSection.get(f.getWindowId()).asObject();
            Point pos = new Point(settings.get("x").asInt(), settings.get("y").asInt());
            Dimension size = new Dimension(settings.get("width").asInt(), settings.get("height").asInt());
            f.restoreState(settings.get("state").asInt());
            f.restorePosition(pos);
            f.restoreSize(size);
        }
    }
}