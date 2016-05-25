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

import java.sql.Connection;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageAccounts;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.WarningsAware;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SetterAwareFolder;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StorageParametersUtility;
import com.openexchange.folderstorage.StoragePriority;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.filestorage.contentType.FileStorageContentType;
import com.openexchange.folderstorage.tx.TransactionManager;
import com.openexchange.folderstorage.type.FileStorageType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Collators;
import com.openexchange.messaging.MessagingPermission;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link FileStorageFolderStorage} - The file storage folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStorageFolderStorage implements FolderStorage {

    /**
     * <code>"infostore"</code>
     */
    private static final String INFOSTORE_ACCOUNT_ID = com.openexchange.file.storage.composition.FileID.INFOSTORE_ACCOUNT_ID;

    private static final String PARAM = FileStorageParameterConstants.PARAM_ID_BASED_FOLDER_ACCESS;

    /**
     * <code>"1"</code>
     */
    private static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    /**
     * <code>"9"</code>
     */
    private static final String INFOSTORE = Integer.toString(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);

    // --------------------------------------------------------------------------------------------------------------------------- //

    private final ServiceLookup services;

    /**
     * Initializes a new {@link FileStorageFolderStorage}.
     *
     * @param services A service lookup reference
     */
    public FileStorageFolderStorage(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public boolean startTransaction(StorageParameters parameters, boolean modify) throws OXException {
        /*
         * initialize ID based file access if necessary
         */
        if (null == parameters.getParameter(getFolderType(), PARAM)) {
            /*
             * ensure the session is present
             */
            if (null == parameters.getSession()) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create();
            }
            /*
             * create access via factory
             */
            IDBasedFolderAccessFactory factory = services.getService(IDBasedFolderAccessFactory.class);
            if (null == factory) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(IDBasedFolderAccessFactory.class.getName());
            }
            IDBasedFolderAccess folderAccess = factory.createAccess(parameters.getSession());
            if (parameters.putParameterIfAbsent(getFolderType(), PARAM, folderAccess)) {
                /*
                 * enqueue in managed transaction if possible, otherwise signal that we started the transaction ourselves
                 */
                if (false == TransactionManager.isManagedTransaction(parameters)) {
                    return true;
                }
                TransactionManager transactionManager = TransactionManager.getTransactionManager(parameters);
                Session session = parameters.getSession();
                session.setParameter(Connection.class.getName() + '@' + Thread.currentThread().getId(), transactionManager.getConnection());
                transactionManager.transactionStarted(this);
            }
        }
        return false;
    }

    @Override
    public void rollback(StorageParameters storageParameters) {
        IDBasedFolderAccess folderAccess = storageParameters.getParameter(getFolderType(), PARAM);
        if (null != folderAccess) {
            try {
                folderAccess.rollback();
            } catch (Exception e) {
                // Ignore
                org.slf4j.LoggerFactory.getLogger(FileStorageFolderStorage.class).warn("Unexpected error during rollback: {}", e.getMessage(), e);
            } finally {
                if (null != storageParameters.putParameter(getFolderType(), PARAM, null)) {
                    Session session = storageParameters.getSession();
                    if (null != session && session.containsParameter(Connection.class.getName() + '@' + Thread.currentThread().getId())) {
                        session.setParameter(Connection.class.getName() + '@' + Thread.currentThread().getId(), null);
                    }
                }
            }
            addWarnings(storageParameters, folderAccess);
        }
    }

    @Override
    public void commitTransaction(StorageParameters storageParameters) throws OXException {
        IDBasedFolderAccess folderAccess = storageParameters.getParameter(getFolderType(), PARAM);
        if (null != folderAccess) {
            try {
                folderAccess.commit();
            } finally {
                if (null != storageParameters.putParameter(getFolderType(), PARAM, null)) {
                    Session session = storageParameters.getSession();
                    if (null != session && session.containsParameter(Connection.class.getName() + '@' + Thread.currentThread().getId())) {
                        session.setParameter(Connection.class.getName() + '@' + Thread.currentThread().getId(), null);
                    }
                }
            }
            addWarnings(storageParameters, folderAccess);
        }
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
    public SortableId[] getUserSharedFolders(String treeId, ContentType contentType, StorageParameters storageParameters) throws OXException {
        if (false == FileStorageContentType.class.isInstance(contentType)) {
            throw FolderExceptionErrorMessage.UNKNOWN_CONTENT_TYPE.create(contentType.toString());
        }
        IDBasedFolderAccess folderAccess = getFolderAccess(storageParameters);
        FileStorageFolder[] sharedFolders = folderAccess.getUserSharedFolders();
        if (null == sharedFolders) {
            return null;
        }
        SortableId[] sortableIds = new SortableId[sharedFolders.length];
        for (int i = 0; i < sharedFolders.length; i++) {
            sortableIds[i] = new FileStorageId(sharedFolders[i].getId(), i, sharedFolders[i].getName());
        }
        return sortableIds;
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
    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws OXException {
        final IDBasedFolderAccess folderAccess = getFolderAccess(storageParameters);
        final DefaultFileStorageFolder fsFolder = new DefaultFileStorageFolder();
        fsFolder.setExists(false);
        fsFolder.setParentId(folder.getParentID());
        // Other
        fsFolder.setName(folder.getName());
        fsFolder.setSubscribed(folder.isSubscribed());
        // Permissions
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
            if (FileStorageFolder.ROOT_FULLNAME.equals(folder.getParentID())) {
                final FileStoragePermission[] messagingPermissions = new FileStoragePermission[1];
                {
                    final FileStoragePermission fsPerm = DefaultFileStoragePermission.newInstance();
                    fsPerm.setEntity(storageParameters.getUserId());
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
        boolean hardDelete = StorageParametersUtility.getBoolParameter("hardDelete", storageParameters);
        getFolderAccess(storageParameters).clearFolder(folderId, hardDelete);
    }

    @Override
    public void deleteFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        boolean hardDelete = StorageParametersUtility.getBoolParameter("hardDelete", storageParameters);
        getFolderAccess(storageParameters).deleteFolder(folderId, hardDelete);
    }

    @Override
    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws OXException {
        if (!(contentType instanceof FileStorageContentType)) {
            throw FolderExceptionErrorMessage.UNKNOWN_CONTENT_TYPE.create(contentType.toString());
        }
        // TODO: Return primary InfoStore's default folder
        return INFOSTORE;
    }

    @Override
    public Type getTypeByParent(final User user, final String treeId, final String parentId, final StorageParameters storageParameters) throws OXException {
        return FileStorageType.getInstance();
    }

    @Override
    public boolean containsForeignObjects(final User user, final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        final IDBasedFolderAccess folderAccess = getFolderAccess(storageParameters);

        if (!folderAccess.exists(folderId)) {
            FolderID folderID = new FolderID(folderId);
            throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                folderID.getFolderId(),
                Integer.valueOf(folderID.getAccountId()),
                folderID.getService(),
                Integer.valueOf(storageParameters.getUserId()),
                Integer.valueOf(storageParameters.getContextId()));
        }

        return false;
    }

    @Override
    public boolean isEmpty(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        return 1 > getFolderAccess(storageParameters).getFolder(folderId).getFileCount();
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
        FolderID fid = new FolderID(folderId);
        FileStorageFolder fsFolder = getFolderAccess(storageParameters).getFolder(fid);
        boolean altNames = StorageParametersUtility.getBoolParameter("altNames", storageParameters);
        String accountID = FileStorageAccounts.getQualifiedID(fid.getService(), fid.getAccountId());
        Session session = storageParameters.getSession();
        FileStorageFolderImpl retval;
        if (session == null) {
            retval = new FileStorageFolderImpl(fsFolder, accountID, storageParameters.getUserId(), storageParameters.getContextId(), altNames);
        } else {
            retval = new FileStorageFolderImpl(fsFolder, accountID, session, altNames);
        }
        boolean hasSubfolders = fsFolder.hasSubfolders();
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
            Locale userLocale = null;
            if (storageParameters.getSession() != null) {
                User user = ServerSessionAdapter.valueOf(storageParameters.getSession()).getUser();
                if (user != null) {
                    userLocale = user.getLocale();
                }
            }
            if (null == userLocale) {
                userLocale = storageParameters.getUser().getLocale();
            }

            FileStorageFolder[] rootFolders = folderAccess.getRootFolders(userLocale);
            int size = rootFolders.length;
            if (size <= 0) {
                return new SortableId[0];
            }

            List<SortableId> list = new ArrayList<SortableId>(size);
            if (isRealTree) {
                int index = 0;
                for (int j = 0; j < size; j++) {
                    String id = rootFolders[j].getId();
                    if ((id.length() != 0) && !INFOSTORE_ACCOUNT_ID.equals(new FolderID(id).getAccountId())) {
                        list.add(new FileStorageId(id, index++, null));
                    }
                }
            } else {
                for (int j = 0; j < size; j++) {
                    list.add(new FileStorageId(rootFolders[j].getId(), j, null));
                }
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
    public void updateFolder(Folder folder, StorageParameters storageParameters) throws OXException {
        /*
         * convert supplied folder & determine required updates in storage
         */
        IDBasedFolderAccess folderAccess = getFolderAccess(storageParameters);
        DefaultFileStorageFolder folderToUpdate = getFileStorageFolder(folder);
        FileStorageFolder originalFolder = folderAccess.getFolder(folder.getID());
        boolean move = null != folderToUpdate.getParentId() && false == originalFolder.getParentId().equals(folderToUpdate.getParentId());
        boolean rename = null != folderToUpdate.getName() && false == originalFolder.getName().equals(folderToUpdate.getName());
        boolean permissions = null != folderToUpdate.getPermissions() && 0 < folderToUpdate.getPermissions().size() &&
            false == folderToUpdate.getPermissions().equals(originalFolder.getPermissions());
        /*
         * perform move and/or rename operation
         */
        if (move) {
            boolean ignoreWarnings = StorageParametersUtility.getBoolParameter("ignoreWarnings", storageParameters);
            String newName = rename ? folderToUpdate.getName() : null;
            String newID = folderAccess.moveFolder(folderToUpdate.getId(), folderToUpdate.getParentId(), newName, ignoreWarnings);
            folderToUpdate.setId(newID);
        } else if (rename) {
            String newID = folderAccess.renameFolder(folderToUpdate.getId(), folderToUpdate.getName());
            folderToUpdate.setId(newID);
        }
        /*
         * update permissions separately if needed
         */
        if (permissions) {
            String newID = folderAccess.updateFolder(folderToUpdate.getId(), folderToUpdate);
            folderToUpdate.setId(newID);
            if (StorageParametersUtility.isHandDownPermissions(storageParameters)) {
                handDown(folderToUpdate.getId(), folderToUpdate.getPermissions(), folderAccess);
            }
        }
        /*
         * take over updated identifiers in passed folder reference
         */
        folder.setID(folderToUpdate.getId());
        folder.setParentID(folderToUpdate.getParentId());
        folder.setLastModified(folderToUpdate.getLastModifiedDate());
    }

    private static void handDown(final String parentId, final List<FileStoragePermission> fsPermissions, final IDBasedFolderAccess folderAccess) throws OXException {
        final FileStorageFolder[] subfolders = folderAccess.getSubfolders(parentId, true);
        for (final FileStorageFolder subfolder : subfolders) {
            final DefaultFileStorageFolder fsFolder = new DefaultFileStorageFolder();
            fsFolder.setExists(true);
            // Full name
            final String id = subfolder.getId();
            fsFolder.setId(id);
            fsFolder.setPermissions(fsPermissions);
            folderAccess.updateFolder(id, fsFolder);
            // Recursive
            handDown(id, fsPermissions, folderAccess);
        }
    }

    /**
     * Gets the ID based folder access reference from the supplied storage parameters, throwing an appropriate exception in case it is
     * absent.
     *
     * @param storageParameters The storage parameters to get the folder access from
     * @return The folder access
     */
    private static IDBasedFolderAccess getFolderAccess(StorageParameters storageParameters) throws OXException {
        IDBasedFolderAccess folderAccess = storageParameters.getParameter(FileStorageFolderType.getInstance(), PARAM);
        if (null == folderAccess) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(PARAM);
        }
        return folderAccess;
    }

    private static final class SimpleFileStorageFolderComparator implements Comparator<FileStorageFolder> {

        private final Collator collator;

        SimpleFileStorageFolderComparator(final Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale);
        }

        @Override
        public int compare(final FileStorageFolder o1, final FileStorageFolder o2) {
            return collator.compare(o1.getName(), o2.getName());
        }
    } // End of SimpleFileStorageFolderComparator

    /**
     * Create a file storage folder equivalent to the supplied folder.
     *
     * @param folder the folder to create the file storage folder for
     * @return The file storage folder
     */
    private static DefaultFileStorageFolder getFileStorageFolder(Folder folder) {
        DefaultFileStorageFolder fileStorageFolder = new DefaultFileStorageFolder();
        fileStorageFolder.setExists(true);
        fileStorageFolder.setId(folder.getID());
        fileStorageFolder.setParentId(folder.getParentID());
        fileStorageFolder.setName(folder.getName());
        if (false == SetterAwareFolder.class.isInstance(fileStorageFolder) || ((SetterAwareFolder) folder).containsSubscribed()) {
            fileStorageFolder.setSubscribed(folder.isSubscribed());
        }
        fileStorageFolder.setPermissions(getfFileStoragePermissions(folder.getPermissions()));
        fileStorageFolder.setCreatedBy(folder.getCreatedBy());
        fileStorageFolder.setModifiedBy(folder.getModifiedBy());
        return fileStorageFolder;
    }

    /**
     * Create file storage permissions equivalent to the supplied permissions.
     *
     * @param permissions The permissions to create the file storage permissions for
     * @return The file storage permissions, or <code>null</code> if passed array was <code>null</code> or empty
     */
    private static List<FileStoragePermission> getfFileStoragePermissions(Permission[] permissions) {
        if (null != permissions && 0 < permissions.length) {
            List<FileStoragePermission> fileStoragePermissions = new ArrayList<FileStoragePermission>(permissions.length);
            for (Permission permission : permissions) {
                fileStoragePermissions.add(getfFileStoragePermissions(permission));
            }
            return fileStoragePermissions;
        }
        return null;
    }

    /**
     * Creates a file storage permission equivalent to the supplied permission.
     *
     * @param permission The permission to create the file storage permission for
     * @return The file storage permission
     */
    private static FileStoragePermission getfFileStoragePermissions(Permission permission) {
        FileStoragePermission fileStoragePermission = DefaultFileStoragePermission.newInstance();
        fileStoragePermission.setEntity(permission.getEntity());
        fileStoragePermission.setAllPermissions(
            permission.getFolderPermission(), permission.getReadPermission(), permission.getWritePermission(), permission.getDeletePermission());
        fileStoragePermission.setAdmin(permission.isAdmin());
        fileStoragePermission.setGroup(permission.isGroup());
        return fileStoragePermission;
    }

    /**
     * Adds any present warnings from the supplied {@link WarningsAware} reference to the storage parameters warnings list.
     *
     * @param storageParameters The storage parameters to add the warnings to
     * @param warningsAware The warnings aware to get and flush the warnings from
     */
    private static void addWarnings(StorageParameters storageParameters, WarningsAware warningsAware) {
        List<OXException> list = warningsAware.getAndFlushWarnings();
        if (null != list && false == list.isEmpty()) {
            for (OXException warning : list) {
                storageParameters.addWarning(warning);
            }
        }
    }

}
