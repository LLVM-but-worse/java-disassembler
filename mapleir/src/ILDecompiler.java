import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.decompilers.JDADecompiler;
import org.mapleir.ir.cfg.ControlFlowGraph;
import org.mapleir.ir.printer.ClassPrinter;
import org.mapleir.ir.printer.FieldNodePrinter;
import org.mapleir.ir.printer.MethodNodePrinter;
import org.mapleir.propertyframework.api.IPropertyDictionary;
import org.mapleir.propertyframework.util.PropertyHelper;
import org.mapleir.stdlib.util.TabbedStringWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ILDecompiler extends JDADecompiler {
    @Override
    public String decompileClassNode(FileContainer container, ClassNode cn) {
        TabbedStringWriter sw = new TabbedStringWriter();
        sw.setTabString("  ");
        IPropertyDictionary settings = PropertyHelper.createDictionary();
        //			settings.put(new BooleanProperty(ASMPrinter.PROP_ACCESS_FLAG_SAFE, true));
        ClassPrinter cp = new ClassPrinter(sw, settings,
                new FieldNodePrinter(sw, settings),
                new MethodNodePrinter(sw, settings) {
                    @Override
                    protected ControlFlowGraph getCfg(MethodNode mn) {
                        return MaplePlugin.cxt.getIRCache().getFor(mn);
                    }

                });
        cp.print(cn);
        return sw.toString();
    }

    @Override
    public void decompileToZip(String zipName) {

    }

    @Override
    public String getName() {
        return "MapleIL";
    }
}
