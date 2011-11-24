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

package com.openexchange.folderstorage.cache.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.RemoveAfterAccessFolder;

/**
 * {@link FolderMap} - An in-memory folder map with LRU eviction policy.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderMap {

    private final ConcurrentMap<Key, Wrapper> map;

    private final int maxLifeMillis;

    /**
     * Initializes a new {@link FolderMap}.
     */
    public FolderMap(final int maxCapacity, final int maxLifeUnits, final TimeUnit unit) {
        super();
        final Lock lock = new ReentrantLock();
        map = new LockBasedConcurrentMap<Key, Wrapper>(lock, lock, new MaxCapacityLinkedHashMap<Key, Wrapper>(maxCapacity));
        this.maxLifeMillis = (int) unit.toMillis(maxLifeUnits);
    }

    /**
     * Initializes a new {@link FolderMap}.
     */
    public FolderMap(final int maxCapacity, final int maxLifeMillis) {
        this(maxCapacity, maxLifeMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Removes elapsed entries from map.
     */
    public void shrink() {
        final List<Key> removeKeys = new ArrayList<Key>(16);
        final long minStamp = System.currentTimeMillis() - maxLifeMillis;
        for (final Entry<Key, Wrapper> entry : map.entrySet()) {
            final Wrapper wrapper = entry.getValue();
            if (!wrapper.removeAfterAccess && wrapper.getStamp() < minStamp) {
                removeKeys.add(entry.getKey());
            }
        }
        for (final Key key : removeKeys) {
            map.remove(key);
        }
    }

    public Folder putIfAbsent(final String treeId, final Folder folder) {
        return putIfAbsent(folder.getID(), treeId, folder);
    }

    public Folder putIfAbsent(final String folderId, final String treeId, final Folder folder) {
        final Wrapper wrapper = wrapperOf(folder);
        final Key key = keyOf(folderId, treeId);
        Wrapper prev = map.putIfAbsent(key, wrapper);
        if (null == prev) {
            // Successfully put into map
            return null;
        }
        if (prev.elapsed(maxLifeMillis)) {
            synchronized (map) {
                prev = map.get(key);
                if (prev.elapsed(maxLifeMillis)) {
                    shrink();
                    map.put(key, wrapper);
                    return null;
                }
            }
        }
        return prev.getValue();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean contains(final String folderId, final String treeId) {
        return map.containsKey(keyOf(folderId, treeId));
    }

    public Folder get(final String folderId, final String treeId) {
        final Key key = keyOf(folderId, treeId);
        final Wrapper wrapper = map.get(key);
        if (null == wrapper) {
            return null;
        }
        if (wrapper.elapsed(maxLifeMillis)) {
            map.remove(key);
            shrink();
            return wrapper.removeAfterAccess ? wrapper.getValue() : null;
        }
        return wrapper.getValue();
    }

    public Folder put(final String treeId, final Folder folder) {
        return put(folder.getID(), treeId, folder);
    }

    public Folder put(final String folderId, final String treeId, final Folder folder) {
        final Key key = keyOf(folderId, treeId);
        final Wrapper wrapper = map.put(key, wrapperOf(folder));
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

    public Folder remove(final String folderId, final String treeId) {
        final Wrapper wrapper = map.remove(keyOf(folderId, treeId));
        return wrapper == null ? null : wrapper.getIfNotElapsed(maxLifeMillis);
    }

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

    private static Wrapper wrapperOf(final Folder folder) {
        return new Wrapper(folder);
    }

    private static final class Wrapper {

        private final Folder value;

        private final long stamp;

        protected final boolean removeAfterAccess;

        public Wrapper(final Folder value) {
            super();
            this.value = value;
            this.stamp = System.currentTimeMillis();
            removeAfterAccess = (value instanceof RemoveAfterAccessFolder);
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

}
