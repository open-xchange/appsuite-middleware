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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.socketio.server.io.socket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.MDC;
import com.openexchange.websockets.ConnectionId;
import com.openexchange.websockets.WebSocket;
import com.openexchange.websockets.WebSocketListener;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;

/**
 * {@link WebSocketRegistry} - Socket.IO adapter for WebSocket.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class WebSocketRegistry implements WebSocketListener {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(WebSocketRegistry.class);
    }

    private final EngineIoServer engineIoServer;
    private final ConcurrentMap<ConnectionId, WebSocketConnection> connections;

    /**
     * Initializes a new {@link WebSocketRegistry}.
     */
    public WebSocketRegistry() {
        super();
        // Initialize Engine.IO server
        EngineIoServer engineIoServer =  new EngineIoServer(EngineIoServerOptions.newFromDefault());
        this.engineIoServer = engineIoServer;

        // Initialize Socket.IO server
        SocketIoServer socketIoServer = new SocketIoServer(engineIoServer);

        // Initialize mapping
        final ConcurrentMap<ConnectionId, WebSocketConnection> connections = new ConcurrentHashMap<>(128, 0.9F, 1);
        this.connections = connections;

        // Register listener
        socketIoServer.namespace("/").on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];

            String sConnectionId = MDC.get("socketio.connectionid");
            if (sConnectionId != null) {
                WebSocketConnection webSocketConnection = connections.get(ConnectionId.newInstance(sConnectionId));
                webSocketConnection.setSocketIoSocket(socket);
            }
        });
    }

    private boolean isAppropriateWebSocket(WebSocket socket) {
        String path = socket.getPath();
        return (null != path && path.startsWith("/socket.io"));
    }

    @Override
    public void onWebSocketConnect(WebSocket socket) {
        if (!isAppropriateWebSocket(socket)) {
            return;
        }

        // Create connection instance for accepted Web Socket
        WebSocketConnection connection = new WebSocketConnection(socket);
        connections.put(socket.getConnectionId(), connection);

        // Initialize & connect connection instance
        boolean error = true;
        try {
            socket.setMessageTranscoder(connection);

            // Connect...
            MDC.put("socketio.connectionid", socket.getConnectionId().getId());
            engineIoServer.handleWebSocket(connection);
            error = false;
        } finally {
            MDC.remove("socketio.connectionid");
            if (error) {
                connections.remove(socket.getConnectionId());
            }
        }
    }

    @Override
    public void onWebSocketClose(WebSocket socket) {
        WebSocketConnection connection = connections.remove(socket.getConnectionId());
        if (connection != null) {
            connection.onWebSocketClose();
        }
    }

    @Override
    public void onMessage(WebSocket socket, String text) {
     // Incoming messages already handled in 'WebSocketHandler.onInboundMessage(WebSocket, String)'
    }

    /**
     * Shuts-down this registry.
     */
    public void shutDown() {
        for (WebSocketConnection connection : connections.values()) {
            String connectionId = connection.getEndpoint().getConnectionId().getId();
            try {
                connection.onWebSocketClose();
            } catch (Exception e) {
                LoggerHolder.LOG.warn("Failed to close Web Socket {}", connectionId, e);
            }
        }
    }

}
