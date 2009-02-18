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
import javax.servlet.ServletException;
import org.xsocket.DataConverter;
import org.xsocket.Execution;
import org.xsocket.IDataSource;
import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.connection.IDataHandler;
import org.xsocket.connection.INonBlockingConnection;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.xajp.request.XAJPv13ForwardRequest;
import com.openexchange.ajp13.xajp.request.XAJPv13Request;
import com.openexchange.ajp13.xajp.request.XAJPv13RequestBody;

/**
 * {@link XAJPv13DataHandler} - The multi-threaded handler of an AJP package's content dependent on current package number.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Execution(Execution.MULTITHREADED)
public final class XAJPv13DataHandler implements IDataHandler {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(XAJPv13DataHandler.class);

    /**
     * Starts the request handle cycle with following data.
     * 
     * @value 2
     */
    public static final int FORWARD_REQUEST_PREFIX_CODE = 2;

    /**
     * Web Server asks to shut down the Servlet Container.
     * 
     * @value 7
     */
    public static final int SHUTDOWN_PREFIX_CODE = 7;

    /**
     * Web Server asks the Servlet Container to take control (secure login phase).
     * 
     * @value 8
     */
    public static final int PING_PREFIX_CODE = 8;

    /**
     * Web Server asks the Servlet Container to respond quickly with a CPong.
     * 
     * @value 10
     */
    public static final int CPING_PREFIX_CODE = 10;

    private final XAJPv13ProtocolHandler protocolHandler;

    private final int dataLength;

    /**
     * Initializes a new {@link XAJPv13DataHandler}.
     */
    public XAJPv13DataHandler(final XAJPv13ProtocolHandler protocolHandler, final int dataLength) {
        super();
        this.protocolHandler = protocolHandler;
        this.dataLength = dataLength;
    }

    public boolean onData(final INonBlockingConnection connection) throws IOException, BufferUnderflowException, ClosedChannelException, MaxReadSizeExceededException {
        final int availableBytes = connection.available();

        if (-1 == availableBytes) {
            throw new IOException("Unexpected end-of-stream reached");
        }
        if (availableBytes < dataLength) {
            // Wait for more data
            return true;
        }

        try {
            // Handle data source
            final XAJPv13Session session = (XAJPv13Session) connection.getAttachment();
            final XAJPv13Request ajpRequest = handleDataSource(connection, session);
            if (ajpRequest.doResponse(session)) {
                // Respond
                ajpRequest.response(connection);
            }
        } catch (final AJPv13Exception e) {
            LOG.error(e.getMessage(), e);
            if (!e.keepAlive()) {
                connection.close();
            }
        } catch (final ServletException e) {
            LOG.error(e.getMessage(), e);
        } catch (final RuntimeException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }

        // Pass back to protocol handler to await next AJP package
        connection.setHandler(protocolHandler);

        return true;
    }

    /**
     * Handle specified data source by feeding its data to an appropriate {@link XAJPv13Request AJP request} for processing purpose.
     * 
     * @param dataSource The data source
     * @param session The AJP session
     * @return The {@link XAJPv13Request AJP request} which processed the data source.
     * @throws AJPv13Exception If an AJP error occurs
     * @throws IOException If an I/O error occurs
     */
    public XAJPv13Request handleDataSource(final IDataSource dataSource, final XAJPv13Session session) throws AJPv13Exception, IOException {
        final byte[] content = DataConverter.toBytes(dataSource.readByteBufferByLength(dataLength));
        // Process package dependent on package number
        final XAJPv13Request ajpRequest;
        if (1 == session.getPackageNumber()) {
            final int prefixCode = content[0];
            if (FORWARD_REQUEST_PREFIX_CODE == prefixCode) {
                ajpRequest = new XAJPv13ForwardRequest(content, (INonBlockingConnection) dataSource);
            } else {
                throw new IOException("Unsupported prefix code in first AJP package: " + prefixCode);
            }
        } else {
            ajpRequest = new XAJPv13RequestBody(content);
        }
        // Process package
        ajpRequest.processRequest(session);
        return ajpRequest;
    }

}
