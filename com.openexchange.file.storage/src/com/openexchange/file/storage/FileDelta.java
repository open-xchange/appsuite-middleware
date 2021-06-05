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

package com.openexchange.file.storage;

import java.util.Collection;
import com.openexchange.groupware.results.DeltaImpl;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;


/**
 * {@link FileDelta}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileDelta extends DeltaImpl<File> {

    /**
     * Initializes a new {@link FileDelta}.
     *
     * @param newFiles The new files
     * @param modifiedFiles The modified files
     * @param deletedFiles The deleted files
     * @param sequenceNumber The sequence number
     */
    public FileDelta(final Collection<File> newFiles, final Collection<File> modifiedFiles, final Collection<File> deletedFiles, final long sequenceNumber) {
        super(searchIteratorFor(newFiles), searchIteratorFor(modifiedFiles), searchIteratorFor(deletedFiles), sequenceNumber);
    }

    /**
     * Initializes a new {@link FileDelta}.
     *
     * @param newFiles The iterator for new files
     * @param modifiedFiles The iterator for modified files
     * @param deletedFiles The iterator for deleted files
     * @param sequenceNumber The sequence number
     */
    public FileDelta(final SearchIterator<File> newFiles, final SearchIterator<File> modifiedFiles, final SearchIterator<File> deletedFiles, final long sequenceNumber) {
        super(newFiles, modifiedFiles, deletedFiles, sequenceNumber);
    }

    private static SearchIterator<File> searchIteratorFor(final Collection<File> collection) {
        return new SearchIteratorAdapter<File>(collection.iterator(), collection.size());
    }

}
