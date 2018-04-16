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

package com.openexchange.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry.MetricSupplier;
import com.codahale.metrics.Timer;

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
     * <p>{@link Gauge} is the only metric that requires a {@link MetricSupplier}
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
