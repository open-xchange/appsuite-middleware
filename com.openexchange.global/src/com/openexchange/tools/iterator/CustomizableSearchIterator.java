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
 * A {@link CustomizableSearchIterator} can be used to modify or replace all objects as they come out of {@link #next()}.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CustomizableSearchIterator<T> implements SearchIterator<T> {

    private final SearchIterator<T> searchIterator;
    private final Customizer<T> customizer;

    /**
     * Initializes a new {@link CustomizableSearchIterator}.
     *
     * @param searchIterator The iterator instance to wrap
     * @param customizer The customizer to apply
     */
    public CustomizableSearchIterator(SearchIterator<T> searchIterator, Customizer<T> customizer) {
        super();
        this.searchIterator = searchIterator;
        this.customizer = customizer;
    }

    @Override
    public void addWarning(OXException warning) {
        searchIterator.addWarning(warning);
    }

    @Override
    public void close() {
        SearchIterators.close(searchIterator);
    }

    @Override
    public OXException[] getWarnings() {
        return searchIterator.getWarnings();
    }

    @Override
    public boolean hasNext() throws OXException {
        return searchIterator.hasNext();
    }

    @Override
    public boolean hasWarnings() {
        return searchIterator.hasWarnings();
    }

    @Override
    public T next() throws OXException {
        return null == customizer ? searchIterator.next() : customizer.customize(searchIterator.next());
    }

    @Override
    public int size() {
        return searchIterator.size();
    }

}
