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

package com.openexchange.filestore.s3.metrics;

import java.util.concurrent.TimeUnit;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.metrics.AwsSdkMetrics;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.util.AWSRequestMetrics;
import com.amazonaws.util.AWSRequestMetrics.Field;
import com.amazonaws.util.TimingInfo;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

/**
 * {@link S3FileStorageRequestMetricCollector}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class S3FileStorageRequestMetricCollector extends RequestMetricCollector {

    /**
     * Initializes a new {@link S3FileStorageRequestMetricCollector}.
     */
    public S3FileStorageRequestMetricCollector() {
        super();
    }

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
                    Timer timer = Timer.builder("appsuite.s3.requestTimes."+request.getHttpMethod().name()).description(String.format("The execution time of %s requests measured in events/sec", request.getHttpMethod().name())).register(Metrics.globalRegistry);
                    timer.record(longValue, TimeUnit.MILLISECONDS);
                    break;
                default:
                    break;
            }
        }
    }

}
