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

public class Resources
{
    public static ImageIcon nextIcon = new ImageIcon(Resources.class.getClass().getResource("/nextIcon.png"));
    public static ImageIcon prevIcon = new ImageIcon(Resources.class.getClass().getResource("/prevIcon.png"));
    public static ImageIcon busyIcon = new ImageIcon(Resources.class.getClass().getResource("/1.gif"));
    public static ImageIcon busyB64Icon = new ImageIcon(Resources.class.getClass().getResource("/busyIcon2.gif"));
    public static ImageIcon batIcon = new ImageIcon(Resources.class.getClass().getResource("/bat.png"));
    public static ImageIcon shIcon = new ImageIcon(Resources.class.getClass().getResource("/sh.png"));
    public static ImageIcon csharpIcon = new ImageIcon(Resources.class.getClass().getResource("/c#.png"));
    public static ImageIcon cplusplusIcon = new ImageIcon(Resources.class.getClass().getResource("/c++.png"));
    public static ImageIcon configIcon = new ImageIcon(Resources.class.getClass().getResource("/config.png"));
    public static ImageIcon jarIcon = new ImageIcon(Resources.class.getClass().getResource("/jar.png"));
    public static ImageIcon zipIcon = new ImageIcon(Resources.class.getClass().getResource("/zip.png"));
    public static ImageIcon packagesIcon = new ImageIcon(Resources.class.getClass().getResource("/package.png"));
    public static ImageIcon folderIcon = new ImageIcon(Resources.class.getClass().getResource("/folder.png"));
    public static ImageIcon fileIcon = new ImageIcon(Resources.class.getClass().getResource("/file.png"));
    public static ImageIcon textIcon = new ImageIcon(Resources.class.getClass().getResource("/text.png"));
    public static ImageIcon classIcon = new ImageIcon(Resources.class.getClass().getResource("/class.png"));
    public static ImageIcon imageIcon = new ImageIcon(Resources.class.getClass().getResource("/image.png"));
    public static ImageIcon decodedIcon = new ImageIcon(Resources.class.getClass().getResource("/decoded.png"));
    public static ImageIcon javaIcon = new ImageIcon(Resources.class.getClass().getResource("/java.png"));

    public static ArrayList<BufferedImage> iconList;
    public static BufferedImage icon;

    static
    {
        try
        {
            icon = ImageIO.read(Resources.class.getClass().getResourceAsStream("/icon.png"));
        }
        catch (IOException e)
        {
            System.err.println("Failed to load program icon:");
            e.printStackTrace();
        }

        iconList = new ArrayList<>();
        int size = 16;
        for (int i = 0; i < 24; i++)
        {
            iconList.add(resize(icon, size, size));
            size += 2;
        }
    }

    public static BufferedImage resize(BufferedImage image, int width, int height)
    {
        return Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, width, height);
    }
}
