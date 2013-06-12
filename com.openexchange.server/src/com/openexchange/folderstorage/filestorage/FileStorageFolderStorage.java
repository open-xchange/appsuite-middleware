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

package com.openexchange.folderstorage.filestorage;

import static com.openexchange.folderstorage.filestorage.FileStorageFolderStorageServiceRegistry.getServiceRegistry;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.ServiceAware;
import com.openexchange.file.storage.WarningsAware;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StorageParametersUtility;
import com.openexchange.folderstorage.StoragePriority;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.filestorage.contentType.FileStorageContentType;
import com.openexchange.folderstorage.type.FileStorageType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.messaging.MessagingPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link FileStorageFolderStorage} - The file storage folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStorageFolderStorage implements FolderStorage {

    private static final String PARAM = FileStorageParameterConstants.PARAM_FILE_STORAGE_ACCESS;

    private static final class Key {

        static Key newInstance(final String accountId, final String serviceId) {
            return new Key(accountId, serviceId);
        }

        private final String accountId;

        private final String serviceId;

        private final int hash;

        private Key(final String accountId, final String serviceId) {
            super();
            this.accountId = accountId;
            this.serviceId = serviceId;

            final int prime = 31;
            int result = 1;
            result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
            result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (serviceId == null) {
                if (other.serviceId != null) {
                    return false;
                }
            } else if (!serviceId.equals(other.serviceId)) {
                return false;
            }
            if (accountId == null) {
                if (other.accountId != null) {
                    return false;
                }
            } else if (!accountId.equals(other.accountId)) {
                return false;
            }
            return true;
        }

    } // End of class Key

    /**
     * <code>"1"</code>
     */
    private static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    /**
     * <code>"9"</code>
     */
    private static final String INFOSTORE = Integer.toString(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);

    private static final String SERVICE_INFOSTORE = "infostore";

    /**
     * Initializes a new {@link FileStorageFolderStorage}.
     */
    public FileStorageFolderStorage() {
        super();
    }

    @Override
    public void clearCache(final int userId, final int contextId) {
        /*
         * Nothing to do...
         */
    }

    @Override
    public void restore(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        // TODO:
    }

    @Override
    public Folder prepareFolder(final String treeId, final Folder folder, final StorageParameters storageParameters) throws OXException {
        // TODO
        return folder;
    }

    @Override
    public void checkConsistency(final String treeId, final StorageParameters storageParameters) throws OXException {
        // Nothing to do
    }

    @Override
    public SortableId[] getVisibleFolders(final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws OXException {
        throw new UnsupportedOperationException("FileStorageFolderStorage.getVisibleSubfolders()");
    }

    private FileStorageAccountAccess getFileStorageAccessForAccount(final String serviceId, final String accountId, final Session session, final ConcurrentMap<Key, FileStorageAccountAccess> accesses) throws OXException {
        final Key key = Key.newInstance(accountId, serviceId);
        FileStorageAccountAccess accountAccess = accesses.get(key);
        if (null == accountAccess) {
            accountAccess =
                getServiceRegistry().getService(FileStorageServiceRegistry.class, true).getFileStorageService(serviceId).getAccountAccess(
                    accountId,
                    session);
            final FileStorageAccountAccess prev = accesses.putIfAbsent(key, accountAccess);
            if (null != prev) {
                accountAccess = prev;
            }
        }
        return accountAccess;
    }

    private void openFileStorageAccess(final FileStorageAccountAccess accountAccess) throws OXException {
        if (!accountAccess.isConnected()) {
            try {
                accountAccess.connect();
            } catch (final OXException e) {
                throw e;
            }
        }
    }

    @Override
    public ContentType[] getSupportedContentTypes() {
        return new ContentType[] { FileStorageContentType.getInstance() };
    }

    @Override
    public ContentType getDefaultContentType() {
        return FileStorageContentType.getInstance();
    }

    @Override
    public void commitTransaction(final StorageParameters params) throws OXException {
        @SuppressWarnings("unchecked") final ConcurrentMap<Key, FileStorageAccountAccess> accesses =
            (ConcurrentMap<Key, FileStorageAccountAccess>) params.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null != accesses) {
            try {
                final Collection<FileStorageAccountAccess> values = accesses.values();
                for (final FileStorageAccountAccess fsAccess : values) {
                    fsAccess.close();
                }
            } finally {
                params.putParameter(FileStorageFolderType.getInstance(), PARAM, null);
            }
        }
    }

    @Override
    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws OXException {
        @SuppressWarnings("unchecked") final ConcurrentMap<Key, FileStorageAccountAccess> accesses =
            (ConcurrentMap<Key, FileStorageAccountAccess>) storageParameters.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null == accesses) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(PARAM);
        }

        final FileStorageFolderIdentifier fsfi = new FileStorageFolderIdentifier(folder.getParentID());
        final String serviceId = fsfi.getServiceId();
        final String accountId = fsfi.getAccountId();
        final FileStorageAccountAccess accountAccess =
            getFileStorageAccessForAccount(serviceId, accountId, storageParameters.getSession(), accesses);
        openFileStorageAccess(accountAccess);

        final DefaultFileStorageFolder fsFolder = new DefaultFileStorageFolder();
        fsFolder.setExists(false);
        final String parentId = fsfi.getFolderId();
        fsFolder.setParentId(parentId);
        // Other
        fsFolder.setName(folder.getName());
        fsFolder.setSubscribed(folder.isSubscribed());
        // Permissions
        final Session session = storageParameters.getSession();
        if (null == session) {
            throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
        }
        final Permission[] permissions = folder.getPermissions();
        if (null != permissions && permissions.length > 0) {
            final List<FileStoragePermission> fsPermissions = new ArrayList<FileStoragePermission>(permissions.length);
            for (final Permission permission : permissions) {
                final FileStoragePermission fsPerm = DefaultFileStoragePermission.newInstance();
                fsPerm.setEntity(permission.getEntity());
                fsPerm.setAllPermissions(
                    permission.getFolderPermission(),
                    permission.getReadPermission(),
                    permission.getWritePermission(),
                    permission.getDeletePermission());
                fsPerm.setAdmin(permission.isAdmin());
                fsPerm.setGroup(permission.isGroup());
                fsPermissions.add(fsPerm);
            }
            fsFolder.setPermissions(fsPermissions);
        } else {
            if (FileStorageFolder.ROOT_FULLNAME.equals(parentId)) {
                final FileStoragePermission[] messagingPermissions = new FileStoragePermission[1];
                {
                    final FileStoragePermission fsPerm = DefaultFileStoragePermission.newInstance();
                    fsPerm.setEntity(session.getUserId());
                    fsPerm.setAllPermissions(
                        MessagingPermission.MAX_PERMISSION,
                        MessagingPermission.MAX_PERMISSION,
                        MessagingPermission.MAX_PERMISSION,
                        MessagingPermission.MAX_PERMISSION);
                    fsPerm.setAdmin(true);
                    fsPerm.setGroup(false);
                    messagingPermissions[0] = fsPerm;
                }
                fsFolder.setPermissions(Arrays.asList(messagingPermissions));
            } else {
                final FileStorageFolder parent = accountAccess.getFolderAccess().getFolder(parentId);
                final List<FileStoragePermission> parentPermissions = parent.getPermissions();
                final FileStoragePermission[] ffPermissions = new FileStoragePermission[parentPermissions.size()];
                int i = 0;
                for (final FileStoragePermission parentPerm : parentPermissions) {
                    final FileStoragePermission fsPerm = DefaultFileStoragePermission.newInstance();
                    fsPerm.setEntity(parentPerm.getEntity());
                    fsPerm.setAllPermissions(
                        parentPerm.getFolderPermission(),
                        parentPerm.getReadPermission(),
                        parentPerm.getWritePermission(),
                        parentPerm.getDeletePermission());
                    fsPerm.setAdmin(parentPerm.isAdmin());
                    fsPerm.setGroup(parentPerm.isGroup());
                    ffPermissions[i++] = fsPerm;
                }
                fsFolder.setPermissions(Arrays.asList(ffPermissions));
            }
        }

        final String fullName = accountAccess.getFolderAccess().createFolder(fsFolder);
        folder.setID(new FileStorageFolderIdentifier(serviceId, accountId, fullName).toString());
    }

    @Override
    public void clearFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        @SuppressWarnings("unchecked") final ConcurrentMap<Key, FileStorageAccountAccess> accesses =
            (ConcurrentMap<Key, FileStorageAccountAccess>) storageParameters.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null == accesses) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(PARAM);
        }

        final FileStorageFolderIdentifier fsfi = new FileStorageFolderIdentifier(folderId);
        final FileStorageAccountAccess accountAccess =
            getFileStorageAccessForAccount(fsfi.getServiceId(), fsfi.getAccountId(), storageParameters.getSession(), accesses);
        openFileStorageAccess(accountAccess);

        final String fullName = fsfi.getFolderId();
        final FileStorageFolderAccess folderAccess = accountAccess.getFolderAccess();
        folderAccess.clearFolder(fullName, true);
    }

    @Override
    public void deleteFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        @SuppressWarnings("unchecked") final ConcurrentMap<Key, FileStorageAccountAccess> accesses =
            (ConcurrentMap<Key, FileStorageAccountAccess>) storageParameters.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null == accesses) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(PARAM);
        }

        final FileStorageFolderIdentifier fsfi = new FileStorageFolderIdentifier(folderId);
        final FileStorageAccountAccess accountAccess =
            getFileStorageAccessForAccount(fsfi.getServiceId(), fsfi.getAccountId(), storageParameters.getSession(), accesses);
        openFileStorageAccess(accountAccess);

        final String fullName = fsfi.getFolderId();
        /*
         * Only backup if fullname does not denote trash (sub)folder
         */
        final FileStorageFolderAccess folderAccess = accountAccess.getFolderAccess();
        folderAccess.deleteFolder(fullName, true);
    }

    @Override
    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws OXException {
        if (!(contentType instanceof FileStorageContentType)) {
            throw FolderExceptionErrorMessage.UNKNOWN_CONTENT_TYPE.create(contentType.toString());
        }

        // TODO: Return primary InfoStore's default folder
        return FileStorageFolderIdentifier.getFQN(null, null, null);
    }

    @Override
    public Type getTypeByParent(final User user, final String treeId, final String parentId, final StorageParameters storageParameters) throws OXException {
        return FileStorageType.getInstance();
    }

    @Override
    public boolean containsForeignObjects(final User user, final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        @SuppressWarnings("unchecked") final ConcurrentMap<Key, FileStorageAccountAccess> accesses =
            (ConcurrentMap<Key, FileStorageAccountAccess>) storageParameters.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null == accesses) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(PARAM);
        }

        final FileStorageFolderIdentifier fsfi = new FileStorageFolderIdentifier(folderId);
        final String serviceId = fsfi.getServiceId();
        final String accountId = fsfi.getAccountId();
        final String fullname = fsfi.getFolderId();

        final FileStorageAccountAccess accountAccess =
            getFileStorageAccessForAccount(serviceId, accountId, storageParameters.getSession(), accesses);

        if (!FileStorageFolder.ROOT_FULLNAME.equals(fullname)) {
            openFileStorageAccess(accountAccess);
            if (!accountAccess.getFolderAccess().exists(fullname)) {
                throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                    fullname,
                    Integer.valueOf(accountId),
                    serviceId,
                    Integer.valueOf(storageParameters.getUserId()),
                    Integer.valueOf(storageParameters.getContextId()));
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        @SuppressWarnings("unchecked") final ConcurrentMap<Key, FileStorageAccountAccess> accesses =
            (ConcurrentMap<Key, FileStorageAccountAccess>) storageParameters.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null == accesses) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(PARAM);
        }

        final FileStorageFolderIdentifier fsfi = new FileStorageFolderIdentifier(folderId);
        final String serviceId = fsfi.getServiceId();
        final String accountId = fsfi.getAccountId();
        final String fullname = fsfi.getFolderId();

        final FileStorageAccountAccess accountAccess =
            getFileStorageAccessForAccount(serviceId, accountId, storageParameters.getSession(), accesses);

        if (FileStorageFolder.ROOT_FULLNAME.equals(fullname)) {
            return 0 == accountAccess.getRootFolder().getFileCount();
        }
        /*
         * Non-root folder
         */
        openFileStorageAccess(accountAccess);
        return 0 == accountAccess.getFolderAccess().getFolder(fullname).getFileCount();
    }

    @Override
    public void updateLastModified(final long lastModified, final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        // Nothing to do
    }

    @Override
    public List<Folder> getFolders(final String treeId, final List<String> folderIds, final StorageParameters storageParameters) throws OXException {
        return getFolders(treeId, folderIds, StorageType.WORKING, storageParameters);
    }

    @Override
    public List<Folder> getFolders(final String treeId, final List<String> folderIds, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        final List<Folder> ret = new ArrayList<Folder>(folderIds.size());
        for (final String folderId : folderIds) {
            ret.add(getFolder(treeId, folderId, storageType, storageParameters));
        }
        return ret;
    }

    @Override
    public Folder getFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        return getFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    @Override
    public Folder getFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        if (StorageType.BACKUP.equals(storageType)) {
            throw FolderExceptionErrorMessage.UNSUPPORTED_STORAGE_TYPE.create(storageType);
        }
        @SuppressWarnings("unchecked") final ConcurrentMap<Key, FileStorageAccountAccess> accesses =
            (ConcurrentMap<Key, FileStorageAccountAccess>) storageParameters.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null == accesses) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(PARAM);
        }

        final FileStorageFolderIdentifier fsfi = new FileStorageFolderIdentifier(folderId);
        final String serviceId = fsfi.getServiceId();
        final String accountId = fsfi.getAccountId();
        final FileStorageAccountAccess accountAccess =
            getFileStorageAccessForAccount(serviceId, accountId, storageParameters.getSession(), accesses);
        openFileStorageAccess(accountAccess);

        final String fullname = fsfi.getFolderId();

        final FileStorageFolderImpl retval;
        final boolean hasSubfolders;
        if ("".equals(fullname)) {
            final FileStorageFolder rootFolder = accountAccess.getFolderAccess().getRootFolder();
            retval = new FileStorageFolderImpl(rootFolder, accountId, serviceId);
            /*
             * Set proper name
             */
            final FileStorageServiceRegistry fssr =
                FileStorageFolderStorageServiceRegistry.getServiceRegistry().getService(FileStorageServiceRegistry.class, true);
            final FileStorageService fsService = fssr.getFileStorageService(serviceId);
            final FileStorageAccount fsAccount = fsService.getAccountManager().getAccount(accountId, storageParameters.getSession());
            retval.setName(fsAccount.getDisplayName());
            hasSubfolders = rootFolder.hasSubfolders();
        } else {
            final FileStorageFolder fsFolder = accountAccess.getFolderAccess().getFolder(fullname);
            retval = new FileStorageFolderImpl(fsFolder, accountId, serviceId);
            hasSubfolders = fsFolder.hasSubfolders();
        }
        retval.setTreeID(treeId);
        /*
         * Check if denoted parent can hold default folders like Trash, Sent, etc.
         */
        if (!"".equals(fullname) && !"INBOX".equals(fullname)) {
            /*
             * Denoted parent is not capable to hold default folders. Therefore output as it is.
             */
            final List<FileStorageFolder> children = Arrays.asList(accountAccess.getFolderAccess().getSubfolders(fullname, true));
            Collections.sort(children, new SimpleFileStorageFolderComparator(storageParameters.getUser().getLocale()));
            final String[] subfolderIds = new String[children.size()];
            int i = 0;
            for (final FileStorageFolder child : children) {
                subfolderIds[i++] = FileStorageFolderIdentifier.getFQN(serviceId, accountId, child.getId());
            }
            retval.setSubfolderIDs(subfolderIds);
        } else {
            /*
             * This one needs sorting. Just pass null or an empty array.
             */
            retval.setSubfolderIDs(hasSubfolders ? null : new String[0]);
        }

        return retval;
    }

    @Override
    public FolderType getFolderType() {
        return FileStorageFolderType.getInstance();
    }

    @Override
    public SortableId[] getSubfolders(final String treeId, final String parentId, final StorageParameters storageParameters) throws OXException {
        final ServerSession session;
        {
            final Session s = storageParameters.getSession();
            if (null == s) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            if (s instanceof ServerSession) {
                session = (ServerSession) s;
            } else {
                session = ServerSessionAdapter.valueOf(s);
            }
        }

        if (REAL_TREE_ID.equals(treeId) ? PRIVATE_FOLDER_ID.equals(parentId) : INFOSTORE.equals(parentId)) {
            /*
             * Get all user file storage accounts
             */
            final List<FileStorageAccount> accounts = new ArrayList<FileStorageAccount>(8);
            {
                final FileStorageServiceRegistry registry = getServiceRegistry().getService(FileStorageServiceRegistry.class, true);
                final List<FileStorageService> allServices = registry.getAllServices();
                for (final FileStorageService fsService : allServices) {
                    /*
                     * Check if file storage service provides a root folder
                     */
                    List<FileStorageAccount> userAccounts = null;
                    if (fsService instanceof AccountAware) {
                        userAccounts = ((AccountAware) fsService).getAccounts(session);
                    }
                    if (null == userAccounts) {
                        userAccounts = fsService.getAccountManager().getAccounts(session);
                    }
                    for (final FileStorageAccount userAccount : userAccounts) {
                        if (SERVICE_INFOSTORE.equals(userAccount.getId()) || FileStorageAccount.DEFAULT_ID.equals(userAccount.getId())) {
                            continue;
                        }
                        final FileStorageAccountAccess accountAccess =
                            userAccount.getFileStorageService().getAccountAccess(userAccount.getId(), session);
                        accountAccess.connect();
                        try {
                            final FileStorageFolder rootFolder = accountAccess.getFolderAccess().getRootFolder();
                            if (null != rootFolder) {
                                accounts.add(userAccount);
                            }
                        } finally {
                            accountAccess.close();
                        }
                    }
                }
            }
            if (accounts.isEmpty()) {
                return new SortableId[0];
            }
            final int size = accounts.size();
            if (size > 1) {
                /*
                 * Sort by name
                 */
                Collections.sort(accounts, new FileStorageAccountComparator(session.getUser().getLocale()));
            }
            /*-
             * TODO:
             * 1. Check for file storage permission; e.g. session.getUserConfiguration().isMultipleMailAccounts()
             *    Add primary only if not enabled
             * 2. Strip Unified-FileStorage account from obtained list
             */
            final List<SortableId> list = new ArrayList<SortableId>(size);
            for (int j = 0; j < size; j++) {
                final FileStorageAccount acc = accounts.get(j);
                final String serviceId;
                if (acc instanceof ServiceAware) {
                    serviceId = ((ServiceAware) acc).getServiceId();
                } else {
                    final FileStorageService tmp = acc.getFileStorageService();
                    serviceId = null == tmp ? null : tmp.getId();
                }
                list.add(new FileStorageId(FileStorageFolderIdentifier.getFQN(serviceId, acc.getId(), ""), j, null));
            }
            return list.toArray(new SortableId[list.size()]);
        }

        // A file storage folder denoted by fullname
        @SuppressWarnings("unchecked") final ConcurrentMap<Key, FileStorageAccountAccess> accesses =
            (ConcurrentMap<Key, FileStorageAccountAccess>) storageParameters.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null == accesses) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(PARAM);
        }

        final FileStorageFolderIdentifier fsfi = new FileStorageFolderIdentifier(parentId);
        final String serviceId = fsfi.getServiceId();
        final String accountId = fsfi.getAccountId();
        final FileStorageAccountAccess accountAccess =
            getFileStorageAccessForAccount(serviceId, accountId, storageParameters.getSession(), accesses);
        openFileStorageAccess(accountAccess);

        final String fullname = fsfi.getFolderId();

        final List<FileStorageFolder> children = Arrays.asList(accountAccess.getFolderAccess().getSubfolders(fullname, true));
        if (accountAccess instanceof WarningsAware) {
            addWarnings(storageParameters, (WarningsAware) accountAccess);
        }
        /*
         * Sort
         */
        Collections.sort(children, new SimpleFileStorageFolderComparator(storageParameters.getUser().getLocale()));
        final List<SortableId> list = new ArrayList<SortableId>(children.size());
        final int size = children.size();
        for (int j = 0; j < size; j++) {
            final FileStorageFolder cur = children.get(j);
            list.add(new FileStorageId(FileStorageFolderIdentifier.getFQN(serviceId, accountId, cur.getId()), j, cur.getName()));
        }
        return list.toArray(new SortableId[list.size()]);
    }

    @Override
    public void rollback(final StorageParameters params) {
        @SuppressWarnings("unchecked") final ConcurrentMap<Key, FileStorageAccountAccess> accesses =
            (ConcurrentMap<Key, FileStorageAccountAccess>) params.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null != accesses) {
            try {
                final Collection<FileStorageAccountAccess> values = accesses.values();
                for (final FileStorageAccountAccess access : values) {
                    access.close();
                }
            } finally {
                params.putParameter(FileStorageFolderType.getInstance(), PARAM, null);
            }
        }
    }

    @Override
    public boolean startTransaction(final StorageParameters parameters, final boolean modify) throws OXException {
        /*
         * Ensure session is present
         */
        if (null == parameters.getSession()) {
            throw FolderExceptionErrorMessage.MISSING_SESSION.create();
        }
        /*
         * Put map
         */
        return parameters.putParameterIfAbsent(
            FileStorageFolderType.getInstance(),
            PARAM,
            new ConcurrentHashMap<Key, FileStorageAccountAccess>());
    }

    @Override
    public StoragePriority getStoragePriority() {
        return StoragePriority.NORMAL;
    }

    @Override
    public boolean containsFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        return containsFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    @Override
    public boolean containsFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        if (StorageType.BACKUP.equals(storageType)) {
            return false;
        }
        @SuppressWarnings("unchecked") final ConcurrentMap<Key, FileStorageAccountAccess> accesses =
            (ConcurrentMap<Key, FileStorageAccountAccess>) storageParameters.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null == accesses) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(PARAM);
        }

        final FileStorageFolderIdentifier fsfi = new FileStorageFolderIdentifier(folderId);
        final String serviceId = fsfi.getServiceId();
        final String accountId = fsfi.getAccountId();
        final FileStorageAccountAccess accountAccess =
            getFileStorageAccessForAccount(serviceId, accountId, storageParameters.getSession(), accesses);
        openFileStorageAccess(accountAccess);

        return accountAccess.getFolderAccess().exists(fsfi.getFolderId());
    }

    @Override
    public String[] getDeletedFolderIDs(final String treeId, final Date timeStamp, final StorageParameters storageParameters) throws OXException {
        return new String[0];
    }

    @Override
    public String[] getModifiedFolderIDs(final String treeId, final Date timeStamp, final ContentType[] includeContentTypes, final StorageParameters storageParameters) throws OXException {
        if (null == includeContentTypes || includeContentTypes.length == 0) {
            return new String[0];
        }
        final List<String> ret = new ArrayList<String>();
        final Set<ContentType> supported = new HashSet<ContentType>(Arrays.asList(getSupportedContentTypes()));
        for (final ContentType includeContentType : includeContentTypes) {
            if (supported.contains(includeContentType)) {
                final SortableId[] subfolders = getSubfolders(FolderStorage.REAL_TREE_ID, PRIVATE_FOLDER_ID, storageParameters);
                for (final SortableId sortableId : subfolders) {
                    ret.add(sortableId.getId());
                }
            }
        }
        return ret.toArray(new String[ret.size()]);
    }

    @Override
    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws OXException {
        @SuppressWarnings("unchecked") final ConcurrentMap<Key, FileStorageAccountAccess> accesses =
            (ConcurrentMap<Key, FileStorageAccountAccess>) storageParameters.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null == accesses) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(PARAM);
        }

        final FileStorageFolderIdentifier fsfi = new FileStorageFolderIdentifier(folder.getID());
        final String serviceId = fsfi.getServiceId();
        final String accountId = fsfi.getAccountId();
        String id = fsfi.getFolderId();
        final FileStorageAccountAccess accountAccess =
            getFileStorageAccessForAccount(serviceId, accountId, storageParameters.getSession(), accesses);
        openFileStorageAccess(accountAccess);

        final DefaultFileStorageFolder fsFolder = new DefaultFileStorageFolder();
        fsFolder.setExists(true);
        // Identifier
        fsFolder.setId(id);
        // TODO: fsFolder.setAccountId(accountId);
        // Parent
        final FileStorageFolderIdentifier pfi;
        if (null != folder.getParentID()) {
            pfi = new FileStorageFolderIdentifier(folder.getParentID());
            fsFolder.setParentId(pfi.getFolderId());
            // TODO: fsFolder.setParentAccountId(parentArg.getAccountId());
        } else {
            pfi = null;
        }
        // Name
        if (null != folder.getName()) {
            fsFolder.setName(folder.getName());
        }
        // Subscribed
        fsFolder.setSubscribed(folder.isSubscribed());
        // Permissions
        FileStoragePermission[] fsPermissions = null;
        {
            final Permission[] permissions = folder.getPermissions();
            if (null != permissions && permissions.length > 0) {
                fsPermissions = new FileStoragePermission[permissions.length];
                final Session session = storageParameters.getSession();
                if (null == session) {
                    throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
                }
                for (int i = 0; i < permissions.length; i++) {
                    final Permission permission = permissions[i];
                    final FileStoragePermission dmp = DefaultFileStoragePermission.newInstance();
                    dmp.setEntity(permission.getEntity());
                    dmp.setAllPermissions(
                        permission.getFolderPermission(),
                        permission.getReadPermission(),
                        permission.getWritePermission(),
                        permission.getDeletePermission());
                    dmp.setAdmin(permission.isAdmin());
                    dmp.setGroup(permission.isGroup());
                    fsPermissions[i] = dmp;
                }
                fsFolder.setPermissions(Arrays.asList(fsPermissions));
            }
        }
        /*
         * Load storage version
         */
        final String oldParent;
        final String oldName;
        {
            final FileStorageFolder storageVersion = accountAccess.getFolderAccess().getFolder(id);
            oldParent = storageVersion.getParentId();
            oldName = storageVersion.getName();
        }

        // Here we go------------------------------------------------------------------------
        // TODO: Allow differing service identifiers in provided parent ID?

        final String newName = fsFolder.getName();

        boolean movePerformed = false;
        {
            /*
             * Check if a move shall be performed
             */
            final String newParent = fsFolder.getParentId();
            if (newParent != null) {
                final String parentAccountID = pfi.getAccountId();
                if (accountId.equals(parentAccountID)) {
                    /*
                     * Move to another parent in the same account
                     */
                    if (!newParent.equals(oldParent)) {
                        /*
                         * Check for possible duplicate folder
                         */
                        final boolean rename = (null != newName) && !newName.equals(oldName);
                        check4DuplicateFolder(accountAccess, newParent, rename ? newName : oldName);
                        /*
                         * Perform move operation
                         */
                        String movedFolder = accountAccess.getFolderAccess().moveFolder(id, newParent);
                        if (rename) {
                            /*
                             * Perform rename
                             */
                            movedFolder = accountAccess.getFolderAccess().renameFolder(movedFolder, newName);
                        }
                        folder.setID(FileStorageFolderIdentifier.getFQN(serviceId, accountId, movedFolder));
                        movePerformed = true;
                    }
                } else {
                    // Move to another account
                    final FileStorageAccountAccess otherAccess =
                        getFileStorageAccessForAccount(serviceId, parentAccountID, storageParameters.getSession(), accesses);
                    openFileStorageAccess(otherAccess);
                    try {
                        // Check if parent folder exists
                        final FileStorageFolder p = otherAccess.getFolderAccess().getFolder(newParent);
                        // Check permission on new parent
                        final FileStoragePermission ownPermission = p.getOwnPermission();
                        if (ownPermission.getFolderPermission() < FileStoragePermission.CREATE_SUB_FOLDERS) {
                            throw FileStorageExceptionCodes.NO_CREATE_ACCESS.create(newParent);
                        }
                        // Check for duplicate
                        check4DuplicateFolder(otherAccess, newParent, null == newName ? oldName : newName);
                        // Copy
                        final String destFullname =
                            fullCopy(
                                accountAccess,
                                id,
                                otherAccess,
                                newParent,
                                storageParameters.getUserId(),
                                p.getCapabilities().contains(FileStorageFolder.CAPABILITY_PERMISSIONS));
                        // Delete source
                        accountAccess.getFolderAccess().deleteFolder(id, true);
                        // Perform other updates
                        otherAccess.getFolderAccess().updateFolder(destFullname, fsFolder);
                    } finally {
                        otherAccess.close();
                    }
                }
            }
        }
        /*
         * Check if a rename shall be performed
         */
        if (!movePerformed && newName != null && !newName.equals(oldName)) {
            id = accountAccess.getFolderAccess().renameFolder(id, newName);
            folder.setID(FileStorageFolderIdentifier.getFQN(serviceId, accountId, id));
        }
        /*
         * Handle update of permission or subscription
         */
        accountAccess.getFolderAccess().updateFolder(id, fsFolder);
        /*
         * Is hand-down?
         */
        if ((null != fsPermissions) && StorageParametersUtility.isHandDownPermissions(storageParameters)) {
            handDown(accountId, id, fsPermissions, accountAccess);
        }
    }

    private static void handDown(final String accountId, final String parentId, final FileStoragePermission[] fsPermissions, final FileStorageAccountAccess accountAccess) throws OXException {
        final FileStorageFolder[] subfolders = accountAccess.getFolderAccess().getSubfolders(parentId, true);
        for (FileStorageFolder subfolder : subfolders) {
            final DefaultFileStorageFolder fsFolder = new DefaultFileStorageFolder();
            fsFolder.setExists(true);
            // Full name
            final String id = subfolder.getId();
            fsFolder.setId(id);
            fsFolder.setPermissions(Arrays.asList(fsPermissions));
            accountAccess.getFolderAccess().updateFolder(id, fsFolder);
            // Recursive
            handDown(accountId, id, fsPermissions, accountAccess);
        }
    }

    private void check4DuplicateFolder(final FileStorageAccountAccess accountAccess, final String parentId, final String name2check) throws OXException {
        final FileStorageFolder[] subfolders = accountAccess.getFolderAccess().getSubfolders(parentId, true);
        for (final FileStorageFolder subfolder : subfolders) {
            if (name2check.equals(subfolder.getName())) {
                throw FileStorageExceptionCodes.DUPLICATE_FOLDER.create(name2check, parentId);
            }
        }
    }

    private static String fullCopy(final FileStorageAccountAccess srcAccess, final String srcFullname, final FileStorageAccountAccess destAccess, final String destParent, final int user, final boolean hasPermissions) throws OXException {
        // Create folder
        final FileStorageFolder source = srcAccess.getFolderAccess().getFolder(srcFullname);
        final DefaultFileStorageFolder mfd = new DefaultFileStorageFolder();
        mfd.setName(source.getName());
        mfd.setParentId(destParent);
        mfd.setSubscribed(source.isSubscribed());
        if (hasPermissions) {
            // Copy permissions
            final List<FileStoragePermission> perms = source.getPermissions();
            for (final FileStoragePermission perm : perms) {
                mfd.addPermission((FileStoragePermission) perm.clone());
            }
        }
        final String destFullname = destAccess.getFolderAccess().createFolder(mfd);
        // TODO: Copy files
        /*
         * final List<FileStorageMessage> msgs = srcAccess.getMessageAccess().getAllMessages( srcFullname, null,
         * FileStorageField.RECEIVED_DATE, OrderDirection.ASC, new FileStorageField[] { FileStorageField.FULL }); final
         * FileStorageMessageAccess destMessageStorage = destAccess.getMessageAccess();
         */
        // Append files to destination account
        /* final String[] mailIds = */// destMessageStorage.appendMessages(destFullname, msgs.toArray(new FileStorageMessage[msgs.size()]));
        /*-
         *
        // Ensure flags
        final String[] arr = new String[1];
        for (int i = 0; i < msgs.length; i++) {
            final MailMessage m = msgs[i];
            final String mailId = mailIds[i];
            if (null != m && null != mailId) {
                arr[0] = mailId;
                // System flags
                destMessageStorage.updateMessageFlags(destFullname, arr, m.getFlags(), true);
                // Color label
                if (m.containsColorLabel() && m.getColorLabel() != MailMessage.COLOR_LABEL_NONE) {
                    destMessageStorage.updateMessageColorLabel(destFullname, arr, m.getColorLabel());
                }
            }
        }
         */
        // Iterate subfolders
        final FileStorageFolder[] tmp = srcAccess.getFolderAccess().getSubfolders(srcFullname, true);
        for (final FileStorageFolder element : tmp) {
            fullCopy(srcAccess, element.getId(), destAccess, destFullname, user, hasPermissions);
        }
        return destFullname;
    }

    private static final class FileStorageAccountComparator implements Comparator<FileStorageAccount> {

        private final Collator collator;

        FileStorageAccountComparator(final Locale locale) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        @Override
        public int compare(final FileStorageAccount o1, final FileStorageAccount o2) {
            /*-
             *
            if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o1.getMailProtocol())) {
                if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
                    return 0;
                }
                return -1;
            } else if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
                return 1;
            }
            if (0 == o1.getId()) {
                if (0 == o2.getId()) {
                    return 0;
                }
                return -1;
            } else if (0 == o2.getId()) {
                return 1;
            }
             */
            return collator.compare(o1.getDisplayName(), o2.getDisplayName());
        }

    } // End of FileStorageAccountComparator

    private static final class SimpleFileStorageFolderComparator implements Comparator<FileStorageFolder> {

        private final Collator collator;

        SimpleFileStorageFolderComparator(final Locale locale) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        @Override
        public int compare(final FileStorageFolder o1, final FileStorageFolder o2) {
            return collator.compare(o1.getName(), o2.getName());
        }
    } // End of SimpleFileStorageFolderComparator

    private static final class FileStorageFolderComparator implements Comparator<FileStorageFolder> {

        private final Map<String, Integer> indexMap;

        private final Collator collator;

        private final Integer na;

        FileStorageFolderComparator(final String[] names, final Locale locale) {
            super();
            indexMap = new HashMap<String, Integer>(names.length);
            for (int i = 0; i < names.length; i++) {
                indexMap.put(names[i], Integer.valueOf(i));
            }
            na = Integer.valueOf(names.length);
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        private Integer getNumberOf(final String name) {
            final Integer ret = indexMap.get(name);
            if (null == ret) {
                return na;
            }
            return ret;
        }

        @Override
        public int compare(final FileStorageFolder o1, final FileStorageFolder o2) {
            if (o1.isDefaultFolder()) {
                if (o2.isDefaultFolder()) {
                    return getNumberOf(o1.getId()).compareTo(getNumberOf(o2.getId()));
                }
                return -1;
            }
            if (o2.isDefaultFolder()) {
                return 1;
            }
            return collator.compare(o1.getName(), o2.getName());
        }
    } // End of FileStorageFolderComparator

    private static void addWarnings(final StorageParameters storageParameters, final WarningsAware warningsAware) {
        final List<OXException> list = warningsAware.getAndFlushWarnings();
        if (null != list && !list.isEmpty()) {
            for (OXException warning : list) {
                storageParameters.addWarning(warning);
            }
        }
    }

}
