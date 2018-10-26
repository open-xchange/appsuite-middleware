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
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.metrics.AwsSdkMetrics;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.util.AWSRequestMetrics;
import com.amazonaws.util.AWSRequestMetrics.Field;
import com.amazonaws.util.TimingInfo;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.MetricDescriptorCache;
import com.openexchange.metrics.MetricService;
import com.openexchange.metrics.MetricType;
import com.openexchange.metrics.types.Timer;

/**
 * {@link S3FileStorageRequestMetricCollector}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class S3FileStorageRequestMetricCollector extends RequestMetricCollector {

    private final MetricService metricService;
    private MetricDescriptorCache metricDescriptorCache;

    /**
     * Initialises a new {@link S3FileStorageRequestMetricCollector}.
     */
    public S3FileStorageRequestMetricCollector(MetricService metricService) {
        super();
        this.metricService = metricService;
        metricDescriptorCache = new MetricDescriptorCache(metricService, "s3");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.amazonaws.metrics.RequestMetricCollector#collectMetrics(com.amazonaws.Request, com.amazonaws.Response)
     */
    @Override
    public void collectMetrics(Request<?> request, Response<?> response) {
        timeRequest(request);
    }

    /**
     * Measures the the runtime of the specified {@link Request}
     *
     * @param request The {@link Request} for which to measure the runtime
     */
    private void timeRequest(Request<?> request) {
        for (com.amazonaws.metrics.MetricType type : AwsSdkMetrics.getPredefinedMetrics()) {
            if (!(type instanceof Field)) {
                continue;
            }

            AWSRequestMetrics metrics = request.getAWSRequestMetrics();
            TimingInfo timingInfo = metrics.getTimingInfo();
            Double timeTakenMillisIfKnown = timingInfo.getTimeTakenMillisIfKnown();
            if (timeTakenMillisIfKnown == null) {
                return;
            }

            long longValue = timeTakenMillisIfKnown.longValue();
            Field predefined = (Field) type;
            switch (predefined) {
                case ClientExecuteTime:
                    MetricDescriptor metricDescriptor = metricDescriptorCache.getMetricDescriptor(MetricType.TIMER, "RequestTimes." + request.getHttpMethod().name(), "The execution time of %s requests measured in events/sec", "events");
                    Timer timer = metricService.getTimer(metricDescriptor);
                    timer.update(longValue, TimeUnit.MILLISECONDS);
                default:
                    break;
            }
        }
    }

    /**
     * Stops the metric collector and unregisters all metrics
     */
    void stop() {
        metricDescriptorCache.clear();
    }
}
