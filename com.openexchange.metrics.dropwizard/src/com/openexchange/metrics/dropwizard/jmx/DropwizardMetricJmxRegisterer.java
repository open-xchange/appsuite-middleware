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

package com.openexchange.metrics.dropwizard.jmx;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistryListener;
import com.codahale.metrics.Timer;
import com.openexchange.management.ManagementService;
import com.openexchange.metrics.jmx.AbstractMetricJmxRegisterer;
import com.openexchange.metrics.jmx.MetricMBeanFactory;

/**
 * {@link DropwizardMetricJmxRegisterer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropwizardMetricJmxRegisterer extends AbstractMetricJmxRegisterer implements MetricRegistryListener {

    /**
     * Initialises a new {@link DropwizardMetricJmxRegisterer}.
     * @param managementService
     * @param mbeanFactory
     */
    public DropwizardMetricJmxRegisterer(ManagementService managementService, MetricMBeanFactory mbeanFactory) {
        super(managementService, mbeanFactory);
    }

    /* (non-Javadoc)
     * @see com.codahale.metrics.MetricRegistryListener#onGaugeAdded(java.lang.String, com.codahale.metrics.Gauge)
     */
    @Override
    public void onGaugeAdded(String name, Gauge<?> gauge) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.codahale.metrics.MetricRegistryListener#onGaugeRemoved(java.lang.String)
     */
    @Override
    public void onGaugeRemoved(String name) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.codahale.metrics.MetricRegistryListener#onCounterAdded(java.lang.String, com.codahale.metrics.Counter)
     */
    @Override
    public void onCounterAdded(String name, Counter counter) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.codahale.metrics.MetricRegistryListener#onCounterRemoved(java.lang.String)
     */
    @Override
    public void onCounterRemoved(String name) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.codahale.metrics.MetricRegistryListener#onHistogramAdded(java.lang.String, com.codahale.metrics.Histogram)
     */
    @Override
    public void onHistogramAdded(String name, Histogram histogram) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.codahale.metrics.MetricRegistryListener#onHistogramRemoved(java.lang.String)
     */
    @Override
    public void onHistogramRemoved(String name) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.codahale.metrics.MetricRegistryListener#onMeterAdded(java.lang.String, com.codahale.metrics.Meter)
     */
    @Override
    public void onMeterAdded(String name, Meter meter) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see com.codahale.metrics.MetricRegistryListener#onMeterRemoved(java.lang.String)
     */
    @Override
    public void onMeterRemoved(String name) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see com.codahale.metrics.MetricRegistryListener#onTimerAdded(java.lang.String, com.codahale.metrics.Timer)
     */
    @Override
    public void onTimerAdded(String name, Timer timer) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.codahale.metrics.MetricRegistryListener#onTimerRemoved(java.lang.String)
     */
    @Override
    public void onTimerRemoved(String name) {
        // TODO Auto-generated method stub
        
    }
}
