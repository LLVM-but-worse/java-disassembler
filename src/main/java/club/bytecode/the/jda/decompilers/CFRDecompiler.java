package club.bytecode.the.jda.decompilers;

import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.api.JDANamespace;
import club.bytecode.the.jda.settings.JDADecompilerSettings;
import club.bytecode.the.jda.settings.JDADecompilerSettings.SettingsEntry;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.relationship.MemberNameResolver;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.bytestream.BaseByteData;
import org.benf.cfr.reader.util.getopt.GetOptParser;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.output.*;
import org.objectweb.asm.tree.ClassNode;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * CFR Java Wrapper
 *
 * @author Konloch
 */
public final class CFRDecompiler extends JDADecompiler {

    public CFRDecompiler() {
        settings.registerSetting(new SettingsEntry("decodeenumswitch", "Decode Enum Switch", true));
        settings.registerSetting(new SettingsEntry("sugarenums", "SugarEnums", true));
        settings.registerSetting(new SettingsEntry("decodestringswitch", "Decode String Switch", true));
        settings.registerSetting(new SettingsEntry("arrayiter", "Arrayiter", true));
        settings.registerSetting(new SettingsEntry("collectioniter", "Collectioniter", true));
        settings.registerSetting(new SettingsEntry("innerclasses", "Inner Classes", true));
        settings.registerSetting(new SettingsEntry("removeboilerplate", "Remove Boiler Plate", true));
        settings.registerSetting(new SettingsEntry("removeinnerclasssynthetics", "Remove Inner Class Synthetics", true));
        settings.registerSetting(new SettingsEntry("decodelambdas", "Decode Lambdas", true));
        settings.registerSetting(new SettingsEntry("hidebridgemethods", "Hide Bridge Methods", true));
        settings.registerSetting(new SettingsEntry("liftconstructorinit", "Lift Constructor Init", true));
        settings.registerSetting(new SettingsEntry("removedeadmethods", "Remove Dead Methods", true));
        settings.registerSetting(new SettingsEntry("removebadgenerics", "Remove Bad Generics", true));
        settings.registerSetting(new SettingsEntry("sugarasserts", "Sugar Asserts", true));
        settings.registerSetting(new SettingsEntry("sugarboxing", "Sugar Boxing", true));
        settings.registerSetting(new SettingsEntry("showversion", "Show Version", true));
        settings.registerSetting(new SettingsEntry("decodefinally", "Decode Finally", true));
        settings.registerSetting(new SettingsEntry("tidymonitors", "Tidy Monitors", true));
        settings.registerSetting(new SettingsEntry("lenient", "Lenient", false));
        settings.registerSetting(new SettingsEntry("dumpclasspath", "Dump Classpath", false));
        settings.registerSetting(new SettingsEntry("comments", "Comments", true));
        settings.registerSetting(new SettingsEntry("forcetopsort", "Force Top Sort", true));
        settings.registerSetting(new SettingsEntry("forcetopsortaggress", "Force Top Sort Aggressive", true));
        settings.registerSetting(new SettingsEntry("stringbuffer", "StringBuffer", false));
        settings.registerSetting(new SettingsEntry("stringbuilder", "StringBuilder", true));
        settings.registerSetting(new SettingsEntry("silent", "Silent", true));
        settings.registerSetting(new SettingsEntry("recover", "Recover", true));
        settings.registerSetting(new SettingsEntry("eclipse", "Eclipse", true));
        settings.registerSetting(new SettingsEntry("override", "Override", true));
        settings.registerSetting(new SettingsEntry("showinferrable", "Show Inferrable", true));
        settings.registerSetting(new SettingsEntry("aexagg", "Force Aggressive Exception Aggregation", true));
        settings.registerSetting(new SettingsEntry("forcecondpropagate", "Force Conditional Propogation", true));
        settings.registerSetting(new SettingsEntry("hideutf", "Hide UTF", true));
        settings.registerSetting(new SettingsEntry("hidelongstrings", "Hide Long Strings", false));
        settings.registerSetting(new SettingsEntry("commentmonitors", "Comment Monitors", false));
        settings.registerSetting(new SettingsEntry("allowcorrecting", "Allow Correcting", true));
        settings.registerSetting(new SettingsEntry("labelledblocks", "Labelled Blocks", true));
        settings.registerSetting(new SettingsEntry("j14classobj", "Java 1.4 Class Objects", false));
        settings.registerSetting(new SettingsEntry("hidelangimports", "Hide Lang Imports", true));
        settings.registerSetting(new SettingsEntry("recovertypeclash", "Recover Type Clash", true));
        settings.registerSetting(new SettingsEntry("recovertypehints", "Recover Type Hints", true));
        settings.registerSetting(new SettingsEntry("forcereturningifs", "Force Returning Ifs", true));
        settings.registerSetting(new SettingsEntry("forloopaggcapture", "For Loop Aggressive Capture", true));
    }

    @Override
    public JDADecompilerSettings getSettings() {
        return settings;
    }

    @Override
    public String getName() {
        return "CFR";
    }
    
    @Override
    public JDANamespace getNamespace() {
        return JDA.namespace;
    }

    @Override
    public String decompileClassNode(FileContainer container, ClassNode cn) {
        try {
            byte[] bytes = JDA.dumpClassToBytes(cn);
            Options options = new GetOptParser().parse(generateMainMethod(), OptionsImpl.getFactory());
            ClassFileSourceImpl classFileSource = new ClassFileSourceImpl(options);
            DCCommonState dcCommonState = new DCCommonState(options, classFileSource);
            return doClass(dcCommonState, bytes);
        } catch (Exception e) {
            return parseException(e);
        }
    }

    public String[] generateMainMethod() {
        Set<SettingsEntry> entries = settings.getEntries();
        String[] result = new String[entries.size() * 2 + 1];
        result[0] = "bytecodeviewer";
        int index = 1;
        for (SettingsEntry setting : entries) {
            result[index++] = "--" + setting.key;
            result[index++] = String.valueOf(setting.getBool());
        }
        return result;
    }

    public static String doClass(DCCommonState dcCommonState, byte[] content1) throws Exception {
        Options options = dcCommonState.getOptions();
        Dumper d = new ToStringDumper();
        BaseByteData data = new BaseByteData(content1);
        ClassFile var24 = new ClassFile(data, "", dcCommonState);
        dcCommonState.configureWith(var24);

        try {
            var24 = dcCommonState.getClassFile(var24.getClassType());
        } catch (CannotLoadClassException var18) {
            var18.printStackTrace();
        }

        if (options.getOption(OptionsImpl.DECOMPILE_INNER_CLASSES)) {
            var24.loadInnerClasses(dcCommonState);
        }

        if (options.getOption(OptionsImpl.RENAME_MEMBERS)) {
            MemberNameResolver.resolveNames(dcCommonState, ListFactory.newList(dcCommonState.getClassCache().getLoadedTypes()));
        }

        var24.analyseTop(dcCommonState);
        TypeUsageCollector var25 = new TypeUsageCollector(var24);
        var24.collectTypeUsages(var25);
        String var26 = options.getOption(OptionsImpl.METHODNAME);
        if (var26 == null) {
            var24.dump(d);
        } else {
            try {
                for (Method method : var24.getMethodByName(var26)) {
                    method.dump(d, true);
                }
            } catch (NoSuchMethodException var19) {
                throw new IllegalArgumentException("No such method \'" + var26 + "\'.");
            }
        }
        d.print("");
        return d.toString();
    }

    public static void doJar(DCCommonState dcCommonState, Path input, Path output) throws Exception {
        SummaryDumper summaryDumper = new NopSummaryDumper();
        Dumper d = new ToStringDumper();
        Options options = dcCommonState.getOptions();
        IllegalIdentifierDump illegalIdentifierDump = IllegalIdentifierDump.Factory.get(options);

        final Predicate e = org.benf.cfr.reader.util.MiscUtils.mkRegexFilter(options.getOption(OptionsImpl.JAR_FILTER), true);

        List<JavaTypeInstance> err1 = dcCommonState.explicitlyLoadJar(input.toAbsolutePath().toString());
        err1 = Functional.filter(err1, in -> e.test(in.getRawName()));
        if (options.getOption(OptionsImpl.RENAME_MEMBERS)) {
            MemberNameResolver.resolveNames(dcCommonState, err1);
        }

        for (JavaTypeInstance type : err1) {
            try {
                ClassFile e1 = dcCommonState.getClassFile(type);
                if (e1.isInnerClass()) {
                    d = null;
                } else {
                    if (options.getOption(OptionsImpl.DECOMPILE_INNER_CLASSES)) {
                        e1.loadInnerClasses(dcCommonState);
                    }

                    e1.analyseTop(dcCommonState);
                    TypeUsageCollector collectingDumper = new TypeUsageCollector(e1);
                    e1.collectTypeUsages(collectingDumper);
                    d = new FileDumper(output.toAbsolutePath().toString(), true, e1.getClassType(), summaryDumper, collectingDumper.getTypeUsageInformation(), options, illegalIdentifierDump);
                    e1.dump(d);
                    d.print("\n");
                    d.print("\n");
                }
            } catch (Dumper.CannotCreate var25) {
                throw var25;
            } catch (RuntimeException var26) {
                d.print(var26.toString()).print("\n").print("\n").print("\n");
            } finally {
                if (d != null) {
                    d.close();
                }
            }
        }
    }
}
