package cern.extjfx.chart.data;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import cern.extjfx.chart.data.LimitedFifoObservableList;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

public class LimitedFifoObservableListTest {

    private static final String TEST_ELEMENT_1 = "TEST_ELEMENT_1";
    private static final String TEST_ELEMENT_2 = "TEST_ELEMENT_2";
    private static final String TEST_ELEMENT_3 = "TEST_ELEMENT_3";
    private static final Collection<String> TEST_COLLECTION_1 = Arrays.asList(TEST_ELEMENT_1);
    private static final Collection<String> TEST_COLLECTION_2 = Arrays.asList(TEST_ELEMENT_1, TEST_ELEMENT_2);
    private static final Collection<String> TEST_COLLECTION_3 = Arrays.asList(TEST_ELEMENT_1, TEST_ELEMENT_2,
            TEST_ELEMENT_3);
    private static final String[] TEST_ARRAY_1 = new String[] { TEST_ELEMENT_1 };
    private static final String[] TEST_ARRAY_2 = new String[] { TEST_ELEMENT_1, TEST_ELEMENT_2 };

    private static <E> LimitedFifoObservableList<E> getListWithMaxSize(final int maxSize) {
        return new LimitedFifoObservableList<>(maxSize);
    }

    @Test
    public void testAddAll() {
        final List<String> list = getListWithMaxSize(3);
        list.addAll(TEST_COLLECTION_1);
        assertThat(list.size(), is(1));
        assertThat(list.get(0), is(TEST_ELEMENT_1));

        list.addAll(TEST_COLLECTION_2);
        assertThat(list.size(), is(3));
        assertThat(list.get(0), is(TEST_ELEMENT_1));
        assertThat(list.get(1), is(TEST_ELEMENT_1));
        assertThat(list.get(2), is(TEST_ELEMENT_2));

        list.addAll(TEST_COLLECTION_3);
        assertThat(list.size(), is(3));
        assertThat(list.get(0), is(TEST_ELEMENT_1));
        assertThat(list.get(1), is(TEST_ELEMENT_2));
        assertThat(list.get(2), is(TEST_ELEMENT_3));
    }

    @Test
    public void testAddAllMoreThanMax() {
        final List<String> list = getListWithMaxSize(1);
        list.addAll(TEST_COLLECTION_3);
        assertThat(list.size(), is(1));
        assertThat(list.get(0), is(TEST_ELEMENT_3));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAtIndex() {
        final List<String> list = getListWithMaxSize(2);
        list.addAll(0, TEST_COLLECTION_2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddAtIndex() {
        final List<String> list = getListWithMaxSize(2);
        list.add(0, TEST_ELEMENT_1);
    }

    @Test
    public void testAddAndGet() {
        final List<String> list = getListWithMaxSize(1);

        // add single element
        assertThat(list.size(), is(0));
        list.add(TEST_ELEMENT_1);
        assertThat(list.size(), is(1));
        assertThat(list.get(0), is(TEST_ELEMENT_1));

        // add more elements than max
        list.add(TEST_ELEMENT_2);
        assertThat(list.size(), is(1));
        assertThat(list.get(0), is(TEST_ELEMENT_2));
    }

    @Test
    public void testClear() {
        final List<String> list = getListWithMaxSize(3);
        list.addAll(TEST_COLLECTION_3);
        assertThat(list.size(), is(3));
        list.clear();
        assertTrue(list.isEmpty());
    }

    @Test
    public void testContains() {
        final List<String> list = getListWithMaxSize(3);
        list.addAll(TEST_COLLECTION_3);
        assertTrue(list.contains(TEST_ELEMENT_1));
        assertTrue(list.contains(TEST_ELEMENT_2));
        assertTrue(list.contains(TEST_ELEMENT_3));
        list.add(TEST_ELEMENT_2);
        assertFalse(list.contains(TEST_ELEMENT_1));
    }

    @Test
    public void testContainsAll() {
        final List<String> list = getListWithMaxSize(3);
        list.addAll(TEST_COLLECTION_3);
        assertTrue(list.containsAll(TEST_COLLECTION_3));
    }

    @Test
    public void testIndexOf() {
        final List<String> list = getListWithMaxSize(3);
        list.add(TEST_ELEMENT_1);
        list.add(TEST_ELEMENT_3);
        list.add(TEST_ELEMENT_1);
        assertThat(list.indexOf(TEST_ELEMENT_1), is(0));
        assertThat(list.indexOf(TEST_ELEMENT_3), is(1));
        list.add(TEST_ELEMENT_3);
        assertThat(list.indexOf(TEST_ELEMENT_1), is(1));
        assertThat(list.indexOf(TEST_ELEMENT_3), is(0));
        list.add(TEST_ELEMENT_2);
        assertThat(list.indexOf(TEST_ELEMENT_1), is(0));
        assertThat(list.indexOf(TEST_ELEMENT_3), is(1));
        assertThat(list.indexOf(TEST_ELEMENT_2), is(2));
    }

    @Test
    public void testIsEmpty() {
        final List<Object> list = getListWithMaxSize(1);
        assertTrue(list.isEmpty());
        list.add(new Object());
        assertThat(list.size(), is(1));
        list.clear();
        assertTrue(list.isEmpty());
    }

    @Test
    public void testLastIndexOf() {
        final List<String> list = getListWithMaxSize(3);
        list.add(TEST_ELEMENT_1);
        list.add(TEST_ELEMENT_3);
        list.add(TEST_ELEMENT_1);
        assertThat(list.lastIndexOf(TEST_ELEMENT_1), is(2));
        assertThat(list.lastIndexOf(TEST_ELEMENT_3), is(1));
        list.add(TEST_ELEMENT_3);
        assertThat(list.lastIndexOf(TEST_ELEMENT_1), is(1));
        assertThat(list.lastIndexOf(TEST_ELEMENT_3), is(2));
        list.add(TEST_ELEMENT_2);
        assertThat(list.lastIndexOf(TEST_ELEMENT_1), is(0));
        assertThat(list.lastIndexOf(TEST_ELEMENT_3), is(1));
        assertThat(list.lastIndexOf(TEST_ELEMENT_2), is(2));
    }

    @Test
    public void testListIterator() {
        final List<String> list = getListWithMaxSize(3);
        list.addAll(TEST_COLLECTION_3);

        Iterator<String> it = list.iterator();
        assertTrue(it.hasNext());
        assertThat(it.next(), is(TEST_ELEMENT_1));
        assertTrue(it.hasNext());
        assertThat(it.next(), is(TEST_ELEMENT_2));
        assertTrue(it.hasNext());
        assertThat(it.next(), is(TEST_ELEMENT_3));
        assertFalse(it.hasNext());

        list.add(TEST_ELEMENT_1);
        it = list.iterator();
        assertTrue(it.hasNext());
        assertThat(it.next(), is(TEST_ELEMENT_2));
        assertTrue(it.hasNext());
        assertThat(it.next(), is(TEST_ELEMENT_3));
        assertTrue(it.hasNext());
        assertThat(it.next(), is(TEST_ELEMENT_1));
        assertFalse(it.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAll() {
        final List<String> list = getListWithMaxSize(3);
        list.addAll(TEST_COLLECTION_3);

        list.removeAll(TEST_COLLECTION_2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        final List<String> list = getListWithMaxSize(3);
        list.addAll(TEST_COLLECTION_3);
        list.remove(TEST_ELEMENT_1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAll() {
        final List<String> list = getListWithMaxSize(3);
        list.addAll(TEST_COLLECTION_3);

        list.retainAll(TEST_COLLECTION_1);

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetAtIndex() {
        final List<String> list = getListWithMaxSize(3);
        list.add(TEST_ELEMENT_1);
        list.set(1, TEST_ELEMENT_1);
    }

    @Test
    public void testSize() {
        final List<Object> list = getListWithMaxSize(1);
        assertThat(list.size(), is(0));
        list.add(new Object());
        assertThat(list.size(), is(1));
        list.add(new Object());
        assertThat(list.size(), is(1));
    }

    @Test
    public void testSubList() {
        final List<String> list = getListWithMaxSize(3);
        list.addAll(TEST_COLLECTION_3);
        list.add(TEST_ELEMENT_1);

        assertThat(list.subList(1, 3), is(Arrays.asList(TEST_ELEMENT_3, TEST_ELEMENT_1)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListNegativeIndex() {
        final List<String> list = getListWithMaxSize(3);
        list.addAll(TEST_COLLECTION_3);

        assertThat(list.subList(-1, 3), is(Arrays.asList(TEST_ELEMENT_3, TEST_ELEMENT_1)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListIndexGreaterSize() {
        final List<String> list = getListWithMaxSize(4);
        list.addAll(TEST_COLLECTION_3);

        assertThat(list.subList(1, 4), is(Arrays.asList(TEST_ELEMENT_3, TEST_ELEMENT_1)));
    }

    @Test
    public void testToArray() {
        final List<String> list = getListWithMaxSize(2);
        list.add(TEST_ELEMENT_1);
        assertThat(list.toArray(new String[list.size()]), is(TEST_ARRAY_1));

        list.add(TEST_ELEMENT_2);
        assertThat(list.toArray(new String[list.size()]), is(TEST_ARRAY_2));
    }

    @Test
    public void testChangeEventForAdd() {
        final ObservableList<Integer> list = getListWithMaxSize(2);
        list.addAll(Arrays.asList(1, 2));

        final TestListChangeListener changeListener = new TestListChangeListener();
        list.addListener(changeListener);
        list.add(3);

        assertThat(changeListener.changes.size(), is(1));
        final Change<? extends Integer> change = changeListener.changes.get(0);
        assertTrue(change.next());
        assertThat(change.getRemoved(), is(Arrays.asList(1)));
        assertTrue(change.next());
        assertThat(change.getAddedSubList(), is(Arrays.asList(3)));
        assertFalse(change.next());
    }

    @Test
    public void testChangeEventForAddAll() {
        final ObservableList<Integer> list = getListWithMaxSize(3);
        list.addAll(Arrays.asList(1, 2, 3));

        final TestListChangeListener changeListener = new TestListChangeListener();
        list.addListener(changeListener);
        list.addAll(Arrays.asList(4, 5));

        assertThat(changeListener.changes.size(), is(1));
        Change<? extends Integer> change = changeListener.changes.get(0);
        assertTrue(change.next());
        assertThat(change.getRemoved(), is(Arrays.asList(1, 2)));
        assertTrue(change.next());
        assertThat(change.getAddedSubList(), is(Arrays.asList(4, 5)));
        assertFalse(change.next());
    }

    @Test
    public void testChangeEventForAddAllEmpty() {
        final ObservableList<Integer> list = getListWithMaxSize(3);

        final TestListChangeListener changeListener = new TestListChangeListener();
        list.addListener(changeListener);
        list.addAll(Arrays.asList(1, 2, 3));

        assertThat(changeListener.changes.size(), is(1));
        Change<? extends Integer> change = changeListener.changes.get(0);
        assertTrue(change.next());
        assertThat(change.getRemoved(), is(Collections.emptyList()));
        assertThat(change.getAddedSubList(), is(Arrays.asList(1, 2, 3)));
        assertFalse(change.next());
    }

    @Test
    public void testChangeEventForAddAllFill() {
        final ObservableList<Integer> list = getListWithMaxSize(3);
        list.add(0);

        final TestListChangeListener changeListener = new TestListChangeListener();
        list.addListener(changeListener);
        list.addAll(Arrays.asList(1, 2, 3));

        assertThat(changeListener.changes.size(), is(1));
        Change<? extends Integer> change = changeListener.changes.get(0);
        assertTrue(change.next());
        assertThat(change.getRemoved(), is(Arrays.asList(0)));
        assertThat(change.getAddedSubList(), is(Arrays.asList(1, 2, 3)));
        assertFalse(change.next());
    }

    @Test
    public void testChangeEventForAddAllSimple() {
        final ObservableList<Integer> list = getListWithMaxSize(3);
        list.add(0);

        final TestListChangeListener changeListener = new TestListChangeListener();
        list.addListener(changeListener);
        list.addAll(Arrays.asList(1, 2));

        assertThat(changeListener.changes.size(), is(1));
        Change<? extends Integer> change = changeListener.changes.get(0);
        assertTrue(change.next());
        assertThat(change.getRemoved(), is(Collections.emptyList()));
        assertThat(change.getAddedSubList(), is(Arrays.asList(1, 2)));
        assertFalse(change.next());
    }

    @Test
    public void testPerformance() {
        final int size = 100_000;
        final List<Integer> list = getListWithMaxSize(size);
        for (int i = 0; i < size + 1; i++) {
            list.add(i);
        }

        final long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < size; j++) {
                list.get(j);
            }
        }
        final long stop = System.currentTimeMillis();
        System.out.println("Time: " + (stop - start));
        assertTrue("Calling list.get(..) " + size + " values supposed to take less than 250ms but took ["
                + (stop - start) + "]",
                stop - start < 250);
    }

    private static class TestListChangeListener implements ListChangeListener<Integer> {
        private final List<Change<? extends Integer>> changes = new ArrayList<>();

        @Override
        public void onChanged(final Change<? extends Integer> change) {
            changes.add(change);
        }
    }

}
