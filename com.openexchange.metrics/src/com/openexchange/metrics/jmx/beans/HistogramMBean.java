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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
