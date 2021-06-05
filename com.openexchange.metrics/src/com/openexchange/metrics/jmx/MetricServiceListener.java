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
import com.openexchange.metrics.types.Counter;
import com.openexchange.metrics.types.Gauge;
import com.openexchange.metrics.types.Histogram;
import com.openexchange.metrics.types.Meter;
import com.openexchange.metrics.types.Timer;

/**
 * {@link MetricServiceListener}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface MetricServiceListener {

    /**
     * Called when a {@link Gauge} is added to the registry.
     *
     * @param descriptor The {@link Gauge}'s descriptor
     * @param gauge the gauge
     */
    void onGaugeAdded(MetricDescriptor descriptor, Gauge<?> gauge);

    /**
     * Called when a {@link Gauge} is removed from the registry.
     *
     * @param name the gauge's name
     */
    void onGaugeRemoved(MetricDescriptor descriptor);

    /**
     * Called when a {@link Counter} is added to the registry.
     *
     * @param descriptor The {@link Counter}'s descriptor
     * @param counter the counter
     */
    void onCounterAdded(MetricDescriptor descriptor, Counter counter);

    /**
     * Called when a {@link Counter} is removed from the registry.
     *
     * @param name the counter's name
     */
    void onCounterRemoved(MetricDescriptor descriptor);

    /**
     * Called when a {@link Histogram} is added to the registry.
     *
     * @param descriptor The {@link Histogram}'s descriptor
     * @param histogram the histogram
     */
    void onHistogramAdded(MetricDescriptor descriptor, Histogram histogram);

    /**
     * Called when a {@link Histogram} is removed from the registry.
     *
     * @param name the histogram's name
     */
    void onHistogramRemoved(MetricDescriptor descriptor);

    /**
     * Called when a {@link Meter} is added to the registry.
     *
     * @param descriptor The {@link Meter}'s descriptor
     * @param meter the meter
     */
    void onMeterAdded(MetricDescriptor descriptor, Meter meter);

    /**
     * Called when a {@link Meter} is removed from the registry.
     *
     * @param name the meter's name
     */
    void onMeterRemoved(MetricDescriptor descriptor);

    /**
     * Called when a {@link Timer} is added to the registry.
     *
     * @param descriptor The {@link Timer}'s descriptor
     * @param timer the timer
     */
    void onTimerAdded(MetricDescriptor descriptor, Timer timer);

    /**
     * Called when a {@link Timer} is removed from the registry.
     *
     * @param name the timer's name
     */
    void onTimerRemoved(MetricDescriptor descriptor);
}
