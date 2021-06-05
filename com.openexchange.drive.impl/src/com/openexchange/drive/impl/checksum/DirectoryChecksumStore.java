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
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.FolderID;

/**
 * {@link DirectoryChecksumStore}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface DirectoryChecksumStore {

    /**
     * Inserts the supplied directory checksum into the store.
     *
     * @param directoryChecksum The checksum to insert
     * @return The checksum
     * @throws OXException
     */
    DirectoryChecksum insertDirectoryChecksum(DirectoryChecksum directoryChecksum) throws OXException;

    /**
     * Inserts the supplied directory checksums into the store.
     *
     * @param directoryChecksums The checksums to insert
     * @return The checksums
     * @throws OXException
     */
    List<DirectoryChecksum> insertDirectoryChecksums(List<DirectoryChecksum> directoryChecksums) throws OXException;

    /**
     * Updates the supplied directory checksum.
     *
     * @param directoryChecksum The checksum to update
     * @return The checksum
     * @throws OXException
     */
    DirectoryChecksum updateDirectoryChecksum(DirectoryChecksum directoryChecksum) throws OXException;

    /**
     * Updates the supplied directory checksums.
     *
     * @param directoryChecksums The checksums to update
     * @return The checksums
     * @throws OXException
     */
    List<DirectoryChecksum> updateDirectoryChecksums(List<DirectoryChecksum> directoryChecksums) throws OXException;

    /**
     * Updates the folder ID to another one in all stored directory checksums.
     *
     * @param folderID The folder ID to update
     * @param newFolderID The new folder ID
     * @return <code>true</code> if at least one checksum was updated, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean updateDirectoryChecksumFolder(FolderID folderID, FolderID newFolderID) throws OXException;

    /**
     * Removes all directory checksums of a folder.
     *
     * @param folderID The folder ID to remove the checksum for
     * @return <code>true</code> if at least one checksum was removed, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean removeDirectoryChecksum(FolderID folderID) throws OXException;

    /**
     * Removes all directory checksums of multiple folders.
     *
     * @param folderIDs The folder IDs to remove the checksums for
     * @return The number of affected rows
     * @throws OXException
     */
    int removeAllDirectoryChecksums(List<FolderID> folderIDs) throws OXException;

    /**
     * Gets the directory checksum of a folder.
     *
     * @param userID The user ID
     * @param folderID The folder ID to get the checksum for
     * @param view The view of the directory, or <code>0</code> for the default view
     * @return The checksum, or <code>null</code> if not found
     * @throws OXException
     */
    DirectoryChecksum getDirectoryChecksum(int userID, FolderID folderID, int view) throws OXException;

    /**
     * Gets the directory checksums of multiple folders.
     *
     * @param userID The user ID
     * @param folderIDs The folder IDs to get the checksum for
     * @param view The view of the directory, or <code>0</code> for the default view
     * @return The found checksums
     * @throws OXException
     */
    List<DirectoryChecksum> getDirectoryChecksums(int userID, List<FolderID> folderIDs, int view) throws OXException;

    /**
     * Touches a list of directory checksums by resetting their <code>used</code>-column to the current timestamp.
     *
     * @param directoryChecksums The directory checksums to touch
     * @return The number of updated rows
     */
    int touchDirectoryChecksums(List<DirectoryChecksum> directoryChecksums) throws OXException;

    /**
     * Gets a list of directory checksums that were not "used", i.e. not have been inserted/updated or touched since the supplied
     * timestamp.
     *
     * @param unusedSince The maximum "used" timestamp of a checksum to be considered as "unused"
     * @return The unused checksums, or <code>null</code> if there are none
     */
    List<DirectoryChecksum> getUnusedDirectoryChecksums(long unusedSince) throws OXException;

    List<DirectoryChecksum> getDirectoryChecksums(List<FolderID> folderIDs) throws OXException;

    int removeDirectoryChecksums(List<DirectoryChecksum> directoryChecksums) throws OXException;

}
