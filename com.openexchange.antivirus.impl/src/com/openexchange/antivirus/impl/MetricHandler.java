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

package com.openexchange.antivirus.impl;

import java.time.Duration;
import com.google.common.cache.Cache;
import com.openexchange.antivirus.AntiVirusResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.cache.GuavaCacheMetrics;

/**
 * {@link MetricHandler} - Simple utility class to handle metric updates
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
final class MetricHandler {

    private final Counter byteTransferCounter;

    /**
     * Initializes a new {@link MetricHandler}.
     * @param cachedResults
     */
    public MetricHandler(Cache<String, AntiVirusResult> cachedResults) {
        super();
        GuavaCacheMetrics.monitor(Metrics.globalRegistry, cachedResults, "antivirus");
        this.byteTransferCounter = Counter.builder("appsuite.antivirus.transfer")
            .description("Measures the amount of bytes transfered to the anti-virus server")
            .baseUnit("bytes")
            .register(Metrics.globalRegistry);
    }

    /**
     * Monitor timing of an ICAP request that failed due to an IO error
     *
     * @param duration Duration from request start to occurred error
     */
    public void recordScanIOError(Duration duration) {
        Timer timer = Timer.builder("appsuite.antivirus.scans.duration")
            .description("Measures the number of files scanned per second")
            .tags("status", "IO_ERROR")
            .register(Metrics.globalRegistry);
        timer.record(duration);
    }

    /**
     * Monitor timing of an ICAP request that was answered by a proper HTTP response
     *
     * @param statusCode The HTTP status response from the ICAP server
     * @param duration Duration from request start to received response
     * @param contentLength Length of request body in bytes to update the transfer rate meter
     */
    public void recordScanResult(int statusCode, Duration duration, long contentLength) {
        Timer timer = Timer.builder("appsuite.antivirus.scans.duration")
            .description("Measures the number of files scanned per second")
            .tags("status", Integer.toString(statusCode))
            .register(Metrics.globalRegistry);
        timer.record(duration);

        byteTransferCounter.increment(contentLength);
    }

}
