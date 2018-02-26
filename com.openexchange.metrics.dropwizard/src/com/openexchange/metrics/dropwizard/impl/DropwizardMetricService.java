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

package com.openexchange.metrics.dropwizard.impl;

import java.util.function.Supplier;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistryListener;
import com.openexchange.metrics.MetricService;
import com.openexchange.metrics.descriptors.MeterDescriptor;
import com.openexchange.metrics.dropwizard.types.DropwizardCounter;
import com.openexchange.metrics.dropwizard.types.DropwizardGauge;
import com.openexchange.metrics.dropwizard.types.DropwizardHistogram;
import com.openexchange.metrics.dropwizard.types.DropwizardMeter;
import com.openexchange.metrics.dropwizard.types.DropwizardTimer;
import com.openexchange.metrics.types.Counter;
import com.openexchange.metrics.types.Gauge;
import com.openexchange.metrics.types.Histogram;
import com.openexchange.metrics.types.Meter;
import com.openexchange.metrics.types.Timer;

/**
 * {@link DropwizardMetricService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
// FIXME: Create a new delegate every time? Maybe cache?
public class DropwizardMetricService implements MetricService {

    private final MetricRegistry registry;

    /**
     * Initialises a new {@link DropwizardMetricService}.
     */
    public DropwizardMetricService() {
        super();
        registry = new MetricRegistry();
    }

    public void addListener(MetricRegistryListener listener) {
        registry.addListener(listener);
    }

    public void removeListener(MetricRegistryListener listener) {
        registry.removeListener(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricService#getHistogram(java.lang.String, java.lang.String)
     */
    @Override
    public Histogram getHistogram(String group, String name) {
        return new DropwizardHistogram(registry.histogram(MetricRegistry.name(group, name)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricService#timer(java.lang.String, java.lang.String)
     */
    @Override
    public Timer getTimer(String group, String name) {
        return new DropwizardTimer(registry.timer(MetricRegistry.name(group, name)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricService#getCounter(java.lang.String, java.lang.String)
     */
    @Override
    public Counter getCounter(String group, String name) {
        return new DropwizardCounter(registry.counter(MetricRegistry.name(group, name)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricService#getGauge(java.lang.String, java.lang.String, java.util.function.Supplier)
     */
    @Override
    public <T> Gauge<T> getGauge(String group, String name, Supplier<T> metricSupplier) {
        return (Gauge<T>) new DropwizardGauge(registry.gauge(MetricRegistry.name(group, name), () -> () -> metricSupplier.get()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricService#meter(com.openexchange.metrics.descriptors.MeterDescriptor)
     */
    @Override
    public Meter meter(MeterDescriptor descriptor) {
        return new DropwizardMeter(registry.meter(MetricRegistry.name(descriptor.getGroup(), descriptor.getName())));
    }
}
