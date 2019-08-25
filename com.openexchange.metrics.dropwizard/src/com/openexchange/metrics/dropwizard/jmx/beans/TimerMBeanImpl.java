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
import com.openexchange.metrics.dropwizard.types.DropwizardTimer;
import com.openexchange.metrics.jmx.beans.AbstractMetricMBean;
import com.openexchange.metrics.jmx.beans.TimerMBean;
import com.openexchange.metrics.types.Timer;

/**
 * {@link TimerMBeanImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TimerMBeanImpl extends AbstractMetricMBean implements TimerMBean {

    private final DropwizardTimer timer;
    private final double durationFactor;
    private final String durationUnit;
    private final double rateFactor;
    private final String rateUnit;

    /**
     * Initialises a new {@link TimerMBeanImpl}.
     * 
     * @param timer The {@link Timer} metric
     * @param metricDescriptor The {@link MetricDescriptor}
     * @throws NotCompliantMBeanException When this MBean is not JMX compliant
     */
    public TimerMBeanImpl(DropwizardTimer timer, MetricDescriptor metricDescriptor) throws NotCompliantMBeanException {
        super(TimerMBean.class, metricDescriptor);
        this.timer = timer;
        this.rateUnit = metricDescriptor.getUnit() + "/" + calculateRateUnit(metricDescriptor.getRate());
        this.rateFactor = calculateRateFactor(metricDescriptor.getRate());
        this.durationFactor = 1.0 / TimeUnit.MILLISECONDS.toNanos(1);
        this.durationUnit = TimeUnit.MILLISECONDS.toString().toLowerCase(Locale.US);
    }

    /**
     * Calculates the rate unit using the specified TimeUnit for
     * the calculation.
     * 
     * @param unit The TimeUnit to use for the calculation
     * @return the rate unit as string
     */
    private String calculateRateUnit(TimeUnit unit) {
        String s = unit.toString().toLowerCase(Locale.US);
        return s.substring(0, s.length() - 1);
    }

    @Override
    public double getMin() {
        return timer.getSnapshot().getMin() * durationFactor;
    }

    @Override
    public double getMax() {
        return timer.getSnapshot().getMax() * durationFactor;
    }

    @Override
    public double getMean() {
        return timer.getSnapshot().getMean() * durationFactor;
    }

    @Override
    public double getStdDev() {
        return timer.getSnapshot().getStdDev() * durationFactor;
    }

    @Override
    public double get50thPercentile() {
        return timer.getSnapshot().getMedian() * durationFactor;
    }

    @Override
    public double get75thPercentile() {
        return timer.getSnapshot().get75thPercentile() * durationFactor;
    }

    @Override
    public double get95thPercentile() {
        return timer.getSnapshot().get95thPercentile() * durationFactor;
    }

    @Override
    public double get98thPercentile() {
        return timer.getSnapshot().get98thPercentile() * durationFactor;
    }

    @Override
    public double get99thPercentile() {
        return timer.getSnapshot().get99thPercentile() * durationFactor;
    }

    @Override
    public double get999thPercentile() {
        return timer.getSnapshot().get999thPercentile() * durationFactor;
    }

    @Override
    public long getCount() {
        return timer.getCount();
    }

    @Override
    public double getMeanRate() {
        return timer.getMeanRate() * rateFactor;
    }

    @Override
    public double getOneMinuteRate() {
        return timer.getOneMinuteRate() * rateFactor;
    }

    @Override
    public double getFiveMinuteRate() {
        return timer.getFiveMinuteRate() * rateFactor;
    }

    @Override
    public double getFifteenMinuteRate() {
        return timer.getFifteenMinuteRate() * rateFactor;
    }

    @Override
    public long[] values() {
        return timer.getSnapshot().getValues();
    }

    @Override
    public String getDurationUnit() {
        return durationUnit;
    }

    @Override
    public String getRateUnit() {
        return rateUnit;
    }

}
