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

import com.openexchange.metrics.types.Metric;

/**
 * {@link MetricInformation}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MetricInformation {

    private final Metric metric;
    private final MetricDescriptor metricDescriptor;

    /**
     * Initialises a new {@link MetricInformation}.
     */
    public MetricInformation(final Metric metric, final MetricDescriptor metricDescriptor) {
        super();
        this.metric = metric;
        this.metricDescriptor = metricDescriptor;
    }

    /**
     * Gets the metricDescriptor
     *
     * @return The metricDescriptor
     */
    public MetricDescriptor getMetricDescriptor() {
        return metricDescriptor;
    }

    /**
     * Gets the metric
     *
     * @return The metric
     */
    public Metric getMetric() {
        return metric;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((metricDescriptor == null) ? 0 : metricDescriptor.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MetricInformation other = (MetricInformation) obj;
        if (metricDescriptor == null) {
            if (other.metricDescriptor != null) {
                return false;
            }
        } else if (!metricDescriptor.equals(other.metricDescriptor)) {
            return false;
        }
        return true;
    }
}
