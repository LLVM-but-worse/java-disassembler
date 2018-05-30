import club.bytecode.the.jda.decompilers.bytecode.*;
import org.mapleir.ir.cfg.ControlFlowGraph;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class IRDecompiler extends BytecodeDecompiler {
    @Override
   	protected MethodNodeDecompiler getMethodNodeDecompiler(PrefixedStringBuilder sb, ClassNode cn, Iterator<MethodNode> it) {
   		return new IRMethodDecompiler(this, sb, it.next(), cn);
   	}

   	@Override
   	public void decompileToZip(String zipName) {
   	}

   	@Override
   	public String getName() {
   		return "MapleIR";
   	}
   }

   class IRMethodDecompiler extends MethodNodeDecompiler {
   	public IRMethodDecompiler(BytecodeDecompiler parent, PrefixedStringBuilder sb, MethodNode mn, ClassNode cn) {
   		super(parent, sb, mn, cn);
   	}

   	@Override
   	protected InstructionPrinter getInstructionPrinter(MethodNode m, TypeAndName[] args) {
   		return new IRInstructionPrinter(this, m, args);
    	}
   }

   class IRInstructionPrinter extends InstructionPrinter {
   	public IRInstructionPrinter(MethodNodeDecompiler parent, MethodNode m, TypeAndName[] args) {
   		super(parent, m, args);
   	}

   	@Override
   	public ArrayList<String> createPrint() {
   		ControlFlowGraph cfg = MaplePlugin.cfgs.get(mNode);
   		String result = cfg.toString();
   		return new ArrayList<>(Arrays.asList(result.split("\n")));
   	}
   }
