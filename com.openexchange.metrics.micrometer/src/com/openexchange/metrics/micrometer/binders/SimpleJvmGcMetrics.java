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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.prometheus.client.hotspot.GarbageCollectorExports;


/**
 * This resembles the  exported metrics of {@link GarbageCollectorExports} to provide
 * a more light-weight and reliable result than {@link JvmGcMetrics}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class SimpleJvmGcMetrics implements MeterBinder {

    private final List<GarbageCollectorMXBean> garbageCollectors;

    public SimpleJvmGcMetrics() {
        this(ManagementFactory.getGarbageCollectorMXBeans());
    }

    SimpleJvmGcMetrics(List<GarbageCollectorMXBean> garbageCollectors) {
        this.garbageCollectors = garbageCollectors;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        for (final GarbageCollectorMXBean gaCollectorMXBean : garbageCollectors) {
            Tags tags = Tags.of("gc", gaCollectorMXBean.getName());
            FunctionTimer.builder("jvm.gc.collection",
                gaCollectorMXBean,
                    (gc) -> gc.getCollectionCount(),
                    (gc) -> gc.getCollectionTime(), TimeUnit.MILLISECONDS)
                .description("Time spent in a given JVM garbage collector in seconds.")
                .tags(tags)
                .register(registry);
        }
    }
}
