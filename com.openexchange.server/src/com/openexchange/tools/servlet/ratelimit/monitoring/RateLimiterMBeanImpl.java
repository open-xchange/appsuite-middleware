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

package com.openexchange.tools.servlet.ratelimit.monitoring;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import com.openexchange.tools.servlet.ratelimit.RateLimiter;

/**
 * {@link RateLimiterMBeanImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RateLimiterMBeanImpl extends StandardMBean implements RateLimiterMBean {

    /** Window size for average calculation: 1 hour */
    private static final long WINDOW_SIZE = 60L * 60000L;

    final LinkedBlockingDeque<Measurement> measurements;
    private final Timer timer;

    /**
     * Initializes a new {@link RateLimiterMBeanImpl}.
     *
     * @throws NotCompliantMBeanException
     */
    public RateLimiterMBeanImpl() throws NotCompliantMBeanException {
        super(RateLimiterMBean.class);
        measurements = new LinkedBlockingDeque<Measurement>();
        timer = new Timer(true);
        timer.schedule(new MeasureTask(), 0L, 60000L);
    }

    @Override
    public long getProcessedRequestsPerMinute() {
        Iterator<Measurement> it = measurements.iterator();
        Measurement last = null;
        long meantimes = 0L;
        long processedRequests = 0L;
        while (it.hasNext()) {
            Measurement current = it.next();
            if (last != null) {
                meantimes += current.getTimestamp() - last.getTimestamp();
                processedRequests += current.getProcessedRequests() - last.getProcessedRequests();
            }
            last = current;
        }
        double eventsPerMillis = 0L;
        if (processedRequests > 0L && meantimes > 0L) {
            eventsPerMillis = processedRequests / (double) meantimes;
        }
        return Math.round(eventsPerMillis * 60000L);
    }

    @Override
    public long getSlotCount() {
    	return RateLimiter.getSlotCount();
    }

    @Override
    public long getTotalProcessedRequests() {
        Measurement current = measurements.peekLast();
        return null != current ? current.getProcessedRequests() : 0L;
    }

    @Override
    public void clear() {
        RateLimiter.clear();
    }

    public void close() {
        timer.cancel();
        timer.purge();
    }

    private final class MeasureTask extends TimerTask {

        /**
         * Initializes a new {@link RateLimiterMBeanImpl.MeasureTask}.
         */
        MeasureTask() {
            super();
        }

        @Override
        public void run() {
            measurements.add(new Measurement(RateLimiter.getProcessedRequests()));
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
        private final long measuredProcessedRequest;

        Measurement(long processedRequests) {
            super();
            this.measuredProcessedRequest = processedRequests;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * Gets the time in milliseconds after 1970-01-01 00:00:00 UTC when this measurement was created.
         *
         * @return The timestamp
         */
        long getTimestamp() {
            return timestamp;
        }

        /**
         * Gets the total number of processed requests.
         *
         * @return The processed requests
         */
        long getProcessedRequests() {
            return measuredProcessedRequest;
        }

    }

}
