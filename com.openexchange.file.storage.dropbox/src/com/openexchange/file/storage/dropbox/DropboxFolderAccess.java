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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.dropbox.session.DropboxOAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link DropboxFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DropboxFolderAccess extends AbstractDropboxAccess implements FileStorageFolderAccess {

    private final DropboxAccountAccess accountAccess;
    private final int userId;

    /**
     * Initializes a new {@link DropboxFolderAccess}.
     */
    public DropboxFolderAccess(final DropboxOAuthAccess dropboxOAuthAccess, final FileStorageAccount account, final Session session, final DropboxAccountAccess accountAccess) {
        super(dropboxOAuthAccess, account, session);
        this.accountAccess = accountAccess;
        userId = session.getUserId();
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        try {
            final Entry entry = dropboxAPI.metadata(toPath(folderId), 1, null, false, null);
            return entry.isDir && !entry.isDeleted;
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                return false;
            }
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder getFolder(final String folderId) throws OXException {
        try {
            final Entry entry = dropboxAPI.metadata(toPath(folderId), 0, null, true, null);
            if (!entry.isDir || entry.isDeleted) {
                throw DropboxExceptionCodes.NOT_FOUND.create(folderId);
            }
            return new DropboxFolder(userId).parseDirEntry(entry, accountAccess.getAccount().getDisplayName());
        } catch (final DropboxServerException e) {
            throw handleServerError(folderId, e);
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
    public FileStorageFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws OXException {
        try {
            final Entry entry = dropboxAPI.metadata(toPath(parentIdentifier), 0, null, true, null);
            if (!entry.isDir || entry.isDeleted) {
                throw DropboxExceptionCodes.NOT_FOUND.create(parentIdentifier);
            }
            final List<FileStorageFolder> list = new LinkedList<FileStorageFolder>();
            String accDisplayName = accountAccess.getAccount().getDisplayName();
            for (final Entry childEntry : entry.contents) {
                if (childEntry.isDir && !childEntry.isDeleted) {
                    list.add(new DropboxFolder(userId).parseDirEntry(childEntry, accDisplayName));
                }
            }
            return list.toArray(new FileStorageFolder[0]);
        } catch (final DropboxServerException e) {
            throw handleServerError(parentIdentifier, e);
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        return getFolder(FileStorageFolder.ROOT_FULLNAME);
    }

    @Override
    public String createFolder(final FileStorageFolder toCreate) throws OXException {
        try {
            final String parentPath = toPath(toCreate.getParentId());
            final Entry entry =
                dropboxAPI.createFolder(new StringBuilder("/".equals(parentPath) ? "" : parentPath).append('/').append(toCreate.getName()).toString());
            return toId(entry.path);
        } catch (final DropboxServerException e) {
            throw handleServerError(null, e);
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
        try {
            final String path = toPath(folderId);
            final int pos = path.lastIndexOf('/');
            final String newParentPath = toPath(newParentId);
            final Entry moved =
                dropboxAPI.move(
                    path,
                    new StringBuilder("/".equals(newParentPath) ? "" : newParentPath).append('/').append(
                        null != newName ? newName : (pos < 0 ? path : path.substring(pos + 1))).toString());
            return toId(moved.path);
        } catch (final DropboxServerException e) {
            throw handleServerError(folderId, e);
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        try {
            final String path = toPath(folderId);
            final int pos = path.lastIndexOf('/');
            final Entry moved =
                dropboxAPI.move(
                    path,
                    pos <= 0 ? newName : new StringBuilder(path.substring(0, pos)).append('/').append(newName).toString());
            return toId(moved.path);
        } catch (final DropboxServerException e) {
            throw handleServerError(folderId, e);
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String deleteFolder(final String folderId) throws OXException {
        try {
            final String path = toPath(folderId);
            dropboxAPI.delete(path);
            return folderId;
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                return folderId;
            }
            throw handleServerError(null, e);
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String deleteFolder(final String folderId, final boolean hardDelete) throws OXException {
        return deleteFolder(folderId);
    }

    @Override
    public void clearFolder(final String folderId) throws OXException {
        try {
            final Entry directoryEntry = dropboxAPI.metadata(toPath(folderId), 0, null, true, null);
            if (!directoryEntry.isDir) {
                throw DropboxExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final List<Entry> contents = directoryEntry.contents;
            for (final Entry childEntry : contents) {
                if (!childEntry.isDir && !childEntry.isDeleted) {
                    dropboxAPI.delete(childEntry.path);
                }
            }
        } catch (final DropboxServerException e) {
            throw handleServerError(folderId, e);
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void clearFolder(final String folderId, final boolean hardDelete) throws OXException {
        clearFolder(folderId);
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        try {
            final Entry directoryEntry = dropboxAPI.metadata(toPath(folderId), 0, null, true, null);
            if (!directoryEntry.isDir) {
                throw DropboxExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final List<FileStorageFolder> list = new ArrayList<FileStorageFolder>();
            FileStorageFolder f = new DropboxFolder(userId).parseDirEntry(directoryEntry, accountAccess.getAccount().getDisplayName());
            list.add(f);
            String parentId;
            while (null != (parentId = f.getParentId())) {
                f = getFolder(parentId);
                list.add(f);
            }
            return list.toArray(new FileStorageFolder[list.size()]);
        } catch (final DropboxServerException e) {
            throw handleServerError(folderId, e);
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }

    }

    @Override
    public Quota getStorageQuota(final String folderId) throws OXException {
        return Type.STORAGE.getUnlimited();
    }

    @Override
    public Quota getFileQuota(final String folderId) throws OXException {
        return Type.FILE.getUnlimited();
    }

    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        final Quota[] ret = new Quota[types.length];
        for (int i = 0; i < types.length; i++) {
            ret[i] = types[i].getUnlimited();
        }
        return ret;
    }

}
