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

    @Override
    public long getCount() {
        return meter.getCount();
    }

    @Override
    public double getMeanRate() {
        return meter.getMeanRate() * rateFactor;
    }

    @Override
    public double getOneMinuteRate() {
        return meter.getOneMinuteRate() * rateFactor;
    }

    @Override
    public double getFiveMinuteRate() {
        return meter.getFiveMinuteRate() * rateFactor;
    }

    @Override
    public double getFifteenMinuteRate() {
        return meter.getFifteenMinuteRate() * rateFactor;
    }

    @Override
    public String getRateUnit() {
        return rateUnit;
    }
}
