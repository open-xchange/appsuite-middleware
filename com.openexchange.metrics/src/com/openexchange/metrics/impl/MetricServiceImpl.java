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

package com.openexchange.metrics.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistry.MetricSupplier;
import com.codahale.metrics.MetricRegistryListener;
import com.codahale.metrics.Timer;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;
import com.openexchange.metrics.MetricMetadata;
import com.openexchange.metrics.MetricService;
import com.openexchange.metrics.MetricType;
import com.openexchange.metrics.descriptors.MeterDescriptor;
import com.openexchange.metrics.dropwizard.jmx.MetricMBeanFactory;
import com.openexchange.metrics.impl.dropwizard.MeterImpl;
import com.openexchange.metrics.jmx.MetricMBean;

/**
 * {@link MetricServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MetricServiceImpl implements MetricService {

    private static final Logger LOG = LoggerFactory.getLogger(MetricServiceImpl.class);

    private final MetricRegistry registry;
    private AtomicReference<JmxRegisterer> jmxRegistererRef;

    public MetricServiceImpl() {
        super();
        registry = new MetricRegistry();
        jmxRegistererRef = new AtomicReference<>();
    }

    @Override
    public Histogram getHistogram(String group, String name) {
        return registry.histogram(MetricRegistry.name(group, name));
    }

    @Override
    public Timer getTimer(String group, String name) {
        return registry.timer(MetricRegistry.name(group, name));
    }

    @Override
    public Counter getCounter(String group, String name) {
        return registry.counter(MetricRegistry.name(group, name));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Gauge getGauge(String group, String name, MetricSupplier<Gauge> supplier) {
        return registry.gauge(MetricRegistry.name(group, name), supplier);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricService#getGauge(java.lang.String, java.lang.String, java.util.function.Supplier)
     */
    @Override
    public Gauge<?> getGauge(String group, String name, Supplier<MetricSupplier<Gauge>> metricSupplier) {
        return registry.gauge(MetricRegistry.name(group, name), metricSupplier.get());
    }

    @SuppressWarnings("rawtypes")
    public Gauge getGauge2(String group, String name, Supplier<Integer> s) {
        return registry.gauge(MetricRegistry.name(group, name), () -> () -> s.get());
    }

    @Override
    public Meter getMeter(String group, String name) {
        return registry.meter(MetricRegistry.name(group, name));
    }

    @Override
    public com.openexchange.metrics.types.Meter meter(MeterDescriptor descriptor) {
        Meter delegate = registry.meter(MetricRegistry.name(descriptor.getGroup(), descriptor.getName()));
        return new MeterImpl(delegate);
    }

    public void setManagementService(ManagementService service) {
        JmxRegisterer registerer = new JmxRegisterer(service);
        if (jmxRegistererRef.compareAndSet(null, registerer)) {
            LOG.info("Starting MBean population for metrics");
            registry.addListener(registerer);
        } else {
            LOG.warn("ManagementService was set twice. The second instance is ignored for metric MBeans!");
        }
    }

    public void unsetManagementService(ManagementService service) {
        JmxRegisterer registerer = jmxRegistererRef.get();
        if (registerer != null && registerer.managementService == service) {
            jmxRegistererRef.compareAndSet(registerer, null);
            registry.removeListener(registerer);
            registerer.unregisterAll();
            LOG.info("Stopped MBeans for metrics");
        }
    }

    private static final class JmxRegisterer implements MetricRegistryListener {

        private static final String DOMAIN_NAME = "com.openexchange.metrics";

        private final ManagementService managementService;
        private final Map<MetricType, BiFunction<Metric, MetricMetadata, MetricMBean>> mbeanCreators;
        private final List<ObjectName> registeredNames;

        /**
         * Initialises a new {@link MBeanRegisterer}.
         * 
         * @param managementService
         */
        public JmxRegisterer(ManagementService managementService) {
            super();
            this.managementService = managementService;

            Map<MetricType, BiFunction<Metric, MetricMetadata, MetricMBean>> c = new HashMap<>();
            c.put(MetricType.COUNTER, (metric, metricMetadata) -> MetricMBeanFactory.counter(metric));
            c.put(MetricType.TIMER, (metric, metricMetadata) -> MetricMBeanFactory.getTimer(metric, metricMetadata));
            c.put(MetricType.METER, (metric, metricMetadata) -> MetricMBeanFactory.meter(metric, metricMetadata));
            c.put(MetricType.HISTOGRAM, (metric, metricMetadata) -> MetricMBeanFactory.histogram(metric));
            c.put(MetricType.GAUGE, (metric, metricMetadata) -> MetricMBeanFactory.gauge(metric));
            c.put(MetricType.RATIO_GAUGE, (metric, metricMetadata) -> MetricMBeanFactory.ratioGauge(metric));
            mbeanCreators = Collections.unmodifiableMap(c);

            registeredNames = Collections.synchronizedList(new LinkedList<>());
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.codahale.metrics.MetricRegistryListener#onGaugeAdded(java.lang.String, com.codahale.metrics.Gauge)
         */
        @Override
        public void onGaugeAdded(String name, Gauge<?> gauge) {
            registerMBean(MetricType.GAUGE, name, gauge);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.codahale.metrics.MetricRegistryListener#onGaugeRemoved(java.lang.String)
         */
        @Override
        public void onGaugeRemoved(String name) {
            unregisterMBean(MetricType.GAUGE, name);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.codahale.metrics.MetricRegistryListener#onCounterAdded(java.lang.String, com.codahale.metrics.Counter)
         */
        @Override
        public void onCounterAdded(String name, Counter counter) {
            registerMBean(MetricType.COUNTER, name, counter);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.codahale.metrics.MetricRegistryListener#onCounterRemoved(java.lang.String)
         */
        @Override
        public void onCounterRemoved(String name) {
            unregisterMBean(MetricType.COUNTER, name);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.codahale.metrics.MetricRegistryListener#onHistogramAdded(java.lang.String, com.codahale.metrics.Histogram)
         */
        @Override
        public void onHistogramAdded(String name, Histogram histogram) {
            registerMBean(MetricType.HISTOGRAM, name, histogram);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.codahale.metrics.MetricRegistryListener#onHistogramRemoved(java.lang.String)
         */
        @Override
        public void onHistogramRemoved(String name) {
            unregisterMBean(MetricType.HISTOGRAM, name);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.codahale.metrics.MetricRegistryListener#onMeterAdded(java.lang.String, com.codahale.metrics.Meter)
         */
        @Override
        public void onMeterAdded(String name, Meter meter) {
            registerMBean(MetricType.METER, name, meter);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.codahale.metrics.MetricRegistryListener#onMeterRemoved(java.lang.String)
         */
        @Override
        public void onMeterRemoved(String name) {
            unregisterMBean(MetricType.METER, name);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.codahale.metrics.MetricRegistryListener#onTimerAdded(java.lang.String, com.codahale.metrics.Timer)
         */
        @Override
        public void onTimerAdded(String name, Timer timer) {
            registerMBean(MetricType.TIMER, name, timer);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.codahale.metrics.MetricRegistryListener#onTimerRemoved(java.lang.String)
         */
        @Override
        public void onTimerRemoved(String name) {
            unregisterMBean(MetricType.TIMER, name);
        }

        private void unregisterAll() {
            for (ObjectName name : registeredNames) {
                try {
                    managementService.unregisterMBean(name);
                } catch (OXException e) {
                    LOG.warn("Unable to unregister MBean for metric {}", name, e);
                }
            }
        }

        /**
         * Creates and registers an MBean for the specified {@link Metric} under the specified component name
         *
         * @param type The metric type
         * @param name The metric's name
         * @param metric The {@link Metric} for which to create and register the MBean
         * @param metricMetadata The {@link MetricMetadata} of the {@link Metric}
         * @throws OXException if the MBean for the specified {@link Metric} cannot be registered
         */
        private void registerMBean(MetricType type, String name, Metric metric) {
            BiFunction<Metric, MetricMetadata, MetricMBean> registerer = mbeanCreators.get(type);
            if (registerer == null) {
                LOG.warn("No metric type mbean registerer for '{}' was found.", type);
                return;
            }
            try {
                // TODO: get metadata from service call
                MetricMetadata metadata = new MetricMetadata(type, name);
                ObjectName objectName = getObjectName(type, name);
                managementService.registerMBean(objectName, registerer.apply(metric, metadata));
                registeredNames.add(objectName);
            } catch (MalformedObjectNameException | OXException e) {
                LOG.warn("Unable to register MBean for metric {}", name, e);
            }
        }

        /**
         * Unregisters an MBean with the specified component name
         *
         * @param type the metric type
         * @param name The metric's name
         * @param metricMetadata The {@link MetricMetadata} of the {@link Metric}
         * @throws OXException if the MBean for the specified {@link Metric} cannot be unregistered
         */
        private void unregisterMBean(MetricType type, String name) {
            try {
                managementService.unregisterMBean(getObjectName(type, name));
            } catch (MalformedObjectNameException | OXException e) {
                LOG.warn("Unable to unregister MBean for metric {}", name, e);
            }
        }

        /**
         * Gets the {@link ObjectName} for the specified component and metric
         *
         * @param type the metric type
         * @param name the metric name
         * @return The created {@link ObjectName}
         * @throws MalformedObjectNameException if the string passed as a parameter does not have the right format.
         */
        private ObjectName getObjectName(MetricType type, String name) throws MalformedObjectNameException {
            Hashtable<String, String> properties = new Hashtable<>();
            int index = name.indexOf('.');
            if (index > 0 && index < name.length() - 1) {
                properties.put("type", name.substring(0, index));
                properties.put("name", name.substring(index + 1));
            } else {
                properties.put("type", name);
            }

            return ObjectName.getInstance(DOMAIN_NAME, properties);
        }
    }
}
