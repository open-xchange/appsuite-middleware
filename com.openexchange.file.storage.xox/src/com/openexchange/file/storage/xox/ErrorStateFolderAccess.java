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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.xox;

import java.util.Objects;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;

/**
 * {@link ErrorStateFolderAccess} - A {@link FileStorageFolderAccess} implementation which can be used in case of an account error.
 * <p>
 * If the real folder storage is known to be in an error state, this implementation will, at least, return the last known folders.
 * This is useful to serve them to a client but tag them as defective.
 * </p>
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
class ErrorStateFolderAccess implements FileStorageFolderAccess {

    private final OXException error;
    private final ErrorStateFolderAccess.OXFunction<String, FileStorageFolder> getFolderFunction;

    /**
     * {@link OXFunction} Represents a OXException aware function that accepts one argument and produces a result.
     *
     * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
     * @since v7.10.5
     * @param <T> The type of the input to the function
     * @param <R> The type of the result of the function
     */
    @FunctionalInterface
    public interface OXFunction<T, R> {

        R apply(T t) throws OXException;
    }

    /**
     * Initializes a new {@link ErrorStateFolderAccess}.
     *
     * @param error The current problem preventing to query the remote folders
     * @param getFolderFunction A function which will be used to retrieve the last known folder, when loaded
     */
    public ErrorStateFolderAccess(OXException error, OXFunction<String, FileStorageFolder> getFolderFunction) {
        this.error = Objects.requireNonNull(error, "error must not be null");
        this.getFolderFunction = Objects.requireNonNull(getFolderFunction, "getFolderFunction must not be null");
    }

    @Override
    public boolean exists(String folderId) throws OXException {
        return getFolder(folderId) != null;
    }

    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        FileStorageFolder lastKnownFolder = this.getFolderFunction.apply(folderId);
        if (lastKnownFolder == null) {
            throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
        }

        lastKnownFolder.getProperties().put("accountError", error);

        //TODO remove FolderAccountErrorField
        //lastKnownFolder.getProperties().put(FolderAccountErrorField.getInstance(), error);
        return lastKnownFolder;
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder getTrashFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        throw error;
    }

    @Override
    public FileStorageFolder[] getUserSharedFolders() throws OXException {
        throw error;
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        throw error;
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        throw error;
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        throw error;
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        throw error;
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        throw error;
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        throw error;
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        throw error;
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        throw error;
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        throw error;
    }

    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        throw error;
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        throw error;
    }

    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        throw error;
    }

    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        throw error;
    }

    @Override
    public Quota[] getQuotas(String folder, Type[] types) throws OXException {
        throw error;
    }
}
