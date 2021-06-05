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

package com.openexchange.threadpool.internal;

import java.util.concurrent.RejectedExecutionHandler;

/**
 * {@link RejectedExecutionType} - The rejected execution type.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum RejectedExecutionType {

    /**
     * Aborts execution by throwing an appropriate exception to the caller.
     */
    ABORT("abort", new CustomThreadPoolExecutor.AbortPolicy()),
    /**
     * The caller is considered to run the task if thread pool is unable to do so.
     */
    CALLER_RUNS("caller-runs", new CustomThreadPoolExecutor.CallerRunsPolicy()),
    /**
     * The task is silently discarded. No exception is thrown.
     */
    DISCARD("discard", new CustomThreadPoolExecutor.DiscardPolicy());

    private final String identifier;

    private final RejectedExecutionHandler handler;

    private RejectedExecutionType(final String identifier, final RejectedExecutionHandler handler) {
        this.identifier = identifier;
        this.handler = handler;
    }

    public RejectedExecutionHandler getHandler() {
        return handler;
    }

    /**
     * Gets the rejected execution type for given identifier.
     *
     * @param identifier The identifier
     * @return The rejected execution type for given identifier or <code>null</code>
     */
    public static RejectedExecutionType getRejectedExecutionType(final String identifier) {
        final RejectedExecutionType[] types = RejectedExecutionType.values();
        for (final RejectedExecutionType rejectedExecutionType : types) {
            if (rejectedExecutionType.identifier.equalsIgnoreCase(identifier)) {
                return rejectedExecutionType;
            }
        }
        return null;
    }
}
