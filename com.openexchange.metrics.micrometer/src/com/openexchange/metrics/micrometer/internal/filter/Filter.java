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

package com.openexchange.metrics.micrometer.internal.filter;

import java.util.Map;

/**
 * {@link Filter}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
class Filter {

    private final String metricName;
    private final Map<String, Condition> conditionMap;

    /**
     * Initializes a new {@link Filter}.
     *
     * @param metricName The metric name
     * @param conditionMap The map with all the conditions for this filter
     */
    public Filter(String metricName, Map<String, Condition> conditionMap) {
        super();
        this.metricName = metricName;
        this.conditionMap = conditionMap;
    }

    /**
     * Gets the name of the metric this query applies to
     *
     * @return The name of the metric
     */
    public String getMetricName() {
        return metricName;
    }

    /**
     * Gets a map of all conditions of this filter
     *
     * @return A map of all conditions of this filter
     */
    public Map<String, Condition> getConditions() {
        return conditionMap;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Query [metricName=").append(metricName).append(", conditionMap=").append(conditionMap).append("]");
        return builder.toString();
    }
}
