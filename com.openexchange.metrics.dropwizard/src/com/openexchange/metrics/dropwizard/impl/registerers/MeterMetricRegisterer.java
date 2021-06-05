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

package com.openexchange.metrics.dropwizard.impl.registerers;

import com.codahale.metrics.MetricRegistry;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.MetricRegisterer;
import com.openexchange.metrics.dropwizard.types.DropwizardMeter;
import com.openexchange.metrics.types.Metric;

/**
 * {@link MeterMetricRegisterer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MeterMetricRegisterer implements MetricRegisterer {

    private final MetricRegistry registry;

    /**
     * Initialises a new {@link MeterMetricRegisterer}.
     */
    public MeterMetricRegisterer(MetricRegistry registry) {
        super();
        this.registry = registry;
    }

    @Override
    public Metric register(MetricDescriptor descriptor) {
        return new DropwizardMeter(registry.meter(getNameFor(descriptor)));
    }

    @Override
    public void unregister(MetricDescriptor descriptor) {
        registry.remove(getNameFor(descriptor));
    }

}
