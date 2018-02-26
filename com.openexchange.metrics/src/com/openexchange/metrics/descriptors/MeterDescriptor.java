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

package com.openexchange.metrics.descriptors;

import java.util.concurrent.TimeUnit;
import com.openexchange.metrics.MetricType;
import com.openexchange.metrics.types.Meter;

/**
 * 
 * {@link MeterDescriptor}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MeterDescriptor extends MetricDescriptor {

    private TimeUnit rate;
    private String unit;

    /**
     * Initialises a new {@link MeterDescriptor}.
     */
    public MeterDescriptor() {
        super();
    }

    /**
     * Gets the rate
     * 
     * @return the rate
     */
    public TimeUnit getRate() {
        return rate;
    }

    /**
     * Gets the unit
     * 
     * @return the unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the rate
     * 
     * @param rate to set
     */
    public void setRate(TimeUnit rate) {
        this.rate = rate;
    }

    /**
     * Sets the unit
     * 
     * @param unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /////////////////////////////// Builder /////////////////////////////////

    /**
     * Creates and returns a new {@link Builder} instance for the specified
     * group and name
     * 
     * @param group The group of the descriptor
     * @param name The name of the descriptor
     * @return The {@link Builder}
     */
    public static Builder newBuilder(String group, String name, MetricType metricType) {
        return new Builder(group, name, metricType);
    }

    /**
     * {@link Builder}
     */
    public static final class Builder extends AbstractBuilder<MeterDescriptor> {

        private TimeUnit rate = TimeUnit.SECONDS;
        private String unit = "events";

        /**
         * Initialises a new {@link Builder}.
         * 
         * @param group The group or domain this metric is valid for
         * @param name The name of the metric
         */
        public Builder(String group, String name, MetricType metricType) {
            super(group, name, metricType);
        }

        /**
         * The rate unit of the {@link Meter}
         * 
         * @param unit the {@link TimeUnit}
         * @return the {@link Builder} for chained calls
         */
        public Builder withRate(TimeUnit unit) {
            this.rate = unit;
            return this;
        }

        /**
         * The unit of the {@link Meter}
         * 
         * @param unit the unit
         * @return the {@link Builder} for chained calls
         */
        public Builder withUnit(String unit) {
            this.unit = unit;
            return this;
        }

        @Override
        protected MeterDescriptor prepare() {
            MeterDescriptor descriptor = new MeterDescriptor();
            descriptor.setGroup(group);
            descriptor.setName(name);
            return descriptor;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.openexchange.metrics.descriptors.MetricDescriptor.AbstractBuilder#check()
         */
        @Override
        protected void check() {
            checkNotNull(rate, "rate");
            checkNotNull(unit, "unit");
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.openexchange.metrics.descriptors.MetricDescriptor.AbstractBuilder#fill(com.openexchange.metrics.descriptors.MetricDescriptor)
         */
        @Override
        protected void fill(MeterDescriptor descriptor) {
            descriptor.setRate(rate);
            descriptor.setUnit(unit);
        }
    }
}
