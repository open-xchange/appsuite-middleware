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

package com.openexchange.groupware.update.tools;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.groupware.tasks.mapping.Status;
import com.openexchange.java.util.UUIDs;

/**
 * {@link UpdateTaskToolkitJob}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class UpdateTaskToolkitJob<V> extends FutureTask<V> {

    private final String jobId;
    private final CountDownLatch latch;
    private final AtomicReference<String> info;

    /**
     * Initializes a new {@link UpdateTaskToolkitJob}.
     */
    public UpdateTaskToolkitJob(Callable<V> job, AtomicReference<String> info) {
        super(job);
        this.jobId = UUIDs.getUnformattedString(UUID.randomUUID());
        this.info = info;
        this.latch = new CountDownLatch(1);
    }

    @Override
    public void run() {
        // Await permit
        try {
            latch.await();
        } catch (@SuppressWarnings("unused") InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        // Do run...
        super.run();
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the job identifier
     *
     * @return The identifier
     */
    public String getId() {
        return jobId;
    }

    /**
     * Gets the job's status text
     *
     * @return The {@link Status} text
     */
    public String getStatusText() {
        return info.get();
    }

    /**
     * Signals to start processing for the associated job
     */
    public void start() {
        latch.countDown();
    }

}
