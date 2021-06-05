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

package com.openexchange.metrics;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;
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
@SuppressWarnings("deprecation")
public abstract class AbstractMetricService implements MetricService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMetricService.class);

    private final ConcurrentMap<String, MetricInformation> metrics;
    private final Map<MetricType, MetricRegisterer> registerers;
    private final Map<MetricType, MetricServiceListenerNotifier> listenerNotifiersOnAdd;
    private final Map<MetricType, MetricServiceListenerNotifier> listenerNotifiersOnRemove;
    private final Queue<MetricServiceListener> listeners;

    /**
     * Initialises a new {@link AbstractMetricService}.
     */
    public AbstractMetricService() {
        super();
        metrics = new ConcurrentHashMap<>();
        registerers = new ConcurrentHashMap<>();
        listeners = new ConcurrentLinkedQueue<>();

        // @formatter:off
        listenerNotifiersOnAdd = ImmutableMap.of(
            MetricType.COUNTER, (listener, metric, descriptor) -> listener.onCounterAdded(descriptor, (Counter) metric),
            MetricType.GAUGE, (listener, metric, descriptor) -> listener.onGaugeAdded(descriptor, (Gauge<?>) metric),
            MetricType.HISTOGRAM, (listener, metric, descriptor) -> listener.onHistogramAdded(descriptor, (Histogram) metric),
            MetricType.METER, (listener, metric, descriptor) -> listener.onMeterAdded(descriptor, (Meter) metric),
            MetricType.TIMER, (listener, metric, descriptor) -> listener.onTimerAdded(descriptor, (Timer) metric)
        );

        listenerNotifiersOnRemove = ImmutableMap.of(
            MetricType.COUNTER, (listener, metric, descriptor) -> listener.onCounterRemoved(descriptor),
            MetricType.GAUGE, (listener, metric, descriptor) -> listener.onGaugeRemoved(descriptor),
            MetricType.HISTOGRAM, (listener, metric, descriptor) -> listener.onHistogramRemoved(descriptor),
            MetricType.METER, (listener, metric, descriptor) -> listener.onMeterRemoved(descriptor),
            MetricType.TIMER, (listener, metric, descriptor) -> listener.onTimerRemoved(descriptor)
        );
        // @formatter:on
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

    @Override
    public void addListener(MetricServiceListener listener) {
        if (listeners.offer(listener)) {
            for (MetricInformation metricInformation : metrics.values()) {
                notifyListenersOnAdd(metricInformation);
            }
        }
    }

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
     * @throws IllegalArgumentException if the {@link MetricDescriptor} is <code>null</code> or
     *             no registerer exists for the {@link MetricType} specified by the descriptor
     */
    protected Metric registerOrGet(MetricDescriptor descriptor) {
        if (descriptor == null) {
            throw new IllegalArgumentException("Cannot register a metric with a 'null' metric descriptor.");
        }
        String key = descriptor.getFullName();
        MetricInformation metricInformation = metrics.get(key);
        if (metricInformation != null) {
            return metricInformation.getMetric();
        }

        MetricRegisterer registerer = registerers.get(descriptor.getMetricType());
        if (registerer == null) {
            throw new IllegalArgumentException("There is no metric registerer for metric type '" + descriptor.getMetricType() + "'");
        }
        MetricInformation dropwizardMetric = new MetricInformation(registerer.register(descriptor), descriptor);
        MetricInformation raced = metrics.putIfAbsent(key, dropwizardMetric);
        if (raced == null) {
            notifyListenersOnAdd(dropwizardMetric);
            return dropwizardMetric.getMetric();
        }
        LOG.debug("Meanwhile another metric of type '{}' and name '{}' was registered by another thread. Returning that metric.", descriptor.getMetricType(), descriptor.getFullName());
        return raced.getMetric();
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
    private void notifyListenersOnAdd(MetricInformation metricInformation) {
        for (MetricServiceListener listener : listeners) {
            notifyListener(listenerNotifiersOnAdd, listener, metricInformation.getMetric(), metricInformation.getMetricDescriptor());
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
