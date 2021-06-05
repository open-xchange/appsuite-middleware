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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.metrics.micrometer.internal.property.MicrometerFilterProperty;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * {@link FilterMetricMicrometerFilterPerformer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class FilterMetricMicrometerFilterPerformer extends AbstractMicrometerFilterPerformer {

    private static final Logger LOG = LoggerFactory.getLogger(FilterMetricMicrometerFilterPerformer.class);

    /**
     * Initializes a new {@link FilterMetricMicrometerFilterPerformer}.
     */
    public FilterMetricMicrometerFilterPerformer() {
        super(MicrometerFilterProperty.FILTER);
    }

    @Override
    public void applyFilter(MeterRegistry meterRegistry, ConfigurationService configurationService) {
        final ImmutableMap.Builder<String, Filter> filterRegistry = ImmutableMap.builder();
        applyFilterFor(configurationService, (entry) -> {
            filterRegistry.put(extractMetricId(entry.getKey(), MicrometerFilterProperty.FILTER), extractFilter(entry.getValue()));
        });
        filterRegistryReference.set(filterRegistry.build());
    }

    /**
     * Extracts the filter from the specified filter string
     *
     * @param filter The filter string
     * @return The Filter or <code>null</code> if no filter can be extracted
     */
    private Filter extractFilter(String filter) {
        if (Strings.isEmpty(filter)) {
            return null;
        }
        LOG.trace("Extracting filter: {}", filter);
        int startIndex = filter.indexOf("{") + 1;
        int endIndex = filter.indexOf("}");
        if (startIndex < 0 || endIndex < 0 || endIndex < startIndex) {
            LOG.error("Invalid filter detected: {}", filter);
            return null;
        }
        //Valid indexes, apply
        String metricName = filter.substring(0, startIndex - 1);
        String condition = filter.substring(startIndex, endIndex);
        LOG.trace("Extracted --> Metric name: {}, Filter: {}", metricName, condition);

        Map<String, Condition> map = new HashMap<>(4);
        List<String> filterList = Arrays.asList(Strings.splitByComma(condition));
        for (String entry : filterList) {
            // Negated regex
            if (entry.contains("!~")) {
                String[] s = entry.split("!~");
                if (s.length == 2) {
                    map.put(s[0], new Condition(s[1].replaceAll("\"", ""), true, true));
                    continue;
                }
            }
            // Negated exact match
            if (entry.contains("!=")) {
                String[] s = entry.split("!=");
                if (s.length == 2) {
                    map.put(s[0], new Condition(s[1].replaceAll("\"", ""), false, true));
                    continue;
                }
            }
            String[] split = Strings.splitBy(entry, '=', true);
            if (split.length != 2) {
                continue;
            }
            // Regex
            if (false == split[1].startsWith("~")) {
                map.put(split[0], new Condition(split[1].replaceAll("\"", ""), false, false));
                continue;
            }
            // Exact match
            map.put(split[0], new Condition(split[1].substring(1).replaceAll("\"", ""), true, false));
        }
        return new Filter(metricName, map);
    }
}
