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

package com.openexchange.threadpool.internal;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

/**
 * {@link BoundedExecutor} - Accomplishes the saturation policy to make execute block when the work queue is full.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BoundedExecutor {

    private final Executor executor;
    private final Semaphore semaphore;

    /**
     * Initializes a new {@link BoundedExecutor}.
     *
     * @param executor The executor to delegate execution to
     * @param bound The capacity boundary; actually the pool size plus the number of queued tasks you want to allow
     */
    public BoundedExecutor(Executor executor, int bound) {
        super();
        this.executor = executor;
        this.semaphore = new Semaphore(bound);
    }

    /**
     * Submits specified task to executor; waits if no queue space or worker thread is immediately available.
     *
     * @param command The command to submit
     * @throws InterruptedException If interrupted while waiting for queue space or thread to become available
     * @throws RejectedExecutionException If given command cannot be accepted for execution.
     */
    public void submitTask(Runnable command) throws InterruptedException {
        if (null == command) {
            return;
        }
        Semaphore semaphore = this.semaphore;
        semaphore.acquire();
        try {
            executor.execute(new SemaphoredRunnable(semaphore, command));
        } catch (final RejectedExecutionException e) {
            semaphore.release();
            throw e;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------

    private static final class SemaphoredRunnable implements Runnable {

        private final Semaphore semaphore;
        private final Runnable command;

        SemaphoredRunnable(Semaphore semaphore, Runnable command) {
            super();
            this.semaphore = semaphore;
            this.command = command;
        }

        @Override
        public void run() {
            try {
                command.run();
            } finally {
                semaphore.release();
            }
        }
    }

}
