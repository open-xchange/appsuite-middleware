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

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

/**
 * {@link MetricRegistryService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface MetricRegistryService {

    /**
     * Registers a new {@link Meter} with the specified name. If another
     * {@link Meter} with the same name is already registered it will be returned
     * instead.
     * 
     * @param meterName The {@link Meter} name
     * @return The created {@link Meter} or a pre-existing one
     */
    Meter registerMeter(String meterName);

    /**
     * Registers a new {@link Meter} with the specified name and for
     * the specified {@link Class}. If another {@link Meter} with the
     * same name is already registered it will be returned instead.
     * 
     * @param clazz The {@link Class} for which the metric will be registered
     * @param meterName The name of the {@link Meter}
     * @return The created {@link Meter} or a pre-existing one
     */
    <T> Meter registerMeter(Class<T> clazz, String meterName);

    /**
     * Registers a new {@link Timer} with the specified name. If another
     * {@link Timer} with the same name is already registered it will be returned
     * instead.
     * 
     * @param timerName The {@link Timer} name
     * @return The created {@link Timer} or a pre-existing one
     */
    Timer registerTimer(String timerName);

    /**
     * Registers a new {@link Timer} with the specified name and for
     * the specified {@link Class}. If another {@link Timer} with the
     * same name and {@link Class} is already registered it will be
     * returned instead.
     * 
     * @param clazz The {@link Class} for which the metric will be registered
     * @param timerName The {@link Timer} name
     * @return The created {@link Timer} or a pre-existing one
     */
    <T> Timer registerTimer(Class<T> clazz, String timerName);

    /**
     * Registers the specified {@link Gauge} with the specified name.
     * If another {@link Gauge} with the same name is already registered
     * it will be returned instead.
     * 
     * @param gaugeName the {@link Gauge}'s name
     * @param gauge The {@link Gauge} or a pre-existing one
     */
    <T> void registerGauge(String gaugeName, Gauge<T> gauge);

    /**
     * Registers the specified {@link Gauge} with the specified name and {@link Class}
     * If another {@link Gauge} with the same name and {@link Class} is already registered
     * it will be returned instead.
     * 
     * @param clazz The {@link Class} for the {@link Gauge}
     * @param gaugeName the {@link Gauge}'s name
     * @param gauge The {@link Gauge} or a pre-existing one
     */
    <T, V> void registerGauge(Class<T> clazz, String gaugeName, Gauge<V> gauge);

    /**
     * Registers a new {@link Histogram} with the specified name.
     * If another {@link Histogram} with the same name is already registered
     * it will be returned instead.
     * 
     * @param histogramName the {@link Histogram}'s name
     * @return The created {@link Histogram} or a pre-existing one
     */
    Histogram registerHistogram(String histogramName);

    /**
     * Registers a new {@link Histogram} with the specified name and for
     * the specified {@link Class}. If another {@link Histogram} with the
     * same name and {@link Class} is already registered it will be returned
     * instead.
     * 
     * @param clazz The {@link Class} for which the metric will be registered
     * @param timerName The {@link Histogram} name
     * @return The created {@link Histogram} or a pre-existing one
     */
    <T> Histogram registerHistogram(Class<T> clazz, String histogramName);
}
