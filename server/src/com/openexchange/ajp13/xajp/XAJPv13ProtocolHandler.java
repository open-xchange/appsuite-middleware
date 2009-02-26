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

package com.openexchange.ajp13.xajp;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.channels.ClosedChannelException;
import org.xsocket.Execution;
import org.xsocket.IDataSource;
import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.connection.IConnectExceptionHandler;
import org.xsocket.connection.IConnectHandler;
import org.xsocket.connection.IDataHandler;
import org.xsocket.connection.IDisconnectHandler;
import org.xsocket.connection.INonBlockingConnection;
import com.openexchange.ajp13.AJPv13Utility;
import com.openexchange.ajp13.exception.AJPv13Exception;

/**
 * {@link XAJPv13ProtocolHandler} - The non-threaded AJP protocol handler; meaning its onXXX() methods are directly processes by dispatcher
 * thread rather than a worker thread.
 * <p>
 * Reads the first four mandatory bytes of a non-blocking connection which signaled read-readiness in a transaction-like manner. Further
 * processing of AJP package's content is delegated to a data handler.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Execution(Execution.NONTHREADED)
public class XAJPv13ProtocolHandler implements IConnectHandler, IDataHandler, IConnectExceptionHandler, IDisconnectHandler {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(XAJPv13ProtocolHandler.class);

    protected static final byte MAGIC1 = 0x12;

    protected static final byte MAGIC2 = 0x34;

    /**
     * The flag indicating whether to read starting mandatory bytes in a transactional manner.
     */
    private final boolean transactional;

    /**
     * Initializes a new {@link XAJPv13ProtocolHandler}.
     */
    public XAJPv13ProtocolHandler(final boolean transactional) {
        super();
        this.transactional = transactional;
    }

    public boolean onConnect(final INonBlockingConnection connection) throws IOException, MaxReadSizeExceededException {
        connection.setAttachment(new XAJPv13Session(this));
        connection.setAutoflush(false);
        connection.setOption(INonBlockingConnection.TCP_NODELAY, "true");
        // A final good article for final tuning can be final found here http://www.onlamp.com/lpt/a/6324
        // TODO: connection.setMaxReadBufferThreshold(8192);
        return true;
    }

    public boolean onData(final INonBlockingConnection connection) throws IOException, ClosedChannelException, MaxReadSizeExceededException {
        final int dataLength;

        if (transactional) {
            /*
             * Read the mandatory first four bytes in a transactional manner. These bytes contain the two magic bytes 0x12 and 0x34 and
             * further two bytes indicating the total AJP package's length
             */
            // //////////
            // "transaction" start
            //
            connection.markReadPosition();
            try {
                /*
                 * Read the mandatory first four bytes.
                 */
                dataLength = readMandatoryBytes(connection);

                connection.removeReadMark();

            } catch (final BufferUnderflowException bue) {
                connection.resetToReadMark();
                return true;
            }
            //
            // "transaction" end
            // /////////////
        } else {
            /*
             * Directly read the mandatory first four bytes and thus a BufferUnderflowException possibly occurs.
             */
            dataLength = readMandatoryBytes(connection);
        }

        // Increment package number
        ((XAJPv13Session) connection.getAttachment()).incrementPackageNumber();

        // Apply data handler to connection
        connection.setHandler(new XAJPv13DataHandler(this, dataLength));

        return true;
    }

    /**
     * Processes the incoming data based on the given blocking connection.
     * 
     * @param dataSource The data source to process.
     * @param session The AJP session
     * @throws IOException If an I/O error occurs
     * @throws BufferUnderflowException If not enough data is available and passed data source is a {@link INonBlockingConnection
     *             non-blocking connection}
     * @throws AJPv13Exception If an AJP error occurs
     */
    public void handleConnection(final IDataSource dataSource, final XAJPv13Session session) throws IOException, AJPv13Exception {
        /*
         * Read the mandatory first four bytes.
         */
        final int dataLength = readMandatoryBytes(dataSource);

        // Increment package number
        session.incrementPackageNumber();

        // Handle passed data source by data handler
        new XAJPv13DataHandler(this, dataLength).handleDataSource(dataSource, session);
    }

    /**
     * Reads the first four mandatory bytes of an incoming AJP package:<br>
     * <code>0x12 0x34 <i>&lt;data-length&gt;</i></code>.
     * 
     * @param dataSource The data source from which to read the mandatory bytes
     * @return
     * @throws IOException If an I/O error occurs
     * @throws BufferUnderflowException If not enough data is available and passed data source is a {@link INonBlockingConnection
     *             non-blocking connection}
     */
    private int readMandatoryBytes(final IDataSource dataSource) throws IOException {
        byte b = dataSource.readByte();
        if (MAGIC1 != b) {
            throw new IOException("Illegal first magic byte:" + AJPv13Utility.dumpByte(b));
        }
        b = dataSource.readByte();
        if (MAGIC2 != b) {
            throw new IOException("Illegal second magic byte:" + AJPv13Utility.dumpByte(b));
        }
        // Return data length
        return AJPv13Utility.parseInt(dataSource.readByte(), dataSource.readByte());
    }

    public boolean onDisconnect(final INonBlockingConnection connection) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder(256).append("AJP connection disconnected. Either ").append(
                "the client-side initiated the disconnect by actively closing the connection ").append(
                "or the connection is broken or the peer disconnected improperly and the Java VM detected the broken connection.").toString());
        }
        connection.close();
        return true;
    }

    public boolean onConnectException(final INonBlockingConnection connection, final IOException ioe) throws IOException {
        LOG.error("Establishing AJP connection failed or timed out: " + ioe.getMessage(), ioe);
        return true;
    }
}
