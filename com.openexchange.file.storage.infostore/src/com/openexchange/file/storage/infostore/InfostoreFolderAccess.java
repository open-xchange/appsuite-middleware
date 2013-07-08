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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.file.storage.infostore;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.infostore.folder.FolderParser;
import com.openexchange.file.storage.infostore.folder.FolderWriter;
import com.openexchange.file.storage.infostore.folder.ParsedFolder;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link InfostoreFolderAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreFolderAccess implements FileStorageFolderAccess {

    private static final String INFOSTORE_FOLDER_ID = "9";

    private static final String REAL_TREE_ID = FolderStorage.REAL_TREE_ID;

    private final ServerSession session;
    private final InfostoreFacade infostore;

    /**
     * Initializes a new {@link InfostoreFolderAccess}.
     * @param session
     */
    public InfostoreFolderAccess(final ServerSession session, final InfostoreFacade infostore) {
        super();
        this.session = session;
        this.infostore = infostore;
    }

    @Override
    public void clearFolder(final String folderId) throws OXException {
        final FolderService service = Services.getService(FolderService.class);
        service.clearFolder(REAL_TREE_ID, folderId, session);
    }

    @Override
    public void clearFolder(final String folderId, final boolean hardDelete) throws OXException {
        final FolderService service = Services.getService(FolderService.class);
        service.clearFolder(REAL_TREE_ID, folderId, session);
    }

    @Override
    public String createFolder(final FileStorageFolder toCreate) throws OXException {
        final FolderService service = Services.getService(FolderService.class);
        final FolderResponse<String> response = service.createFolder(FolderParser.parseFolder(toCreate), session, null);
        return response.getResponse();
    }

    @Override
    public String deleteFolder(final String folderId) throws OXException {
        final FolderService service = Services.getService(FolderService.class);
        service.deleteFolder(REAL_TREE_ID, folderId, null, session);
        return folderId;
    }

    @Override
    public String deleteFolder(final String folderId, final boolean hardDelete) throws OXException {
        final FolderService service = Services.getService(FolderService.class);
        service.deleteFolder(REAL_TREE_ID, folderId, null, session);
        return folderId;
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        try {
            final FolderService service = Services.getService(FolderService.class);
            service.getFolder(REAL_TREE_ID, folderId, session, null);
            return true;
        } catch (final OXException e) {
            return false;
        }
    }

    @Override
    public FileStorageFolder getFolder(final String folderId) throws OXException {
        final FolderService service = Services.getService(FolderService.class);
        return FolderWriter.writeFolder(service.getFolder(REAL_TREE_ID, folderId, session, null));
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        final FolderService service = Services.getService(FolderService.class);
        return FolderWriter.writeFolder(service.getDefaultFolder(
            UserStorage.getStorageUser(session.getUserId(), session.getContext()),
            REAL_TREE_ID,
            FolderParser.getContentType(),
            session,
            null));
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        final FolderService service = Services.getService(FolderService.class);
        final UserizedFolder[] subfolders = service.getSubfolders(REAL_TREE_ID, "15", true, session, null).getResponse();
        final FileStorageFolder[] ret = new FileStorageFolder[subfolders.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = FolderWriter.writeFolder(subfolders[i]);
        }
        return ret;
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        final FolderService service = Services.getService(FolderService.class);
        final UserizedFolder[] folders = service.getPath(REAL_TREE_ID, folderId, session, null).getResponse();
        final FileStorageFolder[] ret = new FileStorageFolder[folders.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = FolderWriter.writeFolder(folders[i]);
        }
        return ret;
    }

    @Override
    public Quota getFileQuota(final String folderId) throws OXException {
        return infostore.getFileQuota(session);
    }

    @Override
    public Quota getStorageQuota(final String folderId) throws OXException {
        return infostore.getStorageQuota(session);
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
                throw new UnsupportedOperationException("unknown type: " + types[i]);
            }
        }
        return quotas;
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        return getFolder(INFOSTORE_FOLDER_ID);
    }

    @Override
    public FileStorageFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws OXException {
        final FolderService service = Services.getService(FolderService.class);
        final UserizedFolder[] subfolders = service.getSubfolders(REAL_TREE_ID, parentIdentifier, all, session, null).getResponse();
        final FileStorageFolder[] ret = new FileStorageFolder[subfolders.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = FolderWriter.writeFolder(subfolders[i]);
        }
        return ret;
    }

    @Override
    public String moveFolder(final String folderId, final String newParentId) throws OXException {
        final FolderService service = Services.getService(FolderService.class);
        final ParsedFolder folder = new ParsedFolder();
        folder.setID(folderId);
        folder.setParentID(newParentId);
        folder.setTreeID(REAL_TREE_ID);
        service.updateFolder(folder, null, session, null);
        return folder.getNewID() == null ? folderId : folder.getNewID();
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        final FolderService service = Services.getService(FolderService.class);
        final ParsedFolder folder = new ParsedFolder();
        folder.setID(folderId);
        folder.setName(newName);
        folder.setTreeID(REAL_TREE_ID);
        service.updateFolder(folder, null, session, null);
        return folder.getNewID() == null ? folderId : folder.getNewID();
    }

    @Override
    public String updateFolder(final String identifier, final FileStorageFolder toUpdate) throws OXException {
        final FolderService service = Services.getService(FolderService.class);
        final Folder parsedFolder = FolderParser.parseFolder(toUpdate);
        service.updateFolder(parsedFolder, null, session, null);
        return parsedFolder.getNewID();
    }

}
