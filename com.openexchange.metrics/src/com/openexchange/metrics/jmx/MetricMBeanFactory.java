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
