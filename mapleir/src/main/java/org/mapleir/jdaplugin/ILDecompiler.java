package org.mapleir.jdaplugin;

import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.api.JDANamespace;
import club.bytecode.the.jda.decompilers.JDADecompiler;
import org.mapleir.ir.algorithms.BoissinotDestructor;
import org.mapleir.ir.cfg.ControlFlowGraph;
import org.mapleir.ir.cfg.builder.ControlFlowGraphBuilder;
import org.mapleir.ir.printer.ClassPrinter;
import org.mapleir.ir.printer.FieldNodePrinter;
import org.mapleir.ir.printer.MethodNodePrinter;
import org.mapleir.propertyframework.api.IPropertyDictionary;
import org.mapleir.propertyframework.util.PropertyHelper;
import org.mapleir.stdlib.util.TabbedStringWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ILDecompiler extends JDADecompiler implements MapleComponent {
    @Override
    public String decompileClassNode(FileContainer container, ClassNode cn) {
        TabbedStringWriter sw = new TabbedStringWriter();
        sw.setTabString("  ");
        IPropertyDictionary settings = PropertyHelper.createDictionary();
        final FieldNodePrinter fieldPrinter = new FieldNodePrinter(sw, settings);
        final MethodNodePrinter methodPrinter = new MethodNodePrinter(sw, settings) {
            @Override
            protected ControlFlowGraph getCfg(MethodNode mn) {
                ControlFlowGraph cfg = ControlFlowGraphBuilder.build(mn);
                BoissinotDestructor.leaveSSA(cfg);
                cfg.getLocals().realloc(cfg);
                return cfg;
            }
        };
        ClassPrinter cp = new ClassPrinter(sw, settings, fieldPrinter, methodPrinter);
        cp.print(cn);
        return sw.toString();
    }

    @Override
    public String getName() {
        return "MapleIL";
    }

    @Override
    public JDANamespace getNamespace() {
        return MaplePlugin.getInstance().getNamespace();
    }
}
