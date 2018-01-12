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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.requesthandler.jobqueue.impl;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.google.common.cache.Cache;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.jobqueue.Job;
import com.openexchange.ajax.requesthandler.jobqueue.JobInfo;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link FutureJobInfo}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class FutureJobInfo implements JobInfo {

    private final UUID id;
    private final Job job;
    private final Future<AJAXRequestResult> future;
    private final Cache<UUID, FutureJobInfo> userJobs;
    private final long stamp;

    /**
     * Initializes a new {@link FutureJobInfo}.
     */
    public FutureJobInfo(UUID id, Job job, Future<AJAXRequestResult> future, Cache<UUID, FutureJobInfo> userJobs) {
        super();
        this.job = job;
        this.id = id;
        this.future = future;
        this.userJobs = userJobs;
        this.stamp = System.currentTimeMillis();
    }

    /**
     * Checks if this future job info lasts in queue longer than specified number of milliseconds.
     *
     * @param maxIdleMillis The max. number of milliseconds, which might be exceeded
     * @return <code>true</code> if specified number of milliseconds is exceeded; otherwise <code>false</code>
     */
    public boolean isExceeded(long maxIdleMillis) {
        return (System.currentTimeMillis() - stamp) > maxIdleMillis;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Job getJob() {
        return job;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean canceled = future.cancel(mayInterruptIfRunning);
        userJobs.invalidate(id);
        return canceled;
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public AJAXRequestResult get(boolean removeAfterRetrieval) throws InterruptedException, OXException {
        try {
            if (!removeAfterRetrieval) {
                return future.get();
            }

            AJAXRequestResult ajaxRequestResult = future.get();
            userJobs.invalidate(id);
            return ajaxRequestResult;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(cause, cause.getMessage());
        }
    }

    @Override
    public AJAXRequestResult get(long timeout, TimeUnit unit, boolean removeAfterRetrieval) throws InterruptedException, OXException, TimeoutException {
        try {
            if (!removeAfterRetrieval) {
                return future.get(timeout, unit);
            }

            AJAXRequestResult ajaxRequestResult = future.get(timeout, unit);
            userJobs.invalidate(id);
            return ajaxRequestResult;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(cause, cause.getMessage());
        }
    }

}
