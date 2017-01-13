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

package com.openexchange.tools.oxfolder.permissionLoader;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.concurrent.TimeoutConcurrentMap;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.osgi.ServerActivator;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIteratorExceptionCodes;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link PermissionLoaderService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PermissionLoaderService implements Runnable {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PermissionLoaderService.class);

    private static volatile PermissionLoaderService instance;

    /**
     * Gets the {@link JobQueue} instance
     *
     * @return The {@link JobQueue} instance
     */
    public static PermissionLoaderService getInstance() {
        PermissionLoaderService tmp = instance;
        if (null == tmp) {
            synchronized (PermissionLoaderService.class) {
                tmp = instance;
                if (null == tmp) {
                    instance = tmp = new PermissionLoaderService();
                }
            }
        }
        return tmp;
    }

    /**
     * Drops the {@link JobQueue} instance.
     */
    public static void dropInstance() {
        final PermissionLoaderService tmp = instance;
        if (null != tmp) {
            tmp.shutDown();
            instance = null;
        }
    }

    /**
     * The poison element.
     */
    private static final Pair POISON = newPair(-1, -1);

    private final BlockingQueue<Pair> queue;
    private final AtomicBoolean keepgoing;
    private final String simpleName;
    private final Gate gate;

    private volatile TimeoutConcurrentMap<Pair, OCLPermission[]> permsMap;
    private volatile Future<Object> future;
    private volatile ServiceTracker<ThreadPoolService, ThreadPoolService> poolTracker;
    protected volatile ThreadPoolService threadPool;

    /**
     * Initializes a new {@link PermissionLoaderService}.
     */
    public PermissionLoaderService() {
        super();
        keepgoing = new AtomicBoolean(true);
        queue = new LinkedBlockingQueue<Pair>();
        simpleName = getClass().getSimpleName();
        gate = new Gate(-1);
    }

    /**
     * Starts up this service.
     *
     * @throws OXException If start-up fails
     */
    public void startUp() throws OXException {
        final BundleContext context = ServerActivator.getContext();
        if(context == null){
        	threadPool = ThreadPools.getThreadPool();
        } else {
	        final ServiceTrackerCustomizer<ThreadPoolService, ThreadPoolService> customizer = new ServiceTrackerCustomizer<ThreadPoolService, ThreadPoolService>() {

	            @Override
	            public ThreadPoolService addingService(final ServiceReference<ThreadPoolService> reference) {
	                final ThreadPoolService service = context.getService(reference);
	                threadPool = service;
	                return service;
	            }

	            @Override
	            public void modifiedService(final ServiceReference<ThreadPoolService> reference, final ThreadPoolService service) {
	                // Nope
	            }

	            @Override
	            public void removedService(final ServiceReference<ThreadPoolService> reference, final ThreadPoolService service) {
	                threadPool = null;
	                context.ungetService(reference);
	            }
	        };
	        poolTracker = new ServiceTracker<ThreadPoolService, ThreadPoolService>(context, ThreadPoolService.class, customizer);
	        poolTracker.open();
        }
        future = ThreadPools.getThreadPool().submit(ThreadPools.task(this, simpleName));
        permsMap = new TimeoutConcurrentMap<Pair, OCLPermission[]>(20, true);
        gate.open();
    }

    /**
     * Shuts-down this service.
     */
    public void shutDown() {
        keepgoing.set(false);
        queue.offer(POISON);
        try {
            future.get(3, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            // Keep interrupted state
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            LOG.error("Error stopping queue", e.getCause());
        } catch (final TimeoutException e) {
            future.cancel(true);
        } finally {
            if (null != poolTracker) {
                poolTracker.close();
                poolTracker = null;
            }
        }
    }

    /**
     * Submits to load permission for specified folder in given context.
     *
     * @param contextId The context identifier
     * @param folderId The folder identifier
     */
    public void submitPermissionsFor(final int contextId, final int folderId) {
        queue.offer(newPair(folderId, contextId));
    }

    /**
     * Submits to load permission for specified folder in given context.
     *
     * @param folderId The folder identifier
     * @param contextId The context identifier
     */
    public void submitPermissionsFor(final int contextId, final int... folderIds) {
        final List<Pair> tmp = new ArrayList<Pair>(folderIds.length);
        for (final int folderId : folderIds) {
            tmp.add(newPair(folderId, contextId));
        }
        queue.addAll(tmp);
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

    private final class DelegaterTask implements Task<Object> {

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

    @Override
    public void run() {
        try {
            final Gate gate = this.gate;
            final List<Pair> list = new ArrayList<Pair>(128);
            final TObjectProcedure<GroupedPairs> proc = new TObjectProcedure<GroupedPairs>() {

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
            while (keepgoing.get()) {
                /*
                 * Check if paused
                 */
                gate.pass();
                try {
                    /*
                     * Proceed taking from queue
                     */
                    if (queue.isEmpty()) {
                        final Pair next;
                        try {
                            next = queue.take();
                        } catch (final InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        if (POISON == next) {
                            return;
                        }
                        list.add(next);
                        // Await possible more
                        await(100, true);
                    }
                    queue.drainTo(list);
                    final boolean quit = list.remove(POISON);
                    if (!list.isEmpty()) {
                        final DelegaterTask task = new DelegaterTask(list, proc);
                        final ThreadPoolService threadPool = this.threadPool;
                        if (null == threadPool) {
                            task.call();
                        } else {
                            threadPool.submit(task);
                        }
                    }
                    if (quit) {
                        return;
                    }
                    list.clear();
                } finally {
                    gate.signalDone();
                }
            }
        } catch (final InterruptedException e) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
            LOG.error("Interrupted permission loader run.", e);
        } catch (final Exception e) {
            LOG.error("Failed permission loader run.", e);
        }
    }

    private static final int MAX_FILLER_CHUNK = 1024;

    /**
     * Handles specified equally-grouped fillers
     *
     * @param groupedPairs The equally-grouped pairs
     */
    protected void handlePairs(GroupedPairs groupedPairs) {
        int size = groupedPairs.size();
        int configuredBlockSize = MAX_FILLER_CHUNK;
        if (size <= configuredBlockSize) {
            handlePairsSublist(groupedPairs);
            return;
        }

        // Handle chunk-wise...
        int fromIndex = 0;
        while (fromIndex < size) {
            boolean lastChunk;
            int len;

            int toIndex = fromIndex + configuredBlockSize;
            if (toIndex > size) {
                toIndex = size;
                len = toIndex - fromIndex;
                lastChunk = true;
            } else {
                len = configuredBlockSize;
                lastChunk = false;
            }

            GroupedPairs chunk = new GroupedPairs(groupedPairs.contextId, new TIntArrayList(groupedPairs.folderIds.toArray(fromIndex, len)));
            if (lastChunk) {
                // Handle last chunk with this thread
                handlePairsSublist(chunk);
            } else {
                // Submit (if possible)
                schedulePairsSublist(chunk);
            }
            fromIndex = toIndex;
        }
    }

    private void schedulePairsSublist(GroupedPairs groupedPairsSublist) {
        final ThreadPoolService threadPool = this.threadPool;
        if (null == threadPool) {
            /*
             * Caller runs because thread pool is absent
             */
            handlePairsSublist(groupedPairsSublist);
        } else {
            /*
             * Submit without check for a free slot
             */
            threadPool.submit(ThreadPools.task(new PairHandlerTask(groupedPairsSublist)));
        }
    }

    /**
     * Handles specified chunk of equally-grouped pairs
     *
     * @param pairsChunk The chunk of equally-grouped pairs
     */
    protected void handlePairsSublist(GroupedPairs pairsChunk) {
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
                permsMap.put(new Pair(folderId, contextId), perms.toArray(new OCLPermission[perms.size()]), 60);
            }
        } catch (OXException e) {
            LOG.error("Failed loading permissions.", e);
        } catch (RuntimeException e) {
            LOG.error("Failed loading permissions.", e);
        }
    }

    private static final String SQL_LOAD_P = "SELECT permission_id, fp, orp, owp, odp, admin_flag, group_flag, system, fuid FROM oxfolder_permissions WHERE cid = ? AND fuid IN ";

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

    protected static TIntObjectMap<GroupedPairs> groupByContext(final Collection<Pair> pairs) {
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

    protected static void await(final long millis, final boolean blocking) throws InterruptedException {
        if (blocking) {
            Thread.sleep(millis);
            return;
        }
        final long time0 = System.currentTimeMillis();
        long time1;
        do {
            time1 = System.currentTimeMillis();
        } while ((time1 - time0) < millis);
    }

    private static Pair newPair(final int folderid, final int contextId) {
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

    private class PairHandlerTask implements Task<Object> {

        protected final GroupedPairs pairs;

        protected PairHandlerTask(GroupedPairs pairs) {
            super();
            this.pairs = pairs;
        }

        @Override
        public void setThreadName(final ThreadRenamer threadRenamer) {
            // Nope
        }

        @Override
        public void beforeExecute(final Thread t) {
            // Nope
        }

        @Override
        public void afterExecute(final Throwable t) {
            // Nope
        }

        @Override
        public Object call() throws Exception {
            handlePairsSublist(pairs);
            return null;
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
