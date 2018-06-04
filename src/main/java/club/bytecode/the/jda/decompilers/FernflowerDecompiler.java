package club.bytecode.the.jda.decompilers;

import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.api.JDANamespace;
import club.bytecode.the.jda.settings.JDADecompilerSettings.SettingsEntry;
import club.bytecode.the.jda.settings.Setting;
import org.jetbrains.java.decompiler.main.decompiler.BaseDecompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.Manifest;

import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.*;

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
    public JDANamespace getNamespace() {
        return JDA.namespace;
    }

    @Override
    public String decompileClassNode(FileContainer container, final ClassNode cn) {
        try {
            Map<String, Object> options = generateFernflowerArgs();
            Map<String, ClassNode> classCache = new HashMap<>();

            final AtomicReference<String> result = new AtomicReference<>();
            result.set(null);

            BaseDecompiler baseDecompiler = new BaseDecompiler((externalPath, internalPath) -> {
                String className = JDA.extractProxyClassName(externalPath);
                ClassNode requestedCn;
                if (classCache.containsKey(className)) {
                    requestedCn = classCache.get(className);
                } else {
                    requestedCn = container.loadClass(container.findClassfile(className));
                    if (requestedCn == null) {
                        System.err.println("Couldn't load " + externalPath);
                        throw new IOException(container + "$" + cn + " is missing");
                    }
                    applyFilters(requestedCn);
                    classCache.put(className, requestedCn);
                }
                return JDA.dumpClassToBytes(requestedCn);
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

            // DFS for inner classes
            Set<String> visited = new HashSet<>(); // necessary apparently...
            ArrayDeque<ClassNode> fifo = new ArrayDeque<>();
            fifo.add(cn);
            while (!fifo.isEmpty()) {
                ClassNode curCn = fifo.pop();
                visited.add(curCn.name);
                baseDecompiler.addSpace(JDA.getClassFileProxy(curCn), true);
                for (InnerClassNode innerClass : curCn.innerClasses) {
                    if (visited.contains(innerClass.name))
                        continue;
                    ClassNode innerCn;
                    if (classCache.containsKey(innerClass.name)) {
                        innerCn = classCache.get(innerClass.name);
                    } else {
                        innerCn = container.loadClass(container.findClassfile(innerClass.name));
                        if (innerCn != null) {
                            applyFilters(innerCn);
                            classCache.put(innerCn.name, innerCn);
                        }
                    }
                    if (innerCn != null) {
                        fifo.add(innerCn);
                    }
                }
            }

            baseDecompiler.decompileContext();
            String decompileResult = result.get();
            if (decompileResult == null) {
                return "// Fernflower returned null; perhaps this class is an inner class? Fernflower didn't play nice.";
            } else {
                return decompileResult;
            }
        } catch (Exception e) {
            return parseException(e);
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

