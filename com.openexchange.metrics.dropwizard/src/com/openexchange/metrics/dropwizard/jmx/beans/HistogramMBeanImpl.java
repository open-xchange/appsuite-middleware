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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.HistogramMBean#getCount()
     */
    @Override
    public long getCount() {
        return histogram.getCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.HistogramMBean#getMin()
     */
    @Override
    public long getMin() {
        return histogram.getSnapshot().getMin();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.HistogramMBean#getMax()
     */
    @Override
    public long getMax() {
        return histogram.getSnapshot().getMax();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.HistogramMBean#getMean()
     */
    @Override
    public double getMean() {
        return histogram.getSnapshot().getMean();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.HistogramMBean#getStdDev()
     */
    @Override
    public double getStdDev() {
        return histogram.getSnapshot().getStdDev();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.HistogramMBean#get50thPercentile()
     */
    @Override
    public double get50thPercentile() {
        return histogram.getSnapshot().getMedian();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.HistogramMBean#get75thPercentile()
     */
    @Override
    public double get75thPercentile() {
        return histogram.getSnapshot().get75thPercentile();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.HistogramMBean#get95thPercentile()
     */
    @Override
    public double get95thPercentile() {
        return histogram.getSnapshot().get95thPercentile();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.HistogramMBean#get98thPercentile()
     */
    @Override
    public double get98thPercentile() {
        return histogram.getSnapshot().get98thPercentile();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.HistogramMBean#get99thPercentile()
     */
    @Override
    public double get99thPercentile() {
        return histogram.getSnapshot().get99thPercentile();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.HistogramMBean#get999thPercentile()
     */
    @Override
    public double get999thPercentile() {
        return histogram.getSnapshot().get999thPercentile();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.HistogramMBean#values()
     */
    @Override
    public long[] values() {
        return histogram.getSnapshot().getValues();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.jmx.HistogramMBean#getSnapshotSize()
     */
    @Override
    public long getSnapshotSize() {
        return histogram.getSnapshot().size();
    }

}
