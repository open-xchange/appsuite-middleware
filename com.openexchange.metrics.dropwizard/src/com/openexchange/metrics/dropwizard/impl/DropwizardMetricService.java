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

package com.openexchange.metrics.dropwizard.impl;

import com.codahale.metrics.MetricRegistry;
import com.openexchange.metrics.AbstractMetricService;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.MetricType;
import com.openexchange.metrics.dropwizard.impl.registerers.CounterMetricRegisterer;
import com.openexchange.metrics.dropwizard.impl.registerers.GaugeMetricRegisterer;
import com.openexchange.metrics.dropwizard.impl.registerers.HistogramMetricRegisterer;
import com.openexchange.metrics.dropwizard.impl.registerers.MeterMetricRegisterer;
import com.openexchange.metrics.dropwizard.impl.registerers.TimerMetricRegisterer;
import com.openexchange.metrics.types.Counter;
import com.openexchange.metrics.types.Gauge;
import com.openexchange.metrics.types.Histogram;
import com.openexchange.metrics.types.Meter;
import com.openexchange.metrics.types.Timer;

/**
 * {@link DropwizardMetricService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropwizardMetricService extends AbstractMetricService {

    private final MetricRegistry registry;

    /**
     * Initialises a new {@link DropwizardMetricService}.
     */
    public DropwizardMetricService() {
        super();
        registry = new MetricRegistry();

        addRegisterer(MetricType.METER, new MeterMetricRegisterer(registry));
        addRegisterer(MetricType.TIMER, new TimerMetricRegisterer(registry));
        addRegisterer(MetricType.COUNTER, new CounterMetricRegisterer(registry));
        addRegisterer(MetricType.HISTOGRAM, new HistogramMetricRegisterer(registry));
        addRegisterer(MetricType.GAUGE, new GaugeMetricRegisterer(registry));
    }

    @Override
    public Histogram getHistogram(MetricDescriptor descriptor) {
        return (Histogram) registerOrGet(descriptor);
    }

    @Override
    public Timer getTimer(MetricDescriptor descriptor) {
        return (Timer) registerOrGet(descriptor);
    }

    @Override
    public Counter getCounter(MetricDescriptor descriptor) {
        return (Counter) registerOrGet(descriptor);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Gauge<T> getGauge(MetricDescriptor descriptor) {
        return (Gauge<T>) registerOrGet(descriptor);
    }

    @Override
    public Meter getMeter(MetricDescriptor descriptor) {
        return (Meter) registerOrGet(descriptor);
    }

    @Override
    public void removeMetric(MetricDescriptor descriptor) {
        remove(descriptor);
    }
}
