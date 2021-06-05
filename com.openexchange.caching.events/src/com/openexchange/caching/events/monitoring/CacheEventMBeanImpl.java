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

package com.openexchange.caching.events.monitoring;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;


/**
 * {@link CacheEventMBeanImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CacheEventMBeanImpl extends StandardMBean implements CacheEventMBean {

    /** Window size for average calculation: 1 hour */
    private static final long WINDOW_SIZE = 60L * 60000L;

    final LinkedBlockingDeque<Measurement> measurements;
    final CacheEventMonitor monitor;
    private final Timer timer;

    /**
     * Initializes a new {@link CacheEventMBeanImpl}.
     *
     * @param monitor The cache event monitor
     * @throws NotCompliantMBeanException
     */
    public CacheEventMBeanImpl(CacheEventMonitor monitor) throws NotCompliantMBeanException {
        super(CacheEventMBean.class);
        this.monitor = monitor;
        measurements = new LinkedBlockingDeque<Measurement>();
        timer = new Timer(true);
        timer.schedule(new MeasureTask(), 0L, 60000L);
    }

    @Override
    public long getEventsPerMinute() {
        Iterator<Measurement> it = measurements.iterator();
        Measurement last = null;
        long meantimes = 0L;
        long events = 0L;
        while (it.hasNext()) {
            Measurement current = it.next();
            if (last != null) {
                meantimes += current.getTimestamp() - last.getTimestamp();
                events += current.getOfferedEvents() - last.getOfferedEvents();
            }

            last = current;
        }

        double eventsPerMillis = 0L;
        if (events > 0L && meantimes > 0L) {
            eventsPerMillis = events / (double) meantimes;
        }

        return Math.round(eventsPerMillis * 60000L);
    }

    @Override
    public long getEnqueuedEvents() {
        Measurement current = measurements.peekLast();
        if (current != null) {
            return current.getOfferedEvents() - current.getDeliveredEvents();
        }

        return 0L;
    }

    @Override
    public long getTotalEventCount() {
        Measurement current = measurements.peekLast();
        if (current != null) {
            return current.getOfferedEvents();
        }

        return 0L;
    }

    public void close() {
        timer.cancel();
        timer.purge();
    }

    private final class MeasureTask extends TimerTask {

        /**
         * Initializes a new {@link CacheEventMBeanImpl.MeasureTask}.
         */
        MeasureTask() {
            super();
        }

        @Override
        public void run() {
            long deliveredEvents = monitor.getDeliveredEvents();
            long offeredEvents = monitor.getOfferedEvents();
            if (offeredEvents < deliveredEvents) {
                // invalid measurement maybe due to overflow
                return;
            }
            measurements.add(new Measurement(offeredEvents, deliveredEvents));
            cleanUp();
        }

        private void cleanUp() {
            long minTime = System.currentTimeMillis() - WINDOW_SIZE;
            Measurement measurement = null;
            while ((measurement = measurements.peek()) != null && measurement.getTimestamp() < minTime) {
                measurements.poll();
            }
        }
    }

    private static final class Measurement {

        private final long timestamp;
        private final long measuredOfferedEvents;
        private final long measuredDeliveredEvents;

        public Measurement(long offeredEvents, long deliveredEvents) {
            super();
            this.measuredOfferedEvents = offeredEvents;
            this.measuredDeliveredEvents = deliveredEvents;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * Gets the time in milliseconds after 1970-01-01 00:00:00 UTC when this measurement was created.
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * Gets the total number of events that have been offered.
         */
        public long getOfferedEvents() {
            return measuredOfferedEvents;
        }

        /**
         * Gets the total number of events that have already been delivered.
         */
        public long getDeliveredEvents() {
            return measuredDeliveredEvents;
        }

    }

}
