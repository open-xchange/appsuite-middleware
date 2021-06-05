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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * {@link MetricDescriptorCache}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.10.1
 */
@SuppressWarnings("deprecation")
public class MetricDescriptorCache {

    private final ConcurrentMap<String, MetricDescriptor> metricDescriptors;
    private final MetricService metricService;
    private final String group;

    /**
     * Initialises a new {@link MetricDescriptorCache}.
     *
     * @param metricService The instance of the {@link MetricService}
     * @param group The name of the group for with the metric descriptor cache shall be initialised.
     */
    public MetricDescriptorCache(MetricService metricService, String group) {
        super();
        this.metricService = metricService;
        this.group = group;
        metricDescriptors = new ConcurrentHashMap<>(8);
    }

    /**
     * Retrieves the metric descriptor for the metric type
     *
     * @param metricType The {@link MetricType}
     * @param name the method name
     * @param description The metric's description
     * @param unit The metric's unit
     * @return The {@link MetricDescriptor} for the specified metric
     */
    public MetricDescriptor getMetricDescriptor(MetricType metricType, String name, String description, String unit) {
        return getMetricDescriptor(metricType, name, description, unit, TimeUnit.SECONDS);
    }

    /**
     * Retrieves the metric descriptor for the metric type
     *
     * @param metricType The {@link MetricType}
     * @param name the method name
     * @param description The metric's description
     * @param unit The metric's unit
     * @param rate The rate unit
     * @return The {@link MetricDescriptor} for the specified metric
     */
    public MetricDescriptor getMetricDescriptor(MetricType metricType, String name, String description, String unit, TimeUnit rate) {
        MetricDescriptor metricDescriptor = metricDescriptors.get(name);
        if (metricDescriptor != null) {
            return metricDescriptor;
        }

        metricDescriptor = MetricDescriptor.newBuilder(group, name, metricType).withUnit(unit).withRate(rate).withDescription(String.format(description, name)).build();
        MetricDescriptor raced = metricDescriptors.putIfAbsent(name, metricDescriptor);
        if (raced == null) {
            return metricDescriptor;
        }
        return raced;
    }

    /**
     * Unregisters all metrics and clears the cache
     */
    public void clear() {
        metricDescriptors.clear();
        MetricService metricService = this.metricService;
        if (metricService == null) {
            return;
        }
        for (MetricDescriptor metricDescriptor : metricDescriptors.values()) {
            metricService.removeMetric(metricDescriptor);
        }
    }
}
