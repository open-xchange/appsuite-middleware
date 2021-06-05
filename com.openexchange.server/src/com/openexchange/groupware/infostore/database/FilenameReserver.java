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

package com.openexchange.groupware.infostore.database;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;

/**
 * {@link FilenameReserver}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface FilenameReserver {

    /**
     * Silently cleans up any previous reservation held by this filename reserver.
     */
    void cleanUp();

    /**
     * Reserves the filenames of the supplied documents in their target folders.
     *
     * @param documents The documents to reserve the filenames for
     * @param adjustAsNeeded <code>true</code> to automatically adjust the filenames in case of conflicts in the target folder,
     *                       <code>false</code>, otherwise
     * @return The reservations, each one mapped to its corresponding document
     */
    Map<DocumentMetadata, FilenameReservation> reserve(List<DocumentMetadata> documents, boolean adjustAsNeeded) throws OXException;

    /**
     * Reserves the filename of the supplied documents in their target folders.
     *
     * @param document The document to reserve the filenames for
     * @param adjustAsNeeded <code>true</code> to automatically adjust the filename in case of conflicts in the target folder,
     *                       <code>false</code>, otherwise
     * @return The reservation
     */
    FilenameReservation reserve(DocumentMetadata document, boolean adjustAsNeeded) throws OXException;

}

