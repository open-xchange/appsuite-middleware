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

package com.openexchange.tools.timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link Timeout} - A simple timeout
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Timeout implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Timeout.class);

    private final Lock lock;

    private final Condition condition;

    private final Thread target;

    private long timeoutMillis;

    private final Thread watcher;

    private boolean loop;

    private boolean enabled;

    /**
     * Timeout for current thread
     *
     * @param timeoutMillis The timeout in milliseconds
     */
    public Timeout(final long timeoutMillis) {
        this(Thread.currentThread(), timeoutMillis);
    }

    /**
     * Timeout for given thread
     *
     * @param target The target thread to kill if timeout elapsed
     * @param timeoutMillis The timeout in milliseconds
     */
    public Timeout(final Thread target, final long timeoutMillis) {
        super();
        lock = new ReentrantLock();
        condition = lock.newCondition();
        this.target = target;
        this.timeoutMillis = timeoutMillis;
        enabled = true;
        watcher = new Thread(this);
        watcher.start();
    }

    /**
     * Tell the timeout that target has finished
     */
    public void done() {
        lock.lock();
        try {
            loop = false;
            enabled = false;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Restart the timeout from zero
     */
    public void reset() {
        lock.lock();
        try {
            loop = true;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Reset
     *
     * @param millis The new timeout in milliseconds
     */
    public void reset(final long timeoutMillis) {
        lock.lock();
        try {
            this.timeoutMillis = timeoutMillis;
            reset();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        lock.lock();
        try {
            final Thread current = Thread.currentThread();
            current.setPriority(Thread.MAX_PRIORITY);
            if (enabled) {
                do {
                    loop = false;
                    try {
                        condition.await(timeoutMillis, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                        Thread.currentThread().interrupt();
                        LOG.error("", e);
                    }
                } while (enabled && loop);
            }
            if (enabled && target.isAlive()) {
                LOG.info("Timeout.run(): Stopping thread {}", target.getName());
                target.interrupt();
            }
        } finally {
            lock.unlock();
        }
    }
}
