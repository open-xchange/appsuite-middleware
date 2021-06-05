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

package com.openexchange.groupware.results;

import com.openexchange.exception.OXException;
import com.openexchange.tools.iterator.CombinedSearchIterator;
import com.openexchange.tools.iterator.SearchIterator;

public class DeltaImpl<T> implements Delta<T> {

    private final SearchIterator<T> newOnes;
    private final SearchIterator<T> modifiedOnes;
    private final SearchIterator<T> deletedOnes;
    private final long sequenceNumber;

    public DeltaImpl(final SearchIterator<T> newOnes, final SearchIterator<T> modifiedOnes, final SearchIterator<T> deletedOnes, final long sequenceNumber) {
        this.newOnes = newOnes;
        this.modifiedOnes = modifiedOnes;
        this.deletedOnes = deletedOnes;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public SearchIterator<T> getNew() {
        return newOnes;
    }

    @Override
    public SearchIterator<T> getModified() {
        return modifiedOnes;
    }

    @Override
    public SearchIterator<T> getDeleted() {
        return deletedOnes;
    }

    @Override
    public SearchIterator<T> results() throws OXException {
        return new CombinedSearchIterator<T>(newOnes, modifiedOnes);
    }

    @Override
    public long sequenceNumber() throws OXException {
        return sequenceNumber;
    }

    public void close() {
        newOnes.close();
        modifiedOnes.close();
        deletedOnes.close();
    }

}
