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
 *    trademarks of the OX Software GmbH. group of companies.
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
     * SLA to publish concrete value buckets.
     * Comma-separated-list with time values (e.g. 50ms, 100ms, etc.)
     */
    SLA("distribution.", ""),
    /**
     * Defines queries for metrics
     */
    QUERY(),
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
