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

package com.openexchange.metrics.jmx;

import javax.management.NotCompliantMBeanException;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Timer;
import com.openexchange.metrics.MetricMetadata;
import com.openexchange.metrics.jmx.impl.CounterMBeanImpl;
import com.openexchange.metrics.jmx.impl.GaugeMBeanImpl;
import com.openexchange.metrics.jmx.impl.HistogramMBeanImpl;
import com.openexchange.metrics.jmx.impl.MeterMBeanImpl;
import com.openexchange.metrics.jmx.impl.TimerMBeanImpl;

/**
 * {@link MetricMBeanFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class MetricMBeanFactory {

    /**
     * Creates a new {@link CounterMBean} with the specified {@link Metric}
     * 
     * @param metric The {@link Metric} of type {@link Counter} for the {@link CounterMBean}
     * @return The newly created {@link CounterMBean}
     * @throws IllegalArgumentException if an invalid type of {@link Metric} is supplied or if the
     *             {@link CounterMBean} does not follow JMX design patterns for Management Interfaces
     */
    public static CounterMBean counter(Metric metric) {
        checkInstance(metric, Counter.class);
        try {
            return new CounterMBeanImpl((Counter) metric);
        } catch (NotCompliantMBeanException e) {
            throw new IllegalArgumentException("The CounterMBean is not a compliant MBean");
        }
    }

    /**
     * Creates a new {@link TimerMBean} with the specified {@link Metric}
     * 
     * @param metric The {@link Metric} of type {@link Timer} for the {@link TimerMBean}
     * @return The newly created {@link TimerMBean}
     * @throws IllegalArgumentException if an invalid type of {@link Metric} is supplied or if the
     *             {@link TimerMBean} does not follow JMX design patterns for Management Interfaces
     */
    public static TimerMBean timer(Metric metric, MetricMetadata metricMetadata) {
        checkInstance(metric, Timer.class);
        try {
            return new TimerMBeanImpl((Timer) metric, metricMetadata.getMetricTimeUnit());
        } catch (NotCompliantMBeanException e) {
            throw new IllegalArgumentException("The TimerMBean is not a compliant MBean");
        }
    }

    /**
     * Creates a new {@link MeterMBean} with the specified {@link Metric}
     * 
     * @param metric The {@link Metric} of type {@link Meter} for the {@link MeterMBean}
     * @return The newly created {@link MeterMBean}
     * @throws IllegalArgumentException if an invalid type of {@link Metric} is supplied or if the
     *             {@link MeterMBean} does not follow JMX design patterns for Management Interfaces
     */
    public static MeterMBean meter(Metric metric, MetricMetadata metricMetadata) {
        checkInstance(metric, Meter.class);
        try {
            return new MeterMBeanImpl((Meter) metric, metricMetadata.getMetricRate(), metricMetadata.getMetricTimeUnit());
        } catch (NotCompliantMBeanException e) {
            throw new IllegalArgumentException("The MeterMBean is not a compliant MBean");
        }
    }

    /**
     * Creates a new {@link HistogramMBean} with the specified {@link Metric}
     * 
     * @param metric The {@link Metric} of type {@link Histogram} for the {@link HistogramMBean}
     * @return The newly created {@link HistogramMBean}
     * @throws IllegalArgumentException if an invalid type of {@link Metric} is supplied or if the
     *             {@link HistogramMBean} does not follow JMX design patterns for Management Interfaces
     */
    public static HistogramMBean histogram(Metric metric) {
        checkInstance(metric, Histogram.class);
        try {
            return new HistogramMBeanImpl((Histogram) metric);
        } catch (NotCompliantMBeanException e) {
            throw new IllegalArgumentException("The HistogramMBean is not a compliant MBean");
        }
    }

    /**
     * Creates a new {@link GaugeMBean} with the specified {@link Metric}
     * 
     * @param metric The {@link Metric} of type {@link Gauge} for the {@link GaugeMBean}
     * @return The newly created {@link GaugeMBean}
     * @throws IllegalArgumentException if an invalid type of {@link Metric} is supplied or if the
     *             {@link GaugeMBean} does not follow JMX design patterns for Management Interfaces
     */
    public static GaugeMBean gauge(Metric metric) {
        checkInstance(metric, Gauge.class);
        try {
            return new GaugeMBeanImpl((Gauge<?>) metric);
        } catch (NotCompliantMBeanException e) {
            throw new IllegalArgumentException("The GaugeMBean is not a compliant MBean");
        }
    }

    /**
     * Checks if the instance of the specified {@link Metric} is assignable from the specified {@link Class}
     * 
     * @param metric The {@link Metric} to check
     * @param clazz The expected assignable {@link Class}
     * @throws IllegalArgumentException if the specified {@link Metric} is not assignable from the specified {@link Class}
     */
    private static void checkInstance(Metric metric, Class<?> clazz) {
        if (!metric.getClass().isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Invalid metric specified for '" + clazz.getSimpleName() + "' mbean: '" + metric.getClass() + "'");
        }
    }
}
