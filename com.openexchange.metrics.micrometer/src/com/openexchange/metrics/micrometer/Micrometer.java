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

package com.openexchange.metrics.micrometer;

import java.util.function.ToDoubleFunction;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.lang.Nullable;

/**
 * Provides common utility methods for Micrometer metrics.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class Micrometer {

    /**
     * Registers and returns a Gauge with given arguments at the given registry. If a Gauge with equal name and tags already exists,
     * it is removed from the registry first. Effectively an existing Gauge gets overridden within the registry. This is desired in
     * cases, where the observed instance {@code obj} gets replaced during application life time.
     *
     * @param registry The registry
     * @param name The name
     * @param tags The tags
     * @param description The description
     * @param baseUnit The base unit
     * @param obj The source object
     * @param f The value conversion function
     * @return The newly registered Gauge
     */
    public static <T> Gauge registerOrUpdateGauge(MeterRegistry registry, String name, Tags tags, @Nullable String description, @Nullable String baseUnit, @Nullable T obj, ToDoubleFunction<T> f) {
        Gauge oldGauge = registry.find(name).tags(tags).gauge();
        if (oldGauge != null) {
            registry.remove(oldGauge);
        }
        return Gauge.builder(name, obj, f)
            .description(description)
            .tags(tags)
            .baseUnit(baseUnit)
            .register(registry);
    }

}
