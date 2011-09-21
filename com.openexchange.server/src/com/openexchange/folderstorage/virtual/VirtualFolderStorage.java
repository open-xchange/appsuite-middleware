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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.virtual;

import static com.openexchange.folderstorage.internal.Tools.getUnsignedInteger;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.concurrent.CallerRunsCompletionService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StoragePriority;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.cache.CacheServiceRegistry;
import com.openexchange.folderstorage.internal.performers.InstanceStorageParametersProvider;
import com.openexchange.folderstorage.internal.performers.SessionStorageParametersProvider;
import com.openexchange.folderstorage.internal.performers.StorageParametersProvider;
import com.openexchange.folderstorage.outlook.OutlookId;
import com.openexchange.folderstorage.outlook.memory.MemoryTable;
import com.openexchange.folderstorage.outlook.memory.MemoryTree;
import com.openexchange.folderstorage.virtual.sql.Delete;
import com.openexchange.folderstorage.virtual.sql.Insert;
import com.openexchange.folderstorage.virtual.sql.Select;
import com.openexchange.folderstorage.virtual.sql.Update;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link VirtualFolderStorage} - The virtual folder storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VirtualFolderStorage implements FolderStorage {

    private static final ThreadPools.ExpectedExceptionFactory<OXException> FACTORY =
        new ThreadPools.ExpectedExceptionFactory<OXException>() {

            @Override
            public Class<OXException> getType() {
                return OXException.class;
            }

            @Override
            public OXException newUnexpectedError(final Throwable t) {
                return FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(t, t.getMessage());
            }
        };

    private final FolderType folderType;

    /**
     * Initializes a new {@link VirtualFolderStorage}.
     */
    public VirtualFolderStorage() {
        super();
        folderType = new VirtualFolderType();
    }

    @Override
    public void clearCache(final int userId, final int contextId) {
        /*
         * Nothing to do...
         */
    }

    @Override
    public Folder prepareFolder(final String treeId, final Folder folder, final StorageParameters params) throws OXException {
        return folder;
    }

    @Override
    public void checkConsistency(final String treeId, final StorageParameters params) throws OXException {
        // TODO: Implement it
    }

    @Override
    public void restore(final String treeId, final String folderId, final StorageParameters params) throws OXException {
        // No real storage
    }

    @Override
    public ContentType getDefaultContentType() {
        return null;
    }

    @Override
    public void commitTransaction(final StorageParameters params) throws OXException {
        // Nothing to commit
    }

    @Override
    public void createFolder(final Folder folder, final StorageParameters params) throws OXException {
        final int tree = unsignedInt(folder.getTreeID());
        final int contextId = params.getContextId();
        Insert.insertFolder(contextId, tree, params.getUserId(), folder);
        MemoryTable.getMemoryTableFor(params.getSession()).initializeFolder(folder.getID(), tree, params.getUserId(), contextId);
    }

    @Override
    public void clearFolder(final String treeId, final String folderId, final StorageParameters params) throws OXException {
        // Nothing to do
    }

    @Override
    public void deleteFolder(final String treeId, final String folderId, final StorageParameters params) throws OXException {
        final int contextId = params.getContextId();
        final int tree = unsignedInt(treeId);
        Delete.deleteFolder(contextId, tree, params.getUserId(), folderId, true);
        MemoryTable.getMemoryTableFor(params.getSession()).initializeTree(tree, params.getUserId(), contextId);
    }

    @Override
    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final Type type, final StorageParameters params) throws OXException {
        // Get default folder
        final FolderStorage byContentType = VirtualFolderStorageRegistry.getInstance().getFolderStorageByContentType(treeId, contentType);
        if (null == byContentType) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType);
        }
        final boolean started = byContentType.startTransaction(params, false);
        try {
            final String defaultFolderID = byContentType.getDefaultFolderID(user, treeId, contentType, type, params);
            if (started) {
                byContentType.commitTransaction(params);
            }
            return defaultFolderID;
        } catch (final OXException e) {
            if (started) {
                byContentType.rollback(params);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                byContentType.rollback(params);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public Type getTypeByParent(final User user, final String treeId, final String parentId, final StorageParameters params) throws OXException {
        return null;
    }

    @Override
    public boolean containsForeignObjects(final User user, final String treeId, final String folderId, final StorageParameters params) throws OXException {
        /*
         * Get real folder storage
         */
        final FolderStorage realFolderStorage =
            VirtualFolderStorageRegistry.getInstance().getFolderStorage(FolderStorage.REAL_TREE_ID, folderId);
        if (null == realFolderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folderId);
        }
        final boolean started = realFolderStorage.startTransaction(params, false);
        try {
            final boolean containsForeignObjects = realFolderStorage.containsForeignObjects(user, treeId, folderId, params);
            if (started) {
                realFolderStorage.commitTransaction(params);
            }
            return containsForeignObjects;
        } catch (final OXException e) {
            if (started) {
                realFolderStorage.rollback(params);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                realFolderStorage.rollback(params);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean isEmpty(final String treeId, final String folderId, final StorageParameters params) throws OXException {
        /*
         * Get real folder storage
         */
        final FolderStorage realFolderStorage =
            VirtualFolderStorageRegistry.getInstance().getFolderStorage(FolderStorage.REAL_TREE_ID, folderId);
        if (null == realFolderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folderId);
        }
        final boolean started = realFolderStorage.startTransaction(params, false);
        try {
            final boolean isEmpty = realFolderStorage.isEmpty(treeId, folderId, params);
            if (started) {
                realFolderStorage.commitTransaction(params);
            }
            return isEmpty;
        } catch (final OXException e) {
            if (started) {
                realFolderStorage.rollback(params);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                realFolderStorage.rollback(params);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void updateLastModified(final long lastModified, final String treeId, final String folderId, final StorageParameters params) throws OXException {
        /*
         * Get real folder storage
         */
        final FolderStorage folderStorage =
            VirtualFolderStorageRegistry.getInstance().getFolderStorage(FolderStorage.REAL_TREE_ID, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folderId);
        }
        final boolean started = folderStorage.startTransaction(params, false);
        try {
            // Get folder
            folderStorage.updateLastModified(lastModified, FolderStorage.REAL_TREE_ID, folderId, params);
            MemoryTable.getMemoryTableFor(params.getSession()).initializeFolder(
                folderId,
                unsignedInt(treeId),
                params.getUserId(),
                params.getContextId());
            if (started) {
                folderStorage.commitTransaction(params);
            }
        } catch (final OXException e) {
            if (started) {
                folderStorage.rollback(params);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                folderStorage.rollback(params);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public List<Folder> getFolders(final String treeId, final List<String> folderIds, final StorageParameters params) throws OXException {
        return getFolders(treeId, folderIds, StorageType.WORKING, params);
    }

    @Override
    public List<Folder> getFolders(final String treeId, final List<String> folderIds, final StorageType storageType, final StorageParameters params) throws OXException {
        final User user = params.getUser();
        final MemoryTable memoryTable = MemoryTable.getMemoryTableFor(params.getSession());
        final MemoryTree memoryTree = memoryTable.getTree(unsignedInt(treeId), user.getId(), params.getContextId());
        final Locale locale = user.getLocale();
        /*
         * Get real folders
         */
        final Map<String, Folder> realFolders = loadFolders(treeId, folderIds, storageType, params);
        final List<Folder> ret = new ArrayList<Folder>(folderIds.size());
        for (final String folderId : folderIds) {
            final Folder realFolder = realFolders.get(folderId);
            ret.add(getFolder0(realFolder, treeId, folderId, memoryTree, locale));
        }
        return ret;
    }

    private Map<String, Folder> loadFolders(final String treeId, final List<String> folderIds, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        /*
         * Collect by folder storage
         */
        final int size = folderIds.size();
        final Map<FolderStorage, TIntList> map = new HashMap<FolderStorage, TIntList>(4);
        for (int i = 0; i < size; i++) {
            final String id = folderIds.get(i);
            final FolderStorage tmp = VirtualFolderStorageRegistry.getInstance().getFolderStorage(treeId, id);
            if (null == tmp) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, id);
            }
            TIntList list = map.get(tmp);
            if (null == list) {
                list = new TIntArrayList();
                map.put(tmp, list);
            }
            list.add(i);
        }
        /*
         * Process by folder storage
         */
        final CompletionService<Object> completionService;
        final StorageParametersProvider paramsProvider;
        if (1 == map.size()) {
            completionService = new CallerRunsCompletionService<Object>();
            paramsProvider = new InstanceStorageParametersProvider(storageParameters);
        } else {
            completionService =
                new ThreadPoolCompletionService<Object>(CacheServiceRegistry.getServiceRegistry().getService(ThreadPoolService.class, true));

            final Session session = storageParameters.getSession();
            paramsProvider =
                null == session ? new SessionStorageParametersProvider(storageParameters.getUser(), storageParameters.getContext()) : new SessionStorageParametersProvider(
                    (ServerSession) storageParameters.getSession());
        }
        /*
         * Create destination map
         */
        final Map<String, Folder> ret = new ConcurrentHashMap<String, Folder>(size);
        int taskCount = 0;
        for (final java.util.Map.Entry<FolderStorage, TIntList> entry : map.entrySet()) {
            final FolderStorage tmp = entry.getKey();
            final int[] indexes = entry.getValue().toArray();
            completionService.submit(new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    final StorageParameters newParameters = paramsProvider.getStorageParameters();
                    final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(2);
                    if (tmp.startTransaction(newParameters, false)) {
                        openedStorages.add(tmp);
                    }
                    try {
                        /*
                         * Create the list of IDs to load with current storage
                         */
                        final List<String> ids = new ArrayList<String>(indexes.length);
                        for (final int index : indexes) {
                            ids.add(folderIds.get(index));
                        }
                        /*
                         * Load them & commit
                         */
                        final List<Folder> folders = tmp.getFolders(treeId, ids, storageType, newParameters);
                        for (final FolderStorage fs : openedStorages) {
                            fs.commitTransaction(newParameters);
                        }
                        /*
                         * Fill into map
                         */
                        for (final Folder folder : folders) {
                            ret.put(folder.getID(), folder);
                        }
                        /*
                         * Return
                         */
                        return null;
                    } catch (final OXException e) {
                        for (final FolderStorage fs : openedStorages) {
                            fs.rollback(newParameters);
                        }
                        throw e;
                    } catch (final RuntimeException e) {
                        for (final FolderStorage fs : openedStorages) {
                            fs.rollback(newParameters);
                        }
                        throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e);
                    }
                }
            });
            taskCount++;
        }
        /*
         * Wait for completion
         */
        ThreadPools.takeCompletionService(completionService, taskCount, FACTORY);
        return ret;
    }

    @Override
    public Folder getFolder(final String treeId, final String folderId, final StorageParameters params) throws OXException {
        return getFolder(treeId, folderId, StorageType.WORKING, params);
    }

    @Override
    public Folder getFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters params) throws OXException {
        final User user = params.getUser();
        final MemoryTable memoryTable = MemoryTable.getMemoryTableFor(params.getSession());
        final MemoryTree memoryTree = memoryTable.getTree(unsignedInt(treeId), user.getId(), params.getContextId());
        /*
         * Get real storage
         */
        final FolderStorage realFolderStorage =
            VirtualFolderStorageRegistry.getInstance().getFolderStorage(FolderStorage.REAL_TREE_ID, folderId);
        if (null == realFolderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folderId);
        }
        final boolean started = realFolderStorage.startTransaction(params, false);
        try {
            final Folder realFolder = realFolderStorage.getFolder(FolderStorage.REAL_TREE_ID, folderId, params);
            final Folder ret = getFolder0(realFolder, treeId, folderId, memoryTree, user.getLocale());
            if (started) {
                realFolderStorage.commitTransaction(params);
            }
            return ret;
        } catch (final OXException e) {
            if (started) {
                realFolderStorage.rollback(params);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                realFolderStorage.rollback(params);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private Folder getFolder0(final Folder realFolder, final String treeId, final String folderId, final MemoryTree memoryTree, final Locale locale) throws OXException {
        final VirtualFolder virtualFolder = new VirtualFolder(realFolder);
        virtualFolder.setTreeID(treeId);
        virtualFolder.setID(folderId);
        // Load folder data from database
        if (!memoryTree.fillFolder(virtualFolder)) {
            throw FolderExceptionErrorMessage.NOT_FOUND.create(folderId, treeId);
        }
        final String[] subfolderIds = memoryTree.getSubfolderIds(locale, folderId, Collections.<String[]> emptyList());
        virtualFolder.setSubfolderIDs(subfolderIds);
        virtualFolder.setSubscribedSubfolders(subfolderIds != null && subfolderIds.length > 0);
        /*-
         * 
        Select.fillFolder(
            params.getContextId(),
            unsignedInt(treeId),
            user.getId(),
            user.getLocale(),
            virtualFolder,
            storageType);
         * 
         */
        return virtualFolder;
    }

    @Override
    public FolderType getFolderType() {
        return folderType;
    }

    @Override
    public StoragePriority getStoragePriority() {
        return StoragePriority.NORMAL;
    }

    @Override
    public SortableId[] getSubfolders(final String treeId, final String parentId, final StorageParameters params) throws OXException {
        final User user = params.getUser();
        final Locale locale = user.getLocale();
        /*
         * Check memory table
         */
        final MemoryTable memoryTable = MemoryTable.optMemoryTableFor(params.getSession());
        if (null != memoryTable) {
            final MemoryTree memoryTree = memoryTable.getTree(unsignedInt(treeId), params.getSession());
            if (null == memoryTree) {
                throw FolderExceptionErrorMessage.TREE_NOT_FOUND.create(treeId);
            }
            final String[] ids = memoryTree.getSubfolderIds(locale, parentId, Collections.<String[]> emptyList());
            final SortableId[] ret = new SortableId[ids.length];
            for (int i = 0; i < ids.length; i++) {
                ret[i] = new OutlookId(ids[i], i, null);
            }
            return ret;
        }
        /*
         * Load from database
         */
        final String[] ids =
            Select.getSubfolderIds(params.getContextId(), unsignedInt(treeId), user.getId(), locale, parentId, StorageType.WORKING);
        final SortableId[] ret = new SortableId[ids.length];
        for (int i = 0; i < ids.length; i++) {
            ret[i] = new VirtualId(ids[i], i, null);
        }
        return ret;
    }

    @Override
    public void rollback(final StorageParameters params) {
        // Nope
    }

    @Override
    public boolean startTransaction(final StorageParameters parameters, final boolean modify) throws OXException {
        return false;
    }

    @Override
    public void updateFolder(final Folder folder, final StorageParameters params) throws OXException {
        final Folder storageFolder = getFolder(folder.getTreeID(), folder.getID(), params);
        /*
         * Ensure all field set
         */
        if (null == folder.getParentID()) {
            folder.setParentID(storageFolder.getParentID());
        }
        if (null == folder.getPermissions()) {
            folder.setPermissions(storageFolder.getPermissions());
        }
        if (folder.getName() == null) {
            folder.setName(storageFolder.getName());
        }
        final int tree = unsignedInt(folder.getTreeID());
        final int userId = params.getUserId();
        final int contextId = params.getContextId();
        Update.updateFolder(contextId, tree, userId, folder);
        MemoryTable.getMemoryTableFor(params.getSession()).initializeTree(tree, userId, contextId);
    }

    @Override
    public ContentType[] getSupportedContentTypes() {
        return new ContentType[0];
    }

    @Override
    public boolean containsFolder(final String treeId, final String folderId, final StorageParameters params) throws OXException {
        return containsFolder(treeId, folderId, StorageType.WORKING, params);
    }

    @Override
    public boolean containsFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters params) throws OXException {
        return Select.containsFolder(params.getContextId(), unsignedInt(treeId), params.getUserId(), folderId, storageType);
    }

    @Override
    public String[] getDeletedFolderIDs(final String treeId, final Date timeStamp, final StorageParameters params) throws OXException {
        return new String[0];
    }

    @Override
    public String[] getModifiedFolderIDs(final String treeId, final Date timeStamp, final ContentType[] includeContentTypes, final StorageParameters params) throws OXException {
        return new String[0];
    }

    @Override
    public SortableId[] getVisibleFolders(final String treeId, final ContentType contentType, final Type type, final StorageParameters params) throws OXException {
        throw new UnsupportedOperationException("VirtualFolderStorage.getVisibleSubfolders()");
    }

    private static int unsignedInt(final String sInteger) {
        return getUnsignedInteger(sInteger);
    }

}
