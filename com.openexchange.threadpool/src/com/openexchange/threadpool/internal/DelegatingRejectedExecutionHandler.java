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

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import com.openexchange.threadpool.RefusedExecutionBehavior;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link DelegatingRejectedExecutionHandler} - Delegates to either default handler or task's individual {@link RefusedExecutionBehavior
 * behavior}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DelegatingRejectedExecutionHandler implements RejectedExecutionHandler {

    private final RejectedExecutionHandler defaultHandler;

    private final ThreadPoolService threadPool;

    /**
     * Initializes a new {@link DelegatingRejectedExecutionHandler}.
     *
     * @param defaultBehavior The default behavior for refused tasks
     * @param threadPool The thread pool
     */
    public DelegatingRejectedExecutionHandler(final RejectedExecutionHandler defaultHandler, final ThreadPoolService threadPool) {
        super();
        this.defaultHandler = defaultHandler;
        this.threadPool = threadPool;
    }

    @Override
    public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
        if (r instanceof CustomFutureTask<?>) {
            // Perform task's handler or default if null
            final CustomFutureTask<?> cft = (CustomFutureTask<?>) r;
            try {
                handleTask(cft);
            } catch (RejectedExecutionException e) {
                // No remedy
                throw e;
            } catch (Exception e) {
                // Signal failed execution
                cft.setException(e);
            }
        }
        defaultHandler.rejectedExecution(r, executor);
    }

    private <V> void handleTask(final CustomFutureTask<V> cft) throws Exception {
        final RefusedExecutionBehavior<V> reb = cft.getRefusedExecutionBehavior();
        if (null != reb) {
            final V result = reb.refusedExecution(cft.getTask(), threadPool);
            if (RefusedExecutionBehavior.DISCARDED == result) {
                /*
                 * TODO: What to do on discarded task? If cft's set() method is never invoked (either through innerRun() or innerSet()), a
                 * call to get() will block forever.
                 */
            } else {
                cft.set(result);
            }
        }
    }

}
