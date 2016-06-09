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

package com.openexchange.file.storage.infostore.internal;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FolderStatsAware;
import com.openexchange.file.storage.MediaFolderAwareFolderAccess;
import com.openexchange.file.storage.PermissionAware;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.infostore.folder.FolderParser;
import com.openexchange.file.storage.infostore.folder.FolderWriter;
import com.openexchange.file.storage.infostore.folder.ParsedFolder;
import com.openexchange.file.storage.infostore.osgi.Services;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.type.DocumentsType;
import com.openexchange.folderstorage.type.MusicType;
import com.openexchange.folderstorage.type.PicturesType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.TemplatesType;
import com.openexchange.folderstorage.type.TrashType;
import com.openexchange.folderstorage.type.VideosType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link InfostoreFolderAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreFolderAccess implements FileStorageFolderAccess, MediaFolderAwareFolderAccess, PermissionAware, FolderStatsAware {

    private static final String INFOSTORE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);
    private static final String TREE_ID = "1";

    private final ServerSession session;
    private final InfostoreFacade infostore;

    /**
     * Initializes a new {@link InfostoreFolderAccess}.
     *
     * @param session The session
     * @param infostore A reference to the underlying infostore facade
     */
    public InfostoreFolderAccess(ServerSession session, InfostoreFacade infostore) {
        super();
        this.session = session;
        this.infostore = infostore;
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        clearFolder(folderId, false);
    }

    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        getFolderService().clearFolder(TREE_ID, folderId, session);
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        return getFolderService().createFolder(FolderParser.parseFolder(toCreate), session, initDecorator()).getResponse();
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        return deleteFolder(folderId, false);
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        getFolderService().deleteFolder(TREE_ID, folderId, null, session, initDecorator().put("hardDelete", String.valueOf(hardDelete))).getResponse();
        return folderId;
    }

    @Override
    public boolean exists(String folderId) throws OXException {
        try {
            getFolderService().getFolder(TREE_ID, folderId, session, initDecorator());
            return true;
        } catch (OXException e) {
            return false;
        }
    }

    @Override
    public FileStorageFolder getFolder(final String folderId) throws OXException {
        return FolderWriter.writeFolder(getFolderService().getFolder(TREE_ID, folderId, session, initDecorator()));
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        return getDefaultFolder(PublicType.getInstance());
    }

    @Override
    public FileStorageFolder getTrashFolder() throws OXException {
        return getDefaultFolder(TrashType.getInstance());
    }

    @Override
    public FileStorageFolder getPicturesFolder() throws OXException {
        return getDefaultFolder(PicturesType.getInstance());
    }

    @Override
    public FileStorageFolder getDocumentsFolder() throws OXException {
        return getDefaultFolder(DocumentsType.getInstance());
    }

    @Override
    public FileStorageFolder getTemplatesFolder() throws OXException {
        return getDefaultFolder(TemplatesType.getInstance());
    }

    @Override
    public FileStorageFolder getMusicFolder() throws OXException {
        return getDefaultFolder(MusicType.getInstance());
    }

    @Override
    public FileStorageFolder getVideosFolder() throws OXException {
        return getDefaultFolder(VideosType.getInstance());
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        UserizedFolder[] subfolders = getFolderService().getSubfolders(TREE_ID, "15", true, session, initDecorator()).getResponse();
        return FolderWriter.writeFolders(subfolders);
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        UserizedFolder[] folders = getFolderService().getPath(TREE_ID, folderId, session, initDecorator()).getResponse();
        return FolderWriter.writeFolders(folders);
    }

    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        return infostore.getFileQuota(session);
    }

    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        return infostore.getStorageQuota(session);
    }

    @Override
    public Quota[] getQuotas(String folder, Type[] types) throws OXException {
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
        try {
            return getFolder(INFOSTORE_FOLDER_ID);
        } catch (OXException e) {
            if (FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.equals(e)) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        UserizedFolder[] subfolders = getFolderService().getSubfolders(TREE_ID, parentIdentifier, all, session, initDecorator()).getResponse();
        return FolderWriter.writeFolders(subfolders);
    }

    @Override
    public FileStorageFolder[] getUserSharedFolders() throws OXException {
        UserizedFolder[] folders = getFolderService().getUserSharedFolders(TREE_ID, InfostoreContentType.getInstance(), session, initDecorator()).getResponse();
        return FolderWriter.writeFolders(folders);
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        return moveFolder(folderId, newParentId, null);
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        return moveFolder(folderId, newParentId, newName, null);
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        return moveFolder(folderId, null, newName, null);
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        Folder parsedFolder = FolderParser.parseFolder(toUpdate);
        parsedFolder.setID(identifier);
        getFolderService().updateFolder(parsedFolder, null, session, initDecorator()).getResponse();
        return null != parsedFolder.getNewID() ? parsedFolder.getNewID() : identifier;
    }

    @Override
    public long getNumFiles(String folderId) throws OXException {
        return infostore.countDocuments(Long.parseLong(folderId), session);
    }

    @Override
    public long getTotalSize(String folderId) throws OXException {
        return infostore.getTotalSize(Long.parseLong(folderId), session);
    }

    /**
     * Moves and/or renames a folder.
     *
     * @param folderId the ID of the folder to move
     * @param newParentId The ID of the target folder, or <code>null</code> to leave unchanged
     * @param newName The target name of the folder, or <code>null</code> to leave unchanged
     * @param decorator The decorator, or <code>null</code> if not used
     * @return The ID of the moved folder
     * @throws OXException
     */
    private String moveFolder(final String folderId, final String newParentId, String newName, FolderServiceDecorator decorator) throws OXException {
        ParsedFolder folder = new ParsedFolder();
        folder.setTreeID(TREE_ID);
        folder.setID(folderId);
        if (null != newParentId) {
            folder.setParentID(newParentId);
        }
        if (null != newName) {
            folder.setName(newName);
        }
        getFolderService().updateFolder(folder, null, session, initDecorator());
        return null == folder.getNewID() ? folderId : folder.getNewID();
    }

    private FileStorageFolder getDefaultFolder(com.openexchange.folderstorage.Type type) throws OXException {
        try {
            return FolderWriter.writeFolder(getFolderService().getDefaultFolder(
                session.getUser(), TREE_ID, InfostoreContentType.getInstance(), type, session, initDecorator()));
        } catch (OXException e) {
            if (FolderExceptionErrorMessage.NO_DEFAULT_FOLDER.equals(e)) {
                throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create(e);
            }
            throw e;
        }
    }

    /**
     * Gets the folder service, throwing an appropriate exception in case the service is absent.
     *
     * @return The folder service
     */
    private FolderService getFolderService() throws OXException {
        FolderService folderService = Services.getService(FolderService.class);
        if (null == folderService) {
            throw ServiceExceptionCode.absentService(FolderService.class);
        }
        return folderService;
    }

    /**
     * Creates and initializes a folder service decorator ready to use with calls to the underlying folder service.
     *
     * @return A new folder service decorator
     */
    private FolderServiceDecorator initDecorator() {
        FolderServiceDecorator decorator = new FolderServiceDecorator();
        Object connection = session.getParameter(Connection.class.getName());
        if (null != connection) {
            decorator.put(Connection.class.getName(), connection);
        }
        decorator.put("altNames", Boolean.TRUE.toString());
        decorator.setLocale(session.getUser().getLocale());
        return decorator;
    }

}
