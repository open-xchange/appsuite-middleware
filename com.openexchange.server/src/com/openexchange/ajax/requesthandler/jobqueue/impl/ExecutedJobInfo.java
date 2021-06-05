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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.jobqueue.Job;
import com.openexchange.ajax.requesthandler.jobqueue.JobInfo;
import com.openexchange.exception.OXException;


/**
 * {@link ExecutedJobInfo}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ExecutedJobInfo implements JobInfo {

    private static final UUID EXECUTED_UUID = new UUID(-3199395759027697298L, -8091154291610420059L); //d3997682-1f43-496e-8fb6-720a61aefca5

    private final AJAXRequestResult result;
    private final Job job;

    /**
     * Initializes a new {@link ExecutedJobInfo}.
     */
    public ExecutedJobInfo(AJAXRequestResult result, Job job) {
        super();
        this.result = result;
        this.job = job;
    }

    @Override
    public UUID getId() {
        return EXECUTED_UUID;
    }

    @Override
    public Job getJob() {
        return job;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public AJAXRequestResult get(boolean removeAfterRetrieval) throws InterruptedException, OXException {
        return result;
    }

    @Override
    public AJAXRequestResult get(long timeout, TimeUnit unit, boolean removeAfterRetrieval) throws InterruptedException, OXException, TimeoutException {
        return result;
    }

}
