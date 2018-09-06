package club.bytecode.the.jda.gui.fileviewer;

import club.bytecode.the.jda.FileContainer;

import java.util.Objects;

// really just a pair of FileContainer and String name
public class ViewerFile implements Comparable<ViewerFile> {
    public final FileContainer container;
    public final String name;
    private final String toStringCached;

    public ViewerFile(FileContainer container, String name) {
        this.container = container;
        this.name = name;
        toStringCached = container + "$" + name;
    }
    
    @Override
    public String toString() {
        return toStringCached;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ViewerFile that = (ViewerFile) o;
        boolean result = Objects.equals(container, that.container) &&
                Objects.equals(name, that.name);
        assert (result == (compareTo(that) == 0));
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(container, name);
    }

    @Override
    public int compareTo(ViewerFile other) {
        return toStringCached.compareTo(other.toStringCached);
    }
}
