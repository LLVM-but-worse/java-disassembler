package the.bytecode.club.jda.decompilers;

import org.apache.commons.io.FileUtils;
import org.jetbrains.java.decompiler.main.decompiler.BaseDecompiler;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import the.bytecode.club.jda.JDA;
import the.bytecode.club.jda.JarUtils;
import the.bytecode.club.jda.settings.JDADecompilerSettings.SettingsEntry;
import the.bytecode.club.jda.settings.Setting;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.Manifest;

import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.*;

/**
 * A FernFlower wrapper with all the options (except 2)
 *
 * @author Konloch
 * @author WaterWolf
 */

public final class FernflowerDecompiler extends JDADecompiler {
    public FernflowerDecompiler() {
        settings.registerSetting(new SettingsEntry(REMOVE_BRIDGE, "Hide Bridge Methods", false));
        settings.registerSetting(new SettingsEntry(REMOVE_SYNTHETIC, "Hide Synthetic Class Members", false));
        settings.registerSetting(new SettingsEntry(DECOMPILE_INNER, "Decompile Inner Classes", true));
        settings.registerSetting(new SettingsEntry(DECOMPILE_CLASS_1_4, "Collapse 1.4 Class References", true));
        settings.registerSetting(new SettingsEntry(DECOMPILE_ASSERTIONS, "Decompile Assertions", true));
        settings.registerSetting(new SettingsEntry(HIDE_EMPTY_SUPER, "Hide Empty Super Invocation", true));
        settings.registerSetting(new SettingsEntry(HIDE_DEFAULT_CONSTRUCTOR, "Hide Empty Default Constructor", true));
        settings.registerSetting(new SettingsEntry(DECOMPILE_GENERIC_SIGNATURES, "Decompile Generic Signatures", false));
        settings.registerSetting(new SettingsEntry(NO_EXCEPTIONS_RETURN, "Assume return not throwing exceptions", true));
        settings.registerSetting(new SettingsEntry(DECOMPILE_ENUM, "Decompile enumerations", true));
        settings.registerSetting(new SettingsEntry(REMOVE_GET_CLASS_NEW, "Remove getClass()", true));
        settings.registerSetting(new SettingsEntry(LITERALS_AS_IS, "Output numeric literals 'as-is'", false));
        settings.registerSetting(new SettingsEntry(BOOLEAN_TRUE_ONE, "Assume int 1 is boolean true", true));
        settings.registerSetting(new SettingsEntry(ASCII_STRING_CHARACTERS, "Encode non-ASCII as unicode escapes", true));
        settings.registerSetting(new SettingsEntry(SYNTHETIC_NOT_SET, "Allow not set synthetic attribute", true));
        settings.registerSetting(new SettingsEntry(UNDEFINED_PARAM_TYPE_OBJECT, "Consider nameless types as java.lang.Object", true));
        settings.registerSetting(new SettingsEntry(USE_DEBUG_VAR_NAMES, "Recover variable names", true));
        settings.registerSetting(new SettingsEntry(REMOVE_EMPTY_RANGES, "Remove empty exceptions", true));
        settings.registerSetting(new SettingsEntry(FINALLY_DEINLINE, "De-inline finally", true));
        settings.registerSetting(new SettingsEntry(IDEA_NOT_NULL_ANNOTATION, "Remove IntelliJ @NotNull", true));
        settings.registerSetting(new SettingsEntry(LAMBDA_TO_ANONYMOUS_CLASS, "Decompile lambdas to anonymous classes", false));
        settings.registerSetting(new SettingsEntry(MAX_PROCESSING_METHOD, "Maximum processing time", 0, Setting.SettingType.INT));
        settings.registerSetting(new SettingsEntry(RENAME_ENTITIES, "Rename ambigious members", false));
        // USER_RENAMER_CLASS IIDentifierRenamer
        // settings.registerSetting(new SettingsEntry(NEW_LINE_SEPARATOR, "Newline character")); // this is an optional argument!
        settings.registerSetting(new SettingsEntry(INDENT_STRING, "Indentation string", "    ", Setting.SettingType.STRING));
    }

    @Override
    public String getName() {
        return "Fernflower";
    }

    @Override
    public String decompileClassNode(String containerName, final ClassNode cn) {
        try {
            Map<String, Object> options = generateFernflowerArgs();

            final AtomicReference<String> result = new AtomicReference<>();
            result.set(null);

            BaseDecompiler baseDecompiler = new BaseDecompiler((externalPath, internalPath) -> {
                ClassNode requestedCn = JDA.getClassNode(containerName, JDA.extractProxyClassName(externalPath));
                if (requestedCn == null) {
                    System.err.println("Couldn't load " + externalPath);
                    return new byte[0];
                }
                byte[] bytes = JDA.getClassBytes(containerName, requestedCn);
                byte[] clone = new byte[bytes.length];
                System.arraycopy(bytes, 0, clone, 0, bytes.length);
                return clone;
            }, new IResultSaver() {
                @Override
                public void saveFolder(String s) {

                }

                @Override
                public void copyFile(String s, String s1, String s2) {

                }

                @Override
                public void saveClassFile(String filename, String className, String entryName, String decompilation, int[] ints) {
                    if (className.equals(cn.name))
                        result.set(decompilation);
                }

                @Override
                public void createArchive(String s, String s1, Manifest manifest) {

                }

                @Override
                public void saveDirEntry(String s, String s1, String s2) {

                }

                @Override
                public void copyEntry(String s, String s1, String s2, String s3) {

                }

                @Override
                public void saveClassEntry(String s, String s1, String s2, String s3, String s4) {
                }

                @Override
                public void closeArchive(String s, String s1) {

                }
            }, options, new PrintStreamLogger(System.out));

            baseDecompiler.addSpace(JDA.getClassFileProxy(cn), true);
            for (InnerClassNode innerCn : cn.innerClasses)
                baseDecompiler.addSpace(JDA.getClassFileProxy(innerCn), true);
            baseDecompiler.decompileContext();
            while (true) {
                if (result.get() != null) {
                    break;
                }
            }
            return result.get();
        } catch (Exception e) {
            return parseException(e);
        }
    }

    @Override
    public void decompileToZip(String zipName) {
        try {
            Path outputDir = Files.createTempDirectory("fernflower_output");
            Path tempJar = Files.createTempFile("fernflower_input", ".jar");
            File output = new File(zipName);
            JarUtils.saveAsJar(JDA.getLoadedBytes(), tempJar.toAbsolutePath().toString());
            ConsoleDecompiler decompiler = new ConsoleDecompiler(outputDir.toFile(), generateFernflowerArgs());
            decompiler.addSpace(tempJar.toFile(), true);
            decompiler.decompileContext();
            Files.move(outputDir.toFile().listFiles()[0].toPath(), output.toPath());
            Files.delete(tempJar);
            FileUtils.deleteDirectory(outputDir.toFile());
        } catch (Exception e) {
            handleException(e);
        }
    }

    private Map<String, Object> generateFernflowerArgs() {
        Map<String, Object> options = new HashMap<>();
        for (SettingsEntry setting : settings.getEntries()) {
            Object value = setting.get();
            if (value instanceof Boolean)
                value = (Boolean) value? "1" : "0";
            options.put(setting.key, value);
        }
        return options;
    }
}

