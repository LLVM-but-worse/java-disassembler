package org.mapleir.jdaplugin.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AboutDialog extends JDialog {
    public AboutDialog() {
        super(new JFrame(), "MapleIR - About", true);
        rootPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        Box b = Box.createVerticalBox();
        b.add(Box.createGlue());
        b.add(new JLabel("Powered by MapleIR"));
        b.add(Box.createGlue());
        getContentPane().add(b, "Center");

        JPanel okPanel = new JPanel();
        JButton ok = new JButton("OK");
        okPanel.add(ok);
        getContentPane().add(okPanel, "South");

        ok.addActionListener(evt -> setVisible(false));

        setSize(250, 100);
    }
}
