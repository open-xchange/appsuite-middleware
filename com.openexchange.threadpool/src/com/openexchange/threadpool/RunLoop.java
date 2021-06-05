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

package com.openexchange.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.osgi.ExceptionUtils;

/**
 * {@link RunLoop}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public abstract class RunLoop<E> implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RunLoop.class);

    protected final BlockingQueue<E> queue = new LinkedBlockingDeque<E>();

    private final String name;

    /** Is the RunLoop currently paused */
    private final AtomicBoolean isPaused = new AtomicBoolean();

    private final Lock handleLock = new ReentrantLock();

    /** The condition to await before the run-loop continues processing */
    private final Condition proceedCondition = handleLock.newCondition();

    /** Reference for the element we have just taken from the queue for handling */
    protected final AtomicReference<E> currentElementReference = new AtomicReference<E>();

    private volatile boolean isRunning = false;

    /**
     * Initializes a new {@link RunLoop}.
     *
     * @param name The name to set
     */
    protected RunLoop(String name) {
        super();
        this.name = name;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(name);
        isRunning = true;
        while (isRunning) {
            /*
             * Get the current element from the queue. blocking, so this must be done outside the handleLock
             */
            try {
                currentElementReference.set(queue.take());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                LOG.info("Returning from RunLoop due to interruption");
                return;
            }

            /*
             * Try to handle the element if the RunLoop isn't paused
             */
            handleLock.lock();
            try {
                try {
                    while (isPaused.get()) {
                        proceedCondition.await();
                    }
                    /*
                     * Element could have been removed while RunLoop was paused
                     */
                    E currentElement = currentElementReference.get();
                    if (null != currentElement) {
                        handle(currentElement);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.info("Returning from RunLoop due to interruption");
                    isRunning = false;
                    return;
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    LOG.error("", t);
                } finally {
                    // Do not prevent GC for last handled element
                    currentElementReference.set(null);
                }
            } finally {
                handleLock.unlock();
            }
        }
        LOG.info("Leaving run loop");
        Thread.currentThread().setName(name + "-stopped");
    }

    /**
     * Offer an element to this RunLoop.
     * 
     * @param element The element to offer
     * @return false if the Runloop was paused or is out of capacity
     */
    public boolean offer(final E element) {
        return this.queue.offer(element);
    }

    /**
     * Causes the Runloop to pause until {@link RunLoop#continueHandling()} is called again.
     *
     * @throws InterruptedException
     */
    protected void pauseHandling() {
        isPaused.set(true);
    }

    /**
     * Causes the Runloop to continue handling offered Elements after {@link RunLoop#pauseHandling()} was called.
     * 
     * @throws InterruptedException
     */
    protected void continueHandling() {
        handleLock.lock();
        try {
            isPaused.set(false);
            proceedCondition.signalAll();
        } finally {
            handleLock.unlock();
        }
    }

    /**
     * Check if the RunLoop is running
     * 
     * @return true if the RunLoop is running, else false
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Stop the {@link RunLoop} by ending the while loop and poisoning the internal {@link BlockingQueue}.
     */
    public void stop() {
        isRunning = false;
        unblock();
    }

    /**
     * Get the name of this RunLoop
     * 
     * @return the name of this RunLoop
     */
    public String getName() {
        return name;
    }

    /**
     * Get the current number of Elements being enqueued in this {@link RunLoop}.
     * 
     * @return the number of Elements that are currently enqueued.
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Handles specified element.
     *
     * @param element The element to handle
     */
    protected abstract void handle(E element);

    /**
     * Unblock the potentially blocked internal {@link BlockingQueue} by inserting a NoOp element.
     */
    protected abstract void unblock();

}
