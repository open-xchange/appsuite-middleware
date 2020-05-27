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

package com.openexchange.metrics.micrometer.internal;

import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.metrics.micrometer.binders.SimpleJvmGcMetrics;
import com.openexchange.metrics.micrometer.internal.filter.ActivateMetricMicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.DistributionHistogramMicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.DistributionMaximumMicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.DistributionMinimumMicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.DistributionPercentilesMicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.DistributionSLAMicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.FilterMetricMicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.MicrometerFilterPerformer;
import com.openexchange.metrics.micrometer.internal.filter.RenameCacheMetricsFilter;
import com.openexchange.metrics.micrometer.internal.filter.RenameExecutorMetricsFilter;
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

        // rename built-in io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics for the
        // ThreadPoolService executor to carry the "appsuite." prefix
        prometheusRegistry.config().meterFilter(new RenameExecutorMetricsFilter());

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
        filterPerformers.add(new DistributionSLAMicrometerFilterPerformer());
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
