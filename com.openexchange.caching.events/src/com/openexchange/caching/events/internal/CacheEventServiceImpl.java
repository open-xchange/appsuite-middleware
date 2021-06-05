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

package com.openexchange.caching.events.internal;

import static com.openexchange.caching.events.internal.StampedCacheEvent.POISON;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventConfiguration;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.caching.events.monitoring.CacheEventMetricHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link CacheEventServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class CacheEventServiceImpl implements CacheEventService, Reloadable {

    /** The logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CacheEventServiceImpl.class);

    /**
     * Notifies given listeners about specified cache event.
     *
     * @param listeners The listeners to notify
     * @param sender The sender
     * @param event The event
     * @param fromRemote Whether remotely or locally generated
     * @param threadPool The thread pool to use or <code>null</code>
     * @param optionalMetricHandler The optional metric handler
     * @throws Exception If notification fails
     */
    static void notify(List<CacheListener> listeners, Object sender, CacheEvent event, boolean fromRemote, ThreadPoolService threadPool, Optional<CacheEventMetricHandler> optionalMetricHandler) throws Exception {
        // Notify asynchronously via executor service, falling back to sequential delivery
        Task<Void> notificationTask = new ListenerNotificationTask(fromRemote, event, listeners, sender, optionalMetricHandler);
        if (null == threadPool) {
            ThreadPools.execute(notificationTask);
        } else {
            threadPool.submit(notificationTask);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------------------------

    private final ConcurrentMap<String, List<CacheListener>> cacheRegionListeners;
    private final List<CacheListener> cacheListeners;
    private final CacheEventQueue delayedEvents;
    private final AtomicReference<CacheEventConfiguration> configurationRef;
    private final AtomicBoolean keepgoing;
    private final AtomicReference<ThreadPoolService> threadPoolRef;
    private final CacheEventMetricHandler metricHandler;

    /**
     * Initializes a new {@link CacheEventServiceImpl}.
     */
    public CacheEventServiceImpl(CacheEventConfiguration initialConfiguration, ThreadPoolService threadPool, CacheEventMetricHandler metricHandler) {
        super();
        cacheRegionListeners = new ConcurrentHashMap<String, List<CacheListener>>();
        cacheListeners = new CopyOnWriteArrayList<CacheListener>();
        configurationRef = new AtomicReference<CacheEventConfiguration>(initialConfiguration);
        threadPoolRef = new AtomicReference<ThreadPoolService>(threadPool);
        this.metricHandler = metricHandler;

        final CacheEventQueue delayedEvents = new CacheEventQueue();
        this.delayedEvents = delayedEvents;
        final AtomicBoolean keepgoing = new AtomicBoolean(true);
        this.keepgoing = keepgoing;

        Task<Void> queueConsumer = new CacheEventQueueConsumer(keepgoing, delayedEvents, threadPoolRef, metricHandler);
        threadPool.submit(queueConsumer);
    }

    /**
     * Sets the thread pool reference to given value
     *
     * @param threadPool The thread pool or <code>null</code>
     */
    public void setThreadPoolService(ThreadPoolService threadPool) {
        threadPoolRef.set(threadPool);
    }

    @Override
    public CacheEventConfiguration getConfiguration() {
        return configurationRef.get();
    }

    /**
     * Shuts-down this service.
     */
    public void shutdown() {
        keepgoing.set(false);
        delayedEvents.add(StampedCacheEvent.POISON);
    }

    @Override
    public void addListener(CacheListener listener) {
        if (cacheListeners.add(listener)) {
            LOG.debug("Added cache listener: {}", listener);
        }
    }

    @Override
    public void removeListener(CacheListener listener) {
        if (cacheListeners.remove(listener)) {
            LOG.debug("Removed cache listener for region: {}", listener);
        }
    }

    @Override
    public void addListener(String region, CacheListener listener) {
        if (getListeners(region).add(listener)) {
            LOG.debug("Added cache listener for region '{}': {}", region, listener);
        }
    }

    @Override
    public void removeListener(String region, CacheListener listener) {
        if (getListeners(region).remove(listener)) {
            LOG.debug("Removed cache listener for region '{}': {}", region, listener);
        }
    }

    @Override
    public void notify(Object sender, CacheEvent event, boolean fromRemote) {
        // Determine which listeners to notify
        final List<CacheListener> listenersToNotify = new LinkedList<CacheListener>();
        if (null != event.getRegion()) {
            listenersToNotify.addAll(getListeners(event.getRegion()));
        }
        listenersToNotify.addAll(cacheListeners);
        listenersToNotify.remove(sender);
        // Perform notifications
        if (false == listenersToNotify.isEmpty()) {
            if (fromRemote) {
                // Deliver remotely received event immediately to local cache listeners to prevent event being aggregated into another local
                // event and thus re-distributed remotely again
                try {
                    ThreadPoolService threadPool = threadPoolRef.get();
                    CacheEventServiceImpl.notify(listenersToNotify, sender, event, fromRemote, threadPool, Optional.empty());
                } catch (Exception e) {
                    LOG.warn("Failed to notify cache listeners about {} event: {}", fromRemote ? "remote" : "local", event, e);
                }
            } else {
                // Schedule locally received event for being processed. Possibly aggregate it into another event.
                if (delayedEvents.offerIfAbsentElseReset(listenersToNotify, sender, event, fromRemote)) {
                    // Increment offered events
                    metricHandler.incrementOfferedEvents(event.getRegion());
                }
            }
        }
    }

    /**
     * Gets the registered cache listeners for a region.
     *
     * @param region The cache region name
     * @return The cache listeners, or an empty list if no listeners are registered
     */
    private List<CacheListener> getListeners(String region) {
        List<CacheListener> listeners = cacheRegionListeners.get(region);
        if (null == listeners) {
            listeners = new CopyOnWriteArrayList<CacheListener>();
            List<CacheListener> existingListeners = cacheRegionListeners.putIfAbsent(region, listeners);
            if (null != existingListeners) {
                return existingListeners;
            }
        }
        return listeners;
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        configurationRef.set(new CacheEventConfigurationImpl(configService));
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().propertiesOfInterest("com.openexchange.caching.jcs.remoteInvalidationForPersonalFolders").build();
    }

    // -----------------------------------------------------------------------------------------------------------------------------------------------

    private static final class CacheEventQueueConsumer extends AbstractTask<Void> {

        private final AtomicBoolean keepgoing;
        private final CacheEventQueue delayedEvents;
        private final AtomicReference<ThreadPoolService> threadPoolRef;
        private final CacheEventMetricHandler metricHandler;

        /**
         * Initializes a new {@link CacheEventQueueConsumer}.
         */
        CacheEventQueueConsumer(AtomicBoolean keepgoing, CacheEventQueue delayedEvents, AtomicReference<ThreadPoolService> threadPoolRef, CacheEventMetricHandler metricHandler) {
            super();
            this.keepgoing = keepgoing;
            this.delayedEvents = delayedEvents;
            this.threadPoolRef = threadPoolRef;
            this.metricHandler = metricHandler;
        }

        @Override
        public void setThreadName(ThreadRenamer threadRenamer) {
            threadRenamer.renamePrefix("CacheEventQueueConsumer");
        }

        @Override
        public Void call() throws Exception {
            List<StampedCacheEvent> drained = new LinkedList<StampedCacheEvent>();
            while (keepgoing.get()) {
                try {
                    drained.clear();

                    // Blocking wait for at least 1 StampedCacheEvent to expire.
                    StampedCacheEvent object = delayedEvents.take();
                    if (POISON == object) {
                        return null;
                    }
                    drained.add(object);

                    // Drain more if available
                    boolean leave = false;
                    if (delayedEvents.drainTo(drained) > 0) {
                        leave = drained.remove(POISON);
                    }

                    // Deliver events
                    ThreadPoolService threadPool = threadPoolRef.get();
                    for (StampedCacheEvent sce : drained) {
                        CacheEventServiceImpl.notify(sce.listeners, sce.sender, sce.event, sce.fromRemote, threadPool, Optional.of(metricHandler));
                    }

                    // Terminate?
                    if (leave) {
                        return null;
                    }
                } catch (InterruptedException e) {
                    LOG.debug("Interrupted while checking for delayed cache events", e);
                } catch (Exception e) {
                    LOG.error("Checking for delayed cache events failed", e);
                }
            }

            return null;
        }
    }

    private static final class ListenerNotificationTask extends AbstractTask<Void> {

        private final boolean fromRemote;
        private final CacheEvent event;
        private final List<CacheListener> listeners;
        private final Object sender;
        private final Optional<CacheEventMetricHandler> optionalMetricHandler;

        /**
         * Initializes a new {@link ListenerNotificationTask}.
         */
        ListenerNotificationTask(boolean fromRemote, CacheEvent event, List<CacheListener> listeners, Object sender, Optional<CacheEventMetricHandler> optionalMetricHandler) {
            super();
            this.fromRemote = fromRemote;
            this.event = event;
            this.listeners = listeners;
            this.sender = sender;
            this.optionalMetricHandler = optionalMetricHandler;
        }

        @Override
        public Void call() throws Exception {
            // Notify listeners
            LOG.debug("Notifying cache listeners about {} event: {}", fromRemote ? "remote" : "local", event);
            for (CacheListener listener : listeners) {
                try {
                    listener.onEvent(sender, event, fromRemote);
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    LOG.error("Error while calling event listener {} for event: {}", listener.getClass().getName(), event, t);
                }
            }
            // Increment delivered events
            if (optionalMetricHandler.isPresent()) {
                optionalMetricHandler.get().incrementDeliveredEvents(event.getRegion());
            }
            return null;
        }
    }
}
