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

import java.io.InputStream;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link FileStorageIgnorableVersionFileAccess} - Extends {@link FileStorageFileAccess} by a <tt>saveDocument()</tt> method that allows to
 * specify whether a document's version shall be set to a new value or not.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FileStorageIgnorableVersionFileAccess extends FileStorageFileAccess {

    /**
     * Save the file metadata and binary content.
     * <p>
     * It is allowed to specify whether that document's version shall be set to a new value or not
     *
     * @param file The metadata to save
     * @param data The binary content
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass DISTANT_FUTURE to circumvent the check
     * @param modifiedFields The fields to save. All other fields will be ignored
     * @param ignoreVersion Whether a new version is supposed to be set if binary content is available; or <code>true</code> to keep version as is
     * @throws OXException If operation fails
     */
    IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<File.Field> modifiedFields, boolean ignoreVersion) throws OXException;

    /**
     * Save the file as new file version, if file exists in folder
     *
     * @param file The metadata to save
     * @param data The binary content
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass DISTANT_FUTURE to circumvent the check
     * @param modifiedFields The fields to save. All other fields will be ignored
     * @return
     * @throws OXException On error
     */
    IDTuple saveDocumentTryAddVersion(File file, InputStream data, long sequenceNumber, List<File.Field> modifiedFields) throws OXException;
}
