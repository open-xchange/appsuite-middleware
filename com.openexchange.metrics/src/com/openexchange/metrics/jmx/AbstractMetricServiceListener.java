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

package com.openexchange.metrics.jmx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.management.ManagementService;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.MetricType;
import com.openexchange.metrics.jmx.beans.MetricMBean;
import com.openexchange.metrics.types.Counter;
import com.openexchange.metrics.types.Gauge;
import com.openexchange.metrics.types.Histogram;
import com.openexchange.metrics.types.Meter;
import com.openexchange.metrics.types.Metric;
import com.openexchange.metrics.types.Timer;

/**
 * {@link AbstractMetricServiceListener}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractMetricServiceListener implements MetricServiceListener {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMetricServiceListener.class);

    private static final String DOMAIN_NAME = "com.openexchange.metrics";

    private final ManagementService managementService;
    private final Map<MetricType, BiFunction<Metric, MetricDescriptor, MetricMBean>> mbeanCreators;
    private final List<ObjectName> registeredNames;

    /**
     * Initialises a new {@link AbstractMetricServiceListener}.
     * 
     * @param managementService The {@link ManagementService}
     */
    public AbstractMetricServiceListener(ManagementService managementService, MetricMBeanFactory mbeanFactory) {
        super();
        this.managementService = managementService;

        Map<MetricType, BiFunction<Metric, MetricDescriptor, MetricMBean>> c = new HashMap<>();
        c.put(MetricType.COUNTER, (metric, metricDescriptor) -> mbeanFactory.counter((Counter) metric, metricDescriptor));
        c.put(MetricType.TIMER, (metric, metricDescriptor) -> mbeanFactory.timer((Timer) metric, metricDescriptor));
        c.put(MetricType.METER, (metric, metricDescriptor) -> mbeanFactory.meter((Meter) metric, metricDescriptor));
        c.put(MetricType.HISTOGRAM, (metric, metricDescriptor) -> mbeanFactory.histogram((Histogram) metric, metricDescriptor));
        c.put(MetricType.GAUGE, (metric, metricDescriptor) -> mbeanFactory.gauge((Gauge<?>) metric, metricDescriptor));
        mbeanCreators = Collections.unmodifiableMap(c);

        registeredNames = Collections.synchronizedList(new LinkedList<>());
    }

    /**
     * Unregisters all metric management beans
     */
    public void unregisterAll() {
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
     * @param metric The {@link Metric} for which to create and register the MBean
     * @param metricDescriptor The {@link MetricDescriptor} of the {@link Metric}
     * @throws OXException if the MBean for the specified {@link Metric} cannot be registered
     */
    protected void registerMBean(Metric metric, MetricDescriptor metricDescriptor) {
        BiFunction<Metric, MetricDescriptor, MetricMBean> registerer = mbeanCreators.get(metricDescriptor.getMetricType());
        if (registerer == null) {
            LOG.warn("No metric type mbean registerer for '{}' was found.", metricDescriptor.getMetricType());
            return;
        }
        try {
            ObjectName objectName = getObjectName(metricDescriptor);
            managementService.registerMBean(objectName, registerer.apply(metric, metricDescriptor));
            registeredNames.add(objectName);
        } catch (MalformedObjectNameException | OXException e) {
            LOG.warn("Unable to register MBean for metric {}", metricDescriptor.getName(), e);
        }
    }

    /**
     * Unregisters an MBean with the specified name
     *
     * @param name The metric's name
     * @throws OXException if the MBean for the specified {@link Metric} cannot be unregistered
     */
    protected void unregisterMBean(String name) {
        try {
            managementService.unregisterMBean(getObjectName(name));
        } catch (MalformedObjectNameException | OXException e) {
            LOG.warn("Unable to unregister MBean for metric {}", name, e);
        }
    }

    /**
     * Unregisters an MBean with the specified metric descriptor
     *
     * @param metricDescriptor The {@link MetricDescriptor} of the {@link Metric}
     * @throws OXException if the MBean for the specified {@link Metric} cannot be unregistered
     */
    protected void unregisterMBean(MetricDescriptor metricDescriptor) {
        try {
            managementService.unregisterMBean(getObjectName(metricDescriptor));
        } catch (MalformedObjectNameException | OXException e) {
            LOG.warn("Unable to unregister MBean for metric {}", metricDescriptor.getName(), e);
        }
    }

    /**
     * Gets the {@link ObjectName} for the specified component and metric
     *
     * @param metricDescriptor The {@link MetricDescriptor} of the {@link Metric}
     * @return The created {@link ObjectName}
     * @throws MalformedObjectNameException if the string passed as a parameter does not have the right format.
     */
    private ObjectName getObjectName(MetricDescriptor metricDescriptor) throws MalformedObjectNameException {
        Hashtable<String, String> properties = new Hashtable<>();
        properties.put("type", metricDescriptor.getGroup());
        properties.put("name", metricDescriptor.getName());
        if(metricDescriptor.getDimensions() != null && metricDescriptor.getDimensions().isEmpty() == false) {
            properties.putAll(metricDescriptor.getDimensions());
        }

        return ObjectName.getInstance(DOMAIN_NAME, properties);
    }

    /**
     * Gets the {@link ObjectName} for the specified component and metric
     *
     * @param name the metric name
     * @return The created {@link ObjectName}
     * @throws MalformedObjectNameException if the string passed as a parameter does not have the right format.
     */
    private ObjectName getObjectName(String name) throws MalformedObjectNameException {
        if (Strings.isEmpty(name)) {
            throw new IllegalArgumentException("Cannot create an ObjectName for an empty or 'null' name");
        }

        Hashtable<String, String> properties = new Hashtable<>();
        int indexOf = name.indexOf('.');
        if (indexOf <= 0) {
            properties.put("name", name);
        } else {
            properties.put("type", name.substring(0, indexOf));
            properties.put("name", name.substring(indexOf + 1));
        }
        return ObjectName.getInstance(DOMAIN_NAME, properties);
    }
}
