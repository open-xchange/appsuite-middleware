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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(MetricMBeanFactory.class);

    public static CounterMBean counter(Metric metric) {
        try {
            return new CounterMBeanImpl((Counter) metric);
        } catch (NotCompliantMBeanException e) {
            LOG.error("The mbean could not be registered", e);
            throw new IllegalArgumentException("Invalid metric specified for counter '" + metric.getClass() + "'");
        }
    }

    public static TimerMBean timer(Metric metric, MetricMetadata metricMetadata) {
        try {
            return new TimerMBeanImpl((Timer) metric, metricMetadata.getMetricTimeUnit());
        } catch (NotCompliantMBeanException e) {
            LOG.error("The mbean could not be registered", e);
            throw new IllegalArgumentException("Invalid metric specified for timer '" + metric.getClass() + "'");
        }
    }

    public static MeterMBean meter(Metric metric, MetricMetadata metricMetadata) {
        try {
            return new MeterMBeanImpl((Meter) metric, metricMetadata.getMetricRate(), metricMetadata.getMetricTimeUnit());
        } catch (NotCompliantMBeanException e) {
            LOG.error("The mbean could not be registered", e);
            throw new IllegalArgumentException("Invalid metric specified for meter '" + metric.getClass() + "'");
        }
    }

    public static HistogramMBean histogram(Metric metric) {
        try {
            return new HistogramMBeanImpl((Histogram) metric);
        } catch (NotCompliantMBeanException e) {
            LOG.error("The mbean could not be registered", e);
            throw new IllegalArgumentException("Invalid metric specified for histogram '" + metric.getClass() + "'");
        }
    }

    public static GaugeMBean gauge(Metric metric) {
        try {
            return new GaugeMBeanImpl((Gauge<?>) metric);
        } catch (NotCompliantMBeanException e) {
            LOG.error("The mbean could not be registered", e);
            throw new IllegalArgumentException("Invalid metric specified for gauge '" + metric.getClass() + "'");
        }
    }
}
