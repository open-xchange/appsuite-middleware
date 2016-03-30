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

package com.openexchange.folderstorage.cache.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.RemoveAfterAccessFolder;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.cache.CacheFolderStorage;
import com.openexchange.folderstorage.internal.StorageParametersImpl;
import com.openexchange.folderstorage.mail.MailFolderType;
import com.openexchange.log.LogProperties;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.AbortBehavior;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link FolderMap} - An in-memory folder map with LRU eviction policy.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderMap {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderMap.class);

    private final ConcurrentMap<Key, Wrapper> map;
    private final int maxLifeMillis;
    private final int userId;
    private final int contextId;

    /**
     * Initializes a new {@link FolderMap}.
     *
     * @param maxCapacity the max capacity
     * @param maxLifeUnits the max life units
     * @param unit the unit
     */
    public FolderMap(final int maxCapacity, final int maxLifeUnits, final TimeUnit unit, final int userId, final int contextId) {
        super();
        map = new ConcurrentLinkedHashMap.Builder<Key, Wrapper>().maximumWeightedCapacity(maxCapacity).weigher(Weighers.entrySingleton()).build();
        this.maxLifeMillis = (int) unit.toMillis(maxLifeUnits);
        this.contextId = contextId;
        this.userId = userId;
    }

    /**
     * Initializes a new {@link FolderMap}.
     *
     * @param maxCapacity the max capacity
     * @param maxLifeMillis the max life milliseconds
     */
    public FolderMap(final int maxCapacity, final int maxLifeMillis, final int userId, final int contextId) {
        this(maxCapacity, maxLifeMillis, TimeUnit.MILLISECONDS, userId, contextId);
    }

    /**
     * Removes elapsed entries from map.
     */
    public void shrink() {
        final List<Key> removeKeys = new ArrayList<Key>(16);
        final long minStamp = System.currentTimeMillis() - maxLifeMillis;
        for (final Entry<Key, Wrapper> entry : map.entrySet()) {
            final Wrapper wrapper = entry.getValue();
            if (!wrapper.removeAfterAccess && (wrapper.getStamp() < minStamp)) {
                removeKeys.add(entry.getKey());
            }
        }
        map.keySet().removeAll(removeKeys);
    }

    /**
     * Put if absent.
     *
     * @param treeId the tree id
     * @param folder the folder
     * @return The folder
     */
    public Folder putIfAbsent(final String treeId, final Folder folder, final Session session) {
        return putIfAbsent(folder.getID(), treeId, folder, session);
    }

    /**
     * Put if absent.
     *
     * @param folderId the folder id
     * @param treeId the tree id
     * @param folder the folder
     * @return The folder
     */
    public Folder putIfAbsent(final String folderId, final String treeId, final Folder folder, final Session session) {
        final Wrapper wrapper = wrapperOf(folder, session);
        final Key key = keyOf(folderId, treeId);
        Wrapper prev = map.putIfAbsent(key, wrapper);
        if (null == prev) {
            // Successfully put into map
            return null;
        }
        if (prev.elapsed(maxLifeMillis)) {
            if (map.replace(key, prev, wrapper)) {
                // Successfully replaced with elapsed one
                return null;
            }
            prev = map.get(key);
            if (null == prev) {
                prev = map.putIfAbsent(key, wrapper);
                return null == prev ? null : prev.getValue();
            }
            return prev.getValue();
        }
        return prev.getValue();
    }

    /**
     * Gets the size.
     *
     * @return The size
     */
    public int size() {
        return map.size();
    }

    /**
     * Checks if empty flag is set.
     *
     * @return <code>true</code> if empty flag is set; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Contains.
     *
     * @param folderId the folder id
     * @param treeId the tree id
     * @return <code>true</code> if successful; otherwise <code>false</code>
     */
    public boolean contains(final String folderId, final String treeId) {
        return map.containsKey(keyOf(folderId, treeId));
    }

    /**
     * Gets the folder.
     *
     * @param folderId the folder id
     * @param treeId the tree id
     * @param session The session or <code>null</code>
     * @return The folder
     */
    public Folder get(final String folderId, final String treeId, final Session session) {
        final Key key = keyOf(folderId, treeId);
        final Wrapper wrapper = map.get(key);
        if (null == wrapper) {
            return null;
        }
        if (wrapper.elapsed(maxLifeMillis)) {
            map.remove(key);
            shrink();
            if (!wrapper.removeAfterAccess) {
                return null;
            }
            final Folder folder = wrapper.getValue();
            reloadFolder(folderId, treeId, wrapper.loadSubfolders, session);
            return folder;
        }
        return wrapper.getValue();
    }

    private void reloadFolder(final String folderId, final String treeId, final boolean loadSubfolders, final Session ses) {
        try {
            final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
            if (null == sessiondService) {
                return;
            }
            final ServerSession session = ServerSessionAdapter.valueOf(null == ses ? sessiondService.getAnyActiveSessionForUser(userId, contextId) : ses);
            if (null == session) {
                return;
            }
            ThreadPools.getThreadPool().submit(ThreadPools.trackableTask(new RunnableImpl(folderId, treeId, loadSubfolders, this, session)), AbortBehavior.getInstance());
        } catch (final Exception e) {
            // Ignore
        }
    }

    private void loadSubolders(final Folder folder, final String treeId, final Session ses) {
        try {
            final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
            if (null == sessiondService) {
                return;
            }
            final ServerSession session = ServerSessionAdapter.valueOf(null == ses ? sessiondService.getAnyActiveSessionForUser(userId, contextId) : ses);
            if (null == session) {
                return;
            }
            LogProperties.putSessionProperties(session);
            ThreadPools.getThreadPool().submit(ThreadPools.trackableTask(new LoadSubfolders(folder, treeId, this, session)), AbortBehavior.getInstance());
        } catch (final Exception e) {
            // Ignore
            folder.setSubfolderIDs(null);
        }
    }

    /**
     * Puts specified folder.
     *
     * @param treeId the tree id
     * @param folder the folder
     * @param session The session or <code>null</code>
     * @return The folder
     */
    public Folder put(final String treeId, final Folder folder, final Session session) {
        return put(folder.getID(), treeId, folder, session);
    }

    /**
     * Puts specified folder.
     *
     * @param folderId the folder id
     * @param treeId the tree id
     * @param folder the folder
     * @param session The session or <code>null</code>
     * @return The folder
     */
    public Folder put(final String folderId, final String treeId, final Folder folder, final Session session) {
        final Key key = keyOf(folderId, treeId);
        final Wrapper wrapper = map.put(key, wrapperOf(folder, session));
        if (null == wrapper) {
            return null;
        }
        if (wrapper.elapsed(maxLifeMillis)) {
            map.remove(key);
            shrink();
            return null;
        }
        return wrapper.getValue();
    }

    /**
     * Removes the folder.
     *
     * @param folderId the folder id
     * @param treeId the tree id
     */
    public void remove(final String folderId, final String treeId) {
        map.remove(keyOf(folderId, treeId));
    }

    /**
     * Removes the folder.
     *
     * @param folderId the folder id
     * @param treeId the tree id
     * @param session The session
     * @return The folder
     */
    public Folder remove(final String folderId, final String treeId, final Session session) {
        final Wrapper wrapper = map.remove(keyOf(folderId, treeId));
        if (null == wrapper) {
            return null;
        }
        final Folder ret = wrapper.getIfNotElapsed(maxLifeMillis);
        /*
         * Check remove-after-access flag
         */
        if (wrapper.removeAfterAccess) {
            // Reload actively removed folder
            reloadFolder(folderId, treeId, wrapper.loadSubfolders, session);
        }
        return ret;
    }

    /**
     * Removes the folder hierarchy.
     *
     * @param folderId the folder id
     * @param treeId the tree id
     * @param ids The optional set to collect removed identifiers
     */
    public void removeHierarchy(String folderId, String treeId, Set<String> ids) {
        removeHierarchy(folderId, treeId, true, true, ids);
    }

    private void removeHierarchy(String folderId, String treeId, boolean parent, boolean children, Set<String> ids) {
        Wrapper wrapper = map.remove(keyOf(folderId, treeId));
        if (null == wrapper) {
            return;
        }

        if (null != ids) {
            ids.add(folderId);
        }

        Folder ret = wrapper.getValue();
        if (null == ret) {
            return;
        }

        if (parent) {
            String parentID = ret.getParentID();
            if (null != parentID) {
                removeHierarchy(parentID, treeId, true, false, ids);
            }
        }

        if (children) {
            String[] subfolderIDs = ret.getSubfolderIDs();
            if (null == subfolderIDs) {
                for (Wrapper wr : map.values()) {
                    Folder f = wr.getIfNotElapsed(maxLifeMillis);
                    if (null != f && folderId.equals(f.getParentID())) {
                        removeHierarchy(f.getID(), treeId, false, true, ids);
                    }
                }
            } else {
                for (String subId : subfolderIDs) {
                    removeHierarchy(subId, treeId, false, true, ids);
                }
            }
        }
    }

    /**
     * Clears this map.
     */
    public void clear() {
        map.clear();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    private static Key keyOf(final String folderId, final String treeId) {
        return new Key(folderId, treeId);
    }

    private Wrapper wrapperOf(final Folder folder, final Session session) {
        final Wrapper wrapper = new Wrapper(folder);
        if (wrapper.loadSubfolders && null == folder.getSubfolderIDs()) {
            loadSubolders(folder, folder.getTreeID(), session);
        }
        return wrapper;
    }

    private static final class Wrapper {

        protected final Folder value;
        private final long stamp;
        protected final boolean removeAfterAccess;
        protected final boolean loadSubfolders;

        public Wrapper(final Folder value) {
            super();
            this.value = value;
            this.stamp = System.currentTimeMillis();
            if (value instanceof RemoveAfterAccessFolder) {
                removeAfterAccess = true;
                loadSubfolders = ((RemoveAfterAccessFolder) value).loadSubfolders();
            } else {
                removeAfterAccess = false;
                loadSubfolders = false;
            }
        }

        public long getStamp() {
            return stamp;
        }

        public boolean elapsed(final int maxLifeMillis) {
            return (System.currentTimeMillis() - stamp) > maxLifeMillis;
        }

        public Folder getIfNotElapsed(final int maxLifeMillis) {
            return elapsed(maxLifeMillis) ? null : value;
        }

        public Folder getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value.getID();
        }

    } // End of class Wrapper

    private static final class Key {

        private final String treeId;

        private final String folderId;

        private final int hash;

        public Key(final String folderId, final String treeId) {
            super();
            this.folderId = folderId;
            this.treeId = treeId;
            final int prime = 31;
            int result = 1;
            result = prime * result + ((folderId == null) ? 0 : folderId.hashCode());
            result = prime * result + ((treeId == null) ? 0 : treeId.hashCode());
            this.hash = result;
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
            if (folderId == null) {
                if (other.folderId != null) {
                    return false;
                }
            } else if (!folderId.equals(other.folderId)) {
                return false;
            }
            if (treeId == null) {
                if (other.treeId != null) {
                    return false;
                }
            } else if (!treeId.equals(other.treeId)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Key ( ");
            sb.append("treeId = ");
            sb.append(treeId);
            sb.append(", folderId = ");
            sb.append(folderId);
            sb.append(" )");
            return sb.toString();
        }

    } // End of class Key

    private static final class RunnableImpl implements Runnable {

        private final String folderId;
        private final String treeId;
        private final boolean loadSubfolders;
        private final FolderMap folderMap;
        private final ServerSession session;

        protected RunnableImpl(final String folderId, final String treeId, final boolean loadSubfolders, final FolderMap folderMap, final ServerSession session) {
            this.folderId = folderId;
            this.treeId = treeId;
            this.loadSubfolders = loadSubfolders;
            this.folderMap = folderMap;
            this.session = session;
        }

        @Override
        public void run() {
            try {
                StorageParameters params = new StorageParametersImpl(session);
                params.putParameter(MailFolderType.getInstance(), StorageParameters.PARAM_ACCESS_FAST, Boolean.FALSE);
                CacheFolderStorage folderStorage = CacheFolderStorage.getInstance();
                Folder loaded = folderStorage.loadFolder(treeId, folderId, StorageType.WORKING, params);
                if (loaded.isGlobalID()) {
                    // Eh... No global folder here.
                    return;
                }
                folderMap.put(treeId, loaded, session);
                // Check for subfolders
                if (loadSubfolders) {
                    String[] subfolderIDs = loaded.getSubfolderIDs();
                    if (null != subfolderIDs) {
                        for (String subfolderId : subfolderIDs) {
                            loaded = folderStorage.loadFolder(treeId, subfolderId, StorageType.WORKING, params);
                            if (loaded.isGlobalID()) {
                                folderStorage.putFolder(loaded, treeId, params, false);
                            } else {
                                folderMap.put(treeId, loaded, session);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.debug("", e);
            }
        }
    }

    private static final class LoadSubfolders implements Runnable {

        private final Folder folder;
        private final String treeId;
        private final FolderMap folderMap;
        private final ServerSession session;

        protected LoadSubfolders(final Folder folder, final String treeId, final FolderMap folderMap, final ServerSession session) {
            this.folder = folder;
            this.treeId = treeId;
            this.folderMap = folderMap;
            this.session = session;
        }

        @Override
        public void run() {
            try {
                StorageParameters params = new StorageParametersImpl(session);
                params.putParameter(MailFolderType.getInstance(), StorageParameters.PARAM_ACCESS_FAST, Boolean.FALSE);
                CacheFolderStorage folderStorage = CacheFolderStorage.getInstance();
                // Check for subfolders
                SortableId[] subfolders = folderStorage.getSubfolders(treeId, folder.getID(), params);
                {
                    final String[] ids = new String[subfolders.length];
                    for (int i = 0; i < ids.length; i++) {
                        ids[i] = subfolders[i].getId();
                    }
                    folder.setSubfolderIDs(ids);
                }
                for (SortableId sortableId : subfolders) {
                    Folder loaded = folderStorage.loadFolder(treeId, sortableId.getId(), StorageType.WORKING, params);
                    if (loaded.isGlobalID()) {
                        folderStorage.putFolder(loaded, treeId, params, false);
                    } else {
                        folderMap.put(treeId, loaded, session);
                    }
                }
            } catch (Exception e) {
                LOG.debug("", e);
            }
        }
    }

}
