package club.bytecode.the.jda.gui.components;

import javax.swing.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class SortedUniqueListModel<T> extends AbstractListModel implements Iterable<T> {
    SortedSet<T> model;
    private boolean deferringUpdates;

    public SortedUniqueListModel() {
        model = new TreeSet<>();
    }

    public int getSize() {
        return model.size();
    }

    // performance hack
    public void deferUpdates() {
        deferringUpdates = true;
    }

    public void commitUpdates() {
        deferringUpdates = false;
        fireUpdate();
    }

    public T getElementAt(int index) {
        Iterator<T> it = model.iterator();
        for (int i = 0; i < index; i++)
            if (!it.hasNext()) return null;
            else it.next();
        if (!it.hasNext()) return null;
        return it.next();
    }

    public void add(T element) {
        if (model.add(element)) {
            fireUpdate();
        }
    }

   public void fireUpdate() {
        if (deferringUpdates)
            return;
        fireContentsChanged(this, 0, getSize());
    }

    public void addAll(Collection<T> elements) {
        model.addAll(elements);
        fireUpdate();
    }

    public void clear() {
        model.clear();
        fireUpdate();
    }

    public boolean contains(T element) {
        return model.contains(element);
    }

    public T firstElement() {
        return model.first();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Iterator<T> delegate = model.iterator();

            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public T next() {
                return delegate.next();
            }

            @Override
            public void remove() {
                delegate.remove();
                fireUpdate();
            }
        };
    }

    public T lastElement() {
        return model.last();
    }

    public boolean removeElement(T element) {
        boolean removed = model.remove(element);
        if (removed) {
            fireUpdate();
        }
        return removed;
    }
}

