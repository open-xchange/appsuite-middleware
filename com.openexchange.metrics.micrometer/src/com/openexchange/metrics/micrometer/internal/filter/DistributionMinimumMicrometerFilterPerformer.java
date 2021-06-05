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
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.metrics.micrometer.internal.property.MicrometerFilterProperty;
import com.openexchange.tools.strings.TimeSpanParser;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;

/**
 * {@link DistributionMinimumMicrometerFilterPerformer} - Applies metric filters for
 * properties <code>com.openexchange.metrics.micrometer.distribution.minimum.*</code>
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class DistributionMinimumMicrometerFilterPerformer extends AbstractMicrometerFilterPerformer {

    /**
     * Initializes a new {@link DistributionMinimumMicrometerFilterPerformer}.
     */
    public DistributionMinimumMicrometerFilterPerformer() {
        super(MicrometerFilterProperty.MINIMUM);
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

        String value = entry.getValue();
        if (Strings.isEmpty(value)) {
            return config;
        }

        long nanos = TimeUnit.MILLISECONDS.toNanos(TimeSpanParser.parseTimespanToPrimitive(value));
        return DistributionStatisticConfig.builder().minimumExpectedValue(Double.valueOf(nanos)).build().merge(config);

    }
}
