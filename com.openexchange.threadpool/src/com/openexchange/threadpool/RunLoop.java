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
        isRunning=true;
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
            try {
                handleLock.lock();
                while(isPaused.get()) {
                    proceedCondition.await();
                }
                /*
                 * Element could have been removed while RunLoop was paused
                 */
                E currentElement = this.currentElementReference.get();
                if(null != currentElement) {
                    handle(currentElement);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.info("Returning from RunLoop due to interruption");
                isRunning=false;
                return;
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                LOG.error("", t);
            } finally {
                // Do not prevent GC of last handled element
                currentElementReference.set(null);
                handleLock.unlock();
            }
        }
        LOG.info("Leaving run loop");
        Thread.currentThread().setName(name + "-stopped");
    }

    /**
     * Offer an element to this RunLoop.
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
     * @throws InterruptedException
     */
    protected void continueHandling() {
          try {
              handleLock.lock();
              isPaused.set(false);
              proceedCondition.signalAll();
          } finally {
              handleLock.unlock();
          }
    }

    /**
     * Check if the RunLoop is running
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
     * @return the name of this RunLoop
     */
    public String getName() {
        return name;
    }

    /**
     * Get the number of Elements being enqueued in this {@link RunLoop}.
     * @return the number of Elements that are currently enqueued.
     */
    public int getQueueSize() {
        return queue.size();
    }

    protected abstract void handle(E element);

    /**
     * Unblock the potentially blocked internal {@link BlockingQueue} by inserting a NoOp element.
     */
    protected abstract void unblock();

}
