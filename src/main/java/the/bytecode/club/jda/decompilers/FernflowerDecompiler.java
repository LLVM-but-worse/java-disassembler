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
import the.bytecode.club.jda.settings.DecompilerSettings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.Manifest;

/**
 * A FernFlower wrapper with all the options (except 2)
 *
 * @author Konloch
 * @author WaterWolf
 */

public final class FernflowerDecompiler extends Decompiler {
    public FernflowerDecompiler() {
        for (Settings setting : Settings.values()) {
            settings.registerSetting(setting);
        }
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
//                System.out.println("boi " + externalPath + " " + internalPath);
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
                public void saveClassFile(String s, String s1, String s2, String decompilation, int[] ints) {
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
        for (Settings setting : Settings.values()) {
            options.put(setting.getParam(), getSettings().getValue(setting));
        }
        return options;
    }

    public enum Settings implements DecompilerSettings.SettingsEntry {
        HIDE_BRIDGE_METHODS("rbr", "Hide Bridge Methods", "true"),
        HIDE_SYNTHETIC_CLASS_MEMBERS("rsy", "Hide Synthetic Class Members", "false"),
        DECOMPILE_INNER_CLASSES("din", "Decompile Inner Classes", "true"),
        COLLAPSE_14_CLASS_REFERENCES("dc4", "Collapse 1.4 Class References", "true"),
        DECOMPILE_ASSERTIONS("das", "Decompile Assertions", "true"),
        HIDE_EMPTY_SUPER_INVOCATION("hes", "Hide Empty Super Invocation", "true"),
        HIDE_EMPTY_DEFAULT_CONSTRUCTOR("hec", "Hide Empty Default Constructor", "true"),
        DECOMPILE_GENERIC_SIGNATURES("dgs", "Decompile Generic Signatures", "false"),
        ASSUME_RETURN_NOT_THROWING_EXCEPTIONS("ner", "Assume return not throwing exceptions", "true"),
        DECOMPILE_ENUMS("den", "Decompile enumerations", "true"),
        REMOVE_GETCLASS("rgn", "Remove getClass()", "true"),
        OUTPUT_NUMBERIC_LITERALS("lit", "Output numeric literals 'as-is'", "false"),
        ENCODE_UNICODE("asc", "Encode non-ASCII as unicode escapes", "true"),
        INT_1_AS_BOOLEAN_TRUE("bto", "Assume int 1 is boolean true", "true"),
        ALLOW_NOT_SET_SYNTHETIC("nns", "Allow not set synthetic attribute", "true"),
        NAMELESS_TYPES_AS_OBJECT("uto", "Consider nameless types as java.lang.Object", "true"),
        RECOVER_VARIABLE_NAMES("udv", "Recover variable names", "true"),
        REMOVE_EMPTY_EXCEPTIONS("rer", "Remove empty exceptions", "true"),
        DEINLINE_FINALLY("fdi", "De-inline finally", "true"),
        TIME_LIMIT("mpm", "Maximum processing time", "0", SettingType.INT), // this is a numeric setting!
        RENAME_AMBIGIOUS_MEMBERS("ren", "Rename ambigious members", "false"),
        // urc: IIDentifierRenamer
        REMOVE_INTELLIJ_NOTNULL("inn", "Remove IntelliJ @NotNull", "true"),
        DECOMPILE_LAMBDA_TO_ANONYMOUS("lac", "Decompile lambdas to anonymous classes", "false"),
//        NEWLINE_TYPE("nls", "Newline character"); // this is an optional argument!
        INDENTATION("ind", "Indentation string", "    ", SettingType.STRING);

        private final String name;
        private final String param;
        private final String defaultValue;
        private final SettingType type;

        Settings(String param, String name, String defaultValue) {
            this(param, name, defaultValue, SettingType.BOOLEAN);
        }

        Settings(String param, String name, String defaultValue, SettingType type) {
            this.name = name;
            this.param = param;
            this.defaultValue = defaultValue;
            this.type = type;
        }

        public String getText() {
            return name;
        }

        public String getParam() {
            return param;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public SettingType getType() {
            return type;
        }
    }
}
