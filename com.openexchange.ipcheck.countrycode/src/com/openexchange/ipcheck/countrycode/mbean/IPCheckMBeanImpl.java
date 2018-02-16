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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.ipcheck.countrycode.mbean;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import javax.management.DynamicMBean;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.management.AnnotatedDynamicStandardMBean;
import com.openexchange.management.MetricAware;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link IPCheckMBeanImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class IPCheckMBeanImpl extends AnnotatedDynamicStandardMBean implements IPCheckMBean, DynamicMBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPCheckMBeanImpl.class);

    private final MetricAware<IPCheckMetricCollector> metricAware;
    private IPCheckMetricCollector metricCollector;

    /** Window size for average calculation: 1 day */
    private static final long WINDOW_SIZE = 24L * 60L * 60000L;

    /** Double ended queue holding measurements over an hour */
    private final LinkedBlockingDeque<Measurement> measurements;

    private ScheduledTimerTask timerTask;

    private float acceptedPercentage;
    private float deniedPercentage;

    private float acceptedPrivatePercentage;
    private float acceptedWhiteListedPercentage;
    private float acceptedEligilePercentage;
    private float deniedExceptionPercentage;
    private float deniedCountryChangedPercentage;

    private float acceptedPrivateOverallPercentage;
    private float acceptedWhiteListedOverallPercentage;
    private float acceptedEligileOverallPercentage;
    private float deniedExceptionOverallPercentage;
    private float deniedCountryChangedOverallPercentage;

    private long acceptedChangesPerHour;
    private long deniedChangesPerHour;
    private long ipChangesPerHour;

    /**
     * Represents a measurement of accepted and denied IP changes for a certain point in time.
     */
    static final class Measurement {

        final long timestamp;
        final long acceptedIPChanges;
        final long deniedIPChanges;
        final long totalIPChanges;

        Measurement(long accepted, long denied, long totalIPChanges) {
            super();
            this.acceptedIPChanges = accepted;
            this.deniedIPChanges = denied;
            this.totalIPChanges = totalIPChanges;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * Initialises a new {@link IPCheckMBeanImpl}.
     * 
     * @param services The {@link ServiceLookup} instance
     * @throws NotCompliantMBeanException
     */
    public IPCheckMBeanImpl(ServiceLookup services, MetricAware<IPCheckMetricCollector> metricAware) throws NotCompliantMBeanException {
        super(services, IPCheckMBean.NAME, IPCheckMBean.class);
        this.metricAware = metricAware;
        measurements = new LinkedBlockingDeque<Measurement>();

        refresh();
        startTask();
    }

    /**
     * Start the task
     */
    private void startTask() {
        TimerService timerService = getService(TimerService.class);
        Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    long accepted = metricCollector.getMeter(IPCheckMetric.acceptedEligibleIPChanges.getMetricName()).getCount();
                    long denied = metricCollector.getMeter(IPCheckMetric.deniedIPChanges.getMetricName()).getCount();
                    long ipChanges = metricCollector.getMeter(IPCheckMetric.totalIPChanges.getMetricName()).getCount();
                    measurements.add(new Measurement(accepted, denied, ipChanges));
                    cleanUp();
                } catch (Exception e) {
                    LOGGER.error("{}", e.getMessage(), e);
                }
            }

            /**
             * Cleans all metrics that are expired
             */
            private void cleanUp() {
                long minTime = System.currentTimeMillis() - WINDOW_SIZE;
                for (Measurement measurement; (measurement = measurements.peek()) != null && measurement.timestamp < minTime;) {
                    measurements.poll();
                }
            }
        };
        timerTask = timerService.scheduleAtFixedRate(task, 0L, 60 * 60000L);
    }

    /**
     * Calculates the percentages
     */
    private void calculatePercentages() {
        // Work with local copies
        long total = metricCollector.getMeter(IPCheckMetric.totalIPChanges.getMetricName()).getCount();
        long totalAccepted = metricCollector.getMeter(IPCheckMetric.acceptedIPChanges.getMetricName()).getCount();
        long totalDenied = metricCollector.getMeter(IPCheckMetric.deniedIPChanges.getMetricName()).getCount();
        acceptedPercentage = ((float) totalAccepted / total) * 100;
        deniedPercentage = ((float) totalDenied / total) * 100;

        // Accepted percentages
        long acceptedPrivate = metricCollector.getMeter(IPCheckMetric.acceptedPrivateIP.getMetricName()).getCount();
        acceptedPrivatePercentage = ((float) acceptedPrivate / totalAccepted) * 100;
        long acceptedWL = metricCollector.getMeter(IPCheckMetric.acceptedWhiteListed.getMetricName()).getCount();
        acceptedWhiteListedPercentage = ((float) acceptedWL / totalAccepted) * 100;
        long acceptedEligible = metricCollector.getMeter(IPCheckMetric.acceptedEligibleIPChanges.getMetricName()).getCount();
        acceptedEligilePercentage = ((float) acceptedEligible / totalAccepted) * 100;

        // Overall accepted percentages
        acceptedPrivateOverallPercentage = ((float) acceptedPrivate / total) * 100;
        acceptedWhiteListedOverallPercentage = ((float) acceptedWL / total) * 100;
        acceptedEligileOverallPercentage = ((float) acceptedEligible / total) * 100;

        // Denied percentages
        long deniedEx = metricCollector.getMeter(IPCheckMetric.deniedException.getMetricName()).getCount();
        deniedExceptionPercentage = ((float) deniedEx / totalDenied) * 100;
        long deniedCC = metricCollector.getMeter(IPCheckMetric.deniedCountryChanged.getMetricName()).getCount();
        deniedCountryChangedPercentage = ((float) deniedCC / totalDenied) * 100;

        // Overall denied percentages
        deniedExceptionOverallPercentage = ((float) deniedEx / total) * 100;
        deniedCountryChangedOverallPercentage = ((float) deniedCC / total) * 100;
    }

    /**
     * Calculates the changes per hour
     * 
     * @throws MBeanException
     */
    private void calculateChangesPerHour() {
        long meantimes = 0L;
        long accepted = 0L;
        long denied = 0L;
        long ipChanges = 0L;

        Measurement last = null;
        for (Iterator<Measurement> it = measurements.iterator(); it.hasNext();) {
            Measurement current = it.next();
            if (last != null) {
                meantimes += current.timestamp - last.timestamp;
                accepted += current.acceptedIPChanges - last.acceptedIPChanges;
                denied += current.deniedIPChanges - last.deniedIPChanges;
                ipChanges += current.totalIPChanges - last.totalIPChanges;
            }

            last = current;
        }

        double acceptedPerMillis = 0L;
        if (accepted > 0L && meantimes > 0L) {
            acceptedPerMillis = accepted / (double) meantimes;
        }

        double deniedPerMillis = 0L;
        if (denied > 0L && meantimes > 0L) {
            deniedPerMillis = denied / (double) meantimes;
        }

        double ipChangesPerMillis = 0L;
        if (ipChanges > 0L && meantimes > 0L) {
            ipChangesPerMillis = ipChanges / (double) meantimes;
        }

        acceptedChangesPerHour = Math.round(acceptedPerMillis * 60000L * 60L);
        deniedChangesPerHour = Math.round(deniedPerMillis * 60000L * 60L);
        ipChangesPerHour = Math.round(ipChangesPerMillis * 60000L * 60L);
    }

    /**
     * Stops this MBean.
     */
    public void stop() {
        timerTask.cancel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.management.AnnotatedDynamicStandardMBean#refresh()
     */
    @Override
    protected void refresh() {
        metricCollector = metricAware.getMetricsObject();
        calculatePercentages();
        calculateChangesPerHour();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean#getIPChangesPerHour()
     */
    @Override
    public long getIPChangesPerHour() {
        return ipChangesPerHour;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean#getAcceptedIPChangesPerHour()
     */
    @Override
    public long getAcceptedIPChangesPerHour() {
        return acceptedChangesPerHour;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean#getDeniedIPChangesPerHour()
     */
    @Override
    public long getDeniedIPChangesPerHour() {
        return deniedChangesPerHour;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean#getAcceptedPercentage()
     */
    @Override
    public float getAcceptedPercentage() {
        return acceptedPercentage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean#getDeniedPercentage()
     */
    @Override
    public float getDeniedPercentage() {
        return deniedPercentage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean#getAcceptedPrivatePercentage()
     */
    @Override
    public float getAcceptedPrivatePercentage() {
        return acceptedPrivatePercentage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean#getAcceptedWhiteListedPercentage()
     */
    @Override
    public float getAcceptedWhiteListedPercentage() {
        return acceptedWhiteListedPercentage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean#getAcceptedEligilePercentage()
     */
    @Override
    public float getAcceptedEligilePercentage() {
        return acceptedEligilePercentage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean#getDeniedExceptionPercentage()
     */
    @Override
    public float getDeniedExceptionPercentage() {
        return deniedExceptionPercentage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean#getDeniedCountryChangedPercentage()
     */
    @Override
    public float getDeniedCountryChangedPercentage() {
        return deniedCountryChangedPercentage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean#getAcceptedPrivateOverallPercentage()
     */
    @Override
    public float getAcceptedPrivateOverallPercentage() {
        return acceptedPrivateOverallPercentage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean#getAcceptedWhiteListedOverallPercentage()
     */
    @Override
    public float getAcceptedWhiteListedOverallPercentage() {
        return acceptedWhiteListedOverallPercentage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean#getAcceptedEligileOverallPercentage()
     */
    @Override
    public float getAcceptedEligileOverallPercentage() {
        return acceptedEligileOverallPercentage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean#getDeniedExceptionOverallPercentage()
     */
    @Override
    public float getDeniedExceptionOverallPercentage() {
        return deniedExceptionOverallPercentage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean#getDeniedCountryChangedOverallPercentage()
     */
    @Override
    public float getDeniedCountryChangedOverallPercentage() {
        return deniedCountryChangedOverallPercentage;
    }
}
