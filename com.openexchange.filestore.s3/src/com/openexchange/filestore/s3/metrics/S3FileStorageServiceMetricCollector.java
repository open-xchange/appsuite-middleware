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
import com.openexchange.metrics.MetricCollector;
import com.openexchange.metrics.MetricMetadata;
import com.openexchange.metrics.MetricType;

/**
 * {@link S3FileStorageServiceMetricCollector}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class S3FileStorageServiceMetricCollector extends ServiceMetricCollector {

    private static final String COUNTER_TOTAL_BYTES = "counter.totalBytes";
    private static final String METER_THROUPUT = "meter.throuput";

    private final MetricCollector internalCollector;

    /**
     * Initialises a new {@link S3FileStorageServiceMetricCollector}.
     */
    public S3FileStorageServiceMetricCollector(MetricCollector metricCollector) {
        super();
        internalCollector = metricCollector;
        internalCollector.getMetricMetadata().add(new MetricMetadata(MetricType.METER, METER_THROUPUT, "bytes", TimeUnit.SECONDS));
        internalCollector.getMetricMetadata().add(new MetricMetadata(MetricType.COUNTER, COUNTER_TOTAL_BYTES));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.amazonaws.metrics.ServiceMetricCollector#collectByteThroughput(com.amazonaws.metrics.ByteThroughputProvider)
     */
    @Override
    public void collectByteThroughput(final ByteThroughputProvider provider) {
        internalCollector.getMeter(METER_THROUPUT).mark(new Double(provider.getByteCount()).longValue());
        internalCollector.getCounter(COUNTER_TOTAL_BYTES).inc(provider.getByteCount());
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
}
