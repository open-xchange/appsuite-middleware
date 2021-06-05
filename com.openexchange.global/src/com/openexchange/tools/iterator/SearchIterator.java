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

import java.io.Closeable;
import com.openexchange.exception.OXException;

/**
 * {@link SearchIterator} - An extended iterator over a collection or a (releasable) resource.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SearchIterator<T> extends Closeable {

    /**
     * Returns <code>true</code> if the iteration has more elements. (In other words, returns <code>true</code> if {@link #next()} would
     * return an element.)
     *
     * @return <code>true</code> if the iterator has more elements; otherwise <code>false</code>
     * @throws OXException If check for further elements fails
     */
    boolean hasNext() throws OXException;

    /**
     * Returns the next element in the iteration. Calling this method repeatedly until the {@link #hasNext()} method returns
     * <code>false</code> will return each element in the underlying collection exactly once.
     *
     * @return The next element in the iteration.
     * @throws OXException If next element cannot be returned
     */
    T next() throws OXException;

    /**
     * Closes the search iterator
     */
    @Override
    void close();

    /**
     * This iterator's size.
     *
     * @return The size or <code>-1</code> if no size can be determined
     */
    int size();

    /**
     * Indicates if this iterator has warnings
     *
     * @return <code>true</code> if this iterator has warnings; otherwise <code>false</code>
     */
    boolean hasWarnings();

    /**
     * Adds specified warning to this iterator's warnings
     *
     * @param warning The warning to add
     */
    void addWarning(OXException warning);

    /**
     * Gets the iterator's warnings as an array
     *
     * @return The iterator's warnings as an array or <code>null</code>
     */
    OXException[] getWarnings();

}
