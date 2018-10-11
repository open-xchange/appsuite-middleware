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

package com.openexchange.metrics.jmx;

import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.jmx.beans.CounterMBean;
import com.openexchange.metrics.jmx.beans.GaugeMBean;
import com.openexchange.metrics.jmx.beans.HistogramMBean;
import com.openexchange.metrics.jmx.beans.MeterMBean;
import com.openexchange.metrics.jmx.beans.TimerMBean;
import com.openexchange.metrics.types.Counter;
import com.openexchange.metrics.types.Gauge;
import com.openexchange.metrics.types.Histogram;
import com.openexchange.metrics.types.Meter;
import com.openexchange.metrics.types.Timer;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link MetricMBeanFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@SingletonService
public interface MetricMBeanFactory {

    /**
     * Creates a new {@link CounterMBean} from the specified {@link Counter} metric
     * 
     * @param counter The {@link Counter} from which to create the mbean
     * @param metricDescriptor The {@link MetricDescriptor}
     * @return The {@link CounterMBean}
     */
    CounterMBean counter(Counter counter, MetricDescriptor metricDescriptor);

    /**
     * Creates a new {@link TimerMBean} from the specified {@link Timer} metric
     * 
     * @param counter The {@link Timer} from which to create the mbean
     * @param metricDescriptor The {@link MetricDescriptor}
     * @return The {@link TimerMBean}
     */
    TimerMBean timer(Timer timer, MetricDescriptor metricDescriptor);

    /**
     * Creates a new {@link MeterMBean} from the specified {@link Meter} metric
     * 
     * @param counter The {@link Meter} from which to create the mbean
     * @param metricDescriptor The {@link MetricDescriptor}
     * @return The {@link MeterMBean}
     */
    MeterMBean meter(Meter meter, MetricDescriptor metricDescriptor);

    /**
     * Creates a new {@link HistogramMBean} from the specified {@link Histogram} metric
     * 
     * @param metricDescriptor The {@link MetricDescriptor}
     * @param counter The {@link Histogram} from which to create the mbean
     * 
     * @return The {@link HistogramMBean}
     */
    HistogramMBean histogram(Histogram histogram, MetricDescriptor metricDescriptor);

    /**
     * Creates a new {@link GaugeMBean} from the specified {@link Gauge} metric
     * 
     * @param metricDescriptor The {@link MetricDescriptor}
     * @param counter The {@link Gauge} from which to create the mbean
     * @return The {@link GaugeMBean}
     */
    GaugeMBean gauge(Gauge<?> gauge, MetricDescriptor metricDescriptor);
}
