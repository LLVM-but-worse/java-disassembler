package the.bytecode.club.jda.gui.dialogs;

import the.bytecode.club.jda.JDA;
import the.bytecode.club.jda.api.ExceptionUI;
import the.bytecode.club.jda.settings.Setting;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class FileChooser {
    private Setting target;
    private String message;

    public FileChooser(Setting target, String message) {
        this.target = target;
        this.message = message;
    }

    public void run() {
        File currentFile = new File(
                target.get() == null || target.get().isEmpty() ? System.getProperty("user.home") : target.get());
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return true;
            }

            @Override
            public String getDescription() {
                return message;
            }
        });
        if (currentFile.isDirectory()) {
            fc.setCurrentDirectory(currentFile);
        } else {
            fc.setSelectedFile(currentFile);
        }
        fc.setFileHidingEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);

        int returnVal = fc.showOpenDialog(JDA.viewer);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                target.set(fc.getSelectedFile().getAbsolutePath());
            } catch (Exception e1) {
                new ExceptionUI(e1);
            }
        }
    }
}
