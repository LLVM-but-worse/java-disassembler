package org.mapleir.jdaplugin;

import club.bytecode.the.jda.FileContainer;
import club.bytecode.the.jda.api.JDAPlugin;
import club.bytecode.the.jda.decompilers.Decompilers;
import club.bytecode.the.jda.decompilers.JDADecompiler;
import org.mapleir.DefaultInvocationResolver;
import org.mapleir.app.client.SimpleApplicationContext;
import org.mapleir.app.service.ApplicationClassSource;
import org.mapleir.context.AnalysisContext;
import org.mapleir.context.BasicAnalysisContext;
import org.mapleir.context.IRCache;
import org.mapleir.ir.cfg.builder.ControlFlowGraphBuilder;
import org.mapleir.jdaplugin.gui.AboutDialog;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

public class MaplePlugin implements JDAPlugin {
	public static final Map<FileContainer, AnalysisContext> cxts = new HashMap<>();
	public static final JDADecompiler MAPLEIR = new IRDecompiler();
	public static final JDADecompiler MAPLEIL = new ILDecompiler();
	
	public MaplePlugin() {
	}

	public static void main(String[] args) {
		throw new NotImplementedException();
	}

	@Override
	public String getName() {
		return "MapleIR";
	}

	@Override
	public void onLoad() {
		Decompilers.BY_NAME.put("MapleIR", MAPLEIR);
		Decompilers.BY_NAME.put("MapleIL", MAPLEIL);
		System.out.println("MapleIR plugin loaded");
	}
	
	@Override
	public void onUnload() {
		
	}

	@Override
	public void onGUILoad() {
	}

	@Override
	public void onExit() {
	}

	@Override
	public void onOpenFile(FileContainer fileContainer) {
		ApplicationClassSource app = new ApplicationClassSource(fileContainer.name, fileContainer.getClasses());
		AnalysisContext newCxt = new BasicAnalysisContext.BasicContextBuilder()
				.setApplication(app)
				.setInvocationResolver(new DefaultInvocationResolver(app))
				.setCache(new IRCache(ControlFlowGraphBuilder::build))
				.setApplicationContext(new SimpleApplicationContext(app))
				.build();
		// when we get around to it, do tracing, IPA stuff here.
		cxts.put(fileContainer, newCxt);
	}
	
	@Override
	public void onCloseFile(FileContainer fc) {
		cxts.remove(fc);
	}

	@Override
	public void onPluginButton() {
		new AboutDialog().show();		
	}
}
