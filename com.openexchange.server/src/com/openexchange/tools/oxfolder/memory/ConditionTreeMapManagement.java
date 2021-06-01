/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.tools.oxfolder.memory;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
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

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConditionTreeMapManagement.class);

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
    public static void dropFor(int contextId) {
        final ConditionTreeMapManagement mm = instance;
        if (null != mm) {
            mm.context2maps.invalidate(Integer.valueOf(contextId));
        }
    }

    /*-
     * -------------------- Member stuff -----------------------------
     */

    private static final int TIME2LIVE = 360000; // 6 minutes time-to-live

    protected final LoadingCache<Integer, ConditionTreeMap> context2maps;
    private final boolean enabled;
    private volatile ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link ConditionTreeMapManagement}.
     */
    private ConditionTreeMapManagement() {
        super();

        // Build up cache
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder().concurrencyLevel(4).initialCapacity(8192).expireAfterAccess(TIME2LIVE, TimeUnit.MILLISECONDS);
        context2maps = cacheBuilder.build(new CacheLoader<Integer, ConditionTreeMap>() {

            @Override
            public ConditionTreeMap load(Integer contextId) throws Exception {
                ConditionTreeMap newMap = new ConditionTreeMap(contextId.intValue(), TIME2LIVE);
                newMap.init();
                return newMap;
            }
        });

        ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        enabled = null == service || service.getBoolProperty("com.openexchange.oxfolder.memory.enabled", true);
    }

    private void start() {
        Runnable task = new ShrinkerRunnable();
        int delay = 20000; // Every 20 seconds
        timerTask = ServerServiceRegistry.getInstance().getService(TimerService.class).scheduleWithFixedDelay(task, delay, delay);
    }

    private void stop() {
        ScheduledTimerTask timerTask = this.timerTask;
        if (null != timerTask) {
            timerTask.cancel();
            this.timerTask = null;
        }
        context2maps.invalidateAll();
    }

    /**
     * Gets the tree map for given context identifier.
     *
     * @param contextId The context identifier
     * @return The tree map.
     * @throws OXException If returning tree map fails
     */
    public ConditionTreeMap getMapFor(int contextId) throws OXException {
        if (!enabled) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create("Memory tree map disabled as per configuration.");
        }
        try {
            return context2maps.get(Integer.valueOf(contextId));
        } catch (ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

    /**
     * Gets the tree map for given context identifier if already initialized.
     *
     * @param contextId The context identifier
     * @return The tree map or <code>null</code>
     */
    public ConditionTreeMap optMapFor(final int contextId) {
        return optMapFor(contextId, true);
    }

    /**
     * Gets the tree map for given context identifier if already initialized.
     *
     * @param contextId The context identifier
     * @param triggerLoad Whether to trigger asynchronous loading of the condition tree map if none is available yet
     * @return The tree map or <code>null</code>
     */
    public ConditionTreeMap optMapFor(final int contextId, final boolean triggerLoad) {
        if (!enabled) {
            return null;
        }

        ConditionTreeMap treeMap = context2maps.getIfPresent(Integer.valueOf(contextId));
        if (null != treeMap) {
            return treeMap;
        }

        if (triggerLoad) {
            /*
             * Submit a task for tree map initialization
             */
            ThreadPools.getThreadPool().submit(ThreadPools.trackableTask(new LoadTreeMapRunnable(contextId, LOG)));
        }
        return null;
    }

    /**
     * Drops elapsed maps.
     */
    protected void shrink() {
        context2maps.cleanUp();

        long maxStamp = System.currentTimeMillis() - TIME2LIVE;
        for (Iterator<ConditionTreeMap> it = context2maps.asMap().values().iterator(); it.hasNext();) {
            ConditionTreeMap map = it.next();
            if (null != map) {
                map.trim(maxStamp);
            }
        }
    }

    /*-
     * -------------------------------- Helpers ------------------------------------
     */

    private final class ShrinkerRunnable implements Runnable {

        protected ShrinkerRunnable() {
            super();
        }

        @Override
        public void run() {
            shrink();
        }
    }

    private final class LoadTreeMapRunnable implements Runnable {

        private final int contextId;
        private final org.slf4j.Logger logger;

        LoadTreeMapRunnable(int contextId, org.slf4j.Logger logger) {
            super();
            this.contextId = contextId;
            this.logger = logger;
        }

        @Override
        public void run() {
            try {
                context2maps.get(Integer.valueOf(contextId));
            } catch (Exception e) {
                logger.error("", e.getCause());
            }
        }
    } // End of LoadTreeMapRunnable class

}
