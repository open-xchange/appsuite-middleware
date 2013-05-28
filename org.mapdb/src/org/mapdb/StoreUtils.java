
package org.mapdb;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Store utilities.
 */
public final class StoreUtils {

    /**
     * Initializes a new {@link StoreUtils}.
     */
    private StoreUtils() {
        super();
    }

    /**
     * Returns an iterator that has no elements. More precisely,
     * <ul compact>
     * <li>{@link Iterator#hasNext hasNext} always returns {@code false}.
     * <li>{@link Iterator#next next} always throws {@link NoSuchElementException}.
     * <li>{@link Iterator#remove remove} always throws {@link IllegalStateException}.
     * </ul>
     * <p>
     * Implementations of this method are permitted, but not required, to return the same object from multiple invocations.
     *
     * @return an empty iterator
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> emptyIterator() {

        return (Iterator<T>) EmptyIterator.EMPTY_ITERATOR;
    }

    private static class EmptyIterator<E> implements Iterator<E> {

        static final EmptyIterator<Object> EMPTY_ITERATOR = new EmptyIterator<Object>();

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public E next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new IllegalStateException();
        }
    }

}
