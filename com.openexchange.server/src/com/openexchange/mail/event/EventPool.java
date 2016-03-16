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

package com.openexchange.mail.event;

import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EventPool.class);

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
            } catch (final OXException e) {
                LOG.error("", e);
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

    private final EventAdmin eventAdmin;

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
        eventAdmin = ServerServiceRegistry.getInstance().getService(EventAdmin.class, true);
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
    public void removeByUser(final int userId, final int contextId) {
        blocker.acquire();
        try {
            for (final Iterator<PooledEvent> queueIter = queue.iterator(); queueIter.hasNext();) {
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
    public void put(final PooledEvent pooledEvent) {
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
        } catch (final Throwable t) {
            org.slf4j.LoggerFactory.getLogger(EventPool.class).error("", t);
        } finally {
            blocker.unblock();
        }
    }

    private static final Set<String> RESERVED_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        PushEventConstants.PROPERTY_CONTEXT,
        PushEventConstants.PROPERTY_CONTENT_RELATED,
        PushEventConstants.PROPERTY_FOLDER,
        PushEventConstants.PROPERTY_IMMEDIATELY,
        PushEventConstants.PROPERTY_SESSION,
        PushEventConstants.PROPERTY_USER,
        CommonEvent.PUBLISH_MARKER)));

    private void broadcastEvent(final PooledEvent pooledEvent) {
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
            for (final Entry<String, Object> entry : moreProps.entrySet()) {
                final String name = entry.getKey();
                if (!reservedNames.contains(name)) {
                    properties.put(name, entry.getValue());
                }
            }
        }
        /*
         * Create event with push topic
         */
        final String topic = pooledEvent.getTopic();
        final Event event = new Event(null == topic ? PushEventConstants.TOPIC : topic, properties);
        /*
         * Finally deliver it
         */
        if (pooledEvent.isAsync()) {
            eventAdmin.postEvent(event);
        } else {
            eventAdmin.sendEvent(event);
        }
        LOG.debug("{}Notified {}-wise changed folder \"{}\" in account {} of user {} in context {}", pooledEvent.isRemote() ? "(Remotely) " : "", pooledEvent.isContentRelated() ? "content-related" : "hierarchical", pooledEvent.getFullname(), pooledEvent.getAccountId(), pooledEvent.getUserId(), pooledEvent.getContextId());
    }

}
