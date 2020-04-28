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
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.metrics.micrometer.internal.property.MicrometerFilterProperty;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;

/**
 * {@link AbstractMicrometerFilterPerformer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
abstract class AbstractMicrometerFilterPerformer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMicrometerFilterPerformer.class);

    private final MicrometerFilterProperty property;

    /**
     * Initializes a new {@link AbstractMicrometerFilterPerformer}.
     */
    AbstractMicrometerFilterPerformer(MicrometerFilterProperty property) {
        super();
        this.property = property;
    }

    void configure(MeterRegistry meterRegistry, Entry<String, String> entry) {
        meterRegistry.config().meterFilter(new MeterFilter() {

            @Override
            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                LOG.debug("Applying histogram meter filter for '{}'", id);
                String key = entry.getKey();
                String metricId = extractMetricId(key, property);
                String query = QueryMetricMicrometerFilterPerformer.queryRegistry.get(metricId);
                if (Strings.isEmpty(query)) {
                    return applyConfig(entry, metricId, config);
                }
                Query q = extractQuery(query);
                if (q == null) {
                    return config;
                }
                return matchTags(id, q) ? applyConfig(entry, metricId, config) : config;
            }
        });
    }

    /**
     * Applies the configuration of the specified entry to the specified {@link DistributionStatisticConfig}.
     * 
     * @param entry The entry with the configuration
     * @param metricId the metric identifier
     * @param config The {@link DistributionStatisticConfig}
     * @return The merged config
     */
    @SuppressWarnings("unused")
    DistributionStatisticConfig applyConfig(Entry<String, String> entry, String metricId, DistributionStatisticConfig config) {
        return config;
    }

    /**
     * Applies the MeterFilter by the specified meter filter consumer to the specified meter registry.
     * 
     * @param configurationService The configuration service to read the property's value
     * @param meterFilterConsumer The consumer which dictates the application of the meter filter
     */
    void applyFilterFor(ConfigurationService configurationService, Consumer<Entry<String, String>> meterFilterConsumer) {
        Map<String, String> properties = getPropertiesStartingWith(configurationService, property);
        properties.entrySet().parallelStream().forEach(entry -> meterFilterConsumer.accept(entry));
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
     * @param metricId The metric identifier
     * @param value The string value
     *
     * @return The Long value or <code>null</code> if the sanity check fails.
     */
    Long distributionValueSanityCheck(String metricId, String value) {
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
    void applyQuery(String query, MeterRegistry meterRegistry) {
        Query q = extractQuery(query);
        if (q == null) {
            return;
        }
        meterRegistry.config().meterFilter(MeterFilter.accept(p -> matchTags(p, q)));
    }

    ///////////////////////////////// HELPERS /////////////////////////////

    /**
     * Returns all properties that start with the specified prefix.
     *
     * @param configurationService The configuration service
     * @param property The prefix of the property
     * @return The found properties or an empty map
     */
    private Map<String, String> getPropertiesStartingWith(ConfigurationService configurationService, MicrometerFilterProperty property) {
        try {
            return configurationService.getProperties((name, value) -> name.startsWith(property.getFQPropertyName()));
        } catch (OXException e) {
            LOG.error("", e);
            return ImmutableMap.of();
        }
    }

    /**
     * Matches all tags from the specified {@link Id} with the tags specified
     * in the filter.
     *
     * @param id The id
     * @param query The {@link Query}
     * @return <code>true</code> if all tags match the filter; <code>false</code> otherwise
     */
    private boolean matchTags(Meter.Id id, Query query) {
        LOG.debug("Metric Tags: {}, Filter: {}", id.getTags(), query.getFilterMap());
        if (false == id.getName().equals(query.getMetricName())) {
            return false;
        }
        if (query.containsRegex()) {
            return applyRegex(id, query);
        }
        int matchCount = 0;
        for (Tag t : id.getTags()) {
            if (query.getFilterMap().containsKey(t.getKey()) && query.getFilterMap().get(t.getKey()).equals(t.getValue())) {
                matchCount++;
            }
        }
        return matchCount == query.getFilterMap().size();
    }

    /**
     * Applies the regex to the specified metric
     *
     * @param id The metric id
     * @param query The query
     * @return <code>true</code> if the regex matches at one of the tags; <code>false</code> otherwise
     */
    private boolean applyRegex(Meter.Id id, Query query) {
        for (Tag t : id.getTags()) {
            if (query.getFilterMap().containsKey(t.getKey())) {
                String regex = query.getFilterMap().get(t.getKey());
                return Pattern.compile(regex).matcher(t.getValue()).matches();
            }
        }
        return false;
    }

    /**
     * Extracts the query from the specified filter query string
     *
     * @param query The query string
     * @return The Query or <code>null</code> if no query can be extracted
     */
    private Query extractQuery(String query) {
        LOG.debug("Query: {}", query);
        int startIndex = query.indexOf("{") + 1;
        int endIndex = query.indexOf("}");
        if (startIndex < 0 || endIndex < 0 || endIndex < startIndex) {
            // Invalid indexes
            return null;
        }
        //Valid indexes, apply
        String metricName = query.substring(0, startIndex - 1);
        String filter = query.substring(startIndex, endIndex);
        LOG.debug("Metric name: {}, Filter: {}", metricName, filter);

        Map<String, String> map = new HashMap<>(4);
        List<String> list = Arrays.asList(Strings.splitByComma(filter));
        int elements = 0;
        for (String entry : list) {
            String[] split = Strings.splitBy(entry, '=', true);
            if (split.length != 2) {
                continue;
            }
            if (false == split[1].startsWith("~")) {
                map.put(split[0], split[1].replaceAll("\"", ""));
                elements++;
                continue;
            }
            if (elements == 0) {
                // If the regex is the first element we encounter
                // we extract it and ignore everything else
                map.put(split[0], split[1].substring(1).replaceAll("\"", ""));
                return new Query(metricName, map, true);
            }
            // Otherwise we ignore the regex in the middle
        }
        return new Query(metricName, map, false);
    }
}
