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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.metrics.micrometer.internal.property.MicrometerFilterProperty;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;

/**
 * {@link AbstractMicrometerFilterPerformer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
abstract class AbstractMicrometerFilterPerformer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMicrometerFilterPerformer.class);

    /**
     * Initializes a new {@link AbstractMicrometerFilterPerformer}.
     */
    AbstractMicrometerFilterPerformer() {
        super();
    }

    /**
     * Applies the MeterFilter by the specified meter filter consumer to the specified meter registry.
     *
     * @param property The property
     * @param configurationService The configuration service to read the property's value
     * @param meterFilterConsumer The consumer which dictates the application of the meter filter
     */
    void applyFilterFor(MicrometerFilterProperty property, ConfigurationService configurationService, Consumer<Entry<String, String>> meterFilterConsumer) {
        Map<String, String> properties = getPropertiesStartingWith(configurationService, property);
        properties.entrySet().parallelStream().forEach(entry -> meterFilterConsumer.accept(entry));
    }

    /**
     * Returns all properties that start with the specified prefix.
     *
     * @param configurationService The configuration service
     * @param property The prefix of the property
     * @return The found properties or an empty map
     */
    Map<String, String> getPropertiesStartingWith(ConfigurationService configurationService, MicrometerFilterProperty property) {
        try {
            return configurationService.getProperties((name, value) -> name.startsWith(property.getFQPropertyName()));
        } catch (OXException e) {
            LOG.error("", e);
            return ImmutableMap.of();
        }
    }

    /**
     * Extracts the metric id from the specified propertyName
     * by using the property as a base.
     *
     * @param propertyName The property name
     * @param property The base
     * @return The metric id
     */
    String extractMetricId(String propertyName, MicrometerFilterProperty property) {
        String prop = property.name().toLowerCase();
        return propertyName.substring(propertyName.indexOf(prop) + prop.length() + 1);
    }

    /**
     * Performs a sanity check and returns the specified value as Long for
     * the specified metric.
     * If the sanity check fails, i.e. if the value cannot be parsed or is negative then <code>null</code> will be returned.
     *
     * @param property The property
     * @param metricId The metric identifier
     * @param value The string value
     * @return The Long value or <code>null</code> if the sanity check fails.
     */
    Long distributionValueSanityCheck(MicrometerFilterProperty property, String metricId, String value) {
        try {
            long candidate = Long.parseLong(value);
            if (candidate >= 0) {
                return Long.valueOf(candidate);
            }
            LOG.error("Negative values for the {} bound of a distribution is not allowed. Metric: '{}'", property.name().toLowerCase(), metricId);
            return null;
        } catch (NumberFormatException e) {
            LOG.error("Invalid value was specified for the {} bound of the {} distribution.", property.name().toLowerCase(), metricId);
            return null;
        }
    }

    /**
     * Applies the specified query as filter
     * 
     * @param query The query
     */
    void applyRegex(String query, MeterRegistry meterRegistry) {
        LOG.debug("Query: {}", query);
        int startIndex = query.indexOf("{") + 1;
        int endIndex = query.indexOf("}");
        if (startIndex < 0 || endIndex < 0 || endIndex < startIndex) {
            // Invalid indexes
            return;
        }
        //Valid indexes, apply
        String metricName = query.substring(0, startIndex - 1);
        String filter = query.substring(startIndex, endIndex);
        LOG.debug("Metric name: {}, Filter: {}", metricName, filter);
        Map<String, String> filterMap = extractFilter(filter);

        meterRegistry.config().meterFilter(MeterFilter.accept(p -> {
            List<Tag> tags = p.getTags();
            if (false == p.getName().equals(metricName)) {
                return false;
            }
            LOG.debug("Metric Tags: {}, Filter: {}", tags, filterMap);
            int matchCount = 0;
            for (Tag t : tags) {
                if (filterMap.containsKey(t.getKey()) && filterMap.get(t.getKey()).equals(t.getValue())) {
                    matchCount++;
                }
            }
            return matchCount == filterMap.size();
        }));
    }

    /**
     * Extracts the filter map from the specified filter string
     *
     * @param filter The filter string
     * @return The filter map
     */
    private Map<String, String> extractFilter(String filter) {
        Map<String, String> map = new HashMap<>();
        List<String> list = Arrays.asList(Strings.splitByComma(filter));
        for (String entry : list) {
            String[] split = Strings.splitBy(entry, '=', true);
            if (split.length == 2) {
                map.put(split[0], split[1].replaceAll("\"", ""));
            }
        }
        return map;
    }
}
