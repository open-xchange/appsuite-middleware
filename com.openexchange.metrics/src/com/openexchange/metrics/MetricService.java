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

package com.openexchange.metrics;

import java.util.function.Supplier;
import com.openexchange.metrics.jmx.MetricServiceListener;
import com.openexchange.metrics.types.Counter;
import com.openexchange.metrics.types.Gauge;
import com.openexchange.metrics.types.Histogram;
import com.openexchange.metrics.types.Meter;
import com.openexchange.metrics.types.Metric;
import com.openexchange.metrics.types.Timer;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link MetricService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @deprecated Use meters from io.micrometer.core.instrument instead
 */
@SingletonService
@Deprecated
public interface MetricService {

    /**
     * Gets an existing {@link Histogram} for the specified {@link MetricDescriptor} or creates and
     * remembers a new one if it doesn't exist yet.
     *
     * @param descriptior The {@link MetricDescriptor}
     * @return the metric instance
     */
    Histogram getHistogram(MetricDescriptor descriptor);

    /**
     * Gets an existing {@link Timer} for the specified {@link MetricDescriptor} or creates and
     * remembers a new one if it doesn't exist yet.
     *
     * @param descriptior The {@link MetricDescriptor}
     * @return the metric instance
     */
    Timer getTimer(MetricDescriptor descriptor);

    /**
     * Gets an existing {@link Counter} for the specified {@link MetricDescriptor} or creates and
     * remembers a new one if it doesn't exist yet.
     *
     * @param descriptior The {@link MetricDescriptor}
     * @return the metric instance
     */
    Counter getCounter(MetricDescriptor descriptor);

    /**
     * Gets an existing {@link Gauge} for the specified {@link MetricDescriptor} with the specified {@link Supplier}
     * or creates and remembers a new one if it doesn't exist yet.
     *
     * @param descriptior The {@link MetricDescriptor}
     * @return the metric instance
     */
    <T> Gauge<T> getGauge(MetricDescriptor descriptor);

    /**
     * Gets an existing {@link Meter} for the specified {@link MetricDescriptor} or creates and
     * remembers a new one if it doesn't exist yet.
     *
     * @param descriptior The {@link MetricDescriptor}
     * @return the metric instance
     */
    Meter getMeter(MetricDescriptor descriptor);

    /**
     * Removes the {@link Metric} defined with the specified {@link MetricDescriptor}
     *
     * @param descriptor The {@link MetricDescriptor}
     */
    void removeMetric(MetricDescriptor descriptor);

    /**
     * Adds a {@link MetricServiceListener} to a collection of listeners that will be notified on
     * metric creation. Listeners will be notified in the order in which they are added.
     * <p/>
     * <b>N.B.:</b> The listener will be notified of all existing metrics when it first registers.
     *
     * @param listener the listener that will be notified
     */
    void addListener(MetricServiceListener listener);

    /**
     * Removes a {@link MetricServiceListener} from this registry's collection of listeners.
     *
     * @param listener the listener that will be removed
     */
    void removeListener(MetricServiceListener listener);
}
