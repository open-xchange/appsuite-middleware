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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajp13.coyote.sockethandler;

import java.net.Socket;
import javax.management.NotCompliantMBeanException;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.IAJPv13SocketHandler;
import com.openexchange.ajp13.Services;
import com.openexchange.ajp13.coyote.AjpProcessor;
import com.openexchange.ajp13.coyote.Constants;
import com.openexchange.ajp13.monitoring.AJPv13Monitors;
import com.openexchange.ajp13.watcher.AJPv13TaskMonitor;
import com.openexchange.ajp13.watcher.AJPv13TaskWatcher;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link CoyoteSocketHandler} - Handles accepted client sockets by {@link #handleSocket(Socket)} which hands-off to a dedicated AJP
 * processor.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CoyoteSocketHandler implements IAJPv13SocketHandler {

    protected static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CoyoteSocketHandler.class));

    /**
     * The atomic boolean to track started status.
     */
    private volatile boolean started;

    /**
     * The AJP task monitor for JMX interface.
     */
    private final AJPv13TaskMonitor listenerMonitor;

    /**
     * The refused execution behavior initialized on start-up.
     */
    private CoyoteRefusedExecutionBehavior behavior;

    /**
     * The AJP task watcher initialized on start-up.
     */
    private AJPv13TaskWatcher watcher;

    /**
     * The thread pool service used to submit AJP tasks for execution.
     */
    private ThreadPoolService pool;

    /**
     * The socket read timeout.
     */
    private final int readTimeout;

    /**
     * The socket connection linger.
     */
    private final int linger;

    /**
     * Whether e secure connection is enforced.
     */
    private final boolean forceHttps;

    /**
     * Initializes a new {@link CoyoteSocketHandler}.
     */
    public CoyoteSocketHandler() {
        super();
        final ConfigurationService configurationService = Services.getService(ConfigurationService.class);
        if (configurationService == null) {
            forceHttps = false;
        } else {
            forceHttps = configurationService.getBoolProperty(ServerConfig.Property.FORCE_HTTPS.getPropertyName(), false);
        }
        AJPv13TaskMonitor tmp = null;
        try {
            tmp = new AJPv13TaskMonitor();
        } catch (final NotCompliantMBeanException e) {
            LOG.error(e.getMessage(), e);
        }
        listenerMonitor = tmp;
        readTimeout = AJPv13Config.getAJPListenerReadTimeout();
        // TODO: Configure linger
        linger = Constants.DEFAULT_CONNECTION_LINGER;
    }

    /**
     * Starts this pool that creates new threads as needed, but will reuse previously constructed threads when they are available.
     */
    @Override
    public void startUp() {
        if (!started) {
            synchronized (this) {
                if (!started) {
                    pool = ThreadPools.getThreadPool();
                    watcher = new AJPv13TaskWatcher(pool);
                    behavior = new CoyoteRefusedExecutionBehavior(watcher);
                    AJPv13Monitors.setListenerMonitor(listenerMonitor);
                    started = true;
                }
            }
        }
    }

    /**
     * Attempts to stop all actively executing tasks and halts the processing of waiting tasks.
     */
    @Override
    public void shutDownNow() {
        if (started) {
            synchronized (this) {
                if (started) {
                    try {
                        watcher.stop();
                        // pool.shutdownNow();
                    } finally {
                        watcher = null;
                        pool = null;
                        behavior = null;
                        AJPv13Monitors.releaseListenerMonitor();
                        started = false;
                    }
                }
            }
        }
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted. Invocation has no
     * additional effect if already shut down.
     */
    @Override
    public void shutDown() {
        if (started) {
            synchronized (this) {
                if (started) {
                    try {
                        watcher.stop();
                        // pool.shutdownNow();
                    } finally {
                        watcher = null;
                        pool = null;
                        behavior = null;
                        AJPv13Monitors.releaseListenerMonitor();
                        started = false;
                    }
                }
            }
        }

        // try {
        // pool.shutdown();
        // pool.awaitTermination(10L, TimeUnit.SECONDS);
        // } catch (final InterruptedException e) {
        // // Restore interrupted flag for borrowed thread if not already set
        // if (!Thread.currentThread().isInterrupted()) {
        // Thread.currentThread().interrupt();
        // }
        // } finally {
        // watcher.stop();
        // watcher = null;
        // behavior = null;
        // pool = null;
        // AJPv13Monitors.releaseListenerMonitor();
        // }
    }

    /**
     * Checks if this socket handler has been shut down or has not been started.
     *
     * @return <code>true</code> if this socket handler has been shut down or has not been started; otherwise <code>false</code>
     */
    @Override
    public boolean isShutdown() {
        if (!started) {
            return true;
        }
        return (null == watcher);
    }

    /**
     * Handles given client socket.
     *
     * @param client The client socket to handle
     */
    @Override
    public void handleSocket(final Socket client) {
        final AjpProcessor ajpProcessor = new AjpProcessor(Constants.MAX_PACKET_SIZE, listenerMonitor, forceHttps);
        if (readTimeout > 0) {
            ajpProcessor.setKeepAliveTimeout(readTimeout);
        }
        final CoyoteTask coyoteTask = new CoyoteTask(client, ajpProcessor, listenerMonitor, watcher);
        ajpProcessor.setControl(pool.submit(coyoteTask, behavior));
        coyoteTask.open();
    }

}
