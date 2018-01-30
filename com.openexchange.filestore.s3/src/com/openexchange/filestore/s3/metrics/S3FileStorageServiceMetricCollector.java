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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.filestore.s3.metrics;

import java.util.concurrent.TimeUnit;
import com.amazonaws.metrics.ByteThroughputProvider;
import com.amazonaws.metrics.ServiceLatencyProvider;
import com.amazonaws.metrics.ServiceMetricCollector;
import com.codahale.metrics.Histogram;
import com.openexchange.metrics.MetricRegistryService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link S3FileStorageServiceMetricCollector}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class S3FileStorageServiceMetricCollector extends ServiceMetricCollector {

    private static final double NANO_PER_SEC = TimeUnit.SECONDS.toNanos(1);
    private final Histogram throughputMetric;

    /**
     * Initialises a new {@link S3FileStorageServiceMetricCollector}.
     */
    public S3FileStorageServiceMetricCollector(String filestoreId, ServiceLookup services) {
        super();
        MetricRegistryService registryService = services.getService(MetricRegistryService.class);
        throughputMetric = registryService.registerHistogram(this.getClass(), filestoreId + ".throughput");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.amazonaws.metrics.ServiceMetricCollector#collectByteThroughput(com.amazonaws.metrics.ByteThroughputProvider)
     */
    @Override
    public void collectByteThroughput(final ByteThroughputProvider provider) {
        double bytesPerSec = bytesPerSecond(provider.getByteCount(), provider.getDurationNano());
        throughputMetric.update(new Double(bytesPerSec).longValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.amazonaws.metrics.ServiceMetricCollector#collectLatency(com.amazonaws.metrics.ServiceLatencyProvider)
     */
    @Override
    public void collectLatency(final ServiceLatencyProvider provider) {
        // no-op
    }

    /**
     * Returns the number of bytes per second, given the byte count and
     * duration in nano seconds. Duration of zero nanosecond will be treated
     * as 1 nanosecond.
     */
    double bytesPerSecond(final double byteCount, double durationNano) {
        if (byteCount < 0 || durationNano < 0) {
            throw new IllegalArgumentException("Neither the byte count nor the duration must be negative!");
        }

        // Defend against division by zero
        if (durationNano == 0) {
            durationNano = 1.0;
        }
        return (byteCount / durationNano) * NANO_PER_SEC;
    }
}
