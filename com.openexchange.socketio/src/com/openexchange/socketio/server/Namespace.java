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

package com.openexchange.socketio.server;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.socketio.common.DisconnectReason;
import com.openexchange.socketio.common.MultipleSocketIOException;
import com.openexchange.socketio.common.SocketIOException;

/**
 * @author Alexander Sova (bird@codeminders.com)
 */
public class Namespace implements Outbound, ConnectionListener, DisconnectListener {

    private final String id;

    private final Queue<Socket> sockets;
    private final Queue<ConnectionListener> connectionListeners;
    private final ConcurrentMap<String, Room> rooms;

    Namespace(String id) {
        super();
        this.id = id;
        sockets = new ConcurrentLinkedQueue<>();
        connectionListeners = new ConcurrentLinkedQueue<>();
        rooms = new ConcurrentHashMap<>();
    }

    public String getId() {
        return id;
    }

    @Override
    public void emit(String name, Object... args) throws SocketIOException {
        List<SocketIOException> exceptions = null;
        for (Socket s : sockets) {
            try {
                s.emit(name, args);
            } catch (SocketIOException e) {
                if (null == exceptions) {
                    exceptions = new LinkedList<>();
                }
                exceptions.add(e);
            }
        }

        if (null != exceptions) {
            throw MultipleSocketIOException.chainedSocketIOExceptionFor(exceptions);
        }
    }

    public void on(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    @Override
    public void onConnect(Socket socket) throws ConnectionException {
        for (ConnectionListener listener : connectionListeners) {
            listener.onConnect(socket);
        }
    }

    public Socket createSocket(Session session) {
        Socket socket = new Socket(session, this);
        socket.on(this);
        sockets.add(socket);

        return socket;
    }

    @Override
    public void onDisconnect(Socket socket, DisconnectReason reason, String errorMessage) {
        leaveAll(socket);
        sockets.remove(socket);
    }

    /**
     * Finds or creates a room.
     *
     * @param roomId room id
     * @return Room object
     */
    public Room room(String roomId) {
        Room room = rooms.get(roomId);
        if (null == room) {
            Room newroom = new Room(roomId);
            room = rooms.putIfAbsent(roomId, newroom);
            if (null == room) {
                room = newroom;
            }
        }
        return room;
    }

    /**
     * Finds or creates a room.
     *
     * @param roomId room id
     * @return Room object
     */
    public Room in(String roomId) {
        return room(roomId);
    }

    void leaveAll(Socket socket) {
        for (Room room : rooms.values()) {
            if (room.contains(socket)) {
                room.leave(socket);
            }
        }
    }
}
