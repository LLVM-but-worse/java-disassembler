package club.bytecode.the.jda.util;

import java.awt.*;

public class GuiUtils {
    public static void setWmClassName(String className) throws NoSuchFieldException, IllegalAccessException {
        Toolkit xToolkit = Toolkit.getDefaultToolkit();
        java.lang.reflect.Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
        awtAppClassNameField.setAccessible(true);
        awtAppClassNameField.set(xToolkit, className);
    }
}
