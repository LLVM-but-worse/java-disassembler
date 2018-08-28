package club.bytecode.the.jda.settings;

import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.decompilers.Decompilers;
import club.bytecode.the.jda.decompilers.JDADecompiler;
import club.bytecode.the.jda.gui.JDAWindow;
import club.bytecode.the.jda.gui.MainViewerGUI;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.google.common.collect.Lists;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import static club.bytecode.the.jda.settings.Setting.SettingType.BOOLEAN;
import static club.bytecode.the.jda.settings.Setting.SettingType.INT;

/**
 * Used to handle loading/saving the GUI (options).
 *
 * @author Konloch
 */
public class Settings {
    // todo: I should really refactor this
    public static final Setting PATH = new Setting("path", "");
    public static final Setting SHOW_CONTAINER_NAME = new Setting("showfilename", false, BOOLEAN);
    public static final Setting SNAP_TO_EDGES = new Setting("snaptoedges", false, BOOLEAN);
    public static final Setting DO_UPDATE_CHECK = new Setting("doupdatecheck", false, BOOLEAN);
    public static final Setting REFRESH_ON_VIEW_CHANGE = new Setting("refreshonviewchange", false, BOOLEAN);
    public static final Setting DONT_SHOW_EXCEPTIONS = new Setting("dontshowexceptions", false, BOOLEAN);

    public static final Setting FONT_SIZE = new Setting("font", "fontsize", 12, INT);
    public static final Setting FONT_FAMILY = new Setting("font", "fontfamily", Font.MONOSPACED);
    public static final Setting FONT_OPTIONS = new Setting("font", "fontoptions", Font.PLAIN, INT);

    static final List<Setting> ALL_SETTINGS = Lists.newArrayList(PATH, SHOW_CONTAINER_NAME, SNAP_TO_EDGES, DO_UPDATE_CHECK,
            REFRESH_ON_VIEW_CHANGE, DONT_SHOW_EXCEPTIONS, FONT_SIZE, FONT_FAMILY, FONT_OPTIONS);

    public static final Setting[] PANE_DECOMPILERS;

    static
    {
        PANE_DECOMPILERS = new Setting[MainViewerGUI.NUM_PANEL_GROUPS]; // bleh
        PANE_DECOMPILERS[0] = new Setting("panedecompiler0", Decompilers.FERNFLOWER.getFullName());
        PANE_DECOMPILERS[1] = new Setting("panedecompiler1", Decompilers.BYTECODE.getFullName());
        for (int i = 2; i < PANE_DECOMPILERS.length; i++)
            PANE_DECOMPILERS[i] = new Setting("panedecompiler" + i, "None");
        ALL_SETTINGS.addAll(Arrays.asList(PANE_DECOMPILERS));
    }

    public static void saveGUI() {
        try {
            JsonObject settings = new JsonObject();
            for (JDADecompiler decompiler : Decompilers.getAllDecompilers())
                decompiler.getSettings().saveTo(settings);

            for (Setting setting : Settings.ALL_SETTINGS) {
                getNode(settings, setting.node).add(setting.name, setting.getString());
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

            System.out.println("Saved all settings successfully");
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
            for (JDADecompiler decompiler : Decompilers.getAllDecompilers())
                decompiler.getSettings().loadFrom(settings);
            for (Setting setting : Settings.ALL_SETTINGS) {
                String nodeId = setting.node;
                JsonValue nodeValue = settings.get(nodeId);
                if (nodeValue != null) {
                    if ((nodeValue = nodeValue.asObject().get(setting.name)) != null)
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

    public static Font getCodeFont() {
        return new Font(Settings.FONT_FAMILY.getString(), Settings.FONT_OPTIONS.getInt(), Settings.FONT_SIZE.getInt());
    }
}
