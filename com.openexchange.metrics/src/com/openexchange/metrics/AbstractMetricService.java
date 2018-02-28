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

package com.openexchange.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.metrics.jmx.MetricServiceListener;
import com.openexchange.metrics.types.Counter;
import com.openexchange.metrics.types.Gauge;
import com.openexchange.metrics.types.Histogram;
import com.openexchange.metrics.types.Meter;
import com.openexchange.metrics.types.Metric;
import com.openexchange.metrics.types.Timer;

/**
 * {@link AbstractMetricService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractMetricService implements MetricService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMetricService.class);

    private final ConcurrentMap<String, Metric> metrics;
    private final Map<MetricType, MetricRegisterer> registerers;
    private final Map<MetricType, MetricServiceListenerNotifier> listenerNotifiersOnAdd;
    private final Map<MetricType, MetricServiceListenerNotifier> listenerNotifiersOnRemove;
    private final List<MetricServiceListener> listeners;

    /**
     * Initialises a new {@link AbstractMetricService}.
     */
    public AbstractMetricService() {
        super();
        metrics = new ConcurrentHashMap<>();
        registerers = new HashMap<>();
        listeners = new ArrayList<>();

        Map<MetricType, MetricServiceListenerNotifier> l = new HashMap<>();
        l.put(MetricType.COUNTER, (listener, metric, descriptor) -> listener.onCounterAdded(descriptor, (Counter) metric));
        l.put(MetricType.GAUGE, (listener, metric, descriptor) -> listener.onGaugeAdded(descriptor, (Gauge) metric));
        l.put(MetricType.HISTOGRAM, (listener, metric, descriptor) -> listener.onHistogramAdded(descriptor, (Histogram) metric));
        l.put(MetricType.METER, (listener, metric, descriptor) -> listener.onMeterAdded(descriptor, (Meter) metric));
        l.put(MetricType.TIMER, (listener, metric, descriptor) -> listener.onTimerAdded(descriptor, (Timer) metric));

        listenerNotifiersOnAdd = Collections.unmodifiableMap(l);

        l = new HashMap<>();
        l.put(MetricType.COUNTER, (listener, metric, descriptor) -> listener.onCounterRemoved(descriptor.getFullName()));
        l.put(MetricType.GAUGE, (listener, metric, descriptor) -> listener.onGaugeRemoved(descriptor.getFullName()));
        l.put(MetricType.HISTOGRAM, (listener, metric, descriptor) -> listener.onHistogramRemoved(descriptor.getFullName()));
        l.put(MetricType.METER, (listener, metric, descriptor) -> listener.onMeterRemoved(descriptor.getFullName()));
        l.put(MetricType.TIMER, (listener, metric, descriptor) -> listener.onTimerRemoved(descriptor.getFullName()));

        listenerNotifiersOnRemove = Collections.unmodifiableMap(l);
    }

    /**
     * Adds the specified registerer to the local registry.
     * 
     * @param metricType The {@link MetricType}
     * @param registerer The registerer
     */
    protected void addRegisterer(MetricType metricType, MetricRegisterer registerer) {
        registerers.put(metricType, registerer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricService#addListener(com.openexchange.metrics.jmx.MetricServiceListener)
     */
    @Override
    public void addListener(MetricServiceListener listener) {
        listeners.add(listener);
        //TODO: notify already registered metrics
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricService#removeListener(com.openexchange.metrics.jmx.MetricServiceListener)
     */
    @Override
    public void removeListener(MetricServiceListener listener) {
        listeners.remove(listener);
    }

    /**
     * Registers a new metric with the specified {@link MetricDescriptor} or gets
     * an already existing one
     * 
     * @param descriptor The {@link MetricDescriptor}
     * @return The newly registered {@link Metric} or an already existing one
     */
    protected Metric registerOrGet(MetricDescriptor descriptor) {
        String key = descriptor.getFullName();
        Metric metric = metrics.get(key);
        if (metric != null) {
            return metric;
        }

        MetricRegisterer registerer = registerers.get(descriptor.getMetricType());
        if (registerer == null) {
            throw new IllegalArgumentException("There is no metric registerer for metric type '" + descriptor.getMetricType() + "'");
        }
        Metric dropwizardMetric = registerer.register(descriptor);
        Metric raced = metrics.putIfAbsent(key, dropwizardMetric);
        if (raced == null) {
            notifyListenersOnAdd(descriptor, dropwizardMetric);
            return dropwizardMetric;
        }
        return raced;
    }

    /**
     * Remove the specified metric
     * 
     * @param metricDescriptor
     */
    protected void remove(MetricDescriptor metricDescriptor) {
        String key = metricDescriptor.getFullName();
        metrics.remove(key);
        MetricRegisterer registerer = registerers.get(metricDescriptor.getMetricType());
        if (registerer == null) {
            return;
        }

        registerer.unregister(metricDescriptor);
        notifyListenersOnRemove(metricDescriptor);
    }

    /**
     * Notifies all listeners about the specified added {@link Metric}
     * 
     * @param descriptor The {@link MetricDescriptor}
     * @param metric The added {@link Metric}
     */
    private void notifyListenersOnAdd(MetricDescriptor descriptor, Metric metric) {
        for (MetricServiceListener listener : listeners) {
            notifyListener(listenerNotifiersOnAdd, listener, metric, descriptor);
        }
    }

    /**
     * Notifies all listeners about the specified removed {@link Metric}
     * 
     * @param descriptor The {@link MetricDescriptor}
     * @param metric The added {@link Metric}
     */
    private void notifyListenersOnRemove(MetricDescriptor descriptor) {
        for (MetricServiceListener listener : listeners) {
            notifyListener(listenerNotifiersOnRemove, listener, null, descriptor);
        }
    }

    /**
     * Notifies the specified listener
     * 
     * @param notifiers The notifiers
     * @param listener The listener
     * @param metric The {@link Metric}
     * @param descriptor The {@link MetricDescriptor}
     */
    private void notifyListener(Map<MetricType, MetricServiceListenerNotifier> notifiers, MetricServiceListener listener, Metric metric, MetricDescriptor descriptor) {
        MetricServiceListenerNotifier notifier = notifiers.get(descriptor.getMetricType());
        if (notifier == null) {
            LOG.debug("No listener notifier found for metric type '{}'", descriptor.getMetricType());
            return;
        }
        notifier.notify(listener, metric, descriptor);
    }
}
