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

package com.openexchange.processing.internal;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link BoundedRoundRobinProcessor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class BoundedRoundRobinProcessor extends RoundRobinProcessor {

    private final int maxTasks;
    private final AtomicInteger numberOfPendingTasks;

    /**
     * Initializes a new {@link BoundedRoundRobinProcessor}.
     */
    public BoundedRoundRobinProcessor(String name, int numThreads, int maxTasks) {
        super(name, numThreads);
        if (maxTasks <= 0) {
            throw new IllegalArgumentException("maxTask must not be equal to/less than zero");
        }
        this.maxTasks = maxTasks;
        numberOfPendingTasks = new AtomicInteger();
    }

    @Override
    protected boolean allowNewTask(Runnable task) {
        int count;
        do {
            count = numberOfPendingTasks.get();
            if (count >= maxTasks) {
                // Limitation exceeded... Denied
                return false;
            }
        } while (!numberOfPendingTasks.compareAndSet(count, count + 1));

        // Allowed...
        return true;
    }

    @Override
    protected void handleFailedTaskOffer(Runnable task) {
        numberOfPendingTasks.decrementAndGet();
    }

    @Override
    protected Runnable getNextTaskFrom(TaskManager manager) {
        Runnable task = super.getNextTaskFrom(manager);
        if (null != task) {
            numberOfPendingTasks.decrementAndGet();
        }
        return task;
    }

}
