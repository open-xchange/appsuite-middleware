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

package com.openexchange.metrics.micrometer.internal.property;

import com.openexchange.config.lean.Property;

/**
 * {@link MicrometerFilterProperty}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public enum MicrometerFilterProperty implements Property {

    /**
     * Enables/Disables metrics
     */
    ENABLE(Boolean.TRUE),
    /**
     * Enables/Disables the percentiles histograms.
     * {@link Boolean}
     */
    HISTOGRAM("distribution.", ""),
    /**
     * Sets the minimum expected value for the distribution.
     * {@link Long}
     */
    MINIMUM("distribution.", ""),
    /**
     * Sets the maximum expected value for the distribution.
     * {@link Long}
     */
    MAXIMUM("distribution.", ""),
    /**
     * Publishes concrete percentiles for the distribution.
     * Comma-separated-list with {@link Double}s
     */
    PERCENTILES("distribution.", ""),
    /**
     * SLO to publish concrete value buckets.
     * Comma-separated-list with time values (e.g. 50ms, 100ms, etc.)
     */
    SLO("distribution.", ""),
    /**
     * Defines queries for metrics
     */
    FILTER(),
    ;

    public static final String BASE = "com.openexchange.metrics.micrometer.";
    private static final String EMPTY = "";
    private final Object defaultValue;
    private final String midfix;

    /**
     * Initializes a new {@link MicrometerFilterProperty}.
     */
    private MicrometerFilterProperty() {
        this(EMPTY, null);
    }

    /**
     * Initializes a new {@link MicrometerFilterProperty}.
     *
     * @param defaultValue The default value
     */
    private MicrometerFilterProperty(Object defaultValue) {
        this(EMPTY, defaultValue);
    }

    /**
     * Initializes a new {@link MicrometerFilterProperty}.
     *
     * @param midfix The midfix
     * @param defaultValue The default value
     */
    private MicrometerFilterProperty(String midfix, Object defaultValue) {
        this.defaultValue = defaultValue;
        this.midfix = midfix;
    }

    /**
     * Returns the midfix of the property
     *
     * @return the midfix of the property
     */
    public String getMidFix() {
        return midfix;
    }

    @Override
    public String getFQPropertyName() {
        return BASE + midfix + name().toLowerCase();
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
