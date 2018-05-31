package org.mapleir.jdaplugin.gui;

import javax.swing.*;

public class AboutDialog extends JDialog {
    public AboutDialog() {
        super(new JFrame(), "MapleIR - About", true);
        Box b = Box.createVerticalBox();
        b.add(Box.createGlue());
        b.add(new JLabel("Powered by MapleIR"));
        b.add(Box.createGlue());
        getContentPane().add(b, "Center");

        JPanel okPanel = new JPanel();
        JButton ok = new JButton("Ok");
        okPanel.add(ok);
        getContentPane().add(okPanel, "South");

        ok.addActionListener(evt -> setVisible(false));

        setSize(250, 100);
    }
}
