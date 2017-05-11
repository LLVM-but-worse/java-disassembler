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
import the.bytecode.club.jda.settings.DecompilerSettings.SettingsEntry;
import the.bytecode.club.jda.settings.Setting;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private static Map<String, Object> generateFernflowerArgs() {
        Map<String, Object> options = new HashMap<>();
        for (SettingsEntry setting : FernflowerDecompiler.settings) {
            options.put(setting.param, setting.get());
            System.out.println(setting.param + " " + setting.get());
        }
        return options;
    }

    private static List<SettingsEntry> settings = new ArrayList<>();

    static
    {
        SettingsEntry HIDE_BRIDGE_METHODS = new SettingsEntry("rbr", "Hide Bridge Methods", "true");
        SettingsEntry HIDE_SYNTHETIC_CLASS_MEMBERS = new SettingsEntry("rsy", "Hide Synthetic Class Members", "false");
        SettingsEntry DECOMPILE_INNER_CLASSES = new SettingsEntry("din", "Decompile Inner Classes", "true");
        SettingsEntry COLLAPSE_14_CLASS_REFERENCES = new SettingsEntry("dc4", "Collapse 1.4 Class References", "true");
        SettingsEntry DECOMPILE_ASSERTIONS = new SettingsEntry("das", "Decompile Assertions", "true");
        SettingsEntry HIDE_EMPTY_SUPER_INVOCATION = new SettingsEntry("hes", "Hide Empty Super Invocation", "true");
        SettingsEntry HIDE_EMPTY_DEFAULT_CONSTRUCTOR = new SettingsEntry("hec", "Hide Empty Default Constructor", "true");
        SettingsEntry DECOMPILE_GENERIC_SIGNATURES = new SettingsEntry("dgs", "Decompile Generic Signatures", "false");
        SettingsEntry ASSUME_RETURN_NOT_THROWING_EXCEPTIONS = new SettingsEntry("ner", "Assume return not throwing exceptions", "true");
        SettingsEntry DECOMPILE_ENUMS = new SettingsEntry("den", "Decompile enumerations", "true");
        SettingsEntry REMOVE_GETCLASS = new SettingsEntry("rgn", "Remove getClass = new SettingsEntry()", "true");
        SettingsEntry OUTPUT_NUMBERIC_LITERALS = new SettingsEntry("lit", "Output numeric literals 'as-is'", "false");
        SettingsEntry ENCODE_UNICODE = new SettingsEntry("asc", "Encode non-ASCII as unicode escapes", "true");
        SettingsEntry INT_1_AS_BOOLEAN_TRUE = new SettingsEntry("bto", "Assume int 1 is boolean true", "true");
        SettingsEntry ALLOW_NOT_SET_SYNTHETIC = new SettingsEntry("nns", "Allow not set synthetic attribute", "true");
        SettingsEntry NAMELESS_TYPES_AS_OBJECT = new SettingsEntry("uto", "Consider nameless types as java.lang.Object", "true");
        SettingsEntry RECOVER_VARIABLE_NAMES = new SettingsEntry("udv", "Recover variable names", "true");
        SettingsEntry REMOVE_EMPTY_EXCEPTIONS = new SettingsEntry("rer", "Remove empty exceptions", "true");
        SettingsEntry DEINLINE_FINALLY = new SettingsEntry("fdi", "De-inline finally", "true");
        SettingsEntry TIME_LIMIT = new SettingsEntry("mpm", "Maximum processing time", "0", Setting.SettingType.INT); // this is a numeric setting!
        SettingsEntry RENAME_AMBIGIOUS_MEMBERS = new SettingsEntry("ren", "Rename ambigious members", "false");
        // urc: IIDentifierRenamer
        SettingsEntry REMOVE_INTELLIJ_NOTNULL = new SettingsEntry("inn", "Remove IntelliJ @NotNull", "true");
        SettingsEntry DECOMPILE_LAMBDA_TO_ANONYMOUS = new SettingsEntry("lac", "Decompile lambdas to anonymous classes", "false");
        //        NEWLINE_TYPE = new SettingsEntry("nls", "Newline character"); // this is an optional argument!
        SettingsEntry INDENTATION = new SettingsEntry("ind", "Indentation string", "    ", Setting.SettingType.STRING);

        settings.add(HIDE_BRIDGE_METHODS);
        settings.add(HIDE_SYNTHETIC_CLASS_MEMBERS);
        settings.add(DECOMPILE_INNER_CLASSES);
        settings.add(COLLAPSE_14_CLASS_REFERENCES);
        settings.add(DECOMPILE_ASSERTIONS);
        settings.add(HIDE_EMPTY_SUPER_INVOCATION);
        settings.add(HIDE_EMPTY_DEFAULT_CONSTRUCTOR);
        settings.add(DECOMPILE_GENERIC_SIGNATURES);
        settings.add(ASSUME_RETURN_NOT_THROWING_EXCEPTIONS);
        settings.add(DECOMPILE_ENUMS);
        settings.add(REMOVE_GETCLASS);
        settings.add(OUTPUT_NUMBERIC_LITERALS);
        settings.add(ENCODE_UNICODE);
        settings.add(INT_1_AS_BOOLEAN_TRUE);
        settings.add(ALLOW_NOT_SET_SYNTHETIC);
        settings.add(NAMELESS_TYPES_AS_OBJECT);
        settings.add(RECOVER_VARIABLE_NAMES);
        settings.add(REMOVE_EMPTY_EXCEPTIONS);
        settings.add(DEINLINE_FINALLY);
        settings.add(TIME_LIMIT);
        settings.add(RENAME_AMBIGIOUS_MEMBERS);
        settings.add(REMOVE_INTELLIJ_NOTNULL);
        settings.add(DECOMPILE_LAMBDA_TO_ANONYMOUS);
        settings.add(INDENTATION);
    }
}
