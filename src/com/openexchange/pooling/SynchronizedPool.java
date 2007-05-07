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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



package com.openexchange.pooling;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the object pool.
 * TODO this implementation is currently not useable anymore. A lot of fixes
 * must be applied.
 * @param <T> type of pooled objects.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class SynchronizedPool<T> implements Pool<T>, Runnable {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(SynchronizedPool.class);

    /* --- Constants --- */

    /**
     * Possible actions if the pool is exhausted.
     */
    public static enum ExhaustedActions {
        /**
         * An error will be thrown if a maximum number of active objects is
         * defined and the pool has no more idle objects.
         */
        FAIL,

        /**
         * The thread will have to wait if a maximum number of active objects is
         * defined and the pool has no more idle objects. A maximum wait time
         * can be defined.
         */
        BLOCK,

        /**
         * Allthough a maximum number of active objects is defined the pool will
         * enlarge the number of pooled objects if they are needed.
         */
        GROW }

    /* --- Settings --- */

    private final int minIdle;

    private final int maxIdle;

    private final long maxIdleTime;

    private final int maxActive;

    private final long maxWait;

    private ExhaustedActions exhaustedAction;

    private final boolean testOnActivate;

    private final boolean testOnDeactivate;

    private final boolean testOnIdle;

    private final boolean testThreads;

    private boolean running = true;

    private int timeBetweenIdleRun = 60000;

    private final PoolImplData<T> data = new PoolImplData<T>();

    private final PoolableLifecycle<T> lifecycle;

    /**
     * The longest time an object has been used.
     */
    private long maxUseTime;

    /**
     * The shortest time an object has been used.
     */
    private long minUseTime = Long.MAX_VALUE;

    /**
     * Number of broken objects. An object is broken if
     * {@link PoolableLifecycle#activate(PooledData)},
     * {@link PoolableLifecycle#deactivate(PooledData)} or
     * {@link PoolableLifecycle#validate(PooledData)} return <code>false</code>.
     */
    private int numBroken;

    /**
     * Minimal constructor. Uses the default configuration of the pool.
     * @param lifecycle Implementation of the interface for handling the life
     * cycle of pooled objects.
     */
    public SynchronizedPool(final PoolableLifecycle<T> lifecycle) {
        this(lifecycle, DEFAULT_CONFIG);
    }

    /**
     * Default constructor.
     * @param lifecycle Implementation of the interface for handling the life
     * cycle of pooled objects.
     * @param config Configuration of the pool parameters.
     */
    public SynchronizedPool(final PoolableLifecycle<T> lifecycle,
        final Config config) {
        super();
        minIdle = config.minIdle;
        maxIdle = config.maxIdle;
        maxIdleTime = config.maxIdleTime;
        maxActive = config.maxActive;
        maxWait = config.maxWait;
        exhaustedAction = config.exhaustedAction;
        testOnActivate = config.testOnActivate;
        testOnDeactivate = config.testOnDeactivate;
        testOnIdle = config.testOnIdle;
        testThreads = config.testThreads;
        this.lifecycle = lifecycle;
        try {
            ensureMinIdle();
        } catch (PoolingException e) {
            LOG.error("Problem while creating initial objects.", e);
        }
    }

    /**
     * @return the exhaustedAction
     */
    public ExhaustedActions getExhaustedAction() {
        return exhaustedAction;
    }

    /**
     * @param exhaustedAction the exhaustedAction to set
     */
    public void setExhaustedAction(final ExhaustedActions exhaustedAction) {
        this.exhaustedAction = exhaustedAction;
    }

    /**
     * {@inheritDoc}
     */
    public void back(final T pooled) throws PoolingException {
        if (null == pooled) {
            throw new NullPointerException(
                "A null object was returned to pool.");
        }
        back(pooled, true);
    }

    /**
     * Puts an object into the pool.
     * @param pooled Object to pool.
     * @param decrementActive <code>true</code> if an active object is returned.
     * @throws PoolingException if the object does not belong to this pool.
     */
    private void back(final T pooled, final boolean decrementActive)
        throws PoolingException {
        // checks
        final PooledData <T> metaData;
        if (decrementActive) {
            metaData = data.getActive(pooled);
        } else {
            metaData = new PooledData<T>(pooled);
        }
        // object of this pool?
        if (null == metaData) {
            throw new PoolingException("Object does not belong to this pool.");
        }
        // reuseable?
        final boolean poolable;
        if (running && testOnDeactivate && !lifecycle.validate(metaData)) {
            poolable = false;
        } else {
            poolable = lifecycle.deactivate(metaData);
        }
        // update meta data
        if (testThreads) {
            synchronized (data) {
                data.removeByThread(metaData);
            }
        }
        metaData.resetTrace();
        metaData.touch();
        // return to pool
        boolean destroy = !poolable;
        synchronized (data) {
            if (decrementActive) {
                data.removeActive(metaData);
            }
            data.notifyAll();
            if (maxIdle > 0 && data.numIdle() >= maxIdle) {
                destroy = true;
            } else if (poolable) {
                data.addIdle(metaData);
            }
        }
        if (destroy) {
            lifecycle.destroy(metaData.getPooled());
        }
    }

    /**
     * {@inheritDoc}
     */
    public T get() throws PoolingException {
        final long startTime = System.currentTimeMillis();
        while (running) {
            PooledData<T> retval;
            boolean created = false;
            synchronized (data) {
                if (testThreads) {
                    final PooledData<T> other;
                    final Thread thread = Thread.currentThread();
                    other = data.getByThread(thread);
                    if (other != null && thread.equals(other.getThread())) {
                        final StringBuilder sb = new StringBuilder();
                        sb.append("Found thread using two objects: \n");
                        createStackTrace(sb, other.getTrace());
                        sb.append('\n');
                        createStackTrace(sb, thread.getStackTrace());
                        if (LOG.isDebugEnabled()) {
							LOG.debug(sb.toString());
						}
                    }
                }
                retval = data.popIdle();
                if (null == retval && maxActive > 0
                    && data.numActive() >= maxActive) {
                    // now we are getting in trouble. no more idle objects, a
                    // maximum number of active is defined and we reached this
                    // border.
                    switch (exhaustedAction) {
                    case GROW:
                        break;
                    case FAIL:
                        throw new PoolingException("Pool exhausted.");
                    case BLOCK:
                        try {
                            if (maxWait > 0) {
                                data.wait(getWaitTime(startTime));
                            } else {
                                data.wait();
                            }
                        } catch (InterruptedException e) {
                            data.notifyAll();
                        }
                        if (maxWait > 0 && getWaitTime(startTime) > maxWait) {
                            data.notifyAll();
                            throw new PoolingException("Wait time exceeded. "
                                + "Active: " + data.numActive() + ", Idle: "
                                + data.numIdle() + ", Waiting: "
                                + ", Time: " + getWaitTime(startTime));
                        }
                        continue;
                    default:
                        throw new IllegalStateException(
                            "Unknown exhausted action: " + exhaustedAction);
                    }
                }
                // create
                if (null == retval) {
                    final T pooled;
                    try {
                        pooled = lifecycle.create();
                    } catch (Exception e) {
//                        lock.lock();
//                        try {
                            data.notifyAll();
//                        } finally {
//                            lock.unlock();
//                        }
                        throw new PoolingException(
                            "Cannot create pooled object.", e);
                    }
                    retval = new PooledData<T>(pooled);
                    // maybe we add here more than maxActive objects. this happens
                    // if this thread successfully passed the to full check above
                    // and the create takes a long time while another thread got an
                    // idle object. we accept this because creation can take a long
                    // time.
//                    lock.lock();
//                    try {
                        data.addActive(retval);
//                    } finally {
//                        lock.unlock();
//                    }
                    created = true;
                }
            }
            // LifeCycle
            if (!lifecycle.activate(retval)
                || (testOnActivate && !lifecycle.validate(retval))) {
                synchronized (data) {
                    data.removeActive(retval);
                    data.notifyAll();
                }
                lifecycle.destroy(retval.getPooled());
                if (created) {
                    throw new PoolingException(
                        "Problem while creating new object.");
                }
				continue;
            }
            final Thread thread = Thread.currentThread();
            retval.setThread(thread);
            retval.setTrace(thread.getStackTrace());
            retval.touch();
            if (testThreads) {
                synchronized (data) {
                    data.addByThread(retval);
                }
            }
            return retval.getPooled();
        }
        throw new PoolingException("Pool has been stopped.");
    }

    private static final long getWaitTime(final long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * {@inheritDoc}
     */
    public void destroy() {
        running = false;
        cleaner.cancel();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        synchronized (data) {
            return data.isIdleEmpty() && data.isActiveEmpty();
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getNumIdle() {
        synchronized (data) {
            return data.numIdle();
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getNumActive() {
        synchronized (data) {
            return data.numActive();
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getPoolSize() {
        synchronized (data) {
            return data.numActive() + data.numIdle();
        }
    }

    /**
     * @return the number of threads waiting for an object.
     */
    public int numWaiting() {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    public long getMaxUseTime() {
        return maxUseTime;
    }

    /**
     * {@inheritDoc}
     */
    public long getMinUseTime() {
        return minUseTime;
    }

    /**
     * {@inheritDoc}
     */
    public int getNumBroken() {
        return numBroken;
    }

    /**
     * {@inheritDoc}
     */
    public void resetMaxUseTime() {
        maxUseTime = 0;
    }

    /**
     * {@inheritDoc}
     */
    public void resetMinUseTime() {
        minUseTime = Long.MAX_VALUE;
    }

    public void registerCleaner(final Timer timer) {
        timer.scheduleAtFixedRate(getCleanerTask(), timeBetweenIdleRun,
            timeBetweenIdleRun);
    }

    private final TimerTask cleaner = new TimerTask() {
        @Override
        public void run() {
            final Thread thread = new Thread(SynchronizedPool.this);
            thread.setName("PoolCleaner");
            thread.start();
        }
    };

    public TimerTask getCleanerTask() {
        return cleaner;
    }

    private boolean isBelowMinIdle() {
        final int numIdle;
        final int numActive;
        synchronized (data) {
            numIdle = data.numIdle();
            numActive = data.numActive();
        }
        final int maxCreate = Math.max(0, maxActive - numActive - numIdle);
        return Math.min(minIdle - numIdle, maxCreate) > 0;
    }

    private void ensureMinIdle() throws PoolingException {
        while (isBelowMinIdle()) {
            createObject();
        }
    }

    private void createObject() throws PoolingException {
        final T pooled;
        try {
            pooled = lifecycle.create();
        } catch (Exception e) {
            throw new PoolingException("Cannot create pooled object.", e);
        }
        back(pooled, false);
    }

    private void createStackTrace(final StringBuilder sb,
        final StackTraceElement[] trace) {
        for (StackTraceElement e : trace) {
            sb.append("\tat ");
            sb.append(e);
            sb.append('\n');
        }
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
    	if (LOG.isTraceEnabled()) {
    		LOG.trace("Starting cleaner run.");
    	}
        synchronized (data) {
            try {
            int idleSize = data.numIdle();
            boolean remove = true;
            for (int index = 0; remove && index < idleSize;) {
                final PooledData<T> metaData = data.getIdle(index);
                remove = (
                        // timeout
                        maxIdleTime > 0 && metaData.getTimeDiff() > maxIdleTime
                    ) || (
                        // object not valid anymore
                        testOnIdle && !(lifecycle.activate(metaData)
                        && lifecycle.validate(metaData)
                        && lifecycle.deactivate(metaData))
                    );
                if (remove) {
                    data.removeIdle(index);
                    idleSize = data.numIdle();
                    lifecycle.destroy(metaData.getPooled());
                    continue;
                } 
                index++;
            }
            ensureMinIdle();
            if (testThreads) {
                final Iterator<PooledData<T>> iter = data.listActive();
                while (iter.hasNext()) {
                    final PooledData<T> metaData = iter.next();
                    if (metaData.getTimeDiff() > maxIdleTime) {
                        final StringBuilder sb = new StringBuilder();
                        sb.append("Object was not returned. Fetched: ");
                        sb.append(metaData.getTimestamp());
                        sb.append('\n');
                        createStackTrace(sb, metaData.getTrace());
                        LOG.error(sb.toString());
                        iter.remove();
                        data.notifyAll();
                    }
                }
            }
        } catch (PoolingException e) {
            LOG.error("Housekeeping problem.", e);
        }
        }
        if (LOG.isTraceEnabled()) {
        	LOG.trace("Clean run ending.");
        }
    }

    public static class Config {
        /**
         * Default constructor.
         */
        public Config() {
            super();
        }
        public int minIdle;
        public int maxIdle;
        public long maxIdleTime;
        public int maxActive;
        public long maxWait;
        public ExhaustedActions exhaustedAction;
        public boolean testOnActivate;
        public boolean testOnDeactivate;
        public boolean testOnIdle;
        public boolean testThreads;
    }

    private static final Config DEFAULT_CONFIG;

    static {
        DEFAULT_CONFIG = new Config();
        DEFAULT_CONFIG.minIdle = 0;
        DEFAULT_CONFIG.maxIdle = -1;
        DEFAULT_CONFIG.maxIdleTime = 60000;
        DEFAULT_CONFIG.maxActive = -1;
        DEFAULT_CONFIG.maxWait = 10000;
        DEFAULT_CONFIG.exhaustedAction = SynchronizedPool.ExhaustedActions
            .GROW;
        DEFAULT_CONFIG.testOnActivate = true;
        DEFAULT_CONFIG.testOnDeactivate = true;
        DEFAULT_CONFIG.testOnIdle = false;
        DEFAULT_CONFIG.testThreads = false;
    }
}
