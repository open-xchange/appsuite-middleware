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

package org.apache.felix.eventadmin.impl;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.felix.eventadmin.EventAdminMBean;
import org.apache.felix.eventadmin.impl.tasks.AsyncDeliverTasks;
import org.apache.felix.eventadmin.impl.tasks.AsyncDeliverTasks.Measurement;


/**
 * {@link EventAdminMBeanImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class EventAdminMBeanImpl extends StandardMBean implements EventAdminMBean {

    /** Window size for average calculation: 1 hour */
    private static final long WINDOW_SIZE = 60L * 60000L;

    private final LinkedBlockingDeque<Measurement> measurements;

    private final AsyncDeliverTasks m_postManager;

    private final Timer timer;

    public EventAdminMBeanImpl(final AsyncDeliverTasks m_postManager) throws NotCompliantMBeanException {
        super(EventAdminMBean.class);
        this.m_postManager = m_postManager;
        measurements = new LinkedBlockingDeque<AsyncDeliverTasks.Measurement>();
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
                events += current.getPostedEvents() - last.getPostedEvents();
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
            return current.getPostedEvents() - current.getDeliveredEvents();
        }

        return 0L;
    }

    @Override
    public long getTotalEventCount() {
        Measurement current = measurements.peekLast();
        if (current != null) {
            return current.getPostedEvents();
        }

        return 0L;
    }

    public void close() {
        timer.cancel();
        timer.purge();
    }

    private final class MeasureTask extends TimerTask {
        @Override
        public void run() {
            Measurement measurement = m_postManager.createMeasurement();
            if (measurement.getPostedEvents() < measurement.getDeliveredEvents()) {
                // invalid measurement maybe due to overflow
                return;
            }

            measurements.add(measurement);
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

}
