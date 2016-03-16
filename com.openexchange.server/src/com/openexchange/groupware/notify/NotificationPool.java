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

package com.openexchange.groupware.notify;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.notify.ParticipantNotify.MailMessage;
import com.openexchange.i18n.tools.RenderMap;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link NotificationPool} - Pools instances of {@link PooledNotification} for a consolidated update notification.
 * <p>
 * Ensure method {@link #startup()} is invoked prior to using this pool.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NotificationPool {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(NotificationPool.class);

    private static final NotificationPool INSTANCE = new NotificationPool();

    /**
     * Gets the {@link NotificationPool} instance
     *
     * @return The {@link NotificationPool} instance
     */
    public static NotificationPool getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private final AtomicBoolean started;
    private final ReadWriteLock lock;
    private final Lock readLock;
    private volatile ScheduledTimerTask timerTask;
    private final Map<PooledNotification, PooledNotification> map;
    private final DelayQueue<PooledNotification> queue;

    /**
     * Initializes a new {@link NotificationPool}
     */
    private NotificationPool() {
        super();
        started = new AtomicBoolean();
        map = new ConcurrentHashMap<PooledNotification, PooledNotification>(1024, 0.9f, 1);
        queue = new DelayQueue<PooledNotification>();
        lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
    }

    /**
     * Removes all pooled notifications from this pool whose calendar object matches specified object ID and context ID
     *
     * @param objectId The calendar object's ID
     * @param contextId The calendar object's context ID
     */
    public void removeByObject(final int objectId, final int contextId) {
        readLock.lock();
        try {
            for (final Iterator<PooledNotification> queueIter = queue.iterator(); queueIter.hasNext();) {
                final PooledNotification pn = queueIter.next();
                if (pn.equalsByObject(objectId, contextId)) {
                    map.remove(pn);
                    queueIter.remove();
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Puts given pooled notification into this pool. If an equal pooled notification is already present, it is merged with given pooled
     * notification. Moreover its time stamp is updated.
     *
     * @param pooledNotification The pooled notification to put.
     */
    public void put(final PooledNotification pooledNotification) {
        readLock.lock();
        try {
            final PooledNotification prev = map.get(pooledNotification);
            if (prev == null) {
                map.put(pooledNotification, pooledNotification);
                queue.offer(pooledNotification);
                LOG.debug("New pooled notification added for receiver {}", pooledNotification.getParticipant().email);
            } else {
                prev.merge(pooledNotification);
                prev.touch();
                LOG.debug("Pooled notification merged for receiver {}", pooledNotification.getParticipant().email);
            }
        } finally {
            readLock.unlock();
        }
    }

    // For the tests we need to get at the pool
    public List<PooledNotification> getNotifications() {
        return new ArrayList<PooledNotification>(queue);
    }

    /**
     * Clears the notification pool
     */
    public void clear() {
        readLock.lock();
        try {
            queue.clear();
            map.clear();
        } finally {
            readLock.unlock();
        }
    }

    // For tests to force message sending
    public void sendAllMessages() {
        new NotificationPoolTimerTask(map, queue, lock.writeLock()).run();
    }

    /**
     * Start-up for this notification pool
     */
    public void startup() {
        if (started.compareAndSet(false, true)) {
            /*
             * Create timer task and schedule it
             */
            final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class);
            if (null != timer) {
                timerTask = timer.scheduleWithFixedDelay(new NotificationPoolTimerTask(map, queue, lock.writeLock()), 1000, 60000);
            }
        }
    }

    /**
     * Shut-down for this notification pool
     */
    public void shutdown() {
        if (started.compareAndSet(true, false)) {
            ScheduledTimerTask timerTask = this.timerTask;
            timerTask.cancel(false);
            final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class);
            if (null != timer) {
                timer.purge();
            }
            map.clear();
            queue.clear();
            this.timerTask = null;
        }
    }

    private class NotificationPoolTimerTask implements Runnable {

        private final org.slf4j.Logger logger;

        private final Lock taskWriteLock;

        private final Map<PooledNotification, PooledNotification> taskMap;

        private final DelayQueue<PooledNotification> taskQueue;

        public NotificationPoolTimerTask(final Map<PooledNotification, PooledNotification> map, final DelayQueue<PooledNotification> queue, final Lock writeLock) {
            super();
            logger = org.slf4j.LoggerFactory.getLogger(NotificationPoolTimerTask.class);
            this.taskMap = map;
            this.taskQueue = queue;
            this.taskWriteLock = writeLock;
        }

        @Override
        public void run() {
            taskWriteLock.lock();
            try {
                /*
                 * Poll the head of the queue until it is null; meaning the queue has no more elements with an unexpired delay.
                 */
                PooledNotification cur = taskQueue.poll();
                if (null != cur) {
                    final StringBuilder b = new StringBuilder(2048);
                    do {
                        /*
                         * An expired pooled notification
                         */
                        taskMap.remove(cur);
                        /*
                         * Handle pooled notification
                         */
                        handlePooledNotification(cur, b);
                        /*
                         * Poll next
                         */
                        cur = taskQueue.poll();
                    } while (null != cur);
                }
            } catch (final Throwable t) {
                logger.error("", t);
            } finally {
                taskWriteLock.unlock();
            }
        }

        private void handlePooledNotification(final PooledNotification cur, final StringBuilder b) {
            final EmailableParticipant p = cur.getParticipant();
            logger.debug("Found elapsed pooled notification for receiver {}", p.email);
            final RenderMap renderMap = cur.getRenderMap();
            renderMap.applyLocale(cur.getLocale());
            renderMap.applyTimeZone(p.timeZone == null ? TimeZone.getDefault() : p.timeZone);
            /*
             * Check start/end if message is still allowed to be sent
             */
            final State state = cur.getState();
            final CalendarObject calendarObject = cur.getCalendarObject();
            if (ParticipantNotify.checkStartAndEndDate(calendarObject, state.getModule())) {
                /*
                 * Create message
                 */
                final MailMessage mmsg;
                if (Participant.USER == p.type) {
                    mmsg =
                        ParticipantNotify.createUserMessage(
                            cur.getSession(),
                            calendarObject,
                            p,
                            (ParticipantNotify.userCanReadObject(p, calendarObject, cur.getSession())),
                            cur.getTitle(),
                            cur.getState().getAction(),
                            state,
                            cur.getLocale(),
                            cur.getRenderMap(),
                            true,
                            b);
                } else {
                    mmsg =
                        ParticipantNotify.createParticipantMessage(
                            cur.getSession(),
                            calendarObject,
                            p,
                            cur.getTitle(),
                            cur.getState().getAction(),
                            state,
                            cur.getLocale(),
                            cur.getRenderMap(),
                            true,
                            b);
                }
                logger.debug(
                    "Pooled {} (id = {}) notification message generated for receiver {}",
                    (Types.APPOINTMENT == state.getModule() ? "Appointment" : "Task"),
                    calendarObject.getObjectID(),
                    p.email);
                /*
                 * Send notification
                 */
                if (null != mmsg) {
                    ParticipantNotify.sendMessage(mmsg, cur.getSession(), calendarObject, state);
                }
            }
        }

    } // End of NotificationPoolTimerTask

}
