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

package com.openexchange.metrics.dropwizard.jmx.beans;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.management.NotCompliantMBeanException;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.dropwizard.types.DropwizardMeter;
import com.openexchange.metrics.jmx.beans.AbstractMetricMBean;
import com.openexchange.metrics.jmx.beans.MeterMBean;
import com.openexchange.metrics.types.Meter;

/**
 * {@link MeterMBeanImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MeterMBeanImpl extends AbstractMetricMBean implements MeterMBean {

    private final double rateFactor;
    private final String rateUnit;
    private final DropwizardMeter meter;

    /**
     * Initialises a new {@link MeterMBeanImpl}.
     * 
     * @param meter The {@link Meter} metric
     * @param metricDescriptor The {@link MetricDescriptor}
     * @throws NotCompliantMBeanException
     */
    public MeterMBeanImpl(DropwizardMeter meter, MetricDescriptor metricDescriptor) throws NotCompliantMBeanException {
        super(MeterMBean.class, metricDescriptor);
        this.meter = meter;
        this.rateFactor = calculateRateFactor(metricDescriptor.getRate());
        this.rateUnit = metricDescriptor.getUnit() + "/" + calculateRateUnit(metricDescriptor.getRate());
    }

    /**
     *
     * @param unit
     * @return
     */
    private String calculateRateUnit(TimeUnit unit) {
        final String s = unit.toString().toLowerCase(Locale.US);
        return s.substring(0, s.length() - 1);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.MeterMBean#getCount()
     */
    @Override
    public long getCount() {
        return meter.getCount();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.MeterMBean#getMeanRate()
     */
    @Override
    public double getMeanRate() {
        return meter.getMeanRate() * rateFactor;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.MeterMBean#getOneMinuteRate()
     */
    @Override
    public double getOneMinuteRate() {
        return meter.getOneMinuteRate() * rateFactor;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.MeterMBean#getFiveMinuteRate()
     */
    @Override
    public double getFiveMinuteRate() {
        return meter.getFiveMinuteRate() * rateFactor;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.MeterMBean#getFifteenMinuteRate()
     */
    @Override
    public double getFifteenMinuteRate() {
        return meter.getFifteenMinuteRate() * rateFactor;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.MeterMBean#getRateUnit()
     */
    @Override
    public String getRateUnit() {
        return rateUnit;
    }
}
