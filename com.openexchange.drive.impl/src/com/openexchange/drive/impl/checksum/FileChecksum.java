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

package com.openexchange.drive.impl.checksum;

import com.openexchange.file.storage.composition.FileID;


/**
 * {@link FileChecksum}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileChecksum extends StoredChecksum {

    private FileID fileID;
    private String version;

    /**
     * Initializes a new {@link FileChecksum}.
     */
    public FileChecksum() {
        super();
    }

    /**
     * Initializes a new {@link FileChecksum}.
     *
     * @param fileID The file ID
     * @param version The version
     * @param sequenceNumber The sequence number
     * @param checksum The checksum
     */
    public FileChecksum(FileID fileID, String version, long sequenceNumber, String checksum) {
        super();
        this.fileID = fileID;
        this.version = version;
        this.sequenceNumber = sequenceNumber;
        this.checksum = checksum;
    }

    /**
     * Gets the fileID
     *
     * @return The fileID
     */
    public FileID getFileID() {
        return fileID;
    }

    /**
     * Sets the fileID
     *
     * @param fileID The fileID to set
     */
    public void setFileID(FileID fileID) {
        this.fileID = fileID;
    }

    /**
     * Gets the version
     *
     * @return The version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version
     *
     * @param version The version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return fileID.getFolderId() + " | " + fileID.getFileId() + " | " + getVersion() + " | " + getChecksum() + " | " + getSequenceNumber();
    }

}
