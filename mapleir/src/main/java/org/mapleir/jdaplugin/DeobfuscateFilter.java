package org.mapleir.jdaplugin;

import club.bytecode.the.jda.decompilers.filter.DecompileFilter;
import org.objectweb.asm.tree.ClassNode;

public class DeobfuscateFilter implements DecompileFilter, MapleComponent {
    @Override
    public void process(ClassNode cn) {
        
    }

    @Override
    public String getName() {
        return "Deobfuscator";
    }
}
