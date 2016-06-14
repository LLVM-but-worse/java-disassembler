package the.bytecode.club.jda.api;

import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.jda.BytecodeViewer;

import java.util.ArrayList;

/**
 * A simple plugin class, it will run the plugin in a background thread.
 *
 * @author Konloch
 */

public abstract class Plugin extends Thread
{

    @Override
    public void run()
    {
        BytecodeViewer.viewer.setIcon(true);
        try
        {
            if (BytecodeViewer.getLoadedBytes().isEmpty())
            {
                BytecodeViewer.showMessage("First open a class, jar, or zip file.");
                return;
            }
            execute(BytecodeViewer.loadAllClasses());
        }
        catch (Exception e)
        {
            new ExceptionUI(e);
        }
        finally
        {
            finished = true;
            BytecodeViewer.viewer.setIcon(false);
        }
    }

    private boolean finished = false;

    /**
     * When the plugin is finally finished, this will return true
     *
     * @return true if the plugin is finished executing
     */
    public boolean isFinished()
    {
        return finished;
    }

    /**
     * If for some reason your plugin needs to keep the thread alive, yet will
     * still be considered finished (EZ-Injection), you can call this function
     * and it will set the finished boolean to true.
     */
    public void setFinished()
    {
        finished = true;
    }

    /**
     * Whenever the plugin is started, this method is called
     *
     * @param classNodeList all of the loaded classes for easy access.
     */
    public abstract void execute(ArrayList<ClassNode> classNodeList);

}
