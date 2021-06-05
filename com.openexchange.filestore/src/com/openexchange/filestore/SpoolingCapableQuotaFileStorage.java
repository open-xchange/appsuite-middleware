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

package com.openexchange.filestore;

import java.io.InputStream;
import com.openexchange.exception.OXException;

/**
 * {@link SpoolingCapableQuotaFileStorage} - A {@link FileStorage file storage} that is quota aware and offers the possibility to spool a
 * passed stream to a temporary file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public interface SpoolingCapableQuotaFileStorage extends QuotaFileStorage {

    /**
     * Saves a new file
     *
     * @param file The file to save
     * @param sizeHint The appr. file size or <code>-1</code> if unknown
     * @param spoolToFile Whether to spool given stream to a temporary file
     * @return The identifier of the newly saved file
     * @throws OXException If save operation fails
     * @see Spool
     */
    String saveNewFile(InputStream file, long sizeHint, boolean spoolToFile) throws OXException;

    /**
     * Appends specified stream to the supplied file.
     *
     * @param file The stream to append to the file
     * @param name The existing file's path in associated file storage
     * @param offset The offset in bytes where to append the data, must be equal to the file's current length
     * @param sizeHint A size hint about the expected stream length in bytes, or <code>-1</code> if unknown
     * @param spoolToFile Whether to spool given stream to a temporary file
     * @return The updated length of the file
     * @throws OXException If appending file fails
     * @see Spool
     */
    long appendToFile(InputStream file, String name, long offset, long sizeHint, boolean spoolToFile) throws OXException;
}
