package org.mapleir.jdaplugin;

import club.bytecode.the.jda.JDA;

/**
 * Quick dependency injector to hack the circular reference so I can debug this plugin in my IDE
 */
public class MapleInjectedJDA {
    public static void main(String[] args) {
        JDA.injectedPlugin = MaplePlugin::new;
        JDA.main(args);
    }
}
