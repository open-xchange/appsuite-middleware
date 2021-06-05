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

import java.util.function.Function;
import com.openexchange.exception.OXException;

/**
 * {@link ConvertingSearchIterator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class ConvertingSearchIterator<T, R> implements SearchIterator<R> {

    private final SearchIterator<T> delegate;
    private final Function<T, R> converter;

    /**
     * Initializes a new {@link ConvertingSearchIterator}.
     *
     * @param delegate The underlying search iterator
     * @param converter The convert function to produce the resulting elements from the delegate's elements
     */
    public ConvertingSearchIterator(SearchIterator<T> delegate, Function<T, R> converter) {
        super();
        this.delegate = delegate;
        this.converter = converter;
    }

    @Override
    public void addWarning(OXException warning) {
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
        return delegate.hasNext();
    }

    @Override
    public boolean hasWarnings() {
        return delegate.hasWarnings();
    }

    @Override
    public R next() throws OXException {
        return converter.apply(delegate.next());
    }

    @Override
    public int size() {
        return delegate.size();
    }

}
