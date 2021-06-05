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

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.metrics.micrometer.internal.property.MicrometerFilterProperty;
import com.openexchange.tools.strings.TimeSpanParser;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;

/**
 * {@link DistributionSLOMicrometerFilterPerformer} - Applies metric filters for
 * properties <code>com.openexchange.metrics.micrometer.distribution.slo.*</code>
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class DistributionSLOMicrometerFilterPerformer extends AbstractMicrometerFilterPerformer {

    private static final Logger LOG = LoggerFactory.getLogger(DistributionSLOMicrometerFilterPerformer.class);

    /**
     * Initializes a new {@link DistributionSLOMicrometerFilterPerformer}.
     */
    public DistributionSLOMicrometerFilterPerformer() {
        super(MicrometerFilterProperty.SLO);
    }

    @Override
    public void applyFilter(MeterRegistry meterRegistry, ConfigurationService configurationService) {
        applyFilterFor(configurationService, (entry) -> configure(meterRegistry, entry));
    }

    @Override
    DistributionStatisticConfig applyConfig(Id id, Entry<String, String> entry, String metricId, DistributionStatisticConfig config) {
        if (!id.getName().startsWith(metricId)) {
            return config;
        }
        if (Strings.isEmpty(entry.getValue())) {
            return DistributionStatisticConfig.builder().serviceLevelObjectives(new double[0]).build().merge(config);
        }
        String[] p = Strings.splitByComma(entry.getValue());
        double[] slo = new double[p.length];
        int index = 0;
        for (String s : p) {
            try {
                slo[index++] = TimeUnit.MILLISECONDS.toNanos(TimeSpanParser.parseTimespanToPrimitive(s));
            } catch (IllegalArgumentException e) {
                LOG.error("Cannot parse {} as long. Ignoring SLAs configuration for '{}'.", s, metricId, e);
                return config;
            }
        }
        return DistributionStatisticConfig.builder().serviceLevelObjectives(slo).build().merge(config);
    }
}
