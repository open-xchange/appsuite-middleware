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

package com.openexchange.antivirus.impl.impl;

import java.util.function.Consumer;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.MetricDescriptorCache;
import com.openexchange.metrics.MetricService;
import com.openexchange.metrics.MetricType;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MetricHandler} - Simple utility class to handle metric updates
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
final class MetricHandler {

    private final MetricDescriptorCache metricDescriptorCache;
    private final ServiceLookup services;

    /**
     * Initialises a new {@link MetricHandler}.
     */
    public MetricHandler(ServiceLookup services) {
        super();
        this.services = services;
        this.metricDescriptorCache = new MetricDescriptorCache(services.getService(MetricService.class), "antivirus");
    }

    /**
     * Increments the cache hits metric
     */
    void incrementCacheHits() {
        MetricDescriptor descriptor = metricDescriptorCache.getMetricDescriptor(MetricType.COUNTER, "Cache Hit", "Cached Anti-Virus results hits", "results");
        updateMetric(t -> t.getCounter(descriptor).incement());
    }

    /**
     * Increments the cache misses metric
     */
    void incrementCacheMisses() {
        MetricDescriptor descriptor = metricDescriptorCache.getMetricDescriptor(MetricType.COUNTER, "Cache Miss", "Cached Anti-Virus results misses", "results");
        updateMetric(t -> t.getCounter(descriptor).incement());
    }

    /**
     * Increments the cache invalidations metric
     */
    void incrementCacheInvalidations() {
        MetricDescriptor descriptor = metricDescriptorCache.getMetricDescriptor(MetricType.COUNTER, "Cache Invalidations", "Cached Anti-Virus results invalidations", "results");
        updateMetric(t -> t.getCounter(descriptor).incement());
    }

    /**
     * Increments the scans per second metric
     */
    void updateScansPerSecond() {
        MetricDescriptor descriptor = metricDescriptorCache.getMetricDescriptor(MetricType.METER, "Scanning Rate", "Measures the number of files scanned per second", "scans");
        updateMetric(t -> t.getMeter(descriptor).mark());
    }

    /**
     * Updates the scanning time by the specified elapsed time
     * 
     * @param timeElapsed The elapsed time
     */
    void updateScanningTime(long timeElapsed) {
        MetricDescriptor descriptor = metricDescriptorCache.getMetricDescriptor(MetricType.HISTOGRAM, "Scanning Time", "Measures the time elapsed during scanning a file", "");
        updateMetric(t -> t.getHistogram(descriptor).update(timeElapsed));
    }

    /**
     * Updates the transfer rate by the specified content length
     * 
     * @param contentLength The content length
     */
    void updateTransferRate(long contentLength) {
        MetricDescriptor descriptor = metricDescriptorCache.getMetricDescriptor(MetricType.METER, "Transfer Rate", "Measures the transfer rate when uploading a file to the anti-virus server", "bytes");
        updateMetric(t -> t.getMeter(descriptor).mark(contentLength));
    }

    /**
     * Updates the metric specified in the provided {@link Consumer}
     * 
     * @param consumer The consumer
     */
    private void updateMetric(Consumer<MetricService> consumer) {
        MetricService metricService = services.getService(MetricService.class);
        if (metricService == null) {
            return;
        }
        consumer.accept(metricService);
    }
}
