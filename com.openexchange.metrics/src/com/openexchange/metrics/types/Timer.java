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

package com.openexchange.metrics.types;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * {@link Timer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface Timer extends Metric {

    /**
     * Adds a recorded duration.
     *
     * @param duration the length of the duration
     * @param unit the scale unit of {@code duration}
     */
    void update(long duration, TimeUnit timeUnit);

    /**
     * Times and records the duration of event.
     *
     * @param event a {@link Callable} whose {@link Callable#call()} method implements a process
     *            whose duration should be timed
     * @param <T> the type of the value returned by {@code event}
     * @return the value returned by {@code event}
     * @throws Exception if an error is occurred during the execution of the {@code event}s
     */
    <T> T time(Callable<T> event) throws Exception;

    /**
     * Times and records the duration of event. This method is useful where the event may throw
     * a certain catched exception, that is to be re-thrown immediately. Consider the following
     * code pattern:
     *
     * <pre>
     * void perform() throws OXException {
     * // prepare work here...
     * boolean result = metrics.getTimer(desc).time(() -> {
     * return innerPerform(input);
     * });
     * // act on result here...
     * }
     *
     * boolean innerPerform(Object input) throws OXException {
     * // produce boolean result or throw exception
     * }
     * </pre>
     */
    <T, E extends Exception> T time(Timeable<T, E> timeable) throws E;

    /**
     * Times and records the duration of event. Should not throw exceptions, for that use the
     * {@link #time(Callable)} method.
     *
     * @param event a {@link Supplier} whose {@link Supplier#get()} method implements a process
     *            whose duration should be timed
     * @param <T> the type of the value returned by {@code event}
     * @return the value returned by {@code event}
     */
    <T> T timeSupplier(Supplier<T> event);

    /**
     * Times and records the duration of event.
     *
     * @param event a {@link Runnable} whose {@link Runnable#run()} method implements a process
     *            whose duration should be timed
     */
    void time(Runnable event);

    /**
     * Returns the number of events which have been marked.
     *
     * @return the number of events which have been marked
     */
    long getCount();

    /**
     * Returns the fifteen-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     * 
     * @return the fifteen-minute exponentially-weighted moving average rate at which events have
     *         occurred since the meter was created
     */
    double getFifteenMinuteRate();

    /**
     * Returns the five-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     *
     * @return the five-minute exponentially-weighted moving average rate at which events have
     *         occurred since the meter was created
     */
    double getFiveMinuteRate();

    /**
     * Returns the mean rate at which events have occurred since the meter was created.
     *
     * @return the mean rate at which events have occurred since the meter was created
     */
    double getMeanRate();

    /**
     * Returns the one-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     *
     * @return the one-minute exponentially-weighted moving average rate at which events have
     *         occurred since the meter was created
     */
    double getOneMinuteRate();
}
