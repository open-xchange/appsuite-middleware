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

package com.openexchange.groupware.infostore.utils;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.results.Delta;
import com.openexchange.tools.iterator.SearchIterator;

public class FileDelta implements Delta<DocumentMetadata> {

    private final SearchIterator<DocumentMetadata> newOnes;
    private final SearchIterator<DocumentMetadata> modifiedOnes;
    private final SearchIterator<DocumentMetadata> deletedOnes;
    private final long sequenceNumber;

    public FileDelta(SearchIterator<DocumentMetadata> newOnes, SearchIterator<DocumentMetadata> modifiedOnes, SearchIterator<DocumentMetadata> deletedOnes, long sequenceNumber) {
        this.newOnes = newOnes;
        this.modifiedOnes = modifiedOnes;
        this.deletedOnes = deletedOnes;
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Gets a search iterator with information about new added document metadata.
     * Possibly this value is not known and the iterator is empty.
     * 
     * @return SearchIterator<DocumentMetadata> The document metadata search iterator.
     */
    @Override
    public SearchIterator<DocumentMetadata> getNew() {
        return newOnes;
    }

    @Override
    public SearchIterator<DocumentMetadata> getModified() {
        return modifiedOnes;
    }

    @Override
    public SearchIterator<DocumentMetadata> getDeleted() {
        return deletedOnes;
    }

    @Override
    public SearchIterator<DocumentMetadata> results() throws OXException {
        SearchIterator<DocumentMetadata> it = new CombinedFilteringFileSearchIterator(newOnes, modifiedOnes);
        return it;
    }

    @Override
    public long sequenceNumber() throws OXException {
        return sequenceNumber;
    }

    public void close() throws OXException {
        newOnes.close();
        modifiedOnes.close();
        deletedOnes.close();
    }

}
