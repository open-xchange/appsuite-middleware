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

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jmx.JmxReporter;
import com.openexchange.metrics.MetricRegistryService;

/**
 * {@link MetricRegistryServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MetricRegistryServiceImpl implements MetricRegistryService {

    private final MetricRegistry metricRegistry;
    private final JmxReporter jmxReporter;

    /**
     * Initialises a new {@link MetricRegistryServiceImpl}.
     */
    public MetricRegistryServiceImpl() {
        super();
        metricRegistry = new MetricRegistry();

        jmxReporter = JmxReporter.forRegistry(metricRegistry).inDomain("com.openexchange.metrics").build();
        jmxReporter.start();
    }

    /**
     * Shutdown the service
     */
    public void shutDown() {
        jmxReporter.stop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricService#registerMeter(java.lang.String)
     */
    @Override
    public Meter registerMeter(String meterName) {
        return metricRegistry.meter(meterName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricRegistryService#registerMeter(java.lang.Class, java.lang.String)
     */
    @Override
    public <T> Meter registerMeter(Class<T> clazz, String meterName) {
        return metricRegistry.meter(MetricRegistry.name(clazz, meterName));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricService#registerTimer(java.lang.String)
     */
    @Override
    public Timer registerTimer(String timerName) {
        return metricRegistry.timer(timerName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricRegistryService#registerTimer(java.lang.Class, java.lang.String)
     */
    @Override
    public <T> Timer registerTimer(Class<T> clazz, String timerName) {
        return metricRegistry.timer(MetricRegistry.name(clazz, timerName));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricRegistryService#registerGauge(java.lang.String, com.codahale.metrics.Gauge)
     */
    @Override
    public <T> void registerGauge(String gaugeName, Gauge<T> gauge) {
        metricRegistry.register(gaugeName, gauge);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricRegistryService#registerGauge(java.lang.Class, java.lang.String, com.codahale.metrics.Gauge)
     */
    @Override
    public <T, V> void registerGauge(Class<T> clazz, String gaugeName, Gauge<V> gauge) {
        metricRegistry.register(MetricRegistry.name(clazz, gaugeName), gauge);
    }
}
