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
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistry.MetricSupplier;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.jmx.JmxReporter;
import com.openexchange.exception.OXException;
import com.openexchange.metrics.AbstractMetricCollector;
import com.openexchange.metrics.MetricCollector;
import com.openexchange.metrics.MetricCollectorRegistry;
import com.openexchange.metrics.MetricMetadata;
import com.openexchange.metrics.MetricType;
import com.openexchange.metrics.MetricTypeRegisterer;

/**
 * {@link MetricCollectorRegistryImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MetricCollectorRegistryImpl implements MetricCollectorRegistry {

    private final Map<MetricType, MetricTypeRegisterer> registerers;
    private final Map<String, MetricCollector> collectors;

    /**
     * Initialises a new {@link MetricCollectorRegistryImpl}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MetricCollectorRegistryImpl() {
        super();
        collectors = new ConcurrentHashMap<>();

        Map<MetricType, MetricTypeRegisterer> r = new HashMap<>();
        r.put(MetricType.COUNTER, (name, metricRegistry, supplier) -> metricRegistry.counter(name));
        r.put(MetricType.TIMER, (name, metricRegistry, supplier) -> metricRegistry.timer(name));
        r.put(MetricType.METER, (name, metricRegistry, supplier) -> metricRegistry.meter(name));
        r.put(MetricType.HISTOGRAM, (name, metricRegistry, supplier) -> metricRegistry.histogram(name));
        r.put(MetricType.GAUGE, (name, metricRegistry, supplier) -> metricRegistry.gauge(name, (MetricSupplier<Gauge>) supplier));
        registerers = Collections.unmodifiableMap(r);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricCollectorRegistry#registerCollector(com.openexchange.metrics.MetricCollector)
     */
    @Override
    public void registerCollector(MetricCollector metricCollector) throws OXException {
        MetricCollector existingMc = collectors.get(metricCollector.getComponentName());
        if (existingMc != null) {
            // TODO: Simple return instead of throwing an exception?
            throw new OXException(1138, "There is already another metric collector registered with '" + metricCollector.getComponentName() + "'");
        }
        collectors.put(metricCollector.getComponentName(), metricCollector);

        MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate(metricCollector.getComponentName());
        ((AbstractMetricCollector) metricCollector).setMetricRegistry(metricRegistry);

        for (MetricMetadata metadata : metricCollector.getMetricMetadata()) {
            registerers.get(metadata.getMetricType()).register(metadata.getMetricName(), metricRegistry, metadata.getMetricSupplier());
        }

        // TODO: implement JMX Reporter
        JmxReporter reporter = JmxReporter.forRegistry(metricRegistry).inDomain("com.openexchange.metrics." + metricCollector.getComponentName()).build();
        reporter.start();
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
}
