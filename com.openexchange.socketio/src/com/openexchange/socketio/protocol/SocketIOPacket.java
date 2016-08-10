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

package com.openexchange.socketio.protocol;

import com.openexchange.socketio.server.SocketIOProtocolException;

/**
 * @author Alexander Sova (bird@codeminders.com)
 */
public abstract class SocketIOPacket {

    public enum Type {
        CONNECT(0),
        DISCONNECT(1),
        EVENT(2),
        ACK(3),
        ERROR(4),
        BINARY_EVENT(5),
        BINARY_ACK(6);

        private int value;

        Type(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Type fromInt(int i) throws SocketIOProtocolException {
            switch (i) {
                case 0:
                    return CONNECT;
                case 1:
                    return DISCONNECT;
                case 2:
                    return EVENT;
                case 3:
                    return ACK;
                case 4:
                    return ERROR;
                case 5:
                    return BINARY_EVENT;
                case 6:
                    return BINARY_ACK;
                default:
                    throw new SocketIOProtocolException("Unexpected packet type: " + i);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------

    private final int id;
    private final Type type;
    private final String namespace;

    protected SocketIOPacket(Type type) {
        this(type, SocketIOProtocol.DEFAULT_NAMESPACE);
    }

    protected SocketIOPacket(Type type, String namespace) {
        this(type, -1, namespace);
    }

    protected SocketIOPacket(Type type, int id, String namespace) {
        this.type = type;
        this.namespace = namespace;
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public String getNamespace() {
        return namespace;
    }

    public int getId() {
        return id;
    }

    protected abstract String encodeArgs() throws SocketIOProtocolException;

    protected String encodeAttachments() {
        return "";
    }

    private String encodePacketId() {
        if (id < 0) {
            return "";
        }

        return String.valueOf(id);
    }

    public String encode() throws SocketIOProtocolException {
        String str = String.valueOf(type.value());

        String tail = encodePacketId() + encodeArgs();

        str += encodeAttachments();
        str += SocketIOProtocol.encodeNamespace(namespace, !tail.isEmpty());
        str += tail;

        return str;
    }

}
