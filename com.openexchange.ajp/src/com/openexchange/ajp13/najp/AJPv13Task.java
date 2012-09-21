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

package com.openexchange.ajp13.najp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13Connection;
import com.openexchange.ajp13.AJPv13RequestHandler;
import com.openexchange.ajp13.AJPv13Response;
import com.openexchange.ajp13.AJPv13Server;
import com.openexchange.ajp13.AJPv13ServiceRegistry;
import com.openexchange.ajp13.AbstractAJPv13Request;
import com.openexchange.ajp13.BlockableBufferedOutputStream;
import com.openexchange.ajp13.exception.AJPv13BrokenCycleException;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.exception.AJPv13SocketClosedException;
import com.openexchange.ajp13.exception.AJPv13TimeoutException;
import com.openexchange.ajp13.servlet.http.HttpServletResponseWrapper;
import com.openexchange.ajp13.watcher.AJPv13TaskWatcher;
import com.openexchange.exception.OXException;
import com.openexchange.log.Log;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.servlet.UploadServletException;

/**
 * {@link AJPv13Task} - Processes an accepted client socket until either executing thread is interrupted or assigned socket is closed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13Task implements Task<Object>, com.openexchange.ajp13.watcher.Task {

    private static final StackTraceElement[] EMPTY_STACK = new StackTraceElement[0];

    /**
     * The logger constant.
     */
    protected static final org.apache.commons.logging.Log LOG = Log.valueOf(com.openexchange.log.LogFactory.getLog(AJPv13Task.class));

    /**
     * Whether debug log level is enabled.
     */
    protected static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    /**
     * Creates a new {@link AJPv13Task} instance.
     *
     * @param client The client socket to process
     * @param listenerMonitor The listener monitor
     * @param watcher The task watcher
     * @return A newly created AJP task
     */
    public static AJPv13Task newAJPTask(final Socket client, final AJPv13TaskMonitor listenerMonitor, final AJPv13TaskWatcher watcher) {
        return new AJPv13Task(client, listenerMonitor, watcher);
    }

    /**
     * The atomic integer to count active AJP tasks.
     */
    private static final AtomicInteger numRunning = new AtomicInteger();

    /**
     * The counter to generate a (temporary) unique number for each AJP task.
     */
    private static final AtomicLong COUNTER = new AtomicLong();

    /**
     * The accepted AJP client socket.
     */
    private volatile Socket client;

    /**
     * Flag to set processing status; meaning thread currently processes received AJP data.
     */
    private volatile boolean processing;

    /**
     * The start time stamp.
     */
    private volatile long processingStart;

    /**
     * Flag to set waiting-on-socket status: meaning thread entered blocking <i><code>read()</code></i> method.
     */
    private volatile boolean waitingOnAJPSocket;

    /**
     * The borrowed thread.
     */
    private volatile Thread thread;

    /**
     * The currently used AJP connection.
     */
    private volatile AJPv13ConnectionImpl ajpConnection;

    /**
     * The listener monitor
     */
    private final AJPv13TaskMonitor listenerMonitor;

    /**
     * Whether this task is long-running.
     */
    private volatile boolean longRunning;

    /**
     * The (temporary) unique task number.
     */
    private final Long num;

    /**
     * The task watcher reference.
     */
    private final AJPv13TaskWatcher watcher;

    /**
     * Control for AJP task.
     */
    private volatile Future<Object> control;

    /**
     * The scheduled keep-alive task.
     */
    private volatile ScheduledTimerTask scheduledKeepAliveTask;

    /**
     * Initializes a new {@link AJPv13Task}.
     */
    private AJPv13Task(final Socket client, final AJPv13TaskMonitor listenerMonitor, final AJPv13TaskWatcher watcher) {
        super();
        num = Long.valueOf(COUNTER.incrementAndGet());
        this.client = client;
        this.listenerMonitor = listenerMonitor;
        this.watcher = watcher;
    }

    /**
     * Sets the control for this AJP task.
     *
     * @param control The control
     */
    public void setControl(final Future<Object> control) {
        this.control = control;
    }

    /**
     * Gets the sequential task number.
     *
     * @return The sequential task number
     */
    @Override
    public Long getNum() {
        return num;
    }

    /**
     * Cancels this AJP task; meaning to close the client socket and to stop its execution.
     */
    @Override
    public void cancel() {
        final Socket s = client;
        if (null != s) {
            try {
                closeQuitely(s);
            } finally {
                client = null;
            }
        }
        final Future<Object> f = control;
        if (f != null) {
            f.cancel(false);
            control = null;
        }
    }

    /**
     * Gets the client socket bound to this task.
     *
     * @return The client socket bound to this task.
     */
    public Socket getSocket() {
        return client;
    }

    /**
     * Sets this task's processing flag
     */
    void markProcessing() {
        processing = true;
        processingStart = System.currentTimeMillis();
        waitingOnAJPSocket = false;
        listenerMonitor.incrementNumProcessing();
    }

    /**
     * Mark this task as non-processing
     */
    void markNonProcessing() {
        waitingOnAJPSocket = true;
        if (processing) {
            processing = false;
            listenerMonitor.decrementNumProcessing();
        }
    }

    /**
     * Increments number of AJP tasks waiting for incoming AJP data.
     */
    void incrementWaiting() {
        listenerMonitor.incrementNumWaiting();
    }

    /**
     * Decrements number of AJP tasks waiting for incoming AJP data.
     */
    void decrementWaiting() {
        listenerMonitor.decrementNumWaiting();
    }

    /**
     * @return <code>true</code> if task is currently listening to client socket's input stream, otherwise <code>false</code>
     */
    @Override
    public boolean isWaitingOnAJPSocket() {
        return waitingOnAJPSocket;
    }

    /**
     * @return <code>true</code> if task is currently processing, otherwise <code>false</code>
     */
    @Override
    public boolean isProcessing() {
        return processing;
    }

    /**
     * @return The processing start time stamp
     */
    @Override
    public long getProcessingStartTime() {
        return processingStart;
    }

    @Override
    public long getLastWriteAccess() {
        return ajpConnection.getLastWriteAccess();
    }

    /**
     * Checks if this task is long-running.
     *
     * @return <code>true</code> if this task is long-running; otherwise <code>false</code>
     */
    @Override
    public boolean isLongRunning() {
        return longRunning;
    }

    /**
     * Sets if this task is long-running.
     *
     * @param longRunning <code>true</code> if this task is long-running; otherwise <code>false</code>
     */
    void setLongRunning(final boolean longRunning) {
        this.longRunning = longRunning;
    }

    /**
     * Gets currently executing thread's stack trace.
     *
     * @return The currently executing thread's stack trace or an empty stack trace if no thread processes this task.
     */
    @Override
    public StackTraceElement[] getStackTrace() {
        if (null == thread) {
            return EMPTY_STACK;
        }
        return thread.getStackTrace();
    }

    @Override
    public Thread getThread() {
        return thread;
    }

    /**
     * Gets currently executing thread's name.
     *
     * @return The currently executing thread's name or an empty string if no threads processes this task.
     */
    @Override
    public String getThreadName() {
        if (null == thread) {
            return "";
        }
        return thread.getName();
    }

    /**
     * Gets the currently used AJP connection.
     *
     * @return The currently used AJP connection or <code>null</code> if none in use.
     */
    AJPv13ConnectionImpl getAJPConnection() {
        return ajpConnection;
    }

    /**
     * Processes an accepted client socket for its complete lifetime. Incoming AJP cycles are delegated to a dedicated
     * {@link AJPv13Connection}.
     * <p>
     * The client socket is closed, when executing thread leaves this <code>run()</code> method.
     */
    @Override
    public Object call() {
        final Thread t = thread = Thread.currentThread();
        if (!t.isInterrupted() && client != null && !client.isClosed()) {
            if (LogProperties.isEnabled()) {
                /*
                 * Gather logging info
                 */
                final Props properties = LogProperties.getLogProperties();
                properties.put("com.openexchange.ajp13.threadName", t.getName());
                properties.put("com.openexchange.ajp13.remotePort", Integer.valueOf(client.getPort()));
                properties.put("com.openexchange.ajp13.remoteAddress", client.getInetAddress().getHostAddress());
            }
            final long start = System.currentTimeMillis();
            /*
             * Assign a connection to this listener
             */
            final AJPv13ConnectionImpl ajpCon;
            try {
                ajpCon = new AJPv13ConnectionImpl(this);
            } catch (final AJPv13Exception e) {
                LOG.error(e.getMessage(), e);
                terminateAndClose(false, null);
                return null;
            }
            boolean closeOrderly = true;
            ajpConnection = ajpCon;
            final AJPv13TaskMonitor monitor = listenerMonitor;
            try {
                client.setKeepAlive(true);
                /*
                 * Keep on processing underlying stream's data as long as accepted client socket is alive, its input is not shut down and no
                 * communication failure occurred.
                 */
                do {
                    try {
                        ajpCon.processRequest();
                        ajpCon.createResponse();
                        if (!ajpCon.getAjpRequestHandler().isEndResponseSent()) {
                            LOG.warn("Detected AJP cycle without terminating END_RESPONSE package.");
                            /*
                             * Just for safety reason to ensure END_RESPONSE package is going to be sent.
                             */
                            writeEndResponse(client, false);
                        }
                    } catch (final UploadServletException e) {
                        /*
                         * Log ServletException's own root cause separately
                         */
                        final Throwable rootCause = e.getRootCause();
                        if (null != rootCause) {
                            LOG.error(rootCause.getMessage(), rootCause);
                        }
                        /*
                         * Now log actual UploadServletException
                         */
                        LOG.error(e.getMessage(), e);
                        closeAndKeepAlive((HttpServletResponseWrapper) e.getRes(), e.getData().getBytes(com.openexchange.java.Charsets.UTF_8), ajpCon);
                    } catch (final ServletException e) {
                        LOG.error(e.getMessage(), e);
                        closeAndKeepAlive(ajpCon);
                    } catch (final AJPv13TimeoutException e) {
                        /*
                         * Read on socket input stream timed out
                         */
                        throw e;
                    } catch (final AJPv13Exception e) {
                        if (e.keepAlive()) {
                            LOG.error(e.getMessage(), e);
                            closeAndKeepAlive(ajpCon);
                        } else {
                            /*
                             * Leave outer while loop since connection shall be closed
                             */
                            throw e;
                        }
                    } catch (final IOException e) {
                        /*
                         * Obviously a socket communication error occurred
                         */
                        throw new AJPv13SocketClosedException(AJPv13Exception.AJPCode.IO_ERROR, e, e.getMessage());
                    } catch (final Throwable e) {
                        /*
                         * Catch every exception
                         */
                        final OXException logMe;
                        if (e instanceof OXException) {
                            logMe = (OXException) e;
                        } else {
                            logMe = new AJPv13Exception(e);
                        }
                        LOG.error(logMe.getMessage(), logMe);
                        closeAndKeepAlive(ajpCon);
                    }
                    ajpCon.resetConnection(true);
                    monitor.decrementNumProcessing();
                    monitor.addProcessingTime(System.currentTimeMillis() - processingStart);
                    monitor.incrementNumRequests();
                    processing = false;
                } while (!t.isInterrupted() && client != null && !client.isClosed());
                /*-
                 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                 * ------------------------------------ End of loop processing AJP socket's data ---------------------------------------
                 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                 */
            } catch (final AJPv13SocketClosedException e) {
                /*
                 * Just as debug info
                 */
                if (DEBUG_ENABLED) {
                    LOG.debug(e.getMessage(), e);
                }
                closeOrderly = false;
            } catch (final AJPv13BrokenCycleException e) {
                final String dump = e.getDump();
                LOG.error(e.getMessage() + (dump == null ? "" : "\nCorresponding AJP package:\n" + dump));
            } catch (final AJPv13Exception e) {
                LOG.error(e.getMessage(), e);
            } catch (final AJPv13TimeoutException e) {
                if (DEBUG_ENABLED) {
                    LOG.debug("AJP read timed out");
                }
                closeOrderly = false;
            } catch (final Throwable e) {
                /*
                 * Catch Throwable to catch every throwable object.
                 */
                final AJPv13Exception wrapper = new AJPv13Exception(e);
                LOG.error(wrapper.getMessage(), wrapper);
            } finally {
                terminateAndClose(closeOrderly, ajpCon);
                waitingOnAJPSocket = false;
                thread = null;
                if (processing) {
                    monitor.decrementNumProcessing();
                    monitor.addProcessingTime(System.currentTimeMillis() - processingStart);
                    monitor.incrementNumRequests();
                    processing = false;
                }
                AJPv13Server.decrementNumberOfOpenAJPSockets();
                /*
                 * Drop logging info for executing thread
                 */
                if (LogProperties.isEnabled()) {
                    LogProperties.removeLogProperties();
                }
            }
            final long duration = System.currentTimeMillis() - start;
            monitor.addUseTime(duration);
        }
        return null;
    }

    /**
     * Checks if this task was canceled before it completed normally.
     *
     * @return <code>true</code> if this task was canceled before it completed normally; otherwise <code>false</code>
     */
    public boolean isCancelled() {
        return control.isCancelled();
    }

    /**
     * Checks if this task completed. Completion may be due to normal termination, an exception, or cancellation -- in all of these cases,
     * this method will return <code>true</code>.
     *
     * @return <code>true</code> if this task completed; otherwise <code>false</code>
     */
    public boolean isDone() {
        return control.isDone();
    }

    @Override
    public void afterExecute(final Throwable t) {
        watcher.removeTask(this);
        if (null != scheduledKeepAliveTask) {
            scheduledKeepAliveTask.cancel(false);
            scheduledKeepAliveTask = null;
            /*
             * Task is automatically purged from TimerService by PurgeRunnable
             */
        }
        changeNumberOfRunningAJPTasks(false);
        listenerMonitor.decrementNumActive();
    }

    @Override
    public void beforeExecute(final Thread t) {
        watcher.addTask(this);
        final TimerService timer = AJPv13ServiceRegistry.getInstance().getService(TimerService.class);
        if (null != timer) {
            final int max = AJPv13Config.getKeepAliveTime();
            scheduledKeepAliveTask =
                timer.scheduleWithFixedDelay(new KeepAliveRunnable(this, max), max, max, TimeUnit.MILLISECONDS);
        }
        changeNumberOfRunningAJPTasks(true);
        listenerMonitor.incrementNumActive();
    }

    @Override
    public void setThreadName(final ThreadRenamer threadRenamer) {
        threadRenamer.renamePrefix("AJPListener");
    }

    /**
     * Increments/decrements the number of running AJP tasks.
     *
     * @param increment whether to increment or to decrement
     */
    private static void changeNumberOfRunningAJPTasks(final boolean increment) {
        MonitoringInfo.setNumberOfRunningAJPListeners(increment ? numRunning.incrementAndGet() : numRunning.decrementAndGet());
    }

    /**
     * Writes pending data to client and closes current AJP cycle (End-Response package) but keeps socket connection alive.
     *
     * @param resp The HTTP response for writing possibly outstanding header package
     * @param data The pending data
     * @param ajpCon The AJP connection
     * @throws AJPv13Exception If an AJP error occurs
     * @throws IOException If an I/O error occurs
     */
    private void closeAndKeepAlive(final HttpServletResponseWrapper resp, final byte[] data, final AJPv13ConnectionImpl ajpCon) throws AJPv13Exception, IOException {
        final Socket s = client;
        if (null != s) {
            if (null != resp) {
                /*
                 * Send response headers
                 */
                writeSendHeaders(s, resp);
            }
            if (null != data) {
                /*
                 * Send response body
                 */
                writeSendBody(s, data);
            }
            /*
             * Send END_RESPONSE package
             */
            writeEndResponse(s, false);
            ajpCon.getAjpRequestHandler().setEndResponseSent();
        }
    }

    /**
     * Closes current AJP cycle (End-Response package) but keeps socket connection alive.
     *
     * @param ajpCon The AJP connection
     * @throws AJPv13Exception If an AJP error occurs
     * @throws IOException If an I/O error occurs
     */
    private void closeAndKeepAlive(final AJPv13ConnectionImpl ajpCon) throws AJPv13Exception, IOException {
        final Socket s = client;
        if (null != s) {
            /*
             * Send END_RESPONSE package
             */
            writeAJPCycleEnd(s, ajpCon.getAjpRequestHandler(), false);
            ajpCon.getAjpRequestHandler().setEndResponseSent();
        }
    }

    /**
     * Closes the accepted client socket.
     *
     * @param closeOrderly Whether to write connection-terminating AJP END_RESPONSE package to web server
     */
    private void terminateAndClose(final boolean closeOrderly, final AJPv13ConnectionImpl ajpCon) {
        try {
            /*
             * Terminate AJP cycle and close socket
             */
            final Socket s = client;
            if (s != null) {
                try {
                    if (closeOrderly && !s.isClosed()) {
                        writeAJPCycleEnd(s, ajpCon.getAjpRequestHandler(), true);
                    }
                } catch (final Exception e) {
                    if (DEBUG_ENABLED) {
                        LOG.debug("Writing END_RESPONSE package failed.", e);
                    }
                } finally {
                    closeQuitely(s);
                    client = null;
                }
            }
            if (control != null) {
                // control.cancel(false);
                control = null;
            }
        } catch (final Exception e) {
            if (DEBUG_ENABLED) {
                LOG.debug(e.getMessage(), e);
            }
        }
    }

    private static void writeAJPCycleEnd(final Socket s, final AJPv13RequestHandler requestHandler, final boolean closeConnection) throws IOException, AJPv13Exception {
        if (!requestHandler.isHeadersSent()) {
            final OutputStream out = s.getOutputStream();
            final HttpServletResponseWrapper response = new HttpServletResponseWrapper(null);
            final byte[] errMsg = response.composeAndSetError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, null);
            // Write headers
            out.write(AJPv13Response.getSendHeadersBytes(response));
            out.flush();
            // Write error message
            out.write(AJPv13Response.getSendBodyChunkBytes(errMsg));
            out.flush();
            // Write end-response
            out.write(AJPv13Response.getEndResponseBytes(closeConnection));
            out.flush();
        } else if (!requestHandler.isEndResponseSent()) {
            final OutputStream out = s.getOutputStream();
            /*
             * Send end-response
             */
            out.write(AJPv13Response.getEndResponseBytes(closeConnection));
            out.flush();
        }
    }

    private static void closeQuitely(final Socket s) {
        try {
            s.close();
        } catch (final IOException e) {
            if (DEBUG_ENABLED) {
                LOG.debug("Socket could not be closed. Probably due to a broken socket connection (e.g. broken pipe).", e);
            }
        }
    }

    private static void writeEndResponse(final Socket client, final boolean closeConnection) throws AJPv13Exception, IOException {
        final OutputStream out = client.getOutputStream();
        out.write(AJPv13Response.getEndResponseBytes(closeConnection));
        out.flush();
    }

    private static void writeSendHeaders(final Socket client, final HttpServletResponseWrapper resp) throws AJPv13Exception, IOException {
        final OutputStream out = client.getOutputStream();
        out.write(AJPv13Response.getSendHeadersBytes(resp));
        out.flush();
    }

    private static void writeSendBody(final Socket client, final byte[] data) throws AJPv13Exception, IOException {
        final OutputStream out = client.getOutputStream();
        out.write(AJPv13Response.getSendBodyChunkBytes(data));
        out.flush();
    }

    private static final class KeepAliveRunnable implements Runnable {

        private final AJPv13Task task;

        private final boolean info;

        private final int max;

        /**
         * Initializes a new {@link KeepAliveRunnable} to only perform keep-alive on given AJP task.
         *
         * @param task The AJP task
         * @param max The max. processing time when a AJP task is considered as exceeded an keep-alive takes place
         */
        public KeepAliveRunnable(final AJPv13Task task, final int max) {
            super();
            this.task = task;
            this.max = max;
            info = LOG.isInfoEnabled();
        }

        @Override
        public void run() {
            try {
                if (task.isProcessing() && ((System.currentTimeMillis() - task.getAJPConnection().getLastWriteAccess()) > max)) {
                    /*
                     * Send "keep-alive" package
                     */
                    keepAlive();
                }
            } catch (final AJPv13Exception e) {
                if (DEBUG_ENABLED) {
                    LOG.error("AJP KEEP-ALIVE failed.", e);
                }
            } catch (final IOException e) {
                if (DEBUG_ENABLED) {
                    LOG.error("AJP KEEP-ALIVE failed.", e);
                }
            } catch (final Exception e) {
                if (DEBUG_ENABLED) {
                    LOG.error("AJP KEEP-ALIVE failed.", e);
                }
            }
        }

        /**
         * Performs AJP-style keep-alive poll to web server to avoid connection timeout.
         *
         * @throws IOException If an I/O error occurs
         * @throws AJPv13Exception If an AJP error occurs
         */
        private void keepAlive() throws IOException, AJPv13Exception {
            /*
             * Send "keep-alive" package depending on current request handler's state.
             */
            final AJPv13ConnectionImpl ajpConnection = task.getAJPConnection();
            final AJPv13RequestHandler ajpRequestHandler = ajpConnection.getAjpRequestHandler();
            ajpConnection.blockOutputStream(true);
            try {
                if (!ajpRequestHandler.isEndResponseSent()) {
                    final String remoteAddress = info ? task.getSocket().getRemoteSocketAddress().toString() : null;
                    final BlockableBufferedOutputStream out = ajpConnection.getOutputStream();
                    if (ajpRequestHandler.isHeadersSent()) {
                        /*
                         * SEND_HEADERS package already flushed to web server. Keep-Alive needs to be performed by flushing available data
                         * or an empty SEND_BODY package.
                         */
                        final byte[] remainingData = ajpRequestHandler.getAndClearResponseData();
                        if (remainingData.length > 0) {
                            /*
                             * Flush available data cut into MAX_BODY_CHUNK_SIZE chunks
                             */
                            keepAliveSendAvailableData(remoteAddress, out, remainingData);
                        } else {
                            /*
                             * Empty SEND_BODY package.
                             */
                            keepAliveSendEmptyBody(remoteAddress, out);
                        }
                    } else {
                        /*
                         * Pending SEND_HEADERS package. Keep-Alive needs to be performed by requesting an empty data chunk.
                         */
                        keepAliveGetEmptyBody(ajpConnection, remoteAddress, out);
                    }
                }
            } finally {
                ajpConnection.blockOutputStream(false);
            }
        } // End of keepAlive()

        private void keepAliveSendAvailableData(final String remoteAddress, final BlockableBufferedOutputStream out, final byte[] remainingData) throws IOException, AJPv13Exception {
            AbstractAJPv13Request.writeChunked(remainingData, out);
            if (DEBUG_ENABLED) {
                LOG.debug(new StringBuilder().append("AJP KEEP-ALIVE: Flushed available data to socket \"").append(remoteAddress).append(
                    "\" to initiate a KEEP-ALIVE poll."));
            }
        }

        private void keepAliveSendEmptyBody(final String remoteAddress, final BlockableBufferedOutputStream out) throws IOException, AJPv13Exception {
            AbstractAJPv13Request.writeEmpty(out);
            if (DEBUG_ENABLED) {
                LOG.debug(new StringBuilder().append("AJP KEEP-ALIVE: Flushed empty SEND-BODY-CHUNK response to socket \"").append(
                    remoteAddress).append("\" to initiate a KEEP-ALIVE poll."));
            }
        }

        private void keepAliveGetEmptyBody(final AJPv13ConnectionImpl ajpConnection, final String remoteAddress, final OutputStream out) throws IOException, AJPv13Exception {
            ajpConnection.blockInputStream(true);
            try {
                out.write(AJPv13Response.getGetBodyChunkBytes(0));
                out.flush();
                if (DEBUG_ENABLED) {
                    LOG.debug(new StringBuilder().append("AJP KEEP-ALIVE: Flushed empty GET-BODY request to socket \"").append(
                        remoteAddress).append("\" to initiate a KEEP-ALIVE poll."));
                }
                /*
                 * Swallow expected empty body chunk
                 */
                final int bodyRequestDataLength = ajpConnection.readInitialBytes(true, false);
                if (bodyRequestDataLength > 0 && parseInt(ajpConnection.getPayloadData(bodyRequestDataLength, true)) > 0) {
                    LOG.warn("AJP KEEP-ALIVE: Got a non-empty data chunk from web server although an empty one was requested");
                } else if (DEBUG_ENABLED) {
                    LOG.debug(new StringBuilder().append("AJP KEEP-ALIVE: Swallowed empty REQUEST-BODY from socket \"").append(
                        remoteAddress).append("\" initiated by former KEEP-ALIVE poll."));
                }
            } finally {
                ajpConnection.blockInputStream(false);
            }
        }

        private static int parseInt(final byte[] payloadData) {
            return ((payloadData[0] & 0xff) << 8) + (payloadData[1] & 0xff);
        }

    } // End of class

}
