import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.api.JDAPlugin;
import club.bytecode.the.jda.decompilers.Decompilers;
import club.bytecode.the.jda.decompilers.JDADecompiler;
import org.mapleir.DefaultInvocationResolver;
import org.mapleir.app.client.SimpleApplicationContext;
import org.mapleir.app.service.ApplicationClassSource;
import org.mapleir.app.service.InstalledRuntimeClassSource;
import org.mapleir.app.service.LibraryClassSource;
import org.mapleir.context.AnalysisContext;
import org.mapleir.context.BasicAnalysisContext;
import org.mapleir.context.IRCache;
import org.mapleir.deob.IPass;
import org.mapleir.deob.PassGroup;
import org.mapleir.deob.interproc.IRCallTracer;
import org.mapleir.deob.passes.ConstantExpressionReorderPass;
import org.mapleir.deob.util.RenamingHeuristic;
import org.mapleir.ir.algorithms.BoissinotDestructor;
import org.mapleir.ir.algorithms.ControlFlowGraphDumper;
import org.mapleir.ir.cfg.ControlFlowGraph;
import org.mapleir.ir.cfg.builder.ControlFlowGraphBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.byteengineer.commons.data.JarInfo;
import org.topdank.byteio.in.SingleJarDownloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MaplePlugin implements JDAPlugin {
    public static Map<MethodNode, ControlFlowGraph> cfgs;
    public static AnalysisContext cxt;
	public static JDADecompiler MAPLEIR = new IRDecompiler();
	public static JDADecompiler MAPLEIL = new ILDecompiler();

	public MaplePlugin() {
        Decompilers.BY_NAME.put("MapleIR", MAPLEIR);
        Decompilers.BY_NAME.put("MapleIL", MAPLEIL);
		System.out.println("MapleIR plugin loaded");
	}

	public static void main(String[] args) {
		new MaplePlugin();
	}

	@Override
	public int onGUILoad() {
		cfgs = new HashMap<>();
		return 0;
	}

	@Override
	public int onExit() {
		return 0;
	}

    private static LibraryClassSource rt(ApplicationClassSource app, File rtjar) throws IOException {
  		System.out.println("Loading rt.jar from " + rtjar.getAbsolutePath());
  		SingleJarDownloader<ClassNode> dl = new SingleJarDownloader<>(new JarInfo(rtjar));
  		dl.download();

  		return new LibraryClassSource(app, dl.getJarContents().getClassContents());
  	}

	@Override
	public int onAddFile(FileContainer fileContainer) {
	    //ManagementFactory.getRuntimeMXBean().getBootClassPath()
        File rtjar = new File("C:\\Program Files\\Java\\jdk1.8.0_131\\jre\\lib\\rt.jar");
        // Load input jar
        ApplicationClassSource app = new ApplicationClassSource(fileContainer.name, fileContainer.getClasses());
        //		app.addLibraries(new InstalledRuntimeClassSource(app));
        try {
            app.addLibraries(rt(app, rtjar), new InstalledRuntimeClassSource(app));
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        System.out.println("Initialising context.");

        cxt = new BasicAnalysisContext.BasicContextBuilder()
                .setApplication(app)
                .setInvocationResolver(new DefaultInvocationResolver(app))
                .setCache(new IRCache(ControlFlowGraphBuilder::build))
                .setApplicationContext(new SimpleApplicationContext(app))
                .build();

        System.out.println("Expanding callgraph and generating cfgs.");

        IRCallTracer tracer = new IRCallTracer(cxt);
        for(ClassNode cn : app.iterate())  {
            System.out.println(cn);
            for(MethodNode m : cn.methods) {
                System.out.println(m);
                try {
                    tracer.trace(m);
                }catch(Exception e){e.printStackTrace();}
            }
        }

        // for(ClassNode cn : app.iterate()) {
        //     TabbedStringWriter sw = new TabbedStringWriter();
        //     sw.setTabString("  ");
        //     IPropertyDictionary settings = PropertyHelper.createDictionary();
        //     //			settings.put(new BooleanProperty(ASMPrinter.PROP_ACCESS_FLAG_SAFE, true));
        //     ClassPrinter cp = new ClassPrinter(sw, settings,
        //             new FieldNodePrinter(sw, settings),
        //             new MethodNodePrinter(sw, settings) {
        //                 @Override
        //                 protected ControlFlowGraph getCfg(MethodNode mn) {
        //                     return cxt.getIRCache().getFor(mn);
        //                 }
        //
        //             });
        //     cp.print(cn);
        //     System.out.println(sw.toString());
        // }

        System.out.println("...generated " + cxt.getIRCache().size() + " cfgs.\nPreparing to transform.");

        // do passes
        PassGroup masterGroup = new PassGroup("MasterController");
        for(IPass p : getTransformationPasses()) {
            masterGroup.add(p);
        }
        run(cxt, masterGroup);

        // for(MethodNode m : cxt.getIRCache().getActiveMethods()) {
        // 	if(m.instructions.size() > 100 && m.instructions.size() < 500) {
        // 		System.out.println(cxt.getIRCache().get(m));
        // 	}
        // }

        System.out.println("Retranslating SSA IR to standard flavour.");
        for(Map.Entry<MethodNode, ControlFlowGraph> e : cxt.getIRCache().entrySet()) {
            try {
                MethodNode mn = e.getKey();
                ControlFlowGraph cfg = e.getValue();

                BoissinotDestructor.leaveSSA(cfg);
                cfg.getLocals().realloc(cfg);
                (new ControlFlowGraphDumper(cfg, mn)).dump();
                cfgs.put(mn, cfg);
            }catch(Exception e3){e3.printStackTrace();}
        }

        System.out.println("Rewriting jar.");
        // dumpJar(app, dl, masterGroup, "out/osb5.jar");

        System.out.println("Finished.");

        return 0;
	}

    private static void run(AnalysisContext cxt, PassGroup group) {
  		group.accept(cxt, null, new ArrayList<>());
  	}

  	private static IPass[] getTransformationPasses() {
  		RenamingHeuristic heuristic = RenamingHeuristic.RENAME_ALL;
  		return new IPass[] {
  //				new ConcreteStaticInvocationPass(),
  //				new ClassRenamerPass(heuristic),
  //				new MethodRenamerPass(heuristic),
  //				new FieldRenamerPass(),
  //				new CallgraphPruningPass(),

  				// new PassGroup("Interprocedural Optimisations")
  				// 	.add(new ConstantParameterPass())
  				// new LiftConstructorCallsPass(),
  //				 new DemoteRangesPass(),

  				new ConstantExpressionReorderPass(),
  				// new FieldRSADecryptionPass(),
  				// new ConstantParameterPass(),
  //				new ConstantExpressionEvaluatorPass(),
  // 				new DeadCodeEliminationPass()

  		};
  	}
}
