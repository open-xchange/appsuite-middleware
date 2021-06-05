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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link SearchIteratorDelegator} - An implementation of {@link SearchIterator} backed by a common instance of {@link Iterator} to which
 * calls are delegated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SearchIteratorDelegator<T> implements SearchIterator<T> {

    private final Collection<T> collection;
    private final Iterator<T> delegate;
    private final int size;
    private final List<OXException> warnings;

    /**
     * Initializes a new {@link SearchIteratorDelegator}
     *
     * @param iter The delegate iterator
     */
    public SearchIteratorDelegator(final Iterator<T> iter) {
        super();
        delegate = iter;
        warnings = new ArrayList<OXException>(2);
        size = -1;
        collection = null;
    }

    /**
     * Initializes a new {@link SearchIteratorDelegator}
     *
     * @param iter The delegate iterator
     * @param size The delegate iterator's size
     * @throws IllegalArgumentException If specified size is equal to or less than zero
     */
    public SearchIteratorDelegator(final Iterator<T> iter, final int size) {
        super();
        if (size < 0) {
            throw new IllegalArgumentException("invalid size: " + size);
        }
        delegate = iter;
        this.size = size;
        warnings = new ArrayList<OXException>(2);
        collection = null;
    }

    /**
     * Initializes a new {@link SearchIteratorDelegator}.
     *
     * @param collection The collection to iterate
     */
    public SearchIteratorDelegator(final Collection<T> collection) {
        super();
        delegate = collection.iterator();
        this.size = collection.size();
        warnings = new ArrayList<OXException>(2);
        this.collection = collection;
    }

    /**
     * Gets the collection
     *
     * @return The collection
     */
    public Collection<T> getCollection() {
        return collection;
    }

    /**
     * Spawns a new {@link SearchIterator} from this instance (if possible).
     *
     * @return The new {@code SearchIterator} instance or <code>null</code>
     */
    public SearchIteratorDelegator<T> newSearchIterator() {
        return null == collection ? null : new SearchIteratorDelegator<>(collection);
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
        // Nothing to do
    }

    @Override
    public int size() {
        if (size < 0) {
            return -1;
        }
        return size;
    }

    public boolean hasSize() {
        return (size >= 0);
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
}
