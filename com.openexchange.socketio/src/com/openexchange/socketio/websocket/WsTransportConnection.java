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
import com.openexchange.socketio.common.ConnectionState;
import com.openexchange.socketio.common.DisconnectReason;
import com.openexchange.socketio.common.SocketIOException;
import com.openexchange.socketio.protocol.BinaryPacket;
import com.openexchange.socketio.protocol.EngineIOPacket;
import com.openexchange.socketio.protocol.EngineIOProtocol;
import com.openexchange.socketio.protocol.SocketIOPacket;
import com.openexchange.socketio.server.Config;
import com.openexchange.socketio.server.Session;
import com.openexchange.socketio.server.SocketIOClosedException;
import com.openexchange.socketio.server.SocketIOProtocolException;
import com.openexchange.socketio.server.transport.AbstractTransportConnection;
import com.openexchange.websockets.WebSocket;
import com.openexchange.websockets.WebSocketListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class WsTransportConnection extends AbstractTransportConnection implements WebSocketListener {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(WsTransportConnection.class);

    private final WsTransport wsTransport;
    private volatile WebSocket remote_endpoint;
    private volatile String sessionId;

    public WsTransportConnection(WsTransport transport) {
        super(transport);
        wsTransport = transport;
    }

    @Override
    public void setSession(Session session) {
        super.setSession(session);
        sessionId = null == session ? null : session.getSessionId();
    }

    @Override
    protected void init() {
        getSession().setTimeout(getConfig().getTimeout(Config.DEFAULT_PING_TIMEOUT));

        LOGGER.debug("{} WebSocket configuration: timeout={}", getConfig().getNamespace(), getSession().getTimeout());
    }

    @Override
    public void onWebSocketConnect(WebSocket socket) {
        remote_endpoint = socket;
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

    @Override
    public void onWebSocketClose(WebSocket socket) {
        //If close is unexpected then try to guess the reason based on closeCode, otherwise the reason is already set
        if(getSession().getConnectionState() != ConnectionState.CLOSING) {
            getSession().setDisconnectReason(fromCloseCode(false));
        }

        getSession().setDisconnectMessage("Closing");
        getSession().onShutdown();
    }

    @Override
    public void onMessage(WebSocket socket, String text) {
        LOGGER.debug("Session[{}]: text received: {}", getSession().getSessionId(), text);

        getSession().resetTimeout();

        try {
            getSession().onPacket(EngineIOProtocol.decode(text), this);
        } catch (SocketIOProtocolException e) {
            LOGGER.warn("Invalid packet received", e);
        }
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unexpected request on upgraded WebSocket connection");
    }

    @Override
    public void abort() {
        getSession().clearTimeout();

        WebSocket remote_endpoint = this.remote_endpoint;
        if (remote_endpoint != null) {
            this.remote_endpoint = null;
            disconnectEndpoint(remote_endpoint);
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
                //sendBinary(os.toByteArray());
            }
        }
    }

    protected void sendString(String data) throws SocketIOException {
        WebSocket remote_endpoint = this.remote_endpoint;
        if (null == remote_endpoint) {
            throw new SocketIOClosedException();
        }

        LOGGER.debug("Session[{}]: send text: {}", getSession().getSessionId(), data);

        try {
            remote_endpoint.sendMessage(data);
        } catch (Exception e) {
            disconnectEndpoint(remote_endpoint);
            this.remote_endpoint = null;
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
