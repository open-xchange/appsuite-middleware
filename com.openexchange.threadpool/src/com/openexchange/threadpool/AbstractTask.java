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

/**
 * {@link AbstractTask} - An abstract {@link Task} which leaves {@link #afterExecute(Throwable)}, {@link #beforeExecute(Thread)}, and
 * {@link #setThreadName(ThreadRenamer)} empty.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractTask<V> implements Task<V> {

    /**
     * Initializes a new {@link AbstractTask}.
     */
    protected AbstractTask() {
        super();
    }

    /**
     * Executes this task with current thread.
     *
     * @return The task's return value or <code>null</code> if an {@code Exception} occurred (in that {@link #afterExecute(Throwable)} is
     *         invoked with a non-<code>null</code> {@code Throwable} reference)
     */
    public V execute() {
        final Thread currentThread = Thread.currentThread();
        if (!(currentThread instanceof ThreadRenamer)) {
            return innerExecute(currentThread);
        }
        // Current thread supports ThreadRenamer
        final String name = currentThread.getName();
        setThreadName((ThreadRenamer) currentThread);
        try {
            return innerExecute(currentThread);
        } finally {
            currentThread.setName(name);
        }
    }

    /**
     * Execute with respect to <code>beforeExecute()</code> and <code>afterExecute()</code> methods
     *
     * @param currentThread The current thread
     * @return The return value or <code>null</code>
     */
    protected V innerExecute(final Thread currentThread) {
        V retval = null;
        boolean ran = false;
        beforeExecute(currentThread);
        try {
            retval = call();
            ran = true;
            afterExecute(null);
        } catch (final Exception ex) {
            if (!ran) {
                afterExecute(ex);
            }
            // Else the exception occurred within
            // afterExecute itself in which case we don't
            // want to call it again.
        }
        return retval;
    }

    @Override
    public void afterExecute(final Throwable throwable) {
        // NOP
    }

    @Override
    public void beforeExecute(final Thread thread) {
        // NOP
    }

    @Override
    public void setThreadName(final ThreadRenamer threadRenamer) {
        // NOP
    }

}
