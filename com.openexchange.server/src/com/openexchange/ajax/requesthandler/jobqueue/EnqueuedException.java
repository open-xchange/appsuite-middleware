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

package com.openexchange.ajax.requesthandler.jobqueue;


/**
 * {@link EnqueuedException} - Special exception thrown when invoking {@link JobQueueService#enqueueAndWait(Job, long, java.util.concurrent.TimeUnit)} and wait time is exceeded.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class EnqueuedException extends Exception {

    private static final long serialVersionUID = 1727453323646225156L;

    private final JobInfo jobInfo;

    /**
     * Initializes a new {@link EnqueuedException}.
     *
     * @param jobInfo The enqueued job info
     */
    public EnqueuedException(JobInfo jobInfo) {
        super();
        this.jobInfo = jobInfo;
    }

    /**
     * Initializes a new {@link EnqueuedException}.
     *
     * @param jobInfo The enqueued job info
     * @param cause The initial cause for this exception
     */
    public EnqueuedException(JobInfo jobInfo, Throwable cause) {
        super(cause);
        this.jobInfo = jobInfo;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    /**
     * Gets the job info
     *
     * @return The job info
     */
    public JobInfo getJobInfo() {
        return jobInfo;
    }

}
