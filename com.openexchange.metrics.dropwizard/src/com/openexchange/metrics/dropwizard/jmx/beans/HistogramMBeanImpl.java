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

import javax.management.NotCompliantMBeanException;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.dropwizard.types.DropwizardHistogram;
import com.openexchange.metrics.jmx.beans.AbstractMetricMBean;
import com.openexchange.metrics.jmx.beans.HistogramMBean;
import com.openexchange.metrics.types.Histogram;

/**
 * {@link HistogramMBeanImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class HistogramMBeanImpl extends AbstractMetricMBean implements HistogramMBean {

    private final DropwizardHistogram histogram;

    /**
     * Initialises a new {@link HistogramMBeanImpl}.
     * 
     * @param histogram The {@link Histogram} metric
     * @param metricDescriptor The {@link MetricDescriptor}
     * @throws NotCompliantMBeanException
     */
    public HistogramMBeanImpl(DropwizardHistogram histogram, MetricDescriptor metricDescriptor) throws NotCompliantMBeanException {
        super(HistogramMBean.class, metricDescriptor);
        this.histogram = histogram;
    }

    @Override
    public long getCount() {
        return histogram.getCount();
    }

    @Override
    public long getMin() {
        return histogram.getSnapshot().getMin();
    }

    @Override
    public long getMax() {
        return histogram.getSnapshot().getMax();
    }

    @Override
    public double getMean() {
        return histogram.getSnapshot().getMean();
    }

    @Override
    public double getStdDev() {
        return histogram.getSnapshot().getStdDev();
    }

    @Override
    public double get50thPercentile() {
        return histogram.getSnapshot().getMedian();
    }

    @Override
    public double get75thPercentile() {
        return histogram.getSnapshot().get75thPercentile();
    }

    @Override
    public double get95thPercentile() {
        return histogram.getSnapshot().get95thPercentile();
    }

    @Override
    public double get98thPercentile() {
        return histogram.getSnapshot().get98thPercentile();
    }

    @Override
    public double get99thPercentile() {
        return histogram.getSnapshot().get99thPercentile();
    }

    @Override
    public double get999thPercentile() {
        return histogram.getSnapshot().get999thPercentile();
    }

    @Override
    public long[] values() {
        return histogram.getSnapshot().getValues();
    }

    @Override
    public long getSnapshotSize() {
        return histogram.getSnapshot().size();
    }

}
