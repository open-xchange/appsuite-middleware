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

import static com.openexchange.java.Autoboxing.D;
import static com.openexchange.java.Autoboxing.d;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.util.AWSRequestMetrics;
import com.amazonaws.util.AWSRequestMetrics.Field;
import com.amazonaws.util.TimingInfo;
import com.openexchange.filestore.s3.internal.config.S3ClientConfig;
import com.openexchange.filestore.s3.internal.config.S3ClientProperty;
import com.openexchange.java.Strings;
import com.openexchange.metrics.micrometer.Micrometer;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

/**
 * {@link PerClientMetricCollector}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class PerClientMetricCollector extends RequestMetricCollector {

    private static final String NO_UNIT = null;
    private static final Double NaN = D(Double.NaN);
    private final AtomicReference<Double> poolAvailable = new AtomicReference<>(NaN);
    private final AtomicReference<Double> poolLeased = new AtomicReference<>(NaN);
    private final AtomicReference<Double> poolPending = new AtomicReference<>(NaN);
    private final double maxConnections;
    private final Tags tags;

    /**
     * Initializes a new {@link PerClientMetricCollector}.
     * @param clientConfig
     */
    public PerClientMetricCollector(S3ClientConfig clientConfig) {
        super();
        if (! clientConfig.getClientScope().isShared() || clientConfig.getClientID().isPresent() == false) {
            throw new IllegalArgumentException("Either the client config is not a shared one or it is missing the client id!");
        }
        double maxConnections = Double.NaN;
        String maxConnectionPoolSize = clientConfig.getValue(S3ClientProperty.MAX_CONNECTION_POOL_SIZE);
        if (Strings.isNotEmpty(maxConnectionPoolSize)) {
            try {
                maxConnections = new Integer(maxConnectionPoolSize.trim()).doubleValue();
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                // ignore
            }
        }
        this.maxConnections = maxConnections;
        this.tags = Tags.of("client", clientConfig.getClientID().get());
        initPoolMetrics();
    }

    /**
     * Initializes the pool metrics
     */
    private void initPoolMetrics() {
        // @formatter:off
        Micrometer.registerOrUpdateGauge(Metrics.globalRegistry,
            "appsuite.filestore.s3.connections.max",
            tags,
            "The configured maximum number of concurrent connections.",
            NO_UNIT,
            this, (m) -> m.maxConnections);

        Micrometer.registerOrUpdateGauge(Metrics.globalRegistry,
            "appsuite.filestore.s3.connections.available",
            tags,
            "The number of available pooled HTTP connections.",
            NO_UNIT,
            this, (m) -> d(m.poolAvailable.get()));

        Micrometer.registerOrUpdateGauge(Metrics.globalRegistry,
            "appsuite.filestore.s3.connections.leased",
            tags,
            "The number of leased pooled HTTP connections.",
            NO_UNIT,
            this, (m) -> d(m.poolLeased.get()));
        Micrometer.registerOrUpdateGauge(Metrics.globalRegistry,
            "appsuite.filestore.s3.connections.pending",
            tags,
            "The number of threads pending on getting a HTTP connection from the pool.",
            NO_UNIT,
            this, (m) -> d(m.poolPending.get()));
        // @formatter:on
    }

    @Override
    public void collectMetrics(Request<?> request, Response<?> response) {
        AWSRequestMetrics m = request.getAWSRequestMetrics();
        if (m == null) {
            return;
        }

        TimingInfo ti = m.getTimingInfo();
        if (ti == null) {
            return;
        }

        EnumMap<Field, AtomicReference<Double>> poolStatsCounters = new EnumMap<>(Field.class);
        poolStatsCounters.put(Field.HttpClientPoolAvailableCount, poolAvailable);
        poolStatsCounters.put(Field.HttpClientPoolLeasedCount, poolLeased);
        poolStatsCounters.put(Field.HttpClientPoolPendingCount, poolPending);
        for (Field field : poolStatsCounters.keySet()) {
            AtomicReference<Double> ref = poolStatsCounters.get(field);
            Number counter = ti.getCounter(field.name());
            if (counter != null && counter.doubleValue() >= 0.0d) {
                ref.set(new Double(counter.doubleValue()));
            }
        }

        // ServiceName=[Amazon S3], StatusCode=[200], ServiceEndpoint=[https://st-files-2-push.s3.eu-central-1.amazonaws.com], RequestType=[GetObjectMetadataRequest], AWSRequestID=[3A7F314BC82CD57A], HttpClientPoolPendingCount=0, RetryCapacityConsumed=0, HttpClientPoolAvailableCount=1, RequestCount=1, HttpClientPoolLeasedCount=0, ResponseProcessingTime=[8.717], ClientExecuteTime=[25.678], HttpClientSendRequestTime=[0.251], HttpRequestTime=[16.294], RequestSigningTime=[0.183], CredentialsRequestTime=[0.002, 0.001], HttpClientReceiveResponseTime=[15.52],

        String requestType = "UNKNOWN";
        List<Object> property = m.getProperty(Field.RequestType);
        if (property != null && property.isEmpty() == false) {
            requestType = property.get(property.size() - 1).toString();
        }

        String statusCode = "UNKNOWN";
        property = m.getProperty(Field.StatusCode);
        if (property != null && property.isEmpty() == false) {
            statusCode = property.get(property.size() - 1).toString();
        }

        Double timeTakenMillisIfKnown = ti.getTimeTakenMillisIfKnown();
        if (timeTakenMillisIfKnown != null && timeTakenMillisIfKnown.doubleValue() > 0.0d) {
            recordRequest(requestType, statusCode, timeTakenMillisIfKnown.doubleValue());
        }
    }

    /**
     * Records the given timing vale
     *
     * @param requestType The request type
     * @param statusCode The status code
     * @param doubleValue The timing
     */
    private void recordRequest(String requestType, String statusCode, double doubleValue) {
        Timer timer = Timer.builder("appsuite.filestore.s3.requests")
            .description("S3 HTTP request times")
            .tags(tags.and("type", requestType, "status", statusCode))
            .register(Metrics.globalRegistry);

        timer.record((long) doubleValue, TimeUnit.MILLISECONDS);
    }

}
