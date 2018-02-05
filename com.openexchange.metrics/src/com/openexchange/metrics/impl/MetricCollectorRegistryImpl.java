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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.metrics.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistry.MetricSupplier;
import com.codahale.metrics.SharedMetricRegistries;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;
import com.openexchange.metrics.AbstractMetricCollector;
import com.openexchange.metrics.MetricCollector;
import com.openexchange.metrics.MetricCollectorRegistry;
import com.openexchange.metrics.MetricMetadata;
import com.openexchange.metrics.MetricType;
import com.openexchange.metrics.MetricTypeRegisterer;
import com.openexchange.metrics.jmx.MetricMBean;
import com.openexchange.metrics.jmx.MetricMBeanFactory;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MetricCollectorRegistryImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MetricCollectorRegistryImpl implements MetricCollectorRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(MetricCollectorRegistryImpl.class);

    private static final String DOMAIN_NAME = "com.openexchange.metrics";
    private final Map<MetricType, MetricTypeRegisterer> registerers;
    private final ConcurrentMap<String, MetricCollector> collectors;
    private final Map<MetricType, BiFunction<Metric, MetricMetadata, MetricMBean>> mbeanCreators;
    private ServiceLookup services;

    /**
     * Initialises a new {@link MetricCollectorRegistryImpl}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MetricCollectorRegistryImpl(ServiceLookup services) {
        super();
        this.services = services;
        collectors = new ConcurrentHashMap<>();

        Map<MetricType, MetricTypeRegisterer> r = new HashMap<>();
        r.put(MetricType.COUNTER, (componentName, metricMetadata, metricRegistry) -> metricRegistry.counter(metricMetadata.getMetricName()));
        r.put(MetricType.TIMER, (componentName, metricMetadata, metricRegistry) -> metricRegistry.timer(metricMetadata.getMetricName()));
        r.put(MetricType.METER, (componentName, metricMetadata, metricRegistry) -> metricRegistry.meter(metricMetadata.getMetricName()));
        r.put(MetricType.HISTOGRAM, (componentName, metricMetadata, metricRegistry) -> metricRegistry.histogram(metricMetadata.getMetricName()));
        r.put(MetricType.GAUGE, (componentName, metricMetadata, metricRegistry) -> metricRegistry.gauge(metricMetadata.getMetricName(), (MetricSupplier<Gauge>) metricMetadata.getMetricSupplier()));
        registerers = Collections.unmodifiableMap(r);

        Map<MetricType, BiFunction<Metric, MetricMetadata, MetricMBean>> c = new HashMap<>();
        c.put(MetricType.COUNTER, (metric, metricMetadata) -> MetricMBeanFactory.counter(metric));
        c.put(MetricType.TIMER, (metric, metricMetadata) -> MetricMBeanFactory.timer(metric, metricMetadata));
        c.put(MetricType.METER, (metric, metricMetadata) -> MetricMBeanFactory.meter(metric, metricMetadata));
        c.put(MetricType.HISTOGRAM, (metric, metricMetadata) -> MetricMBeanFactory.histogram(metric));
        c.put(MetricType.GAUGE, (metric, metricMetadata) -> MetricMBeanFactory.gauge(metric));
        mbeanCreators = Collections.unmodifiableMap(c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricCollectorRegistry#registerCollector(com.openexchange.metrics.MetricCollector)
     */
    @Override
    public void registerCollector(MetricCollector metricCollector) throws OXException {
        if (metricCollector == null) {
            throw new IllegalArgumentException("Cannot register a 'null' metric collector");
        }

        MetricCollector existingMC = collectors.putIfAbsent(metricCollector.getComponentName(), metricCollector);
        if (existingMC != null) {
            LOG.warn("There is already another metric collector registered with '{}'", metricCollector.getComponentName());
            return;
        }

        MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate(metricCollector.getComponentName());
        ((AbstractMetricCollector) metricCollector).setMetricRegistry(metricRegistry);

        for (MetricMetadata metadata : metricCollector.getMetricMetadata()) {
            registerMetric(metricCollector, metadata, metricRegistry);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricCollectorRegistry#getCollector(java.lang.String)
     */
    @Override
    public MetricCollector getCollector(String componentName) {
        return collectors.get(componentName);
    }

    /**
     * Registers the specified {@link MetricCollector} with the specified {@link MetricMetadata} to the specified {@link MetricRegistry}
     * and create an MBean for the {@link Metric}
     * 
     * @param metricCollector The {@link MetricCollector}
     * @param metadata The {@link MetricMetadata}
     * @param metricRegistry The {@link MetricRegistry}
     * @throws OXException if the MBean for the specified {@link Metric} cannot be registered
     */
    private void registerMetric(MetricCollector metricCollector, MetricMetadata metadata, MetricRegistry metricRegistry) throws OXException {
        MetricType metricType = metadata.getMetricType();
        MetricTypeRegisterer metricTypeRegisterer = registerers.get(metricType);
        if (metricTypeRegisterer == null) {
            LOG.warn("No metric type registerer for '{}' was found.", metricType);
            return;
        }

        String componentName = metricCollector.getComponentName();
        Metric metric = metricTypeRegisterer.register(componentName, metadata, metricRegistry);
        registerMBean(metric, componentName, metadata);
    }

    /**
     * Creates and registers an MBean for the specified {@link Metric} under the specified component name
     * 
     * @param metric The {@link Metric} for which to create and register the MBean
     * @param componentName The component's name
     * @param metricMetadata The {@link MetricMetadata} of the {@link Metric}
     * @throws OXException if the MBean for the specified {@link Metric} cannot be registered
     */
    private void registerMBean(Metric metric, String componentName, MetricMetadata metricMetadata) throws OXException {
        try {
            ManagementService managementService = services.getService(ManagementService.class);
            managementService.registerMBean(getObjectName(componentName, metricMetadata.getMetricName()), mbeanCreators.get(metricMetadata.getMetricType()).apply(metric, metricMetadata));
        } catch (MalformedObjectNameException e) {
            throw new OXException(e);
        }
    }

    /**
     * Gets the {@link ObjectName} for the specified component and metric
     * 
     * @param componentName The component name
     * @param metricName the metric name
     * @return The created {@link ObjectName}
     * @throws MalformedObjectNameException if the string passed as a parameter does not have the right format.
     */
    private ObjectName getObjectName(String componentName, String metricName) throws MalformedObjectNameException {
        StringBuilder sb = new StringBuilder(DOMAIN_NAME);
        sb.append(":00=").append(componentName);
        sb.append(",name=").append(metricName);
        return new ObjectName(sb.toString());
    }
}
