/**
 * The MIT License
 * Copyright (c) 2010 Tad Glines
 * Copyright (c) 2015 Alexander Sova (bird@codeminders.com)
 * <p>
 * Contributors: Ovea.com, Mycila.com
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.openexchange.socketio.server;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import com.openexchange.socketio.common.ConnectionState;
import com.openexchange.socketio.common.DisconnectReason;
import com.openexchange.socketio.common.SocketIOException;
import com.openexchange.socketio.protocol.ACKPacket;
import com.openexchange.socketio.protocol.BinaryPacket;
import com.openexchange.socketio.protocol.EngineIOPacket;
import com.openexchange.socketio.protocol.EngineIOProtocol;
import com.openexchange.socketio.protocol.EventPacket;
import com.openexchange.socketio.protocol.SocketIOPacket;
import com.openexchange.socketio.protocol.SocketIOProtocol;
import com.openexchange.timer.ScheduledTimerTask;

/**
 * A SocketIO session.
 * <ul>
 * <li>Provides access to the active connection</li>
 * <li>Holds open sockets (using the active connection) per different namespace</li>
 * <li>Managed connection life-cycle (connect, close, ...)</li>
 * <li>Stores arbitrary attributes</li>
 * </ul>
 *
 *
 * @author Alexander Sova (bird@codeminders.com)
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Session implements DisconnectListener {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Session.class);

    private final SocketIOManager socketIOManager;
    private final String sessionId;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    private final Map<String, Socket> sockets = new LinkedHashMap<String, Socket>(); // namespace, socket

    private TransportConnection activeConnection;
    private ConnectionState state = ConnectionState.CONNECTING;

    private DisconnectReason disconnectReason = DisconnectReason.UNKNOWN;
    private String disconnectMessage;

    private long timeout;
    private ScheduledTimerTask timeoutTask;
    private boolean timedOut;

    private BinaryPacket binaryPacket;
    private final AtomicInteger packetId = new AtomicInteger(0); // packet id. used for requesting ACK
    private final Map<Integer, ACKListener> ack_listeners = new LinkedHashMap<>(); // packetid, listener

    /**
     * Initializes a new {@link Session}.
     *
     * @param socketIOManager The session manager
     * @param sessionId The session identifier
     */
    Session(SocketIOManager socketIOManager, String sessionId) {
        super();
        assert (socketIOManager != null);
        this.socketIOManager = socketIOManager;
        this.sessionId = sessionId;
    }

    /**
     * Creates a new socket and associates it to specified namespace.
     *
     * @param ns The identifier of the namespace to which the new socket is supposed to be bound
     * @return The new socket
     */
    public Socket createSocket(String ns) {
        Namespace namespace = socketIOManager.getNamespace(ns);
        if (namespace == null) {
            throw new IllegalArgumentException("Namespace does not exist");
        }

        Socket socket = namespace.createSocket(this);
        socket.on(this); // listen for disconnect event
        synchronized (this) {
            sockets.put(ns, socket);
        }
        return socket;
    }

    /**
     * Associates an attribute with this session
     *
     * @param key The attribute name
     * @param val The attribute value
     */
    public void setAttribute(String key, Object val) {
        attributes.put(key, val);
    }

    /**
     * Gets the attribute associated with given name.
     *
     * @param key The attribute name
     * @return The attribute value or <code>null</code> (if there is no such attribute)
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Gets the session identifier.
     *
     * @return The session identifier
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the current state of the active connection.
     *
     * @return The connection state
     */
    public synchronized ConnectionState getConnectionState() {
        return state;
    }

    /**
     * Gets the active connection, which is set during connect.
     *
     * @return The active connection
     */
    public synchronized TransportConnection getConnection() {
        return activeConnection;
    }

    /**
     * Resets this session's timeout.
     */
    public synchronized void resetTimeout() {
        clearTimeout();

        if (timedOut) {
            return;
        }

        long timeout = this.timeout;
        if (timeout == 0) {
            return;
        }

        timeoutTask = socketIOManager.getTimerService().schedule(new Runnable() {

            @Override
            public void run() {
                Session.this.onTimeout();
            }
        }, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Clears the timeout.
     * <p>
     * Stops the background timer task.
     */
    public synchronized void clearTimeout() {
        ScheduledTimerTask timeoutTask = this.timeoutTask;
        if (timeoutTask != null) {
            this.timeoutTask = null;
            timeoutTask.cancel(false);
        }
    }

    /**
     * Sets the timeout for this session. <code>0</code> (zero) is infinite.
     *
     * @param timeout The timeout (in milliseconds)
     */
    public synchronized void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets the timeout for this session.
     *
     * @return The timeout (in milliseconds); <code>0</code> (zero) is infinite
     */
    public synchronized long getTimeout() {
        return timeout;
    }

    private void onBinary(InputStream is) throws SocketIOProtocolException {
        BinaryPacket binaryPacket = this.binaryPacket;
        if (binaryPacket == null) {
            throw new SocketIOProtocolException("Unexpected binary object");
        }

        SocketIOProtocol.insertBinaryObject(binaryPacket, is);
        binaryPacket.addAttachment(is); //keeping copy of all attachments in attachments list
        if (binaryPacket.isComplete()) {
            if (binaryPacket.getType() == SocketIOPacket.Type.BINARY_EVENT) {
                onEvent((EventPacket) binaryPacket);
            } else if (binaryPacket.getType() == SocketIOPacket.Type.BINARY_ACK) {
                onACK((ACKPacket) binaryPacket);
            }

            binaryPacket = null;
        }
    }

    /**
     * The call-back when a new connection is established.
     *
     * @param connection The new connection to associate with his session
     * @throws SocketIOException If a Socket.IO violation occurs; e.g. already connected
     */
    public synchronized void onConnect(TransportConnection connection) throws SocketIOException {
        if (null == connection) {
            throw new AssertionError("connection is null");
        }
        if (null != activeConnection) {
            throw new AssertionError("active connection already set");
        }

        activeConnection = connection;

        Socket socket = createSocket(SocketIOProtocol.DEFAULT_NAMESPACE);
        try {
            connection.send(SocketIOProtocol.createConnectPacket(SocketIOProtocol.DEFAULT_NAMESPACE));
            state = ConnectionState.CONNECTED;
            socketIOManager.getNamespace(SocketIOProtocol.DEFAULT_NAMESPACE).onConnect(socket); // callback
        } catch (ConnectionException e) {
            LOGGER.debug("Connection failed", e);
            connection.send(SocketIOProtocol.createErrorPacket(SocketIOProtocol.DEFAULT_NAMESPACE, e.getArgs()));
            closeConnection(DisconnectReason.CONNECT_FAILED, connection);
        }
    }

    /**
     * Optional. If transport knows detailed error message, it could be set before calling onShutdown()
     *
     * @param message The detailed explanation of the disconnect reason
     */
    public synchronized void setDisconnectMessage(String message) {
        this.disconnectMessage = message;
    }

    /**
     * Calling this method will close the active connection.
     * <p>
     * Setting its state to <code>CLOSING</code>.
     *
     * @param reason The session disconnect reason
     */
    public synchronized void setDisconnectReason(DisconnectReason reason) {
        this.state = ConnectionState.CLOSING;
        this.disconnectReason = reason;
    }

    /**
     * callback to be called by transport activeConnection socket is closed.
     */
    public synchronized void onShutdown() {
        if (state == ConnectionState.CLOSING) {
            onDisconnect(disconnectReason);
        } else {
            onDisconnect(DisconnectReason.ERROR);
        }
    }

    /**
     * Disconnect callback. to be called by session itself. Transport activeConnection should always call onShutdown()
     */
    private void onDisconnect(DisconnectReason reason) {
        LOGGER.debug("Session[{}]: onDisconnect: {} message: [{}]", sessionId, reason, disconnectMessage);

        if (state == ConnectionState.CLOSED) {
            return; // to prevent calling it twice
        }

        state = ConnectionState.CLOSED;

        clearTimeout();

        // taking copy of sockets because
        // session will be modifying the collection while iterating
        for (Object o : sockets.values().toArray()) {
            Socket socket = (Socket) o;
            socket.onDisconnect(socket, reason, disconnectMessage);
        }

        socketIOManager.deleteSession(sessionId);
    }

    synchronized void onTimeout() {
        LOGGER.debug("Session[{}]: onTimeout", sessionId);

        if (!timedOut) {
            timedOut = true;
            closeConnection(DisconnectReason.TIMEOUT, activeConnection);
        }
    }

    /**
     * Handles an incoming packet.
     *
     * @param packet The packet
     * @param connection The connection that received the packet
     */
    public synchronized void onPacket(EngineIOPacket packet, TransportConnection connection) {
        switch (packet.getType()) {
            case OPEN:
            case PONG:
                // ignore. OPEN and PONG are server -> client only
                return;

            case MESSAGE:
                resetTimeout();
                try {
                    if (packet.getTextData() != null) {
                        onPacket(SocketIOProtocol.decode(packet.getTextData()));
                    } else if (packet.getBinaryData() != null) {
                        onBinary(packet.getBinaryData());
                    }
                } catch (SocketIOProtocolException e) {
                    LOGGER.warn("Invalid SIO packet: {}", packet.getTextData(), e);
                }
                return;

            case PING:
                resetTimeout();
                onPing(packet.getTextData(), connection);

                // ugly hack to replicate current sio client behaviour
                if (connection != getConnection()) {
                    forcePollingCycle();
                }

                return;

            case CLOSE:
                closeConnection(DisconnectReason.CLOSED_REMOTELY, connection);
                return;

            case UPGRADE:
                upgradeConnection(connection);
                return;

            default:
                throw new UnsupportedOperationException("EIO Packet " + packet + " is not implemented yet");

        }
    }

    private void onPacket(SocketIOPacket packet) {
        switch (packet.getType()) {
            case CONNECT:
                try {
                    if (socketIOManager.getNamespace(packet.getNamespace()) == null) {
                        getConnection().send(SocketIOProtocol.createErrorPacket(packet.getNamespace(), "Invalid namespace"));
                        return;
                    }
                    Socket socket = createSocket(packet.getNamespace());
                    getConnection().send(SocketIOProtocol.createConnectPacket(packet.getNamespace()));
                    try {
                        socketIOManager.getNamespace(socket.getNamespace()).onConnect(socket);
                    } catch (ConnectionException e) {
                        getConnection().send(SocketIOProtocol.createErrorPacket(socket.getNamespace(), e.getArgs()));
                        socket.disconnect(false);
                    }
                } catch (SocketIOException e) {
                    LOGGER.debug("Cannot send packet to the client", e);
                    closeConnection(DisconnectReason.CONNECT_FAILED, activeConnection);
                }
                return;

            case DISCONNECT:
                closeConnection(DisconnectReason.CLOSED_REMOTELY, activeConnection);
                return;

            case EVENT:
                onEvent((EventPacket) packet);
                return;

            case ACK:
                onACK((ACKPacket) packet);
                return;

            case BINARY_ACK:
            case BINARY_EVENT:
                binaryPacket = (BinaryPacket) packet;
                return;

            default:
                throw new UnsupportedOperationException("SocketIO packet " + packet.getType() + " is not implemented yet");
        }
    }

    private void onPing(String data, TransportConnection connection) {
        try {
            connection.send(EngineIOProtocol.createPongPacket(data));
        } catch (SocketIOException e) {
            LOGGER.debug("connection.send failed: ", e);

            closeConnection(DisconnectReason.ERROR, connection);
        }
    }

    private void onEvent(EventPacket packet) {
        if (state != ConnectionState.CONNECTED) {
            return;
        }

        try {
            Namespace ns = socketIOManager.getNamespace(packet.getNamespace());
            if (ns == null) {
                getConnection().send(SocketIOProtocol.createErrorPacket(packet.getNamespace(), "Invalid namespace"));
                return;
            }

            Socket socket = sockets.get(ns.getId());
            if (socket == null) {
                activeConnection.send(SocketIOProtocol.createErrorPacket(packet.getNamespace(), "No socket is connected to the namespace"));
                return;
            }

            boolean ackRequested = packet.getId() != -1;
            Object ack = socket.onEvent(packet.getName(), packet.getArgs(), ackRequested);

            if (ackRequested && ack != null) {
                Object[] args;
                if (ack instanceof Objects[]) {
                    args = (Object[]) ack;
                } else {
                    args = new Object[] { ack };
                }

                activeConnection.send(SocketIOProtocol.createACKPacket(packet.getId(), packet.getNamespace(), args));
            }
        } catch (Throwable e) {
            LOGGER.warn("Session[{}]: Exception thrown by one of the event listeners", sessionId, e);
        }
    }

    private void onACK(ACKPacket packet) {
        if (state != ConnectionState.CONNECTED) {
            return;
        }

        try {
            ACKListener listener = ack_listeners.get(packet.getId());
            unsubscribeACK(packet.getId());
            if (listener != null) {
                listener.onACK(packet.getArgs());
            }
        } catch (Throwable e) {
            LOGGER.warn("Session[{}]: Exception thrown by ACK listener", sessionId, e);
        }
    }

    private synchronized void upgradeConnection(TransportConnection connection) {
        LOGGER.debug("Upgrading from {} to {}", activeConnection.getTransport(), connection.getTransport());
        activeConnection = connection;
    }

    /**
     * Remembers the disconnect reason and closes underlying transport activeConnection
     */
    private void closeConnection(DisconnectReason reason, TransportConnection connection) {
        if (this.activeConnection == connection) {
            setDisconnectReason(reason);
        }
        connection.abort(); //this call should trigger onShutdown() eventually
    }

    /**
     * Yields the next packet identifier.
     *
     * @return The next packet identifier
     */
    public int getNewPacketId() {
        return packetId.getAndIncrement();
    }

    //TODO: what if ACK never comes? We will have a memory leak. Need to cleanup the list or fail on timeout?
    public synchronized void subscribeACK(int packet_id, ACKListener ack_listener) {
        ack_listeners.put(Integer.valueOf(packet_id), ack_listener);
    }

    public synchronized void unsubscribeACK(int packet_id) {
        ack_listeners.remove(Integer.valueOf(packet_id));
    }

    @Override
    public synchronized void onDisconnect(Socket socket, DisconnectReason reason, String errorMessage) {
        sockets.remove(socket.getNamespace());
    }

    // hack to replicate current Socket.IO client behaviour
    private void forcePollingCycle() {
        try {
            getConnection().send(EngineIOProtocol.createNoopPacket());
        } catch (SocketIOException e) {
            LOGGER.warn("Cannot send NOOP packet while upgrading the transport", e);
        }
    }
}
