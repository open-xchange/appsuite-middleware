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

package com.openexchange.groupware.tasks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements the queue of preread tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
final class PreRead<T> {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PreRead.class);

    /**
     * What is the minimum count of tasks for additional sub requests.
     */
    private static final int MINIMUM_PREREAD = 1;

    /**
     * Contains the tasks read by the thread.
     */
    private final Queue<T> elements = new LinkedList<T>();

    /**
     * Lock for the condition.
     */
    private final Lock lock = new ReentrantLock();

    /**
     * Condition for waiting for enough elements.
     */
    private final Condition waitForMinimum = lock.newCondition();

    /**
     * For blocking client so prereader is able to set state properly.
     */
    private final Condition waitForPreReader = lock.newCondition();

    /**
     * Did the pre reader finish?
     */
    private boolean preReaderFinished = false;

    /**
     * Default constructor.
     */
    PreRead() {
        super();
    }

    public void finished() {
        lock.lock();
        try {
            preReaderFinished = true;
            waitForPreReader.signal();
            waitForMinimum.signal();
            LOG.trace("Finished.");
        } finally {
            lock.unlock();
        }
    }

    public void offer(final T element) {
        lock.lock();
        try {
            elements.offer(element);
            waitForPreReader.signal();
            if (elements.size() >= MINIMUM_PREREAD) {
                waitForMinimum.signal();
            }
            LOG.trace("Offered. {}", elements.size());
        } finally {
            lock.unlock();
        }
    }

    public List<T> take(final boolean minimum) throws InterruptedException {
        final List<T> retval;
        lock.lock();
        try {
            LOG.debug("Taking. {}", minimum ? Boolean.TRUE : Boolean.FALSE);
            if (minimum && elements.size() < MINIMUM_PREREAD
                && !preReaderFinished) {
                LOG.debug("Waiting for enough.");
                waitForMinimum.await();
            }
            if (elements.isEmpty()) {
                throw new NoSuchElementException();
            }
            retval = new ArrayList<T>(elements.size());
            retval.addAll(elements);
            elements.clear();
            LOG.trace("Taken.");
        } finally {
            lock.unlock();
        }
        return retval;
    }

    public boolean hasNext() {
        lock.lock();
        try {
            try {
                while (!preReaderFinished && elements.isEmpty()) {
                    LOG.trace("Waiting for state.");
                    waitForPreReader.await();
                }
            } catch (InterruptedException e) {
                // Nothing to do. Continue with normal work.
                // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                Thread.currentThread().interrupt();
                LOG.trace("", e);
            }
            return !elements.isEmpty();
        } finally {
            lock.unlock();
        }
    }
}
