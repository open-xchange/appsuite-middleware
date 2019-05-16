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

package com.openexchange.push.impl.jobqueue;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushUser;

/**
 * {@link PermanentListenerJob} - A job scheduled for starting a permanent listener.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface PermanentListenerJob extends Comparable<PermanentListenerJob> {

    /**
     * Gets the associated push user.
     *
     * @return The push user
     */
    PushUser getPushUser();

    /**
     * Checks if this job completed.
     * <p>
     * Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return
     * <code>true</code>.
     *
     * @return <code>true</code> if this job completed; otherwise <code>false</code>
     */
    boolean isDone();

    /**
     * Waits if necessary for the established push listener, and then returns it.
     *
     * @return The established push listener
     * @throws CancellationException If the establishing a push listener was cancelled
     * @throws ExecutionException If the establishing a push listener threw an exception
     * @throws InterruptedException If the current thread was interrupted while waiting
     */
    PushListener get() throws InterruptedException, ExecutionException;

    /**
     * Waits if necessary for at most the given time for the established push listener, and then returns it, if available.
     *
     * @param timeout The maximum time to wait
     * @param unit The time unit of the timeout argument
     * @return The established push listener
     * @throws CancellationException If the establishing a push listener was cancelled
     * @throws ExecutionException If the establishing a push listener threw an exception
     * @throws InterruptedException If the current thread was interrupted while waiting
     * @throws TimeoutException If the wait timed out
     */
    PushListener get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

}
