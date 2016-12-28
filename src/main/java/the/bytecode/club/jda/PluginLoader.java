package the.bytecode.club.jda;

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class PluginLoader {
    public static void tryLoadPlugin(File pluginFile) throws MalformedURLException {
        String pluginFileName = pluginFile.getName();
        try {
            ClassLoader loader = new URLClassLoader(new URL[] { pluginFile.toURI().toURL() }) {
                public URL getResource(String name) {
                    if (name.startsWith("\0JDA-hack:"))
                        return findResource(name.substring(name.indexOf(':') + 1));
                    else
                        return super.getResource(name);
                }
            };

            InputStream metaInfStream = loader.getResourceAsStream("\0JDA-hack:META-INF/MANIFEST.MF");
            if (metaInfStream == null) {
                System.out.println("Invalid plugin " + pluginFileName + ": no manifest");
                return;
            }

            String mainClass = parseManifest(metaInfStream);
            if (mainClass == null) {
                System.out.println("Invalid plugin " + pluginFileName + ": unable to parse manifest");
                return;
            }

            Class<?> clazz = Class.forName(mainClass, true, loader);
            Method mainMethod = clazz.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) new String[0]);
        } catch (ReflectiveOperationException e) {
            System.err.println("Failed to load plugin " + pluginFileName);
            e.printStackTrace();
        }
    }

    // Read Main-Class attribute from manifest. Return null if invalid
    public static String parseManifest(InputStream manifestStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(manifestStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length < 2)
                    return null;

                String header = parts[0];
                if (header.equals("Main-Class")) {
                    return parts[1].trim();
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
