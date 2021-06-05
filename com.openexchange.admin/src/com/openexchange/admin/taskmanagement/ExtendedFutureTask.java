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
            } catch (InterruptedException e) {
                // Keep interrupted state
                Thread.currentThread().interrupt();
                return true;
            } catch (ExecutionException e) {
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
