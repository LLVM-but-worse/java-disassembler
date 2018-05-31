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
        for (MethodNode mn : cn.methods) {
            ControlFlowGraph cfg = ControlFlowGraphBuilder.build(mn);
            BoissinotDestructor.leaveSSA(cfg);
            cfg.getLocals().realloc(cfg);
            (new ControlFlowGraphDumper(cfg, mn)).dump();
        }
    }

    @Override
    public String getName() {
        return "Deobfuscator";
    }
}
