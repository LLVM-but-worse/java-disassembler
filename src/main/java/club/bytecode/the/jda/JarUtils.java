package club.bytecode.the.jda;

import club.bytecode.the.jda.api.ExceptionUI;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Loading and saving jars
 *
 * @author Konloch
 * @author WaterWolf
 */

public class JarUtils {
    public static FileContainer load(final File jarFile) throws IOException {
        HashMap<String, byte[]> files = new HashMap<>();
        ZipInputStream jis = new ZipInputStream(new FileInputStream(jarFile));
        ZipEntry entry;
        while ((entry = jis.getNextEntry()) != null) {
            try {
                final String name = entry.getName();
                final byte[] bytes = getBytes(jis);
                if (!files.containsKey(name)) {
                    if (!name.endsWith(".class")) {
                        if (!entry.isDirectory())
                            files.put(name, bytes);
                    } else {
                        files.put(name, bytes);
                    }
                }
            } catch (Exception e) {
                new ExceptionUI(e, "loading jar");
            } finally {
                jis.closeEntry();
            }
        }
        jis.close();
        return new FileContainer(jarFile, files);
    }

    /**
     * Reads an InputStream and returns the read byte[]
     *
     * @param is the InputStream
     * @return the read byte[]
     * @throws IOException
     */
    public static byte[] getBytes(final InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int a = 0;
        while ((a = is.read(buffer)) != -1) {
            baos.write(buffer, 0, a);
        }
        baos.close();
        return baos.toByteArray();
    }

    public static void saveAsJar(Map<String, byte[]> nodeList, String path) {
        try {
            JarOutputStream out = new JarOutputStream(new FileOutputStream(path));
            ArrayList<String> noDupe = new ArrayList<>();
            for (Entry<String, byte[]> entry : nodeList.entrySet()) {
                String name = entry.getKey();
                if (!noDupe.contains(name)) {
                    noDupe.add(name);
                    out.putNextEntry(new ZipEntry(name));
                    out.write(entry.getValue());
                    out.closeEntry();
                }
            }

            for (FileContainer container : JDA.getOpenFiles())
                for (Entry<String, byte[]> entry : container.files.entrySet()) {
                    String filename = entry.getKey();
                    if (!filename.startsWith("META-INF")) {
                        if (!noDupe.contains(filename)) {
                            noDupe.add(filename);
                            out.putNextEntry(new ZipEntry(filename));
                            out.write(entry.getValue());
                            out.closeEntry();
                        }
                    }
                }

            noDupe.clear();
            out.close();
        } catch (IOException e) {
            new ExceptionUI(e, "saving as jar");
        }
    }
}
