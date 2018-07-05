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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.tools.oxfolder.permissionLoader;

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderPermissionType;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIteratorExceptionCodes;
import com.openexchange.tools.sql.DBUtils;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;

/**
 * {@link PermissionLoader}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class PermissionLoader implements Runnable {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PermissionLoader.class);

    /** The poison element to abort processing */
    static final Pair POISON_PAIR = newPair(-1, -1);

    private static final String THREAD_NAME = "PermissionLoader";

    final BlockingQueue<Pair> pairsToLoad;
    final ConcurrentMap<Pair, OCLPermission[]> permsMap;
    private final AtomicBoolean started;

    /**
     * Initializes a new {@link PermissionLoader}.
     */
    public PermissionLoader() {
        super();
        permsMap = new ConcurrentHashMap<>(16, 0.9F, 1);
        pairsToLoad = new LinkedBlockingQueue<Pair>();
        started = new AtomicBoolean(false);
    }

    private void checkStarted() {
        if (started.compareAndSet(false, true)) {
            ThreadPools.getThreadPool().submit(ThreadPools.task(this, THREAD_NAME));
        }
    }

    /**
     * Submits to load permission for specified folder in given context.
     *
     * @param contextId The context identifier
     * @param folderId The folder identifier
     */
    public void submitPermissionsFor(final int contextId, final int folderId) {
        checkStarted();
        pairsToLoad.offer(newPair(folderId, contextId));
    }

    /**
     * Submits to load permission for specified folder in given context.
     *
     * @param folderId The folder identifier
     * @param contextId The context identifier
     */
    public void submitPermissionsFor(final int contextId, final int... folderIds) {
        checkStarted();
        List<Pair> tmp = new ArrayList<Pair>(folderIds.length);
        for (int folderId : folderIds) {
            tmp.add(newPair(folderId, contextId));
        }
        pairsToLoad.addAll(tmp);
    }

    /**
     * Polls the possibly loaded permissions for specified folder in given context.
     *
     * @param folderId The folder identifier
     * @param contextId The context identifier
     * @return The loaded permissions or <code>null</code>
     */
    public OCLPermission[] pollPermissions(final int folderId, final int contextId) {
        return permsMap.remove(newPair(folderId, contextId));
    }

    /**
     * Stops this loader.
     */
    public void stop() {
        if (started.compareAndSet(true, false)) {
            pairsToLoad.offer(POISON_PAIR);
        }
    }

    @Override
    public void run() {
        try {
            List<Pair> list = new ArrayList<Pair>(16);
            TObjectProcedure<GroupedPairs> proc = new TObjectProcedure<GroupedPairs>() {

                @Override
                public boolean execute(GroupedPairs pairs) {
                    try {
                        handlePairs(pairs);
                    } catch (final Exception e) {
                        LOG.error("Failed permission loader run.", e);
                    }
                    return true;
                }
            };

            while (true) {
                if (pairsToLoad.isEmpty()) {
                    final Pair next;
                    try {
                        next = pairsToLoad.poll(60, TimeUnit.SECONDS);
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    if (null == next || PermissionLoader.POISON_PAIR == next) {
                        return;
                    }
                    list.add(next);
                    // Await possible more
                    await(100);
                }
                pairsToLoad.drainTo(list);
                final boolean quit = list.remove(PermissionLoader.POISON_PAIR);
                if (!list.isEmpty()) {
                    DelegaterTask task = new DelegaterTask(list, proc);
                    ThreadPools.execute(task);
                }
                if (quit) {
                    return;
                }
                list.clear();
            }

        } catch (InterruptedException e) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
            LOG.error("Interrupted permission loader run.", e);
        } catch (Exception e) {
            LOG.error("Failed permission loader run.", e);
        }
    }

    private static final int MAX_FILLER_CHUNK = 1024;

    /**
     * Handles specified equally-grouped fillers
     *
     * @param groupedPairs The equally-grouped pairs
     */
    void handlePairs(GroupedPairs groupedPairs) {
        int size = groupedPairs.size();
        int configuredBlockSize = MAX_FILLER_CHUNK;
        if (size <= configuredBlockSize) {
            handlePairsSublist(groupedPairs);
            return;
        }

        // Handle chunk-wise...
        int fromIndex = 0;
        while (fromIndex < size) {
            int len;

            int toIndex = fromIndex + configuredBlockSize;
            if (toIndex > size) {
                toIndex = size;
                len = toIndex - fromIndex;
            } else {
                len = configuredBlockSize;
            }

            GroupedPairs chunk = new GroupedPairs(groupedPairs.contextId, new TIntArrayList(groupedPairs.folderIds.toArray(fromIndex, len)));
            handlePairsSublist(chunk);
            fromIndex = toIndex;
        }
    }

    /**
     * Handles specified chunk of equally-grouped pairs
     *
     * @param pairsChunk The chunk of equally-grouped pairs
     */
    void handlePairsSublist(GroupedPairs pairsChunk) {
        if (pairsChunk.isEmpty()) {
            return;
        }
        try {
            int contextId = pairsChunk.contextId;

            // Create partitions
            TIntObjectMap<List<OCLPermission>> mapping = new TIntObjectHashMap<>(pairsChunk.size());
            List<TIntList> fPartitions = createPartitions(pairsChunk);

            {
                Connection con = Database.get(contextId, false);
                try {
                    for (TIntList fPartition : fPartitions) {
                        loadFolderPermissions(fPartition.toArray(), contextId, con, mapping);
                    }
                } catch (SQLException e) {
                    throw SearchIteratorExceptionCodes.SQL_ERROR.create(e, EnumComponent.FOLDER, e.getMessage());
                } finally {
                    Database.back(contextId, false, con);
                }
            }

            TIntObjectIterator<List<OCLPermission>> iterator = mapping.iterator();
            for (int i = mapping.size(); i-- > 0;) {
                iterator.advance();
                int folderId = iterator.key();
                List<OCLPermission> perms = iterator.value();
                permsMap.put(new Pair(folderId, contextId), perms.toArray(new OCLPermission[perms.size()]));
            }
        } catch (OXException e) {
            LOG.error("Failed loading permissions.", e);
        } catch (RuntimeException e) {
            LOG.error("Failed loading permissions.", e);
        }
    }

    private static final String SQL_LOAD_P = "SELECT permission_id, fp, orp, owp, odp, admin_flag, group_flag, system, fuid, type, sharedParentFolder FROM oxfolder_permissions WHERE cid = ? AND fuid IN ";

    protected static void loadFolderPermissions(int[] folderIds, int cid, Connection con, TIntObjectMap<List<OCLPermission>> mapping) throws OXException, SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_LOAD_P + StringCollection.getSqlInString(folderIds));
            stmt.setInt(1, cid);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                /*
                 * Empty result set
                 */
                return;
            }

            do {
                int folderId = rs.getInt(9);
                List<OCLPermission> permissions = mapping.get(folderId);
                if (null == permissions) {
                    permissions = new ArrayList<OCLPermission>(8);
                    mapping.put(folderId, permissions);
                }

                OCLPermission p = new OCLPermission();
                p.setEntity(rs.getInt(1)); // Entity
                p.setAllPermission(rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5)); // fp, orp, owp, and odp
                p.setFolderAdmin(rs.getInt(6) > 0 ? true : false); // admin_flag
                p.setGroupPermission(rs.getInt(7) > 0 ? true : false); // group_flag
                p.setSystem(rs.getInt(8)); // system
                p.setType(FolderPermissionType.getType(rs.getInt(10))); // type
                int legator = rs.getInt(11);
                p.setPermissionLegator(legator == 0 ? null : String.valueOf(legator)); // legator
                permissions.add(p);
            } while (rs.next());
        } catch (SQLException e) {
            if ("Connection was already closed.".equals(e.getMessage())) {
                // Fatal...
                throw e;
            }
            throw SearchIteratorExceptionCodes.SQL_ERROR.create(e, EnumComponent.FOLDER, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static List<TIntList> createPartitions(GroupedPairs pairsChunk) {
        int size = pairsChunk.size();
        if (size <= DBUtils.IN_LIMIT) {
            return Collections.singletonList(pairsChunk.folderIds);
        }

        List<TIntList> fPartitions = new ArrayList<>(4);
        int off = 0;
        do {
            int end = off + DBUtils.IN_LIMIT;
            if (end > size) {
                end = size;
            }

            fPartitions.add(new TIntArrayList(pairsChunk.folderIds.toArray(off, end - off)));
            off = end;
        } while (off < size);
        return fPartitions;
    }

    // ----------------------------------------------------------------------------------------------------

    private static class DelegaterTask implements Task<Object> {

        private final TIntObjectMap<GroupedPairs> groupByContext;
        private final TObjectProcedure<GroupedPairs> proc;

        protected DelegaterTask(List<Pair> list, TObjectProcedure<GroupedPairs> proc) {
            super();
            this.groupByContext = groupByContext(list);
            this.proc = proc;
        }

        @Override
        public void setThreadName(ThreadRenamer threadRenamer) {
            //
        }

        @Override
        public void beforeExecute(Thread t) {
            //
        }

        @Override
        public void afterExecute(Throwable t) {
            //
        }

        @Override
        public Object call() throws Exception {
            groupByContext.forEachValue(proc);
            return null;
        }

    }

    static TIntObjectMap<GroupedPairs> groupByContext(final Collection<Pair> pairs) {
        TIntObjectMap<GroupedPairs> map = new TIntObjectHashMap<GroupedPairs>(pairs.size());
        for (Pair pair : pairs) {
            int contextId = pair.contextId;
            GroupedPairs set = map.get(contextId);
            if (null == set) {
                set = new GroupedPairs(contextId);
                map.put(contextId, set);
            }
            set.add(pair.folderId);
        }
        return map;
    }

    static void await(long millis) throws InterruptedException {
        Thread.sleep(millis);
        return;
    }

    static Pair newPair(final int folderid, final int contextId) {
        return new Pair(folderid, contextId);
    }

    private static final class Pair {

        public final int folderId;
        public final int contextId;

        private final int hash;

        public Pair(final int folderId, final int contextId) {
            super();
            this.folderId = folderId;
            this.contextId = contextId;
            final int prime = 31;
            int result = 1;
            result = prime * result + folderId;
            result = prime * result + contextId;
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
            if (!(obj instanceof Pair)) {
                return false;
            }
            final Pair other = (Pair) obj;
            if (folderId != other.folderId) {
                return false;
            }
            if (contextId != other.contextId) {
                return false;
            }
            return true;
        }
    }

    /** A collection of folder identifiers associated with the same context */
    private static final class GroupedPairs {

        final int contextId;
        final TIntList folderIds;

        GroupedPairs(int contextId) {
            this(contextId, new TIntArrayList(10));
        }

        GroupedPairs(int contextId, TIntList folderIds) {
            super();
            this.contextId = contextId;
            this.folderIds = folderIds;
        }

        boolean isEmpty() {
            return folderIds.isEmpty();
        }

        void add(int folderId) {
            folderIds.add(folderId);
        }

        int size() {
            return folderIds.size();
        }
    }

}
