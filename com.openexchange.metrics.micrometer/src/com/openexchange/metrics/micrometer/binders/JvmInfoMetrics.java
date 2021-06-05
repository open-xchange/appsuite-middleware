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

package com.openexchange.metrics.micrometer.binders;

import static com.openexchange.java.Autoboxing.D;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.prometheus.client.hotspot.VersionInfoExports;

/**
 * This resembles {@link VersionInfoExports}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class JvmInfoMetrics implements MeterBinder {

    @Override
    public void bindTo(MeterRegistry registry) {

        Gauge.builder("jvm.info", () -> D(1.0))
            .description("JVM version info")
            .tags(
                "version", System.getProperty("java.runtime.version", "unknown"),
                "vendor", System.getProperty("java.vm.vendor", "unknown"),
                "runtime", System.getProperty("java.runtime.name", "unknown"))
            .register(registry);
    }

}
