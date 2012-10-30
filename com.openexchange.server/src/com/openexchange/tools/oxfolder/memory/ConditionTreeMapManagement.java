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

package com.openexchange.tools.oxfolder.memory;

import gnu.trove.ConcurrentTIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;

/**
 * {@link ConditionTreeMapManagement}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConditionTreeMapManagement {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ConditionTreeMapManagement.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static volatile ConditionTreeMapManagement instance;

    /**
     * Starts the {@link ConditionTreeMapManagement management} instance.
     */
    public static void startInstance() {
        stopInstance();
        instance = new ConditionTreeMapManagement();
        instance.start();
    }

    /**
     * Stops the {@link ConditionTreeMapManagement management} instance.
     */
    public static void stopInstance() {
        final ConditionTreeMapManagement mm = instance;
        if (null == mm) {
            return;
        }
        mm.stop();
        instance = null;
    }

    /**
     * Gets the {@link ConditionTreeMapManagement management} instance.
     * 
     * @return The {@link ConditionTreeMapManagement management} instance
     */
    public static ConditionTreeMapManagement getInstance() {
        return instance;
    }

    /**
     * Drops the map for given context identifier
     * 
     * @param contextId The context identifier
     */
    public static void dropFor(final int contextId) {
        final ConditionTreeMapManagement mm = instance;
        if (null != mm) {
            mm.maps.remove(contextId);
        }
    }

    /*-
     * -------------------- Member stuff -----------------------------
     */

    protected final ConcurrentTIntObjectHashMap<Future<ConditionTreeMap>> maps;

    private final boolean enabled;

    private volatile ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link ConditionTreeMapManagement}.
     */
    private ConditionTreeMapManagement() {
        super();
        maps = new ConcurrentTIntObjectHashMap<Future<ConditionTreeMap>>(256);
        final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        enabled = null == service || service.getBoolProperty("com.openexchange.oxfolder.memory.enabled", true);
    }

    private void start() {
        final int time2live = 300000; // 5 minutes time-to-live
        final Runnable task = new ShrinkerRunnable(time2live);
        final int delay = 60000; // Every minute
        timerTask = ServerServiceRegistry.getInstance().getService(TimerService.class).scheduleWithFixedDelay(task, delay, delay);
    }

    private void stop() {
        final ScheduledTimerTask timerTask = this.timerTask;
        if (null != timerTask) {
            timerTask.cancel();
            this.timerTask = null;
        }
        maps.clear();
    }

    /**
     * Gets the tree map for given context identifier.
     * 
     * @param contextId The context identifier
     * @return The tree map.
     * @throws OXException If returning tree map fails
     */
    public ConditionTreeMap getMapFor(final int contextId) throws OXException {
        if (!enabled) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create("Memory tree map disabled as per configuration.");
        }
        Future<ConditionTreeMap> f = maps.get(contextId);
        if (null == f) {
            final FutureTask<ConditionTreeMap> ft = new FutureTask<ConditionTreeMap>(new InitTreeMapCallable(contextId, LOG));
            f = maps.putIfAbsent(contextId, ft);
            if (null == f) {
                f = ft;
                ft.run();
            }
        }
        return getFrom(f);
    }

    /**
     * Gets the tree map for given context identifier if already initialized.
     * 
     * @param contextId The context identifier
     * @return The tree map or <code>null</code>
     * @throws OXException If returning tree map fails
     */
    public ConditionTreeMap optMapFor(final int contextId) throws OXException {
        if (!enabled) {
            return null;
        }
        final Future<ConditionTreeMap> f = maps.get(contextId);
        if (null == f) {
            /*
             * Submit a task for tree map initialization
             */
            ThreadPools.getThreadPool().submit(ThreadPools.trackableTask(new LoadTreeMapRunnable(contextId, LOG)));
            return null;
        }
        return timedFrom(f, 1000);
    }

    protected ConditionTreeMap getFrom(final Future<ConditionTreeMap> f) throws OXException {
        try {
            return f.get();
        } catch (final InterruptedException e) {
            // Cannot occur
            Thread.currentThread().interrupt();
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

    protected ConditionTreeMap timedFrom(final Future<ConditionTreeMap> f, final long timeoutMillis) throws OXException {
        try {
            return f.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            // Cannot occur
            Thread.currentThread().interrupt();
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        } catch (final TimeoutException e) {
            return null;
        }
    }

    /**
     * Drops elapsed maps.
     * 
     * @param procedure The shrinker procedure
     */
    protected void shrink(final ShrinkTIntObjectProcedure procedure) {
        /*
         * Invoke for-each with given (prepared) shrinker procedure
         */
        if (DEBUG) {
            final long st = System.currentTimeMillis();
            maps.forEachEntry(procedure.prepareNextRun());
            final long dur = System.currentTimeMillis() - st;
            LOG.debug("ConditionTreeMapManagement.shrink() took " + dur + "msec.");
        } else {
            maps.forEachEntry(procedure.prepareNextRun());
        }
    }

    /*-
     * -------------------------------- Helpers ------------------------------------
     */

    private final class ShrinkerRunnable implements Runnable {

        private final ShrinkTIntObjectProcedure procedure;

        protected ShrinkerRunnable(final int time2live) {
            super();
            this.procedure = new ShrinkTIntObjectProcedure(time2live);
        }

        @Override
        public void run() {
            shrink(procedure);
        }
    }

    private final class ShrinkTIntObjectProcedure implements TIntObjectProcedure<Future<ConditionTreeMap>> {

        private final Lock rlock;

        private final Lock wlock;

        private final long time2live;

        private volatile long maxStamp;

        protected ShrinkTIntObjectProcedure(final long time2live) {
            super();
            this.time2live = time2live;
            final ReadWriteLock readWriteLock = maps.getReadWriteLock();
            this.rlock = readWriteLock.readLock();
            this.wlock = readWriteLock.writeLock();
        }

        /**
         * Updates the <code>maxStamp</code> time stamp to check for elapsed entries.
         * 
         * @return The prepared procedure
         */
        protected ShrinkTIntObjectProcedure prepareNextRun() {
            this.maxStamp = System.currentTimeMillis() - time2live;
            return this;
        }

        @Override
        public boolean execute(final int contextId, final Future<ConditionTreeMap> f) {
            /*
             * Surrounding invocation of ConcurrentTIntObjectHashMap.forEachEntry(TIntObjectProcedure) already holds read lock
             */
            try {
                final ConditionTreeMap map = getFrom(f);
                if (map.stamp < maxStamp) { // Elapsed one
                    // Must release read lock before acquiring write lock
                    rlock.unlock();
                    wlock.lock();
                    try {
                        maps.remove(contextId);
                    } finally {
                        // Downgrade by acquiring read lock before releasing write lock
                        rlock.lock();
                        wlock.unlock();
                    }
                }
            } catch (final OXException e) {
                // Ignore
            }
            return true;
        }
    } // End of ShrinkTIntObjectProcedure class

    private final class LoadTreeMapRunnable implements Runnable {

        private final int contextId;

        private final Log logger;

        protected LoadTreeMapRunnable(final int contextId, final Log logger) {
            super();
            this.contextId = contextId;
            this.logger = logger;
        }

        @Override
        public void run() {
            final FutureTask<ConditionTreeMap> ft = new FutureTask<ConditionTreeMap>(new InitTreeMapCallable(contextId, logger));
            final Future<ConditionTreeMap> prev = maps.putIfAbsent(contextId, ft);
            if (null == prev) {
                ft.run();
            }
        }
    } // End of LoadTreeMapRunnable class

    private static final class InitTreeMapCallable implements Callable<ConditionTreeMap> {

        private final int contextId;

        private final Log logger;

        protected InitTreeMapCallable(final int contextId, final Log logger) {
            super();
            this.contextId = contextId;
            this.logger = logger;
        }

        @Override
        public ConditionTreeMap call() {
            try {
                final ConditionTreeMap newMap = new ConditionTreeMap(contextId);
                newMap.init();
                return newMap;
            } catch (final OXException e) {
                logger.warn(e.getMessage(), e);
                return null;
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
    } // End of InitTreeMapCallable class

}
