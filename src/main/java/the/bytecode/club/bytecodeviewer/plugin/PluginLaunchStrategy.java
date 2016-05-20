package the.bytecode.club.bytecodeviewer.plugin;

import the.bytecode.club.bytecodeviewer.api.Plugin;

import java.io.File;

/**
 * @author Bibl (don't ban me pls)
 * @created 1 Jun 2015
 */
public interface PluginLaunchStrategy
{

    Plugin run(File file) throws Throwable;
}