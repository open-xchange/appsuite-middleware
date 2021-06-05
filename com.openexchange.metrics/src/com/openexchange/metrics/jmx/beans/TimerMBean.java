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

package com.openexchange.metrics.jmx.beans;

import com.openexchange.management.MBeanMethodAnnotation;
import com.openexchange.metrics.types.Meter;

/**
 * {@link TimerMBean}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface TimerMBean extends MetricMBean {

    /**
     * Returns the lowest value in the snapshot.
     *
     * @return the lowest value
     */
    @MBeanMethodAnnotation(description = "Returns the lowest value in the snapshot.", parameterDescriptions = { "" }, parameters = { "" })
    double getMin();

    /**
     * Returns the highest value in the snapshot.
     *
     * @return the highest value
     */
    @MBeanMethodAnnotation(description = "Returns the highest value in the snapshot.", parameterDescriptions = { "" }, parameters = { "" })
    double getMax();

    /**
     * Returns the arithmetic mean of the values in the snapshot.
     *
     * @return the arithmetic mean
     */
    @MBeanMethodAnnotation(description = "Returns the arithmetic mean of the values in the snapshot.", parameterDescriptions = { "" }, parameters = { "" })
    double getMean();

    /**
     * Returns the standard deviation of the values in the snapshot.
     *
     * @return the standard value
     */
    @MBeanMethodAnnotation(description = "Returns the standard deviation of the values in the snapshot.", parameterDescriptions = { "" }, parameters = { "" })
    double getStdDev();

    /**
     * Returns the median value in the distribution.
     *
     * @return the median value
     */
    @MBeanMethodAnnotation(description = "Returns the median value in the distribution.", parameterDescriptions = { "" }, parameters = { "" })
    double get50thPercentile();

    /**
     * Returns the value at the 75th percentile in the distribution.
     *
     * @return the value at the 75th percentile
     */
    @MBeanMethodAnnotation(description = "Returns the value at the 75th percentile in the distribution.", parameterDescriptions = { "" }, parameters = { "" })
    double get75thPercentile();

    /**
     * Returns the value at the 95th percentile in the distribution.
     *
     * @return the value at the 95th percentile
     */
    @MBeanMethodAnnotation(description = "Returns the value at the 95th percentile in the distribution.", parameterDescriptions = { "" }, parameters = { "" })
    double get95thPercentile();

    /**
     * Returns the value at the 98th percentile in the distribution.
     *
     * @return the value at the 98th percentile
     */
    @MBeanMethodAnnotation(description = "Returns the value at the 98th percentile in the distribution.", parameterDescriptions = { "" }, parameters = { "" })
    double get98thPercentile();

    /**
     * Returns the value at the 99th percentile in the distribution.
     *
     * @return the value at the 99th percentile
     */
    @MBeanMethodAnnotation(description = "Returns the value at the 99th percentile in the distribution.", parameterDescriptions = { "" }, parameters = { "" })
    double get99thPercentile();

    /**
     * Returns the value at the 99.9th percentile in the distribution.
     *
     * @return the value at the 99.9th percentile
     */
    @MBeanMethodAnnotation(description = "Returns the value at the 99.9th percentile in the distribution.", parameterDescriptions = { "" }, parameters = { "" })
    double get999thPercentile();

    /**
     * Returns the entire set of values in the snapshot.
     *
     * @return the entire set of values
     */
    @MBeanMethodAnnotation(description = "Returns the entire set of values in the snapshot.", parameterDescriptions = { "" }, parameters = { "" })
    long[] values();

    /**
     * Returns the sampling unit
     *
     * @return the sampling unit
     */
    @MBeanMethodAnnotation(description = "Returns the sampling unit.", parameterDescriptions = { "" }, parameters = { "" })
    String getDurationUnit();
    
    /**
     * Returns the number of events which have been marked.
     *
     * @return the number of events which have been marked
     */
    @MBeanMethodAnnotation(description = "Returns the number of events which have been marked.", parameterDescriptions = { "" }, parameters = { "" })
    long getCount();

    /**
     * Returns the mean rate at which events have occurred since the meter was created.
     *
     * @return the mean rate at which events have occurred since the meter was created
     */
    @MBeanMethodAnnotation(description = "Returns the mean rate at which events have occurred since the meter was created.", parameterDescriptions = { "" }, parameters = { "" })
    double getMeanRate();

    /**
     * Returns the one-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     * <p/>
     * This rate has the same exponential decay factor as the one-minute load average in the
     * <code>top</code> Unix command.
     *
     * @return the one-minute exponentially-weighted moving average rate at which events have
     *         occurred since the meter was created
     */
    @MBeanMethodAnnotation(description = "Returns the one-minute exponentially-weighted moving average rate at which events have occurred since the meter was created. This rate has the same exponential decay factor as the one-minute load average in the top Unix command.", parameterDescriptions = { "" }, parameters = { "" })
    double getOneMinuteRate();

    /**
     * Returns the five-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     * <p/>
     * This rate has the same exponential decay factor as the five-minute load average in the
     * <code>top</code> Unix command.
     *
     * @return the five-minute exponentially-weighted moving average rate at which events have
     *         occurred since the meter was created
     */
    @MBeanMethodAnnotation(description = "Returns the five-minute exponentially-weighted moving average rate at which events have occurred since the meter was created. This rate has the same exponential decay factor as the five-minute load average in the top Unix command.", parameterDescriptions = { "" }, parameters = { "" })
    double getFiveMinuteRate();

    /**
     * Returns the fifteen-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     * <p/>
     * This rate has the same exponential decay factor as the fifteen-minute load average in the
     * {@code top} Unix command.
     *
     * @return the fifteen-minute exponentially-weighted moving average rate at which events have
     *         occurred since the meter was created
     */
    @MBeanMethodAnnotation(description = "Returns the fifteen-minute exponentially-weighted moving average rate at which events have occurred since the meter was created. This rate has the same exponential decay factor as the fifteen-minute load average in the top Unix command.", parameterDescriptions = { "" }, parameters = { "" })
    double getFifteenMinuteRate();

    /**
     * Returns the rate unit of the {@link Meter}
     * 
     * @return the rate unit of the {@link Meter}
     */
    @MBeanMethodAnnotation(description = "Returns the rate unit of the meter", parameterDescriptions = { "" }, parameters = { "" })
    String getRateUnit();
}
