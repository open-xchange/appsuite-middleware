/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
