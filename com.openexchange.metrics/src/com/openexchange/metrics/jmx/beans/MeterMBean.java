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
 * {@link MeterMBean}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface MeterMBean extends MetricMBean {

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
