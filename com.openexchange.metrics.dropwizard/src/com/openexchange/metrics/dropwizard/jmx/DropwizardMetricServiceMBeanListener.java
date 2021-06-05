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

import com.openexchange.management.ManagementService;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.jmx.AbstractMetricServiceListener;
import com.openexchange.metrics.jmx.MetricMBeanFactory;
import com.openexchange.metrics.types.Counter;
import com.openexchange.metrics.types.Gauge;
import com.openexchange.metrics.types.Histogram;
import com.openexchange.metrics.types.Meter;
import com.openexchange.metrics.types.Timer;

/**
 * {@link DropwizardMetricServiceMBeanListener}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropwizardMetricServiceMBeanListener extends AbstractMetricServiceListener {

    /**
     * Initialises a new {@link DropwizardMetricServiceMBeanListener}.
     * 
     * @param managementService
     * @param mbeanFactory
     */
    public DropwizardMetricServiceMBeanListener(ManagementService managementService, MetricMBeanFactory mbeanFactory) {
        super(managementService, mbeanFactory);
    }

    @Override
    public void onGaugeAdded(MetricDescriptor descriptor, Gauge<?> gauge) {
        registerMBean(gauge, descriptor);
    }

    @Override
    public void onGaugeRemoved(MetricDescriptor descriptor) {
        unregisterMBean(descriptor);
    }

    @Override
    public void onCounterAdded(MetricDescriptor descriptor, Counter counter) {
        registerMBean(counter, descriptor);
    }

    @Override
    public void onCounterRemoved(MetricDescriptor descriptor) {
        unregisterMBean(descriptor);
    }

    @Override
    public void onHistogramAdded(MetricDescriptor descriptor, Histogram histogram) {
        registerMBean(histogram, descriptor);
    }

    @Override
    public void onHistogramRemoved(MetricDescriptor descriptor) {
        unregisterMBean(descriptor);
    }

    @Override
    public void onMeterAdded(MetricDescriptor descriptor, Meter meter) {
        registerMBean(meter, descriptor);
    }

    @Override
    public void onMeterRemoved(MetricDescriptor descriptor) {
        unregisterMBean(descriptor);
    }

    @Override
    public void onTimerAdded(MetricDescriptor descriptor, Timer timer) {
        registerMBean(timer, descriptor);
    }

    @Override
    public void onTimerRemoved(MetricDescriptor descriptor) {
        unregisterMBean(descriptor);
    }
}
