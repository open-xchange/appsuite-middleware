/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import com.openexchange.java.Strings;
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
            String query = filterRegistryReference.get().get(metricId);
            boolean enabled = Boolean.parseBoolean(entry.getValue());
            if (Strings.isEmpty(query)) {
                LOGGER.debug("Applying enable/disable meter filter for '{}'", metricId);
                meterRegistry.config().meterFilter(enabled ? MeterFilter.acceptNameStartsWith(metricId) : MeterFilter.denyNameStartsWith(metricId));
                return;
            }
            applyFilter(query, enabled, meterRegistry);
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
            if (Strings.isEmpty(filterRegistryReference.get().get(metricId))) {
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
            Filter filter = extractFilter(filterRegistryReference.get().get(name));
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
    private void applyFilter(String filter, boolean enabled, MeterRegistry meterRegistry) {
        Filter q = extractFilter(filter);
        if (q == null) {
            return;
        }
        meterRegistry.config().meterFilter(enabled ? MeterFilter.accept(p -> matchTags(p, q)) : MeterFilter.deny(p -> matchTags(p, q)));
    }
}
