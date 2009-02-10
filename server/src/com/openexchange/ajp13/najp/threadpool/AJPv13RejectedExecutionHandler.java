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

package com.openexchange.ajp13.najp.threadpool;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajp13.AJPv13Response;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.najp.AJPv13Task;
import com.openexchange.ajp13.najp.AJPv13TaskWatcher;
import com.openexchange.tools.servlet.http.HttpServletResponseWrapper;

/**
 * {@link AJPv13RejectedExecutionHandler} - A handler for rejected AJP tasks.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class AJPv13RejectedExecutionHandler implements RejectedExecutionHandler {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13RejectedExecutionHandler.class);

    private final AJPv13TaskWatcher watcher;

    /**
     * Initializes a new {@link AJPv13RejectedExecutionHandler}.
     * 
     * @param watcher The task watcher to remove excess tasks from
     */
    public AJPv13RejectedExecutionHandler(final AJPv13TaskWatcher watcher) {
        super();
        this.watcher = watcher;
    }

    public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
        if (executor.isShutdown()) {
            // Proper logging
            LOG.error(
                "AJP task cannot be executed since thread pool has been shut down. Please restart AJP module.",
                new RejectedExecutionException());
        } else {
            // Still running, but task rejected (by synchronous queue if used)
            if (r instanceof AJPv13TaskWatcher.WatcherFutureTask) {
                // Uncomment this to run in calling thread.
                // r.run();
                final AJPv13TaskWatcher.WatcherFutureTask watcherFutureTask = (AJPv13TaskWatcher.WatcherFutureTask) r;
                try {
                    final AJPv13Task rejectedTask = watcherFutureTask.getAjpTask();
                    final Socket client = rejectedTask.getSocket();
                    if (null != client) {
                        LOG.error(new StringBuilder(64).append("Rejected AJP request from: \"").append(client.getRemoteSocketAddress()).append(
                            "\". Trying to terminate AJP cycle").toString());
                        try {
                            final HttpServletResponseWrapper response = new HttpServletResponseWrapper(null);
                            final byte[] errMsg = response.composeAndSetError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, null);
                            // Write headers
                            client.getOutputStream().write(AJPv13Response.getSendHeadersBytes(response));
                            client.getOutputStream().flush();
                            // Write error message
                            client.getOutputStream().write(AJPv13Response.getSendBodyChunkBytes(errMsg));
                            client.getOutputStream().flush();
                            // Write END-RESPONSE
                            client.getOutputStream().write(AJPv13Response.getEndResponseBytes(true));
                            client.getOutputStream().flush();
                            // Close socket since Web Server does not reliably close the socket even though END-RRESPONSE indicates to
                            // close the connection.
                            rejectedTask.cancel();
                            LOG.info(new StringBuilder("AJP cycle terminated. Closed connection to \"").append(
                                client.getRemoteSocketAddress()).append('"').toString());
                        } catch (final AJPv13Exception e) {
                            LOG.error(
                                new StringBuilder(64).append("Could not terminate AJP cycle: ").append(e.getMessage()).toString(),
                                e);
                        } catch (final IOException e) {
                            LOG.error(
                                new StringBuilder(64).append("Could not terminate AJP cycle: ").append(e.getMessage()).toString(),
                                e);
                        }
                    }
                } finally {
                    watcher.removeListener(watcherFutureTask);
                }
            } else {
                // Huh..?! No AJP task? Then run in calling thread
                r.run();
            }
        }
    }

}
