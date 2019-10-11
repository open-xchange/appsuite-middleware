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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.management.ObjectName;
import com.google.common.collect.ImmutableMap;

/**
 *
 * {@link MetricDescriptor}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MetricDescriptor {

    private final String group;
    private final String name;
    private final String unit;
    private final MetricType metricType;
    private final TimeUnit rate;
    private final Supplier<?> metricSupplier;
    private final String fullName;
    private final String description;
    private final Map<String, String> dimensions;

    /**
     * Initializes a new {@link MetricDescriptor}.
     */
    MetricDescriptor(String group, String name, String unit, MetricType metricType, TimeUnit rate, Supplier<?> metricSupplier, String fullName, String description, Map<String, String> dimensions) {
        super();
        this.group = group;
        this.name = name;
        this.unit = unit;
        this.metricType = metricType;
        this.rate = rate;
        this.metricSupplier = metricSupplier;
        this.fullName = fullName;
        this.description = description;
        this.dimensions = dimensions == null || dimensions.isEmpty() ? null : ImmutableMap.copyOf(dimensions);
    }


    /**
     * Gets the group name of this metric.
     *
     * @return The group name of this metric
     */
    public String getGroup() {
        return group;
    }

    /**
     * Gets the name of this metric.
     *
     * @return The name of this metric
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the metric type.
     *
     * @return The metric type
     */
    public MetricType getMetricType() {
        return metricType;
    }

    /**
     * Gets the rate.
     *
     * @return The rate
     */
    public TimeUnit getRate() {
        return rate;
    }

    /**
     * Gets the unit.
     *
     * @return The unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Gets the metric {@link Supplier} or <code>null</code> if none is specified.
     *
     * @return The metric {@link Supplier} or <code>null</code> if none is specified
     */
    public Supplier<?> getMetricSupplier() {
        return metricSupplier;
    }

    /**
     * Gets the fully qualifying name of the metric.
     *
     * @return The full name of the metric
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Gets the description.
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the optional dimensions.
     *
     * @return The optional dimensions
     */
    public Optional<Map<String, String>> getDimensions() {
        return Optional.ofNullable(dimensions);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((metricType == null) ? 0 : metricType.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((rate == null) ? 0 : rate.hashCode());
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
        result = prime * result + ((dimensions == null) ? 0 : dimensions.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MetricDescriptor other = (MetricDescriptor) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (fullName == null) {
            if (other.fullName != null) {
                return false;
            }
        } else if (!fullName.equals(other.fullName)) {
            return false;
        }
        if (group == null) {
            if (other.group != null) {
                return false;
            }
        } else if (!group.equals(other.group)) {
            return false;
        }
        if (metricType != other.metricType) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (rate != other.rate) {
            return false;
        }
        if (unit == null) {
            if (other.unit != null) {
                return false;
            }
        } else if (!unit.equals(other.unit)) {
            return false;
        }
        if (dimensions == null) {
            if (other.dimensions != null) {
                return false;
            }
        } else if (!dimensions.equals(other.dimensions)) {
            return false;
        }
        return true;
    }

    /////////////////////////////// Builder /////////////////////////////////

    /**
     * Initializes a new {@link MetricBuilder}
     *
     * @param group The group
     * @param name The metric's name
     * @param metricType metric's type
     * @return the new {@link MetricBuilder}
     */
    public static MetricBuilder newBuilder(final String group, final String name, final MetricType metricType) {
        return new MetricBuilder(group, name, metricType);
    }

    /**
     * The builder ofr an instance of <code>MetricDescriptor</code>.
     */
    public static class MetricBuilder {

        private final String group;
        private final String name;
        private final MetricType metricType;
        private TimeUnit rate = TimeUnit.SECONDS;
        private String unit = "events";
        private Supplier<?> supplier;
        private String description;
        private Map<String, String> dimensions;

        /**
         * Initializes a new {@link AbstractBuilder}.
         *
         * @param group The group for the metric
         * @param name The name for the metric
         * @param metricType The {@link MetricType}
         */
        MetricBuilder(final String group, final String name, final MetricType metricType) {
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
        public MetricBuilder withRate(final TimeUnit rate) {
            this.rate = rate;
            return this;
        }

        /**
         * Set the unit
         *
         * @param unit The unit to set
         * @return the {@link MetricBuilder} for chained calls
         */
        public MetricBuilder withUnit(final String unit) {
            this.unit = unit;
            return this;
        }

        /**
         * Set the {@link Supplier}
         *
         * @param supplier The {@link Supplier} to set
         * @return the {@link MetricBuilder} for chained calls
         */
        public MetricBuilder withMetricSupplier(final Supplier<?> supplier) {
            this.supplier = supplier;
            return this;
        }

        /**
         * Sets the description
         *
         * @param description the description to set
         * @return the {@link MetricBuilder} for chained calls
         */
        public MetricBuilder withDescription(final String description) {
            this.description = description;
            return this;
        }

        /**
         * Adds an additional dimension to this descriptor.
         * <p>
         * This dimensions are used to create the {@link ObjectName} for the MBean.
         * So please see {@link ObjectName} descriptions for limitations.
         *
         * @param key The key of the dimension. Must not be 'type' or 'name'
         * @param value The value of the dimension
         * @return the {@link MetricBuilder} for chained calls
         * @throws IllegalArgumentException If either key or value are invalid
         */
        public MetricBuilder addDimension(String key, String value) {
            if (key == null) {
                throw new IllegalArgumentException("The key must not be null");
            }
            if (value == null) {
                throw new IllegalArgumentException("The value must not be null");
            }
            if ("type".equals(key) || "name".equals(key)) {
                throw new IllegalArgumentException("The key is not allowed");
            }
            checkKey(key);
            checkValue(value);

            if (dimensions == null) {
                dimensions = new LinkedHashMap<>();
            }
            dimensions.put(key, value);
            return this;
        }

        private static void checkKey(String key) {
            int len = key.length();
            for (int i = len; i-- > 0;) {
                char c = key.charAt(i);
                switch (c) {
                    case '*':
                    case '?':
                    case ',':
                    case ':':
                    case '\n':
                        String ichar = ((c == '\n') ? "\\n" : "" + c);
                        throw new IllegalArgumentException("Invalid character in key: '" + ichar + "'");
                    default:
                        break;
                }
            }
        }

        private static void checkValue(String value) {
            int len = value.length();
            for (int i = len; i-- > 0;) {
                char c = value.charAt(i);
                switch (c) {
                    case '=':
                    case ':':
                    case '\n':
                        String ichar = ((c == '\n') ? "\\n" : "" + c);
                        throw new IllegalArgumentException("Invalid character '" + ichar + "' in value");
                    default:
                        break;
                }
            }
        }

        /**
         * Builds and returns the {@link MetricDescriptor}
         *
         * @return the {@link MetricDescriptor}
         */
        public MetricDescriptor build() {
            checkNotNull(group, "group");
            checkNotNull(name, "name");
            StringBuilder fqn = new StringBuilder(group).append('.').append(name);
            if (dimensions != null) {
                for (Map.Entry<String, String> dimension : dimensions.entrySet()) {
                    fqn.append('.').append(dimension.getKey()).append('-').append(dimension.getValue());
                }
            }
            return new MetricDescriptor(group, name, unit, metricType, rate, supplier, fqn.toString(), description, dimensions);
        }

        /**
         * Check for <code>null</code> reference
         *
         * @param reference The reference to check for <code>null</code>
         * @param errorMessage The error message for the {@link IllegalArgumentException}
         * @return The reference if not <code>null</code>
         * @throws IllegalArgumentException if the specified reference is <code>null</code>
         */
        static <T> T checkNotNull(final T reference, final String errorMessage) {
            if (reference == null) {
                throw new IllegalArgumentException(errorMessage);
            }
            return reference;
        }
    } // End of MetricBuilder

}
