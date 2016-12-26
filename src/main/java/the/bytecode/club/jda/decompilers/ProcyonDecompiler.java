package the.bytecode.club.jda.decompilers;

import com.beust.jcommander.JCommander;
import com.strobel.assembler.InputTypeLoader;
import com.strobel.assembler.metadata.*;
import com.strobel.core.StringUtilities;
import com.strobel.decompiler.CommandLineOptions;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.decompiler.languages.Languages;
import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.jda.JDA;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

/**
 * Procyon Java Decompiler Wrapper
 *
 * @author Konloch
 * @author DeathMarine
 */

public class ProcyonDecompiler extends Decompiler {

    public ProcyonDecompiler() {
        for (Settings setting : Settings.values()) {
            settings.registerSetting(setting);
        }
    }

    @Override
    public String getName() {
        return "Procyon";
    }

    public DecompilerSettings getDecompilerSettings() {
        CommandLineOptions options = new CommandLineOptions();
        JCommander jCommander = new JCommander(options);
        List<String> args = new ArrayList<>();
        for (the.bytecode.club.jda.settings.DecompilerSettings.Setting setting : Settings.values())
            if (getSettings().isSelected(setting))
                args.add("--" + setting.getParam());
        String[] argsArr = new String[args.size()];
        args.toArray(argsArr);
        jCommander.parse(argsArr);
        DecompilerSettings settings = new DecompilerSettings();
        settings.setFlattenSwitchBlocks(options.getFlattenSwitchBlocks());
        settings.setForceExplicitImports(!options.getCollapseImports());
        settings.setForceExplicitTypeArguments(options.getForceExplicitTypeArguments());
        settings.setRetainRedundantCasts(options.getRetainRedundantCasts());
        settings.setShowSyntheticMembers(options.getShowSyntheticMembers());
        settings.setExcludeNestedTypes(options.getExcludeNestedTypes());
        settings.setOutputDirectory(options.getOutputDirectory());
        settings.setIncludeLineNumbersInBytecode(options.getIncludeLineNumbers());
        settings.setRetainPointlessSwitches(options.getRetainPointlessSwitches());
        settings.setUnicodeOutputEnabled(options.isUnicodeOutputEnabled());
        settings.setMergeVariables(options.getMergeVariables());
        settings.setShowDebugLineNumbers(options.getShowDebugLineNumbers());
        settings.setSimplifyMemberReferences(options.getSimplifyMemberReferences());
        settings.setDisableForEachTransforms(options.getDisableForEachTransforms());
        settings.setTypeLoader(new InputTypeLoader());
        if (options.isRawBytecode()) {
            settings.setLanguage(Languages.bytecode());
        } else if (options.isBytecodeAst()) {
            settings.setLanguage(
                    options.isUnoptimized() ? Languages.bytecodeAstUnoptimized() : Languages.bytecodeAst());
        }
        return settings;
    }

    @Override
    public String decompileClassNode(final ClassNode cn, byte[] b) {
        try {
            if (cn.version < 49) {
                b = fixBytes(b);
            }
            final byte[] bytesToUse = b;
            final Map<String, byte[]> loadedClasses = JDA.getLoadedBytes();
            DecompilerSettings settings = getDecompilerSettings();
            MetadataSystem metadataSystem = new MetadataSystem(new ITypeLoader() {
                private InputTypeLoader backLoader = new InputTypeLoader();

                @Override
                public boolean tryLoadType(String s, Buffer buffer) {
                    if (s.equals(cn.name)) {
                        buffer.putByteArray(bytesToUse, 0, bytesToUse.length);
                        buffer.position(0);
                        return true;
                    } else {
                        byte[] toUse = loadedClasses.get(s + ".class");
                        if (toUse != null) {
                            buffer.putByteArray(toUse, 0, toUse.length);
                            buffer.position(0);
                            return true;
                        } else {
                            return backLoader.tryLoadType(s, buffer);
                        }
                    }
                }
            });
            TypeReference type = metadataSystem.lookupType(cn.name);
            DecompilationOptions decompilationOptions = new DecompilationOptions();
            decompilationOptions.setSettings(getDecompilerSettings());
            decompilationOptions.setFullDecompilation(true);
            TypeDefinition resolvedType = null;
            if (type == null || ((resolvedType = type.resolve()) == null)) {
                throw new Exception("Unable to resolve type.");
            }
            StringWriter stringwriter = new StringWriter();
            settings.getLanguage().decompileType(resolvedType, new PlainTextOutput(stringwriter), decompilationOptions);
            String decompiledSource = stringwriter.toString();
            return decompiledSource;
        } catch (Throwable e) {
            return parseException(e);
        }
    }

    @Override
    public void decompileToZip(String zipName) {
        // todo: rewrite
    }

    /**
     * @author DeathMarine
     */
    private void doSaveJarDecompiled(File inFile, File outFile) throws Exception {
        try (JarFile jfile = new JarFile(inFile);
             FileOutputStream dest = new FileOutputStream(outFile);
             BufferedOutputStream buffDest = new BufferedOutputStream(dest);
             ZipOutputStream out = new ZipOutputStream(buffDest);) {
            byte data[] = new byte[1024];
            DecompilerSettings settings = getDecompilerSettings();
            MetadataSystem metadataSystem = new MetadataSystem(new JarTypeLoader(jfile));

            DecompilationOptions decompilationOptions = new DecompilationOptions();
            decompilationOptions.setSettings(settings);
            decompilationOptions.setFullDecompilation(true);

            Enumeration<JarEntry> ent = jfile.entries();
            Set<JarEntry> history = new HashSet<>();
            while (ent.hasMoreElements()) {
                JarEntry entry = ent.nextElement();
                if (entry.getName().endsWith(".class")) {
                    JarEntry etn = new JarEntry(entry.getName().replace(".class", ".java"));
                    if (history.add(etn)) {
                        out.putNextEntry(etn);
                        try {
                            String internalName = StringUtilities.removeRight(entry.getName(), ".class");
                            TypeReference type = metadataSystem.lookupType(internalName);
                            TypeDefinition resolvedType = null;
                            if ((type == null) || ((resolvedType = type.resolve()) == null)) {
                                throw new Exception("Unable to resolve type.");
                            }
                            Writer writer = new OutputStreamWriter(out);
                            settings.getLanguage().decompileType(resolvedType, new PlainTextOutput(writer), decompilationOptions);
                            writer.flush();
                        } finally {
                            out.closeEntry();
                        }
                    }
                } else {
                    try {
                        JarEntry etn = new JarEntry(entry.getName());
                        if (history.add(etn))
                            continue;
                        history.add(etn);
                        out.putNextEntry(etn);
                        try {
                            InputStream in = jfile.getInputStream(entry);
                            if (in != null) {
                                try {
                                    int count;
                                    while ((count = in.read(data, 0, 1024)) != -1) {
                                        out.write(data, 0, count);
                                    }
                                } finally {
                                    in.close();
                                }
                            }
                        } finally {
                            out.closeEntry();
                        }
                    } catch (ZipException ze) {
                        // some jar-s contain duplicate pom.xml entries: ignore
                        // it
                        if (!ze.getMessage().contains("duplicate")) {
                            throw ze;
                        }
                    }
                }
            }
        }
    }

    public enum Settings implements the.bytecode.club.jda.settings.DecompilerSettings.Setting {
        SHOW_DEBUG_LINE_NUMBERS("debug-line-numbers", "Show Debug Line Numbers"),
        SIMPLIFY_MEMBER_REFERENCES("simplify-member-references", "Simplify Member References"),
        MERGE_VARIABLES("merge-variables", "Merge Variables"),
        UNICODE_OUTPUT("unicode", "Allow Unicode Output"),
        RETAIN_POINTLESS_SWITCHES("retain-pointless-switches", "Retain pointless switches"),
        INCLUDE_LINE_NUMBERS_IN_BYTECODE("with-line-numbers", "Include line numbers in bytecode"),
        RETAIN_REDUNDANT_CASTS("retain-explicit-casts", "Retain redundant casts"),
        SHOW_SYNTHETIC_MEMBERS("show-synthetic", "Show synthetic members"),
        FORCE_EXPLICIT_TYPE_ARGS("explicit-type-arguments", "Force explicit type arguments"),
        FORCE_EXPLICIT_IMPORTS("explicit-imports", "Force explicit imports"),
        FLATTEN_SWITCH_BLOCKS("flatten-switch-blocks", "Flatten switch blocks"),
        EXCLUDE_NESTED_TYPES("exclude-nested", "Exclude nested types");

        private String name;
        private String param;
        private boolean on;

        Settings(String param, String name) {
            this(param, name, false);
        }

        Settings(String param, String name, boolean on) {
            this.name = name;
            this.param = param;
            this.on = on;
        }

        public String getText() {
            return name;
        }

        public boolean isDefaultOn() {
            return on;
        }

        public String getParam() {
            return param;
        }
    }
}
