package org.mapleir.jdaplugin;

import club.bytecode.the.jda.decompilers.filter.DecompileFilter;
import org.mapleir.ir.algorithms.BoissinotDestructor;
import org.mapleir.ir.algorithms.ControlFlowGraphDumper;
import org.mapleir.ir.cfg.ControlFlowGraph;
import org.mapleir.ir.cfg.builder.ControlFlowGraphBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class DeobfuscateFilter implements DecompileFilter, MapleComponent {
    @Override
    public void process(ClassNode cn) {
        if (cn == null)
            return;
        for (MethodNode mn : cn.methods) {
            ControlFlowGraph cfg = ControlFlowGraphBuilder.build(mn);
            BoissinotDestructor.leaveSSA(cfg);
            cfg.getLocals().realloc(cfg);
            (new ControlFlowGraphDumper(cfg, mn)).dump();
            System.out.println("Processed " + mn);
        }
    }

    @Override
    public String getName() {
        return "Deobfuscator";
    }
}
