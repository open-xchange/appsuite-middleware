/**
 * The MIT License
 * Copyright (c) 2015 Alexander Sova (bird@codeminders.com)
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

import com.google.common.io.ByteStreams;
import com.openexchange.java.Strings;
import com.openexchange.socketio.common.ConnectionState;
import com.openexchange.socketio.common.DisconnectReason;
import com.openexchange.socketio.common.SocketIOException;
import com.openexchange.socketio.protocol.BinaryPacket;
import com.openexchange.socketio.protocol.EngineIOPacket;
import com.openexchange.socketio.protocol.EngineIOProtocol;
import com.openexchange.socketio.protocol.EventPacket;
import com.openexchange.socketio.protocol.SocketIOPacket;
import com.openexchange.socketio.protocol.SocketIOProtocol;
import com.openexchange.socketio.server.Config;
import com.openexchange.socketio.server.Session;
import com.openexchange.socketio.server.SocketIOClosedException;
import com.openexchange.socketio.server.SocketIOProtocolException;
import com.openexchange.socketio.server.transport.AbstractTransportConnection;
import com.openexchange.websockets.MessageTranscoder;
import com.openexchange.websockets.WebSocket;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class WsTransportConnection extends AbstractTransportConnection implements MessageTranscoder {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(WsTransportConnection.class);

    private final WsTransport wsTransport;
    private volatile WebSocket remoteEndpoint;

    /**
     * Initializes a new {@link WsTransportConnection}.
     *
     * @param transport
     */
    public WsTransportConnection(WsTransport transport) {
        super(transport);
        wsTransport = transport;
    }

    // ---------------------------------------------- WebSocketListener stuff ------------------------------------------------------

    /**
     * Call-back for a connected Web Socket.
     *
     *  @param socket The socket
     */
    public void onWebSocketConnect(WebSocket socket) {
        remoteEndpoint = socket;
        Session session = getSession();
        if (session.getConnectionState() == ConnectionState.CONNECTING) {
            try {
                send(EngineIOProtocol.createHandshakePacket(session.getSessionId(),
                        new String[]{},
                        getConfig().getPingInterval(Config.DEFAULT_PING_INTERVAL),
                        getConfig().getTimeout(Config.DEFAULT_PING_TIMEOUT)));

                session.onConnect(this);
            } catch (SocketIOException e) {
                LOGGER.error("Cannot connect", e);
                session.setDisconnectReason(DisconnectReason.CONNECT_FAILED);
                abort();
            }
        }
    }

    /**
     * Call-back for a closed Web Socket.
     *
     * @param socket The socket
     */
    public void onWebSocketClose(WebSocket socket) {
        // If close is unexpected then try to guess the reason based on closeCode, otherwise the reason is already set
        Session session = getSession();
        if (session.getConnectionState() != ConnectionState.CLOSING) {
            session.setDisconnectReason(fromCloseCode(false));
        }

        session.setDisconnectMessage("Closing");
        session.onShutdown();
    }

    /**
     * Call-back for a received message
     *
     * @param socket The socket
     * @param text The received message
     */
    public void onMessage(WebSocket socket, String text) {
        Session session = getSession();
        LOGGER.debug("Session[{}]: text received: {}", session.getSessionId(), text);

        session.resetTimeout();

        try {
            session.onPacket(EngineIOProtocol.decode(text), this);
        } catch (SocketIOProtocolException e) {
            LOGGER.warn("Invalid packet received", e);
        }
    }

    // ---------------------------------------------- MessageTranscoder stuff ------------------------------------------------------

    @Override
    public String onInboundMessage(WebSocket socket, String message) {
        Session session = getSession();
        LOGGER.debug("Session[{}]: text received: {}", session.getSessionId(), message);

        session.resetTimeout();

        String retval = null;
        try {
            EngineIOPacket packet = EngineIOProtocol.decode(message);
            if (EngineIOPacket.Type.MESSAGE == packet.getType()) {
                SocketIOPacket socketIOPacket = SocketIOProtocol.decode(packet.getTextData());
                if (SocketIOPacket.Type.EVENT == socketIOPacket.getType()) {
                    EventPacket eventPacket = (EventPacket) socketIOPacket;
                    String name = eventPacket.getName();
                    Object[] args = eventPacket.getArgs();
                    String namespace = eventPacket.getNamespace();
                    if (Strings.isEmpty(namespace)) {
                        namespace = SocketIOProtocol.DEFAULT_NAMESPACE;
                    }
                    try {
                        retval = new JSONObject(3).put("name", name).put("args", new JSONArray(Arrays.asList(args))).put("namespace", namespace).toString();
                    } catch (JSONException e) {
                        LOGGER.warn("Invalid event packet received", e);
                    }
                }
            }

            // Trigger common handling
            session.onPacket(packet, this);
        } catch (SocketIOProtocolException e) {
            LOGGER.warn("Invalid packet received", e);
        }

        return retval;
    }

    @Override
    public String onOutboundMessage(WebSocket socket, String message) {
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
            LOGGER.warn("Invalid message to send: {}", message, e);
            return message;
        }

        try {
            getSession().getConnection().emit(ns, name, args);
            return null;
        } catch (SocketIOException e) {
            LOGGER.error("Failed to emit message: {}", message, e);
        }
        return message;
    }

    // ---------------------------------------------- TransportConnection stuff ------------------------------------------------------

    @Override
    public String getId() {
        return "socket.io";
    }

    @Override
    public void setSession(Session session) {
        super.setSession(session);
        wsTransport.register(this);
    }

    @Override
    protected void init() {
        Session session = getSession();
        session.setTimeout(getConfig().getTimeout(Config.DEFAULT_PING_TIMEOUT));

        LOGGER.debug("{} WebSocket configuration: timeout={}", getConfig().getNamespace(), session.getTimeout());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unexpected request on upgraded WebSocket connection");
    }

    @Override
    public void abort() {
        getSession().clearTimeout();

        WebSocket remoteEndpoint = this.remoteEndpoint;
        if (remoteEndpoint != null) {
            this.remoteEndpoint = null;
            disconnectEndpoint(remoteEndpoint);
        }
    }

    @Override
    public void send(EngineIOPacket packet) throws SocketIOException {
        sendString(EngineIOProtocol.encode(packet));
    }

    @Override
    public void send(SocketIOPacket packet) throws SocketIOException {
        send(EngineIOProtocol.createMessagePacket(packet.encode()));
        if (packet instanceof BinaryPacket) {
            Collection<InputStream> attachments = ((BinaryPacket) packet).getAttachments();
            for (InputStream is : attachments) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                try {
                    os.write(EngineIOPacket.Type.MESSAGE.value());
                    ByteStreams.copy(is, os);
                } catch (IOException e) {
                    LOGGER.error("Cannot load binary object to send it to the socket", e);
                }
                // TODO: sendBinary(os.toByteArray());
            }
        }
    }

    protected void sendString(String data) throws SocketIOException {
        WebSocket remoteEndpoint = this.remoteEndpoint;
        if (null == remoteEndpoint) {
            throw new SocketIOClosedException();
        }

        LOGGER.debug("Session[{}]: send text: {}", getSession().getSessionId(), data);

        try {
            remoteEndpoint.sendMessageRaw(data);
        } catch (Exception e) {
            disconnectEndpoint(remoteEndpoint);
            this.remoteEndpoint = null;
            throw new SocketIOException(e);
        }
    }

    private void disconnectEndpoint(WebSocket remote_endpoint ) {
        try {
            remote_endpoint.close();
        } catch (Exception ex) {
            // ignore
        }

        wsTransport.unregister(this);
    }

    private DisconnectReason fromCloseCode(boolean error) {
        return error ? DisconnectReason.ERROR : DisconnectReason.CLIENT_GONE;
    }
}
