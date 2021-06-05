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

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;

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
    private boolean enforceCorePoolSize;
    private boolean prestartAllCoreThreads;
    private int maximumPoolSize;
    private long keepAliveTime;
    private String workQueue;
    private int workQueueSize;
    private String refusedExecutionBehavior;
    private boolean blocking;
    private long watcherMinWaitTime;
    private long watcherMaxRunningTime;

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
    public ThreadPoolProperties init(ConfigurationService configurationService) {
        if (null != configurationService) {

            corePoolSize = 3;
            {
                final String tmp = configurationService.getProperty("com.openexchange.threadpool.corePoolSize");
                if (null != tmp) {
                    try {
                        corePoolSize = Integer.parseInt(tmp.trim());
                        if (corePoolSize < 0) {
                            corePoolSize = 3;
                        }
                    } catch (NumberFormatException e) {
                        corePoolSize = 3;
                    }
                }
            }

            enforceCorePoolSize = false;
            {
                String tmp = configurationService.getProperty("com.openexchange.threadpool.corePoolSize.enforce");
                if (null != tmp) {
                    enforceCorePoolSize = Boolean.parseBoolean(tmp.trim());
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
                        if (maximumPoolSize < 0) {
                            maximumPoolSize = Integer.MAX_VALUE;
                        }
                    } catch (NumberFormatException e) {
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
                        if (keepAliveTime < 0) {
                            keepAliveTime = 60000L;
                        }
                    } catch (NumberFormatException e) {
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
                        if (workQueueSize < 0) {
                            workQueueSize = 0;
                        }
                    } catch (NumberFormatException e) {
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
                    } catch (NumberFormatException e) {
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

            watcherMaxRunningTime = 60000L;
            {
                String tmp = configurationService.getProperty("com.openexchange.requestwatcher.maxRequestAge");
                if (null != tmp) {
                    try {
                        watcherMaxRunningTime = Integer.parseInt(tmp.trim());
                        if (watcherMaxRunningTime < 0) {
                            watcherMaxRunningTime = 60000L;
                        }
                    } catch (NumberFormatException e) {
                        watcherMaxRunningTime = 60000L;
                    }
                }
            }

            watcherMinWaitTime = 20000L;
            {
                String tmp = configurationService.getProperty("com.openexchange.requestwatcher.frequency");
                if (null != tmp) {
                    try {
                        watcherMinWaitTime = Integer.parseInt(tmp.trim());
                        if (watcherMinWaitTime < 0) {
                            watcherMinWaitTime = 20000L;
                        }
                    } catch (NumberFormatException e) {
                        watcherMinWaitTime = 20000L;
                    }
                }
            }
        } else {
            corePoolSize = 3;
            enforceCorePoolSize = false;
            prestartAllCoreThreads = true;
            maximumPoolSize = Integer.MAX_VALUE;
            keepAliveTime = 60000L;
            workQueue = "synchronous";
            refusedExecutionBehavior = "abort";
            watcherMaxRunningTime = 60000L;
            watcherMinWaitTime = 20000L;
        }
        org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ThreadPoolProperties.class);
        String ls = Strings.getLineSeparator();
        LOG.info("Thread Pool Configuration:\n\tcorePoolSize={}{}\tenforceCorePoolSize={}{}\tprestartAllCoreThreads={}{}\tkeepAliveTime={}sec{}\tworkQueue={}{}\trefusedExecutionBehavior={}", I(corePoolSize), ls, B(enforceCorePoolSize), ls, B(prestartAllCoreThreads), ls, L(keepAliveTime), ls, workQueue, ls, refusedExecutionBehavior);
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
     * Checks whether configured core pool size will be accepted regardless of the rule "Number of CPUs + 1".
     *
     * @return <code>true</code> if accepted as-is; otherwise <code>false</code>
     */
    public boolean isEnforceCorePoolSize() {
        return enforceCorePoolSize;
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

    /**
     * Gets the watcher's max. running time
     *
     * @return The max. running time
     */
    public long getWatcherMaxRunningTime() {
        return watcherMaxRunningTime;
    }

    /**
     * Gets the watcher's min. wait time
     *
     * @return The min. wait time
     */
    public long getWatcherMinWaitTime() {
        return watcherMinWaitTime;
    }

}
