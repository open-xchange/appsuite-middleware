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

package com.openexchange.metrics.jmx.beans;

import com.openexchange.management.MBeanMethodAnnotation;
import com.openexchange.metrics.types.Meter;

/**
 * {@link MeterMBean}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface MeterMBean extends MetricMBean {

    /**
     * Returns the number of events which have been marked.
     *
     * @return the number of events which have been marked
     */
    @MBeanMethodAnnotation(description = "Returns the number of events which have been marked.", parameterDescriptions = { "" }, parameters = { "" })
    long getCount();

    /**
     * Returns the mean rate at which events have occurred since the meter was created.
     *
     * @return the mean rate at which events have occurred since the meter was created
     */
    @MBeanMethodAnnotation(description = "Returns the mean rate at which events have occurred since the meter was created.", parameterDescriptions = { "" }, parameters = { "" })
    double getMeanRate();

    /**
     * Returns the one-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     * <p/>
     * This rate has the same exponential decay factor as the one-minute load average in the
     * <code>top</code> Unix command.
     *
     * @return the one-minute exponentially-weighted moving average rate at which events have
     *         occurred since the meter was created
     */
    @MBeanMethodAnnotation(description = "Returns the one-minute exponentially-weighted moving average rate at which events have occurred since the meter was created. This rate has the same exponential decay factor as the one-minute load average in the top Unix command.", parameterDescriptions = { "" }, parameters = { "" })
    double getOneMinuteRate();

    /**
     * Returns the five-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     * <p/>
     * This rate has the same exponential decay factor as the five-minute load average in the
     * <code>top</code> Unix command.
     *
     * @return the five-minute exponentially-weighted moving average rate at which events have
     *         occurred since the meter was created
     */
    @MBeanMethodAnnotation(description = "Returns the five-minute exponentially-weighted moving average rate at which events have occurred since the meter was created. This rate has the same exponential decay factor as the five-minute load average in the top Unix command.", parameterDescriptions = { "" }, parameters = { "" })
    double getFiveMinuteRate();

    /**
     * Returns the fifteen-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     * <p/>
     * This rate has the same exponential decay factor as the fifteen-minute load average in the
     * {@code top} Unix command.
     *
     * @return the fifteen-minute exponentially-weighted moving average rate at which events have
     *         occurred since the meter was created
     */
    @MBeanMethodAnnotation(description = "Returns the fifteen-minute exponentially-weighted moving average rate at which events have occurred since the meter was created. This rate has the same exponential decay factor as the fifteen-minute load average in the top Unix command.", parameterDescriptions = { "" }, parameters = { "" })
    double getFifteenMinuteRate();

    /**
     * Returns the rate unit of the {@link Meter}
     * 
     * @return the rate unit of the {@link Meter}
     */
    @MBeanMethodAnnotation(description = "Returns the rate unit of the meter", parameterDescriptions = { "" }, parameters = { "" })
    String getRateUnit();
}
