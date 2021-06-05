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

package com.openexchange.pns.monitoring.impl;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import org.slf4j.Logger;
import com.openexchange.management.AnnotatedStandardMBean;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.pns.monitoring.PushNotificationMBean;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;


/**
 * {@link PushNotificationMBeanImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class PushNotificationMBeanImpl extends AnnotatedStandardMBean implements PushNotificationMBean {

    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PushNotificationMBeanImpl.class);

    /** Window size for average calculation: 1 hour */
    private static final long WINDOW_SIZE = 60L * 60000L;

    private final PushNotificationService pushNotificationService;
    private final LinkedBlockingDeque<Measurement> measurements;
    private final ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link PushNotificationMBeanImpl}.
     */
    public PushNotificationMBeanImpl(final PushNotificationService pushNotificationService, TimerService timerService) throws NotCompliantMBeanException {
        super("Management Bean for Push Notification Service", PushNotificationMBean.class);
        this.pushNotificationService = pushNotificationService;
        final LinkedBlockingDeque<Measurement> measurements = new LinkedBlockingDeque<Measurement>();
        this.measurements = measurements;

        Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    long submittedNots = pushNotificationService.getTotalNumberOfSubmittedNotifications();
                    long processedNots = pushNotificationService.getTotalNumberOfProcessedNotifications();
                    Measurement measurement = new Measurement(submittedNots, processedNots);


                    if (measurement.measuredSubmittedNotifications < measurement.measuredProcessedNotifications) {
                        // invalid measurement maybe due to overflow
                        return;
                    }

                    measurements.add(measurement);
                    cleanUp();
                } catch (Exception e) {
                    // Failed run...
                    LOG.error("", e);
                }
            }

            private void cleanUp() {
                long minTime = System.currentTimeMillis() - WINDOW_SIZE;
                for (Measurement measurement; (measurement = measurements.peek()) != null && measurement.timestamp < minTime;) {
                    measurements.poll();
                }
            }
        };
        timerTask = timerService.scheduleAtFixedRate(task, 0L, 60000L);
    }

    /**
     * Stops this MBean.
     */
    public void stop() {
        timerTask.cancel();
    }

    @Override
    public long getNumberOfBufferedNotifications() throws MBeanException {
        try {
            return pushNotificationService.getNumberOfBufferedNotifications();
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PushNotificationMBeanImpl.class);
            logger.error("", e);
            String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public long getTotalNumberOfSubmittedNotifications() throws MBeanException {
        try {
            return pushNotificationService.getTotalNumberOfSubmittedNotifications();
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PushNotificationMBeanImpl.class);
            logger.error("", e);
            String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public long getTotalNumberOfProcessedNotifications() throws MBeanException {
        try {
            return pushNotificationService.getTotalNumberOfProcessedNotifications();
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PushNotificationMBeanImpl.class);
            logger.error("", e);
            String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public long getNotificationsPerMinute() throws MBeanException {
        try {
            long meantimes = 0L;
            long events = 0L;

            Measurement last = null;
            for (Iterator<Measurement> it = measurements.iterator(); it.hasNext();) {
                Measurement current = it.next();
                if (last != null) {
                    meantimes += current.timestamp - last.timestamp;
                    events += current.measuredSubmittedNotifications - last.measuredSubmittedNotifications;
                }

                last = current;
            }

            double eventsPerMillis = 0L;
            if (events > 0L && meantimes > 0L) {
                eventsPerMillis = events / (double) meantimes;
            }

            return Math.round(eventsPerMillis * 60000L);
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PushNotificationMBeanImpl.class);
            logger.error("", e);
            String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public long getEnqueuedNotifications() throws MBeanException {
        try {
            Measurement current = measurements.peekLast();
            if (current != null) {
                return current.measuredSubmittedNotifications - current.measuredProcessedNotifications;
            }

            return 0L;
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PushNotificationMBeanImpl.class);
            logger.error("", e);
            String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    /**
     * Represents a measurement of submitted and in-progress notifications for a certain point in time.
     */
    static final class Measurement {

        final long timestamp;
        final long measuredSubmittedNotifications;
        final long measuredProcessedNotifications;

        Measurement(long submittedNots, long processedNots) {
            super();
            this.measuredSubmittedNotifications = submittedNots;
            this.measuredProcessedNotifications = processedNots;
            this.timestamp = System.currentTimeMillis();
        }
    }

}
