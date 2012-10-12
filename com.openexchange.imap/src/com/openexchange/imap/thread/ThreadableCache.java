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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.imap.thread;

import gnu.trove.ConcurrentTIntObjectHashMap;
import gnu.trove.TLongCollection;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.imap.cache.util.LockedConcurrentMap;
import com.openexchange.imap.cache.util.MaxCapacityLinkedHashMap;
import com.openexchange.session.Session;

/**
 * {@link ThreadableCache} - A volatile thread cache.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadableCache {

    private static final ThreadableCache INSTANCE = new ThreadableCache();

    /**
     * Gets the instance
     * 
     * @return The instance
     */
    public static ThreadableCache getInstance() {
        return INSTANCE;
    }

    /**
     * Drops the cache associated with specified user.
     * 
     * @param session The session providing user information
     */
    public static void dropFor(final Session session) {
        INSTANCE.userMap.remove(new UserKey(session.getUserId(), session.getContextId()));
    }

    /**
     * Indicates whether <tt>Threadable</tt> cache is enabled.
     * 
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public static boolean isThreadableCacheEnabled() {
        return false;
    }

    /*-
     * ------------------------------------------------------------------------
     */

    private final ConcurrentMap<UserKey, ConcurrentTIntObjectHashMap<ConcurrentMap<String, ThreadableCacheEntry>>> userMap;

    /**
     * Initializes a new {@link ThreadableCache}.
     */
    private ThreadableCache() {
        super();
        userMap = new NonBlockingHashMap<UserKey, ConcurrentTIntObjectHashMap<ConcurrentMap<String, ThreadableCacheEntry>>>(1024);
    }

    /**
     * Clears this cache completely.
     */
    public void clear() {
        userMap.clear();
    }

    /**
     * Clears the cache associated with specified user.
     * 
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void clear(final int userId, final int contextId) {
        userMap.remove(new UserKey(userId, contextId));
    }

    /**
     * Gets the associated cache entry.
     * 
     * @param fullName The full name
     * @param accountId The account identifier
     * @param uids The UIDs
     * @return The cache entry (never <code>null</code>)
     */
    public ThreadableCacheEntry getEntry(final String fullName, final int accountId, final Session session) {
        final UserKey key = new UserKey(session.getUserId(), session.getContextId());
        ConcurrentTIntObjectHashMap<ConcurrentMap<String, ThreadableCacheEntry>> accountMap = userMap.get(key);
        if (null == accountMap) {
            final ConcurrentTIntObjectHashMap<ConcurrentMap<String, ThreadableCacheEntry>> newAccMap =
                new ConcurrentTIntObjectHashMap<ConcurrentMap<String, ThreadableCacheEntry>>(8);
            accountMap = userMap.putIfAbsent(key, newAccMap);
            if (null == accountMap) {
                accountMap = newAccMap;
            }
        }
        ConcurrentMap<String, ThreadableCacheEntry> map = accountMap.get(accountId);
        if (null == map) {
            final ReadWriteLock rwl = new ReentrantReadWriteLock();
            final MaxCapacityLinkedHashMap<String, ThreadableCacheEntry> maxCapacityMap =
                new MaxCapacityLinkedHashMap<String, ThreadableCacheEntry>(32);
            final ConcurrentMap<String, ThreadableCacheEntry> newmap =
                new LockedConcurrentMap<String, ThreadableCacheEntry>(rwl.readLock(), rwl.writeLock(), maxCapacityMap);
            map = accountMap.putIfAbsent(accountId, newmap);
            if (null == map) {
                map = newmap;
            }
        }
        ThreadableCacheEntry entry = map.get(fullName);
        if (null == entry) {
            final ThreadableCacheEntry newentry = new ThreadableCacheEntry();
            entry = map.putIfAbsent(fullName, newentry);
            if (null == entry) {
                entry = newentry;
            }
        }
        return entry;
    }

    /**
     * (Optionally) Gets the associated cache entry.
     * 
     * @param fullName The full name
     * @param accountId The account identifier
     * @return The cache entry or <code>null</code>
     */
    public ThreadableCacheEntry optEntry(final String fullName, final int accountId, final Session session) {
        final ConcurrentTIntObjectHashMap<ConcurrentMap<String, ThreadableCacheEntry>> accountMap =
            userMap.get(new UserKey(session.getUserId(), session.getContextId()));
        if (null == accountMap) {
            return null;
        }
        final ConcurrentMap<String, ThreadableCacheEntry> map = accountMap.get(accountId);
        if (null == map) {
            return null;
        }
        return map.get(fullName);
    }

    /**
     * The cache entry holding the <tt>Threadable</tt>.
     */
    public static final class ThreadableCacheEntry {

        private TLongSet uids;

        private Threadable threadable;

        private boolean sorted;

        /**
         * Initializes a new {@link ThreadableCacheEntry}.
         */
        protected ThreadableCacheEntry() {
            super();
        }

        /**
         * Gets the cached thread.
         * 
         * @return The cached thread
         */
        public Threadable getThreadable() {
            return threadable;
        }

        /**
         * Gets the sorted flag
         * 
         * @return The sorted flag
         */
        public boolean isSorted() {
            return sorted;
        }

        /**
         * Sets the cached thread.
         * 
         * @param uids The UIDs
         * @param threadable The cached thread
         * @return This entry with thread applied
         */
        public ThreadableCacheEntry set(final TLongSet uids, final Threadable threadable, final boolean sorted) {
            this.uids = uids;
            this.threadable = threadable;
            this.sorted = sorted;
            return this;
        }

        /**
         * Checks if a reconstruct is needed.
         * 
         * @param uids The current UIDs
         * @return <code>true</code> to signal needed reconstruct; otherwise <code>false</code>
         */
        public boolean reconstructNeeded(final long[] uids) {
            return reconstructNeeded(null == uids ? null : new TLongHashSet(uids));
        }

        /**
         * Checks if a reconstruct is needed.
         * 
         * @param uids The current UIDs
         * @return <code>true</code> to signal needed reconstruct; otherwise <code>false</code>
         */
        public boolean reconstructNeeded(final TLongCollection currentUids) {
            if (null == currentUids) {
                return false;
            }
            final TLongSet thisUids = uids;
            if (null == thisUids) {
                return true;
            }
            // Calculate new UIDs
            TLongSet newUids = new TLongHashSet(currentUids);
            newUids.removeAll(thisUids);
            if (!newUids.isEmpty()) {
                // Reconstruct needed if new UIDs are present
                return true;
            }
            newUids = null;
            // Calculate deleted UIDs
            final TLongSet deletedUids = new TLongHashSet(thisUids);
            deletedUids.removeAll(currentUids);
            if (!deletedUids.isEmpty()) {
                // Deleted ones may be silently updated without a reconstruct
                thisUids.removeAll(deletedUids);
                removeDeleted(deletedUids, this.threadable);
            }
            return false;
        }

        private Threadable removeDeleted(final TLongSet deletedUids, final Threadable threadable) {
            if (null == threadable) {
                return null;
            }
            // Check root nodes
            final List<Threadable> list = Threadables.unfold(threadable);
            boolean done = false;
            mainLoop: while (!done) {

                final int size = list.size();
                int i = 0;
                for (; i < size; i++) {
                    final Threadable node = list.get(i);
                    if (deletedUids.contains(node.uid)) {
                        break;
                    }
                }
                if (i < size) {
                    final Threadable removed = list.remove(i);
                    list.addAll(i, Threadables.unfold(removed.kid));
                    continue mainLoop;
                }
                done = true;
            }
            // Check children; recursive invocation
            for (final Threadable node : list) {
                node.kid = removeDeleted(deletedUids, node.kid);
            }
            return Threadables.fold(list);
        }

    } // End of ThreadSortCacheEntry class

    private static final class UserKey {

        private final int userId;

        private final int contextId;

        private final int hash;

        protected UserKey(final int userId, final int contextId) {
            super();
            this.userId = userId;
            this.contextId = contextId;
            final int prime = 31;
            int result = 1;
            result = prime * result + contextId;
            result = prime * result + userId;
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
            if (!(obj instanceof UserKey)) {
                return false;
            }
            final UserKey other = (UserKey) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            return true;
        }
    } // End of UserKey class

}
