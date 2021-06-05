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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.metrics.micrometer.internal.property.MicrometerFilterProperty;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;

/**
 * {@link ActivateMetricMicrometerFilterPerformer} - Applies metric filters for
 * properties <code>com.openexchange.metrics.micrometer.enable.*</code>
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class ActivateMetricMicrometerFilterPerformer extends AbstractMicrometerFilterPerformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivateMetricMicrometerFilterPerformer.class);

    /**
     * Initializes a new {@link ActivateMetricMicrometerFilterPerformer}.
     */
    public ActivateMetricMicrometerFilterPerformer() {
        super(MicrometerFilterProperty.ENABLE);
    }

    @Override
    public void applyFilter(MeterRegistry meterRegistry, ConfigurationService configurationService) {
        boolean enableAll = Boolean.parseBoolean(configurationService.getProperty(MicrometerFilterProperty.ENABLE.getFQPropertyName() + ".all", Boolean.TRUE.toString()));
        if (!enableAll) {
            selectiveWhitelist(meterRegistry, configurationService);
            return;
        }

        applyFilterFor(configurationService, (entry) -> {
            String key = entry.getKey();
            if (key.endsWith("all")) {
                return;
            }
            String metricId = extractMetricId(key, MicrometerFilterProperty.ENABLE);
            Filter filter = filterRegistryReference.get().get(metricId);
            boolean enabled = Boolean.parseBoolean(entry.getValue());
            if (filter == null) {
                LOGGER.debug("Applying enable/disable meter filter for '{}'", metricId);
                meterRegistry.config().meterFilter(enabled ? MeterFilter.acceptNameStartsWith(metricId) : MeterFilter.denyNameStartsWith(metricId));
                return;
            }
            applyFilter(filter, enabled, meterRegistry);
        });
    }

    /////////////////////////////////////////// HELPERS ////////////////////////////////////////////

    /**
     * Applies the 'denyUnless' filter
     *
     * @param meterRegistry The {@link MeterRegistry}
     * @param configurationService The {@link ConfigurationService}
     */
    private void selectiveWhitelist(MeterRegistry meterRegistry, ConfigurationService configurationService) {
        Map<String, String> enabledProperties = getPropertiesStartingWith(configurationService, MicrometerFilterProperty.ENABLE);
        Set<String> staticNames = new HashSet<>();
        Set<String> namesWithQuery = new HashSet<>();
        for (Entry<String, String> entry : enabledProperties.entrySet()) {
            String metricId = extractMetricId(entry.getKey(), MicrometerFilterProperty.ENABLE);
            if (null == filterRegistryReference.get().get(metricId)) {
                staticNames.add(metricId);
            } else {
                namesWithQuery.add(metricId);
            }
        }
        meterRegistry.config().meterFilter(MeterFilter.denyUnless(p -> {
            return accept(staticNames, namesWithQuery, p, enabledProperties);
        }));
    }

    /**
     * Applies the 'accept' filter if any of the static names or any of the queries
     * match the specified metric identifier or one of its tags
     * 
     * @param namesStatic Static metric names
     * @param namesQuery Custom filter names
     * @param id The metric identifier
     * @param props The properties
     * @return <code>true</code> if a match is found; <code>false</code> otherwise
     */
    private boolean accept(Set<String> namesStatic, Set<String> namesQuery, Id id, Map<String, String> props) {
        for (String name : namesStatic) {
            if (id.getName().startsWith(name)) {
                String value = props.get(MicrometerFilterProperty.ENABLE.getFQPropertyName() + "." + name);
                if (Boolean.parseBoolean(value)) {
                    return true;
                }
            }
        }
        for (String name : namesQuery) {
            String value = props.get(MicrometerFilterProperty.ENABLE.getFQPropertyName() + "." + name);
            Filter filter = filterRegistryReference.get().get(name);
            if (filter == null) {
                continue;
            }
            if (matchTags(id, filter) && Boolean.parseBoolean(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Applies the specified filter as filter
     *
     * @param filter The filter
     */
    private void applyFilter(Filter filter, boolean enabled, MeterRegistry meterRegistry) {
        if (filter == null) {
            return;
        }
        meterRegistry.config().meterFilter(enabled ? MeterFilter.accept(p -> matchTags(p, filter)) : MeterFilter.deny(p -> matchTags(p, filter)));
    }
}
