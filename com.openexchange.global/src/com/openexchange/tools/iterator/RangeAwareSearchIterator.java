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
 * {@link RangeAwareSearchIterator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @7.10.5
 */
public class RangeAwareSearchIterator<T> extends FilteringSearchIterator<T> {

    private final int start;
    private final int end;

    private int index;

    /**
     * Initializes a new {@link RangeAwareSearchIterator}.
     * 
     * @param delegate The underlying search iterator
     * @param start The inclusive start index for the search results, or <code>-1</code> if not defined
     * @param end The exclusive end index for the search results, or <code>-1</code> if not defined
     */
    public RangeAwareSearchIterator(SearchIterator<T> delegate, int start, int end) throws OXException {
        super(delegate);
        this.start = start;
        this.end = end;
        this.index = 0;
    }

    @Override
    public boolean accept(T thing) throws OXException {
        try {
            if (0 < start && index < start || 0 < end && index >= end) {
                return false;
            }
            return true;
        } finally {
            index++;
        }
    }

}
