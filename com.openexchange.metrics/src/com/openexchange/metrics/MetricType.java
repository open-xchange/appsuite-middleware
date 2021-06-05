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

package com.openexchange.metrics;

import java.util.function.Supplier;
import com.openexchange.metrics.types.Counter;
import com.openexchange.metrics.types.Gauge;
import com.openexchange.metrics.types.Histogram;
import com.openexchange.metrics.types.Meter;
import com.openexchange.metrics.types.Timer;

/**
 * {@link MetricType}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum MetricType {
    /**
     * <p>A {@link Meter} measures the rate at which a set of events occur.</p>
     * 
     * <p>Meters measure the rate of the events in a few different ways.
     * The mean rate is the average rate of events. It's generally useful
     * for trivia, but as it represents the total rate for your application's
     * entire lifetime (e.g., the total number of requests handled, divided
     * by the number of seconds the process has been running), it doesn't
     * offer a sense of recency. Luckily, meters also record three different
     * exponentially-weighted moving average rates: the 1-, 5-, and 15-minute
     * moving averages.</p>
     */
    METER,
    /**
     * <p>A {@link Gauge} is the simplest metric type. It just returns a <i>value</i>. If, for
     * example, the component has a value which is maintained by a third-party
     * library, it easily be exposed by registering a Gauge instance which
     * returns that value.</p>
     * <p>{@link Gauge} is the only metric that requires a {@link Supplier}
     * to be passed along with the {@link MetricMetadata}.</p>
     */
    GAUGE,
    /**
     * A {@link Counter} is a simple incrementing and decrementing 64-bit integer that
     * starts out at <code>0</code>.
     */
    COUNTER,
    /**
     * <p>A {@link Timer} is basically a histogram of the duration of a type of event and
     * a meter of the rate of its occurrence.</p>
     * <p><b>Note:</b>
     * Elapsed times for it events are measured internally in nanoseconds,
     * using Java's high-precision System.nanoTime() method. Its precision and
     * accuracy vary depending on operating system and hardware.
     * </p>
     */
    TIMER,
    /**
     * <p>A {@link Histogram} measures the distribution of values in a stream of data: e.g.,
     * the number of results returned by a search.</p>
     * <p>Histogram metrics allow the measurement of not just easy things like the min,
     * mean, max, and standard deviation of values, but also quantiles like the
     * median or 95th percentile.</p>
     * <p>Traditionally, the way the median (or any other quantile) is calculated is to take
     * the entire data set, sort it, and take the value in the middle (or 1% from the end,
     * for the 99th percentile). This works for small data sets, or batch processing systems,
     * but not for high-throughput, low-latency services.
     * </p>
     */
    HISTOGRAM;
}
