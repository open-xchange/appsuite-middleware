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

package com.openexchange.folderstorage.filestorage;

import java.util.List;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CacheAware;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.TypeAware;
import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SystemContentType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.database.getfolder.SystemInfostoreFolder;
import com.openexchange.folderstorage.filestorage.contentType.DocumentsContentType;
import com.openexchange.folderstorage.filestorage.contentType.FileStorageContentType;
import com.openexchange.folderstorage.filestorage.contentType.MusicContentType;
import com.openexchange.folderstorage.filestorage.contentType.PicturesContentType;
import com.openexchange.folderstorage.filestorage.contentType.PublicContentType;
import com.openexchange.folderstorage.filestorage.contentType.TemplatesContentType;
import com.openexchange.folderstorage.filestorage.contentType.TrashContentType;
import com.openexchange.folderstorage.filestorage.contentType.VideosContentType;
import com.openexchange.folderstorage.type.FileStorageType;
import com.openexchange.folderstorage.type.SystemType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.PutIfAbsent;
import com.openexchange.session.Session;

/**
 * {@link FileStorageFolderImpl} - A file storage folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStorageFolderImpl extends AbstractFolder {

    private static final long serialVersionUID = 6445442372690458946L;

    /**
     * <code>"9"</code>
     */
    private static final String INFOSTORE = Integer.toString(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);

    /**
     * <code>"10"</code>
     */
    private static final String INFOSTORE_USER = Integer.toString(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);

    /**
     * <code>"15"</code>
     */
    private static final String INFOSTORE_PUBLIC = Integer.toString(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);

    /**
     * The mail folder content type.
     */
    public static enum FileStorageDefaultFolderType {
        NONE(FileStorageContentType.getInstance(), 0),
        ROOT(SystemContentType.getInstance(), 0),
        HOME_DIRECTORY(FileStorageContentType.getInstance(), 8), // FolderObject.FILE
        PUBLIC_FOLDER(PublicContentType.getInstance(), 15),
        TRASH(TrashContentType.getInstance(), 16),
        PICTURES(PicturesContentType.getInstance(), 20),
        DOCUMENTS(DocumentsContentType.getInstance(), 21),
        MUSIC(MusicContentType.getInstance(), 22),
        VIDEOS(VideosContentType.getInstance(), 23),
        TEMPLATES(TemplatesContentType.getInstance(), 24);

        private final ContentType contentType;
        private final int type;

        private FileStorageDefaultFolderType(final ContentType contentType, final int type) {
            this.contentType = contentType;
            this.type = type;
        }

        /**
         * Gets the content type associated with this mail folder type.
         *
         * @return The content type
         */
        public ContentType getContentType() {
            return contentType;
        }

        /**
         * Gets the type.
         *
         * @return The type
         */
        public int getType() {
            return type;
        }

    }

    private boolean cacheable;
    private final FileStorageDefaultFolderType defaultFolderType;

    /**
     * The private folder identifier.
     */
    private static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    /**
     * Initializes a new {@link FileStorageFolderImpl} from given messaging folder.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor.
     *
     * @param fsFolder The underlying file storage folder
     * @param accountId The full-qualified file storage account ID
     * @param session The requesting users session
     * @param altNames If the client requested alternative names
     */
    public FileStorageFolderImpl(final FileStorageFolder fsFolder, final String accountId, final Session session, final boolean altNames) {
        this(fsFolder, accountId, showPersonalBelowInfoStore(session, altNames));
    }

    /**
     * Initializes a new {@link FileStorageFolderImpl} from given messaging folder.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor.
     *
     * @param fsFolder The underlying file storage folder
     * @param accountId The full-qualified file storage account ID
     * @param userId ID of the user requesting the folder
     * @param contextId The context ID
     * @param altNames If the client requested alternative names
     */
    public FileStorageFolderImpl(FileStorageFolder fsFolder, String accountId, int userId, int contextId, boolean altNames) {
        this(fsFolder, accountId, showPersonalBelowInfoStore(userId, contextId, altNames));
    }

    /**
     * Initializes a new {@link FileStorageFolderImpl} from given messaging folder.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor.
     *
     * @param fsFolder The underlying file storage folder
     * @param accountId The full-qualified file storage account ID
     * @param showPersonalBelowInfoStore If the users personal FS folder shall be shown below folder 9 instead below folder 10
     */
    private FileStorageFolderImpl(FileStorageFolder fsFolder, String accountId, boolean showPersonalBelowInfoStore) {
        super();
        id = fsFolder.getId();
        name = fsFolder.getName();
        this.accountId = accountId;
        if (fsFolder.isRootFolder()) {
            parent = PRIVATE_FOLDER_ID;
            defaultFolderType = FileStorageDefaultFolderType.NONE;
        } else {
            String parentId = null;
            if (fsFolder instanceof TypeAware) {
                final FileStorageFolderType folderType = ((TypeAware) fsFolder).getType();
                if (FileStorageFolderType.HOME_DIRECTORY.equals(folderType)) {
                    defaultFolderType = FileStorageDefaultFolderType.HOME_DIRECTORY;
                    if (showPersonalBelowInfoStore) {
                        parentId = INFOSTORE;
                    } else {
                        parentId = INFOSTORE_USER;
                    }
                } else if (FileStorageFolderType.PUBLIC_FOLDER.equals(folderType)) {
                    defaultFolderType = FileStorageDefaultFolderType.PUBLIC_FOLDER;
                    parentId = INFOSTORE_PUBLIC;
                } else if (FileStorageFolderType.TRASH_FOLDER.equals(folderType)) {
                    defaultFolderType = FileStorageDefaultFolderType.TRASH;
                } else if (FileStorageFolderType.PICTURES_FOLDER.equals(folderType)) {
                    defaultFolderType = FileStorageDefaultFolderType.PICTURES;
                } else if (FileStorageFolderType.DOCUMENTS_FOLDER.equals(folderType)) {
                    defaultFolderType = FileStorageDefaultFolderType.DOCUMENTS;
                } else if (FileStorageFolderType.MUSIC_FOLDER.equals(folderType)) {
                    defaultFolderType = FileStorageDefaultFolderType.MUSIC;
                } else if (FileStorageFolderType.VIDEOS_FOLDER.equals(folderType)) {
                    defaultFolderType = FileStorageDefaultFolderType.VIDEOS;
                } else if (FileStorageFolderType.TEMPLATES_FOLDER.equals(folderType)) {
                    defaultFolderType = FileStorageDefaultFolderType.TEMPLATES;
                }
                else {
                    defaultFolderType = FileStorageDefaultFolderType.NONE;
                }
            } else {
                defaultFolderType = FileStorageDefaultFolderType.NONE;
            }
            parent = null != parentId ? parentId : fsFolder.getParentId();
        }
        {
            final List<FileStoragePermission> fsPermissions = fsFolder.getPermissions();
            final int size = fsPermissions.size();
            permissions = new Permission[size];
            for (int i = 0; i < size; i++) {
                FileStoragePermissionImpl permissionImpl = new FileStoragePermissionImpl(fsPermissions.get(i));
                if (permissionImpl.isAdmin() && !permissionImpl.isGroup()) {
                    createdBy = permissionImpl.getEntity();
                }
                permissions[i] = permissionImpl;
            }
        }
        type = SystemType.getInstance();
        subscribed = fsFolder.isSubscribed();
        subscribedSubfolders = fsFolder.hasSubscribedSubfolders();
        // capabilities = parseCaps(messagingFolder.getCapabilities());
        deefault = fsFolder.isDefaultFolder();
        total = fsFolder.getFileCount();
        defaultType = deefault ? FileStorageContentType.getInstance().getModule() : 0;
        if (fsFolder instanceof CacheAware) {
            cacheable = !fsFolder.isDefaultFolder() && ((CacheAware) fsFolder).cacheable();
        } else {
            cacheable = !fsFolder.isDefaultFolder();
        }
        meta = fsFolder.getMeta();
        supportedCapabilities = fsFolder.getCapabilities();
        lastModified = fsFolder.getLastModifiedDate();
        creationDate = fsFolder.getCreationDate();
        createdBy = fsFolder.getCreatedBy();
        modifiedBy = fsFolder.getModifiedBy();
    }

    private static boolean showPersonalBelowInfoStore(final Session session, final boolean altNames) {
        if (!altNames) {
            return false;
        }
        final String paramName = "com.openexchange.folderstorage.outlook.showPersonalBelowInfoStore";
        final Boolean tmp = (Boolean) session.getParameter(paramName);
        if (null != tmp) {
            return tmp.booleanValue();
        }

        final boolean b = showPersonalBelowInfoStore(session.getUserId(), session.getContextId(), altNames);
        if (session instanceof PutIfAbsent) {
            ((PutIfAbsent) session).setParameterIfAbsent(paramName, b);
        } else {
            session.setParameter(paramName, b);
        }
        return b;
    }

    private static boolean showPersonalBelowInfoStore(int userId, int contextId, boolean altNames) {
        if (!altNames) {
            return false;
        }
        final String paramName = "com.openexchange.folderstorage.outlook.showPersonalBelowInfoStore";
        final ConfigViewFactory configViewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null == configViewFactory) {
            return false;
        }
        try {
            final ConfigView view = configViewFactory.getView(userId, contextId);
            final Boolean b = view.opt(paramName, boolean.class, Boolean.FALSE);
            return b.booleanValue();
        } catch (final OXException e) {
            org.slf4j.LoggerFactory.getLogger(SystemInfostoreFolder.class).warn("", e);
            return false;
        }
    }

    @Override
    public Object clone() {
        final FileStorageFolderImpl clone = (FileStorageFolderImpl) super.clone();
        clone.cacheable = cacheable;
        return clone;
    }

    @Override
    public boolean isCacheable() {
        return cacheable;
    }

    @Override
    public Type getType() {
        return FileStorageType.getInstance();
    }

    @Override
    public void setContentType(final ContentType contentType) {
        // Nothing to do
    }

    @Override
    public void setType(final Type type) {
        // Nothing to do
    }

    @Override
    public ContentType getContentType() {
        return defaultFolderType.getContentType();
    }

    @Override
    public int getDefaultType() {
        return defaultFolderType.getType();
    }

    @Override
    public void setDefaultType(final int defaultType) {
        // Nothing to do
    }

    @Override
    public boolean isGlobalID() {
        return false;
    }

}
