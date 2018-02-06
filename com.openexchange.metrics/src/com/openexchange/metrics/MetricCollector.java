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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

import java.util.Map;
import java.util.Set;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.Timer;

/**
 * {@link MetricCollector}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface MetricCollector {

    /**
     * Determines whether this collector was enabled via the configuration.
     * 
     * @return <code>true</code> if the collector is enabled via the configuration,
     *         <code>false</code> otherwise
     */
    boolean isEnabled();

    /**
     * Returns the name of the component that this collector is responsible for.
     * 
     * @return the name of the component that this collector is responsible for.
     */
    String getComponentName();

    /**
     * Returns an unmodifiable {@link Map} with the {@link MetricMetadata}
     * for this collector.
     * 
     * @return an unmodifiable {@link Map} of {@link MetricMetadata}s for this collector
     */
    Set<MetricMetadata> getMetricMetadata();

    /**
     * Retrieves the {@link Histogram} registered under the specified name
     * or <code>null</code> if no such {@link Histogram} exists
     * 
     * @param name The name of the {@link Histogram}
     * @return the {@link Histogram} registered under the specified name
     *         or <code>null</code> if no such {@link Histogram} exists
     */
    Histogram getHistogram(String name);

    /**
     * Retrieves the {@link Timer} registered under the specified name
     * or <code>null</code> if no such {@link Timer} exists
     * 
     * @param name The name of the {@link Timer}
     * @return the {@link Timer} registered under the specified name
     *         or <code>null</code> if no such {@link Timer} exists
     */
    Timer getTimer(String name);

    /**
     * Retrieves the {@link Counter} registered under the specified name
     * or <code>null</code> if no such {@link Counter} exists
     * 
     * @param name The name of the {@link Counter}
     * @return the {@link Counter} registered under the specified name
     *         or <code>null</code> if no such {@link Counter} exists
     */
    Counter getCounter(String name);

    /**
     * Retrieves the {@link Gauge} with the specified type {@link T} registered
     * under the specified name or <code>null</code> if no such {@link Gauge} exists
     * 
     * @param name The name of the {@link Gauge}
     * @return the {@link Gauge} with the specified type {@link T} registered
     *         under the specified name or <code>null</code> if no such {@link Gauge} exists
     */
    <T> Gauge<T> getGauge(String name, Class<T> clazz);

    /**
     * Retrieves the {@link RatioGauge} with the specified type {@link T} registered
     * under the specified name or <code>null</code> if no such {@link RatioGauge} exists
     * 
     * @param name The name of the {@link RatioGauge}
     * @return the {@link RatioGauge} with the specified type {@link T} registered
     *         under the specified name or <code>null</code> if no such {@link RatioGauge} exists
     */
    <T extends RatioGauge> T getRatioGauge(String name, Class<T> clazz);

    /**
     * Retrieves the {@link Meter} registered under the specified name
     * or <code>null</code> if no such {@link Meter} exists
     * 
     * @param name The name of the {@link Meter}
     * @return the {@link Meter} registered under the specified name
     *         or <code>null</code> if no such {@link Meter} exists
     */
    Meter getMeter(String name);
}
