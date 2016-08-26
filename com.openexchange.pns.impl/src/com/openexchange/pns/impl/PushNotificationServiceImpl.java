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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.pns.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import com.google.common.collect.Iterators;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.UnsychronizedBufferingQueue;
import com.openexchange.pns.Hit;
import com.openexchange.pns.Hits;
import com.openexchange.pns.PushNotification;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.processing.Processor;
import com.openexchange.processing.ProcessorService;
import com.openexchange.session.UserAndContext;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link PushNotificationServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class PushNotificationServiceImpl implements PushNotificationService {

    /** The logger constant */
    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PushNotificationServiceImpl.class);

    private static volatile Long delayDuration;

    private static long delayDuration(ConfigurationService configService) {
        Long tmp = delayDuration;
        if (null == tmp) {
            synchronized (PushNotificationServiceImpl.class) {
                tmp = delayDuration;
                if (null == tmp) {
                    int defaultValue = 3000; // 3 seconds
                    if (null == configService) {
                        return defaultValue;
                    }
                    tmp = Long.valueOf(configService.getIntProperty("com.openexchange.pns.delayDuration", defaultValue));
                    delayDuration = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    private static volatile Long timerFrequency;

    private static long timerFrequency(ConfigurationService configService) {
        Long tmp = timerFrequency;
        if (null == tmp) {
            synchronized (PushNotificationServiceImpl.class) {
                tmp = timerFrequency;
                if (null == tmp) {
                    int defaultValue = 1500; // 1,5 seconds
                    if (null == configService) {
                        return defaultValue;
                    }
                    tmp = Long.valueOf(configService.getIntProperty("com.openexchange.pns.timerFrequency", defaultValue));
                    timerFrequency = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    private static volatile Integer numProcessorThreads;

    private static int numProcessorThreads(ConfigurationService configService) {
        Integer tmp = numProcessorThreads;
        if (null == tmp) {
            synchronized (PushNotificationServiceImpl.class) {
                tmp = numProcessorThreads;
                if (null == tmp) {
                    int defaultValue = 10; // 10 threads
                    if (null == configService) {
                        return defaultValue;
                    }
                    tmp = Integer.valueOf(configService.getIntProperty("com.openexchange.pns.numProcessorThreads", defaultValue));
                    numProcessorThreads = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private static volatile Integer maxProcessorTasks;

    private static int maxProcessorTasks(ConfigurationService configService) {
        Integer tmp = maxProcessorTasks;
        if (null == tmp) {
            synchronized (PushNotificationServiceImpl.class) {
                tmp = maxProcessorTasks;
                if (null == tmp) {
                    int defaultValue = 65536; // 65536 tasks
                    if (null == configService) {
                        return defaultValue;
                    }
                    tmp = Integer.valueOf(configService.getIntProperty("com.openexchange.pns.maxProcessorTasks", defaultValue));
                    maxProcessorTasks = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    /**
     * Cleans statically initialized values.
     */
    public static void cleanseInits() {
        delayDuration = null;
        timerFrequency = null;
        numProcessorThreads = null;
        maxProcessorTasks = null;
    }

    // -----------------------------------------------------------------------------

    private final Lock lock;
    private final ConfigurationService configService;
    private final TimerService timerService;
    private final PushSubscriptionRegistry subscriptionRegistry;
    private final PushNotificationTransportRegistry transportRegistry;

    private final Processor processor;

    private final UnsychronizedBufferingQueue<PushNotification> scheduledNotifcations; // Accessed synchronized
    private ScheduledTimerTask scheduledTimerTask; // Accessed synchronized
    private boolean stopped; // Accessed synchronized

    /**
     * Initializes a new {@link PushNotificationServiceImpl}.
     *
     * @throws OXException If initialization fails
     */
    public PushNotificationServiceImpl(PushSubscriptionRegistry subscriptionRegistry, ConfigurationService configService, TimerService timerService, ProcessorService processorService, PushNotificationTransportRegistry transportRegistry) throws OXException {
        super();
        lock = new ReentrantLock();
        processor = processorService.newBoundedProcessor(getClass().getSimpleName(), numProcessorThreads(configService), maxProcessorTasks(configService));
        this.subscriptionRegistry = subscriptionRegistry;
        this.configService = configService;
        this.timerService = timerService;
        this.transportRegistry = transportRegistry;
        scheduledNotifcations = new UnsychronizedBufferingQueue<>(delayDuration(configService));
    }

    /**
     * Stops this service
     *
     * @param handleRemaining <code>true</code> to handle remaining queued notifications; otherwise <code>false</code>
     */
    public void stop(boolean handleRemaining) {
        lock.lock();
        try {
            if (stopped) {
                // Already stopped
                return;
            }

            if (handleRemaining) {
                for (PushNotification notification : scheduledNotifcations) {
                    int userId = notification.getUserId();
                    int contextId = notification.getContextId();
                    try {
                        doHandle(Iterators.singletonIterator(notification), notification.getTopic(), userId, contextId);
                    } catch (Exception e) {
                        LOG.error("Failed to handle notification with topic {} for user {} in context {}", notification.getTopic(), I(userId), I(contextId), e);
                    }
                }

                try {
                    processor.stopWhenEmpty();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                // Hard shut-down
                processor.stop();
            }

            scheduledNotifcations.clear();
            cancelTimerTask();
            stopped = true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Cancels the timer task (if present).
     * <p>
     * May only be accessed when holding lock.
     */
    protected void cancelTimerTask() {
        if (null != scheduledTimerTask) {
            scheduledTimerTask.cancel();
            scheduledTimerTask = null;
        }
    }

    @Override
    public void handle(PushNotification notification) throws OXException {
        lock.lock();
        try {
            if (stopped) {
                return;
            }

            scheduledNotifcations.offerIfAbsentElseReset(notification);

            if (null == scheduledTimerTask) {
                Runnable task = new Runnable() {

                    @Override
                    public void run() {
                        checkNotifications();
                    }
                };
                long initialDelay = delayDuration(configService);
                long delay = timerFrequency(configService);
                scheduledTimerTask = timerService.scheduleWithFixedDelay(task, initialDelay, delay);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks for an available notifications.
     */
    protected void checkNotifications() {
        PushNotification first = null;
        Map<UserAndTopicKey, List<PushNotification>> polledNotifications = null;

        // Leave this mutually exclusive section as fast as possible...
        lock.lock();
        try {
            if (stopped) {
                return;
            }

            first = scheduledNotifcations.poll();
            if (null == first) {
                // No notifications with an expired delay
                return;
            }

            // There is at least one. Check further...
            PushNotification notification = scheduledNotifcations.poll();
            if (null != notification) {
                // Further ones available. Start grouping...
                polledNotifications = new LinkedHashMap<>();

                // Put first polled notification
                put(first, polledNotifications);

                // Put others as well
                do {
                    put(notification, polledNotifications);
                    notification = scheduledNotifcations.poll();
                } while (null != notification);
            }

            // Drop timer task if queue is empty
            if (scheduledNotifcations.isEmpty()) {
                cancelTimerTask();
            }
        } finally {
            lock.unlock();
        }

        // Distribute notifications (w/o holding lock)
        if (null == polledNotifications) {
            // There was only a single one available
            int userId = first.getUserId();
            int contextId = first.getContextId();
            if (false == processor.execute(UserAndContext.newInstance(userId, contextId), new NotificationsHandler(Iterators.singletonIterator(first), first.getTopic(), userId, contextId))) {
                try {
                    doHandle(Iterators.singletonIterator(first), first.getTopic(), userId, contextId);
                } catch (Exception e) {
                    LOG.error("Failed to handle notification with topic {} for user {} in context {}", first.getTopic(), I(userId), I(contextId), e);
                }
            }
        } else {
            // Handle them all
            for (Map.Entry<UserAndTopicKey, List<PushNotification>> entry : polledNotifications.entrySet()) {
                UserAndTopicKey key = entry.getKey();
                int userId = key.userId;
                int contextId = key.contextId;
                if (false == processor.execute(UserAndContext.newInstance(userId, contextId), new NotificationsHandler(entry.getValue().iterator(), key.topic, userId, contextId))) {
                    try {
                        doHandle(entry.getValue().iterator(), key.topic, userId, contextId);
                    } catch (Exception e) {
                        LOG.error("Failed to handle notification(s) with topic {} for user {} in context {}", key.topic, I(userId), I(contextId), e);
                    }
                }
            }
        }
    }

    private void put(PushNotification notification, Map<UserAndTopicKey, List<PushNotification>> polledNotifications) {
        UserAndTopicKey key = new UserAndTopicKey(notification);
        List<PushNotification> l = polledNotifications.get(key);
        if (null == l) {
            l = new LinkedList<>();
            polledNotifications.put(key, l);
        }
        l.add(notification);
    }

    /**
     * Handles given notifications.
     *
     * @param notifications The notifications
     * @param topic The topic (for all notifications)
     * @param userId The user identifier (for all notifications)
     * @param contextId The context identifier (for all notifications)
     * @throws OXException If handling fails
     */
    protected void doHandle(Iterator<PushNotification> notifications, String topic, int userId, int contextId) throws OXException {
        // Query appropriate subscriptions
        Hits hits = subscriptionRegistry.getInterestedSubscriptions(userId, contextId, topic);
        if (null == hits || hits.isEmpty()) {
            return;
        }

        // Transport each subscription using associated transport
        for (Hit hit : hits) {
            String client = hit.getClient();
            String transportId = hit.getTransportId();
            PushNotificationTransport transport = transportRegistry.getTransportFor(client, transportId);
            if (null == transport) {
                LOG.warn("No such transport '{}' for client '{}' to publish notification from user {} in context {} for topic {}", transportId, client, I(userId), I(contextId), topic);
            } else {
                while (notifications.hasNext()) {
                    transport.transport(notifications.next(), hit.getMatches());
                }
            }
        }
    }

    private final class NotificationsHandler implements Runnable {

        private final Iterator<PushNotification> notifications;
        private final String topic;
        private final int userId;
        private final int contextId;

        /**
         * Initializes a new {@link PushNotificationServiceImpl.NotificationsHandler}.
         */
        NotificationsHandler(Iterator<PushNotification> notifications, String topic, int userId, int contextId) {
            super();
            this.notifications = notifications;
            this.topic = topic;
            this.userId = userId;
            this.contextId = contextId;

        }

        @Override
        public void run() {
            try {
                doHandle(notifications, topic, userId, contextId);
            } catch (Exception e) {
                LOG.error("Failed to handle notification(s) with topic {} for user {} in context {}", topic, I(userId), I(contextId), e);
            }
        }
    }

}
