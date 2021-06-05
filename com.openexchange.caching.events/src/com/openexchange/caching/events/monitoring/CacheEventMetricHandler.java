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

import java.util.concurrent.atomic.AtomicLong;
import com.openexchange.java.Strings;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

/**
 * {@link CacheEventMetricHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public final class CacheEventMetricHandler implements CacheEventMonitor {

    private static final String GROUP = "appsuite.jcs.cache.events";
    private static final String OFFERED = GROUP + ".offered";
    private static final String DELIVERED = GROUP + ".delivered";
    private static final String TAG_REGION = "region";
    private static final String ALL_REGION = "all";

    private final AtomicLong numOfferedEvents;
    private final AtomicLong numDeliveredEvents;

    /**
     * Initializes a new {@link CacheEventMetricHandler}.
     *
     */
    public CacheEventMetricHandler() {
        super();
        this.numOfferedEvents = new AtomicLong();
        this.numDeliveredEvents = new AtomicLong();
    }

    @Override
    public long getOfferedEvents() {
        return numOfferedEvents.get();
    }

    @Override
    public long getDeliveredEvents() {
        return numDeliveredEvents.get();
    }

    /**
     * Increments the offered events for the specified cache region as well as the overall cache events.
     *
     * @param region The region for which to increment the offered events
     */
    public void incrementOfferedEvents(String region) {
        /*
         * increment legacy overall counter
         */
        if (numOfferedEvents.incrementAndGet() < 0L) {
            numOfferedEvents.set(0L);
        }

        if (Strings.isEmpty(region)) {
            region = ALL_REGION;
        }

        /*
         * increment overall and region-specific metrics
         */
        //@formatter:off
        Counter.builder(OFFERED)
            .description("Offered events for cache regions")
            .tag(TAG_REGION, region)
            .register(Metrics.globalRegistry).increment();
        //@formatter:on
    }

    /**
     * Increments the delivered events for the specified cache region as well as the overall cache events.
     *
     * @param region The region for which to increment the delivered events
     */
    public void incrementDeliveredEvents(String region) {
        /*
         * increment legacy overall counter
         */
        if (numDeliveredEvents.incrementAndGet() < 0L) {
            numDeliveredEvents.set(0L);
        }

        if (Strings.isEmpty(region)) {
            region = ALL_REGION;
        }

        /*
         * increment overall and region-specific metrics
         */
        //@formatter:off
        Counter.builder(DELIVERED)
            .description("Delivered events for cache regions")
            .tag(TAG_REGION, region)
            .register(Metrics.globalRegistry)
            .increment();
        //@formatter:on
    }

}
