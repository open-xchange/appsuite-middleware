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

package com.openexchange.threadpool.internal;

import com.openexchange.config.ConfigurationService;

/**
 * {@link ThreadPoolProperties} - Initialization of thread pool bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
/**
 * {@link ThreadPoolProperties}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadPoolProperties {

    private int corePoolSize;

    private boolean prestartAllCoreThreads;

    private int maximumPoolSize;

    private long keepAliveTime;

    private String workQueue;

    private int workQueueSize;

    private String refusedExecutionBehavior;

    private boolean blocking;

    /**
     * Initializes a new {@link ThreadPoolProperties}.
     */
    public ThreadPoolProperties() {
        super();
    }

    /**
     * Initializes this properties with given configuration service.
     *
     * @param configurationService The configuration service to use
     * @return This instance with properties applied from configuration service
     */
    public ThreadPoolProperties init(final ConfigurationService configurationService) {
        if (null != configurationService) {

            corePoolSize = 3;
            {
                final String tmp = configurationService.getProperty("com.openexchange.threadpool.corePoolSize");
                if (null != tmp) {
                    try {
                        corePoolSize = Integer.parseInt(tmp.trim());
                    } catch (final NumberFormatException e) {
                        corePoolSize = 3;
                    }
                }
            }

            prestartAllCoreThreads = true;
            {
                final String tmp = configurationService.getProperty("com.openexchange.threadpool.prestartAllCoreThreads");
                if (null != tmp) {
                    prestartAllCoreThreads = Boolean.parseBoolean(tmp.trim());
                }
            }

            maximumPoolSize = Integer.MAX_VALUE;
            {
                final String tmp = configurationService.getProperty("com.openexchange.threadpool.maximumPoolSize");
                if (null != tmp) {
                    try {
                        maximumPoolSize = Integer.parseInt(tmp.trim());
                    } catch (final NumberFormatException e) {
                        maximumPoolSize = Integer.MAX_VALUE;
                    }
                }
            }

            keepAliveTime = 60000L;
            {
                final String tmp = configurationService.getProperty("com.openexchange.threadpool.keepAliveTime");
                if (null != tmp) {
                    try {
                        keepAliveTime = Long.parseLong(tmp.trim());
                    } catch (final NumberFormatException e) {
                        keepAliveTime = 60000L;
                    }
                }
            }

            workQueueSize = 0;
            {
                final String tmp = configurationService.getProperty("com.openexchange.threadpool.workQueueSize");
                if (null != tmp) {
                    try {
                        workQueueSize = Integer.parseInt(tmp.trim());
                    } catch (final NumberFormatException e) {
                        workQueueSize = 0;
                    }
                }
            }

            if (workQueueSize <= 0) {
                workQueue = "synchronous";
                {
                    final String tmp = configurationService.getProperty("com.openexchange.threadpool.workQueue");
                    if (null != tmp) {
                        workQueue = tmp.trim();
                    }
                }
            } else {
                /*
                 * Set to "linked" if workQueueSize > 0
                 */
                workQueue = "linked";
            }

            blocking = false;
            {
                final String tmp = configurationService.getProperty("com.openexchange.threadpool.blocking");
                if (null != tmp) {
                    try {
                        blocking = Boolean.parseBoolean(tmp.trim());
                    } catch (final NumberFormatException e) {
                        blocking = false;
                    }
                }
            }

            refusedExecutionBehavior = "abort";
            {
                final String tmp = configurationService.getProperty("com.openexchange.threadpool.refusedExecutionBehavior");
                if (null != tmp) {
                    refusedExecutionBehavior = tmp.trim();
                }
            }
        } else {
            corePoolSize = 3;
            prestartAllCoreThreads = true;
            maximumPoolSize = Integer.MAX_VALUE;
            keepAliveTime = 60000L;
            workQueue = "synchronous";
            refusedExecutionBehavior = "abort";
        }
        final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ThreadPoolProperties.class);
        final String ls = System.getProperty("line.separator");
        LOG.info("Thread Pool Configuration:\n\tcorePoolSize={}{}\tprestartAllCoreThreads={}{}\tkeepAliveTime={}sec{}\tworkQueue={}{}\trefusedExecutionBehavior={}", corePoolSize, ls, prestartAllCoreThreads, ls, keepAliveTime, ls, workQueue, ls, refusedExecutionBehavior);
        return this;
    }

    /**
     * Gets the corePoolSize
     *
     * @return The corePoolSize
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * Gets the prestartAllCoreThreads
     *
     * @return The prestartAllCoreThreads
     */
    public boolean isPrestartAllCoreThreads() {
        return prestartAllCoreThreads;
    }

    /**
     * Gets the maximumPoolSize
     *
     * @return The maximumPoolSize
     */
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * Gets the keepAliveTime
     *
     * @return The keepAliveTime
     */
    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    /**
     * Gets the workQueue
     *
     * @return The workQueue
     */
    public String getWorkQueue() {
        return workQueue;
    }

    /**
     * Gets the work queue size.
     * <p>
     * Note: If size of work queue is set to a value greater than zero, {@link #getWorkQueue()} is implicitly set to <code>"linked"</code>
     * to accomplish a bounded work queue.
     *
     * @return The work queue size
     */
    public int getWorkQueueSize() {
        return workQueueSize;
    }

    /**
     * Whether a blocking behavior should be applied.
     *
     * @return <code>true</code> if a blocking behavior should be applied; otherwsie <code>false</code>
     */
    public boolean isBlocking() {
        return blocking;
    }

    /**
     * Gets the refusedExecutionBehavior
     *
     * @return The refusedExecutionBehavior
     */
    public String getRefusedExecutionBehavior() {
        return refusedExecutionBehavior;
    }

}
