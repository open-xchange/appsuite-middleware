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

import com.openexchange.exception.OXException;


/**
 * {@link FilteringSearchIterator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class FilteringSearchIterator<T> implements SearchIterator<T>{

    private T next = null;
    private final SearchIterator<T> delegate;

    public FilteringSearchIterator(final SearchIterator<T> delegate) throws OXException {
        this.delegate = delegate;
        initNext();
    }

    /**
     * Test whether something should be included in the result or not
     * @param thing
     * @return
     * @throws AbstractOXException
     */
    public abstract boolean accept(T thing) throws OXException;


    @Override
    public void addWarning(final OXException warning) {
        delegate.addWarning(warning);
    }

    @Override
    public void close() {
        SearchIterators.close(delegate);
    }

    @Override
    public OXException[] getWarnings() {
        return delegate.getWarnings();
    }

    @Override
    public boolean hasNext() throws OXException {
        return next != null;
    }

    @Override
    public boolean hasWarnings() {
        return delegate.hasWarnings();
    }

    @Override
    public T next() throws OXException {
        final T current = next;
        initNext();
        return current;
    }

    @Override
    public int size() {
        return -1;
    }

    protected void initNext() throws OXException {
        while (delegate.hasNext()) {
            next = delegate.next();
            if (accept(next)) {
                return;
            }
        }
        next = null;
    }
}
