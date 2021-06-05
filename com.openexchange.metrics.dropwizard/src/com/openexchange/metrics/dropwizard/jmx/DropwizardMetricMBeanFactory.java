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

package com.openexchange.metrics.dropwizard.jmx;

import javax.management.NotCompliantMBeanException;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.dropwizard.jmx.beans.CounterMBeanImpl;
import com.openexchange.metrics.dropwizard.jmx.beans.GaugeMBeanImpl;
import com.openexchange.metrics.dropwizard.jmx.beans.HistogramMBeanImpl;
import com.openexchange.metrics.dropwizard.jmx.beans.MeterMBeanImpl;
import com.openexchange.metrics.dropwizard.jmx.beans.TimerMBeanImpl;
import com.openexchange.metrics.dropwizard.types.DropwizardCounter;
import com.openexchange.metrics.dropwizard.types.DropwizardGauge;
import com.openexchange.metrics.dropwizard.types.DropwizardHistogram;
import com.openexchange.metrics.dropwizard.types.DropwizardMeter;
import com.openexchange.metrics.dropwizard.types.DropwizardTimer;
import com.openexchange.metrics.jmx.MetricMBeanFactory;
import com.openexchange.metrics.jmx.beans.CounterMBean;
import com.openexchange.metrics.jmx.beans.GaugeMBean;
import com.openexchange.metrics.jmx.beans.HistogramMBean;
import com.openexchange.metrics.jmx.beans.MeterMBean;
import com.openexchange.metrics.jmx.beans.TimerMBean;
import com.openexchange.metrics.types.Counter;
import com.openexchange.metrics.types.Gauge;
import com.openexchange.metrics.types.Histogram;
import com.openexchange.metrics.types.Meter;
import com.openexchange.metrics.types.Metric;
import com.openexchange.metrics.types.Timer;

/**
 * {@link DropwizardMetricMBeanFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropwizardMetricMBeanFactory implements MetricMBeanFactory {

    /**
     * Initialises a new {@link DropwizardMetricMBeanFactory}.
     */
    public DropwizardMetricMBeanFactory() {
        super();
    }

    @Override
    public CounterMBean counter(Counter counter, MetricDescriptor metricDescriptor) {
        checkInstance(counter, DropwizardCounter.class);
        try {
            return new CounterMBeanImpl((DropwizardCounter) counter, metricDescriptor);
        } catch (NotCompliantMBeanException e) {
            throw new IllegalArgumentException("The CounterMBean is not a compliant MBean");
        }
    }

    @Override
    public TimerMBean timer(Timer timer, MetricDescriptor metricDescriptor) {
        checkInstance(timer, DropwizardTimer.class);
        try {
            return new TimerMBeanImpl((DropwizardTimer) timer, metricDescriptor);
        } catch (NotCompliantMBeanException e) {
            throw new IllegalArgumentException("The TimerMBean is not a compliant MBean");
        }
    }

    @Override
    public MeterMBean meter(Meter meter, MetricDescriptor metricDescriptor) {
        checkInstance(meter, DropwizardMeter.class);
        try {
            return new MeterMBeanImpl((DropwizardMeter) meter, metricDescriptor);
        } catch (NotCompliantMBeanException e) {
            throw new IllegalArgumentException("The MeterMBean is not a compliant MBean");
        }
    }

    @Override
    public HistogramMBean histogram(Histogram histogram, MetricDescriptor metricDescriptor) {
        checkInstance(histogram, DropwizardHistogram.class);
        try {
            return new HistogramMBeanImpl((DropwizardHistogram) histogram, metricDescriptor);
        } catch (NotCompliantMBeanException e) {
            throw new IllegalArgumentException("The HistogramMBean is not a compliant MBean");
        }
    }

    @Override
    public GaugeMBean gauge(Gauge<?> gauge, MetricDescriptor metricDescriptor) {
        try {
            return new GaugeMBeanImpl((DropwizardGauge) gauge, metricDescriptor);
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
        if (false == metric.getClass().isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Invalid metric specified for '" + clazz.getSimpleName() + "' mbean: '" + metric.getClass() + "'");
        }
    }
}
