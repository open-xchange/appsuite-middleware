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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.exception.OXException;
import com.openexchange.socketio.protocol.SocketIOProtocol;
import com.openexchange.websockets.MessageTranscoder;
import com.openexchange.websockets.WebSocket;
import io.socket.engineio.server.EngineIoWebSocket;
import io.socket.socketio.server.SocketIoSocket;

/**
 * {@link WebSocketConnection} - Represents an Engine.IO Web Socket connection.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class WebSocketConnection extends EngineIoWebSocket implements MessageTranscoder {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(WebSocketConnection.class);
    }

    private final AtomicReference<SocketIoSocket> socketRef;
    private volatile WebSocket endpoint;
    private final Map<String, String> query;

    /**
     * Initializes a new {@link WebSocketConnection}.
     */
    public WebSocketConnection(WebSocket socket) {
        super();
        socketRef = new AtomicReference<>(null);
        this.endpoint = socket;
        query = socket.getParameters();
    }

    /**
     * Sets the Socket.IO socket.
     *
     * @param socket The socket to set
     */
    public void setSocketIoSocket(SocketIoSocket socket) {
        socketRef.set(socket);
    }

    /**
     * Gets the Web Socket end-point.
     *
     * @return The Web Socket end-point
     */
    public WebSocket getEndpoint() {
        return endpoint;
    }

    /* EngineIoWebSocket */

    @Override
    public Map<String, String> getQuery() {
        return query;
    }

    @Override
    public void write(String message) throws IOException {
        assert endpoint != null;

        try {
            endpoint.sendMessageRaw(message);
        } catch (OXException e) {
            IOException ioe = ExceptionUtils.extractFrom(e, IOException.class);
            throw ioe != null ? ioe : new IOException(e.getSoleMessage(), e);
        }
    }

    @Override
    public void write(byte[] message) throws IOException {
        assert endpoint != null;

        try {
            endpoint.sendMessageRaw(new String(message, StandardCharsets.UTF_8));
        } catch (OXException e) {
            IOException ioe = ExceptionUtils.extractFrom(e, IOException.class);
            throw ioe != null ? ioe : new IOException(e.getSoleMessage(), e);
        }
    }

    @Override
    public void close() {
        if (endpoint != null) {
            endpoint.close();
        }
    }

    /* MessageTranscoder */

    /**
     * Invoked when associated Web Socket gets closed.
     */
    public void onWebSocketClose() {
        emit("close");
        endpoint = null;
    }

    @Override
    public String getId() {
        return "socket.io";
    }

    @Override
    public String onInboundMessage(WebSocket socket, String message) {
        emit("message", (Object) message);
        return null;
    }

    @Override
    public String onOutboundMessage(WebSocket socket, String message) {
        SocketIoSocket socketIoSocket = socketRef.get();
        if (socketIoSocket == null) {
            LoggerHolder.LOG.warn("Missing Socket.IO socket");
            return message;
        }

        String ns = SocketIOProtocol.DEFAULT_NAMESPACE;
        String name;
        Object[] args;
        try {
            JSONObject jEvent = new JSONObject(message);
            name = jEvent.getString("name");
            List<Object> list = jEvent.getJSONArray("args").asList();
            args = list.toArray(new Object[list.size()]);
            ns = jEvent.optString("namespace", SocketIOProtocol.DEFAULT_NAMESPACE);
        } catch (JSONException e) {
            LoggerHolder.LOG.warn("Invalid message to send: {}", message, e);
            return message;
        }

        socketIoSocket.send(name, args);
        return null;
    }

}
