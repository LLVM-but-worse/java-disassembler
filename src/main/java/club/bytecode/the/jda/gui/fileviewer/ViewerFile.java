package club.bytecode.the.jda.gui.fileviewer;

import club.bytecode.the.jda.FileContainer;

import java.util.Objects;

// really just a pair of FileContainer and String name
public class ViewerFile {
    public final FileContainer container;
    public final String name;

    public ViewerFile(FileContainer container, String name) {
        this.container = container;
        this.name = name;
    }
    
    @Override
    public String toString() {
        return container + "$" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ViewerFile that = (ViewerFile) o;
        return Objects.equals(container, that.container) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container, name);
    }
}
