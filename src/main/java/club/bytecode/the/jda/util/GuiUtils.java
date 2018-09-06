package club.bytecode.the.jda.util;

import org.imgscalr.Scalr;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GuiUtils {
    public static void setWmClassName(String className) {
        Toolkit xToolkit = Toolkit.getDefaultToolkit();
        java.lang.reflect.Field awtAppClassNameField = null;
        try {
            awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
            awtAppClassNameField.setAccessible(true);
            awtAppClassNameField.set(xToolkit, className);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage resize(BufferedImage image, int width, int height) {
        return Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, width, height);
    }

    public static ImageIcon resize(Icon icon, int width, int height){
        BufferedImage srcImg = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = srcImg.createGraphics();
        icon.paintIcon(null, g, 0,0);
        g.dispose();
        BufferedImage resized = resize(srcImg, width, height);
        return new ImageIcon(resized);
    }

    public static void setLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
