package club.bytecode.the.jda.decompilers;

import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.JDA;
import club.bytecode.the.jda.api.JDANamespace;
import club.bytecode.the.jda.gui.fileviewer.JDAJavaTokenizer;
import club.bytecode.the.jda.settings.JDADecompilerSettings;
import club.bytecode.the.jda.settings.JDADecompilerSettings.SettingsEntry;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.relationship.MemberNameResolver;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.*;
import org.benf.cfr.reader.util.bytestream.BaseByteData;
import org.benf.cfr.reader.util.getopt.GetOptParser;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.output.*;
import org.objectweb.asm.tree.ClassNode;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * CFR Java Wrapper
 *
 * @author Konloch
 */
public final class CFRDecompiler extends JDADecompiler {

    public CFRDecompiler() {
        settings.registerSetting(new SettingsEntry("stringbuffer", "Convert new Stringbuffer().add.add.add to string + string + string - see http://www.benf.org/other/cfr/stringbuilder-vs-concatenation.html", false));
        settings.registerSetting(new SettingsEntry("stringbuilder", "Convert new Stringbuilder().add.add.add to string + string + string - see http://www.benf.org/other/cfr/stringbuilder-vs-concatenation.html", true));
        settings.registerSetting(new SettingsEntry("decodeenumswitch", "Re-sugar switch on enum - see http://www.benf.org/other/cfr/switch-on-enum.html", true));
        settings.registerSetting(new SettingsEntry("sugarenums", "Re-sugar enums - see http://www.benf.org/other/cfr/how-are-enums-implemented.html", true));
        settings.registerSetting(new SettingsEntry("decodestringswitch", "Re-sugar switch on String - see http://www.benf.org/other/cfr/java7switchonstring.html", true));
        settings.registerSetting(new SettingsEntry("arrayiter", "Re-sugar array based iteration.", true));
        settings.registerSetting(new SettingsEntry("collectioniter", "Re-sugar collection based iteration", true));
        settings.registerSetting(new SettingsEntry("tryresources", "Reconstruct try-with-resources", true));
        settings.registerSetting(new SettingsEntry("decodelambdas", "Re-build lambda functions", true));
        settings.registerSetting(new SettingsEntry("innerclasses", "Decompile inner classes", true));
        settings.registerSetting(new SettingsEntry("skipbatchinnerclasses", "When processing many files, skip inner classes, as they will be processed as part of outer classes anyway.  If false, you will see inner classes as separate entities also.", true));
        settings.registerSetting(new SettingsEntry("hideutf", "Hide UTF8 characters - quote them instead of showing the raw characters", true));
        settings.registerSetting(new SettingsEntry("hidelongstrings", "Hide very long strings - useful if obfuscators have placed fake code in strings", false));
        settings.registerSetting(new SettingsEntry("removeboilerplate", "Remove boilderplate functions - constructor boilerplate, lambda deserialisation etc", true));
        settings.registerSetting(new SettingsEntry("removeinnerclasssynthetics", "Remove (where possible) implicit outer class references in inner classes", true));
        settings.registerSetting(new SettingsEntry("hidebridgemethods", "Hide bridge methods", true));
        settings.registerSetting(new SettingsEntry("relinkconststring", "Relink constant strings - if there is a local reference to a string which matches a static final, use the static final.", true));
        settings.registerSetting(new SettingsEntry("liftconstructorinit", "Lift initialisation code common to all constructors into member initialisation", true));
        settings.registerSetting(new SettingsEntry("removedeadmethods", "Remove pointless methods - default constructor etc", true));
        settings.registerSetting(new SettingsEntry("removebadgenerics", "Hide generics where we've obviously got it wrong, and fallback to non-generic", true));
        settings.registerSetting(new SettingsEntry("sugarasserts", "Re-sugar assert calls", true));
        settings.registerSetting(new SettingsEntry("sugarboxing", "Where possible, remove pointless boxing wrappers", true));
        settings.registerSetting(new SettingsEntry("showversion", "Show CFR version used in header (handy to turn off when regression testing)", true));
        settings.registerSetting(new SettingsEntry("decodefinally", "Re-sugar finally statements", true));
        settings.registerSetting(new SettingsEntry("tidymonitors", "Remove support code for monitors - eg catch blocks just to exit a monitor", true));
        settings.registerSetting(new SettingsEntry("commentmonitors", "Replace monitors with comments - useful if we're completely confused", false));
        settings.registerSetting(new SettingsEntry("lenient", "Be a bit more lenient in situations where we'd normally throw an exception", false));
        settings.registerSetting(new SettingsEntry("dumpclasspath", "Dump class path for debugging purposes", false));
        settings.registerSetting(new SettingsEntry("comments", "Output comments describing decompiler status, fallback flags etc", true));
        settings.registerSetting(new SettingsEntry("forcetopsort", "Force basic block sorting.  Usually not necessary for code emitted directly from javac, but required in the case of obfuscation (or dex2jar!).  Will be enabled in recovery.", false));
        settings.registerSetting(new SettingsEntry("forloopaggcapture", "Allow for loops to aggresively roll mutations into update section, even if they don't appear to be involved with the predicate", false));
        settings.registerSetting(new SettingsEntry("forcetopsortaggress", "Force extra aggressive topsort options", false));
        settings.registerSetting(new SettingsEntry("forcecondpropagate", "Pull results of deterministic jumps back through some constant assignments", false));
        settings.registerSetting(new SettingsEntry("forcereturningifs", "Move return up to jump site", false));
        settings.registerSetting(new SettingsEntry("ignoreexceptions", "Drop exception information if completely stuck (WARNING : changes semantics, dangerous!)", false));
        settings.registerSetting(new SettingsEntry("forceexceptionprune", "Try to extend and merge exceptions more aggressively", false));
        settings.registerSetting(new SettingsEntry("aexagg", "Remove nested exception handlers if they don't change semantics", false));
        settings.registerSetting(new SettingsEntry("recovertypeclash", "Split lifetimes where analysis caused type clash", false));
        settings.registerSetting(new SettingsEntry("recovertypehints", "Recover type hints for iterators from first pass.", false));
        settings.registerSetting(new SettingsEntry("silent", "Don't display state while decompiling", false));
        settings.registerSetting(new SettingsEntry("recover", "Allow more and more aggressive options to be set if decompilation fails", true));
        settings.registerSetting(new SettingsEntry("eclipse", "Enable transformations to handle eclipse code better", true));
        settings.registerSetting(new SettingsEntry("override", "Generate @Override annotations (if method is seen to implement interface method, or override a base class method)", true));
        settings.registerSetting(new SettingsEntry("showinferrable", "Decorate methods with explicit types if not implied by arguments.", false));
        settings.registerSetting(new SettingsEntry("allowcorrecting", "Allow transformations which correct errors, potentially at the cost of altering emitted code behaviour.  An example would be removing impossible (in java!) exception handling - if this has any effect, a warning will be emitted.", true));
        settings.registerSetting(new SettingsEntry("labelledblocks", "Allow code to be emitted which uses labelled blocks, (handling odd forward gotos)", true));
        settings.registerSetting(new SettingsEntry("j14classobj", "Reverse java 1.4 class object construction", false));
        settings.registerSetting(new SettingsEntry("hidelangimports", "Hide imports from java.lang.", true));
        settings.registerSetting(new SettingsEntry("rename", "Synonym for 'renamedupmembers' + 'renameillegalidents' + 'renameenummembers'", false));
        settings.registerSetting(new SettingsEntry("usenametable", "Use local variable name table if present", true));
        settings.registerSetting(new SettingsEntry("pullcodecase", "Pull code into case statements agressively.", false));
        settings.registerSetting(new SettingsEntry("elidescala", "Elide things which aren't helpful in scala output (serialVersionUID, @ScalaSignature).", false));
        settings.registerSetting(new SettingsEntry("caseinsensitivefs", "Cope with case insensitive file systems by renaming colliding classes.", false));

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
            byte[] bytes = JDA.getClassBytes(container, cn);
            GetOptParser getOptParser = new GetOptParser();
            Pair processedArgs = getOptParser.parse(generateMainMethod(), OptionsImpl.getFactory());
            List files = (List)processedArgs.getFirst();
            Options options = (Options)processedArgs.getSecond();
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

    public static String doClass(DCCommonState dcCommonState, byte[] content1) {
        Options options = dcCommonState.getOptions();
        IllegalIdentifierDump illegalIdentifierDump = IllegalIdentifierDump.Factory.get(options);
        Dumper d = new ToStringDumper();

        try {
            StackTraceElement[] arr$;
            int len$;
            int i$;
            StackTraceElement x;
            try {
                SummaryDumper summaryDumper = new NopSummaryDumper();
                BaseByteData data = new BaseByteData(content1);
                ClassFile c = new ClassFile(data, "", dcCommonState);

                dcCommonState.configureWith(c);

                try {
                    c = dcCommonState.getClassFile(c.getClassType());
                } catch (CannotLoadClassException var20) {
                }

                if (options.getOption(OptionsImpl.DECOMPILE_INNER_CLASSES)) {
                    c.loadInnerClasses(dcCommonState);
                }

                if (options.getOption(OptionsImpl.RENAME_DUP_MEMBERS)) {
                    MemberNameResolver.resolveNames(dcCommonState, ListFactory.newList(dcCommonState.getClassCache().getLoadedTypes()));
                }

                c.analyseTop(dcCommonState);
                TypeUsageCollector collectingDumper = new TypeUsageCollector(c);
                c.collectTypeUsages(collectingDumper);
                String methname = options.getOption(OptionsImpl.METHODNAME);
                if (methname == null) {
                    c.dump(d);
                } else {
                    try {
                        Iterator i$$ = c.getMethodByName(methname).iterator();

                        while(i$$.hasNext()) {
                            Method method = (Method)i$$.next();
                            method.dump(d, true);
                        }
                    } catch (NoSuchMethodException var21) {
                        throw new IllegalArgumentException("No such method '" + methname + "'.");
                    }
                }

                d.print("");
                return d.toString();
            } catch (ConfusedCFRException var22) {
                System.err.println(var22.toString());
                arr$ = var22.getStackTrace();
                len$ = arr$.length;

                for(i$ = 0; i$ < len$; ++i$) {
                    x = arr$[i$];
                    System.err.println(x);
                }
            } catch (CannotLoadClassException var23) {
                System.out.println("Can't load the class specified:");
                System.out.println(var23.toString());
            } catch (RuntimeException var24) {
                System.err.println(var24.toString());
                arr$ = var24.getStackTrace();
                len$ = arr$.length;

                for(i$ = 0; i$ < len$; ++i$) {
                    x = arr$[i$];
                    System.err.println(x);
                }
            }
        } finally {
            if (d != null) {
                d.close();
            }
        }
        return "";
    }

    @Override
    public String getTarget() {
        return JDAJavaTokenizer.SYNTAX_STYLE_JDA_JAVA;
    }
}
