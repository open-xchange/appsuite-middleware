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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.ajp13.coyote.ActionCode;
import com.openexchange.ajp13.coyote.AjpProcessor;
import com.openexchange.ajp13.najp.AJPv13ServerImpl;
import com.openexchange.ajp13.najp.AJPv13TaskMonitor;
import com.openexchange.ajp13.watcher.AJPv13TaskWatcher;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link CoyoteTask} - The coyote task.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CoyoteTask implements Task<Object> {

    /**
     * The client socket.
     */
    private final Socket client;

    /**
     * The AJP processor.
     */
    private final AjpProcessor ajpProcessor;

    /**
     * The task watcher.
     */
    private final AJPv13TaskWatcher watcher;

    private final AJPv13TaskMonitor listenerMonitor;

    /**
     * Initializes a new {@link AjpProcessorRunnable}.
     *
     * @param client The accepted client socket
     * @param ajpProcessor The AJP processor dedicated to the socket
     */
    protected CoyoteTask(final Socket client, final AjpProcessor ajpProcessor, final AJPv13TaskMonitor listenerMonitor, final AJPv13TaskWatcher watcher) {
        this.client = client;
        this.ajpProcessor = ajpProcessor;
        this.watcher = watcher;
        this.listenerMonitor = listenerMonitor;
    }

    /**
     * Gets the associated AJP processor.
     *
     * @return The AJP processor
     */
    public AjpProcessor getAjpProcessor() {
        return ajpProcessor;
    }

    @Override
    public void setThreadName(final ThreadRenamer threadRenamer) {
        threadRenamer.renamePrefix("AJP-Processor");
    }

    /**
     * The atomic integer to count active AJP tasks.
     */
    private static final AtomicInteger numRunning = new AtomicInteger();

    /**
     * Increments/decrements the number of running AJP tasks.
     *
     * @param increment whether to increment or to decrement
     */
    private static void changeNumberOfRunningAJPTasks(final boolean increment) {
        MonitoringInfo.setNumberOfRunningAJPListeners(increment ? numRunning.incrementAndGet() : numRunning.decrementAndGet());
    }

    @Override
    public void beforeExecute(final Thread t) {
        watcher.addTask(ajpProcessor);
        ajpProcessor.startKeepAlivePing();
        listenerMonitor.incrementNumActive();
        changeNumberOfRunningAJPTasks(true);
    }

    @Override
    public void afterExecute(final Throwable t) {
        ajpProcessor.stopKeepAlivePing();
        watcher.removeTask(ajpProcessor);
        changeNumberOfRunningAJPTasks(false);
        listenerMonitor.decrementNumActive();
    }

    @Override
    public Object call() throws Exception {
        try {
            ajpProcessor.action(ActionCode.START, null);
            ajpProcessor.process(client);
        } catch(final java.net.SocketException e) {
            // SocketExceptions are normal
            CoyoteSocketHandler.LOG.debug(e.getMessage(), e);
        } catch (final java.io.IOException e) {
            // IOExceptions are normal
            CoyoteSocketHandler.LOG.debug(e.getMessage(), e);
        } catch (final Throwable e) {
            /*
             * Any other exception or error is odd.
             */
            CoyoteSocketHandler.LOG.error(e.getMessage(), e);
        } finally {
            ajpProcessor.action(ActionCode.STOP, null);
            AJPv13ServerImpl.decrementNumberOfOpenAJPSockets();
        }
        return null;
    }

} // End of AjpProcessorTask class
