/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

