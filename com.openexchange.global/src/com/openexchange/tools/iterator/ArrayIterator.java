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
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.tools.arrays.Arrays;

/**
 * {@link ArrayIterator} - A {@link SearchIterator} implementation backed by an array.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ArrayIterator<T> implements SearchIterator<T> {

    private int index;

    private final T[] array;

    private final List<OXException> warnings = new ArrayList<OXException>();

    /**
     * Initializes a new {@link ArrayIterator}
     *
     * @param array The underlying array
     */
    public ArrayIterator(final T[] array) {
        super();
        this.array = Arrays.clone(array);
    }

    @Override
    public boolean hasNext() throws OXException {
        return index < array.length;
    }

    @Override
    public T next() throws OXException {
        return array[index++];
    }

    @Override
    public void close() {
        // Does not apply to array-backed iterator
    }

    @Override
    public int size() {
        return array.length;
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
