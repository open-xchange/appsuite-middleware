/**
 * The MIT License
 * Copyright (c) 2010 Tad Glines
 * Copyright (c) 2015 Alexander Sova (bird@codeminders.com)
 * <p/>
 * Contributors: Ovea.com, Mycila.com
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.openexchange.socketio.websocket;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.socketio.protocol.EngineIOProtocol;
import com.openexchange.socketio.server.Session;
import com.openexchange.socketio.server.SocketIOManager;
import com.openexchange.socketio.server.TransportConnection;
import com.openexchange.socketio.server.TransportType;
import com.openexchange.socketio.server.transport.AbstractTransport;
import com.openexchange.socketio.server.transport.AbstractTransportConnection;

public final class WsTransport extends AbstractTransport {

    private final WsTransportConnectionRegistry connectionRegistry;

    /**
     * Initializes a new {@link WsTransport}.
     */
    public WsTransport(WsTransportConnectionRegistry connectionRegistry) {
        super();
        this.connectionRegistry = connectionRegistry;
    }

    /**
     * Gets the connection registry
     *
     * @return The connection registry
     */
    public WsTransportConnectionRegistry getConnectionRegistry() {
        return connectionRegistry;
    }

    @Override
    public void init(ServletConfig config, ServletContext context) throws ServletException {
        super.init(config, context);
    }

    @Override
    public TransportType getType() {
        return TransportType.WEB_SOCKET;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, SocketIOManager sessionManager) throws IOException {
        if (!"GET".equals(request.getMethod())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Only GET method is allowed for websocket transport");
            return;
        }

        final TransportConnection connection = getConnection(request.getParameter(EngineIOProtocol.SESSION_ID), sessionManager);

        // a bit hacky but safe since we know the type of TransportConnection here
        ((AbstractTransportConnection) connection).setRequest(request);
    }

    @Override
    public TransportConnection getConnection(String sessionId, SocketIOManager sessionManager) {
        return super.getConnection(sessionId, sessionManager);
    }

    @Override
    public TransportConnection createConnection(Session session) {
        return super.createConnection(session);
    }

    @Override
    public TransportConnection createConnection() {
        return new WsTransportConnection(this);
    }

    /**
     * Registers specified connection.
     *
     * @param connection The connection
     */
    public void register(WsTransportConnection connection) {
        connectionRegistry.addWsTransportConnection(connection);
    }

    /**
     * Unregisters specified connection.
     *
     * @param connection The connection
     */
    public void unregister(WsTransportConnection connection) {
        connectionRegistry.removeWsTransportConnection(connection);
    }
}
