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

package com.openexchange.file.storage.dropbox;

import static com.openexchange.file.storage.dropbox.Utils.handle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import com.dropbox.client2.DropboxAPI.Account;
import com.dropbox.client2.DropboxAPI.Entry;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.dropbox.access.DropboxOAuthAccess;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link DropboxFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DropboxFolderAccess extends AbstractDropboxAccess implements FileStorageFolderAccess {

    private final String accountDisplayName;
    private final int userId;

    /**
     * Initializes a new {@link DropboxFolderAccess}.
     *
     * @param dropboxOAuthAccess The underlying OAuth access
     * @param account The associated account
     * @param session The session
     * @param accountAccess The account access
     * @throws OXException 
     */
    public DropboxFolderAccess(final DropboxOAuthAccess dropboxOAuthAccess, final FileStorageAccount account, final Session session) throws OXException {
        super(dropboxOAuthAccess, account, session);
        userId = session.getUserId();
        accountDisplayName = account.getDisplayName();
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        String path = toPath(folderId);
        try {
            Entry entry = dropboxAPI.metadata(path, 1, null, false, null);
            return entry.isDir && false == entry.isDeleted;
        } catch (Exception e) {
            OXException x = handle(e, path);
            if (FileStorageExceptionCodes.NOT_FOUND.equals(x)) {
                return false;
            }
            throw x;
        }
    }

    @Override
    public FileStorageFolder getFolder(final String folderId) throws OXException {
        String path = toPath(folderId);
        try {
            Entry entry = dropboxAPI.metadata(path, 0, null, true, null);
            if (false == entry.isDir || entry.isDeleted) {
                throw FileStorageExceptionCodes.NOT_FOUND.create(DropboxConstants.ID, folderId);
            }
            return new DropboxFolder(entry, userId, accountDisplayName);
        } catch (Exception e) {
            throw handle(e, path);
        }
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
        return new FileStorageFolder[0];
    }

    @Override
    public FileStorageFolder[] getUserSharedFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    @Override
    public FileStorageFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws OXException {
        String path = toPath(parentIdentifier);
        try {
            Entry entry = dropboxAPI.metadata(path, 0, null, true, null);
            if (false == entry.isDir || entry.isDeleted) {
                throw FileStorageExceptionCodes.NOT_FOUND.create(DropboxConstants.ID, path);
            }
            List<FileStorageFolder> list = new LinkedList<FileStorageFolder>();
            if (entry.contents != null) {
                for (Entry childEntry : entry.contents) {
                    if (childEntry.isDir && false == childEntry.isDeleted) {
                        Entry ce = dropboxAPI.metadata(childEntry.path, 0, null, true, null);
                        list.add(new DropboxFolder(ce, userId, accountDisplayName));
                    }
                }
            }
            return list.toArray(new FileStorageFolder[0]);
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        DropboxFolder rootFolder = new DropboxFolder(userId);
        rootFolder.setRootFolder(true);
        rootFolder.setId(FileStorageFolder.ROOT_FULLNAME);
        rootFolder.setParentId(null);
        rootFolder.setName(accountDisplayName);
        rootFolder.setSubfolders(true);
        rootFolder.setSubscribedSubfolders(true);
        return rootFolder;
    }

    @Override
    public String createFolder(final FileStorageFolder toCreate) throws OXException {
        String parentPath = toPath(toCreate.getParentId());
        try {
            final Entry entry =
                dropboxAPI.createFolder(new StringBuilder("/".equals(parentPath) ? "" : parentPath).append('/').append(toCreate.getName()).toString());
            return toId(entry.path);
        } catch (Exception e) {
            throw handle(e, parentPath);
        }
    }

    @Override
    public String updateFolder(final String identifier, final FileStorageFolder toUpdate) throws OXException {
        // Dropbox neither supports subscription not permissions
        return identifier;
    }

    @Override
    public String moveFolder(final String folderId, final String newParentId) throws OXException {
        return moveFolder(folderId, newParentId, null);
    }

    @Override
    public String moveFolder(final String folderId, final String newParentId, String newName) throws OXException {
        final String path = toPath(folderId);
        try {
            final int pos = path.lastIndexOf('/');
            final String newParentPath = toPath(newParentId);
            final Entry moved =
                dropboxAPI.move(
                    path,
                    new StringBuilder("/".equals(newParentPath) ? "" : newParentPath).append('/').append(
                        null != newName ? newName : (pos < 0 ? path : path.substring(pos + 1))).toString());
            return toId(moved.path);
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        String path = toPath(folderId);
        int pos = path.lastIndexOf('/');
        String parentPath = 0 >= pos ? "/" : path.subSequence(0, pos) + "/";
        String toPath = parentPath + newName;
        try {
            if (Strings.equalsNormalizedIgnoreCase(path, toPath)) {
                Entry temp = dropboxAPI.move(path, parentPath + UUID.randomUUID().toString() + ' ' + newName);
                path = temp.path;
            }
            Entry entry = dropboxAPI.move(path, toPath);
            return toId(entry.path);
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    @Override
    public String deleteFolder(final String folderId) throws OXException {
        final String path = toPath(folderId);
        try {
            dropboxAPI.delete(path);
            return folderId;
        } catch (Exception e) {
            OXException x = handle(e, path);
            if (FileStorageExceptionCodes.NOT_FOUND.equals(x)) {
                return folderId;
            }
            throw x;
        }
    }

    @Override
    public String deleteFolder(final String folderId, final boolean hardDelete) throws OXException {
        return deleteFolder(folderId);
    }

    @Override
    public void clearFolder(final String folderId) throws OXException {
        String path = toPath(folderId);
        try {
            final Entry directoryEntry = dropboxAPI.metadata(path, 0, null, true, null);
            if (!directoryEntry.isDir) {
                throw FileStorageExceptionCodes.NOT_A_FOLDER.create(DropboxConstants.ID, folderId);
            }
            final List<Entry> contents = directoryEntry.contents;
            for (final Entry childEntry : contents) {
                if (!childEntry.isDir && !childEntry.isDeleted) {
                    dropboxAPI.delete(childEntry.path);
                }
            }
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    @Override
    public void clearFolder(final String folderId, final boolean hardDelete) throws OXException {
        clearFolder(folderId);
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        String path = toPath(folderId);
        try {
            final Entry directoryEntry = dropboxAPI.metadata(path, 0, null, false, null);
            if (!directoryEntry.isDir) {
                throw FileStorageExceptionCodes.NOT_A_FOLDER.create(DropboxConstants.ID, folderId);
            }
            final List<FileStorageFolder> list = new ArrayList<FileStorageFolder>();
            FileStorageFolder f = new DropboxFolder(directoryEntry, userId, accountDisplayName);
            list.add(f);
            String parentId;
            while (null != (parentId = f.getParentId())) {
                f = getFolder(parentId);
                list.add(f);
            }
            return list.toArray(new FileStorageFolder[list.size()]);
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    @Override
    public Quota getStorageQuota(final String folderId) throws OXException {
        /*
         * quota_info/normal The user's used quota outside of shared folders (bytes).
         * quota_info/shared The user's used quota in shared folders (bytes).
         * quota_info/quota The user's total quota allocation (bytes).
         */
        try {
            Account accountInfo = dropboxAPI.accountInfo();
            return new Quota(accountInfo.quota, accountInfo.quotaNormal + accountInfo.quotaShared, Type.STORAGE);
        } catch (Exception e) {
            throw handle(e, toPath(folderId));
        }
    }

    @Override
    public Quota getFileQuota(final String folderId) throws OXException {
        return Type.FILE.getUnlimited();
    }

    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        if (null == types) {
            return null;
        }
        Quota[] quotas = new Quota[types.length];
        for (int i = 0; i < types.length; i++) {
            switch (types[i]) {
                case FILE:
                    quotas[i] = getFileQuota(folder);
                    break;
                case STORAGE:
                    quotas[i] = getStorageQuota(folder);
                    break;
                default:
                    throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create("Quota " + types[i]);
            }
        }
        return quotas;
    }

}
