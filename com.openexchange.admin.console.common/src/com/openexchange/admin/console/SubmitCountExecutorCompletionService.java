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

package com.openexchange.admin.console;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link SubmitCountExecutorCompletionService} - Extends {@code ExecutorCompletionService} by counting submitted tasks; retrievable via
 * {@link #getSubmitCount()}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SubmitCountExecutorCompletionService<V> extends ExecutorCompletionService<V> {

    /** Counter for submitted tasks */
    private final AtomicInteger count;

    /**
     * Initializes a new {@link SubmitCountExecutorCompletionService}.
     *
     * @param executor The executor
     */
    public SubmitCountExecutorCompletionService(final Executor executor) {
        super(executor);
        count = new AtomicInteger(0);
    }

    @Override
    public Future<V> submit(final Callable<V> task) {
        final Future<V> submit = super.submit(task);
        count.incrementAndGet();
        return submit;
    }

    @Override
    public Future<V> submit(final Runnable task, final V result) {
        final Future<V> submit = super.submit(task, result);
        count.incrementAndGet();
        return submit;
    }

    /**
     * Gets the count of submitted tasks.
     *
     * @return The task count.
     */
    public int getSubmitCount() {
        return count.get();
    }

}
