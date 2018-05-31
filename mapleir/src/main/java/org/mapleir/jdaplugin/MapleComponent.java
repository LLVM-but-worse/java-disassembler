package org.mapleir.jdaplugin;

import club.bytecode.the.jda.api.JDANamespace;
import club.bytecode.the.jda.api.JDANamespacedComponent;

// Is this bad design...?
public interface MapleComponent extends JDANamespacedComponent {
    @Override
    default JDANamespace getNamespace() {
        return MaplePlugin.getInstance().getNamespace();
    }
}
