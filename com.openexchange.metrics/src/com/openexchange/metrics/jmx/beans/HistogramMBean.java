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
import com.openexchange.metrics.types.Histogram;

/**
 * {@link HistogramMBean}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface HistogramMBean extends MetricMBean {

    /**
     * Returns the number of values recorded.
     *
     * @return the number of values recorded
     */
    @MBeanMethodAnnotation(description = "Returns the number of values recorded", parameterDescriptions = { "" }, parameters = { "" })
    long getCount();

    /**
     * Returns the lowest value in the snapshot.
     *
     * @return the lowest value
     */
    @MBeanMethodAnnotation(description = "Returns the lowest value in the snapshot.", parameterDescriptions = { "" }, parameters = { "" })
    long getMin();

    /**
     * Returns the highest value in the snapshot.
     *
     * @return the highest value
     */
    @MBeanMethodAnnotation(description = "Returns the highest value in the snapshot.", parameterDescriptions = { "" }, parameters = { "" })
    long getMax();

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
     * Returns the entire set of values in the snapshot of the {@link Histogram}
     *
     * @return the entire set of values
     */
    @MBeanMethodAnnotation(description = "Returns the entire set of values in the snapshot of the histogram", parameterDescriptions = { "" }, parameters = { "" })
    long[] values();

    /**
     * Returns the number of values in the snapshot.
     *
     * @return the number of values
     */
    @MBeanMethodAnnotation(description = "Returns the number of values in the snapshot.", parameterDescriptions = { "" }, parameters = { "" })
    long getSnapshotSize();
}
