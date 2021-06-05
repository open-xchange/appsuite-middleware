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

package com.openexchange.mail.event;

import static com.openexchange.java.Autoboxing.I;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.google.common.collect.ImmutableSet;
import com.openexchange.concurrent.Blocker;
import com.openexchange.concurrent.ConcurrentBlocker;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.push.PushEventConstants;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link EventPool} - The mail event pool.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class EventPool implements Runnable {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EventPool.class);
    }

    /**
     * The delay for a pooled event. TODO: Make configurable
     */
    static final long MSEC_DELAY = 10000L;

    /**
     * The minimum fixed delay between the termination of one execution and the commencement of the next.
     */
    private static final long MIN_TASK_DELAY = 5000L;

    private static volatile EventPool instance;

    /**
     * Initializes this event pool.
     */
    public static synchronized void initInstance() {
        if (null == instance) {
            try {
                instance = new EventPool();
                instance.startup();
            } catch (OXException e) {
                LoggerHolder.LOG.error("", e);
            }
        }
    }

    /**
     * Releases this event pool.
     */
    public static synchronized void releaseInstance() {
        if (null != instance) {
            instance.shutdown();
            instance = null;
        }
    }

    /**
     * Gets the event pool instance.
     *
     * @return The event pool instance
     */
    public static EventPool getInstance() {
        return instance;
    }

    /*-
     * ############################# Member section #############################
     */

    private final Blocker blocker;

    private volatile ScheduledTimerTask timerTask;

    private final ConcurrentMap<PooledEvent, PooledEvent> map;

    private final DelayQueue<PooledEvent> queue;

    /**
     * Initializes a new {@link EventPool}.
     *
     * @throws OXException If initialization fails
     */
    private EventPool() throws OXException {
        super();
        map = new ConcurrentHashMap<PooledEvent, PooledEvent>(1024);
        queue = new DelayQueue<PooledEvent>();
        blocker = new ConcurrentBlocker();
    }

    /**
     * Performs the start-up.
     */
    private void startup() {
        if (null == timerTask) {
            final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class);
            if (null == timer) {
                timerTask = null;
            } else {
                final long calDelay = (MSEC_DELAY / 5);
                timerTask = timer.scheduleWithFixedDelay(this, 1000, ((MIN_TASK_DELAY >= calDelay) ? MIN_TASK_DELAY : calDelay));
            }
        }
    }

    /**
     * Performs the shut-down.
     */
    private void shutdown() {
        final ScheduledTimerTask timerTask = this.timerTask;
        if (null != timerTask) {
            timerTask.cancel(false);
            this.timerTask = null;
            map.clear();
            queue.clear();
        }
    }

    /**
     * Removes all pooled events from this pool associated with given user.
     *
     * @param userId The user ID
     * @param contextId The context ID
     */
    public void removeByUser(int userId, int contextId) {
        blocker.acquire();
        try {
            for (Iterator<PooledEvent> queueIter = queue.iterator(); queueIter.hasNext();) {
                final PooledEvent pooledEvent = queueIter.next();
                if (pooledEvent.equalsByUser(userId, contextId)) {
                    map.remove(pooledEvent);
                    queueIter.remove();
                }
            }
        } finally {
            blocker.release();
        }
    }

    /**
     * Puts given pooled event into this pool. If an equal pooled event is already present its time stamp is updated.
     *
     * @param pooledEvent The pooled event to put.
     */
    public void put(PooledEvent pooledEvent) {
        if ((null == timerTask) || (0 >= pooledEvent.getDelay(TimeUnit.MILLISECONDS))) {
            /*
             * Timer task not active or already elapsed, broadcast immediately
             */
            broadcastEvent(pooledEvent);
        } else {
            /*
             * Enqueue pooled event
             */
            blocker.acquire();
            try {
                final PooledEvent prev = map.putIfAbsent(pooledEvent, pooledEvent);
                if (null == prev) {
                    queue.offer(pooledEvent);
                } else {
                    prev.touch();
                }
            } finally {
                blocker.release();
            }
        }
    }

    /**
     * Clears the event pool.
     */
    public void clear() {
        blocker.acquire();
        try {
            queue.clear();
            map.clear();
        } finally {
            blocker.release();
        }
    }

    @Override
    public void run() {
        blocker.block();
        try {
            /*
             * Poll the head of the queue until it is null; meaning the queue has no more elements with an unexpired delay.
             */
            PooledEvent pooledEvent = queue.poll();
            if (pooledEvent != null) {
                do {
                    /*
                     * An expired pooled event
                     */
                    map.remove(pooledEvent);
                    /*
                     * Broadcast event
                     */
                    broadcastEvent(pooledEvent);
                } while ((pooledEvent = queue.poll()) != null);
            }
        } catch (Throwable t) {
            LoggerHolder.LOG.error("", t);
        } finally {
            blocker.unblock();
        }
    }

    private static final Set<String> RESERVED_NAMES = ImmutableSet.of(
        PushEventConstants.PROPERTY_CONTEXT,
        PushEventConstants.PROPERTY_CONTENT_RELATED,
        PushEventConstants.PROPERTY_FOLDER,
        PushEventConstants.PROPERTY_IMMEDIATELY,
        PushEventConstants.PROPERTY_SESSION,
        PushEventConstants.PROPERTY_USER,
        CommonEvent.PUBLISH_MARKER);

    private void broadcastEvent(PooledEvent pooledEvent) {
        /*
         * Determine event topic
         */
        String topic = pooledEvent.getTopic();
        if (topic == null) {
            topic = PushEventConstants.TOPIC;
        }
        /*
         * Check presence of EventAdmin service
         */
        EventAdmin eventAdmin = ServerServiceRegistry.getInstance().getService(EventAdmin.class);
        if (eventAdmin == null) {
            // EventAdmin unavailable. Probably due to shut-down...
            LoggerHolder.LOG.info("Cannot broadcast mail event with topic {} due to server shut-down", topic);
            return;
        }
        /*
         * Compile event properties
         */
        final Dictionary<String, Object> properties = new Hashtable<String, Object>(6);
        properties.put(PushEventConstants.PROPERTY_CONTEXT, Integer.valueOf(pooledEvent.getContextId()));
        properties.put(PushEventConstants.PROPERTY_USER, Integer.valueOf(pooledEvent.getUserId()));
        properties.put(PushEventConstants.PROPERTY_SESSION, pooledEvent.getSession());
        properties.put(PushEventConstants.PROPERTY_FOLDER, MailFolderUtility.prepareFullname(pooledEvent.getAccountId(), pooledEvent.getFullname()));
        properties.put(PushEventConstants.PROPERTY_CONTENT_RELATED, Boolean.valueOf(pooledEvent.isContentRelated()));
        if (pooledEvent.isRemote()) {
            properties.put(CommonEvent.PUBLISH_MARKER, Boolean.TRUE);
        }
        /*
         * Check for additional properties
         */
        final Map<String, Object> moreProps = pooledEvent.getProperties();
        if (null != moreProps && !moreProps.isEmpty()) {
            final Set<String> reservedNames = RESERVED_NAMES;
            for (Entry<String, Object> entry : moreProps.entrySet()) {
                final String name = entry.getKey();
                if (!reservedNames.contains(name)) {
                    properties.put(name, entry.getValue());
                }
            }
        }
        /*
         * Create event with push topic
         */
        final Event event = new Event(topic, properties);
        /*
         * Finally deliver it
         */
        if (pooledEvent.isAsync()) {
            eventAdmin.postEvent(event);
        } else {
            eventAdmin.sendEvent(event);
        }
        LoggerHolder.LOG.debug("{}Notified {}-wise changed folder \"{}\" in account {} of user {} in context {}", pooledEvent.isRemote() ? "(Remotely) " : "", pooledEvent.isContentRelated() ? "content-related" : "hierarchical", pooledEvent.getFullname(), I(pooledEvent.getAccountId()), I(pooledEvent.getUserId()), I(pooledEvent.getContextId()));
    }

}
