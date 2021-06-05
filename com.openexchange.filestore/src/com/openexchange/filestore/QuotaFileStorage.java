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
import java.util.Set;
import com.openexchange.exception.OXException;

/**
 * {@link QuotaFileStorage} - A {@link FileStorage file storage} that is quota aware.
 */
public interface QuotaFileStorage extends FileStorage, QuotaMode {

    /**
     * Gets the total available quota
     *
     * @return The total quota, <code>0</code> (zero) if no quota at all (deny all), or less than <code>0</code> (zero) for unlimited/not set
     * @throws OXException If quota limit cannot be returned
     */
    long getQuota() throws OXException;

    /**
     * Gets the currently used quota
     *
     * @return The currently used quota
     * @throws OXException
     */
    long getUsage() throws OXException;

    /**
     * Recalculates the used quota
     *
     * @throws OXException If calculation fails
     */
    void recalculateUsage() throws OXException;

    /**
     * Recalculates the used quota ignoring specified files.
     *
     * @param filesToIgnore The files to ignore
     * @throws OXException If calculation fails
     */
    void recalculateUsage(Set<String> filesToIgnore) throws OXException;

    /**
     * Saves a new file
     *
     * @param file The file to save
     * @param sizeHint The appr. file size
     * @return The identifier of the newly saved file
     * @throws OXException If save operation fails
     */
    String saveNewFile(InputStream file, long sizeHint) throws OXException;

    /**
     * Appends specified stream to the supplied file.
     *
     * @param file The stream to append to the file
     * @param name The existing file's path in associated file storage
     * @param offset The offset in bytes where to append the data, must be equal to the file's current length
     * @param sizeHint A size hint about the expected stream length in bytes, or <code>-1</code> if unknown
     * @return The updated length of the file
     * @throws OXException If appending file fails
     */
    long appendToFile(InputStream file, String name, long offset, long sizeHint) throws OXException;

}
