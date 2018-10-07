package club.bytecode.the.jda.decompilers;

import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.api.JDANamespace;
import club.bytecode.the.jda.gui.fileviewer.JDAJavaTokenizer;
import club.bytecode.the.jda.settings.JDADecompilerSettings.SettingsEntry;
import com.strobel.assembler.InputTypeLoader;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import org.objectweb.asm.tree.ClassNode;

import java.io.StringWriter;
import java.util.Map;

/**
 * Procyon Java Decompiler Wrapper
 *
 * @author Konloch
 * @author DeathMarine
 */

public final class ProcyonDecompiler extends JDADecompiler {

    public ProcyonDecompiler() {
        // output modes: Bytecode AST, raw bytecode, Java
        settings.registerSetting(new SettingsEntry("ci", "Use wildcard imports", false));
        settings.registerSetting(new SettingsEntry("dl", "Show LVT comments", false));
        settings.registerSetting(new SettingsEntry("disable-foreach", "Disable 'for each'", false));
        settings.registerSetting(new SettingsEntry("eml", "Eager method loading", false));
        settings.registerSetting(new SettingsEntry("ent", "Exclude nested types", false));
        settings.registerSetting(new SettingsEntry("ei", "Explicit type arguments", false));
        settings.registerSetting(new SettingsEntry("fsb", "Flatten switch blocks", false));
        settings.registerSetting(new SettingsEntry("mv", "Merge variables aggressively", false));
        settings.registerSetting(new SettingsEntry("ec", "Retain redundant casts", false));
        settings.registerSetting(new SettingsEntry("ps", "Retain pointless switches", false));
        settings.registerSetting(new SettingsEntry("ss", "Show synthetic members", true));
        settings.registerSetting(new SettingsEntry("sm", "Simplify member references", false));
        settings.registerSetting(new SettingsEntry("sl", "Stretch lines to match LVT", false));
        settings.registerSetting(new SettingsEntry("unicode", "Do not escape non-ASCII characters", false));
//        settings.registerSetting(new SettingsEntry("u", "Unoptimized AST", false));
    }

    @Override
    public String getName() {
        return "Procyon";
    }
    
    @Override
    public JDANamespace getNamespace() {
        return JDA.namespace;
    }

    public DecompilerSettings getDecompilerSettings() {
        DecompilerSettings procyonSettings = new DecompilerSettings();
        procyonSettings.setFlattenSwitchBlocks(settings.getEntry("fsb").getBool());
        procyonSettings.setForceExplicitImports(!settings.getEntry("ci").getBool());
        procyonSettings.setForceExplicitTypeArguments(settings.getEntry("ei").getBool());
        procyonSettings.setRetainRedundantCasts(settings.getEntry("ec").getBool());
        procyonSettings.setShowSyntheticMembers(settings.getEntry("ss").getBool());
        procyonSettings.setExcludeNestedTypes(settings.getEntry("ent").getBool());
//        procyonSettings.setOutputDirectory(options.getOutputDirectory());
        procyonSettings.setIncludeLineNumbersInBytecode(settings.getEntry("dl").getBool());
        procyonSettings.setRetainPointlessSwitches(settings.getEntry("ps").getBool());
        procyonSettings.setUnicodeOutputEnabled(settings.getEntry("unicode").getBool());
        procyonSettings.setMergeVariables(settings.getEntry("mv").getBool());
        procyonSettings.setShowDebugLineNumbers(settings.getEntry("dl").getBool());
        procyonSettings.setSimplifyMemberReferences(settings.getEntry("sm").getBool());
        procyonSettings.setDisableForEachTransforms(settings.getEntry("disable-foreach").getBool());
        procyonSettings.setTypeLoader(new InputTypeLoader());
//        procyonSettings.setLanguage(Languages.bytecode());
//        procyonSettings.setLanguage(settings.getEntry("Unoptimized AST").getBool() ? Languages.bytecodeAstUnoptimized() : Languages.bytecodeAst());
        return procyonSettings;
    }

    @Override
    public String decompileClassNode(FileContainer container, final ClassNode cn) {
        try {
            byte[] bytes = JDA.getClassBytes(container, cn);
            final Map<String, byte[]> loadedClasses = JDA.getLoadedBytes();
            MetadataSystem metadataSystem = new MetadataSystem(new ITypeLoader() {
                private InputTypeLoader backLoader = new InputTypeLoader();

                @Override
                public boolean tryLoadType(String s, Buffer buffer) {
                    if (s.equals(cn.name)) {
                        buffer.putByteArray(bytes, 0, bytes.length);
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
            DecompilerSettings settings = getDecompilerSettings();
            decompilationOptions.setSettings(settings);
            decompilationOptions.setFullDecompilation(true);
            TypeDefinition resolvedType;
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
    public String getTarget() {
        return JDAJavaTokenizer.SYNTAX_STYLE_JDA_JAVA;
    }
}
