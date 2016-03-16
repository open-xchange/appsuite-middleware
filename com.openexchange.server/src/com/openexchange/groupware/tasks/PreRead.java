/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
            LOG.debug("Taking. {}", minimum);
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
            while (!preReaderFinished && elements.isEmpty()) {
                LOG.trace("Waiting for state.");
                try {
                    waitForPreReader.await();
                } catch (final InterruptedException e) {
                    // Nothing to do. Continue with normal work.
                    // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                    Thread.currentThread().interrupt();
                    LOG.trace("", e);
                }
            }
            return !elements.isEmpty();
        } finally {
            lock.unlock();
        }
    }
}
