package the.bytecode.club.jda.settings;

import java.awt.*;

public interface IPersistentWindow
{
    int getState();
    void restoreState(int state);

    Point getPersistentPosition();
    void restorePosition(Point pos);

    Dimension getPersistentSize();
    void restoreSize(Dimension size);

    boolean isNormalState(); // not maximized/minimized/etc

    String getWindowId();
}
