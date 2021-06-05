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
import java.util.Arrays;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link CombinedSearchIterator} - Combines one or more instances of {@link SearchIterator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CombinedSearchIterator<T> implements SearchIterator<T> {

    private final SearchIterator<T>[] iterators;

    private int i = 0;

    private OXException[] warnings;

    private Boolean hasWarnings;

    /**
     * Initializes a new {@link CombinedSearchIterator}
     *
     * @param iterators The instances of {@link SearchIterator}
     */
    @SafeVarargs
    public CombinedSearchIterator(final SearchIterator<T>... iterators) {
        super();
        this.iterators = iterators;
    }

    private boolean next = false;

    @Override
    public boolean hasNext() throws OXException{
        if (iterators.length == 0) {
            return false;
        }
        next = false;
        while (i < iterators.length && !next) {
            if (iterators[i].hasNext()) {
                next = true;
            } else {
                i++;
            }
        }
        return next;
    }

    @Override
    public T next() throws OXException {
        if (iterators.length == 0 || !next) {
            throw SearchIteratorExceptionCodes.NO_SUCH_ELEMENT.create().setPrefix("NON");
        }
        return iterators[i].next();
    }

    @Override
    public void close() {
        for (final SearchIterator<T> iter : iterators) {
            SearchIterators.close(iter);
        }
    }

    @Override
    public int size() {
        return -1;
    }

    public boolean hasSize() {
        return false;
    }

    @Override
    public void addWarning(final OXException warning) {
        throw new UnsupportedOperationException("Mehtod addWarning() not implemented");
    }

    @Override
    public OXException[] getWarnings() {
        if (null == warnings) {
            if (iterators.length == 0) {
                warnings = new OXException[0];
            } else {
                final List<OXException> list = new ArrayList<OXException>(iterators.length << 1);
                for (final SearchIterator<?> iter : iterators) {
                    if (iter.hasWarnings()) {
                        list.addAll(Arrays.asList(iterators[i].getWarnings()));
                    }
                }
                warnings = list.toArray(new OXException[list.size()]);
            }
        }
        return warnings.length == 0 ? null : warnings;
    }

    @Override
    public boolean hasWarnings() {
        if (null == hasWarnings) {
            if (iterators.length == 0) {
                hasWarnings = Boolean.FALSE;
            } else {
                hasWarnings = Boolean.FALSE;
                for (final SearchIterator<?> iter : iterators) {
                    if (iter.hasWarnings()) {
                        hasWarnings = Boolean.TRUE;
                        break;
                    }
                }
            }
        }
        return hasWarnings.booleanValue();
    }
}
