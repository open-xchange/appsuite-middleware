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

package com.openexchange.folderstorage.cache;

import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.commons.logging.Log;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.concurrent.CallerRunsCompletionService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.AfterReadAwareFolderStorage;
import com.openexchange.folderstorage.AfterReadAwareFolderStorage.Mode;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.RemoveAfterAccessFolder;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StoragePriority;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.cache.lock.TreeLockManagement;
import com.openexchange.folderstorage.cache.memory.FolderMap;
import com.openexchange.folderstorage.cache.memory.FolderMapManagement;
import com.openexchange.folderstorage.database.DatabaseFolderType;
import com.openexchange.folderstorage.internal.StorageParametersImpl;
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
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.StringAllocator;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.threadpool.RefusedExecutionBehavior;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.ThreadPools.TrackableCallable;
import com.openexchange.threadpool.behavior.AbortBehavior;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link CacheFolderStorage} - The cache folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheFolderStorage implements FolderStorage {

    protected static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(CacheFolderStorage.class));

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
        final Cache cache = globalCache;
        if (null != cache) {
            try {
                cache.clear();
            } catch (final Exception e) {
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
    public void removeFromGlobalCache(final String folderId, final String treeId, final int contextId) {
        final Cache cache = globalCache;
        if (null != cache) {
            cache.removeFromGroup(newCacheKey(folderId, treeId), Integer.toString(contextId));
        }
    }

    @Override
    public void clearCache(final int userId, final int contextId) {
        if (contextId <= 0) {
            return;
        }
        final Cache cache = globalCache;
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
     * Clears this cache with respect to specified session.
     *
     * @param session The session
     */
    public void clear(final Session session) {
        clearCache(session.getUserId(), session.getContextId());
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
        final CacheService service = cacheService;
        final Cache cache = globalCache;
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

    protected static final Set<String> IGNORABLES = RemoveAfterAccessFolder.IGNORABLES;

    @Override
    public void checkConsistency(final String treeId, final StorageParameters storageParameters) throws OXException {
        final Lock rlock = readLockFor(treeId, storageParameters);
        acquire(rlock);
        try {
            for (final FolderStorage folderStorage : registry.getFolderStoragesForTreeID(treeId)) {
                final boolean started = folderStorage.startTransaction(storageParameters, false);
                try {
                    folderStorage.checkConsistency(treeId, storageParameters);
                    if (started) {
                        folderStorage.commitTransaction(storageParameters);
                    }
                } catch (final OXException e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw e;
                } catch (final Exception e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
            final String realTreeId = this.realTreeId;
            if (realTreeId.equals(treeId)) {
                try {
                    final ServerSession session = ServerSessionAdapter.valueOf(storageParameters.getSession());
                    final ServiceRegistry serviceRegistry = CacheServiceRegistry.getServiceRegistry();
                    final ThreadPoolService threadPool = ThreadPools.getThreadPool();
                    final RefusedExecutionBehavior<Object> behavior = AbortBehavior.getInstance();
                    /*
                     * Traverse mail accounts in separate task
                     */
                    final Log log = LOG;
                    final boolean debugEnabled = log.isDebugEnabled();
                    final Runnable task = new Runnable() {

                        @Override
                        public void run() {
                            try {
                                final long st = debugEnabled ? System.currentTimeMillis() : 0L;
                                final StorageParameters params = newStorageParameters(storageParameters);
                                params.putParameter(MailFolderType.getInstance(), StorageParameters.PARAM_ACCESS_FAST, Boolean.FALSE);
                                if (session.getUserPermissionBits().isMultipleMailAccounts()) {
                                    final MailAccountStorageService storageService =
                                        serviceRegistry.getService(MailAccountStorageService.class, true);
                                    final MailAccount[] accounts =
                                        storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
                                    /*
                                     * Gather tasks...
                                     */
                                    final List<Runnable> tasks = new ArrayList<Runnable>(accounts.length);
                                    for (final MailAccount mailAccount : accounts) {
                                        final int accountId = mailAccount.getId();
                                        if (accountId != MailAccount.DEFAULT_ID && !IGNORABLES.contains(mailAccount.getMailProtocol())) {
                                            final String folderId = prepareFullname(accountId, MailFolder.DEFAULT_FOLDER_ID);
                                            /*
                                             * Check if already present
                                             */
                                            final Folder rootFolder = getRefFromCache(realTreeId, folderId, params);
                                            if (null == rootFolder) {
                                                final Runnable mailAccountTask = new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        try {
                                                            final long st2 = debugEnabled ? System.currentTimeMillis() : 0L;
                                                            /*
                                                             * Load it
                                                             */
                                                            final Folder rootFolder = loadFolder(realTreeId, folderId, StorageType.WORKING, params);
                                                            putFolder(rootFolder, realTreeId, params, false);
                                                            final String[] subfolderIDs = rootFolder.getSubfolderIDs();
                                                            if (null != subfolderIDs) {
                                                                for (final String subfolderId : subfolderIDs) {
                                                                    final Folder folder =
                                                                        loadFolder(realTreeId, subfolderId, StorageType.WORKING, params);
                                                                    putFolder(folder, realTreeId, params, false);
                                                                }
                                                            }
                                                            if (debugEnabled) {
                                                                final StringAllocator tmp = new StringAllocator(64);
                                                                tmp.append("CacheFolderStorage.checkConsistency(): ");
                                                                tmp.append("Loading external root folder \"");
                                                                tmp.append(mailAccount.generateMailServerURL()).append("\" took ");
                                                                tmp.append((System.currentTimeMillis() - st2)).append("msec");
                                                                log.debug(tmp.toString());
                                                            }
                                                        } catch (final Exception e) {
                                                            // Pre-Accessing external account folder failed.
                                                            LOG.debug(e.getMessage(), e);
                                                        }
                                                    }
                                                };
                                                tasks.add(mailAccountTask);
                                            }
                                        }
                                    }
                                    if (!tasks.isEmpty()) {
                                        for (final Runnable task : tasks) {
                                            threadPool.submit(ThreadPools.trackableTask(task), behavior);
                                        }
                                    }
                                }
                                if (debugEnabled) {
                                    final StringAllocator tmp = new StringAllocator(64);
                                    tmp.append("CacheFolderStorage.checkConsistency(): Submitting loading external root folders took ");
                                    tmp.append((System.currentTimeMillis() - st)).append("msec");
                                    log.debug(tmp.toString());
                                }
                            } catch (final Exception e) {
                                LOG.debug(e.getMessage(), e);
                            }
                        }
                    };
                    threadPool.submit(ThreadPools.trackableTask(task), behavior);
                } catch (final Exception e) {
                    LOG.debug(e.getMessage(), e);
                }
            }
        } finally {
            rlock.unlock();
        }
    }

    /**
     * Checks if given folder storage is already contained in collection of opened storages. If yes, this method terminates immediately.
     * Otherwise the folder storage is opened according to specified modify flag and is added to specified collection of opened storages.
     */
    protected static void checkOpenedStorage(final FolderStorage checkMe, final StorageParameters params, final boolean modify, final java.util.Collection<FolderStorage> openedStorages) throws OXException {
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
    public void restore(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        final Lock lock = writeLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
            if (null == storage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
            }
            final boolean started = storage.startTransaction(storageParameters, false);
            try {
                storage.restore(treeId, folderId, storageParameters);
                if (started) {
                    storage.commitTransaction(storageParameters);
                }
            } catch (final OXException e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                clear(storageParameters.getSession());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Folder prepareFolder(final String treeId, final Folder folder, final StorageParameters storageParameters) throws OXException {
        final Lock lock = readLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            final String folderId = folder.getID();
            final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
            if (null == storage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
            }
            final boolean started = storage.startTransaction(storageParameters, false);
            try {
                final Folder preparedFolder = storage.prepareFolder(treeId, folder, storageParameters);
                if (started) {
                    storage.commitTransaction(storageParameters);
                }
                if (preparedFolder.isCacheable() && preparedFolder.isGlobalID() != folder.isGlobalID()) {
                    putFolder(preparedFolder, treeId, storageParameters, false);
                }
                return preparedFolder;
            } catch (final OXException e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                if (OXFolderExceptionCode.NOT_EXISTS.equals(e) || FolderExceptionErrorMessage.NOT_FOUND.equals(e)) {
                    return folder;
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        } finally {
            lock.unlock();
        }
    }

    private PathPerformer newPathPerformer(final StorageParameters storageParameters) throws OXException {
        final Session session = storageParameters.getSession();
        if (null == session) {
            return new PathPerformer(storageParameters.getUser(), storageParameters.getContext(), null, registry);
        }
        return new PathPerformer(ServerSessionAdapter.valueOf(session), null, registry);
    }

    @Override
    public ContentType getDefaultContentType() {
        return null;
    }

    @Override
    public void commitTransaction(final StorageParameters params) throws OXException {
        // Nothing to do
    }

    @Override
    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws OXException {
        final String treeId = folder.getTreeID();
        final Lock lock = writeLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            final Session session = storageParameters.getSession();
            /*
             * Perform create operation via non-cache storage
             */
            final CreatePerformer createPerformer;
            if (null == session) {
                createPerformer = new CreatePerformer(storageParameters.getUser(), storageParameters.getContext(), storageParameters.getDecorator(), registry);
            } else {
                createPerformer = new CreatePerformer(ServerSessionAdapter.valueOf(session), storageParameters.getDecorator(), registry);
            }
            createPerformer.setCheck4Duplicates(false);
            final String folderId = createPerformer.doCreate(folder);
            /*
             * Get folder from appropriate storage
             */
            final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
            if (null == storage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
            }
            /*
             * Load created folder from real tree
             */
            final int contextId = storageParameters.getContextId();
            Folder createdFolder = null;
            try {
                createdFolder = loadFolder(realTreeId, folderId, StorageType.WORKING, true, storageParameters);
                if (createdFolder.isCacheable()) {
                    putFolder(createdFolder, realTreeId, storageParameters, false);
                }
            } catch (final OXException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.warn("Newly created folder could not be loaded from appropriate storage.", e);
                } else {
                    LOG.warn("Newly created folder could not be loaded from appropriate storage.");
                }
            }
            /*
             * Remove parent from cache(s)
             */
            final FolderMapManagement folderMapManagement = FolderMapManagement.getInstance();
            final Cache cache = globalCache;
            final String sContextId = Integer.toString(contextId);
            final int userId = storageParameters.getUserId();
            for (final String tid : new String[] { treeId, realTreeId }) {
                final CacheKey cacheKey = newCacheKey(folder.getParentID(), tid);
                cache.removeFromGroup(cacheKey, sContextId);
                // Cleanse parent from caches, too
                folderMapManagement.dropFor(folder.getParentID(), treeId, userId, contextId, session);
                folderMapManagement.dropFor(folder.getParentID(), realTreeId, userId, contextId, session);
            }
            if (null != createdFolder) {
                for (final String tid : new String[] { treeId, realTreeId }) {
                    final CacheKey cacheKey = newCacheKey(createdFolder.getParentID(), tid);
                    cache.removeFromGroup(cacheKey, sContextId);
                    // Cleanse parent from caches, too
                    folderMapManagement.dropFor(folder.getParentID(), treeId, userId, contextId, session);
                    folderMapManagement.dropFor(folder.getParentID(), realTreeId, userId, contextId, session);
                }
            }
            /*
             * Load parent from real tree
             */
            Folder parentFolder = loadFolder(realTreeId, folder.getParentID(), StorageType.WORKING, true, storageParameters);
            if (parentFolder.isCacheable()) {
                putFolder(parentFolder, realTreeId, storageParameters, true);
            }
            if (null != createdFolder) {
                parentFolder = loadFolder(realTreeId, createdFolder.getParentID(), StorageType.WORKING, true, storageParameters);
                if (parentFolder.isCacheable()) {
                    putFolder(parentFolder, realTreeId, storageParameters, true);
                }
            }
        } finally {
            lock.unlock();
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
    public void putFolder(final Folder folder, final String treeId, final StorageParameters storageParameters, final boolean invalidate) throws OXException {
        /*
         * Put to cache
         */
        if (folder.isGlobalID()) {
            globalCache.putInGroup(newCacheKey(folder.getID(), treeId), Integer.toString(storageParameters.getContextId()), folder, invalidate);
        } else {
            getFolderMapFor(storageParameters.getSession()).put(treeId, folder, storageParameters.getSession());
        }
    }

    /**
     * Removes specified folder and all of its predecessor folders from cache.
     *
     * @param id The folder identifier
     * @param treeId The tree identifier
     * @param singleOnly <code>true</code> if only specified folder should be removed; otherwise <code>false</code> for complete folder's
     *            path to root folder
     * @param session The session providing user information
     * @throws OXException If removal fails
     */
    public void removeFromCache(final String id, final String treeId, final boolean singleOnly, final Session session) throws OXException {
        final Lock lock = TreeLockManagement.getInstance().getFor(treeId, session).writeLock();
        acquire(lock);
        try {
            if (singleOnly) {
                removeSingleFromCache(id, treeId, session.getUserId(), session, true);
            } else {
                removeFromCache(id, treeId, session, new PathPerformer(ServerSessionAdapter.valueOf(session), null, registry));
            }
        } finally {
            lock.unlock();
        }
    }

    private void removeFromCache(final String id, final String treeId, final Session session, final PathPerformer pathPerformer) throws OXException {
        if (null == id) {
            return;
        }
        {
            List<String> ids;
            try {
                if (existsFolder(treeId, id, StorageType.WORKING, pathPerformer.getStorageParameters())) {
                    final UserizedFolder[] path = pathPerformer.doPath(treeId, id, true);
                    ids = new ArrayList<String>(path.length);
                    for (final UserizedFolder userizedFolder : path) {
                        ids.add(userizedFolder.getID());
                    }
                } else {
                    ids = Collections.singletonList(id);
                }
            } catch (final Exception e) {
                final org.apache.commons.logging.Log log =
                    com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CacheFolderStorage.class));
                if (log.isDebugEnabled()) {
                    log.debug(e.getMessage(), e);
                }
                try {
                    ids = new ArrayList<String>(Arrays.asList(pathPerformer.doForcePath(treeId, id, true)));
                } catch (final Exception e1) {
                    if (log.isDebugEnabled()) {
                        log.debug(e1.getMessage(), e1);
                    }
                    ids = Collections.singletonList(id);
                }
            }
            final int contextId = session.getContextId();
            final int userId = session.getUserId();
            final Cache cache = globalCache;
            final FolderMapManagement folderMapManagement = FolderMapManagement.getInstance();
            if (realTreeId.equals(treeId)) {
                for (final String folderId : ids) {
                    cache.removeFromGroup(newCacheKey(folderId, treeId), Integer.toString(contextId));
                    folderMapManagement.dropFor(folderId, treeId, userId, contextId);
                }
            } else {
                for (final String folderId : ids) {
                    cache.removeFromGroup(newCacheKey(folderId, treeId), Integer.toString(contextId));
                    folderMapManagement.dropFor(folderId, treeId, userId, contextId);
                    // Now for real tree, too
                    cache.removeFromGroup(newCacheKey(folderId, realTreeId), Integer.toString(contextId));
                    folderMapManagement.dropFor(folderId, realTreeId, userId, contextId);
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
    public void removeSingleFromCache(final String id, final String treeId, final int userId, final Session session, final boolean deleted) {
        removeSingleFromCache(id, treeId, userId, session.getContextId(), deleted, session);
    }

    /**
     * Removes a single folder from cache.
     *
     * @param id The folder identifier
     * @param treeId The tree identifier
     * @param contextId The context identifier
     */
    public void removeSingleFromCache(final String id, final String treeId, final int optUserId, final int contextId, final boolean deleted, final Session optSession) {
        removeSingleFromCache(id, treeId, optUserId, contextId, deleted, false, optSession);
    }

    /**
     * Removes a single folder from cache.
     *
     * @param id The folder identifier
     * @param treeId The tree identifier
     * @param contextId The context identifier
     */
    public void removeSingleFromCache(final String id, final String treeId, final int optUserId, final int contextId, final boolean deleted, final boolean userCacheOnly, final Session optSession) {
        final Lock lock = optUserId > 0 ? TreeLockManagement.getInstance().getFor(treeId, optUserId, contextId).writeLock() : Session.EMPTY_LOCK;
        try {
            acquire(lock);
        } catch (final OXException e) {
            // Ignore
            return;
        }
        try {
            final String sContextId = Integer.toString(contextId);
            final Cache cache = userCacheOnly ? null : globalCache;
            // Perform for given folder tree and real tree
            for (final String tid : new HashSet<String>(Arrays.asList(treeId, realTreeId))) {
                if (null != cache) {
                    final CacheKey cacheKey = newCacheKey(id, tid);
                    if (deleted) {
                        final Folder cachedFolder = (Folder) cache.getFromGroup(cacheKey, sContextId);
                        if (null != cachedFolder) {
                            /*
                             * Drop parent, too
                             */
                            final String parentID = cachedFolder.getParentID();
                            if (null != parentID) {
                                cache.removeFromGroup(newCacheKey(parentID, tid), sContextId);
                            }
                        }
                    }
                    cache.removeFromGroup(cacheKey, sContextId);
                }
                final FolderMapManagement folderMapManagement = FolderMapManagement.getInstance();
                if (optUserId > 0) {
                    final FolderMap folderMap = folderMapManagement.optFor(optUserId, contextId);
                    if (null != folderMap) {
                        if (deleted) {
                            final Folder cachedFolder = folderMap.get(id, tid, optSession);
                            if (null != cachedFolder) {
                                /*
                                 * Drop parent, too
                                 */
                                final String parentID = cachedFolder.getParentID();
                                if (null != parentID) {
                                    folderMapManagement.dropFor(parentID, tid, optUserId, contextId, optSession);
                                }
                            }
                        }
                    }
                }
                folderMapManagement.dropFor(id, tid, optUserId, contextId, optSession);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clearFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        final Lock lock = readLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            final Session session = storageParameters.getSession();
            /*
             * Perform clear operation via non-cache storage
             */
            if (null == session) {
                new ClearPerformer(storageParameters.getUser(), storageParameters.getContext(), registry).doClear(treeId, folderId);
            } else {
                new ClearPerformer(ServerSessionAdapter.valueOf(session), registry).doClear(treeId, folderId);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void deleteFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        final Lock lock = writeLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            final String parentId;
            final String realParentId;
            final boolean cacheable;
            final boolean global;
            final int contextId = storageParameters.getContextId();
            final int userId = storageParameters.getUserId();
            final Session session = storageParameters.getSession();
            final String sContextId = Integer.toString(contextId);
            final String[] subfolderIDs;
            {
                final Folder deleteMe;
                try {
                    deleteMe = getFolder(treeId, folderId, storageParameters);
                    /*
                     * Load all subfolders
                     */
                    subfolderIDs = loadAllSubfolders(treeId, deleteMe, false, storageParameters);
                } catch (final OXException e) {
                    /*
                     * Obviously folder does not exist
                     */
                    globalCache.removeFromGroup(newCacheKey(folderId, treeId), sContextId);
                    FolderMapManagement.getInstance().dropFor(folderId, treeId, userId, contextId, session);
                    return;
                }
                {
                    final FolderMapManagement folderMapManagement = FolderMapManagement.getInstance();
                    folderMapManagement.dropFor(folderId, treeId, userId, contextId, session);
                    folderMapManagement.dropFor(folderId, realTreeId, userId, contextId, session);
                    folderMapManagement.dropFor(deleteMe.getParentID(), treeId, userId, contextId, session);
                    folderMapManagement.dropFor(deleteMe.getParentID(), realTreeId, userId, contextId, session);
                }
                cacheable = deleteMe.isCacheable();
                global = deleteMe.isGlobalID();
                parentId = deleteMe.getParentID();
                if (!realTreeId.equals(treeId)) {
                    final StorageParameters parameters = newStorageParameters(storageParameters);
                    final FolderStorage folderStorage = registry.getFolderStorage(realTreeId, folderId);
                    final boolean started = folderStorage.startTransaction(parameters, false);
                    try {
                        realParentId = folderStorage.getFolder(realTreeId, folderId, parameters).getParentID();
                        if (started) {
                            folderStorage.commitTransaction(parameters);
                        }
                    } catch (final OXException e) {
                        if (started) {
                            folderStorage.rollback(parameters);
                        }
                        throw e;
                    } catch (final RuntimeException e) {
                        if (started) {
                            folderStorage.rollback(parameters);
                        }
                        throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e);
                    }
                } else {
                    realParentId = null;
                }
            }
            if (cacheable) {
                /*
                 * Delete from cache
                 */
                if (global) {
                    globalCache.removeFromGroup(newCacheKey(folderId, treeId), sContextId);
                } else {
                    FolderMapManagement.getInstance().dropFor(folderId, treeId, userId, contextId, session);
                }
                /*
                 * ... and from parent folder's sub-folder list
                 */
                removeFromSubfolders(treeId, parentId, sContextId, session);
                if (null != realParentId) {
                    removeFromSubfolders(realTreeId, realParentId, sContextId, session);
                }
            }
            /*
             * Drop subfolders from cache
             */
            {
                for (final String subfolderId : subfolderIDs) {
                    removeSingleFromCache(subfolderId, treeId, userId, contextId, true, session);
                }
            }
            /*
             * Perform delete
             */
            if (null == session) {
                new DeletePerformer(storageParameters.getUser(), storageParameters.getContext(), registry).doDelete(
                    treeId,
                    folderId,
                    storageParameters.getTimeStamp());
            } else {
                new DeletePerformer(ServerSessionAdapter.valueOf(session), registry).doDelete(
                    treeId,
                    folderId,
                    storageParameters.getTimeStamp());
            }
            /*
             * Refresh
             */
            if (null != realParentId && !ROOT_ID.equals(realParentId)) {
                removeFromCache(realParentId, treeId, storageParameters.getSession(), newPathPerformer(storageParameters));
            }
            if (!ROOT_ID.equals(parentId)) {
                removeFromCache(parentId, treeId, storageParameters.getSession(), newPathPerformer(storageParameters));
                try {
                    final Folder parentFolder = loadFolder(treeId, parentId, StorageType.WORKING, true, storageParameters);
                    if (parentFolder.isCacheable()) {
                        putFolder(parentFolder, treeId, storageParameters, true);
                    }
                } catch (final Exception e) {
                    // Ignore
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void removeFromSubfolders(final String treeId, final String parentId, final String contextId, final Session session) {
        registry.clearCaches(session.getUserId(), session.getContextId());
        globalCache.removeFromGroup(newCacheKey(parentId, treeId), contextId);
        FolderMapManagement.getInstance().dropFor(parentId, treeId, session.getUserId(), session.getContextId(), session);
    }

    @Override
    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws OXException {
        final Lock lock = readLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            final FolderStorage storage = registry.getFolderStorageByContentType(treeId, contentType);
            if (null == storage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType);
            }
            final String folderId;
            final boolean started = storage.startTransaction(storageParameters, false);
            try {
                folderId = storage.getDefaultFolderID(user, treeId, contentType, type, storageParameters);
                if (started) {
                    storage.commitTransaction(storageParameters);
                }
            } catch (final OXException e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
            return folderId;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Type getTypeByParent(final User user, final String treeId, final String parentId, final StorageParameters storageParameters) throws OXException {
        final Lock lock = readLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            final FolderStorage storage = registry.getFolderStorage(treeId, parentId);
            if (null == storage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, parentId);
            }
            final Type type;
            final boolean started = storage.startTransaction(storageParameters, false);
            try {
                type = storage.getTypeByParent(user, treeId, parentId, storageParameters);
                if (started) {
                    storage.commitTransaction(storageParameters);
                }
            } catch (final OXException e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
            return type;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsForeignObjects(final User user, final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        final Lock lock = readLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            /*
             * Get folder storage
             */
            final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
            if (null == storage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
            }
            final boolean started = storage.startTransaction(storageParameters, false);
            try {
                final boolean containsForeignObjects = storage.containsForeignObjects(user, treeId, folderId, storageParameters);
                if (started) {
                    storage.commitTransaction(storageParameters);
                }
                return containsForeignObjects;
            } catch (final OXException e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        final Lock lock = readLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            /*
             * Get folder storage
             */
            final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
            if (null == storage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
            }
            final boolean started = storage.startTransaction(storageParameters, false);
            try {
                final boolean isEmpty = storage.isEmpty(treeId, folderId, storageParameters);
                if (started) {
                    storage.commitTransaction(storageParameters);
                }
                return isEmpty;
            } catch (final OXException e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateLastModified(final long lastModified, final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        final Lock lock = writeLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
            if (null == storage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
            }
            final boolean started = storage.startTransaction(storageParameters, false);
            try {
                storage.updateLastModified(lastModified, treeId, folderId, storageParameters);
                if (started) {
                    storage.commitTransaction(storageParameters);
                }
            } catch (final OXException e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
            /*
             * Invalidate cache entry
             */
            removeFromCache(folderId, treeId, storageParameters.getSession(), newPathPerformer(storageParameters));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Folder getFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        return getFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    @Override
    public List<Folder> getFolders(final String treeId, final List<String> folderIds, final StorageParameters storageParameters) throws OXException {
        return getFolders(treeId, folderIds, StorageType.WORKING, storageParameters);
    }

    @Override
    public Folder getFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        final Lock lock = readLockFor(treeId, storageParameters);
        acquire(lock);
        try {
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
        } finally {
            lock.unlock();
        }
    }

    private Folder getCloneFromCache(final String treeId, final String folderId, final StorageParameters storageParameters) {
        final Folder folder = getRefFromCache(treeId, folderId, storageParameters);
        return null == folder ? null : (Folder) folder.clone();
    }

    /**
     * Gets the folder reference from cache.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param params The storage parameters
     * @return The folder or <code>null</code> on cache miss
     */
    protected Folder getRefFromCache(final String treeId, final String folderId, final StorageParameters params) {
        final int contextId = params.getContextId();
        /*
         * Try global cache key
         */
        Folder folder = (Folder) globalCache.getFromGroup(newCacheKey(folderId, treeId), Integer.toString(contextId));
        if (null != folder) {
            /*
             * Return a cloned version from global cache
             */
            return folder;
        }
        /*
         * Try user cache key
         */
        final FolderMap folderMap = optFolderMapFor(params);
        if (null != folderMap) {
            folder = folderMap.get(folderId, treeId, params.getSession());
            if (null != folder) {
                /*
                 * Return a cloned version from user-bound cache
                 */
                if (folder instanceof RemoveAfterAccessFolder) {
                    final RemoveAfterAccessFolder raaf = (RemoveAfterAccessFolder) folder;
                    final int fUserId = raaf.getUserId();
                    final int fContextId = raaf.getContextId();
                    if ((fUserId >= 0 && params.getUserId() != fUserId) || (fContextId >= 0 && params.getContextId() != fContextId)) {
                        return null;
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Locally loaded folder " + folderId + " from context " + contextId + " for user " + params.getUserId());
                }
                return folder;
            }
        }
        /*
         * Cache miss
         */
        return null;
    }

    @Override
    public List<Folder> getFolders(final String treeId, final List<String> folderIds, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        final Lock lock = readLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            final int size = folderIds.size();
            final Folder[] ret = new Folder[size];
            final TObjectIntMap<String> toLoad = new TObjectIntHashMap<String>(size);
            /*
             * Get the ones from cache
             */
            for (int i = 0; i < size; i++) {
                /*
                 * Try from cache
                 */
                final String folderId = folderIds.get(i);
                final Folder folder = getCloneFromCache(treeId, folderId, storageParameters);
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
            final Map<String, Folder> fromStorage;
            if (toLoad.isEmpty()) {
                fromStorage = Collections.emptyMap();
            } else {
                fromStorage = loadFolders(treeId, Arrays.asList(toLoad.keys(new String[toLoad.size()])), storageType, storageParameters);
            }
            /*
             * Fill return value
             */
            for (final Entry<String, Folder> entry : fromStorage.entrySet()) {
                Folder folder = entry.getValue();
                final int index = toLoad.get(entry.getKey());
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
            /*
             * Return
             */
            final List<Folder> l = new ArrayList<Folder>(ret.length);
            for (final Folder folder : ret) {
                if (null != folder) {
                    l.add(folder);
                }
            }
            return l;
        } finally {
            lock.unlock();
        }
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
    public SortableId[] getVisibleFolders(final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws OXException {
        final Lock lock = readLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            final FolderStorage folderStorage = registry.getFolderStorageByContentType(treeId, contentType);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType);
            }
            final boolean started = startTransaction(Mode.WRITE_AFTER_READ, storageParameters, folderStorage);
            try {
                final SortableId[] ret = folderStorage.getVisibleFolders(treeId, contentType, type, storageParameters);
                if (started) {
                    folderStorage.commitTransaction(storageParameters);
                }
                return ret;
            } catch (final OXException e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SortableId[] getSubfolders(final String treeId, final String parentId, final StorageParameters storageParameters) throws OXException {
        final Lock lock = readLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            final Folder parent = getFolder(treeId, parentId, storageParameters);
            final String[] subfolders = ROOT_ID.equals(parentId) ? null : parent.getSubfolderIDs();
            final SortableId[] ret;
            if (null == subfolders) {
                /*
                 * Get needed storages
                 */
                final FolderStorage[] neededStorages = registry.getFolderStoragesForParent(treeId, parentId);
                if (0 == neededStorages.length) {
                    return new SortableId[0];
                }
                try {
                    final java.util.List<SortableId> allSubfolderIds;
                    if (1 == neededStorages.length) {
                        final FolderStorage neededStorage = neededStorages[0];
                        final boolean started = neededStorage.startTransaction(storageParameters, false);
                        try {
                            allSubfolderIds = Arrays.asList(neededStorage.getSubfolders(treeId, parentId, storageParameters));
                            if (started) {
                                neededStorage.commitTransaction(storageParameters);
                            }
                        } catch (final Exception e) {
                            if (started) {
                                neededStorage.rollback(storageParameters);
                            }
                            throw e;
                        }
                    } else {
                        allSubfolderIds = new ArrayList<SortableId>(neededStorages.length * 8);
                        final CompletionService<java.util.List<SortableId>> completionService =
                            new ThreadPoolCompletionService<java.util.List<SortableId>>(ThreadPools.getThreadPool()).setTrackable(true);
                        /*
                         * Get all visible subfolders from each storage
                         */
                        for (final FolderStorage neededStorage : neededStorages) {
                            completionService.submit(new TrackableCallable<java.util.List<SortableId>>() {

                                @Override
                                public java.util.List<SortableId> call() throws Exception {
                                    final StorageParameters newParameters = newStorageParameters(storageParameters);
                                    final boolean started = neededStorage.startTransaction(newParameters, false);
                                    try {
                                        final java.util.List<SortableId> l =
                                            Arrays.asList(neededStorage.getSubfolders(treeId, parentId, newParameters));
                                        if (started) {
                                            neededStorage.commitTransaction(newParameters);
                                        }
                                        return l;
                                    } catch (final Exception e) {
                                        if (started) {
                                            neededStorage.rollback(newParameters);
                                        }
                                        throw e;
                                    }
                                }
                            });
                        }
                        /*
                         * Wait for completion
                         */
                        final List<List<SortableId>> results =
                            ThreadPools.takeCompletionService(completionService, neededStorages.length, FACTORY);
                        for (final List<SortableId> result : results) {
                            allSubfolderIds.addAll(result);
                        }
                    }
                    /*
                     * Sort them
                     */
                    Collections.sort(allSubfolderIds);
                    ret = allSubfolderIds.toArray(new SortableId[allSubfolderIds.size()]);
                } catch (final OXException e) {
                    throw e;
                } catch (final Exception e) {
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            } else {
                ret = new SortableId[subfolders.length];
                for (int i = 0; i < ret.length; i++) {
                    ret[i] = new CacheSortableId(subfolders[i], i, null);
                }
            }
            return ret;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ContentType[] getSupportedContentTypes() {
        return new ContentType[0];
    }

    @Override
    public void rollback(final StorageParameters params) {
        // Nothing to do
    }

    @Override
    public boolean startTransaction(final StorageParameters parameters, final boolean modify) throws OXException {
        return false;
    }

    @Override
    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws OXException {
        final String treeId = folder.getTreeID();
        final Lock lock = writeLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            final Session session = storageParameters.getSession();
            /*
             * Perform update operation via non-cache storage
             */
            final String oldFolderId = folder.getID();
            Folder storageVersion = getCloneFromCache(treeId, oldFolderId, storageParameters);
            if (null == storageVersion) {
                storageVersion = getFolder(treeId, oldFolderId, storageParameters);
            }
            final boolean isMove = null != folder.getParentID();
            final String oldParentId = isMove ? getFolder(treeId, oldFolderId, storageParameters).getParentID() : null;
            if (null == session) {
                final UpdatePerformer updatePerformer = new UpdatePerformer(storageParameters.getUser(), storageParameters.getContext(), storageParameters.getDecorator(), registry);
                updatePerformer.setCheck4Duplicates(false);
                updatePerformer.doUpdate(folder, storageParameters.getTimeStamp());
            } else {
                final UpdatePerformer updatePerformer = new UpdatePerformer(ServerSessionAdapter.valueOf(session), storageParameters.getDecorator(), registry);
                updatePerformer.setCheck4Duplicates(false);
                updatePerformer.doUpdate(folder, storageParameters.getTimeStamp());
            }
            /*
             * Get folder from appropriate storage
             */
            final String newFolderId = folder.getID();
            /*
             * Refresh/Invalidate folder
             */
            final int userId = storageParameters.getUserId();
            if (isMove) {
                removeSingleFromCache(oldFolderId, treeId, userId, session, false);
                removeFromCache(oldParentId, treeId, session, newPathPerformer(storageParameters));
            } else {
                removeFromCache(newFolderId, treeId, session, newPathPerformer(storageParameters));
            }
            /*
             * Put updated folder
             */
            final Folder updatedFolder = loadFolder(treeId, newFolderId, StorageType.WORKING, true, storageParameters);
            {
                final int contextId = storageParameters.getContextId();
                final FolderMapManagement folderMapManagement = FolderMapManagement.getInstance();
                folderMapManagement.dropFor(newFolderId, treeId, userId, contextId, session);
                folderMapManagement.dropFor(newFolderId, realTreeId, userId, contextId, session);
                folderMapManagement.dropFor(updatedFolder.getParentID(), treeId, userId, contextId, session);
                folderMapManagement.dropFor(updatedFolder.getParentID(), realTreeId, userId, contextId, session);
                // Others
                folderMapManagement.dropFor(oldFolderId, treeId, userId, contextId, session);
                folderMapManagement.dropFor(oldFolderId, realTreeId, userId, contextId, session);
                folderMapManagement.dropFor(storageVersion.getParentID(), treeId, userId, contextId, session);
                folderMapManagement.dropFor(storageVersion.getParentID(), realTreeId, userId, contextId, session);
            }
            if (isMove) {
                /*
                 * Invalidate new parent folder
                 */
                final String newParentId = updatedFolder.getParentID();
                if (null != newParentId && !newParentId.equals(oldParentId)) {
                    removeSingleFromCache(newParentId, treeId, userId, storageParameters.getSession(), false);
                }
                /*
                 * Reload folders
                 */
                Folder f = loadFolder(realTreeId, newFolderId, StorageType.WORKING, true, storageParameters);
                removeSingleFromCache(f.getParentID(), treeId, userId, storageParameters.getSession(), false);
                if (f.isCacheable()) {
                    putFolder(f, realTreeId, storageParameters, true);
                }
                f = loadFolder(realTreeId, oldParentId, StorageType.WORKING, true, storageParameters);
                if (f.isCacheable()) {
                    putFolder(f, realTreeId, storageParameters, true);
                }
                f = loadFolder(realTreeId, newParentId, StorageType.WORKING, true, storageParameters);
                if (f.isCacheable()) {
                    putFolder(f, realTreeId, storageParameters, true);
                }
            } else {
                final Folder f = loadFolder(realTreeId, newFolderId, StorageType.WORKING, true, storageParameters);
                if (f.isCacheable()) {
                    putFolder(f, realTreeId, storageParameters, true);
                }
            }
            if (updatedFolder.isCacheable()) {
                putFolder(updatedFolder, treeId, storageParameters, true);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        return containsFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    @Override
    public String[] getModifiedFolderIDs(final String treeId, final Date timeStamp, final ContentType[] includeContentTypes, final StorageParameters storageParameters) throws OXException {
        return getChangedFolderIDs(0, treeId, timeStamp, includeContentTypes, storageParameters);
    }

    @Override
    public String[] getDeletedFolderIDs(final String treeId, final Date timeStamp, final StorageParameters storageParameters) throws OXException {
        return getChangedFolderIDs(1, treeId, timeStamp, null, storageParameters);
    }

    private String[] getChangedFolderIDs(final int index, final String treeId, final Date timeStamp, final ContentType[] includeContentTypes, final StorageParameters storageParameters) throws OXException {
        final Lock lock = readLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            final Session session = storageParameters.getSession();
            /*
             * Perform update operation via non-cache storage
             */
            final UserizedFolder[] folders;
            final boolean ignoreDelete = index == 0;
            if (null == session) {
                folders =
                    new UpdatesPerformer(
                        storageParameters.getUser(),
                        storageParameters.getContext(),
                        storageParameters.getDecorator(),
                        registry).doUpdates(treeId, timeStamp, ignoreDelete, includeContentTypes)[index];
            } else {
                folders =
                    new UpdatesPerformer(ServerSessionAdapter.valueOf(session), storageParameters.getDecorator(), registry).doUpdates(
                        treeId,
                        timeStamp,
                        ignoreDelete,
                        includeContentTypes)[index];
            }
            if (null == folders || folders.length == 0) {
                return new String[0];
            }
            final String[] ids = new String[folders.length];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = folders[i].getID();
            }
            return ids;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        final Lock lock = readLockFor(treeId, storageParameters);
        acquire(lock);
        try {
            final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
            if (null == storage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
            }
            final boolean started = startTransaction(Mode.WRITE_AFTER_READ, storageParameters, storage);
            try {
                final boolean contains = storage.containsFolder(treeId, folderId, storageType, storageParameters);
                if (started) {
                    storage.commitTransaction(storageParameters);
                }
                return contains;
            } catch (final OXException e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    storage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        } finally {
            lock.unlock();
        }
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++ ++ + HELPERS + ++ ++++++++++++++++++++++++++++++++++++
     */

    private boolean startTransaction(final Mode mode, final StorageParameters storageParameters, final FolderStorage storage) throws OXException {
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
     */
    private CacheKey newCacheKey(final String folderId, final String treeId) {
        return cacheService.newCacheKey(1, treeId, folderId);
    }

    private boolean existsFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final boolean started = storage.startTransaction(storageParameters, false);
        try {
            final boolean exists = storage.containsFolder(treeId, folderId, storageType, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
            }
            return exists;
        } catch (final OXException e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
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
    public Folder loadFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        return loadFolder(treeId, folderId, storageType, false, storageParameters);
    }

    private Folder loadFolder(final String treeId, final String folderId, final StorageType storageType, final boolean readWrite, final StorageParameters storageParameters) throws OXException {
        final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final boolean started = startTransaction(readWrite ? Mode.WRITE_AFTER_READ : Mode.READ, storageParameters, storage);
        try {
            final Folder folder = storage.getFolder(treeId, folderId, storageType, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
            }
            return folder;
        } catch (final OXException e) {
            e.printStackTrace();
            if (started) {
                storage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            e.printStackTrace();
            if (started) {
                storage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private String[] loadAllSubfolders(final String treeId, final Folder folder, final boolean readWrite, final StorageParameters storageParameters) throws OXException {
        final Set<FolderStorage> openedStorages = new HashSet<FolderStorage>(2);
        final Set<String> ids = new HashSet<String>(16);
        try {
            final String[] subfolderIds = folder.getSubfolderIDs();
            if (null == subfolderIds) {
                loadAllSubfolders(treeId, folder.getID(), readWrite, storageParameters, ids, openedStorages);
            } else {
                ids.addAll(Arrays.asList(subfolderIds));
                for (final String subfolderId : subfolderIds) {
                    loadAllSubfolders(treeId, subfolderId, readWrite, storageParameters, ids, openedStorages);
                }
            }
            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
            }
        } catch (final OXException e) {
            for (final FolderStorage fs : openedStorages) {
                fs.rollback(storageParameters);
            }
            throw e;
        } catch (final RuntimeException e) {
            for (final FolderStorage fs : openedStorages) {
                fs.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        return ids.toArray(new String[ids.size()]);
    }

    private void loadAllSubfolders(final String treeId, final String folderId, final boolean readWrite, final StorageParameters storageParameters, final Set<String> ids, final Set<FolderStorage> openedStorages) throws OXException {
        final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        checkOpenedStorage(storage, readWrite, openedStorages, storageParameters);
        try {
            final SortableId[] subfolders = storage.getSubfolders(treeId, folderId, storageParameters);
            for (final SortableId sortableId : subfolders) {
                final String id = sortableId.getId();
                loadAllSubfolders(treeId, id, readWrite, storageParameters, ids, openedStorages);
                ids.add(id);
            }
        } catch (final RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    protected void checkOpenedStorage(final FolderStorage checkMe, final boolean modify, final java.util.Collection<FolderStorage> openedStorages, final StorageParameters storageParameters) throws OXException {
        if (openedStorages.contains(checkMe)) {
            // Passed storage is already opened
            return;
        }
        // Passed storage has not been opened before. Open now and add to collection
        if (checkMe.startTransaction(storageParameters, modify)) {
            openedStorages.add(checkMe);
        }
    }

    private Map<String, Folder> loadFolders(final String treeId, final List<String> folderIds, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        /*
         * Collect by folder storage
         */
        final int size = folderIds.size();
        final Map<FolderStorage, TIntList> map = new HashMap<FolderStorage, TIntList>(4);
        for (int i = 0; i < size; i++) {
            final String id = folderIds.get(i);
            final FolderStorage tmp = registry.getFolderStorage(treeId, id);
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
                new ThreadPoolCompletionService<Object>(CacheServiceRegistry.getServiceRegistry().getService(ThreadPoolService.class, true)).setTrackable(true);

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
            final FolderStorage fs = entry.getKey();
            /*
             * Create the list of IDs to load with current storage
             */
            final List<String> ids;
            {
                final int[] indexes = entry.getValue().toArray();
                ids = new ArrayList<String>(indexes.length);
                for (final int index : indexes) {
                    ids.add(folderIds.get(index));
                }
            }
            /*
             * Submit task
             */
            completionService.submit(new TrackableCallable<Object>() {

                @Override
                public Object call() throws Exception {
                    final StorageParameters newParameters = paramsProvider.getStorageParameters();
                    boolean started = !DatabaseFolderType.getInstance().equals(fs.getFolderType()) && fs.startTransaction(newParameters, false);
                    try {
                        /*
                         * Load them & commit
                         */
                        final List<Folder> folders = fs.getFolders(treeId, ids, storageType, newParameters);
                        if (started) {
                            fs.commitTransaction(newParameters);
                            started = false;
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
                    } catch (final RuntimeException e) {
                        throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e);
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
    static StorageParameters newStorageParameters(final StorageParameters source) {
        final Session session = source.getSession();
        if (null == session) {
            return new StorageParametersImpl(source.getUser(), source.getContext());
        }
        return new StorageParametersImpl((ServerSession) session);
    }

    private static volatile Integer maxWaitMillis;
    private static int maxWaitMillis() {
        Integer i = maxWaitMillis;
        if (null == i) {
            synchronized (CacheFolderStorage.class) {
                i = maxWaitMillis;
                if (null == i) {
                    final ConfigurationService service = CacheServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
                    final int millis = null == service ? 60000 : service.getIntProperty("AJP_WATCHER_MAX_RUNNING_TIME", 60000);
                    i = Integer.valueOf(millis << 1);
                    maxWaitMillis = i;
                }
            }
        }
        return i.intValue();
    }

    private static void acquire(final Lock lock) throws OXException {
        if (null == lock) {
            return;
        }
        try {
            // true if the lock was acquired and false if the waiting time elapsed before the lock was acquired
            if (!lock.tryLock(maxWaitMillis(), TimeUnit.MILLISECONDS)) {
                throw FolderExceptionErrorMessage.TRY_AGAIN.create("The maximum time to wait for the lock is exceeded.");
            }
        } catch (final InterruptedException e) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
            throw FolderExceptionErrorMessage.TRY_AGAIN.create(e, e.getMessage());
        }
    }

    public static Lock readLockFor(final String treeId, final StorageParameters params) {
        return lockFor(treeId, params).readLock();
    }

    public static Lock writeLockFor(final String treeId, final StorageParameters params) {
        return lockFor(treeId, params).writeLock();
    }

    private static ReadWriteLock lockFor(final String treeId, final StorageParameters params) {
        return TreeLockManagement.getInstance().getFor(treeId, params.getUserId(), params.getContextId());
    }

    private static FolderMap getFolderMapFor(final Session session) {
        return FolderMapManagement.getInstance().getFor(session);
    }

    private static FolderMap optFolderMapFor(final Session session) {
        return FolderMapManagement.getInstance().optFor(session);
    }

    private static FolderMap optFolderMapFor(final StorageParameters parameters) {
        return FolderMapManagement.getInstance().optFor(parameters.getUserId(), parameters.getContextId());
    }

    /**
     * Drops entries associated with specified user in given context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public static void dropUserEntries(final int userId, final int contextId) {
        final FolderMap folderMap = FolderMapManagement.getInstance().optFor(userId, contextId);
        if (null != folderMap) {
            folderMap.clear();
        }
    }

}
