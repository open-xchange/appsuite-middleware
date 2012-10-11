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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import javax.servlet.ServletException;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13Connection;
import com.openexchange.ajp13.AJPv13RequestHandler;
import com.openexchange.ajp13.AJPv13Response;
import com.openexchange.ajp13.AJPv13Utility;
import com.openexchange.ajp13.BlockableBufferedInputStream;
import com.openexchange.ajp13.BlockableBufferedOutputStream;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.exception.AJPv13Exception.AJPCode;
import com.openexchange.ajp13.exception.AJPv13InvalidByteSequenceException;
import com.openexchange.ajp13.exception.AJPv13InvalidConnectionStateException;
import com.openexchange.ajp13.exception.AJPv13SocketClosedException;
import com.openexchange.ajp13.exception.AJPv13TimeoutException;
import com.openexchange.concurrent.Blockable;
import com.openexchange.concurrent.Blocker;
import com.openexchange.concurrent.ConcurrentBlocker;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link AJPv13ConnectionImpl} - Represents an AJP connection which mainly delegates processing of incoming AJP data packages to an
 * assigned AJP request handler.
 * <p>
 * Moreover it keeps track of package numbers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class AJPv13ConnectionImpl implements AJPv13Connection, Blockable {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AJPv13ConnectionImpl.class));

    private int state;

    private int packageNumber;

    private final BlockableBufferedInputStream inputStream;

    private final BlockableBufferedOutputStream outputStream;

    private final AJPv13Task task;

    private final Blocker blocker;

    private AJPv13RequestHandlerImpl ajpRequestHandler;

    /**
     * Initializes a new {@link AJPv13ConnectionImpl}
     *
     * @param listener The AJP listener providing client socket
     */
    AJPv13ConnectionImpl(final AJPv13Task task) throws AJPv13Exception {
        super();
        state = IDLE_STATE;
        packageNumber = 0;
        blocker = new ConcurrentBlocker();
        this.task = task;
        try {
            final Socket client = task.getSocket();
            inputStream = new BlockableBufferedInputStream(client.getInputStream(), false);
            outputStream = new BlockableBufferedOutputStream(client.getOutputStream(), AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE, false);
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
            throw new AJPv13Exception(AJPv13Exception.AJPCode.IO_ERROR, false, e, e.getMessage());
        }
    }

    @Override
    public void block() {
        blocker.block();
    }

    @Override
    public void unblock() {
        blocker.unblock();
    }

    /**
     * Drops outstanding data.
     */
    public void dropOutstandingData() {
        blocker.acquire();
        try {
            outputStream.acquire();
            try {
                outputStream.dropBuffer();
            } finally {
                outputStream.release();
            }
        } finally {
            blocker.release();
        }
    }

    /**
     * Resets this connection instance and prepares it for next upcoming AJP cycle. That is associated request handler will be set to
     * <code>null</code>, its state is set to <code>IDLE</code> and the output stream is going to be flushed.
     *
     * @param releaseRequestHandler
     */
    void resetConnection(final boolean releaseRequestHandler) {
        blocker.acquire();
        try {
            if (ajpRequestHandler != null) {
                resetRequestHandler(releaseRequestHandler);
            }
            if (outputStream != null) {
                outputStream.acquire();
                try {
                    outputStream.dropBuffer();
                } finally {
                    outputStream.release();
                }
            }
            state = IDLE_STATE;
            packageNumber = 0;
        } finally {
            blocker.release();
        }
    }

    private void resetRequestHandler(final boolean release) {
        /*
         * Discard request handler's reference to this connection if it ought to be released from it
         */
        if (release) {
            /*
             * Discard request handler reference
             */
            ajpRequestHandler = null;
        } else {
            ajpRequestHandler.reset();
        }
    }

    /**
     * Waits for and processes incoming AJP package through delegating to associated request handler.
     * <p>
     * Moreover this connection's state is switched to <tt>ASSIGNED</tt> if it is <tt>IDLE</tt>.
     *
     * @throws AJPv13Exception If an AJP error occurs
     */
    void processRequest() throws AJPv13Exception {
        // if (task.getSocket().isClosed()) {
        // throw new IOException("Socket is closed");
        // }
        if (state == IDLE_STATE) {
            state = ASSIGNED_STATE;
            if (ajpRequestHandler == null) {
                /*
                 * Create a request handler to this newly assigned connection
                 */
                ajpRequestHandler = new AJPv13RequestHandlerImpl(blocker);
                ajpRequestHandler.setAJPConnection(this);
            }
        }
        ajpRequestHandler.processPackage();
    }

    /**
     * Creates the AJP response data to previously received AJP package through delegating to request handler.
     *
     * @throws AJPv13Exception If an AJP error occurs while creating response data or this connection is not in <tt>ASSIGNED</tt> state
     * @throws ServletException If a servlet error occurs
     */
    void createResponse() throws AJPv13Exception, ServletException {
        if (state != ASSIGNED_STATE) {
            throw new AJPv13InvalidConnectionStateException();
        }
        ajpRequestHandler.createResponse();
    }

    /**
     * Gets the associated AJP request handler which processes the AJP data sent over this connection
     *
     * @return The associated AJP request handler.
     */
    @Override
    public AJPv13RequestHandler getAjpRequestHandler() {
        blocker.acquire();
        try {
            return ajpRequestHandler;
        } finally {
            blocker.release();
        }
    }

    /**
     * Gets the input stream from AJP client
     *
     * @return The input stream from AJP client
     * @throws IOException If input stream cannot be returned
     */
    @Override
    public InputStream getInputStream() throws IOException {
        blocker.acquire();
        try {
            return inputStream;
        } finally {
            blocker.release();
        }
    }

    /**
     * Gets the output stream to AJP client
     *
     * @return The output stream to AJP client
     * @throws IOException If output stream cannot be returned
     */
    @Override
    public BlockableBufferedOutputStream getOutputStream() throws IOException {
        blocker.acquire();
        try {
            return outputStream;
        } finally {
            blocker.release();
        }
    }

    /**
     * Sets the SO_TIMEOUT with the specified timeout, in milliseconds.
     *
     * @param millis The timeout in milliseconds
     * @throws AJPv13Exception If there is an error in the underlying protocol, such as a TCP error.
     */
    @Override
    public void setSoTimeout(final int millis) throws AJPv13Exception {
        blocker.acquire();
        try {
            task.getSocket().setSoTimeout(millis);
        } catch (final SocketException e) {
            throw new AJPv13Exception(AJPv13Exception.AJPCode.IO_ERROR, false, e, e.getMessage());
        } finally {
            blocker.release();
        }
    }

    /**
     * Gets the number of current AJP package.
     *
     * @return The number of current AJP package.
     */
    @Override
    public int getPackageNumber() {
        blocker.acquire();
        try {
            return packageNumber;
        } finally {
            blocker.release();
        }
    }

    /**
     * Increments package number by one.
     */
    void incrementPackageNumber() {
        blocker.acquire();
        try {
            packageNumber++;
        } finally {
            blocker.release();
        }
    }

    /**
     * Gets the current AJP connection's state
     *
     * @return Current AJP connection's state
     */
    @Override
    public int getState() {
        blocker.acquire();
        try {
            return state;
        } finally {
            blocker.release();
        }
    }

    @Override
    public void close() {
        blocker.acquire();
        try {
            resetConnection(true);
            discardStreams();
            task.cancel();
        } finally {
            blocker.release();
        }
    }

    /**
     * Sets both input and output stream to <code>null</code>
     */
    private void discardStreams() {
        if (outputStream != null) {
            outputStream.acquire();
            try {
                outputStream.close();
            } catch (final IOException e) {
                LOG.warn("Output stream could not be closed", e);
            } finally {
                outputStream.release();
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (final IOException e) {
                LOG.warn("Input stream could not be closed", e);
            }
        }
    }

    @Override
    public String toString() {
        blocker.acquire();
        try {
            if (IDLE_STATE == state) {
                return "State: IDLE";
            }
            final StringBuilder sb = new StringBuilder(128);
            sb.append("State: ASSIGNED").append("; connected to ").append(task.getSocket().getRemoteSocketAddress());
            return sb.toString();
        } finally {
            blocker.release();
        }
    }

    /**
     * Marks corresponding AJP task as processing
     */
    void markProcessing() {
        blocker.acquire();
        try {
            task.markProcessing();
        } finally {
            blocker.release();
        }
    }

    /**
     * Marks corresponding AJP task as non-processing
     */
    void markNonProcessing() {
        blocker.acquire();
        try {
            task.markNonProcessing();
        } finally {
            blocker.release();
        }
    }

    /**
     * Increments number of AJP tasks waiting for incoming AJP data.
     */
    void incrementWaiting() {
        blocker.acquire();
        try {
            task.incrementWaiting();
        } finally {
            blocker.release();
        }
    }

    /**
     * Decrements number of AJP tasks waiting for incoming AJP data.
     */
    void decrementWaiting() {
        blocker.acquire();
        try {
            task.decrementWaiting();
        } finally {
            blocker.release();
        }
    }

    /**
     * Sets if this connection's task is long-running.
     *
     * @param longRunning <code>true</code> if this connection's task is long-running; otherwise <code>false</code>
     */
    void setLongRunning(final boolean longRunning) {
        blocker.acquire();
        try {
            task.setLongRunning(longRunning);
        } finally {
            blocker.release();
        }
    }

    @Override
    public void blockInputStream(final boolean block) {
        blocker.acquire();
        try {
            if (block) {
                inputStream.block();
            } else {
                inputStream.unblock();
            }
        } finally {
            blocker.release();
        }
    }

    @Override
    public void blockOutputStream(final boolean block) {
        blocker.acquire();
        try {
            if (block) {
                outputStream.block();
            } else {
                outputStream.unblock();
            }
        } finally {
            blocker.release();
        }
    }

    /**
     * Gets the last write access time stamp.
     *
     * @return The last write access time stamp.
     */
    public long getLastWriteAccess() {
        blocker.acquire();
        try {
            return outputStream.getLastAccessed();
        } finally {
            blocker.release();
        }
    }

    /**
     * Reads a certain amount or all data from given <code>InputStream</code> instance dependent on boolean value of <code>strict</code>.
     *
     * @param payloadLength
     * @param in
     * @param strict if <code>true</code> only <code>payloadLength</code> bytes are read, otherwise all data is read
     * @return The read bytes
     * @throws IOException If an I/O error occurs
     */
    byte[] getPayloadData(final int payloadLength, final boolean strict) throws IOException {
        blocker.acquire();
        try {
            final InputStream in = inputStream;
            byte[] bytes = null;
            if (strict) {
                /*
                 * Read only payloadLength bytes
                 */
                bytes = new byte[payloadLength];
                int bytesRead = -1;
                int offset = 0;
                while ((offset < bytes.length) && ((bytesRead = in.read(bytes, offset, bytes.length - offset)) != -1)) {
                    offset += bytesRead;
                }
                if (offset < bytes.length) {
                    Arrays.fill(bytes, offset, bytes.length, ((byte) -1));
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(
                            new StringBuilder().append("Incomplete payload data in AJP package: Should be ").append(payloadLength).append(
                                " but was ").append(offset).toString(),
                            new Throwable());
                    }
                }
            } else {
                /*
                 * Read all available bytes
                 */
                int bytesRead = -1;
                final ByteArrayOutputStream buf = new UnsynchronizedByteArrayOutputStream(8192);
                final byte[] fillMe = new byte[8192];
                while ((bytesRead = in.read(fillMe, 0, fillMe.length)) != -1) {
                    buf.write(fillMe, 0, bytesRead);
                }
                bytes = buf.toByteArray();
            }
            return bytes;
        } finally {
            blocker.release();
        }
    }

    /**
     * Reads the (mandatory) first four bytes of an incoming AJPv13 package which indicate a package from Web Server to Servlet Container
     * with its first two bytes and the payload data size in bytes in the following two bytes.
     */
    int readInitialBytes(final boolean enableTimeout) throws IOException, AJPv13Exception {
        return readInitialBytes(enableTimeout, true);
    }

    /**
     * Reads the (mandatory) first four bytes of an incoming AJPv13 package which indicate a package from Web Server to Servlet Container
     * with its first two bytes and the payload data size in bytes in the following two bytes.
     */
    int readInitialBytes(final boolean enableTimeout, final boolean processingInformation) throws IOException, AJPv13Exception {
        blocker.acquire();
        try {
            int dataLength = -1;
            if (enableTimeout) {
                setSoTimeout(AJPv13Config.getAJPListenerReadTimeout());
            }
            /*
             * Read a package from Web Server to Servlet Container.
             */
            incrementWaiting();
            try {
                final InputStream in = inputStream;
                long start = 0L;
                final int[] magic;
                try {
                    if (processingInformation) {
                        markNonProcessing();
                    }
                    start = System.currentTimeMillis();
                    /*
                     * Read first two bytes
                     */
                    magic = new int[] { in.read(), in.read() };
                } catch (final SocketException e) {
                    throw new AJPv13SocketClosedException(
                        AJPCode.SOCKET_CLOSED_BY_WEB_SERVER,
                        e,
                        Integer.valueOf(getPackageNumber()),
                        Long.valueOf((System.currentTimeMillis() - start)));
                }
                if (checkMagicBytes(magic)) {
                    dataLength = (in.read() << 8) + in.read();
                } else if (magic[0] == -1 || magic[1] == -1) {
                    throw new AJPv13SocketClosedException(
                        AJPCode.EMPTY_INPUT_STREAM,
                        null,
                        Integer.valueOf(getPackageNumber()),
                        Long.valueOf(System.currentTimeMillis() - start));
                } else {
                    throw new AJPv13InvalidByteSequenceException(getPackageNumber(), magic[0], magic[1], AJPv13Utility.dumpBytes(
                        (byte) magic[0],
                        (byte) magic[1],
                        getPayloadData(-1, false)));
                }
                if (enableTimeout) {
                    /*
                     * Set an infinite timeout
                     */
                    setSoTimeout(0);
                }
                /*
                 * Initial bytes have been read, so processing (re-)starts now
                 */
                if (processingInformation) {
                    markProcessing();
                }
            } finally {
                decrementWaiting();
            }
            return dataLength;
        } catch (final java.net.SocketTimeoutException e) {
            throw new AJPv13TimeoutException(e.getMessage(), e);
        } finally {
            blocker.release();
        }
    }

    private static boolean checkMagicBytes(final int[] magic) {
        return (magic[0] == AJPv13RequestHandler.MAGIC1_SERVER_TO_CONTAINER && magic[1] == AJPv13RequestHandler.MAGIC2_SERVER_TO_CONTAINER);
    }
}
