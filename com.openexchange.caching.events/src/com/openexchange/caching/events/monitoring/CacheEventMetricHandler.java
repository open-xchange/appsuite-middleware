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

package com.openexchange.caching.events.monitoring;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.metrics.MetricDescriptorCache;
import com.openexchange.metrics.MetricService;
import com.openexchange.metrics.MetricType;

/**
 * {@link CacheEventMetricHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public final class CacheEventMetricHandler implements CacheEventMonitor {

    private static class MetricServiceAndCache {

        final MetricDescriptorCache metricDescriptorCache;
        final MetricService metricService;

        MetricServiceAndCache(MetricService metricService) {
            super();
            this.metricDescriptorCache = new MetricDescriptorCache(metricService, "cache");
            this.metricService = metricService;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private final AtomicReference<MetricServiceAndCache> metricServiceRef;
    private final AtomicLong numOfferedEvents;
    private final AtomicLong numDeliveredEvents;

    /**
     * Initialises a new {@link CacheEventMetricHandler}.
     *
     * @param metricService The instance of the {@link MetricService}
     */
    public CacheEventMetricHandler(MetricService metricService) {
        super();
        this.metricServiceRef = new AtomicReference<>(metricService == null ? null : new MetricServiceAndCache(metricService));
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
     * Sets the metric service reference to given value
     *
     * @param metricService The metricService or <code>null</code>
     */
    public void setMetricService(MetricService metricService) {
        metricServiceRef.set(metricService == null ? null : new MetricServiceAndCache(metricService));
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
        /*
         * increment overall and region-specific metrics
         */
        MetricServiceAndCache metricServiceAndCache = metricServiceRef.get();
        if (null == metricServiceAndCache) {
            return;
        }
        MetricService metricService = metricServiceAndCache.metricService;
        MetricDescriptorCache metricDescriptorCache = metricServiceAndCache.metricDescriptorCache;
        metricService.getMeter(metricDescriptorCache.getMetricDescriptor(
            MetricType.METER, "offeredEvents", "Offered events for all cache regions", "events", TimeUnit.MINUTES)).mark();
        if (null != region) {
            metricService.getMeter(metricDescriptorCache.getMetricDescriptor(
                MetricType.METER, region + ".offeredEvents", "Offered events for cache region \"" + region + "\"", "events", TimeUnit.MINUTES)).mark();
        }
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
        /*
         * increment overall and region-specific metrics
         */
        MetricServiceAndCache metricServiceAndCache = metricServiceRef.get();
        if (null == metricServiceAndCache) {
            return;
        }
        MetricService metricService = metricServiceAndCache.metricService;
        MetricDescriptorCache metricDescriptorCache = metricServiceAndCache.metricDescriptorCache;
        metricService.getMeter(metricDescriptorCache.getMetricDescriptor(
            MetricType.METER, "deliveredEvents", "Delivered events for all cache regions", "events", TimeUnit.MINUTES)).mark();
        if (null != region) {
            metricService.getMeter(metricDescriptorCache.getMetricDescriptor(
                MetricType.METER, region + ".deliveredEvents", "Delivered events for cache region \"" + region + "\"", "events", TimeUnit.MINUTES)).mark();
        }
    }

}
