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

package com.openexchange.metrics.types;

/**
 * <p>{@link Meter} measures the rate at which a set of events occur.</p>
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface Meter extends Metric {

    /**
     * Mark the occurrence of an event.
     */
    void mark();

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n the number of events
     */
    void mark(long n);

    /**
     * Returns the number of events which have been marked.
     *
     * @return the number of events which have been marked
     */
    long getCount();

    /**
     * Returns the one-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     * 
     * @return the one-minute exponentially-weighted moving average rate at which events have
     *         occurred since the meter was created
     */
    double getOneMinuteRate();

    /**
     * Returns the five-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     *
     * @return the five-minute exponentially-weighted moving average rate at which events have
     *         occurred since the meter was created
     */
    double getFiveMinuteRate();

    /**
     * Returns the fifteen-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     *
     * @return the fifteen-minute exponentially-weighted moving average rate at which events have
     *         occurred since the meter was created
     */
    double getFifteenMinuteRate();

    /**
     * Returns the mean rate at which events have occurred since the meter was created.
     *
     * @return the mean rate at which events have occurred since the meter was created
     */
    double getMeanRate();
}
