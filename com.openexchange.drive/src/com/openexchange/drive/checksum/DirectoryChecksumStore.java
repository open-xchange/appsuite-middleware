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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.checksum;

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
     * @return <code>true</code> if at least one checksum was removed, <code>false</code>, otherwise
     * @throws OXException
     */
    int removeDirectoryChecksums(List<FolderID> folderIDs) throws OXException;

    /**
     * Gets the directory checksum of a folder.
     *
     * @param userID The user ID
     * @param folderID The folder ID to get the checksum for
     * @return The checksum, or <code>null</code> if not found
     * @throws OXException
     */
    DirectoryChecksum getDirectoryChecksum(int userID, FolderID folderID) throws OXException;

    /**
     * Gets the directory checksums of multiple folders.
     *
     * @param userID The user ID
     * @param folderIDs The folder IDs to get the checksum for
     * @return The found checksums
     * @throws OXException
     */
    List<DirectoryChecksum> getDirectoryChecksums(int userID, List<FolderID> folderIDs) throws OXException;

}

