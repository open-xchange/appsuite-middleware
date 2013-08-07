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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.WarningsAware;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
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
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link FileStorageFolderStorage} - The file storage folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStorageFolderStorage implements FolderStorage {

    private static final String PARAM = FileStorageParameterConstants.PARAM_ID_BASED_FILE_STORAGE_ACCESS;

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

    private IDBasedFolderAccess getFolderAccess(final StorageParameters storageParameters) throws OXException {
        final IDBasedFolderAccess folderAccess = storageParameters.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null == folderAccess) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(PARAM);
        }
        return folderAccess;
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

    @Override
    public ContentType[] getSupportedContentTypes() {
        return new ContentType[] { FileStorageContentType.getInstance() };
    }

    @Override
    public ContentType getDefaultContentType() {
        return FileStorageContentType.getInstance();
    }

    @Override
    public void commitTransaction(final StorageParameters storageParameters) throws OXException {
        final IDBasedFolderAccess folderAccess = storageParameters.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null != folderAccess) {
            try {
                folderAccess.commit();
            } finally {
                storageParameters.putParameter(FileStorageFolderType.getInstance(), PARAM, null);
            }
        }
    }

    @Override
    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws OXException {
        final IDBasedFolderAccess folderAccess = getFolderAccess(storageParameters);

        final FileStorageFolderIdentifier fsfi = new FileStorageFolderIdentifier(folder.getParentID());

        final DefaultFileStorageFolder fsFolder = new DefaultFileStorageFolder();
        fsFolder.setExists(false);
        final String parentId = fsfi.getFolderId();
        fsFolder.setParentId(folder.getParentID());
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
                final FileStorageFolder parent = folderAccess.getFolder(folder.getParentID());
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

        final String fullName = folderAccess.createFolder(fsFolder);
        folder.setID(fullName);
    }

    @Override
    public void clearFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        final IDBasedFolderAccess folderAccess = getFolderAccess(storageParameters);

        folderAccess.clearFolder(folderId, true);
    }

    @Override
    public void deleteFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        final IDBasedFolderAccess folderAccess = getFolderAccess(storageParameters);
        folderAccess.deleteFolder(folderId, true);
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
        final IDBasedFolderAccess folderAccess = getFolderAccess(storageParameters);

        if (!folderAccess.exists(folderId)) {
            final FileStorageFolderIdentifier fsfi = new FileStorageFolderIdentifier(folderId);
            throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                fsfi.getFolderId(),
                Integer.valueOf(fsfi.getAccountId()),
                fsfi.getServiceId(),
                Integer.valueOf(storageParameters.getUserId()),
                Integer.valueOf(storageParameters.getContextId()));
        }

        return false;
    }

    @Override
    public boolean isEmpty(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        final IDBasedFolderAccess folderAccess = getFolderAccess(storageParameters);

        return 0 == folderAccess.getFolder(folderId).getFileCount();
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
        final IDBasedFolderAccess folderAccess = getFolderAccess(storageParameters);

        final FileStorageFolderImpl retval;
        final boolean hasSubfolders;
        {
            final FileStorageFolder fsFolder = folderAccess.getFolder(folderId);
            final FileStorageFolderIdentifier fsfi = new FileStorageFolderIdentifier(folderId);
            retval = new FileStorageFolderImpl(fsFolder, fsfi.getAccountId(), fsfi.getServiceId());
            hasSubfolders = fsFolder.hasSubfolders();
        }
        retval.setTreeID(treeId);
        retval.setSubfolderIDs(hasSubfolders ? null : new String[0]);
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

        final IDBasedFolderAccess folderAccess = storageParameters.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null == folderAccess) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(PARAM);
        }

        final boolean isRealTree = REAL_TREE_ID.equals(treeId);
        if (isRealTree ? PRIVATE_FOLDER_ID.equals(parentId) : INFOSTORE.equals(parentId)) {
            /*-
             * TODO:
             * 1. Check for file storage permission; e.g. session.getUserPermissionBits().isMultipleMailAccounts()
             *    Add primary only if not enabled
             * 2. Strip Unified-FileStorage account from obtained list
             */

            final List<FileStorageFolder> rootFolders = new ArrayList<FileStorageFolder>(Arrays.asList(folderAccess.getRootFolders(session.getUser().getLocale())));
            if (isRealTree) {
                for (final Iterator<FileStorageFolder> it = rootFolders.iterator(); it.hasNext();) {
                    if (INFOSTORE.equals(it.next().getId())) {
                        it.remove();
                    }
                }
            }

            final int size = rootFolders.size();
            if (size <= 0) {
                return new SortableId[0];
            }
            final List<SortableId> list = new ArrayList<SortableId>(size);
            for (int j = 0; j < size; j++) {
                list.add(new FileStorageId(rootFolders.get(j).getId(), j, null));
            }
            return list.toArray(new SortableId[list.size()]);
        }

        // A file storage folder denoted by full name
        final List<FileStorageFolder> children = Arrays.asList(folderAccess.getSubfolders(parentId, true));
        /*
         * Sort
         */
        Collections.sort(children, new SimpleFileStorageFolderComparator(storageParameters.getUser().getLocale()));
        final List<SortableId> list = new ArrayList<SortableId>(children.size());
        final int size = children.size();
        for (int j = 0; j < size; j++) {
            final FileStorageFolder cur = children.get(j);
            list.add(new FileStorageId(cur.getId(), j, cur.getName()));
        }
        return list.toArray(new SortableId[list.size()]);
    }

    @Override
    public void rollback(final StorageParameters storageParameters) {
        final IDBasedFolderAccess folderAccess = storageParameters.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null != folderAccess) {
            try {
                folderAccess.rollback();
            } catch (final Exception e) {
                // Ignore
            } finally {
                storageParameters.putParameter(FileStorageFolderType.getInstance(), PARAM, null);
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
        final IDBasedFolderAccessFactory factory = getServiceRegistry().getService(IDBasedFolderAccessFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(IDBasedFolderAccessFactory.class.getName());
        }
        return parameters.putParameterIfAbsent(FileStorageFolderType.getInstance(), PARAM, factory.createAccess(parameters.getSession()));
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
        final IDBasedFolderAccess folderAccess = getFolderAccess(storageParameters);

        return folderAccess.exists(folderId);
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
        final IDBasedFolderAccess folderAccess = getFolderAccess(storageParameters);

        final FileStorageFolderIdentifier fsfi = new FileStorageFolderIdentifier(folder.getID());
        final String serviceId = fsfi.getServiceId();
        final String accountId = fsfi.getAccountId();
        String id = fsfi.getFolderId();

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
            final FileStorageFolder storageVersion = folderAccess.getFolder(id);
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
                        check4DuplicateFolder(folderAccess, newParent, rename ? newName : oldName);
                        /*
                         * Perform move operation
                         */
                        String movedFolder = folderAccess.moveFolder(id, newParent);
                        if (rename) {
                            /*
                             * Perform rename
                             */
                            movedFolder = folderAccess.renameFolder(movedFolder, newName);
                        }
                        folder.setID(FileStorageFolderIdentifier.getFQN(serviceId, accountId, movedFolder));
                        movePerformed = true;
                    }
                } else {
                    // Move to another account
                    // Check if parent folder exists
                    final FileStorageFolder p = folderAccess.getFolder(newParent);
                    // Check permission on new parent
                    final FileStoragePermission ownPermission = p.getOwnPermission();
                    if (ownPermission.getFolderPermission() < FileStoragePermission.CREATE_SUB_FOLDERS) {
                        throw FileStorageExceptionCodes.NO_CREATE_ACCESS.create(newParent);
                    }
                    // Check for duplicate
                    check4DuplicateFolder(folderAccess, newParent, null == newName ? oldName : newName);
                    // Copy
                    final String destFullname = fullCopy(
                        folderAccess,
                        id,
                        newParent,
                        storageParameters.getUserId(),
                        p.getCapabilities().contains(FileStorageFolder.CAPABILITY_PERMISSIONS));
                    // Delete source
                    folderAccess.deleteFolder(id, true);
                    // Perform other updates
                    folderAccess.updateFolder(destFullname, fsFolder);
                }
            }
        }
        /*
         * Check if a rename shall be performed
         */
        if (!movePerformed && newName != null && !newName.equals(oldName)) {
            id = folderAccess.renameFolder(id, newName);
            folder.setID(FileStorageFolderIdentifier.getFQN(serviceId, accountId, id));
        }
        /*
         * Handle update of permission or subscription
         */
        folderAccess.updateFolder(id, fsFolder);
        /*
         * Is hand-down?
         */
        if ((null != fsPermissions) && StorageParametersUtility.isHandDownPermissions(storageParameters)) {
            handDown(accountId, id, fsPermissions, folderAccess);
        }
    }

    private static void handDown(final String accountId, final String parentId, final FileStoragePermission[] fsPermissions, final IDBasedFolderAccess folderAccess) throws OXException {
        final FileStorageFolder[] subfolders = folderAccess.getSubfolders(parentId, true);
        for (final FileStorageFolder subfolder : subfolders) {
            final DefaultFileStorageFolder fsFolder = new DefaultFileStorageFolder();
            fsFolder.setExists(true);
            // Full name
            final String id = subfolder.getId();
            fsFolder.setId(id);
            fsFolder.setPermissions(Arrays.asList(fsPermissions));
            folderAccess.updateFolder(id, fsFolder);
            // Recursive
            handDown(accountId, id, fsPermissions, folderAccess);
        }
    }

    private void check4DuplicateFolder(final IDBasedFolderAccess folderAccess, final String parentId, final String name2check) throws OXException {
        final FileStorageFolder[] subfolders = folderAccess.getSubfolders(parentId, true);
        for (final FileStorageFolder subfolder : subfolders) {
            if (name2check.equals(subfolder.getName())) {
                throw FileStorageExceptionCodes.DUPLICATE_FOLDER.create(name2check, parentId);
            }
        }
    }

    private static String fullCopy(final IDBasedFolderAccess folderAccess, final String srcFullname, final String destParent, final int user, final boolean hasPermissions) throws OXException {
        // Create folder
        final FileStorageFolder source = folderAccess.getFolder(srcFullname);
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
        final String destFullname = folderAccess.createFolder(mfd);
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
        final FileStorageFolder[] tmp = folderAccess.getSubfolders(srcFullname, true);
        for (final FileStorageFolder element : tmp) {
            fullCopy(folderAccess, element.getId(), destFullname, user, hasPermissions);
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
            for (final OXException warning : list) {
                storageParameters.addWarning(warning);
            }
        }
    }

}
