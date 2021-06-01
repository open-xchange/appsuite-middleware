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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

/**
 * {@link ProcessorThreadPoolExecutor} - The thread pool for a processor.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ProcessorThreadPoolExecutor extends ThreadPoolExecutor {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ProcessorThreadPoolExecutor.class);

    /**
     * Initializes a new {@link ProcessorThreadPoolExecutor}.
     *
     * @param name The name prefix to use for associated threads
     * @param nThreads The number of threads in the pool
     * @param supportMDC Whether to support MDC log properties; otherwise MDC will be cleared prior to each log output
     */
    public ProcessorThreadPoolExecutor(String name, int nThreads, boolean supportMDC) {
        // See java.util.concurrent.Executors.newFixedThreadPool(int, ThreadFactory)
        super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ProcessorThreadFactory(name, supportMDC));
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t != null && r instanceof RoundRobinProcessor.Selector) {
            LOG.info("Processor thread '{}' terminated abruptly.", Thread.currentThread().getName(), t);
        }
    }

}
