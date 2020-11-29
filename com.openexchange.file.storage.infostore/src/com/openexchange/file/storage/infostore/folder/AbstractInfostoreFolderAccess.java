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

package com.openexchange.file.storage.infostore.folder;

import static com.openexchange.java.Autoboxing.B;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.FileStorageCaseInsensitiveAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageResult;
import com.openexchange.file.storage.FolderStatsAware;
import com.openexchange.file.storage.PermissionAware;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.SearchableFolderNameFolderAccess;
import com.openexchange.file.storage.infostore.internal.Utils;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.TrashType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractInfostoreFolderAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public abstract class AbstractInfostoreFolderAccess implements FileStorageFolderAccess, PermissionAware, FolderStatsAware, FileStorageCaseInsensitiveAccess, SearchableFolderNameFolderAccess {

    /** The static identifier <code>9</code> of the <i>Infostore</i> root folder ("infostore / <code>FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID</code>) */
    public static final String INFOSTORE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);

    /** The static identifier <code>15</code> of the <i>Public Files</i> root folder ("public_infostore" / <code>FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID</code>) */
    public static final String PUBLIC_INFOSTORE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);

    /** The static identifier <code>10</code> of the <i>Shared Files</i> root folder ("userstore" / <code>FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID</code>) */
    public static final String USER_INFOSTORE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);

    /** The tree identifier used when getting folders from the underlying folder service (<code>OutlookFolderStorage#OUTLOOK_TREE_ID</code>) */
    protected static final String TREE_ID = "1";

    protected final ServerSession session;

    /**
     * Initializes a new {@link AbstractInfostoreFolderAccess}.
     *
     * @param session The session
     */
    protected AbstractInfostoreFolderAccess(ServerSession session) {
        super();
        this.session = session;
    }

    /**
     * Gets the utility to convert {@link FileStorageFolder} arguments to their {@link Folder} equivalents and vice-versa.
     *
     * @return The folder converter
     */
    protected FolderConverter getConverter() {
        return new FolderConverter();
    }

    /**
     * Gets the folder service, throwing an appropriate exception in case the service is absent.
     *
     * @return The folder service
     */
    protected abstract FolderService getFolderService() throws OXException;

    /**
     * Gets the infostore facade, throwing an appropriate exception in case the service is absent.
     *
     * @return The infostore facade
     */
    protected abstract InfostoreFacade getInfostore() throws OXException;

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
        return getFolderService().createFolder(getConverter().getFolder(toCreate), session, initDecorator()).getResponse();
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        return deleteFolder(folderId, false);
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        getFolderService().deleteFolder(TREE_ID, folderId, null, session, initDecorator().put("hardDelete", String.valueOf(hardDelete)));
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
    public DefaultFileStorageFolder getFolder(final String folderId) throws OXException {
        return getConverter().getStorageFolder(getFolderService().getFolder(TREE_ID, folderId, session, initDecorator()));
    }

    @Override
    public DefaultFileStorageFolder getPersonalFolder() throws OXException {
        return getDefaultFolder(PublicType.getInstance());
    }

    @Override
    public DefaultFileStorageFolder getTrashFolder() throws OXException {
        return getDefaultFolder(TrashType.getInstance());
    }

    @Override
    public DefaultFileStorageFolder[] getPublicFolders() throws OXException {
        UserizedFolder[] subfolders = getFolderService().getSubfolders(TREE_ID, PUBLIC_INFOSTORE_FOLDER_ID, true, session, initDecorator()).getResponse();
        return getConverter().getStorageFolders(subfolders);
    }

    @Override
    public DefaultFileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        UserizedFolder[] folders = getFolderService().getPath(TREE_ID, folderId, session, initDecorator()).getResponse();
        return getConverter().getStorageFolders(folders);
    }

    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        return INFOSTORE_FOLDER_ID.equals(folderId) ? getInfostore().getFileQuota(session) : getInfostore().getFileQuota(Utils.parseUnsignedLong(folderId), session);
    }

    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        return INFOSTORE_FOLDER_ID.equals(folderId) ? getInfostore().getStorageQuota(session) : getInfostore().getStorageQuota(Utils.parseUnsignedLong(folderId), session);
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
    public DefaultFileStorageFolder getRootFolder() throws OXException {
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
    public DefaultFileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        UserizedFolder[] subfolders = getFolderService().getSubfolders(TREE_ID, parentIdentifier, all, session, initDecorator()).getResponse();
        return getConverter().getStorageFolders(subfolders);
    }

    @Override
    public DefaultFileStorageFolder[] getUserSharedFolders() throws OXException {
        UserizedFolder[] folders = getFolderService().getUserSharedFolders(TREE_ID, InfostoreContentType.getInstance(), session, initDecorator()).getResponse();
        return getConverter().getStorageFolders(folders);
    }

    @Override
    public FileStorageFolder[] searchFolderByName(String query, String folderId, long date, boolean includeSubfolders, boolean all, int start, int end) throws OXException {
        List<UserizedFolder> result = getFolderService().searchFolderByName(TREE_ID, folderId, InfostoreContentType.getInstance(), query, date, includeSubfolders, all, start, end, session, initDecorator()).getResponse();
        return getConverter().getStorageFolders(result.toArray(new UserizedFolder[result.size()]));
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
    public FileStorageResult<String> moveFolder(boolean ignoreWarnings, String folderId, String newParentId, String newName) throws OXException {
        return moveFolder(folderId, newParentId, newName, null, ignoreWarnings);
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        return moveFolder(folderId, null, newName, null);
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        return updateFolder(identifier, toUpdate, false);
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate, boolean cascadePermissions) throws OXException {
        Folder parsedFolder = getConverter().getFolder(toUpdate);
        parsedFolder.setID(identifier);
        FolderServiceDecorator decorator = initDecorator();
        if (cascadePermissions) {
            decorator.put("cascadePermissions", Boolean.TRUE);
        }
        getFolderService().updateFolder(parsedFolder, null, session, decorator);
        return null != parsedFolder.getNewID() ? parsedFolder.getNewID() : identifier;
    }

    @Override
    public long getNumFiles(String folderId) throws OXException {
        return getInfostore().countDocuments(Long.parseLong(folderId), session);
    }

    @Override
    public long getTotalSize(String folderId) throws OXException {
        return getInfostore().getTotalSize(Long.parseLong(folderId), session);
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

    /**
     * Moves and/or renames a folder.
     *
     * @param folderId the ID of the folder to move
     * @param newParentId The ID of the target folder, or <code>null</code> to leave unchanged
     * @param newName The target name of the folder, or <code>null</code> to leave unchanged
     * @param decorator The decorator, or <code>null</code> if not used
     * @param ignoreWarnings true to force the folder move even if warnings are detected, false, otherwise
     * @return The FolderWarningsResponse object with the ID of the moved folder
     * @throws OXException
     */
    private FileStorageResult<String> moveFolder(final String folderId, final String newParentId, String newName, FolderServiceDecorator decorator, boolean ignoreWarnings) throws OXException {
        ParsedFolder folder = new ParsedFolder();
        folder.setTreeID(TREE_ID);
        folder.setID(folderId);
        if (null != newParentId) {
            folder.setParentID(newParentId);
        }
        if (null != newName) {
            folder.setName(newName);
        }
        FolderServiceDecorator usedDecorator = decorator != null ? decorator : initDecorator();
        Map<String, Boolean> properties = new HashMap<String, Boolean>();
        properties.put("ignoreWarnings", B(ignoreWarnings));
        usedDecorator.putProperties(properties);
        FolderResponse<Void> updateFolder = getFolderService().updateFolder(folder, null, session, usedDecorator);
        Collection<OXException> warnings = updateFolder.getWarnings();
        String resultFolderId = null == folder.getNewID() ? folderId : folder.getNewID();
        return FileStorageResult.newFileStorageResult(resultFolderId, warnings);
    }

    /**
     * Gets the user's default folder of a certain type.
     *
     * @param type The type to get the default folder for
     * @return The default folder
     */
    protected DefaultFileStorageFolder getDefaultFolder(com.openexchange.folderstorage.Type type) throws OXException {
        try {
            return getConverter().getStorageFolder(getFolderService().getDefaultFolder(
                session.getUser(), TREE_ID, InfostoreContentType.getInstance(), type, session, initDecorator()));
        } catch (OXException e) {
            if (FolderExceptionErrorMessage.NO_DEFAULT_FOLDER.equals(e)) {
                throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create(e);
            }
            throw e;
        }
    }

    /**
     * Creates and initializes a folder service decorator ready to use with calls to the underlying folder service.
     *
     * @return A new folder service decorator
     */
    protected FolderServiceDecorator initDecorator() {
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
