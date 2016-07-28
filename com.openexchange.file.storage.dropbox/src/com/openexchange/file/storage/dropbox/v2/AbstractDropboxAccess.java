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

package com.openexchange.file.storage.dropbox.v2;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DeletedMetadata;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.Metadata;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.dropbox.DropboxConstants;
import com.openexchange.file.storage.dropbox.access.DropboxOAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link AbstractDropboxAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractDropboxAccess {

    protected final DropboxOAuthAccess dropboxOAuthAccess;
    protected final Session session;
    protected final FileStorageAccount account;
    protected final DbxClientV2 client;

    /**
     * Initialises a new {@link AbstractDropboxAccess}.
     */
    AbstractDropboxAccess(DropboxOAuthAccess dropboxOAuthAccess, FileStorageAccount account, Session session) {
        super();
        this.dropboxOAuthAccess = dropboxOAuthAccess;
        this.session = session;
        this.account = account;
        this.client = dropboxOAuthAccess.getDropboxClient();
    }

    /**
     * Determines whether the specified folder identifier denotes the root folder
     * 
     * @param folderId The folder identifier
     * @return true if the specified folder identifier denotes a root folder; false otherwise
     */
    boolean isRoot(String folderId) {
        return FileStorageFolder.ROOT_FULLNAME.equals(folderId) || "/".equals(folderId);
    }

    /**
     * Gets the (dropbox-)path for specified folder identifier.
     *
     * @param folderId The folder identifier
     * @return The associated path
     */
    String toPath(String folderId) {
        //TODO: Check for null?
        return isRoot(folderId) ? "/" : folderId;
    }

    /**
     * Gets the (dropbox-)path for the specified folder- and file-identifier.
     *
     * @param folderId The folder identifier
     * @param fileId The file identifier
     * @return The associated path
     */
    String toPath(String folderId, String fileId) {
        String parentPath = isRoot(folderId) ? "/" : folderId;
        return parentPath.endsWith("/") ? parentPath + fileId : parentPath + '/' + fileId;
    }

    /**
     * Returns the metadata for a file ({@link FileMetadata}) or a folder ({@link FolderMetadata})
     * 
     * @param id The resource identifier
     * @return The metadata of the file or folder
     * @throws OXException If the resource is not found
     * @throws GetMetadataErrorException If a metadata error is occurred
     * @throws DbxException if a generic Dropbox error is occurred
     */
    Metadata getMetadata(String id) throws OXException, GetMetadataErrorException, DbxException {
        Metadata metadata = client.files().getMetadata(id);
        if (metadata instanceof DeletedMetadata) {
            throw FileStorageExceptionCodes.NOT_FOUND.create(DropboxConstants.ID, id);
        }
        return metadata;
    }

    /**
     * Gets the {@link FolderMetadata} of the specified folder.
     * 
     * @param folderId The folder identifier
     * @return The {@link FolderMetadata}
     * @throws GetMetadataErrorException If a metadata error is occurred
     * @throws DbxException If a generic Dropbox error is occurred
     * @throws OXException if the specified identifier does not denote a folder
     */
    FolderMetadata getFolderMetadata(String folderId) throws GetMetadataErrorException, DbxException, OXException {
        Metadata metadata = getMetadata(folderId);
        if (metadata instanceof FolderMetadata) {
            return (FolderMetadata) metadata;
        }
        throw FileStorageExceptionCodes.NOT_A_FOLDER.create(DropboxConstants.ID, folderId);
    }
}
