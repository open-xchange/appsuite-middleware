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

package com.openexchange.folderstorage.cache;

import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.concurrent.CallerRunsCompletionService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.AfterReadAwareFolderStorage;
import com.openexchange.folderstorage.AfterReadAwareFolderStorage.Mode;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.ReinitializableFolderStorage;
import com.openexchange.folderstorage.RemoveAfterAccessFolder;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StoragePriority;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.cache.memory.FolderMap;
import com.openexchange.folderstorage.cache.memory.FolderMapManagement;
import com.openexchange.folderstorage.cache.service.FolderCacheInvalidationService;
import com.openexchange.folderstorage.database.DatabaseFolderType;
import com.openexchange.folderstorage.internal.StorageParametersImpl;
import com.openexchange.folderstorage.internal.Tools;
import com.openexchange.folderstorage.internal.performers.ClearPerformer;
import com.openexchange.folderstorage.internal.performers.CreatePerformer;
import com.openexchange.folderstorage.internal.performers.DeletePerformer;
import com.openexchange.folderstorage.internal.performers.InstanceStorageParametersProvider;
import com.openexchange.folderstorage.internal.performers.PathPerformer;
import com.openexchange.folderstorage.internal.performers.SessionStorageParametersProvider;
import com.openexchange.folderstorage.internal.performers.StorageParametersProvider;
import com.openexchange.folderstorage.internal.performers.UpdatePerformer;
import com.openexchange.folderstorage.internal.performers.UpdatesPerformer;
import com.openexchange.folderstorage.mail.MailFolderType;
import com.openexchange.folderstorage.osgi.FolderStorageServices;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountFacade;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.threadpool.RefusedExecutionBehavior;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.ThreadPools.TrackableCallable;
import com.openexchange.threadpool.behavior.AbortBehavior;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.userconf.UserPermissionService;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * {@link CacheFolderStorage} - The cache folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheFolderStorage implements ReinitializableFolderStorage, FolderCacheInvalidationService {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CacheFolderStorage.class);

    private static final ThreadPools.ExpectedExceptionFactory<OXException> FACTORY = new ThreadPools.ExpectedExceptionFactory<OXException>() {

        @Override
        public Class<OXException> getType() {
            return OXException.class;
        }

        @Override
        public OXException newUnexpectedError(Throwable t) {
            return FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(t, t.getMessage());
        }
    };

    private static final CacheFolderStorage INSTANCE = new CacheFolderStorage();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static CacheFolderStorage getInstance() {
        return INSTANCE;
    }

    private static final String ROOT_ID = FolderStorage.ROOT_ID;

    // ------------------------------------------------------------------------ //

    private final String realTreeId;
    private final CacheFolderStorageRegistry registry;

    private volatile CacheService cacheService;
    private volatile Cache globalCache;

    /**
     * Initializes a new {@link CacheFolderStorage}.
     */
    private CacheFolderStorage() {
        super();
        realTreeId = REAL_TREE_ID;
        registry = CacheFolderStorageRegistry.getInstance();
    }

    /**
     * Clears all cached entries.
     */
    public void clearAll() {
        Cache cache = globalCache;
        if (null != cache) {
            try {
                cache.clear();
            } catch (Exception e) {
                // Ignore
            }
        }
        FolderMapManagement.getInstance().clear();
    }

    /**
     * Removes denoted folder from global cache.
     *
     * @param folderId The folder identifier
     * @param treeId The tree identifier
     * @param contextId The context identifier
     */
    public void removeFromGlobalCache(String folderId, String treeId, int contextId) {
        Cache cache = globalCache;
        if (null != cache && Tools.isGlobalId(folderId)) {
            try {
                cache.removeFromGroup(newCacheKey(folderId, treeId), Integer.toString(contextId));
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Override
    public void clearCache(int userId, int contextId) {
        if (contextId <= 0) {
            return;
        }
        Cache cache = globalCache;
        if (null != cache) {
            cache.invalidateGroup(Integer.toString(contextId));
        }
        if (userId > 0) {
            dropUserEntries(userId, contextId);
        } else {
            FolderMapManagement.getInstance().dropFor(contextId);
        }
    }

    /**
     * Initializes this folder cache on available cache service.
     *
     * @throws OXException If initialization of this folder cache fails
     */
    public void onCacheAvailable() throws OXException {
        cacheService = CacheServiceRegistry.getServiceRegistry().getService(CacheService.class, true);
        globalCache = cacheService.getCache("GlobalFolderCache");
    }

    /**
     * Disposes this folder cache on absent cache service.
     *
     * @throws OXException If disposal of this folder cache fails
     */
    public void onCacheAbsent() throws OXException {
        CacheService service = cacheService;
        Cache cache = globalCache;
        if (cache != null) {
            try {
                cache.clear();
                if (null != service) {
                    service.freeCache("GlobalFolderCache");
                }
            } finally {
                globalCache = null;
            }
        }
        if (service != null) {
            cacheService = null;
        }
    }

    @Override
    public boolean reinitialize(String treeId, StorageParameters storageParameters) throws OXException {
        boolean reinitialized = false;
        for (FolderStorage folderStorage : registry.getFolderStoragesForTreeID(treeId)) {
            if (folderStorage instanceof ReinitializableFolderStorage) {
                boolean started = folderStorage.startTransaction(storageParameters, false);
                try {
                    reinitialized |= ((ReinitializableFolderStorage) folderStorage).reinitialize(treeId, storageParameters);
                    if (started) {
                        folderStorage.commitTransaction(storageParameters);
                        started = false;
                    }
                } catch (RuntimeException e) {
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                } finally {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                }
            }
        }
        return reinitialized;
    }

    protected static final Set<String> IGNORABLES = RemoveAfterAccessFolder.IGNORABLES;

    @Override
    public void checkConsistency(String treeId, final StorageParameters storageParameters) throws OXException {
        for (FolderStorage folderStorage : registry.getFolderStoragesForTreeID(treeId)) {
            boolean started = folderStorage.startTransaction(storageParameters, false);
            try {
                folderStorage.checkConsistency(treeId, storageParameters);
                if (started) {
                    folderStorage.commitTransaction(storageParameters);
                    started = false;
                }
            } catch (RuntimeException e) {
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
            }
        }
        final String realTreeId = this.realTreeId;
        if (realTreeId.equals(treeId)) {
            try {
                final int contextId = storageParameters.getContextId();
                final int userId = storageParameters.getUserId();
                final UserPermissionBits userPermissionBits = FolderStorageServices.requireService(UserPermissionService.class).getUserPermissionBits(userId, contextId);
                final ServiceRegistry serviceRegistry = CacheServiceRegistry.getServiceRegistry();
                final ThreadPoolService threadPool = ThreadPools.getThreadPool();
                final RefusedExecutionBehavior<Object> behavior = AbortBehavior.getInstance();
                /*
                 * Traverse mail accounts in separate task
                 */
                Runnable task = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            final StorageParameters params = newStorageParameters(storageParameters);
                            params.putParameter(MailFolderType.getInstance(), StorageParameters.PARAM_ACCESS_FAST, Boolean.FALSE);
                            if (userPermissionBits.isMultipleMailAccounts()) {
                                MailAccountFacade mailAccountFacade = serviceRegistry.getService(MailAccountFacade.class, true);
                                MailAccount[] accounts = mailAccountFacade.getUserMailAccounts(
                                    userId,
                                    contextId);
                                /*
                                 * Gather tasks...
                                 */
                                List<Runnable> tasks = new ArrayList<Runnable>(accounts.length);
                                for (MailAccount mailAccount : accounts) {
                                    int accountId = mailAccount.getId();
                                    if (accountId != MailAccount.DEFAULT_ID && !IGNORABLES.contains(mailAccount.getMailProtocol())) {
                                        final String folderId = prepareFullname(accountId, MailFolder.DEFAULT_FOLDER_ID);
                                        /*
                                         * Check if already present
                                         */
                                        Folder rootFolder = getRefFromCache(realTreeId, folderId, params);
                                        if (null == rootFolder) {
                                            Runnable mailAccountTask = new Runnable() {

                                                @Override
                                                public void run() {
                                                    try {
                                                        /*
                                                         * Load it
                                                         */
                                                        Folder rootFolder = loadFolder(
                                                            realTreeId,
                                                            folderId,
                                                            StorageType.WORKING,
                                                            params);
                                                        putFolder(rootFolder, realTreeId, params, false);
                                                        String[] subfolderIDs = rootFolder.getSubfolderIDs();
                                                        if (null != subfolderIDs) {
                                                            for (String subfolderId : subfolderIDs) {
                                                                Folder folder = loadFolder(
                                                                    realTreeId,
                                                                    subfolderId,
                                                                    StorageType.WORKING,
                                                                    params);
                                                                putFolder(folder, realTreeId, params, false);
                                                            }
                                                        }
                                                    } catch (Exception e) {
                                                        // Pre-Accessing external account folder failed.
                                                        LOG.debug("", e);
                                                    }
                                                }
                                            };
                                            tasks.add(mailAccountTask);
                                        }
                                    }
                                }
                                if (!tasks.isEmpty()) {
                                    for (Runnable task : tasks) {
                                        threadPool.submit(ThreadPools.trackableTask(task), behavior);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LOG.debug("", e);
                        }
                    }
                };
                threadPool.submit(ThreadPools.trackableTask(task), behavior);
            } catch (Exception e) {
                LOG.debug("", e);
            }
        }
    }

    /**
     * Checks if given folder storage is already contained in collection of opened storages. If yes, this method terminates immediately.
     * Otherwise the folder storage is opened according to specified modify flag and is added to specified collection of opened storages.
     */
    protected static void checkOpenedStorage(FolderStorage checkMe, StorageParameters params, boolean modify, java.util.Collection<FolderStorage> openedStorages) throws OXException {
        if (openedStorages.contains(checkMe)) {
            // Passed storage is already opened
            return;
        }
        // Passed storage has not been opened before. Open now and add to collection
        if (checkMe.startTransaction(params, modify)) {
            openedStorages.add(checkMe);
        }
    }

    @Override
    public void restore(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        boolean started = storage.startTransaction(storageParameters, false);
        try {
            storage.restore(treeId, folderId, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
                started = false;
            }
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (started) {
                storage.rollback(storageParameters);
            }
            clearCache(storageParameters.getUserId(), storageParameters.getContextId());
        }
    }

    @Override
    public Folder prepareFolder(String treeId, Folder folder, StorageParameters storageParameters) throws OXException {
        String folderId = folder.getID();
        FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        boolean started = storage.startTransaction(storageParameters, false);
        try {
            Folder preparedFolder = storage.prepareFolder(treeId, folder, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
                started = false;
            }
            if (preparedFolder.isCacheable() && preparedFolder.isGlobalID() != folder.isGlobalID()) {
                putFolder(preparedFolder, treeId, storageParameters, false);
            }
            return preparedFolder;
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (started) {
                storage.rollback(storageParameters);
            }
        }
    }

    private PathPerformer newPathPerformer(StorageParameters storageParameters) throws OXException {
        Session session = storageParameters.getSession();
        FolderServiceDecorator decorator = storageParameters.getDecorator();
        PathPerformer performer;
        if (null == session) {
            performer = new PathPerformer(storageParameters.getUser(), storageParameters.getContext(), decorator, registry);
        } else {
            performer = new PathPerformer(ServerSessionAdapter.valueOf(session), decorator, registry);
        }
        performer.setStorageParameters(storageParameters);
        return performer;
    }

    private CreatePerformer newCreatePerformer(StorageParameters storageParameters) throws OXException {
        Session session = storageParameters.getSession();
        FolderServiceDecorator decorator = storageParameters.getDecorator();
        CreatePerformer performer;
        if (null == session) {
            performer = new CreatePerformer(storageParameters.getUser(), storageParameters.getContext(), decorator, registry);
        } else {
            performer = new CreatePerformer(ServerSessionAdapter.valueOf(session), decorator, registry);
        }
        performer.setStorageParameters(storageParameters);
        return performer;
    }

    private DeletePerformer newDeletePerformer(StorageParameters storageParameters) throws OXException {
        Session session = storageParameters.getSession();
        FolderServiceDecorator decorator = storageParameters.getDecorator();
        DeletePerformer performer;
        if (null == session) {
            performer = new DeletePerformer(storageParameters.getUser(), storageParameters.getContext(), decorator, registry);
        } else {
            performer = new DeletePerformer(ServerSessionAdapter.valueOf(session), decorator, registry);
        }
        performer.setStorageParameters(storageParameters);
        return performer;
    }

    private UpdatePerformer newUpdatePerformer(StorageParameters storageParameters) throws OXException {
        Session session = storageParameters.getSession();
        FolderServiceDecorator decorator = storageParameters.getDecorator();
        UpdatePerformer performer;
        if (null == session) {
            performer = new UpdatePerformer(storageParameters.getUser(), storageParameters.getContext(), decorator, registry);
        } else {
            performer = new UpdatePerformer(ServerSessionAdapter.valueOf(session), decorator, registry);
        }
        performer.setStorageParameters(storageParameters);
        return performer;
    }

    @Override
    public ContentType getDefaultContentType() {
        return null;
    }

    @Override
    public void commitTransaction(StorageParameters params) throws OXException {
        // Nothing to do
    }

    @Override
    public void createFolder(Folder folder, StorageParameters storageParameters) throws OXException {
        String treeId = folder.getTreeID();
        boolean created = false;
        Session session = storageParameters.getSession();
        int userId = storageParameters.getUserId();
        try {
            /*
             * Perform create operation via non-cache storage
             */
            CreatePerformer createPerformer = newCreatePerformer(storageParameters);
            createPerformer.setCheck4Duplicates(false);
            String folderId = createPerformer.doCreate(folder);
            created = true;
            /*
             * Get folder from appropriate storage
             */
            FolderStorage storage = registry.getFolderStorage(treeId, folderId);
            if (null == storage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
            }

            // Load created folder from real tree
            int contextId = storageParameters.getContextId();
            Folder createdFolder = null;
            try {
                createdFolder = loadFolder(realTreeId, folderId, StorageType.WORKING, true, storageParameters);
                if (createdFolder.isCacheable()) {
                    putFolder(createdFolder, realTreeId, storageParameters, false);
                }
            } catch (OXException e) {
                LOG.warn("Newly created folder could not be loaded from appropriate storage.", e);
            }

            // Remove parent from cache(s)
            FolderMapManagement folderMapManagement = FolderMapManagement.getInstance();
            Cache cache = globalCache;
            String sContextId = Integer.toString(contextId);
            List<Serializable> keys = new LinkedList<Serializable>();
            for (String tid : new String[] { treeId, realTreeId }) {
                if (Tools.isGlobalId(folder.getParentID())) {
                    keys.add(newCacheKey(folder.getParentID(), tid));
                }
                // Cleanse parent from caches, too
                folderMapManagement.dropFor(folder.getParentID(), tid, userId, contextId, session);
            }
            if (null != cache && !keys.isEmpty()) {
                cache.removeFromGroup(keys, sContextId);
            }

            if (null != createdFolder && false == createdFolder.getParentID().equals(folder.getParentID())) {
                keys.clear();
                for (String tid : new String[] { treeId, realTreeId }) {
                    if (Tools.isGlobalId(createdFolder.getParentID())) {
                        keys.add(newCacheKey(createdFolder.getParentID(), tid));
                    }
                    // Cleanse parent from caches, too
                    folderMapManagement.dropFor(createdFolder.getParentID(), tid, userId, contextId, session);
                }
                if (null != cache && !keys.isEmpty()) {
                    cache.removeFromGroup(keys, sContextId);
                }
            }
            /*
             * Load parent from real tree
             */
            Folder parentFolder = loadFolder(realTreeId, folder.getParentID(), StorageType.WORKING, true, storageParameters);
            if (parentFolder.isCacheable()) {
                putFolder(parentFolder, realTreeId, storageParameters, true);
            }
            if (null != createdFolder && false == createdFolder.getParentID().equals(folder.getParentID())) {
                parentFolder = loadFolder(realTreeId, createdFolder.getParentID(), StorageType.WORKING, true, storageParameters);
                if (parentFolder.isCacheable()) {
                    putFolder(parentFolder, realTreeId, storageParameters, true);
                }
            }
        } finally {
            if (false == created) {
                removeSingleFromCache(Collections.singletonList(folder.getParentID()), treeId, userId, session, false);
            }
        }
    }

    /**
     * Puts specified folder into appropriate cache.
     *
     * @param folder The folder
     * @param treeId The tree identifier
     * @param storageParameters The storage parameters
     * @throws OXException If put into cache fails
     */
    public void putFolder(Folder folder, String treeId, StorageParameters storageParameters, boolean invalidate) throws OXException {
        /*
         * Put to cache
         */
        if (folder.isGlobalID()) {
            globalCache.putInGroup(newCacheKey(folder.getID(), treeId), Integer.toString(storageParameters.getContextId()), folder, invalidate);
        } else {
            getFolderMapFor(storageParameters).put(treeId, folder, storageParameters.getSession());
        }
    }

    @Override
    public void invalidateSingle(String folderId, String treeId, Session session) throws OXException {
        removeFromCache(folderId, treeId, true, null, null, session);
    }

    @Override
    public void invalidate(String folderId, String treeId, boolean includeParents, Session session) throws OXException {
        removeFromCache(folderId, treeId, !includeParents, null, null, session);
    }

    /**
     * Removes specified folder and all of its predecessor folders from cache.
     *
     * @param id The folder identifier
     * @param treeId The tree identifier
     * @param singleOnly <code>true</code> if only specified folder should be removed; otherwise <code>false</code> for complete folder's
     *            path to root folder
     * @param session The session; never <code>null</code>
     * @throws OXException If removal fails
     */
    public void removeFromCache(String id, String treeId, boolean singleOnly, Session session) throws OXException {
        removeFromCache(id, treeId, singleOnly, null, null, session, null);
    }

    /**
     * Removes specified folder and all of its predecessor folders from cache.
     *
     * @param id The folder identifier
     * @param treeId The tree identifier
     * @param singleOnly <code>true</code> if only specified folder should be removed; otherwise <code>false</code> for complete folder's
     *            path to root folder
     * @param user The user or <code>null</code>, if a session is given
     * @param context The context or <code>null</code>, if a session is given
     * @param optSession The session or <code>null</code>, then user and context must be given
     * @throws OXException If removal fails
     */
    public void removeFromCache(String id, String treeId, boolean singleOnly, User user, Context context, Session optSession) throws OXException {
        removeFromCache(id, treeId, singleOnly, user, context, optSession, null);
    }

    /**
     * Removes specified folder and all of its predecessor folders from cache.
     *
     * @param id The folder identifier
     * @param treeId The tree identifier
     * @param singleOnly <code>true</code> if only specified folder should be removed; otherwise <code>false</code> for complete folder's
     *            path to root folder
     * @param user The user or <code>null</code>, if a session is given
     * @param context The context or <code>null</code>, if a session is given
     * @param optSession The session or <code>null</code>, then user and context must be given
     * @param folderPath The folderPath to <code>rootFolder</code>, if known
     * @throws OXException If removal fails
     */
    public void removeFromCache(String id, String treeId, boolean singleOnly, User user, Context context, Session optSession, List<String> folderPath) throws OXException {
        int userId;
        int contextId;
        if (optSession == null) {
            userId = user.getId();
            contextId = context.getContextId();
        } else {
            userId = optSession.getUserId();
            contextId = optSession.getContextId();
        }
        if (singleOnly) {
            removeSingleFromCache(Collections.singletonList(id), treeId, userId, contextId, true, null);
        } else {
            if (null != folderPath) {
                removeFromCache(id, treeId, userId, contextId, null, folderPath);
            } else {
                PathPerformer pathPerformer;
                if (optSession != null) {
                    pathPerformer = new PathPerformer(ServerSessionAdapter.valueOf(optSession), null, registry);
                } else {
                    pathPerformer = new PathPerformer(user, context, null, registry);
                }
                removeFromCache(id, treeId, userId, contextId, pathPerformer);
            }

        }
    }

    private void removeFromCache(String id, String treeId, int userId, int contextId, PathPerformer pathPerformer) throws OXException {
        removeFromCache(id, treeId, userId, contextId, pathPerformer, null);
    }

    private void removeFromCache(String id, String treeId, int userId, int contextId, PathPerformer pathPerformer, List<String> folderPath) throws OXException {
        if (null == id) {
            return;
        }
        // at least one way to get paths should be provided
        if (null == pathPerformer && null == folderPath) {
            return;
        }
        // but not both
        if (null != pathPerformer && null != folderPath) {
            return;
        }
        {
            List<String> ids;
            if (null != pathPerformer) {
                try {
                    pathPerformer.getStorageParameters().setIgnoreCache(Boolean.TRUE);
                    if (existsFolder(treeId, id, StorageType.WORKING, pathPerformer.getStorageParameters())) {
                        UserizedFolder[] path = pathPerformer.doPath(treeId, id, true);
                        ids = new ArrayList<String>(path.length);
                        for (UserizedFolder userizedFolder : path) {
                            ids.add(userizedFolder.getID());
                        }
                    } else {
                        ids = Collections.singletonList(id);
                    }
                } catch (Exception e) {
                    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CacheFolderStorage.class);
                    log.debug("", e);
                    try {
                        ids = new ArrayList<String>(Arrays.asList(pathPerformer.doForcePath(treeId, id, true)));
                    } catch (Exception e1) {
                        log.debug("", e1);
                        ids = Collections.singletonList(id);
                    }
                } finally {
                    pathPerformer.getStorageParameters().setIgnoreCache(null);
                }
            } else {
                ids = folderPath;
            }
            Cache cache = globalCache;
            FolderMapManagement folderMapManagement = FolderMapManagement.getInstance();
            if (realTreeId.equals(treeId)) {
                List<Serializable> keys = new LinkedList<Serializable>();
                for (String folderId : ids) {
                    if (Tools.isGlobalId(folderId)) {
                        keys.add(newCacheKey(folderId, treeId));
                    }
                    folderMapManagement.dropFor(folderId, treeId, userId, contextId);
                }
                if (!keys.isEmpty()) {
                    cache.removeFromGroup(keys, Integer.toString(contextId));
                }
            } else {
                List<Serializable> keys = new LinkedList<Serializable>();
                for (String folderId : ids) {
                    if (Tools.isGlobalId(folderId)) {
                        keys.add(newCacheKey(folderId, treeId));
                        keys.add(newCacheKey(folderId, realTreeId));
                    }
                    folderMapManagement.dropFor(folderId, treeId, userId, contextId);
                    folderMapManagement.dropFor(folderId, realTreeId, userId, contextId);
                }
                if (!keys.isEmpty()) {
                    cache.removeFromGroup(keys, Integer.toString(contextId));
                }
            }
        }
    }

    /**
     * Removes a single folder from cache.
     *
     * @param id The folder identifier
     * @param treeId The tree identifier
     * @param session The session
     */
    public void removeSingleFromCache(List<String> ids, String treeId, int userId, Session session, boolean deleted) {
        removeSingleFromCache(ids, treeId, userId, session.getContextId(), deleted, session);
    }

    /**
     * Removes a single folder from cache.
     *
     * @param ids The folder identifiers
     * @param treeId The tree identifier
     * @param contextId The context identifier
     */
    public void removeSingleFromCache(List<String> ids, String treeId, int optUserId, int contextId, boolean deleted, Session optSession) {
        removeSingleFromCache(ids, treeId, optUserId, contextId, deleted, false, optSession);
    }

    /**
     * Removes a single folder from cache.
     *
     * @param ids The folder identifiers
     * @param treeId The tree identifier
     * @param contextId The context identifier
     */
    public void removeSingleFromCache(List<String> ids, String treeId, int optUserId, int contextId, boolean deleted, boolean userCacheOnly, Session optSession) {
        try {
            // Perform for given folder tree and real tree
            String sContextId = Integer.toString(contextId);
            Cache cache = userCacheOnly ? null : globalCache;
            if (null == cache) {
                for (String tid : new HashSet<String>(Arrays.asList(treeId, realTreeId))) {
                    for (String id : ids) {
                        cleanseFromFolderManagement(optUserId, contextId, deleted, optSession, tid, id);
                    }
                }
            } else {
                List<Serializable> keys = new LinkedList<Serializable>();
                for (String tid : new HashSet<String>(Arrays.asList(treeId, realTreeId))) {
                    for (String id : ids) {
                        // Add affected cache keys
                        CacheKey cacheKey = newCacheKey(id, tid);
                        if (deleted) {
                            Folder cachedFolder = (Folder) cache.getFromGroup(cacheKey, sContextId);
                            if (null != cachedFolder) {
                                /*
                                 * Drop parent, too
                                 */
                                String parentID = cachedFolder.getParentID();
                                if (Tools.isGlobalId(parentID)) {
                                    keys.add(newCacheKey(parentID, tid));
                                }
                            }
                        }
                        if (Tools.isGlobalId(id)) {
                            keys.add(cacheKey);
                        }
                        // Cleanse from folder management
                        cleanseFromFolderManagement(optUserId, contextId, deleted, optSession, tid, id);
                    }
                }
                if (!keys.isEmpty()) {
                    cache.removeFromGroup(keys, sContextId);
                }
            }
        } catch (Exception x) {
            // Ignore
        }
    }

    private void cleanseFromFolderManagement(int optUserId, int contextId, boolean deleted, Session optSession, String tid, String id) {
        FolderMapManagement folderMapManagement = FolderMapManagement.getInstance();
        if (optUserId > 0) {
            FolderMap folderMap = folderMapManagement.optFor(optUserId, contextId);
            if (null != folderMap) {
                if (deleted) {
                    Folder cachedFolder = folderMap.get(id, tid, optSession);
                    if (null != cachedFolder) {
                        /*
                         * Drop parent, too
                         */
                        String parentID = cachedFolder.getParentID();
                        if (null != parentID) {
                            folderMapManagement.dropFor(parentID, tid, optUserId, contextId, optSession);
                        }
                    }
                }
            }
        }
        folderMapManagement.dropFor(id, tid, optUserId, contextId, optSession);
    }

    @Override
    public void clearFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        boolean cacheable;
        int contextId = storageParameters.getContextId();
        int userId = storageParameters.getUserId();
        Session session = storageParameters.getSession();
        String sContextId = Integer.toString(contextId);
        String[] subfolderIDs;
        {
            Folder clearMe = getFolder(treeId, folderId, storageParameters);
            /*
             * Load all subfolders
             */
            subfolderIDs = loadAllSubfolders(treeId, clearMe, false, storageParameters);
            {
                FolderMapManagement folderMapManagement = FolderMapManagement.getInstance();
                folderMapManagement.dropFor(folderId, treeId, userId, contextId, session);
                folderMapManagement.dropFor(folderId, realTreeId, userId, contextId, session);
                folderMapManagement.dropFor(clearMe.getParentID(), treeId, userId, contextId, session);
                folderMapManagement.dropFor(clearMe.getParentID(), realTreeId, userId, contextId, session);
            }
            cacheable = clearMe.isCacheable();
        }
        if (cacheable) {
            /*
             * Delete from cache
             */
            if (Tools.isGlobalId(folderId)) {
                globalCache.removeFromGroup(newCacheKey(folderId, treeId), sContextId);
            } else {
                FolderMapManagement.getInstance().dropFor(folderId, treeId, userId, contextId, session);
            }
        }
        /*
         * Drop subfolders from cache
         */
        removeSingleFromCache(Arrays.asList(subfolderIDs), treeId, userId, contextId, true, session);
        /*
         * Perform clear
         */
        if (null == session) {
            new ClearPerformer(storageParameters.getUser(), storageParameters.getContext(), registry).doClear(treeId, folderId);
        } else {
            new ClearPerformer(ServerSessionAdapter.valueOf(session), registry).doClear(treeId, folderId);
        }
        /*
         * Refresh
         */
        try {
            Folder clearedFolder = loadFolder(realTreeId, folderId, StorageType.WORKING, true, storageParameters);
            if (clearedFolder.isCacheable()) {
                putFolder(clearedFolder, realTreeId, storageParameters, true);
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    @Override
    public void deleteFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        String parentId;
        String realParentId;
        int contextId = storageParameters.getContextId();
        int userId = storageParameters.getUserId();
        Session session = storageParameters.getSession();
        String sContextId = Integer.toString(contextId);
        String[] subfolderIDs;
        {
            Folder deleteMe;
            try {
                deleteMe = getFolder(treeId, folderId, storageParameters);
                /*
                 * Load all subfolders
                 */
                subfolderIDs = loadAllSubfolders(treeId, deleteMe, false, storageParameters);
            } catch (OXException e) {
                /*
                 * Obviously folder does not exist
                 */
                if (Tools.isGlobalId(folderId)) {
                    globalCache.removeFromGroup(newCacheKey(folderId, treeId), sContextId);
                }
                FolderMapManagement.getInstance().dropFor(folderId, treeId, userId, contextId, session);
                return;
            }
            parentId = deleteMe.getParentID();
            if (!realTreeId.equals(treeId)) {
                StorageParameters parameters = newStorageParameters(storageParameters);
                FolderStorage folderStorage = registry.getFolderStorage(realTreeId, folderId);
                boolean started = folderStorage.startTransaction(parameters, false);
                try {
                    realParentId = folderStorage.getFolder(realTreeId, folderId, parameters).getParentID();
                    if (started) {
                        folderStorage.commitTransaction(parameters);
                        started = false;
                    }
                } catch (RuntimeException e) {
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e);
                } finally {
                    if (started) {
                        folderStorage.rollback(parameters);
                    }
                }
            } else {
                realParentId = null;
            }
        }
        /*
         * Delete from cache
         */
        {
            FolderMapManagement folderMapManagement = FolderMapManagement.getInstance();
            folderMapManagement.dropFor(Arrays.asList(folderId, parentId), treeId, userId, contextId, session);
            if (!treeId.equals(realTreeId)) {
                List<String> fids = new ArrayList<String>(Arrays.asList(folderId, parentId));
                if (null != realParentId) {
                    fids.add(realParentId);
                }
                folderMapManagement.dropFor(fids, realTreeId, userId, contextId, session);
            }
        }
        {
            List<Serializable> keys = new LinkedList<Serializable>();
            if (Tools.isGlobalId(folderId)) {
                keys.add(newCacheKey(folderId, treeId));
            }
            if (Tools.isGlobalId(parentId)) {
                keys.add(newCacheKey(parentId, treeId));
            }
            if (null != realParentId && !realParentId.equals(parentId)) {
                if (Tools.isGlobalId(realParentId)) {
                    keys.add(newCacheKey(realParentId, realTreeId));
                }
            }
            if (!keys.isEmpty()) {
                globalCache.removeFromGroup(keys, sContextId);
            }
        }
        registry.clearCaches(storageParameters.getUserId(), storageParameters.getContextId());
        /*
         * Drop subfolders from cache
         */
        removeSingleFromCache(Arrays.asList(subfolderIDs), treeId, userId, contextId, true, session);
        /*
         * Perform delete
         */
        newDeletePerformer(storageParameters).doDelete(
            treeId,
            folderId,
            storageParameters.getTimeStamp());
        /*
         * Refresh
         */
        if (null != realParentId && !ROOT_ID.equals(realParentId)) {
            if (session == null) {
                removeFromCache(realParentId, treeId, storageParameters.getUserId(), storageParameters.getContextId(), newPathPerformer(storageParameters));
            } else {
                removeFromCache(realParentId, treeId, session.getUserId(), session.getContextId(), newPathPerformer(storageParameters));
            }
        }
        if (!ROOT_ID.equals(parentId)) {
            if (session == null) {
                removeFromCache(parentId, treeId, storageParameters.getUserId(), storageParameters.getContextId(), newPathPerformer(storageParameters));
            } else {
                removeFromCache(parentId, treeId, session.getUserId(), session.getContextId(), newPathPerformer(storageParameters));
            }
        }
    }

    @Override
    public String getDefaultFolderID(User user, String treeId, ContentType contentType, Type type, StorageParameters storageParameters) throws OXException {
        FolderStorage storage = registry.getFolderStorageByContentType(treeId, contentType);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType);
        }
        String folderId;
        boolean started = storage.startTransaction(storageParameters, false);
        try {
            folderId = storage.getDefaultFolderID(user, treeId, contentType, type, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
                started = false;
            }
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (started) {
                storage.rollback(storageParameters);
            }
        }
        return folderId;
    }

    @Override
    public Type getTypeByParent(User user, String treeId, String parentId, StorageParameters storageParameters) throws OXException {
        FolderStorage storage = registry.getFolderStorage(treeId, parentId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, parentId);
        }
        Type type;
        boolean started = storage.startTransaction(storageParameters, false);
        try {
            type = storage.getTypeByParent(user, treeId, parentId, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
                started = false;
            }
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (started) {
                storage.rollback(storageParameters);
            }
        }
        return type;
    }

    @Override
    public boolean containsForeignObjects(User user, String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        /*
         * Get folder storage
         */
        FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        boolean started = storage.startTransaction(storageParameters, false);
        try {
            boolean containsForeignObjects = storage.containsForeignObjects(user, treeId, folderId, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
                started = false;
            }
            return containsForeignObjects;
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (started) {
                storage.rollback(storageParameters);
            }
        }
    }

    @Override
    public boolean isEmpty(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        /*
         * Get folder storage
         */
        FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        boolean started = storage.startTransaction(storageParameters, false);
        try {
            boolean isEmpty = storage.isEmpty(treeId, folderId, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
                started = false;
            }
            return isEmpty;
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (started) {
                storage.rollback(storageParameters);
            }
        }
    }

    @Override
    public void updateLastModified(long lastModified, String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        boolean started = storage.startTransaction(storageParameters, false);
        try {
            storage.updateLastModified(lastModified, treeId, folderId, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
                started = false;
            }
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (started) {
                storage.rollback(storageParameters);
            }
        }
        /*
         * Invalidate cache entry
         */
        removeFromCache(folderId, treeId, storageParameters.getUserId(), storageParameters.getContextId(), newPathPerformer(storageParameters));
    }

    @Override
    public Folder getFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        return getFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    @Override
    public List<Folder> getFolders(String treeId, List<String> folderIds, StorageParameters storageParameters) throws OXException {
        return getFolders(treeId, folderIds, StorageType.WORKING, storageParameters);
    }

    @Override
    public Folder getFolder(String treeId, String folderId, StorageType storageType, StorageParameters storageParameters) throws OXException {
        /*
         * Try from cache
         */
        Folder folder = getCloneFromCache(treeId, folderId, storageParameters);
        if (null != folder) {
            return folder;
        }
        /*
         * Load folder from appropriate storage
         */
        folder = loadFolder(treeId, folderId, storageType, storageParameters);
        /*
         * Check if folder is cacheable
         */
        if (folder.isCacheable()) {
            /*
             * Put to cache and return a cloned version
             */
            putFolder(folder, treeId, storageParameters, false);
            return (Folder) folder.clone();
        }
        /*
         * Return as-is since not cached
         */
        return folder;
    }

    private Folder getCloneFromCache(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        Folder folder = getRefFromCache(treeId, folderId, storageParameters);
        return null == folder ? null : (Folder) folder.clone();
    }

    /**
     * Gets the folder reference from cache.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param params The storage parameters
     * @return The folder or <code>null</code> on cache miss
     * @throws OXException
     */
    protected Folder getRefFromCache(String treeId, String folderId, StorageParameters params) throws OXException {
        int contextId = params.getContextId();
        /*
         * Try global cache key
         */
        if (folderId.length() <= 0 || Strings.isDigit(folderId.charAt(0))) {
            Folder folder = (Folder) globalCache.getFromGroup(newCacheKey(folderId, treeId), Integer.toString(contextId));
            if (null != folder) {
                /*
                 * Return version from global cache
                 */
                return folder;
            }
        }
        /*
         * Try user cache key
         */
        FolderMap folderMap = optFolderMapFor(params);
        if (null != folderMap) {
            Folder folder = folderMap.get(folderId, treeId, params.getSession());
            if (null != folder) {
                /*
                 * Return version from user-bound cache
                 */
                if (folder instanceof RemoveAfterAccessFolder) {
                    RemoveAfterAccessFolder raaf = (RemoveAfterAccessFolder) folder;
                    int fUserId = raaf.getUserId();
                    int fContextId = raaf.getContextId();
                    if ((fUserId >= 0 && params.getUserId() != fUserId) || (fContextId >= 0 && params.getContextId() != fContextId)) {
                        return null;
                    }
                }
                LOG.debug("Locally loaded folder {} from context {} for user {}", folderId, contextId, params.getUserId());
                return folder;
            }
        }
        /*
         * Cache miss
         */
        return null;
    }

    @Override
    public List<Folder> getFolders(String treeId, List<String> folderIds, StorageType storageType, StorageParameters storageParameters) throws OXException {
        int size = folderIds.size();
        Folder[] ret = new Folder[size];
        TObjectIntMap<String> toLoad = new TObjectIntHashMap<String>(size);
        /*
         * Get the ones from cache
         */
        for (int i = 0; i < size; i++) {
            /*
             * Try from cache
             */
            String folderId = folderIds.get(i);
            Folder folder = getCloneFromCache(treeId, folderId, storageParameters);
            if (null == folder) {
                /*
                 * Cache miss; Load from storage
                 */
                toLoad.put(folderId, i);
            } else {
                /*
                 * Cache hit
                 */
                ret[i] = folder;
            }
        }
        /*
         * Load the ones from storage
         */
        if (!toLoad.isEmpty()) {
            Map<String, Folder> fromStorage = loadFolders(
                treeId,
                Arrays.asList(toLoad.keys(new String[toLoad.size()])),
                storageType,
                storageParameters);
            /*
             * Fill return value
             */
            for (Entry<String, Folder> entry : fromStorage.entrySet()) {
                Folder folder = entry.getValue();
                int index = toLoad.get(entry.getKey());
                /*
                 * Put into cache
                 */
                if (folder.isCacheable()) {
                    /*
                     * Put to cache and create a cloned version
                     */
                    putFolder(folder, treeId, storageParameters, false);
                    folder = (Folder) folder.clone();
                }
                ret[index] = folder;
            }
        }
        /*
         * Return
         */
        List<Folder> l = new ArrayList<Folder>(ret.length);
        for (Folder folder : ret) {
            if (null != folder) {
                l.add(folder);
            }
        }
        return l;
    }

    @Override
    public FolderType getFolderType() {
        return CacheFolderType.getInstance();
    }

    @Override
    public StoragePriority getStoragePriority() {
        return StoragePriority.HIGHEST;
    }

    @Override
    public SortableId[] getVisibleFolders(String treeId, ContentType contentType, Type type, StorageParameters storageParameters) throws OXException {
        FolderStorage folderStorage = registry.getFolderStorageByContentType(treeId, contentType);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType);
        }
        boolean started = startTransaction(Mode.WRITE_AFTER_READ, storageParameters, folderStorage);
        try {
            SortableId[] ret = folderStorage.getVisibleFolders(treeId, contentType, type, storageParameters);
            if (started) {
                folderStorage.commitTransaction(storageParameters);
                started = false;
            }
            return ret;
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
        }
    }

    @Override
    public SortableId[] getUserSharedFolders(String treeId, ContentType contentType, StorageParameters storageParameters) throws OXException {
        FolderStorage folderStorage = registry.getFolderStorageByContentType(treeId, contentType);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType);
        }
        boolean started = startTransaction(Mode.WRITE_AFTER_READ, storageParameters, folderStorage);
        try {
            SortableId[] ret = folderStorage.getUserSharedFolders(treeId, contentType, storageParameters);
            if (started) {
                folderStorage.commitTransaction(storageParameters);
                started = false;
            }
            return ret;
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
        }
    }

    @Override
    public SortableId[] getSubfolders(final String treeId, final String parentId, final StorageParameters storageParameters) throws OXException {
        Folder parent = getFolder(treeId, parentId, storageParameters);
        String[] subfolders = ROOT_ID.equals(parentId) ? null : parent.getSubfolderIDs();
        if (null != subfolders) {
            SortableId[] ret = new SortableId[subfolders.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = new CacheSortableId(subfolders[i], i, null);
            }
            return ret;
        }

        // Get needed storages
        FolderStorage[] neededStorages = registry.getFolderStoragesForParent(treeId, parentId);
        if (0 == neededStorages.length) {
            return new SortableId[0];
        }

        try {
            java.util.List<SortableId> allSubfolderIds;
            if (1 == neededStorages.length) {
                FolderStorage neededStorage = neededStorages[0];
                boolean started = neededStorage.startTransaction(storageParameters, false);
                try {
                    allSubfolderIds = Arrays.asList(neededStorage.getSubfolders(treeId, parentId, storageParameters));
                    if (started) {
                        neededStorage.commitTransaction(storageParameters);
                        started = false;
                    }
                } finally {
                    if (started) {
                        neededStorage.rollback(storageParameters);
                    }
                }
            } else {
                allSubfolderIds = new LinkedList<SortableId>();

                // Query storages (except first one) using dedicated threads
                CompletionService<java.util.List<SortableId>> completionService = new ThreadPoolCompletionService<java.util.List<SortableId>>(
                    ThreadPools.getThreadPool()).setTrackable(true);
                int submittedTasks = 0;
                for (int i = 1; i < neededStorages.length; i++) {
                    final FolderStorage neededStorage = neededStorages[i];
                    completionService.submit(new TrackableCallable<java.util.List<SortableId>>() {

                        @Override
                        public java.util.List<SortableId> call() throws Exception {
                            StorageParameters newParameters = newStorageParameters(storageParameters);
                            boolean started = neededStorage.startTransaction(newParameters, false);
                            try {
                                java.util.List<SortableId> l = Arrays.asList(neededStorage.getSubfolders(treeId, parentId, newParameters));
                                if (started) {
                                    neededStorage.commitTransaction(newParameters);
                                    started = false;
                                }
                                return l;
                            } finally {
                                if (started) {
                                    neededStorage.rollback(newParameters);
                                }
                            }
                        }
                    });
                    submittedTasks++;
                }

                // Query the first one with this thread
                {
                    FolderStorage neededStorage = neededStorages[0];
                    boolean started = neededStorage.startTransaction(storageParameters, false);
                    try {
                        java.util.List<SortableId> l = Arrays.asList(neededStorage.getSubfolders(treeId, parentId, storageParameters));
                        if (started) {
                            neededStorage.commitTransaction(storageParameters);
                            started = false;
                        }
                        allSubfolderIds.addAll(l);
                    } finally {
                        if (started) {
                            neededStorage.rollback(storageParameters);
                        }
                    }
                }

                // Wait for completion
                List<List<SortableId>> results = ThreadPools.takeCompletionService(completionService, submittedTasks, FACTORY);
                for (List<SortableId> result : results) {
                    allSubfolderIds.addAll(result);
                }
            }

            // Sort them
            Collections.sort(allSubfolderIds);
            return allSubfolderIds.toArray(new SortableId[allSubfolderIds.size()]);
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public ContentType[] getSupportedContentTypes() {
        return new ContentType[0];
    }

    @Override
    public void rollback(StorageParameters params) {
        // Nothing to do
    }

    @Override
    public boolean startTransaction(StorageParameters parameters, boolean modify) throws OXException {
        return false;
    }

    @Override
    public void updateFolder(Folder folder, StorageParameters storageParameters) throws OXException {
        String treeId = folder.getTreeID();
        /*
         * Perform update operation via non-cache storage
         */
        String oldFolderId = folder.getID();
        Folder storageVersion = getCloneFromCache(treeId, oldFolderId, storageParameters);
        if (null == storageVersion) {
            storageVersion = getFolder(treeId, oldFolderId, storageParameters);
        }
        boolean isMove = null != folder.getParentID();
        String oldParentId = storageVersion.getParentID();
        {
            UpdatePerformer updatePerformer = newUpdatePerformer(storageParameters);
            updatePerformer.setCheck4Duplicates(false);
            updatePerformer.doUpdate(folder, storageParameters.getTimeStamp());

            Set<OXException> warnings = updatePerformer.getWarnings();
            if (null != warnings) {
                for (OXException warning : warnings) {
                    storageParameters.addWarning(warning);
                }
            }
        }
        /*
         * Get folder from appropriate storage
         */
        String newFolderId = folder.getID();
        if (null == newFolderId) {
            // cancel cache invalidations if no folder identifier set
            return;
        }
        /*
         * Refresh/Invalidate folder
         */
        Folder updatedFolder = loadFolder(treeId, newFolderId, StorageType.WORKING, true, storageParameters);
        int userId = storageParameters.getUserId();
        int contextId = storageParameters.getContextId();
        {
            FolderMapManagement folderMapManagement = FolderMapManagement.getInstance();
            List<String> ids = new ArrayList<String>(isMove ? Arrays.asList(oldFolderId, oldParentId, updatedFolder.getParentID()) : Arrays.asList(oldFolderId, oldParentId));
            folderMapManagement.dropHierarchyFor(ids, treeId, userId, contextId);
            if (!treeId.equals(realTreeId)) {
                folderMapManagement.dropHierarchyFor(ids, realTreeId, userId, contextId);
            }

            List<Serializable> keys = new LinkedList<Serializable>();
            if (Tools.isGlobalId(oldFolderId)) {
                keys.add(newCacheKey(oldFolderId, treeId));
            }
            if (isMove) {
                if (Tools.isGlobalId(oldParentId)) {
                    keys.add(newCacheKey(oldParentId, treeId));
                }
                if (Tools.isGlobalId(updatedFolder.getParentID())) {
                    keys.add(newCacheKey(updatedFolder.getParentID(), treeId));
                }
            }
            if (!treeId.equals(realTreeId)) {
                if (Tools.isGlobalId(oldFolderId)) {
                    keys.add(newCacheKey(oldFolderId, realTreeId));
                }
                if (isMove) {
                    if (Tools.isGlobalId(oldParentId)) {
                        keys.add(newCacheKey(oldParentId, realTreeId));
                    }
                    if (Tools.isGlobalId(updatedFolder.getParentID())) {
                        keys.add(newCacheKey(updatedFolder.getParentID(), realTreeId));
                    }
                }
            }
            if (!keys.isEmpty()) {
                globalCache.removeFromGroup(keys, Integer.toString(contextId));
            }

            registry.clearCaches(storageParameters.getUserId(), storageParameters.getContextId());
        }

        /*
         * Put updated folder
         */
        if (isMove) {
            /*
             * Reload folders
             */
            Folder f = loadFolder(realTreeId, newFolderId, StorageType.WORKING, true, storageParameters);
            if (f.isCacheable()) {
                putFolder(f, realTreeId, storageParameters, true);
            }
            f = loadFolder(realTreeId, oldParentId, StorageType.WORKING, true, storageParameters);
            if (f.isCacheable()) {
                putFolder(f, realTreeId, storageParameters, true);
            }
            f = loadFolder(realTreeId, updatedFolder.getParentID(), StorageType.WORKING, true, storageParameters);
            if (f.isCacheable()) {
                putFolder(f, realTreeId, storageParameters, true);
            }
        } else {
            Folder f = loadFolder(realTreeId, newFolderId, StorageType.WORKING, true, storageParameters);
            if (f.isCacheable()) {
                putFolder(f, realTreeId, storageParameters, true);
            }
        }
        if (updatedFolder.isCacheable()) {
            putFolder(updatedFolder, treeId, storageParameters, true);
        }
    }

    @Override
    public boolean containsFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        return containsFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    @Override
    public String[] getModifiedFolderIDs(String treeId, Date timeStamp, ContentType[] includeContentTypes, StorageParameters storageParameters) throws OXException {
        return getChangedFolderIDs(0, treeId, timeStamp, includeContentTypes, storageParameters);
    }

    @Override
    public String[] getDeletedFolderIDs(String treeId, Date timeStamp, StorageParameters storageParameters) throws OXException {
        return getChangedFolderIDs(1, treeId, timeStamp, null, storageParameters);
    }

    private String[] getChangedFolderIDs(int index, String treeId, Date timeStamp, ContentType[] includeContentTypes, StorageParameters storageParameters) throws OXException {
        Session session = storageParameters.getSession();
        /*
         * Perform update operation via non-cache storage
         */
        UserizedFolder[] folders;
        boolean ignoreDelete = index == 0;
        if (null == session) {
            folders = new UpdatesPerformer(
                storageParameters.getUser(),
                storageParameters.getContext(),
                storageParameters.getDecorator(),
                registry).doUpdates(treeId, timeStamp, ignoreDelete, includeContentTypes)[index];
        } else {
            folders = new UpdatesPerformer(ServerSessionAdapter.valueOf(session), storageParameters.getDecorator(), registry).doUpdates(
                treeId,
                timeStamp,
                ignoreDelete,
                includeContentTypes)[index];
        }
        if (null == folders || folders.length == 0) {
            return new String[0];
        }
        String[] ids = new String[folders.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = folders[i].getID();
        }
        return ids;
    }

    @Override
    public boolean containsFolder(String treeId, String folderId, StorageType storageType, StorageParameters storageParameters) throws OXException {
        FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        boolean started = startTransaction(Mode.WRITE_AFTER_READ, storageParameters, storage);
        try {
            boolean contains = storage.containsFolder(treeId, folderId, storageType, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
                started = false;
            }
            return contains;
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (started) {
                storage.rollback(storageParameters);
            }
        }
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++ ++ + HELPERS + ++ ++++++++++++++++++++++++++++++++++++
     */

    private boolean startTransaction(Mode mode, StorageParameters storageParameters, FolderStorage storage) throws OXException {
        if (storage instanceof AfterReadAwareFolderStorage) {
            return ((AfterReadAwareFolderStorage) storage).startTransaction(storageParameters, mode);
        }
        return storage.startTransaction(storageParameters, Mode.READ.equals(mode) ? false : true);
    }

    /**
     * Creates the cache key for specified folder ID and tree ID pair.
     *
     * @param folderId The folder ID
     * @param treeId The tree ID
     * @return The cache key
     * @throws OXException If cache service is absent
     */
    private CacheKey newCacheKey(String folderId, String treeId) throws OXException {
        CacheService cacheService = this.cacheService;
        if (null == cacheService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(CacheService.class.getSimpleName());
        }
        return cacheService.newCacheKey(1, treeId, folderId);
    }

    private boolean existsFolder(String treeId, String folderId, StorageType storageType, StorageParameters storageParameters) throws OXException {
        FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        boolean started = storage.startTransaction(storageParameters, false);
        try {
            boolean exists = storage.containsFolder(treeId, folderId, storageType, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
                started = false;
            }
            return exists;
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (started) {
                storage.rollback(storageParameters);
            }
        }
    }

    /**
     * Loads denoted folder from un-cached storage.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param storageType The storage type
     * @param storageParameters The storage parameters
     * @return The loaded folder
     * @throws OXException If loading folder fails
     */
    public Folder loadFolder(String treeId, String folderId, StorageType storageType, StorageParameters storageParameters) throws OXException {
        return loadFolder(treeId, folderId, storageType, false, storageParameters);
    }

    private Folder loadFolder(String treeId, String folderId, StorageType storageType, boolean readWrite, StorageParameters storageParameters) throws OXException {
        FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        boolean started = startTransaction(readWrite ? Mode.WRITE_AFTER_READ : Mode.READ, storageParameters, storage);
        boolean rollback = true;
        try {
            storageParameters.setIgnoreCache(Boolean.valueOf(readWrite));
            Folder folder = storage.getFolder(treeId, folderId, storageType, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
            }
            rollback = false;
            return folder;
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            storageParameters.setIgnoreCache(null);
            if (started && rollback) {
                storage.rollback(storageParameters);
            }
        }
    }

    private String[] loadAllSubfolders(String treeId, Folder folder, boolean readWrite, StorageParameters storageParameters) throws OXException {
        Set<FolderStorage> openedStorages = new HashSet<FolderStorage>(2);
        Set<String> ids = new HashSet<String>(16);
        boolean rollback = true;
        try {
            String[] subfolderIds = folder.getSubfolderIDs();
            if (null == subfolderIds) {
                loadAllSubfolders(treeId, folder.getID(), readWrite, storageParameters, ids, openedStorages);
            } else {
                ids.addAll(Arrays.asList(subfolderIds));
                for (String subfolderId : subfolderIds) {
                    loadAllSubfolders(treeId, subfolderId, readWrite, storageParameters, ids, openedStorages);
                }
            }
            for (FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
            }
            rollback = false;
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                for (FolderStorage fs : openedStorages) {
                    fs.rollback(storageParameters);
                }
            }
        }
        return ids.toArray(new String[ids.size()]);
    }

    private void loadAllSubfolders(String treeId, String folderId, boolean readWrite, StorageParameters storageParameters, Set<String> ids, Set<FolderStorage> openedStorages) throws OXException {
        FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        checkOpenedStorage(storage, readWrite, openedStorages, storageParameters);
        try {
            SortableId[] subfolders = storage.getSubfolders(treeId, folderId, storageParameters);
            for (SortableId sortableId : subfolders) {
                String id = sortableId.getId();
                loadAllSubfolders(treeId, id, readWrite, storageParameters, ids, openedStorages);
                ids.add(id);
            }
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    protected void checkOpenedStorage(FolderStorage checkMe, boolean modify, java.util.Collection<FolderStorage> openedStorages, StorageParameters storageParameters) throws OXException {
        if (openedStorages.contains(checkMe)) {
            // Passed storage is already opened
            return;
        }
        // Passed storage has not been opened before. Open now and add to collection
        if (checkMe.startTransaction(storageParameters, modify)) {
            openedStorages.add(checkMe);
        }
    }

    private Map<String, Folder> loadFolders(final String treeId, List<String> folderIds, final StorageType storageType, StorageParameters storageParameters) throws OXException {
        /*
         * Collect by folder storage
         */
        int size = folderIds.size();
        Map<FolderStorage, TIntList> map = new HashMap<FolderStorage, TIntList>(4);
        for (int i = 0; i < size; i++) {
            String id = folderIds.get(i);
            FolderStorage tmp = registry.getFolderStorage(treeId, id);
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
        CompletionService<Object> completionService;
        final StorageParametersProvider paramsProvider;
        if (1 == map.size()) {
            completionService = new CallerRunsCompletionService<Object>();
            paramsProvider = new InstanceStorageParametersProvider(storageParameters);
        } else {
            completionService = new ThreadPoolCompletionService<Object>(CacheServiceRegistry.getServiceRegistry().getService(
                ThreadPoolService.class,
                true)).setTrackable(true);

            Session session = storageParameters.getSession();
            paramsProvider = null == session ? new SessionStorageParametersProvider(
                storageParameters.getUser(),
                storageParameters.getContext()) : new SessionStorageParametersProvider((ServerSession) session);
        }
        /*
         * Create destination map
         */
        final Map<String, Folder> ret = new ConcurrentHashMap<String, Folder>(size, 0.9f, 1);
        int taskCount = 0;
        for (java.util.Map.Entry<FolderStorage, TIntList> entry : map.entrySet()) {
            final FolderStorage fs = entry.getKey();
            /*
             * Create the list of IDs to load with current storage
             */
            final List<String> ids;
            {
                int[] indexes = entry.getValue().toArray();
                ids = new ArrayList<String>(indexes.length);
                for (int index : indexes) {
                    ids.add(folderIds.get(index));
                }
            }
            /*
             * Submit task
             */
            completionService.submit(new TrackableCallable<Object>() {

                @Override
                public Object call() throws Exception {
                    StorageParameters newParameters = paramsProvider.getStorageParameters();
                    boolean started = !DatabaseFolderType.getInstance().equals(fs.getFolderType()) && fs.startTransaction(
                        newParameters,
                        false);
                    try {
                        /*
                         * Load them & commit
                         */
                        List<Folder> folders = fs.getFolders(treeId, ids, storageType, newParameters);
                        if (started) {
                            fs.commitTransaction(newParameters);
                            started = false;
                        }
                        /*
                         * Fill into map
                         */
                        for (Folder folder : folders) {
                            ret.put(folder.getID(), folder);
                        }
                        /*
                         * Return
                         */
                        return null;
                    } catch (RuntimeException e) {
                        throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                    } finally {
                        if (started) {
                            fs.rollback(newParameters);
                        }
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

    /**
     * Creates a new storage parameter instance.
     *
     * @return A new storage parameter instance.
     */
    static StorageParameters newStorageParameters(StorageParameters source) {
        Session session = source.getSession();
        if (null == session) {
            return new StorageParametersImpl(source.getUser(), source.getContext());
        }
        return new StorageParametersImpl((ServerSession) session, source.getUser(), source.getContext());
    }

    private static FolderMap getFolderMapFor(StorageParameters parameters) {
        return FolderMapManagement.getInstance().getFor(parameters.getContextId(), parameters.getUserId());
    }

    private static FolderMap optFolderMapFor(StorageParameters parameters) {
        return FolderMapManagement.getInstance().optFor(parameters.getUserId(), parameters.getContextId());
    }

    /**
     * Drops entries associated with specified user in given context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public static void dropUserEntries(int userId, int contextId) {
        FolderMap folderMap = FolderMapManagement.getInstance().optFor(userId, contextId);
        if (null != folderMap) {
            folderMap.clear();
        }
    }

}
