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

package com.openexchange.continuation;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;

/**
 * {@link Continuation} - Represents a continuing/background AJAX request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public interface Continuation<V> extends Serializable {

    /**
     * Gets the UUID.
     *
     * @return The UUID
     */
    UUID getUuid();

    /**
     * Attempts to cancel execution of this continuation.
     * <p>
     * This attempt will fail if the continuation has already completed, has already been cancelled, or could not be cancelled for some
     * other reason. If successful, and this continuation has not started when <tt>cancel</tt> is called, this continuation should never
     * run. If the continuation has already started, then the <tt>mayInterruptIfRunning</tt> parameter determines whether the thread
     * executing this task should be interrupted in an attempt to stop the task.
     *
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this continuation should be interrupted; otherwise, in-progress
     *            continuations are allowed to complete
     */
    void cancel(boolean mayInterruptIfRunning);

    /**
     * Gets the next available value.
     *
     * @param time The maximum time to wait
     * @param unit The time unit of the {@code time} argument
     * @return The next available value or <code>null</code>
     * @throws OXException If awaiting next available response fails
     * @throws InterruptedException If the current thread is interrupted
     */
    ContinuationResponse<V> getNextResponse(long time, TimeUnit unit) throws OXException, InterruptedException;

    /**
     * Gets the next available value.
     *
     * @param time The maximum time to wait
     * @param unit The time unit of the {@code time} argument
     * @param defaultValue The default response to return if no next value was available in given time span
     * @return The next available value or given <code>defaultValue</code>
     * @throws OXException If awaiting next available response fails
     * @throws InterruptedException If the current thread is interrupted
     */
    ContinuationResponse<V> getNextResponse(long time, TimeUnit unit, V defaultResponse) throws OXException, InterruptedException;

}
