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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link CustomThreadFactory} - A thread factory taking a custom name prefix for created threads.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CustomThreadFactory implements java.util.concurrent.ThreadFactory {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CustomThreadFactory.class);

    private final AtomicInteger threadNumber;
    private final String namePrefix;
    private final ExecutorService threadCreatorService;

    /**
     * Initializes a new {@link CustomThreadFactory}.
     *
     * @param namePrefix The name prefix
     */
    public CustomThreadFactory(final String namePrefix) {
        super();
        threadNumber = new AtomicInteger();
        this.namePrefix = namePrefix;
        threadCreatorService = Executors.newSingleThreadExecutor(new MasterThreadFactory(namePrefix));
    }

    @Override
    public CustomThread newThread(Runnable r) {
        // Ensure a positive thread number
        int threadNum;
        while ((threadNum = threadNumber.incrementAndGet()) <= 0) {
            if (threadNumber.compareAndSet(threadNum, 1)) {
                threadNum = 1;
            } else {
                threadNum = threadNumber.incrementAndGet();
            }
        }
        try {
            return createThreadWithMaster(r, threadNum);
        } catch (InterruptedException e) {
            LOG.error("Single thread pool for creating threads was interrupted.", e);
            return null;
        } catch (ExecutionException e) {
            LOG.error("Single thread pool for creating threads catched an exception while creating one.", e);
            return null;
        }
    }

    private static String getThreadName(int threadNumber, String namePrefix) {
        StringBuilder retval = new StringBuilder(namePrefix.length() + 7);
        retval.append(namePrefix);
        for (int i = threadNumber; i < 1000000; i *= 10) {
            retval.append('0');
        }
        retval.append(threadNumber);
        return retval.toString();
    }

    private CustomThread createThreadWithMaster(Runnable r, int threadNum) throws InterruptedException, ExecutionException {
        ThreadCreateCallable callable = new ThreadCreateCallable(r, getThreadName(threadNum, namePrefix));
        Future<CustomThread> future = threadCreatorService.submit(callable);
        return future.get();
    }
}
