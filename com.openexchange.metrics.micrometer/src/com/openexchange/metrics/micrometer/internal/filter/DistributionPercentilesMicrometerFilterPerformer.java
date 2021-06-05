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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.metrics.micrometer.internal.property.MicrometerFilterProperty;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;

/**
 * {@link DistributionPercentilesMicrometerFilterPerformer} - Applies metric filters for
 * properties <code>com.openexchange.metrics.micrometer.distribution.percentiles.*</code>
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class DistributionPercentilesMicrometerFilterPerformer extends AbstractMicrometerFilterPerformer {

    private static final Logger LOG = LoggerFactory.getLogger(DistributionPercentilesMicrometerFilterPerformer.class);

    /**
     * Initializes a new {@link DistributionPercentilesMicrometerFilterPerformer}.
     */
    public DistributionPercentilesMicrometerFilterPerformer() {
        super(MicrometerFilterProperty.PERCENTILES);
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
            return DistributionStatisticConfig.builder().percentiles(new double[0]).build().merge(config);
        }

        String[] p = Strings.splitByComma(entry.getValue());
        double[] percentiles = new double[p.length];
        int index = 0;
        for (String s : p) {
            try {
                double value = Double.parseDouble(s);
                if (value < 0 || value > 1) {
                    LOG.error("Invalid percentile '{}' for '{}'. Only values between 0 and 1 are allowed.", Double.valueOf(value), metricId);
                    return config;
                }
                percentiles[index++] = value;
            } catch (NumberFormatException e) {
                LOG.error("Percentile '{}' cannot be parsed as double. Ignoring percentiles configuration.", s, e);
                return config;
            }
        }
        return DistributionStatisticConfig.builder().percentiles(percentiles).build().merge(config);
    }
}
