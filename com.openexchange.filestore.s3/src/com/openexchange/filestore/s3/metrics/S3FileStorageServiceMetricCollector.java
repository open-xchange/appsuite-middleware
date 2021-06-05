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

import com.amazonaws.metrics.ByteThroughputProvider;
import com.amazonaws.metrics.ServiceLatencyProvider;
import com.amazonaws.metrics.ServiceMetricCollector;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

/**
 * {@link S3FileStorageServiceMetricCollector}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class S3FileStorageServiceMetricCollector extends ServiceMetricCollector {

    /**
     * Initializes a new {@link S3FileStorageServiceMetricCollector}.
     */
    public S3FileStorageServiceMetricCollector() {
        super();
    }

    @Override
    public void collectByteThroughput(final ByteThroughputProvider provider) {
        Counter counter = Counter.builder("appsuite.filestore.s3.transferred")
            .description("The size of s3 requests.")
            .baseUnit("bytes")
            .tags("type", provider.getThroughputMetricType().name())
            .register(Metrics.globalRegistry);
        counter.increment(Integer.toUnsignedLong(provider.getByteCount()));
    }

    @Override
    public void collectLatency(final ServiceLatencyProvider provider) {
        // no-op
    }

}
