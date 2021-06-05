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

import com.openexchange.metrics.jmx.MetricServiceListener;
import com.openexchange.metrics.types.Metric;

/**
 * {@link MetricServiceListenerNotifier}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface MetricServiceListenerNotifier {

    /**
     * Notifies the specified {@link MetricServiceListener} about the specified {@link Metric}
     * described by the specified {@link MetricDescriptor}
     * 
     * @param listener The {@link MetricServiceListener} to notify
     * @param metric The {@link Metric} to be notified
     * @param metricDescriptor The {@link MetricDescriptor}
     */
    void notify(MetricServiceListener listener, Metric metric, MetricDescriptor metricDescriptor);
}
