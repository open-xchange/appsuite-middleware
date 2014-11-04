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

package com.openexchange.caching.events.internal;

import static com.openexchange.caching.events.internal.StampedCacheEvent.POISON;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link CacheEventServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheEventServiceImpl implements CacheEventService {

    /** The logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CacheEventServiceImpl.class);

    private final ConcurrentMap<String, List<CacheListener>> cacheRegionListeners;
    private final List<CacheListener> cacheListeners;
    private final CacheEventQueue delayedEvents;
    private final AtomicBoolean keepgoing;

    /**
     * Initializes a new {@link CacheEventServiceImpl}.
     */
    public CacheEventServiceImpl() {
        super();
        cacheRegionListeners = new ConcurrentHashMap<String, List<CacheListener>>();
        cacheListeners = new ArrayList<CacheListener>();

        final CacheEventQueue delayedEvents = new CacheEventQueue();
        this.delayedEvents = delayedEvents;
        final AtomicBoolean keepgoing = new AtomicBoolean(true);
        this.keepgoing = keepgoing;

        Runnable queueConsumer = new Runnable() {

            @Override
            public void run() {
                List<StampedCacheEvent> drained = new LinkedList<StampedCacheEvent>();
                while (keepgoing.get()) {
                    try {
                        drained.clear();

                        // Blocking wait for at least 1 DelayedPushMsObject to expire.
                        StampedCacheEvent object = delayedEvents.take();
                        if (POISON == object) {
                            return;
                        }
                        drained.add(object);

                        // Drain more if available
                        boolean leave = false;
                        if (delayedEvents.drainTo(drained) > 0) {
                            leave = drained.remove(POISON);
                        }

                        // Deliver events
                        for (StampedCacheEvent sce : drained) {
                            CacheEventServiceImpl.notify(sce.listeners, sce.sender, sce.event, sce.fromRemote);
                        }

                        // Terminate?
                        if (leave) {
                            return;
                        }
                    } catch (Exception e) {
                        LOG.error("Checking for delayed cache events failed", e);
                    }
                }
            }
        };
        ThreadPools.getThreadPool().submit(ThreadPools.task(queueConsumer, "CacheEventQueueConsumer"));
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
        final List<CacheListener> listenersToNotify = new ArrayList<CacheListener>();
        if (null != event.getRegion()) {
            listenersToNotify.addAll(getListeners(event.getRegion()));
        }
        listenersToNotify.addAll(cacheListeners);
        listenersToNotify.remove(sender);
        /*
         * perform notifications
         */
        if (0 < listenersToNotify.size()) {
            delayedEvents.offerIfAbsentElseReset(listenersToNotify, sender, event, fromRemote);
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

    /**
     * Notifies given listeners about cache event.
     *
     * @param listeners The listeners to notify
     * @param sender The sender
     * @param event The event
     * @param fromRemote Whether remotely or locally generated
     */
    protected static void notify(final List<CacheListener> listeners, final Object sender, final CacheEvent event, final boolean fromRemote) {
        LOG.debug("Notifying {} listener(s) about {} event: {}", listeners.size(), fromRemote ? "remote" : "local", event);

        ExecutorService executorService = getExecutorService();
        if (null == executorService) {
            /*
             * Notify sequentially as fallback
             */
            for (CacheListener listener : listeners) {
                listener.onEvent(sender, event, fromRemote);
            }
            return;
        }

        /*
         * Asynchronous notification
         */
        executorService.execute(new Runnable() {

            @Override
            public void run() {
                for (CacheListener listener : listeners) {
                    try {
                        listener.onEvent(sender, event, fromRemote);
                    } catch (Throwable t) {
                        ExceptionUtils.handleThrowable(t);
                        LOG.error("Error while executing event listener.", t);
                    }
                }
            }
        });
    }

    private static ExecutorService getExecutorService() {
        ThreadPoolService threadPoolService = CacheEventServiceLookup.getService(ThreadPoolService.class);
        return null != threadPoolService ? threadPoolService.getExecutor() : null;
    }

}
