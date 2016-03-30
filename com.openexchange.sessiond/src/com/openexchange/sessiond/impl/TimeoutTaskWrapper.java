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

package com.openexchange.sessiond.impl;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link TimeoutTaskWrapper} - Simple wrapper to delegate task execution with respect to a timeout.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @param <V> The task's return type
 */
final class TimeoutTaskWrapper<V> extends AbstractTask<V> {

    /**
     * Submits given task to thread pool for execution using a wrapping {@code TimeoutTaskWrapper} instance
     *
     * @param task The task to submit
     */
    static <V> void submit(Task<V> task) {
        try {
            ThreadPools.getThreadPool().submit(new TimeoutTaskWrapper<V>(task));
        } catch (final RejectedExecutionException e) {
            ThreadPools.execute(task);
        }
    }

    // -------------------------------------------------------------------------------------------------------- //

    private final Task<V> task;
    private final V defaultValue;

    /**
     * Initializes a new {@link TimeoutTaskWrapper}.
     *
     * @param task The task to execute
     */
    TimeoutTaskWrapper(Task<V> task) {
        this(task, null);
    }

    /**
     * Initializes a new {@link TimeoutTaskWrapper}.
     *
     * @param task The task to execute
     * @param defaultValue The default value to return in case timeout will be exceeded
     */
    TimeoutTaskWrapper(Task<V> task, V defaultValue) {
        super();
        this.task = task;
        this.defaultValue = defaultValue;
    }

    @Override
    public V call() throws Exception {
        Future<V> f = ThreadPools.getThreadPool().submit(task);
        try {
            return f.get(SessionHandler.timeout(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            f.cancel(true);
            return defaultValue;
        } catch (ExecutionException e) {
            throw ThreadPools.launderThrowable(e, Exception.class);
        } catch (CancellationException e) {
            return defaultValue;
        }
    }

}
