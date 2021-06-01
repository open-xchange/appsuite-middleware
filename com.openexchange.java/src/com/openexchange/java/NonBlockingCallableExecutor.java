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

package com.openexchange.java;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link NonBlockingCallableExecutor} - According to pattern introduced in <a
 * href="http://mailinator.blogspot.de/2007/05/readerwriter-in-java-in-nonblocking.html"
 * >http://mailinator.blogspot.de/2007/05/readerwriter-in-java-in-nonblocking.html</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class NonBlockingCallableExecutor<V> {

    private final AtomicInteger writeCounter;
    private final Lock lock;

    /**
     * Initializes a new {@link NonBlockingCallableExecutor}.
     */
    public NonBlockingCallableExecutor() {
        super();
        writeCounter = new AtomicInteger();
        lock = new ReentrantLock();
    }

    /**
     * Concurrent/non-exclusive computation of given task.
     *
     * @param task The task
     * @return The concurrently computed result
     * @throws Exception If unable to compute a result
     */
    public V readCall(Callable<V> task) throws Exception {
        int save = 0;
        V value = null;

        do {
            while (((save = writeCounter.get()) & 1) > 0) {
                ;
            }
            value = task.call();
        } while (save != writeCounter.get());

        return value;
    }

    /**
     * Exclusive computation of given task.
     *
     * @param task The task
     * @return The exclusively computed result.
     * @throws Exception If unable to compute a result
     */
    public V writeCall(Callable<V> task) throws Exception {
        lock.lock();
        try {
            return doWriteCall(task);
        } finally {
            lock.unlock();
        }
    }

    private V doWriteCall(Callable<V> task) throws Exception {
        writeCounter.incrementAndGet();
        try {
            return task.call();
        } finally {
            writeCounter.incrementAndGet();
        }
    }

}
