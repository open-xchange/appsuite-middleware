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

package com.openexchange.ajp13.najp;

import java.io.IOException;
import java.net.Socket;
import javax.servlet.ServletException;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13Response;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.exception.AJPv13SocketClosedException;
import com.openexchange.ajp13.stable.AJPv13Server;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.servlet.UploadServletException;
import com.openexchange.tools.servlet.http.HttpServletResponseWrapper;

/**
 * {@link AJPv13Task} - Processes an accepted client socket until either executing thread is interrupted or assigned socket is closed.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13Task implements Runnable {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13Task.class);

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
     * The keep-alive time stamp.
     */
    private volatile long keepAliveStamp;

    /**
     * The keep-alive counter.
     */
    private volatile long keepAliveCounter;

    /**
     * The borrowed thread.
     */
    private volatile Thread thread;

    /**
     * The currently used AJP connection.
     */
    private volatile AJPv13ConnectionImpl ajpConnection;

    /**
     * Initializes a new {@link AJPv13Task}.
     * 
     * @param client The client socket to process
     */
    public AJPv13Task(final Socket client) {
        super();
        this.client = client;
    }

    /**
     * Cancels this AJP task; meaning to close the client socket.
     */
    public void cancel() {
        if (null != client) {
            try {
                client.close();
            } catch (final IOException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(e.getMessage(), e);
                }
            } finally {
                client = null;
            }
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
        AJPv13Server.ajpv13ListenerMonitor.incrementNumProcessing();
    }

    /**
     * Mark this task as non-processing
     */
    void markNonProcessing() {
        waitingOnAJPSocket = true;
        if (processing) {
            processing = false;
            AJPv13Server.ajpv13ListenerMonitor.decrementNumProcessing();
        }
    }

    /**
     * @return <code>true</code> if task is currently listening to client socket's input stream, otherwise <code>false</code>
     */
    boolean isWaitingOnAJPSocket() {
        return waitingOnAJPSocket;
    }

    /**
     * @return <code>true</code> if task is currently processing, otherwise <code>false</code>
     */
    boolean isProcessing() {
        return processing;
    }

    /**
     * @return The processing start time stamp
     */
    long getProcessingStartTime() {
        return processingStart;
    }

    /**
     * @return The keep-alive time stamp
     */
    long getKeepAliveStamp() {
        return keepAliveStamp;
    }

    /**
     * Sets the keep-alive time stamp
     * 
     * @param keepAliveStamp The keep-alive time stamp to set
     */
    void setKeepAliveStamp(final long keepAliveStamp) {
        this.keepAliveStamp = keepAliveStamp;
    }

    /**
     * Gets the keep-alive counter
     * 
     * @return The keep-alive counter
     */
    long getKeepAliveCounter() {
        return keepAliveCounter;
    }

    /**
     * Increments the keep-alive counter
     */
    void incrementKeepAliveCounter() {
        keepAliveCounter++;
    }

    /**
     * Discards the socket.
     */
    void discardSocket() {
        if (client != null) {
            try {
                client.close();
            } catch (final IOException e) {
                LOG.debug("Socket could not be closed. Probably due to a broken socket connection (e.g. broken pipe)", e);
            }
            client = null;
        }
    }

    /**
     * Gets currently executing thread's stack trace.
     * 
     * @return The currently executing thread's stack trace or an empty stack trace if no thread processes this task.
     */
    StackTraceElement[] getStackTrace() {
        if (null == thread) {
            return new StackTraceElement[0];
        }
        return thread.getStackTrace();
    }

    /**
     * Gets currently executing thread's name.
     * 
     * @return The currently executing thread's name or an empty string if no threads processes this task.
     */
    String getThreadName() {
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

    public void run() {
        final Thread t = thread = Thread.currentThread();
        final Socket s = client;
        if (!t.isInterrupted() && s != null && !s.isClosed()) {
            AJPv13Server.ajpv13ListenerMonitor.incrementNumActive();
            final long start = System.currentTimeMillis();
            /*
             * Assign a connection to this listener which is either fetched from connection pool (if configured) or newly created
             */
            final AJPv13ConnectionImpl ajpCon = AJPv13Config.useAJPConnectionPool() ? AJPv13ConnectionPool.getAJPv13Connection(this) : new AJPv13ConnectionImpl(
                this);
            ajpConnection = ajpCon;
            try {
                s.setKeepAlive(true);
                // TODO: waitingOnAJPSocket = true;
                /*
                 * Keep on processing underlying stream's data as long as accepted client socket is alive, its input is not shut down and no
                 * communication failure occurred.
                 */
                do {
                    try {
                        ajpCon.processRequest();
                        ajpCon.createResponse();
                        if (!ajpCon.getAjpRequestHandler().isEndResponseSent()) {
                            LOG.error("Detected AJP cycle (request/response) without terminating END_RESPONSE package!", new Throwable());
                            /*
                             * Just for safety reason to ensure END_RESPONSE package is going to be sent.
                             */
                            writeEndResponse(s, false);
                        }
                    } catch (final UploadServletException e) {
                        LOG.error(e.getMessage(), e);
                        closeAndKeepAlive((HttpServletResponseWrapper) e.getRes(), e.getData().getBytes("UTF-8"), ajpCon);
                    } catch (final ServletException e) {
                        LOG.error(e.getMessage(), e);
                        closeAndKeepAlive(ajpCon);
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
                        final AbstractOXException logMe;
                        if (e instanceof AbstractOXException) {
                            logMe = (AbstractOXException) e;
                        } else {
                            logMe = new AJPv13Exception(e);
                        }
                        LOG.error(logMe.getMessage(), logMe);
                        closeAndKeepAlive(ajpCon);
                    }
                    ajpCon.resetConnection(false);
                    AJPv13Server.ajpv13ListenerMonitor.decrementNumProcessing();
                    AJPv13Server.ajpv13ListenerMonitor.addProcessingTime(System.currentTimeMillis() - processingStart);
                    AJPv13Server.ajpv13ListenerMonitor.incrementNumRequests();
                    processing = false;
                    s.getOutputStream().flush();
                } while (!t.isInterrupted() && !s.isClosed()); // End of loop processing an AJP socket's data
            } catch (final AJPv13SocketClosedException e) {
                /*
                 * Just as debug info
                 */
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getMessage(), e);
                }
            } catch (final AJPv13Exception e) {
                LOG.error(e.getMessage(), e);
            } catch (final Throwable e) {
                /*
                 * Catch Throwable to catch every throwable object.
                 */
                final AJPv13Exception wrapper = new AJPv13Exception(e);
                LOG.error(wrapper.getMessage(), wrapper);
            } finally {
                terminateAndClose(ajpCon);
                waitingOnAJPSocket = false;
                thread = null;
                if (processing) {
                    AJPv13Server.ajpv13ListenerMonitor.decrementNumProcessing();
                    AJPv13Server.ajpv13ListenerMonitor.addProcessingTime(System.currentTimeMillis() - processingStart);
                    AJPv13Server.ajpv13ListenerMonitor.incrementNumRequests();
                    processing = false;
                }
                AJPv13Server.decrementNumberOfOpenAJPSockets();
                AJPv13Server.ajpv13ListenerMonitor.decrementNumActive();
            }
            final long duration = System.currentTimeMillis() - start;
            AJPv13Server.ajpv13ListenerMonitor.addUseTime(duration);
        }
    }

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

    private void closeAndKeepAlive(final AJPv13ConnectionImpl ajpCon) throws AJPv13Exception, IOException {
        final Socket s = client;
        if (null != s) {
            /*
             * Send END_RESPONSE package
             */
            writeEndResponse(s, false);
            ajpCon.getAjpRequestHandler().setEndResponseSent();
        }
    }

    /**
     * Writes connection-terminating AJP END_RESPONSE package to web server, closes the AJP connection and accepted client socket as well.
     */
    private void terminateAndClose(final AJPv13ConnectionImpl ajpCon) {
        try {
            /*
             * Release AJP connection
             */
            if (ajpCon != null) {
                ajpCon.removeListener();
                if (AJPv13Config.useAJPConnectionPool()) {
                    AJPv13ConnectionPool.putBackAJPv13Connection(ajpCon);
                }
            }
        } catch (final Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(e.getMessage(), e);
            }
        }
        try {
            /*
             * Terminate AJP cycle and close socket
             */
            final Socket s = client;
            if (s != null) {
                if (!s.isClosed()) {
                    writeEndResponse(s, true);
                    s.close();
                }
                client = null;
            }
        } catch (final Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }

    private static void writeEndResponse(final Socket client, final boolean closeConnection) throws AJPv13Exception, IOException {
        client.getOutputStream().write(AJPv13Response.getEndResponseBytes(closeConnection));
        client.getOutputStream().flush();
    }

    private static void writeSendHeaders(final Socket client, final HttpServletResponseWrapper resp) throws AJPv13Exception, IOException {
        client.getOutputStream().write(AJPv13Response.getSendHeadersBytes(resp));
        client.getOutputStream().flush();
    }

    private static void writeSendBody(final Socket client, final byte[] data) throws AJPv13Exception, IOException {
        client.getOutputStream().write(AJPv13Response.getSendBodyChunkBytes(data));
        client.getOutputStream().flush();
    }

}
