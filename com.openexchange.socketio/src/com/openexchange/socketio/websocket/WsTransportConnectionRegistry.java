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

package com.openexchange.socketio.websocket;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import com.openexchange.socketio.protocol.EngineIOProtocol;
import com.openexchange.socketio.server.Session;
import com.openexchange.socketio.server.SocketIOManager;
import com.openexchange.socketio.server.TransportConnection;
import com.openexchange.websockets.WebSocket;
import com.openexchange.websockets.WebSocketListener;


/**
 * {@link WsTransportConnectionRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WsTransportConnectionRegistry implements WebSocketListener {

    /** The logger constant */
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(WsTransportConnectionRegistry.class);

    private final ConcurrentMap<String, WsTransportConnection> registeredConnections;
    private volatile WsTransport transport;
    private volatile SocketIOManager socketIOManager;

    /**
     * Initializes a new {@link WsTransportConnectionRegistry}.
     */
    public WsTransportConnectionRegistry() {
        super();
        registeredConnections = new ConcurrentHashMap<>(512, 0.9F, 1);
    }

    /**
     * Shuts-down this connection registry.
     */
    public void shutDown() {
        // Nothing to do so far
    }

    /**
     * Sets the Socket.IO manager.
     *
     * @param socketIOManager The Socket.IO manager
     */
    public void setSocketIOManager(SocketIOManager socketIOManager) {
        this.socketIOManager = socketIOManager;
    }

    /**
     * Sets the Web Socket transport
     *
     * @param transport The Web Socket transport
     */
    public void setTransport(WsTransport transport) {
        this.transport = transport;
    }

    /**
     * Adds specified connection.
     *
     * @param connection The connection to add
     * @return <code>true</code> if connection has been successfully added; otherwise <code>false</code>
     */
    public boolean addWsTransportConnection(WsTransportConnection connection) {
        if (null == connection) {
            return false;
        }
        return null == registeredConnections.putIfAbsent(connection.getSession().getSessionId(), connection);
    }

    /**
     * Removes specified connection.
     *
     * @param connection The connection to remove
     * @return <code>true</code> if connection has been successfully removed; otherwise <code>false</code>
     */
    public boolean removeWsTransportConnection(WsTransportConnection connection) {
        if (null == connection) {
            return false;
        }
        return null != registeredConnections.remove(connection.getSession().getSessionId());
    }

    /**
     * Gets the number of registered connections.
     *
     * @return The number of registered connections
     */
    public long getNumberOfRegisteredConnections() {
        return registeredConnections.size();
    }

    // ------------------------------------------------------ WebSocketListener stuff ---------------------------------------

    private boolean isAppropriateWebSocket(WebSocket socket) {
        String path = socket.getPath();
        return (null != path && path.startsWith("/socket.io"));
    }

    @Override
    public void onWebSocketConnect(WebSocket socket) {
        if (!isAppropriateWebSocket(socket)) {
            return;
        }

        String sessionId = socket.getParameter(EngineIOProtocol.SESSION_ID);
        if (null == sessionId) {
            // Start Socket.IO fresh hand-shake
            String sTransport = socket.getParameter(EngineIOProtocol.TRANSPORT);
            if (!"websocket".equals(sTransport)) {
                LOGGER.warn("Unsupported transport via WS: {}", sTransport);
                return;
            }

            // Not registered, yet
            SocketIOManager socketIOManager = this.socketIOManager;
            if (null == socketIOManager) {
                LOGGER.warn("Not initialized, yet");
                return;
            }

            WsTransport transport = this.transport;
            if (null == transport) {
                LOGGER.warn("Not initialized, yet");
                return;
            }

            WsTransportConnection connection = (WsTransportConnection) transport.getConnection(null, socketIOManager);
            applyToConnection(connection, socket);
            return;
        }

        // "sid" parameter is available. Check if there is already a registered connection for it.
        WsTransportConnection connection = registeredConnections.get(sessionId);
        if (null != connection) {
            applyToConnection(connection, socket);
            return;
        }

        // Not registered, yet
        SocketIOManager socketIOManager = this.socketIOManager;
        if (null == socketIOManager) {
            LOGGER.warn("Not initialized, yet", sessionId);
            return;
        }

        // Grab associated session (if any)
        Session session = socketIOManager.getSession(sessionId);
        if (null == session) {
            LOGGER.warn("No such socket.io session: {}", sessionId);
            return;
        }

        // Upgrade connection (if appropriate)
        TransportConnection activeConnection = session.getConnection();
        if (activeConnection instanceof WsTransportConnection) {
            connection = (WsTransportConnection) activeConnection;
            applyToConnection(connection, socket);
            return;
        }

        String sTransport = socket.getParameter(EngineIOProtocol.TRANSPORT);
        if (!"websocket".equals(sTransport)) {
            LOGGER.warn("Unsupported transport via WS: {}", sTransport);
            return;
        }

        // The new connection considered for an upgrade
        WsTransport transport = this.transport;
        if (null == transport) {
            LOGGER.warn("Not initialized, yet", sessionId);
            return;
        }

        connection = (WsTransportConnection) transport.createConnection(session);
        applyToConnection(connection, socket);
    }

    private void applyToConnection(WsTransportConnection connection, WebSocket socket) {
        connection.onWebSocketConnect(socket);
        socket.setMessageTranscoder(connection);
        socket.getWebSocketSession().setAttribute(EngineIOProtocol.SESSION_ID, connection.getSession().getSessionId());
    }

    @Override
    public void onWebSocketClose(WebSocket socket) {
        if (!isAppropriateWebSocket(socket)) {
            return;
        }

        String sessionId = socket.getParameter(EngineIOProtocol.SESSION_ID);
        if (null == sessionId) {
            sessionId = socket.getWebSocketSession().getAttribute(EngineIOProtocol.SESSION_ID);
        }

        WsTransportConnection connection = null;
        if (null != sessionId) {
            // Get connection by session identifier
            connection = registeredConnections.remove(sessionId);
            if (null == connection) {
                LOGGER.warn("No such socket.io session: {}", sessionId);
                return;
            }

            connection.onWebSocketClose(socket);
        } else {
            for (Iterator<WsTransportConnection> it = registeredConnections.values().iterator(); null == connection && it.hasNext();) {
                WsTransportConnection wsCon = it.next();
                if (socket.equals(wsCon.getRemoteEndpoint())) {
                    connection = wsCon;
                    it.remove();
                }
            }

            if (null != connection) {
                connection.onWebSocketClose(socket);
            }
        }

        socket.setMessageTranscoder(null);
    }

    @Override
    public void onMessage(WebSocket socket, String text) {
        // Incoming messages already handled in 'WsTransportConnection.onInboundMessage(WebSocket, String)'
    }

}
