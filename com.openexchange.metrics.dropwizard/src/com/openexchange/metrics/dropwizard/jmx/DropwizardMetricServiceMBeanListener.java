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

import com.openexchange.management.ManagementService;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.jmx.AbstractMetricServiceListener;
import com.openexchange.metrics.jmx.MetricMBeanFactory;
import com.openexchange.metrics.types.Counter;
import com.openexchange.metrics.types.Gauge;
import com.openexchange.metrics.types.Histogram;
import com.openexchange.metrics.types.Meter;
import com.openexchange.metrics.types.Timer;

/**
 * {@link DropwizardMetricServiceMBeanListener}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropwizardMetricServiceMBeanListener extends AbstractMetricServiceListener {

    /**
     * Initialises a new {@link DropwizardMetricServiceMBeanListener}.
     * 
     * @param managementService
     * @param mbeanFactory
     */
    public DropwizardMetricServiceMBeanListener(ManagementService managementService, MetricMBeanFactory mbeanFactory) {
        super(managementService, mbeanFactory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.MetricServiceListener#onGaugeAdded(com.openexchange.metrics.MetricDescriptor, Gauge)
     */
    @Override
    public void onGaugeAdded(MetricDescriptor descriptor, Gauge<?> gauge) {
        registerMBean(gauge, descriptor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.MetricServiceListener#onGaugeRemoved(java.lang.String)
     */
    @Override
    public void onGaugeRemoved(String name) {
        unregisterMBean(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.MetricServiceListener#onCounterAdded(com.openexchange.metrics.MetricDescriptor, Counter)
     */
    @Override
    public void onCounterAdded(MetricDescriptor descriptor, Counter counter) {
        registerMBean(counter, descriptor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.MetricServiceListener#onCounterRemoved(java.lang.String)
     */
    @Override
    public void onCounterRemoved(String name) {
        unregisterMBean(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.MetricServiceListener#onHistogramAdded(com.openexchange.metrics.MetricDescriptor, Histogram)
     */
    @Override
    public void onHistogramAdded(MetricDescriptor descriptor, Histogram histogram) {
        registerMBean(histogram, descriptor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.MetricServiceListener#onHistogramRemoved(java.lang.String)
     */
    @Override
    public void onHistogramRemoved(String name) {
        unregisterMBean(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.MetricServiceListener#onMeterAdded(com.openexchange.metrics.MetricDescriptor, Meter)
     */
    @Override
    public void onMeterAdded(MetricDescriptor descriptor, Meter meter) {
        registerMBean(meter, descriptor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.MetricServiceListener#onMeterRemoved(java.lang.String)
     */
    @Override
    public void onMeterRemoved(String name) {
        unregisterMBean(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.MetricServiceListener#onTimerAdded(com.openexchange.metrics.MetricDescriptor, Timer)
     */
    @Override
    public void onTimerAdded(MetricDescriptor descriptor, Timer timer) {
        registerMBean(timer, descriptor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.MetricServiceListener#onTimerRemoved(java.lang.String)
     */
    @Override
    public void onTimerRemoved(String name) {
        unregisterMBean(name);
    }
}
