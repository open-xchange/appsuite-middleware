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

    static final Logger LOGGER = LoggerFactory.getLogger(IPCheckMBeanImpl.class);

    private final MetricAware<IPCheckMetricCollector> metricAware;
    private IPCheckMetricCollector metricCollector;

    /** Window size for average calculation: 1 day */
    private static final long WINDOW_SIZE = 24L * 60L * 60000L;

    /** Double ended queue holding measurements over an hour */
    private final LinkedBlockingDeque<Measurement> measurements;

    private ScheduledTimerTask timerTask;

    private double acceptedPercentage;
    private double deniedPercentage;

    private double acceptedPrivatePercentage;
    private double acceptedWhiteListedPercentage;
    private double acceptedEligilePercentage;
    private double deniedExceptionPercentage;
    private double deniedCountryChangedPercentage;

    private double acceptedPrivateOverallPercentage;
    private double acceptedWhiteListedOverallPercentage;
    private double acceptedEligileOverallPercentage;
    private double deniedExceptionOverallPercentage;
    private double deniedCountryChangedOverallPercentage;

    private long acceptedChangesPerHour;
    private long deniedChangesPerHour;
    private long ipChangesPerHour;

    /**
     * Represents a measurement of accepted and denied IP changes for a certain point in time.
     */
    static final class Measurement {

        final long timestamp;
        final double acceptedIPChanges;
        final double deniedIPChanges;
        final double totalIPChanges;

        Measurement(double accepted, double denied, double totalIPChanges) {
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
        if (timerService == null) {
            throw new IllegalStateException("No such service: " + TimerService.class.getName());
        }
        IPCheckMetricCollector collector = this.metricCollector;
        LinkedBlockingDeque<Measurement> tmp_measurements = this.measurements;
        Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    double accepted = collector.getCount(IPCheckMetric.acceptedEligibleIPChanges);
                    double denied = collector.getCount(IPCheckMetric.deniedIPChanges);
                    double ipChanges = collector.getCount(IPCheckMetric.totalIPChanges);
                    tmp_measurements.add(new Measurement(accepted, denied, ipChanges));
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
                for (Measurement measurement; (measurement = tmp_measurements.peek()) != null && measurement.timestamp < minTime;) {
                    tmp_measurements.poll();
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
        double total = metricCollector.getCount(IPCheckMetric.totalIPChanges);
        double totalAccepted = metricCollector.getCount(IPCheckMetric.acceptedIPChanges);
        double totalDenied = metricCollector.getCount(IPCheckMetric.deniedIPChanges);

        double acceptedPrivate = 0;
        double acceptedWL = 0;
        double acceptedEligible = 0;
        if (totalAccepted > 0) {
            // Accepted percentages
            acceptedPrivate = metricCollector.getCount(IPCheckMetric.acceptedPrivateIP);
            acceptedPrivatePercentage = ((float) acceptedPrivate / totalAccepted) * 100;
            acceptedWL = metricCollector.getCount(IPCheckMetric.acceptedWhiteListed);
            acceptedWhiteListedPercentage = ((float) acceptedWL / totalAccepted) * 100;
            acceptedEligible = metricCollector.getCount(IPCheckMetric.acceptedEligibleIPChanges);
            acceptedEligilePercentage = ((float) acceptedEligible / totalAccepted) * 100;
        }

        double deniedEx = 0;
        double deniedCC = 0;
        // Denied percentages
        if (totalDenied > 0) {
            deniedEx = metricCollector.getCount(IPCheckMetric.deniedException);
            deniedExceptionPercentage = (deniedEx / totalDenied) * 100;
            deniedCC = metricCollector.getCount(IPCheckMetric.deniedCountryChanged);
            deniedCountryChangedPercentage = (deniedCC / totalDenied) * 100;
        }

        if (total > 0) {
            acceptedPercentage = (totalAccepted / total) * 100;
            deniedPercentage = (totalDenied / total) * 100;

            // Overall accepted percentages
            acceptedPrivateOverallPercentage = (acceptedPrivate / total) * 100;
            acceptedWhiteListedOverallPercentage = (acceptedWL / total) * 100;
            acceptedEligileOverallPercentage = (acceptedEligible / total) * 100;

            // Overall denied percentages
            deniedExceptionOverallPercentage = (deniedEx / total) * 100;
            deniedCountryChangedOverallPercentage = (deniedCC / total) * 100;
        }
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

    @Override
    protected void refresh() {
        metricCollector = metricAware.getMetricsObject();
        calculatePercentages();
        calculateChangesPerHour();
    }

    @Override
    public long getIPChangesPerHour() {
        return ipChangesPerHour;
    }

    @Override
    public long getAcceptedIPChangesPerHour() {
        return acceptedChangesPerHour;
    }

    @Override
    public long getDeniedIPChangesPerHour() {
        return deniedChangesPerHour;
    }

    @Override
    public float getAcceptedPercentage() {
        return (float) acceptedPercentage;
    }

    @Override
    public float getDeniedPercentage() {
        return (float) deniedPercentage;
    }

    @Override
    public float getAcceptedPrivatePercentage() {
        return (float) acceptedPrivatePercentage;
    }

    @Override
    public float getAcceptedWhiteListedPercentage() {
        return (float) acceptedWhiteListedPercentage;
    }

    @Override
    public float getAcceptedEligilePercentage() {
        return (float) acceptedEligilePercentage;
    }

    @Override
    public float getDeniedExceptionPercentage() {
        return (float) deniedExceptionPercentage;
    }

    @Override
    public float getDeniedCountryChangedPercentage() {
        return (float) deniedCountryChangedPercentage;
    }

    @Override
    public float getAcceptedPrivateOverallPercentage() {
        return (float) acceptedPrivateOverallPercentage;
    }

    @Override
    public float getAcceptedWhiteListedOverallPercentage() {
        return (float) acceptedWhiteListedOverallPercentage;
    }

    @Override
    public float getAcceptedEligileOverallPercentage() {
        return (float) acceptedEligileOverallPercentage;
    }

    @Override
    public float getDeniedExceptionOverallPercentage() {
        return (float) deniedExceptionOverallPercentage;
    }

    @Override
    public float getDeniedCountryChangedOverallPercentage() {
        return (float) deniedCountryChangedOverallPercentage;
    }
}
