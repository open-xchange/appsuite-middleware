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

package com.openexchange.metrics;

import java.util.HashSet;
import java.util.Set;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * {@link AbstractMetricCollector}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractMetricCollector implements MetricCollector {

    private MetricRegistry metricRegistry;
    private final Set<MetricMetadata> metricMetadata;
    private final String componentName;

    /**
     * Initialises a new {@link AbstractMetricCollector}.
     */
    public AbstractMetricCollector(String componentName) {
        super();
        this.componentName = componentName;
        metricMetadata = new HashSet<>();
    }

    /**
     * Sets the metricRegistry
     *
     * @param metricRegistry The metricRegistry to set
     */
    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricCollector#getComponentName()
     */
    @Override
    public String getComponentName() {
        return componentName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricCollector#getMetricMetadata()
     */
    @Override
    public Set<MetricMetadata> getMetricMetadata() {
        return metricMetadata;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.Blah#getHistogram(java.lang.String)
     */
    @Override
    public Histogram getHistogram(String name) {
        return (Histogram) checkAndReturn(metricRegistry.getHistograms().get(name), name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.Blah#getTimer(java.lang.String)
     */
    @Override
    public Timer getTimer(String name) {
        return (Timer) checkAndReturn(metricRegistry.getTimers().get(name), name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.Blah#getCounter(java.lang.String)
     */
    @Override
    public Counter getCounter(String name) {
        return (Counter) checkAndReturn(metricRegistry.getCounters().get(name), name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.Blah#getGauge(java.lang.String, java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Gauge<T> getGauge(String name, Class<T> clazz) {
        return (Gauge<T>) checkAndReturn(metricRegistry.getGauges().get(name), name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.Blah#getMeter(java.lang.String)
     */
    @Override
    public Meter getMeter(String name) {
        return (Meter) checkAndReturn(metricRegistry.getMeters().get(name), name);
    }

    /**
     * 
     * @param metric
     * @param name TODO
     * @return
     */
    private Metric checkAndReturn(Metric metric, String name) {
        if (metric == null) {
            throw new IllegalArgumentException("The metric collector has no metric with the name '" + name + "' registered");
        }
        return metric;
    }
}
