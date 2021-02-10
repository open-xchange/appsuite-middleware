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

import static com.openexchange.java.Autoboxing.Coll2i;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.logging.LogUtility.toStringObjectFor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.UnsynchronizedBufferingQueue;
import com.openexchange.pns.Hit;
import com.openexchange.pns.Hits;
import com.openexchange.pns.PushMatch;
import com.openexchange.pns.PushNotification;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.PushPriority;
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
    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PushNotificationServiceImpl.class);

    private static volatile Long delayDuration;

    private static long delayDuration(ConfigurationService configService) {
        Long tmp = delayDuration;
        if (null == tmp) {
            synchronized (PushNotificationServiceImpl.class) {
                tmp = delayDuration;
                if (null == tmp) {
                    int defaultValue = 1000; // 1 second
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

    private static long delayDuration(ConfigurationService configService, PushPriority priority) {
        long baseDelayDuration = delayDuration(configService);
        return null != priority ? priority.getDelay(baseDelayDuration) : baseDelayDuration;
    }

    private static volatile Long maxDelayDuration;

    private static long maxDelayDuration(ConfigurationService configService) {
        Long tmp = maxDelayDuration;
        if (null == tmp) {
            synchronized (PushNotificationServiceImpl.class) {
                tmp = maxDelayDuration;
                if (null == tmp) {
                    int defaultValue = 10000; // 10 seconds
                    if (null == configService) {
                        return defaultValue;
                    }
                    tmp = Long.valueOf(configService.getIntProperty("com.openexchange.pns.maxDelayDuration", defaultValue));
                    maxDelayDuration = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    private static long maxDelayDuration(ConfigurationService configService, PushPriority priority) {
        long baseMaxDelayDuration = maxDelayDuration(configService);
        return null != priority ? priority.getDelay(baseMaxDelayDuration) : baseMaxDelayDuration;
    }

    private static volatile Long timerFrequency;

    private static long timerFrequency(ConfigurationService configService) {
        Long tmp = timerFrequency;
        if (null == tmp) {
            synchronized (PushNotificationServiceImpl.class) {
                tmp = timerFrequency;
                if (null == tmp) {
                    int defaultValue = 500; // 0,5 seconds
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

    private final UnsynchronizedBufferingQueue<PushNotification> submittedNotifcations; // Accessed synchronized
    private ScheduledTimerTask scheduledTimerTask; // Accessed synchronized
    private boolean stopped; // Accessed synchronized

    /** The counter for submitted notifications */
    final AtomicLong numOfSubmittedNotifications;

    /** The counter for processed notifications */
    final AtomicLong numOfProcessedNotifications;

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
        submittedNotifcations = new UnsynchronizedBufferingQueue<>(delayDuration(configService), maxDelayDuration(configService));
        numOfProcessedNotifications = new AtomicLong(0);
        numOfSubmittedNotifications = new AtomicLong(0);
    }

    @Override
    public long getNumberOfBufferedNotifications() throws OXException {
        lock.lock();
        try {
            if (stopped) {
                // Already stopped
                return 0L;
            }

            return submittedNotifcations.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long getTotalNumberOfSubmittedNotifications() throws OXException {
        return numOfSubmittedNotifications.get();
    }

    @Override
    public long getTotalNumberOfProcessedNotifications() throws OXException {
        return numOfProcessedNotifications.get();
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
                for (PushNotification notification : submittedNotifcations) {
                    try {
                        new NotificationsHandler(notification).run();
                    } catch (Exception e) {
                        LOGGER.error("Failed to handle notification with topic {} for user {} in context {}",
                            notification.getTopic(), I(notification.getUserId()), I(notification.getContextId()), e);
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

            submittedNotifcations.clear();
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
        ScheduledTimerTask scheduledTimerTask = this.scheduledTimerTask;
        if (null != scheduledTimerTask) {
            this.scheduledTimerTask = null;
            scheduledTimerTask.cancel();
        }
    }

    @Override
    public void handle(Collection<PushNotification> notifications, PushPriority priority) throws OXException {
        if (null == notifications || notifications.isEmpty()) {
            return;
        }

        long delayDuration = delayDuration(configService, priority); // Default 1 second
        long maxDelayDuration = maxDelayDuration(configService, priority); // Default 10 seconds
        lock.lock();
        try {
            if (stopped) {
                LOGGER.debug("Push notification service stopped. Aborting enqueueing of notifications...");
                return;
            }

            // Collection is known to be non-empty. Thus at least one element is contained
            Iterator<PushNotification> iterator = notifications.iterator();

            // Grab first one for bootstrapping
            PushNotification firstNotification = iterator.next();
            addToQueue(firstNotification, delayDuration, maxDelayDuration);

            // Fire off worker if paused
            if (null == scheduledTimerTask) {
                Runnable task = new Runnable() {

                    @Override
                    public void run() {
                        checkNotifications();
                    }
                };
                long delay = timerFrequency(configService); // Default 0,5 second
                long initialDelay = delayDuration + 500L; // Default 1 second + 0,5 second
                scheduledTimerTask = timerService.scheduleWithFixedDelay(task, initialDelay, delay);
                LOGGER.debug("Initiated polling timer task with initial delay of {}msec and periodic delay of {}msec", L(initialDelay), L(delay));
            }

            // Add rest to queue
            while (iterator.hasNext()) {
                addToQueue(iterator.next(), delayDuration, maxDelayDuration);
            }
        } finally {
            lock.unlock();
        }
    }

    private void addToQueue(PushNotification notification, long delayDuration, long maxDelayDuration) {
        boolean added = submittedNotifcations.offerIfAbsentElseReset(notification, delayDuration, maxDelayDuration);
        if (added) {
            if (numOfSubmittedNotifications.incrementAndGet() < 0L) {
                numOfSubmittedNotifications.set(0L);
            }
            LOGGER.debug("Enqueued notification \"{}\" for user {} in context {} with delay of {}msec ({}msec max. delay)", notification.getTopic(), I(notification.getUserId()), I(notification.getContextId()), L(delayDuration), L(maxDelayDuration));
        } else {
            LOGGER.debug("Reset & re-enqueued notification \"{}\" for user {} in context {} with delay of {}msec ({}msec max. delay)", notification.getTopic(), I(notification.getUserId()), I(notification.getContextId()), L(delayDuration), L(maxDelayDuration));
        }
    }

    /**
     * Checks for an available notifications.
     */
    protected void checkNotifications() {
        List<PushNotification> polled;
        boolean onlyOne;

        // Leave this mutually exclusive section as fast as possible...
        lock.lock();
        try {
            if (stopped) {
                LOGGER.debug("Push notification service stopped. Aborting polling of queue...");
                return;
            }

            PushNotification first = submittedNotifcations.poll();
            if (null == first) {
                // Queue has no notification with an expired delay
                LOGGER.debug("Queue has no notification with an expired delay and holds {} buffered notifications", I(submittedNotifcations.size()));
                return;
            }

            // Check for more
            PushNotification notification = submittedNotifcations.poll();
            if (notification != null) {
                polled = new LinkedList<>();
                polled.add(first);
                LOGGER.debug("Polled notification \"{}\" for user {} in context {} from queue", first.getTopic(), I(first.getUserId()), I(first.getContextId()));
                do {
                    polled.add(notification);
                    LOGGER.debug("Polled notification \"{}\" for user {} in context {} from queue", notification.getTopic(), I(notification.getUserId()), I(notification.getContextId()));
                    notification = submittedNotifcations.poll();
                } while (null != notification);
                onlyOne = false;
            } else {
                polled = Collections.singletonList(first);
                LOGGER.debug("Polled notification \"{}\" for user {} in context {} from queue", first.getTopic(), I(first.getUserId()), I(first.getContextId()));
                onlyOne = true;
            }

            if (submittedNotifcations.isEmpty()) {
                cancelTimerTask();
                LOGGER.debug("Dropped polling timer task since queue is currently empty");
            }
        } finally {
            lock.unlock();
        }

        if (onlyOne) {
            // There was only a single one available
            PushNotification first = polled.get(0);
            NotificationsHandler task = new NotificationsHandler(first);
            if (tryExecuteTask(task, first.getUserId(), first.getContextId())) {
                LOGGER.debug("Submitted delivery of polled notification with topic \"{}\" to user {} in context {}", first.getTopic(), I(first.getUserId()), I(first.getContextId()));
            } else {
                // Processor rejected task execution. Perform with current thread.
                task.run();
            }
        } else {
            // Handle them all
            Map<ContextAndTopicKey, Map<Integer, List<PushNotification>>> polledNotificationsPerContextAndTopic = new LinkedHashMap<>();
            for (PushNotification notification : polled) {
                put(notification, polledNotificationsPerContextAndTopic);
            }
            for (Map.Entry<ContextAndTopicKey, Map<Integer, List<PushNotification>>> entry : polledNotificationsPerContextAndTopic.entrySet()) {
                ContextAndTopicKey key = entry.getKey();
                Map<Integer, List<PushNotification>> notificationsPerUser = entry.getValue();
                NotificationsHandler task = new NotificationsHandler(key.contextId, key.topic, notificationsPerUser);
                if (tryExecuteTask(task, -1, key.contextId)) {
                    if (LOGGER.isDebugEnabled()) {
                        int[] userIds = Coll2i(notificationsPerUser.keySet());
                        Object sUserIds = userIds.length <= 10 ? toStringObjectFor(userIds) : null;
                        userIds = null;
                        if (sUserIds == null) {
                            LOGGER.debug("Submitted delivery of polled notifications with topic \"{}\" to users in context {}", key.topic, I(key.contextId));
                        } else {
                            LOGGER.debug("Submitted delivery of polled notifications with topic \"{}\" to users {} in context {}", key.topic, sUserIds, I(key.contextId));
                        }
                    }
                } else {
                    // Processor rejected task execution. Perform with current thread.
                    task.run();
                }
            }
        }
    }

    private boolean tryExecuteTask(Runnable task, int userId, int contextId) {
        return processor.execute(UserAndContext.newInstance(userId, contextId), task);
    }

    private void put(PushNotification notification, Map<ContextAndTopicKey, Map<Integer, List<PushNotification>>> polledNotificationsPerUser) {
        ContextAndTopicKey key = new ContextAndTopicKey(notification);
        Map<Integer, List<PushNotification>> m = polledNotificationsPerUser.get(key);
        if (null == m) {
            m = new HashMap<Integer, List<PushNotification>>();
            polledNotificationsPerUser.put(key, m);
        }
        com.openexchange.tools.arrays.Collections.put(m, I(notification.getUserId()), notification);
    }

    /**
     * Handles given notifications.
     *
     * @param contextId The context identifier (for all notifications)
     * @param topic The topic (for all notifications)
     * @param notificationsPerUser The notifications per user identifier
     * @throws OXException If handling fails
     */
    protected void doHandle(int contextId, String topic, Map<Integer, List<PushNotification>> notificationsPerUser) throws OXException {
        // Determine available user identifiers from "notifications per user" map
        int[] userIds = Coll2i(notificationsPerUser.keySet());
        Object sUserIds = userIds.length <= 10 && LOGGER.isDebugEnabled() ? toStringObjectFor(userIds) : null;
        if (sUserIds == null) {
            LOGGER.debug("Delivering notifications for topic \"{}\" to users in context {}", topic, I(contextId));
        } else {
            LOGGER.debug("Delivering notifications for topic \"{}\" to users {} in context {}", topic, sUserIds, I(contextId));
        }

        // Query appropriate hits
        Hits hits = subscriptionRegistry.getInterestedSubscriptions(userIds, contextId, topic);
        if (null == hits || hits.isEmpty()) {
            if (sUserIds == null) {
                LOGGER.debug("No subscriptions of interest for topic \"{}\" for users in context {}", topic, I(contextId));
            } else {
                LOGGER.debug("No subscriptions of interest for topic \"{}\" for users {} in context {}", topic, sUserIds, I(contextId));
            }
            addNumOfProcessedNotifications(getNumberOfNotificationsFrom(notificationsPerUser));
            return;
        }

        // Process by hits
        if (sUserIds == null) {
            LOGGER.debug("Determined subscriptions of interest for topic \"{}\" for users in context {}", topic, I(contextId));
        } else {
            LOGGER.debug("Determined subscriptions of interest for topic \"{}\" for users {} in context {}", topic, sUserIds, I(contextId));
        }
        for (Map.Entry<PushNotificationTransport, List<Hit>> entry : getHitsPerTransport(hits).entrySet()) {
            // Get push matches for each notification
            PushNotificationTransport transport = entry.getKey();
            Map<PushNotification, List<PushMatch>> notifications = new HashMap<>();
            for (Hit hit : entry.getValue()) {
                List<PushMatch> matches = hit.getMatches();
                for (PushMatch match : matches) {
                    int userId = match.getUserId();
                    if (isTransportAllowed(transport, topic, hit.getClient(), userId, contextId)) {
                        List<PushNotification> notificationsForUser = notificationsPerUser.get(I(match.getUserId()));
                        for (PushNotification notification : notificationsForUser) {
                            if (matchesSourceToken(match, notification.getSourceToken())) {
                                LOGGER.debug("Skipping push match for source token {}", notification.getSourceToken());
                            } else {
                                com.openexchange.tools.arrays.Collections.put(notifications, notification, match);
                            }
                        }
                    } else {
                        LOGGER.info("Transport '{}' not enabled for client '{}' to publish notification \"{}\" to user {} in context {}",
                            transport.getId(), hit.getClient(), topic, I(userId), I(contextId));
                    }
                }
            }

            // Hand over to associated transport
            if (sUserIds == null) {
                LOGGER.debug("Going to use \"{}\" transport to publish notification(s) with topic \"{}\" to users in context {}", transport.getId(), topic, I(contextId));
            } else {
                LOGGER.debug("Going to use \"{}\" transport to publish notification(s) with topic \"{}\" to users {} in context {}", transport.getId(), topic, sUserIds, I(contextId));
            }
            transport.transport(notifications);
        }

        addNumOfProcessedNotifications(getNumberOfNotificationsFrom(notificationsPerUser));
    }

    private Map<PushNotificationTransport, List<Hit>> getHitsPerTransport(Hits hits) throws OXException {
        Map<PushNotificationTransport, List<Hit>> hitsPerTransport = new HashMap<PushNotificationTransport, List<Hit>>();
        for (Hit hit : hits) {
            String client = hit.getClient();
            String transportId = hit.getTransportId();
            PushNotificationTransport transport = transportRegistry.getTransportFor(client, transportId);
            if (null == transport) {
                LOGGER.info("No transport '{}' for client '{}' available, skipping notificaton.", transportId, client);
            } else {
                com.openexchange.tools.arrays.Collections.put(hitsPerTransport, transport, hit);
            }
        }
        return hitsPerTransport;
    }

    private boolean isTransportAllowed(PushNotificationTransport transport, String topic, String client, int userId, int contextId) {
        try {
            return transport.isEnabled(topic, client, userId, contextId);
        } catch (Exception e) {
            LOGGER.error("Failed to check whether notification \"{}\" is allowed to be sent via transport '{}' to client '{}' for user {} in context {}. Transport will be denied...", topic, transport.getId(), client, I(userId), I(contextId), e);
            return false;
        }
    }

    private boolean matchesSourceToken(PushMatch match, String sourceToken) {
        return null != sourceToken && null != match && sourceToken.equals(match.getToken());
    }

    private int getNumberOfNotificationsFrom(Map<Integer, List<PushNotification>> notificationsPerUser) {
        int count = 0;
        for (List<PushNotification> notifications : notificationsPerUser.values()) {
            count += notifications.size();
        }
        return count;
    }

    private void addNumOfProcessedNotifications(int numOfNotifications) {
        if (numOfProcessedNotifications.addAndGet(numOfNotifications) < 0L) {
            numOfProcessedNotifications.set(0L);
        }
    }

    private final class NotificationsHandler implements Runnable {

        private final Map<Integer, List<PushNotification>> notificationsPerUser;
        private final String topic;
        private final int contextId;

        /**
         * Initializes a new {@link PushNotificationServiceImpl.NotificationsHandler}.
         */
        NotificationsHandler(PushNotification notification) {
            this(notification.getContextId(), notification.getTopic(), Collections.singletonMap(I(notification.getUserId()), Collections.singletonList(notification)));
        }

        /**
         * Initializes a new {@link PushNotificationServiceImpl.NotificationsHandler}.
         */
        NotificationsHandler(int contextId, String topic, Map<Integer, List<PushNotification>> notificationsPerUser) {
            super();
            this.notificationsPerUser = notificationsPerUser;
            this.topic = topic;
            this.contextId = contextId;
        }

        @Override
        public void run() {
            try {
                doHandle(contextId, topic, notificationsPerUser);
            } catch (Exception e) {
                LOGGER.error("Failed to handle notification(s) with topic \"{}\" in context {}", topic, I(contextId), e);
            }
        }
    }

}
