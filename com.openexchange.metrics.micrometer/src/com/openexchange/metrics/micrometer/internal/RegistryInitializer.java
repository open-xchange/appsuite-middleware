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

package com.openexchange.metrics.micrometer.internal;

import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.metrics.micrometer.binders.JvmInfoMetrics;
import com.openexchange.metrics.micrometer.binders.SimpleJvmGcMetrics;
import com.openexchange.metrics.micrometer.internal.filter.ActivateMetricMicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.DistributionHistogramMicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.DistributionMaximumMicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.DistributionMinimumMicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.DistributionPercentilesMicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.DistributionSLOMicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.FilterMetricMicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.MicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.RenameCacheMetricsFilter;
import com.openexchange.metrics.micrometer.internal.property.MicrometerFilterProperty;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * {@link RegistryInitializer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class RegistryInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(RegistryInitializer.class);

    private final CompositeMeterRegistry parentRegistry;
    private final List<MicrometerFilterPerformer> filterPerformers;
    private volatile PrometheusMeterRegistry prometheusRegistry;

    /**
     * Initializes a new {@link RegistryInitializer}.
     *
     * @param parentRegistry The parent {@link CompositeMeterRegistry} to operate on
     */
    public RegistryInitializer(CompositeMeterRegistry parentRegistry) {
        super();
        this.parentRegistry = parentRegistry;
        filterPerformers = new LinkedList<>();
    }

    /**
     * Initializes the actual {@link PrometheusMeterRegistry} and adds some {@link MeterFilter}s based
     * on configuration. It then registers the prometheus registry at the parent composite registry and
     * returns the new instance.
     * <p>
     * Any previously initialized prometheus registry is removed from the parent and orderly closed. This
     * method is safe to be called subsequently as it resets its internal state on every call.
     * <p>
     * This method is thread safe.
     *
     * @param configService The recent {@link ConfigurationService} instance
     * @return The new prometheus registry
     */
    public synchronized PrometheusMeterRegistry initialize(ConfigurationService configService) {
        reset();
        initFilterPerformers();

        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        // rename built-in io.micrometer.core.instrument.binder.cache metrics  to carry the "appsuite." prefix
        prometheusRegistry.config().meterFilter(new RenameCacheMetricsFilter());

        // set configuration-based filters
        filterPerformers.stream().forEach(p -> p.applyFilter(prometheusRegistry, configService));
        addEnableAllFilter(configService, prometheusRegistry);

        parentRegistry.add(prometheusRegistry);

        // add JVM stats
        new JvmMemoryMetrics().bindTo(prometheusRegistry);
        new JvmThreadMetrics().bindTo(prometheusRegistry);
        new ClassLoaderMetrics().bindTo(prometheusRegistry);
        new UptimeMetrics().bindTo(prometheusRegistry);
        new FileDescriptorMetrics().bindTo(prometheusRegistry);
        new SimpleJvmGcMetrics().bindTo(prometheusRegistry);
        new JvmInfoMetrics().bindTo(prometheusRegistry);

        this.prometheusRegistry = prometheusRegistry;
        return prometheusRegistry;
    }

    /**
     * Any previously initialized prometheus registry is removed from the parent and orderly closed. This
     * method is safe to be called subsequently as it resets its internal state on every call.
     * <p>
     * This method is thread safe.
     */
    public synchronized void reset() {
        filterPerformers.clear();
        PrometheusMeterRegistry prometheusRegistry = this.prometheusRegistry;
        if (prometheusRegistry != null) {
            this.prometheusRegistry = null;
            parentRegistry.remove(prometheusRegistry);
            prometheusRegistry.close();
        }
    }

    /*
     * Visible for testing
     */
    synchronized void initFilterPerformers() {
        filterPerformers.add(new FilterMetricMicrometerFilterPerformer());
        filterPerformers.add(new ActivateMetricMicrometerFilterPerformer());
        filterPerformers.add(new DistributionHistogramMicrometerFilterPerformer());
        filterPerformers.add(new DistributionMinimumMicrometerFilterPerformer());
        filterPerformers.add(new DistributionMaximumMicrometerFilterPerformer());
        filterPerformers.add(new DistributionPercentilesMicrometerFilterPerformer());
        filterPerformers.add(new DistributionSLOMicrometerFilterPerformer());
    }

    /**
     * Checks if all metrics are enabled, and if not apply the default deny policy.
     *
     * @param configService The configuration service
     */
    /*
     * Visible for testing
     */
    synchronized void addEnableAllFilter(ConfigurationService configService, PrometheusMeterRegistry prometheusRegistry) {
        boolean enableAll = Boolean.parseBoolean(configService.getProperty(MicrometerFilterProperty.ENABLE.getFQPropertyName() + ".all", Boolean.TRUE.toString()));
        if (enableAll) {
            return;
        }
        try {
            int enabledCount = configService.getProperties((name, v) -> name.startsWith(MicrometerFilterProperty.ENABLE.getFQPropertyName())).size();
            if (enabledCount == 1) {
                prometheusRegistry.config().meterFilter(MeterFilter.deny());
            }
        } catch (OXException e) {
            LOG.error("Cannot apply 'deny.all' filter.", e);
        }
    }

}
