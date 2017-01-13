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
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.concurrent.TimeoutConcurrentMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.osgi.ServerActivator;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.iterator.SearchIteratorExceptionCodes;

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

    /**
     * The max. running time of 1 minute.
     */
    private static final long MAX_RUNNING_TIME = 60000;

    /**
     * The wait time in milliseconds.
     */
    private static final long WAIT_TIME = 3000;

    private static volatile Integer maxConcurrentTasks;

    /**
     * Gets the max. number of concurrent tasks.
     *
     * @return The max. number of concurrent tasks
     */
    private static int maxConcurrentTasks() {
        Integer tmp = maxConcurrentTasks;
        if (null == tmp) {
            synchronized (PermissionLoaderService.class) {
                tmp = maxConcurrentTasks;
                if (null == tmp) {
                    int defaultValue = -1;
                    ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    tmp = Integer.valueOf(null == service ? defaultValue : service.getIntProperty("com.openexchange.tools.oxfolder.permissionLoader.maxConcurrentTasks", defaultValue));
                    maxConcurrentTasks = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    /**
     * The container for currently running concurrent filler tasks.
     */
    protected final AtomicReferenceArray<StampedFuture> concurrentFutures;

    private final BlockingQueue<Pair> queue;

    private volatile TimeoutConcurrentMap<Pair, OCLPermission[]> permsMap;

    private final AtomicBoolean keepgoing;

    private volatile Future<Object> future;

    private final String simpleName;

    private final Gate gate;

    private final StampedFuture placeHolder;

    private volatile ServiceTracker<ThreadPoolService, ThreadPoolService> poolTracker;

    protected volatile ThreadPoolService threadPool;

    protected volatile ConcurrentMap<Integer, ConWrapper> pooledCons;

    private volatile ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link PermissionLoaderService}.
     */
    public PermissionLoaderService() {
        super();
        int maxConcurrentTasks = maxConcurrentTasks();
        placeHolder = maxConcurrentTasks > 0 ? new StampedFuture(null) : null;
        keepgoing = new AtomicBoolean(true);
        concurrentFutures = maxConcurrentTasks > 0 ? new AtomicReferenceArray<StampedFuture>(maxConcurrentTasks) : null;
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
        pooledCons = new ConcurrentHashMap<Integer, ConWrapper>();
        final Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    final long stamp = System.currentTimeMillis() - 1000;
                    for (final Iterator<ConWrapper> it = pooledCons.values().iterator(); it.hasNext();) {
                        final ConWrapper vw = it.next();
                        final Connection connection = vw.readyForRemoval(stamp);
                        if (connection != null) {
                            it.remove();
                            Database.backNoTimeout(vw.contextId, false, connection);
                            LOG.debug("Closed \"shared\" connection.");
                        }
                    }
                } catch (final Exception e) {
                    // Ignore
                }
            }
        };
        timerTask = ServerServiceRegistry.getInstance().getService(TimerService.class).scheduleWithFixedDelay(task, 1000, 1000);
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
            if (null != timerTask) {
                timerTask.cancel(false);
                timerTask = null;
                for (final Iterator<ConWrapper> it = pooledCons.values().iterator(); it.hasNext();) {
                    final ConWrapper vw = it.next();
                    final Connection connection = vw.con;
                    if (connection != null) {
                        Database.backNoTimeout(vw.contextId, false, connection);
                    }
                }
                pooledCons.clear();
            }
            if (null != poolTracker) {
                poolTracker.close();
                poolTracker = null;
            }
        }
    }

    /**
     * Submits to load permission for specified folder in given context.
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

        private final List<Pair> list;
        private final TObjectProcedure<List<Pair>> proc;

        protected DelegaterTask(final List<Pair> list, final TObjectProcedure<List<Pair>> proc) {
            super();
            this.list = list;
            this.proc = proc;
        }

        @Override
        public void setThreadName(final ThreadRenamer threadRenamer) {
            //
        }

        @Override
        public void beforeExecute(final Thread t) {
            //
        }

        @Override
        public void afterExecute(final Throwable t) {
            //
        }

        @Override
        public Object call() throws Exception {
            groupByContext(list).forEachValue(proc);
            return null;
        }

    }

    @Override
    public void run() {
        try {
            final Gate gate = this.gate;
            final List<Pair> list = new ArrayList<Pair>(128);
            final TObjectProcedure<List<Pair>> proc = new TObjectProcedure<List<Pair>>() {

                @Override
                public boolean execute(final List<Pair> pairs) {
                    try {
                        handlePairs(pairs);
                    } catch (final InterruptedException e) {
                        // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                        Thread.currentThread().interrupt();
                        LOG.error("Interrupted permission loader run.", e);
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
                        final DelegaterTask task = new DelegaterTask(new ArrayList<Pair>(list), proc);
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
     * @throws InterruptedException If thread is interrupted
     */
    protected void handlePairs(final List<Pair> groupedPairs) throws InterruptedException {
        final int size = groupedPairs.size();
        final int configuredBlockSize = MAX_FILLER_CHUNK;
        if (size <= configuredBlockSize) {
            handlePairsSublist(groupedPairs, simpleName);
        } else {
            int fromIndex = 0;
            while (fromIndex < size) {
                final int toIndex = fromIndex + configuredBlockSize;
                if (toIndex > size) {
                    schedulePairsSublist(groupedPairs.subList(fromIndex, size));
                    fromIndex = size;
                } else {
                    schedulePairsSublist(groupedPairs.subList(fromIndex, toIndex));
                    fromIndex = toIndex;
                }
            }
        }
    }

    private void schedulePairsSublist(final List<Pair> groupedPairsSublist) throws InterruptedException {
        final ThreadPoolService threadPool = this.threadPool;
        if (null == threadPool) {
            /*
             * Caller runs because thread pool is absent
             */
            handlePairsSublist(groupedPairsSublist, simpleName);
        } else if (null == concurrentFutures) {
            /*
             * Submit without check for a free slot
             */
            final PairHandlerTask task = new PairHandlerTask(groupedPairsSublist);
            threadPool.submit(ThreadPools.task(task));
            task.start(null);
        } else {
            /*
             * Find a free or elapsed slot
             */
            int maxConcurrentTasks = maxConcurrentTasks();
            if (maxConcurrentTasks > 0) {
                int index = -1;
                while (index < 0) {
                    final long earliestStamp = System.currentTimeMillis() - MAX_RUNNING_TIME;
                    for (int i = 0; (index < 0) && (i < maxConcurrentTasks); i++) {
                        final StampedFuture sf = concurrentFutures.get(i);
                        if (null == sf) {
                            if (concurrentFutures.compareAndSet(i, null, placeHolder)) {
                                index = i; // Found a free slot
                            }
                        } else if (sf.getStamp() < earliestStamp) { // Elapsed
                            sf.getFuture().cancel(true);
                            LOG.debug("Cancelled elapsed task running for {}msec.", (System.currentTimeMillis() - sf.getStamp()));
                            if (concurrentFutures.compareAndSet(i, sf, placeHolder)) {
                                index = i; // Found a slot with an elapsed task
                            }
                        }
                    }
                    LOG.debug(index < 0 ? "Awaiting a free/elapsed slot..." : "Found a free/elapsed slot...");
                    if (index < 0) {
                        synchronized (placeHolder) {
                            placeHolder.wait(WAIT_TIME);
                        }
                    }
                }
                /*
                 * Submit to a free worker thread
                 */
                final PairHandlerTask task = new IndexedPairHandlerTask(groupedPairsSublist, index);
                final Future<Object> f = threadPool.submit(ThreadPools.task(task));
                final StampedFuture sf = new StampedFuture(f);
                concurrentFutures.set(index, sf);
                task.start(sf);
            }
        }
    }

    /**
     * Handles specified chunk of equally-grouped pairs
     *
     * @param pairsChunk The chunk of equally-grouped pairs
     * @param threadDesc The thread description
     */
    protected void handlePairsSublist(final List<Pair> pairsChunk, final String threadDesc) {
        if (pairsChunk.isEmpty()) {
            return;
        }
        try {
            /*
             * Handle fillers in chunks
             */
            final int contextId = pairsChunk.get(0).contextId;
            final Integer key = Integer.valueOf(contextId);
            /*
             * Get read-only connection
             */
            ConWrapper readConWrapper = pooledCons.get(key);
            if (null == readConWrapper) {
                final Connection newReadCon = Database.getNoTimeout(contextId, false);
                final ConWrapper nw = new ConWrapper(newReadCon, contextId);
                readConWrapper = pooledCons.putIfAbsent(key, nw);
                if (null != readConWrapper) {
                    // Wasn't able to put connection; work with newly fetched connection
                    LOG.debug("Using \"un-shared\" connection.");
                    try {
                        for (final Pair pair : pairsChunk) {
                            permsMap.put(pair, loadFolderPermissions(pair.folderId, contextId, newReadCon), 60);
                        }
                    } catch (SQLException e) {
                        throw SearchIteratorExceptionCodes.SQL_ERROR.create(e, EnumComponent.FOLDER, e.getMessage());
                    } finally {
                        Database.backNoTimeout(contextId, false, newReadCon);
                        LOG.debug("Released \"un-shared\" connection.");
                    }
                    // Leave
                    return;
                }
                // "Shared" connection
                readConWrapper = nw;
            } else {
                LOG.debug("Using \"shared\" connection.");
            }
            // Working with "shared" connection
            final Lock rlock = readConWrapper.rlock;
            rlock.lock();
            boolean dec = false;
            try {
                if (readConWrapper.obtain()) {
                    dec = true;
                    synchronized (readConWrapper) {
                        final Connection con = readConWrapper.con;
                        for (final Pair pair : pairsChunk) {
                            permsMap.put(pair, loadFolderPermissions(pair.folderId, contextId, con), 60);
                        }
                    }
                } else {
                    handlePairsSublist(pairsChunk, threadDesc);
                    return;
                }
            } catch (SQLException e) {
                // Fatal SQL error
                pooledCons.remove(key);
                throw SearchIteratorExceptionCodes.SQL_ERROR.create(e, EnumComponent.FOLDER, e.getMessage());
            } finally {
                if (dec) {
                    readConWrapper.release();
                }
                rlock.unlock();
            }
        } catch (OXException e) {
            LOG.error("Failed loading permissions.", e);
        } catch (RuntimeException e) {
            LOG.error("Failed loading permissions.", e);
        } finally {
            if (null != placeHolder) {
                synchronized (placeHolder) {
                    placeHolder.notifyAll();
                }
            }
        }
    }

    private static final String SQL_LOAD_P =
        "SELECT permission_id, fp, orp, owp, odp, admin_flag, group_flag, system FROM oxfolder_permissions WHERE cid = ? AND fuid = ?";

    protected static OCLPermission[] loadFolderPermissions(final int folderId, final int cid, final Connection con) throws OXException, SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_LOAD_P);
            stmt.setInt(1, cid);
            stmt.setInt(2, folderId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                /*
                 * Empty result set
                 */
                return new OCLPermission[0];
            }
            final ArrayList<OCLPermission> ret = new ArrayList<OCLPermission>(8);
            do {
                final OCLPermission p = new OCLPermission();
                p.setEntity(rs.getInt(1)); // Entity
                p.setAllPermission(rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5)); // fp, orp, owp, and odp
                p.setFolderAdmin(rs.getInt(6) > 0 ? true : false); // admin_flag
                p.setGroupPermission(rs.getInt(7) > 0 ? true : false); // group_flag
                p.setSystem(rs.getInt(8)); // system
                ret.add(p);
            } while (rs.next());
            return ret.toArray(new OCLPermission[ret.size()]);
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

    protected static TIntObjectMap<List<Pair>> groupByContext(final Collection<Pair> pairs) {
        final TIntObjectMap<List<Pair>> map = new TIntObjectHashMap<List<Pair>>(pairs.size());
        for (final Pair pair : pairs) {
            final int contextId = pair.contextId;
            List<Pair> set = map.get(contextId);
            if (null == set) {
                set = new LinkedList<Pair>();
                map.put(contextId, set);
            }
            set.add(pair);
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

        protected final List<Pair> pairs;
        protected final CountDownLatch startSignal;

        protected PairHandlerTask(final List<Pair> pairs) {
            super();
            this.startSignal = new CountDownLatch(1);
            this.pairs = pairs;
        }

        /**
         * Opens this task for processing.
         *
         * @param sf The stamped future object associated with this task
         */
        protected void start(final StampedFuture sf) {
            startSignal.countDown();
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
            startSignal.await();
            handlePairsSublist(pairs, null);
            return null;
        }

    }

    private final class IndexedPairHandlerTask extends PairHandlerTask {

        private final int indexPos;

        private volatile StampedFuture sf;

        protected IndexedPairHandlerTask(final List<Pair> pairs, final int indexPos) {
            super(pairs);
            this.indexPos = indexPos;
        }

        /**
         * Opens this task for processing.
         *
         * @param sf The stamped future object associated with this task
         */
        @Override
        protected void start(final StampedFuture sf) {
            this.sf = sf;
            startSignal.countDown();
        }

        @Override
        public Object call() throws Exception {
            try {
                startSignal.await();
                final StampedFuture sf = this.sf;
                if (null != sf) {
                    sf.setStamp(System.currentTimeMillis());
                }
                handlePairsSublist(pairs, String.valueOf(indexPos + 1));
                return null;
            } finally {
                concurrentFutures.set(indexPos, null);
            }
        }

    }

    private static final class ConWrapper {
        protected final int contextId;
        protected volatile Connection con;
        protected volatile long lastAccessed;
        protected final AtomicInteger counter;
        protected final Lock rlock;
        protected final Lock wlock;

        protected ConWrapper(final Connection con, final int contextId) {
            super();
            this.contextId = contextId;
            final ReadWriteLock rwlock = new ReentrantReadWriteLock();
            rlock = rwlock.readLock();
            wlock = rwlock.writeLock();
            this.con = con;
            counter = new AtomicInteger();
            lastAccessed = System.currentTimeMillis();
        }

        protected boolean obtain() {
            // Holding read lock
            if (null == con) {
                return false;
            }
            counter.incrementAndGet();
            return true;
        }

        protected void release() {
            // Holding read lock
            counter.decrementAndGet();
            lastAccessed = System.currentTimeMillis();
        }

        protected Connection readyForRemoval(final long stamp) {
            if (!wlock.tryLock()) {
                // Occupied
                return null;
            }
            try {
                if ((lastAccessed < stamp) && ((null == con) || (0 == counter.get()))) {
                    final Connection c = con;
                    con = null;
                    return c;
                }
                return null;
            } finally {
                wlock.unlock();
            }
        }
    }

}
