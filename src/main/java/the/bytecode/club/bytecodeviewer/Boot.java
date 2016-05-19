package the.bytecode.club.bytecodeviewer;

import the.bytecode.club.bytecodeviewer.api.ExceptionUI;

import javax.swing.*;

/**
 * @author Konloch
 * @author Bibl (don't ban me pls)
 */
public class Boot
{
    private static InitialBootScreen screen;

    static
    {
        try
        {
            screen = new InitialBootScreen();
        }
        catch (Exception e)
        {
            new ExceptionUI(e);
        }
    }

    public static void boot() throws Exception
    {
        screen.getProgressBar().setMaximum(BootSequence.values().length);
        setState(BootSequence.BOOTING);
        SwingUtilities.invokeLater(() -> screen.setVisible(false));
    }

    public static void setState(BootSequence s)
    {
        screen.setTitle("Initialzing Java DisAssembler - " + s.getMessage());
        screen.getProgressBar().setValue(s.ordinal());
        System.out.println(s.getMessage());
    }

    enum BootSequence
    {
        BOOTING("Booting");

        private String message;

        BootSequence(String message)
        {
            this.message = message;
        }

        public String getMessage()
        {
            return this.message;
        }
    }
}
