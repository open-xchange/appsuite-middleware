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

package org.glassfish.grizzly.http.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Filter;
import org.glassfish.grizzly.*;
import org.glassfish.grizzly.asyncqueue.MessageCloner;
import org.glassfish.grizzly.asyncqueue.PushBackHandler;
import org.glassfish.grizzly.asyncqueue.WritableMessage;
import org.glassfish.grizzly.filterchain.*;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.nio.transport.TCPNIOConnection;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;

/**
 * The {@link TCPNIOTransport}'s transport {@link Filter} implementation by Alexey Stashok.
 * <p>
 * Extended by the possiblity to set read/write timeouts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXTCPNIOTransportFilter extends BaseFilter {

    /**
     * The default read timeout in milliseconds (30sec);<br>
     * taken from the initial value for <code>org.glassfish.grizzly.nio.NIOConnection.readTimeoutMillis</code>
     */
    public static final int DEFAULT_READ_TIMEOUT_MILLIS = 30000;

    /**
     * The default write timeout in milliseconds (30sec);<br>
     * taken from the initial value for <code>org.glassfish.grizzly.nio.NIOConnection.writeTimeoutMillis</code>
     */
    public static final int DEFAULT_WRITE_TIMEOUT_MILLIS = 30000;

    // -----------------------------------------------------------------------------------------------------------------------------

    private static interface ConnectionPreparer {

        void prepareConnection(TCPNIOConnection connection);
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    private final TCPNIOTransport transport;
    private final ConnectionPreparer connectionPreparer;

    public OXTCPNIOTransportFilter(final TCPNIOTransport transport, final long readTimeoutMillis, final long writeTimeoutMillis) {
        super();
        this.transport = transport;

        if (readTimeoutMillis <= 0) {
            if (writeTimeoutMillis <= 0) {
                connectionPreparer = new ConnectionPreparer() {

                    @Override
                    public void prepareConnection(TCPNIOConnection connection) {
                        // Do nothing
                    }
                };
            } else {
                connectionPreparer = new ConnectionPreparer() {

                    @Override
                    public void prepareConnection(TCPNIOConnection connection) {
                        connection.setWriteTimeout(writeTimeoutMillis, TimeUnit.MILLISECONDS);
                    }
                };
            }
        } else {
            if (writeTimeoutMillis <= 0) {
                connectionPreparer = new ConnectionPreparer() {

                    @Override
                    public void prepareConnection(TCPNIOConnection connection) {
                        connection.setReadTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS);
                    }
                };
            } else {
                connectionPreparer = new ConnectionPreparer() {

                    @Override
                    public void prepareConnection(TCPNIOConnection connection) {
                        connection.setReadTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS);
                        connection.setWriteTimeout(writeTimeoutMillis, TimeUnit.MILLISECONDS);
                    }
                };
            }
        }
    }

    @Override
    public NextAction handleRead(final FilterChainContext ctx) throws IOException {
        final TCPNIOConnection connection = (TCPNIOConnection) ctx.getConnection();

        // Set read/write timeout (if set)
        connectionPreparer.prepareConnection(connection);

        final boolean isBlocking = ctx.getTransportContext().isBlocking();

        final Buffer buffer;
        if (!isBlocking) {
            buffer = transport.read(connection, null);
        } else {
            GrizzlyFuture<ReadResult<Buffer, SocketAddress>> future =
                    transport.getTemporarySelectorIO().getReader().read(
                    connection, null);
            try {
                ReadResult<Buffer, SocketAddress> result = future.get();
                buffer = result.getMessage();
                future.recycle(true);
            } catch (ExecutionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }

                throw new IOException(cause);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }

        if (buffer == null || buffer.position() == 0) {
            return ctx.getStopAction();
        }

        buffer.trim();

        ctx.setMessage(buffer);
        ctx.setAddress(connection.getPeerAddress());

        return ctx.getInvokeAction();
    }

    @Override
    @SuppressWarnings("unchecked")
    public NextAction handleWrite(final FilterChainContext ctx)
            throws IOException {
        final WritableMessage message = ctx.getMessage();
        if (message != null) {
            ctx.setMessage(null);
            final Connection connection = ctx.getConnection();
            final FilterChainContext.TransportContext transportContext =
                    ctx.getTransportContext();

            final CompletionHandler completionHandler = transportContext.getCompletionHandler();
            final MessageCloner cloner = transportContext.getMessageCloner();
            final PushBackHandler pushBackHandler = transportContext.getPushBackHandler();

            transportContext.setCompletionHandler(null);
            transportContext.setMessageCloner(null);
            transportContext.setPushBackHandler(null);

            if (!transportContext.isBlocking()) {
                transport.getAsyncQueueIO().getWriter().write(connection, null,
                        message, completionHandler, pushBackHandler, cloner);
            } else {
                transport.getTemporarySelectorIO().getWriter().write(connection,
                        null, message, completionHandler, pushBackHandler);
            }
        }


        return ctx.getInvokeAction();
    }

    @Override
    @SuppressWarnings("unchecked")
    public NextAction handleEvent(final FilterChainContext ctx,
            final FilterChainEvent event) throws IOException {
        if (event.type() == TransportFilter.FlushEvent.TYPE) {
            final Connection connection = ctx.getConnection();
            final FilterChainContext.TransportContext transportContext =
                    ctx.getTransportContext();

            if (transportContext.getCompletionHandler() != null) {
                throw new IllegalStateException("TransportContext CompletionHandler must be null");
            }

            final CompletionHandler completionHandler =
                    ((TransportFilter.FlushEvent) event).getCompletionHandler();

            transport.getWriter(transportContext.isBlocking()).write(connection,
                    Buffers.EMPTY_BUFFER, completionHandler);
        }

        return ctx.getInvokeAction();
    }

    @Override
    public void exceptionOccurred(final FilterChainContext ctx,
            final Throwable error) {

        final Connection connection = ctx.getConnection();
        if (connection != null) {
            connection.closeSilently();
        }
    }

}
