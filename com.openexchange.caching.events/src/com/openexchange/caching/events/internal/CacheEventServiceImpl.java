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

package com.openexchange.caching.events.internal;

import static com.openexchange.caching.events.internal.StampedCacheEvent.POISON;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventConfiguration;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.caching.events.monitoring.CacheEventMonitor;
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
 */
public final class CacheEventServiceImpl implements CacheEventService, CacheEventMonitor, Reloadable {

    /** The logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CacheEventServiceImpl.class);

    /**
     * Notifies given listeners about specified cache event.
     *
     * @param listeners The listeners to notify
     * @param sender The sender
     * @param event The event
     * @param fromRemote Whether remotely or locally generated
     * @param numDeliveredEvents The counter for delivered events
     * @param threadPool The thread pool to use or <code>null</code>
     * @throws Exception If notification fails
     */
    static void notify(List<CacheListener> listeners, Object sender, CacheEvent event, boolean fromRemote, AtomicLong numDeliveredEvents, ThreadPoolService threadPool) throws Exception {
        // Notify asynchronously via executor service, falling back to sequential delivery
        Task<Void> notificationTask = new ListenerNotificationTask(fromRemote, event, listeners, sender, numDeliveredEvents);
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
    private final AtomicLong numOfferedEvents;
    private final AtomicLong numDeliveredEvents;
    private final AtomicReference<ThreadPoolService> threadPoolRef;

    /**
     * Initializes a new {@link CacheEventServiceImpl}.
     */
    public CacheEventServiceImpl(CacheEventConfiguration initialConfiguration, ThreadPoolService threadPool) {
        super();
        cacheRegionListeners = new ConcurrentHashMap<String, List<CacheListener>>();
        cacheListeners = new CopyOnWriteArrayList<CacheListener>();
        numOfferedEvents = new AtomicLong();
        numDeliveredEvents = new AtomicLong();
        configurationRef = new AtomicReference<CacheEventConfiguration>(initialConfiguration);
        threadPoolRef = new AtomicReference<ThreadPoolService>(threadPool);

        final CacheEventQueue delayedEvents = new CacheEventQueue();
        this.delayedEvents = delayedEvents;
        final AtomicBoolean keepgoing = new AtomicBoolean(true);
        this.keepgoing = keepgoing;

        Task<Void> queueConsumer = new CacheEventQueueConsumer(keepgoing, delayedEvents, numDeliveredEvents, threadPoolRef);
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

    @Override
    public long getOfferedEvents() {
        return numOfferedEvents.get();
    }

    @Override
    public long getDeliveredEvents() {
        return numDeliveredEvents.get();
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
        /*
         * determine which listeners to notify
         */
        final List<CacheListener> listenersToNotify = new LinkedList<CacheListener>();
        if (null != event.getRegion()) {
            listenersToNotify.addAll(getListeners(event.getRegion()));
        }
        listenersToNotify.addAll(cacheListeners);
        listenersToNotify.remove(sender);
        /*
         * perform notifications
         */
        if (false == listenersToNotify.isEmpty()) {
            if (delayedEvents.offerIfAbsentElseReset(listenersToNotify, sender, event, fromRemote)) {
                /*
                 * increment offered events
                 */
                if (numOfferedEvents.incrementAndGet() < 0L) {
                    numOfferedEvents.set(0L);
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
            List<CacheListener> exitingListeners = cacheRegionListeners.putIfAbsent(region, listeners);
            if (null != exitingListeners) {
                return exitingListeners;
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
        private final AtomicLong numDeliveredEvents;
        private final AtomicReference<ThreadPoolService> threadPoolRef;

        /**
         * Initializes a new {@link CacheEventQueueConsumer}.
         */
        CacheEventQueueConsumer(AtomicBoolean keepgoing, CacheEventQueue delayedEvents, AtomicLong numDeliveredEvents, AtomicReference<ThreadPoolService> threadPoolRef) {
            super();
            this.keepgoing = keepgoing;
            this.delayedEvents = delayedEvents;
            this.numDeliveredEvents = numDeliveredEvents;
            this.threadPoolRef = threadPoolRef;
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
                        CacheEventServiceImpl.notify(sce.listeners, sce.sender, sce.event, sce.fromRemote, numDeliveredEvents, threadPool);
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
        private final AtomicLong numDeliveredEvents;

        /**
         * Initializes a new {@link ListenerNotificationTask}.
         */
        ListenerNotificationTask(boolean fromRemote, CacheEvent event, List<CacheListener> listeners, Object sender, AtomicLong numDeliveredEvents) {
            super();
            this.fromRemote = fromRemote;
            this.event = event;
            this.listeners = listeners;
            this.sender = sender;
            this.numDeliveredEvents = numDeliveredEvents;
        }

        @Override
        public Void call() throws Exception {
            /*
             * notify listeners
             */
            if (fromRemote) {
                LOG.debug("Notifying cache listeners about remote event: {}", event);
            } else {
                LOG.debug("Notifying cache listeners about local event: {}", event);
            }
            for (CacheListener listener : listeners) {
                try {
                    listener.onEvent(sender, event, fromRemote);
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    LOG.error("Error while calling event listener {} for event: {}", listener.getClass().getName(), event, t);
                }
            }
            /*
             * track delivery
             */
            if (numDeliveredEvents.incrementAndGet() < 0L) {
                numDeliveredEvents.set(0L);
            }
            return null;
        }
    }

}
