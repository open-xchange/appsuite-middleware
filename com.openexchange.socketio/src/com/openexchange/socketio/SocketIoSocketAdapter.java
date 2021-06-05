/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.socketio;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.exception.OXException;
import com.openexchange.websockets.MessageTranscoder;
import com.openexchange.websockets.WebSocket;
import com.openexchange.websockets.WebSocketConnectException;
import com.openexchange.websockets.WebSocketListener;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoWebSocket;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;


/**
 * {@link SocketIoSocketAdapter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class SocketIoSocketAdapter extends EngineIoWebSocket implements WebSocketListener, MessageTranscoder {

    private static final Logger LOG = LoggerFactory.getLogger(SocketIoSocketAdapter.class);

    private static final String DEFAULT_NAMESPACE = "/";

    private final AtomicBoolean open = new AtomicBoolean(false);
    private final EngineIoServer engineIoServer;
    private final SocketIoServer socketIoServer;

    private volatile WebSocket socket;
    private volatile SocketIoSocket socketIoSocket;


    /**
     * Initializes a new {@link SocketIoSocketAdapter}.
     * @param engineIoServer
     * @param socketIoServer
     */
    public SocketIoSocketAdapter(EngineIoServer engineIoServer, SocketIoServer socketIoServer) {
        super();
        this.engineIoServer = engineIoServer;
        this.socketIoServer = socketIoServer;
        LOG.trace("new SocketIOSocketAdapter()");
    }

    /* WebSocketListener */

    @Override
    public void onWebSocketConnect(WebSocket socket) {
        LOG.trace("onWebSocketConnect(): {}", socket);
        if (!isAppropriateWebSocket(socket)) {
            return;
        }

        this.socket = socket;

        Thread currentThread = Thread.currentThread();
        Listener socketListener = args -> {
            // this is the only correlation we can use to assign a SocketIoSocket
            // to its according SocketIOSocketAdapter. The "connection" event
            // is emitted during 'engineIoServer.handleWebSocket(this)' and
            // therefore handled within the same thread.
            if (currentThread == Thread.currentThread()) {
                SocketIoSocket socketIoSocket = (SocketIoSocket) args[0];
                this.socketIoSocket = socketIoSocket;
                LOG.debug("Assigned SocketIO socket '{}' to websocket: {}", socketIoSocket.getId(), this.socket);
            }
        };

        SocketIoNamespace namespace = socketIoServer.namespace(DEFAULT_NAMESPACE);
        namespace.on("connection", socketListener);
        try {
            engineIoServer.handleWebSocket(this);
            if (this.socketIoSocket == null) {
                throw new WebSocketConnectException(500, "SocketIoSocket intitialization failed");
            }
            socket.setMessageTranscoder(this);
            open.set(true);
            LOG.debug("Initialized SocketIO for websocket: {}", socket);
        } catch (Exception e) {
            throw new WebSocketConnectException(500, "SocketIoSocket intitialization failed", e);
        } finally {
            namespace.off("connection", socketListener);
        }
    }

    @Override
    public void onWebSocketClose(WebSocket socket) {
        LOG.trace("onWebSocketClose(): {}", socket);
        if (open.compareAndSet(true, false)) {
            emit("close");
        }
        this.socket = null;
        this.socketIoSocket = null;
        LOG.debug("Shutdown SocketIO for closed websocket: {}", socket);
    }

    @Override
    public void onMessage(WebSocket socket, String text) {
        // not called but handled by onInboundMessage()
        LOG.warn("onMessage() called even though a transcoder is set!");
    }

    private boolean isAppropriateWebSocket(WebSocket socket) {
        String path = socket.getPath();
        return (null != path && path.startsWith("/socket.io"));
    }


    /* EngineIoWebSocket */

    @Override
    public Map<String, String> getQuery() {
        WebSocket socket = this.socket;
        if (socket == null) {
            throw new IllegalStateException("WebSocket instance not set");
        }

        Map<String, String> parameters = socket.getParameters();
        return parameters == null ? Collections.emptyMap() : parameters;
    }

    @Override
    public void write(String message) throws IOException {
        WebSocket socket = this.socket;
        if (socket == null) {
            throw new IOException("Websocket is closed");
        }

        try {
            LOG.debug("Sending message: {}", message);
            socket.sendMessageRaw(message);
        } catch (OXException e) {
            IOException ioe = ExceptionUtils.extractFrom(e, IOException.class);
            throw ioe != null ? ioe : new IOException(e.getSoleMessage(), e);
        }
    }

    @Override
    public void write(byte[] message) throws IOException {
        WebSocket socket = this.socket;
        if (socket == null) {
            throw new IOException("Websocket is closed");
        }

        throw new IOException("Support for byte messages is not implemented!");
    }

    @Override
    public void close() {
        LOG.trace("close(): {}", socket);
        WebSocket socket = this.socket;
        if (socket != null) {
            LOG.debug("Shutdown SocketIO due to server-side disconnect attempt for websocket: {}", socket);
            this.socket = null;
            this.socketIoSocket = null;
            socket.close();
        }
    }

    /* MessageTranscoder */

    @Override
    public String getId() {
        return "socket.io";
    }

    @Override
    public String onInboundMessage(WebSocket socket, String message) {
        LOG.trace("onInboundMessage(): {}", socket);
        LOG.debug("Received message: {}", message);
        if (this.socketIoSocket != null) {
            emit("message", (Object) message);
        }

        return null;
    }

    @Override
    public String onOutboundMessage(WebSocket socket, String message) {
        LOG.trace("onOutboundMessage(): {}", socket);

        String name;
        Object[] args;
        try {
            JSONObject jEvent = new JSONObject(message);
            name = jEvent.getString("name");

            String namespace = jEvent.optString("namespace", DEFAULT_NAMESPACE);
            if (!DEFAULT_NAMESPACE.equals(namespace)) {
                LOG.error("SocketIO message for namespace '{}' cannot be sent. Only '{}' is supported.", namespace, DEFAULT_NAMESPACE);
                return null;
            }

            JSONArray jArgs = jEvent.getJSONArray("args");
            args = new Object[jArgs.length()];
            for (int i = 0; i < jArgs.length(); i++) {
                args[i] = jArgs.get(i);
            }
        } catch (JSONException e) {
            LOG.error("Invalid message to send: {}", message, e);
            return message;
        }

        SocketIoSocket socketIoSocket = this.socketIoSocket;
        if (socketIoSocket != null) {
            socketIoSocket.send(name, args);
        }

        return null;
    }

}
