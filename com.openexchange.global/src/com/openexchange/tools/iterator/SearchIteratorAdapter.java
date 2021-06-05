/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.tools.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link SearchIteratorAdapter} - An implementation of {@link SearchIterator} backed by a common instance of {@link Iterator} to which
 * calls are delegated.
 * <p>
 * Moreover this class provides several convenience implementations of {@link SearchIterator} accessible via {@link #emptyIterator()},
 * {@link #createArrayIterator(Object)} and {@link #toIterable(SearchIterator)}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SearchIteratorAdapter<T> implements SearchIterator<T> {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SearchIteratorAdapter.class);

    private static final class EmptySearchIterator<T> implements SearchIterator<T> {

        /**
         * Initializes a new {@link EmptySearchIterator}.
         */
        protected EmptySearchIterator() {
            super();
        }

        @Override
        public boolean hasNext() throws OXException {
            return false;
        }

        @Override
        public T next() throws OXException {
            return null;
        }

        @Override
        public void close() {
            // empty must not be closed.
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void addWarning(final OXException warning) {
            throw new UnsupportedOperationException("Method is not implemented");
        }

        @Override
        public OXException[] getWarnings() {
            return null;
        }

        @Override
        public boolean hasWarnings() {
            return false;
        }
    }

    @SuppressWarnings("rawtypes")
    private static final SearchIterator EMPTY = new EmptySearchIterator<>();

    private final Iterator<T> delegate;

    private final int size;

    private final boolean b_size;

    private final List<OXException> warnings;

    /**
     * Initializes a new {@link SearchIteratorAdapter}.
     *
     * @param iter The iterator to delegate to
     */
    public SearchIteratorAdapter(final Iterator<T> iter) {
        super();
        delegate = iter;
        warnings = new ArrayList<OXException>(2);
        b_size = false;
        size = -1;
    }

    /**
     * Initializes a new {@link SearchIteratorAdapter}.
     *
     * @param iter The iterator to delegate to
     * @param size The number of elements contained by passed iterator
     */
    public SearchIteratorAdapter(final Iterator<T> iter, final int size) {
        super();
        delegate = iter;
        this.size = size;
        warnings = new ArrayList<OXException>(2);
        b_size = true;
    }

    @Override
    public boolean hasNext() throws OXException {
        return delegate.hasNext();
    }

    @Override
    public T next() throws OXException {
        return delegate.next();
    }

    @Override
    public void close() {
        // delegate does not provide a close method.
    }

    @Override
    public int size() {
        if (!b_size) {
            return -1;
        }
        return size;
    }

    public boolean hasSize() {
        return b_size;
    }

    @Override
    public void addWarning(final OXException warning) {
        warnings.add(warning);
    }

    @Override
    public OXException[] getWarnings() {
        return warnings.isEmpty() ? null : warnings.toArray(new OXException[warnings.size()]);
    }

    @Override
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Creates an empty {@link SearchIterator iterator} of specified type.
     *
     * @param <T> The iterator's type
     * @return An empty iterator
     */
    public static <T> SearchIterator<T> emptyIterator() {
        @SuppressWarnings("unchecked") SearchIterator<T> i = EMPTY;
        return i;
    }

    /**
     * Creates an {@link SearchIterator iterator} for given array.
     *
     * @param <T> The array's type
     * @param array The array to iterate
     * @return An {@link SearchIterator iterator} for given array
     */
    public static <T> SearchIterator<T> createArrayIterator(final T[] array) {
        if (null == array) {
            return emptyIterator();
        }
        return new ArrayIterator<T>(array);
    }

    /**
     * Turns specified {@link SearchIterator iterator} to an {@link Iterable}.
     *
     * @param <T> The iterator's type
     * @param iterator The iterator from which to create the {@link Iterable}
     * @return The {@link Iterable}
     */
    public static <T> Iterable<T> toIterable(final SearchIterator<T> iterator) {
        class SIIterator implements Iterator<T> {

            @Override
            public boolean hasNext() {
                try {
                    return iterator.hasNext();
                } catch (OXException e) {
                    LOG.error("", e);
                    return false;
                }
            }

            @Override
            public T next() {
                try {
                    return iterator.next();
                } catch (OXException e) {
                    LOG.error("", e);
                }
                return null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {
                return new SIIterator();
            }
        };
    }

    /**
     * Turns specified {@link SearchIterator iterator} to a {@link List}.
     *
     * @param <T> The iterator's type
     * @param iterator The iterator from which to create the list
     * @return The list created from iterator
     * @throws OXException If list cannot be created
     */
    public static <T> List<T> toList(final SearchIterator<T> iterator) throws OXException {
        final List<T> list = new ArrayList<T>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

}
