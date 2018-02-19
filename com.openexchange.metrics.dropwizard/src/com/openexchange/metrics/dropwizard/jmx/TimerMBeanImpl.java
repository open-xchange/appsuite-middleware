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

package com.openexchange.metrics.dropwizard.jmx;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.management.NotCompliantMBeanException;
import com.codahale.metrics.Timer;
import com.openexchange.metrics.jmx.TimerMBean;

/**
 * {@link TimerMBeanImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TimerMBeanImpl extends MeterMBeanImpl implements TimerMBean {

    private static final String DESCRIPTION = "Timer MBean";
    private final Timer timer;
    private final double durationFactor;
    private final String durationUnit;

    /**
     * Initialises a new {@link TimerMBeanImpl}.
     *
     * @throws NotCompliantMBeanException
     */
    public TimerMBeanImpl(Timer timer, TimeUnit timeUnit) throws NotCompliantMBeanException {
        super(DESCRIPTION, TimerMBean.class, timer, "events", timeUnit);
        this.timer = timer;
        this.durationFactor = 1.0 / TimeUnit.MILLISECONDS.toNanos(1); // TODO
        this.durationUnit = TimeUnit.MILLISECONDS.toString().toLowerCase(Locale.US); // TODO
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.TimerMBean#getMin()
     */
    @Override
    public double getMin() {
        return timer.getSnapshot().getMin() * durationFactor;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.TimerMBean#getMax()
     */
    @Override
    public double getMax() {
        return timer.getSnapshot().getMax() * durationFactor;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.TimerMBean#getMean()
     */
    @Override
    public double getMean() {
        return timer.getSnapshot().getMean() * durationFactor;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.TimerMBean#getStdDev()
     */
    @Override
    public double getStdDev() {
        return timer.getSnapshot().getStdDev() * durationFactor;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.TimerMBean#get50thPercentile()
     */
    @Override
    public double get50thPercentile() {
        return timer.getSnapshot().getMedian() * durationFactor;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.TimerMBean#get75thPercentile()
     */
    @Override
    public double get75thPercentile() {
        return timer.getSnapshot().get75thPercentile() * durationFactor;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.TimerMBean#get95thPercentile()
     */
    @Override
    public double get95thPercentile() {
        return timer.getSnapshot().get95thPercentile() * durationFactor;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.TimerMBean#get98thPercentile()
     */
    @Override
    public double get98thPercentile() {
        return timer.getSnapshot().get98thPercentile() * durationFactor;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.TimerMBean#get99thPercentile()
     */
    @Override
    public double get99thPercentile() {
        return timer.getSnapshot().get99thPercentile() * durationFactor;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.TimerMBean#get999thPercentile()
     */
    @Override
    public double get999thPercentile() {
        return timer.getSnapshot().get999thPercentile() * durationFactor;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.TimerMBean#values()
     */
    @Override
    public long[] values() {
        return timer.getSnapshot().getValues();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.metrics.jmx.TimerMBean#getDurationUnit()
     */
    @Override
    public String getDurationUnit() {
        return durationUnit;
    }


}
