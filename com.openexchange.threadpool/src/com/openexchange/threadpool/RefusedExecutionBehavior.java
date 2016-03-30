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

package com.openexchange.threadpool;

import java.util.concurrent.RejectedExecutionException;

/**
 * {@link RefusedExecutionBehavior} - The behavior for tasks that cannot be executed by a {@link ThreadPoolService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface RefusedExecutionBehavior<V> {

    /**
     * The result constant representing a discarded task.
     * <p>
     * This constant is supposed to be returned by {@link #refusedExecution(Task, ThreadPoolService)} to signal that task has been
     * discarded.
     */
    public static final Object DISCARDED = new Object();

    /**
     * Method that may be invoked by a {@link ThreadPoolService} when it cannot accept a task. This may occur when no more threads or queue
     * slots are available because their bounds would be exceeded, or upon shutdown of the thread pool. In the absence of other
     * alternatives, the method may throw an unchecked {@link RejectedExecutionException}, which will be propagated to the caller of
     * <tt>submit()</tt>.
     *
     * @param task The task requested to be executed
     * @param threadPool The thread pool attempting to execute this task
     * @return Task's result or {@link RefusedExecutionBehavior#DISCARDED DISCARDED} constant if task has been discarded
     * @throws Exception If task execution fails
     * @throws RejectedExecutionException If there is no remedy
     */
    V refusedExecution(Task<V> task, ThreadPoolService threadPool) throws Exception;

}
