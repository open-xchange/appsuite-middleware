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

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;

/**
 * {@link FileChecksumStore}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface FileChecksumStore {

    /**
     * Inserts the supplied file checksum into the store.
     *
     * @param fileChecksum The checksum to insert
     * @return The checksum
     * @throws OXException
     */
    FileChecksum insertFileChecksum(FileChecksum fileChecksum) throws OXException;

    /**
     * Inserts the supplied file checksums into the store.
     *
     * @param fileChecksums The checksums to insert
     * @return The checksums
     * @throws OXException
     */
    List<FileChecksum> insertFileChecksums(List<FileChecksum> fileChecksums) throws OXException;

    /**
     * Updates the supplied file checksum.
     *
     * @param fileChecksum The checksum to update
     * @return The checksum
     * @throws OXException
     */
    FileChecksum updateFileChecksum(FileChecksum fileChecksum) throws OXException;

    /**
     * Updates the supplied file checksums.
     *
     * @param fileChecksums The checksums to update
     * @return The checksums
     * @throws OXException
     */
    List<FileChecksum> updateFileChecksums(List<FileChecksum> fileChecksums) throws OXException;

    /**
     * Updates the folder ID to another one in all matching stored file checksums.
     *
     * @param folderID The folder ID to update
     * @param newFolderID The new folder ID
     * @return The number of updated checksums
     * @throws OXException
     */
    int updateFileChecksumFolders(FolderID folderID, FolderID newFolderID) throws OXException;

    /**
     * Removes the supplied file checksum.
     *
     * @param fileChecksum The checksum to remove
     * @return <code>true</code> if a checksum was removed, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean removeFileChecksum(FileChecksum fileChecksum) throws OXException;

    /**
     * Removes the file checksum matching the supplied parameters.
     *
     * @param fileID The file ID of the checksum to remove
     * @param version The version of the checksum to remove
     * @param sequenceNumber The sequence number of the checksum to remove
     * @return <code>true</code> if a checksum was removed, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean removeFileChecksum(FileID fileID, String version, long sequenceNumber) throws OXException;

    /**
     * Removes the supplied file checksums.
     *
     * @param fileChecksums The checksums to remove
     * @return The number of removed checksums
     * @throws OXException
     */
    int removeFileChecksums(List<FileChecksum> fileChecksums) throws OXException;

    /**
     * Removes all file checksums matching the supplied folder ID.
     *
     * @param folderID The folder ID of the checksums to remove
     * @return The number of removed checksums
     * @throws OXException
     */
    int removeFileChecksumsInFolder(FolderID folderID) throws OXException;

    /**
     * Removes all file checksums matching one of the supplied folder IDs.
     *
     * @param folderIDs The folder IDs of the checksums to remove
     * @return The number of removed checksums
     * @throws OXException
     */
    int removeFileChecksumsInFolders(List<FolderID> folderIDs) throws OXException;

    /**
     * Removes all file checksums matching the supplied file ID.
     *
     * @param fileID The file ID of the checksums to remove
     * @return The number of removed checksums
     * @throws OXException
     */
    int removeFileChecksums(FileID fileID) throws OXException;

    /**
     * Removes all file checksums matching one of the supplied file IDs.
     *
     * @param fileIDs The file IDs of the checksums to remove
     * @return The number of removed checksums
     * @throws OXException
     */
    int removeFileChecksums(FileID...fileIDs) throws OXException;

    /**
     * Gets a file checksum.
     *
     * @param fileID The file ID
     * @param version the version
     * @param sequenceNumber The sequence number
     * @return The file checksum, or <code>null</code> if not found
     * @throws OXException
     */
    FileChecksum getFileChecksum(FileID fileID, String version, long sequenceNumber) throws OXException;

    /**
     * Gets all file checksums in a folder.
     *
     * @param folderID The folder ID of the checksums to get
     * @return The file checksums
     * @throws OXException
     */
    List<FileChecksum> getFileChecksums(FolderID folderID) throws OXException;

    /**
     * Gets all file checksums matching the supplied checksums
     *
     * @param checksum The checksums to lookup
     * @return The file checksums
     * @throws OXException
     */
    Map<String, List<FileChecksum>> getMatchingFileChecksums(List<String> checksums) throws OXException;

}

