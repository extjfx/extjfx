package cern.extjfx.chart.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import javafx.collections.FXCollections;
import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;

/**
 * This class provides a limited list with circular behavior. While the list has less elements than the given maximum
 * size, it behaves like a normal {@link FXCollections#observableArrayList() observable ArrayList}.
 * <p>
 * Once the maximum size is reached, adding any new element will also remove the first element, like a FIFO queue would
 * do.
 * <p>
 * The list is meant to be used as a container of {@link Data} when one needs charts presenting live time trends.
 *
 * @author Andreas Schaller
 * @param <T> Type of the element kept in the list
 */
public class LimitedFifoObservableList<T> extends ModifiableObservableListBase<T> {

    private final List<T> list;
    private final int maxSize;

    /**
     * This index always points to the oldest entry in the list. In a normal list, this would be always 0. Since we have
     * a circular list, to avoid reordering all elements on each add once the list is full (size() == maxSize), we use
     * our own pointer to the first element and replace the older indexes by the new data.
     */
    private int zeroElementPointer = 0;

    /**
     * Creates a new {@link ObservableList} with the maximum element count of <code>maxSize</code>.
     *
     * @param maxSize the maximum number of elements that can be stored in this list (has to be greater than 0)
     * @throws IllegalArgumentException if <code>maxSize</code> is less or equal to 0
     */
    public LimitedFifoObservableList(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be greater than 0!");
        }
        this.maxSize = maxSize;
        list = new ArrayList<>(maxSize);
    }

    /**
     * Returns the maximum size of this list.
     *
     * @return the maximum number of elements this list can hold
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than min({@link #size()},
     *             {@link #getMaxSize()})
     */
    @Override
    public T get(int index) {
        indexCheck(index);
        return list.get(getRelativeIndex(index));
    }

    @Override
    public int size() {
        return list.size();
    }

    /**
     * Appends the specified element to the end of this list (optional operation).
     * <p>
     * Note, if {@link #size()} == {@link #getMaxSize()}, the list won't get longer when adding an element. Instead the
     * first element will be removed.
     * <p>
     * Example:
     *
     * <pre>
     *    list with <code>maxSize</code> = 3 containing [a,b,c]
     *    list.add(d) will result in [b,c,d]
     * </pre>
     */
    @Override
    public boolean add(T element) {
        doAdd(size(), element);
        modCount++;
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note, that the given collection will be reduced to the last {@link #getMaxSize() maxSize} elements if needed.
     * If so, the reduced elements will not be added nor removed, so when using a change listener, these elements won't
     * appear in the listeners changes.
     * <p>
     * Note, if {@link #size()} == {@link #getMaxSize()}, this list won't get longer when adding an element. Instead the
     * first element will be removed.
     */
    @Override
    public boolean addAll(Collection<? extends T> elements) {
        Collection<? extends T> toBeAdded = elements;

        if (toBeAdded.size() > maxSize) {
            // If this isn't done, we can not add/remove all elements in one change without getting IndexOutOfBounds
            // exceptions. Otherwise if we split the changes into multiple ones, they would produce identical changes
            // showing the last changed elements which is more confusing.
            toBeAdded = new ArrayList<>(toBeAdded).subList(elements.size() - maxSize, elements.size());
        }

        boolean modified = false;

        int deletions = Math.max(0, toBeAdded.size() - (maxSize - size()));
        int addFrom = size() - deletions;

        beginChange();
        nextRemove(0, list.subList(0, deletions));

        for (T element : toBeAdded) {
            if (size() < maxSize) {
                int biasedIndex = getRelativeIndex(size());
                list.add(biasedIndex, element);
                if (biasedIndex <= zeroElementPointer) {
                    incrementZeroElementPointer();
                }
            } else {
                list.set(zeroElementPointer, element);
                incrementZeroElementPointer();
            }
            modCount++;
            modified = true;
        }

        nextAdd(addFrom, size());
        endChange();
        return modified;
    }

    @Override
    protected void doAdd(int index, T element) {
        indexCheckForAdd(index);
        if (size() < maxSize) {
            int biasedIndex = getRelativeIndex(index);
            list.add(biasedIndex, element);
            if (biasedIndex <= zeroElementPointer) {
                incrementZeroElementPointer();
            }
            beginChange();
            nextAdd(index, index + 1);
            endChange();
        } else {
            T old = list.set(zeroElementPointer, element);
            incrementZeroElementPointer();
            beginChange();
            nextRemove(0, old);
            nextAdd(size() - 1, size());
            endChange();
        }
    }

    @Override
    public void clear() {
        beginChange();
        nextRemove(0, list);
        list.clear();
        zeroElementPointer = 0;
        endChange();
    }

    // Unsupported Operations

    /**
     * This method is not supported in a FIFO list.
     *
     * @throws UnsupportedOperationException This is not supported in a FIFO list!
     */
    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException("This is not supported of a FIFO list!");
    }

    /**
     * This method is not supported in a FIFO list.
     *
     * @throws UnsupportedOperationException This is not supported in a FIFO list!
     */
    @Override
    public boolean addAll(int index, Collection<? extends T> elements) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported in a FIFO list.
     *
     * @throws UnsupportedOperationException This is not supported in a FIFO list!
     */
    @Override
    public boolean remove(Object element) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported in a FIFO list.
     *
     * @throws UnsupportedOperationException This is not supported in a FIFO list!
     */
    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported in a FIFO list.
     *
     * @throws UnsupportedOperationException This is not supported in a FIFO list!
     */
    @Override
    public void remove(int from, int to) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported in a FIFO list.
     *
     * @throws UnsupportedOperationException This is not supported in a FIFO list!
     */
    @Override
    public boolean removeAll(Collection<?> elements) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported in a FIFO list.
     *
     * @throws UnsupportedOperationException This is not supported in a FIFO list!
     */
    @Override
    public boolean removeAll(@SuppressWarnings("unchecked") T... elements) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported in a FIFO list.
     *
     * @throws UnsupportedOperationException This is not supported in a FIFO list!
     */
    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported in a FIFO list.
     *
     * @throws UnsupportedOperationException This is not supported in a FIFO list!
     */
    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported in a FIFO list.
     *
     * @throws UnsupportedOperationException This is not supported in a FIFO list!
     */
    @Override
    public boolean retainAll(Collection<?> elements) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported in a FIFO list.
     *
     * @throws UnsupportedOperationException This is not supported in a FIFO list!
     */
    @Override
    public boolean retainAll(@SuppressWarnings("unchecked") T... elements) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported in a FIFO list.
     *
     * @throws UnsupportedOperationException Add at index is not supported in a FIFO list!
     */
    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported in a FIFO list.
     *
     * @throws UnsupportedOperationException This is not supported in a FIFO list!
     */
    @Override
    public boolean setAll(Collection<? extends T> col) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported in a FIFO list.
     *
     * @throws UnsupportedOperationException This is not supported in a FIFO list!
     */
    @Override
    public boolean setAll(@SuppressWarnings("unchecked") T... elements) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported in a FIFO list.
     *
     * @throws UnsupportedOperationException This is not supported in a FIFO list!
     */
    @Override
    public void sort(Comparator<? super T> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected T doSet(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected T doRemove(int index) {
        throw new UnsupportedOperationException();
    }

    // Private Methods

    private int getRelativeIndex(int index) {
        if (zeroElementPointer == 0) {
            return index;
        }
        return getZeroElementPointerAddBy(index);
    }

    private void incrementZeroElementPointer() {
        zeroElementPointer = getZeroElementPointerAddBy(1);
    }

    private int getZeroElementPointerAddBy(int index) {
        return (zeroElementPointer + index) % size();
    }

    private void indexCheckForAdd(int index) {
        if (index > size() || index < 0) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    private void indexCheck(int index) {
        if (index >= size() || index < 0) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size() + ", maxSize: " + maxSize;
    }
}