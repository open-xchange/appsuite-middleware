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
package com.openexchange.admin.taskmanagement;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * A FutureTask extended by Progress
 *
 * @author d7
 *
 * @param <V>
 */
public class ExtendedFutureTask<V> extends FutureTask<V> {

    private final Callable<V> callable;

    private final String typeofjob;

    private final String furtherinformation;

    protected final int id;

    protected final int cid;

    /**
     * Initializes a new {@link ExtendedFutureTask}.
     *
     * @param callable The callable
     * @param typeofjob The job type
     * @param furtherinformation Arbitrary information
     * @param id The job identifier
     * @param cid The context identifier
     */
    public ExtendedFutureTask(Callable<V> callable, String typeofjob, String furtherinformation, int id, int cid) {
        super(callable);
        this.callable = callable;
        this.typeofjob = typeofjob;
        this.furtherinformation = furtherinformation;
        this.id = id;
        this.cid = cid;
    }

    /**
     * Convenience method for detecting if a job runs
     *
     * @return
     */
    public boolean isRunning() {
        return (!isCancelled() && !isDone());
    }

    /**
     * Convenience method for detecting if a job failed
     *
     * @return
     */
    public boolean isFailed() {
        if (isDone()) {
            try {
                get();
            } catch (final InterruptedException e) {
                // Keep interrupted state
                Thread.currentThread().interrupt();
                return true;
            } catch (final ExecutionException e) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the progress percentage of the underlying job
     *
     * @return The progress in percent
     * @throws NoSuchMethodException If the job doesn't support this feature
     */
    public int getProgressPercentage() throws NoSuchMethodException {
        if (this.callable instanceof ProgressCallable) {
            return ((ProgressCallable<?>) this.callable).getProgressPercentage();
        }

        throw new NoSuchMethodException();
    }

    public final String getFurtherinformation() {
        return this.furtherinformation;
    }

    public final String getTypeofjob() {
        return this.typeofjob;
    }
}
