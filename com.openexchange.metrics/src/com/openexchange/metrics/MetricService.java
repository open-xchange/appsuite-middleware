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

import com.openexchange.metrics.jmx.MetricServiceListener;
import com.openexchange.metrics.types.Counter;
import com.openexchange.metrics.types.Gauge;
import com.openexchange.metrics.types.Histogram;
import com.openexchange.metrics.types.Meter;
import com.openexchange.metrics.types.Metric;
import com.openexchange.metrics.types.Timer;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link MetricService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
@SingletonService
public interface MetricService {

    /**
     * Gets an existing {@link Histogram} for the specified {@link MetricDescriptor} or creates and
     * remembers a new one if it doesn't exist yet.
     *
     * @param descriptior The {@link MetricDescriptor}
     * @return the metric instance
     */
    Histogram getHistogram(MetricDescriptor descriptor);

    /**
     * Gets an existing {@link Timer} for the specified {@link MetricDescriptor} or creates and
     * remembers a new one if it doesn't exist yet.
     *
     * @param descriptior The {@link MetricDescriptor}
     * @return the metric instance
     */
    Timer getTimer(MetricDescriptor descriptor);

    /**
     * Gets an existing {@link Counter} for the specified {@link MetricDescriptor} or creates and
     * remembers a new one if it doesn't exist yet.
     *
     * @param descriptior The {@link MetricDescriptor}
     * @return the metric instance
     */
    Counter getCounter(MetricDescriptor descriptor);

    /**
     * Gets an existing {@link Gauge} for the specified {@link MetricDescriptor} with the specified {@link MetricSupplier}
     * or creates and remembers a new one if it doesn't exist yet.
     *
     * @param descriptior The {@link MetricDescriptor}
     * @return the metric instance
     */
    <T> Gauge<T> getGauge(MetricDescriptor descriptor);

    /**
     * Gets an existing {@link Meter} for the specified {@link MetricDescriptor} or creates and
     * remembers a new one if it doesn't exist yet.
     *
     * @param descriptior The {@link MetricDescriptor}
     * @return the metric instance
     */
    Meter getMeter(MetricDescriptor descriptor);

    /**
     * Removes the {@link Metric} defined with the specified {@link MetricDescriptor}
     * 
     * @param descriptor The {@link MetricDescriptor}
     */
    void removeMetric(MetricDescriptor descriptor);

    /**
     * Adds a {@link MetricServiceListener} to a collection of listeners that will be notified on
     * metric creation. Listeners will be notified in the order in which they are added.
     * <p/>
     * <b>N.B.:</b> The listener will be notified of all existing metrics when it first registers.
     *
     * @param listener the listener that will be notified
     */
    void addListener(MetricServiceListener listener);

    /**
     * Removes a {@link MetricServiceListener} from this registry's collection of listeners.
     *
     * @param listener the listener that will be removed
     */
    void removeListener(MetricServiceListener listener);
}
