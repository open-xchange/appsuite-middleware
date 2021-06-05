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

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File.Field;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link ObjectPermissionAware}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface ObjectPermissionAware extends FileStorageFileAccess {

    /**
     * Gets all documents that are considered as "shared" by the user, i.e. those documents of the user that have been shared to at least
     * one other entity.
     *
     * @param fields The fields to load, or <code>null</code> to load all fields
     * @param sort The field to sort by, or <code>null</code> for no specific sort order
     * @param order The sorting direction, or <code>null</code> for no specific sort order
     * @return The documents
     * @throws OXException If operation fails
     */
    SearchIterator<File> getUserSharedDocuments(List<Field> fields, Field sort, SortDirection order) throws OXException;

}
