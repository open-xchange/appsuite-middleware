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

package com.openexchange.metrics;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 
 * {@link MetricDescriptor}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MetricDescriptor {

    private String group;
    private String name;
    private String unit;
    private MetricType metricType;
    private TimeUnit rate;
    private Supplier<?> metricSupplier;

    /**
     * Initialises a new {@link MetricDescriptor}.
     */
    public MetricDescriptor() {
        super();
    }

    /**
     * Returns the group of this metric
     * 
     * @return the group of this metric
     */
    public String getGroup() {
        return group;
    }

    /**
     * Returns the name of this metric
     * 
     * @return the name of this metric
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the group for this metric
     * 
     * @param group the group to set
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Sets the name for this metric
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the metricType
     *
     * @return The metricType
     */
    public MetricType getMetricType() {
        return metricType;
    }

    /**
     * Sets the metricType
     *
     * @param metricType The metricType to set
     */
    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
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

    /**
     * Gets the metric {@link Supplier} or <code>null</code> if none is specified
     *
     * @return The metric {@link Supplier} or <code>null</code> if none is specified
     */
    public Supplier<?> getMetricSupplier() {
        return metricSupplier;
    }

    /**
     * Sets the metricSupplier
     *
     * @param metricSupplier The metricSupplier to set
     */
    public void setMetricSupplier(Supplier<?> metricSupplier) {
        this.metricSupplier = metricSupplier;
    }

    /**
     * Initialises a new {@link MetricBuilder}
     * 
     * @param group The group
     * @param name The metric's name
     * @param metricType metric's type
     * @return the new {@link MetricBuilder}
     */
    public static MetricBuilder newBuilder(String group, String name, MetricType metricType) {
        return new MetricBuilder(group, name, metricType);
    }

    /////////////////////////////// Builder /////////////////////////////////

    /**
     * {@link AbstractBuilder}
     *
     * @param <T> The {@link MetricDescriptor} type
     */
    public static class MetricBuilder {

        protected static final String MISSING_FIELD = "A %s must be set!";

        protected final String group;
        protected final String name;
        protected final MetricType metricType;
        private TimeUnit rate = TimeUnit.SECONDS;
        private String unit = "events";
        private Supplier<?> supplier;

        /**
         * Initialises a new {@link AbstractBuilder}.
         * 
         * @param group The group for the metric
         * @param name The name for the metric
         * @param metricType The {@link MetricType}
         */
        public MetricBuilder(final String group, final String name, MetricType metricType) {
            super();
            this.group = group;
            this.name = name;
            this.metricType = metricType;
        }

        /**
         * Set the {@link TimeUnit} rate
         * 
         * @param rate The {@link TimeUnit} rate to set
         * @return the {@link MetricBuilder} for chained calls
         */
        public MetricBuilder withRate(TimeUnit rate) {
            this.rate = rate;
            return this;
        }

        /**
         * Set the unit
         * 
         * @param unit The unit to set
         * @return the {@link MetricBuilder} for chained calls
         */
        public MetricBuilder withUnit(String unit) {
            this.unit = unit;
            return this;
        }

        /**
         * Set the {@link Supplier}
         * 
         * @param supplier The {@link Supplier} to set
         * @return the {@link MetricBuilder} for chained calls
         */
        public MetricBuilder withMetricSupplier(Supplier<?> supplier) {
            this.supplier = supplier;
            return this;
        }

        /**
         * Builds and returns the {@link MetricDescriptor}
         * 
         * @return the {@link MetricDescriptor}
         */
        public MetricDescriptor build() {
            checkNotNull(group, "group");
            checkNotNull(name, "name");
            MetricDescriptor descriptor = prepare();
            fill(descriptor);
            return descriptor;
        }

        /**
         * Performs a preliminary check of the descriptor's values
         */
        protected void check() {
            checkNotNull(rate, "rate");
            checkNotNull(unit, "unit");
        }

        /**
         * Prepares the {@link MetricDescriptor}
         * 
         * @return The prepared {@link MetricDescriptor} as type {@link T}
         */
        protected MetricDescriptor prepare() {
            MetricDescriptor descriptor = new MetricDescriptor();
            descriptor.setGroup(group);
            descriptor.setName(name);
            return descriptor;
        }

        /**
         * Fills values of the specified descriptor
         * 
         * @param descriptor The descriptor of which the values shall be filled
         */
        protected void fill(MetricDescriptor descriptor) {
            descriptor.setRate(rate);
            descriptor.setUnit(unit);
            descriptor.setMetricSupplier(supplier);
        }

        /**
         * Check for <code>null</code> reference
         * 
         * @param reference The reference to check for <code>null</code>
         * @param errorMessage The error message for the {@link IllegalArgumentException}
         * @return The reference if not <code>null</code>
         * @throws IllegalArgumentException if the specified reference is <code>null</code>
         */
        static <T> T checkNotNull(T reference, String errorMessage) {
            if (reference == null) {
                throw new IllegalArgumentException(errorMessage);
            }
            return reference;
        }
    }
}
