package the.bytecode.club.jda;

import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Any resources loaded by disc or by memory.
 *
 * @author Konloch
 */

public class Resources {
    public static ImageIcon nextIcon = new ImageIcon(Resources.class.getClass().getResource("/images/nextIcon.png"));
    public static ImageIcon prevIcon = new ImageIcon(Resources.class.getClass().getResource("/images/prevIcon.png"));
    public static ImageIcon busyIcon = new ImageIcon(Resources.class.getClass().getResource("/images/1.gif"));
    public static ImageIcon busyB64Icon = new ImageIcon(Resources.class.getClass().getResource("/images/busyIcon2.gif"));
    public static ImageIcon batIcon = new ImageIcon(Resources.class.getClass().getResource("/images/bat.png"));
    public static ImageIcon shIcon = new ImageIcon(Resources.class.getClass().getResource("/images/sh.png"));
    public static ImageIcon csharpIcon = new ImageIcon(Resources.class.getClass().getResource("/images/c#.png"));
    public static ImageIcon cplusplusIcon = new ImageIcon(Resources.class.getClass().getResource("/images/c++.png"));
    public static ImageIcon configIcon = new ImageIcon(Resources.class.getClass().getResource("/images/config.png"));
    public static ImageIcon jarIcon = new ImageIcon(Resources.class.getClass().getResource("/images/jar.png"));
    public static ImageIcon zipIcon = new ImageIcon(Resources.class.getClass().getResource("/images/zip.png"));
    public static ImageIcon packagesIcon = new ImageIcon(Resources.class.getClass().getResource("/images/package.png"));
    public static ImageIcon folderIcon = new ImageIcon(Resources.class.getClass().getResource("/images/folder.png"));
    public static ImageIcon fileIcon = new ImageIcon(Resources.class.getClass().getResource("/images/file.png"));
    public static ImageIcon textIcon = new ImageIcon(Resources.class.getClass().getResource("/images/text.png"));
    public static ImageIcon classIcon = new ImageIcon(Resources.class.getClass().getResource("/images/class.png"));
    public static ImageIcon imageIcon = new ImageIcon(Resources.class.getClass().getResource("/images/image.png"));
    public static ImageIcon decodedIcon = new ImageIcon(Resources.class.getClass().getResource("/images/decoded.png"));
    public static ImageIcon javaIcon = new ImageIcon(Resources.class.getClass().getResource("/images/java.png"));

    public static ImageIcon fileNavigatorIcon = new ImageIcon(Resources.class.getClass().getResource("/images/icon.png"));

    public static ArrayList<BufferedImage> iconList;
    public static BufferedImage icon;

    static {
        try {
            icon = ImageIO.read(Resources.class.getClass().getResourceAsStream("/images/icon.png"));
        } catch (IOException e) {
            System.err.println("Failed to load program icon:");
            e.printStackTrace();
        }

        iconList = new ArrayList<>();
        for (int size : new int[]{8, 16, 24, 32, 48, 64, 96, 128, 192, 256}) {
            iconList.add(resize(icon, size, size));
        }
    }

    public static BufferedImage resize(BufferedImage image, int width, int height) {
        return Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, width, height);
    }
}
