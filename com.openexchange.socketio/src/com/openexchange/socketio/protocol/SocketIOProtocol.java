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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.openexchange.socketio.server.SocketIOProtocolException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of Socket.IO Protocol version 4
 *
 * @author Alexander Sova (bird@codeminders.com)
 */
public final class SocketIOProtocol {

    private static final Logger LOGGER = Logger.getLogger(SocketIOProtocol.class.getName());

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    public static final String DEFAULT_NAMESPACE = "/";

    private static final String ATTACHMENTS_DELIMITER = "-";
    private static final String NAMESPACE_PREFIX = "/";
    static final String NAMESPACE_DELIMITER = ",";

    private SocketIOProtocol() {}

    private static class EmptyPacket extends SocketIOPacket {

        public EmptyPacket(Type type, String ns) {
            super(type, ns);
        }

        @Override
        protected String encodeArgs() {
            return "";
        }
    }

    private static class PlainACKPacket extends ACKPacket {

        public PlainACKPacket(int id, String ns, Object[] args) {
            super(Type.ACK, id, ns, args);
        }
    }

    private static class PlainEventPacket extends EventPacket {

        public PlainEventPacket(int id, String ns, String name, Object[] args) {
            super(Type.EVENT, id, ns, name, args);
        }
    }

    public static SocketIOPacket decode(String data) throws SocketIOProtocolException {
        assert (data != null);

        if (data.length() < 1) {
            throw new SocketIOProtocolException("Empty SIO packet");
        }

        try {
            ParsePosition pos = new ParsePosition(0);
            SocketIOPacket.Type type = decodePacketType(data, pos);

            int attachments = 0;
            if (type == SocketIOPacket.Type.BINARY_ACK || type == SocketIOPacket.Type.BINARY_EVENT) {
                attachments = decodeAttachments(data, pos);
            }

            String ns = decodeNamespace(data, pos);
            int packet_id = decodePacketId(data, pos);
            Object json = decodeArgs(data, pos);

            List args = null;
            String eventName = "";
            if (type == SocketIOPacket.Type.EVENT || type == SocketIOPacket.Type.BINARY_EVENT || type == SocketIOPacket.Type.ACK || type == SocketIOPacket.Type.BINARY_ACK) {
                if (!(json instanceof List)) {
                    throw new SocketIOProtocolException("Array payload is expected");
                }

                args = (List) json;

                if (type == SocketIOPacket.Type.EVENT || type == SocketIOPacket.Type.BINARY_EVENT) {
                    if (args.size() == 0) {
                        throw new SocketIOProtocolException("Missing event name");
                    }
                    eventName = args.get(0).toString();
                    args.remove(0);
                }
            }

            switch (type) {
                case CONNECT:
                    return createConnectPacket(ns);

                case DISCONNECT:
                    return createDisconnectPacket(ns);

                case EVENT:
                    return new PlainEventPacket(packet_id, ns, eventName, args.toArray());

                case ACK:
                    return new PlainACKPacket(packet_id, ns, args.toArray());

                case ERROR:
                    return createErrorPacket(ns, json);

                case BINARY_EVENT:
                    return new BinaryEventPacket(packet_id, ns, eventName, args.toArray(), attachments);

                case BINARY_ACK:
                    assert (args != null); //just to make IDEA to shut up about possible NPE
                    return new BinaryACKPacket(packet_id, ns, args.toArray(), attachments);

                default:
                    throw new SocketIOProtocolException("Unsupported packet type " + type);
            }
        } catch (NumberFormatException | SocketIOProtocolException e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "Invalid SIO packet: " + data, e);
            }

            throw new SocketIOProtocolException("Invalid SIO packet: " + data, e);
        }
    }

    public static SocketIOPacket createErrorPacket(String namespace, final Object args) {
        return new SocketIOPacket(SocketIOPacket.Type.ERROR, namespace) {

            @Override
            protected String encodeArgs() throws SocketIOProtocolException {
                return toJSON(args);
            }
        };
    }

    /*
     * This method could create either EventPacket or BinaryEventPacket based
     * on the content of args parameter.
     * If args has any InputStream inside then SockeIOBinaryEventPacket will be created
     */
    public static SocketIOPacket createEventPacket(int packet_id, String ns, String name, Object[] args) {
        if (hasBinary(args)) {
            return new BinaryEventPacket(packet_id, ns, name, args);
        } else {
            return new PlainEventPacket(packet_id, ns, name, args);
        }
    }

    public static SocketIOPacket createACKPacket(int id, String ns, Object[] args) {
        if (hasBinary(args)) {
            return new BinaryACKPacket(id, ns, args);
        } else {
            return new PlainACKPacket(id, ns, args);
        }
    }

    public static SocketIOPacket createDisconnectPacket(String ns) {
        return new EmptyPacket(SocketIOPacket.Type.DISCONNECT, ns);
    }

    public static SocketIOPacket createConnectPacket(String ns) {
        return new EmptyPacket(SocketIOPacket.Type.CONNECT, ns);
    }

    static String toJSON(Object o) throws SocketIOProtocolException {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new SocketIOProtocolException("Cannot convert object to JSON", e);
        }
    }

    static Object fromJSON(String s) throws SocketIOProtocolException {
        try {
            if (s == null || s.isEmpty()) {
                return null;
            }

            return mapper.readValue(s, Object.class);
        } catch (IOException e) {
            throw new SocketIOProtocolException("Cannot parse JSON", e);
        }
    }

    static String decodeNamespace(String data, ParsePosition pos) {
        String ns = DEFAULT_NAMESPACE;
        if (data.startsWith(NAMESPACE_PREFIX, pos.getIndex())) {
            int idx = data.indexOf(NAMESPACE_DELIMITER, pos.getIndex());
            if (idx < 0) {
                ns = data.substring(pos.getIndex());
                pos.setIndex(data.length());
            } else {
                ns = data.substring(pos.getIndex(), idx);
                pos.setIndex(idx + 1);
            }
        }
        return ns;
    }

    static int decodeAttachments(String data, ParsePosition pos) throws SocketIOProtocolException {
        Number n = new DecimalFormat("#").parse(data, pos);
        if (n == null || n.intValue() == 0) {
            throw new SocketIOProtocolException("No attachments defined in BINARY packet: " + data);
        }

        pos.setIndex(pos.getIndex() + 1); //skipping '-' delimiter

        return n.intValue();
    }

    static int decodePacketId(String data, ParsePosition pos) {
        Number id = new DecimalFormat("#").parse(data, pos);
        if (id == null) {
            return -1;
        }

        return id.intValue();
    }

    static SocketIOPacket.Type decodePacketType(String data, ParsePosition pos) throws SocketIOProtocolException {
        int idx = pos.getIndex();
        SocketIOPacket.Type type = SocketIOPacket.Type.fromInt(Integer.parseInt(data.substring(idx, idx + 1)));
        pos.setIndex(idx + 1);
        return type;
    }

    //TODO: pass what type (Array, Map, Object) is expected?
    static Object decodeArgs(String data, ParsePosition pos) throws SocketIOProtocolException {
        Object json = fromJSON(data.substring(pos.getIndex()));
        pos.setIndex(data.length());
        return json;
    }

    static String encodeNamespace(String namespace, boolean addDelimiter) {
        if (namespace.equals(SocketIOProtocol.DEFAULT_NAMESPACE)) {
            return "";
        }
        return namespace + (addDelimiter ? SocketIOProtocol.NAMESPACE_DELIMITER : "");
    }

    static String encodeAttachments(int size) {
        return String.valueOf(size) + ATTACHMENTS_DELIMITER;
    }

    private static boolean hasBinary(Object args) {
        if (args.getClass().isArray()) {
            for (Object o : (Object[]) args) {
                if (hasBinary(o)) {
                    return true;
                }
            }
        } else if (args instanceof Map) {
            for (Object o : ((Map) args).values()) {
                if (hasBinary(o)) {
                    return true;
                }
            }
        } else {
            return (args instanceof InputStream);
        }

        return false;
    }

    /**
     * Extracts binary objects (InputStream) from JSON and replaces it with
     * placeholder objects {@code {"_placeholder":true,"num":1} }
     * This method to be used before sending the packet
     *
     * @param json JSON object
     * @param attachments container for extracted binary object
     * @return modified JSON object
     */
    @SuppressWarnings("unchecked")
    static Object extractBinaryObjects(Object json, List<InputStream> attachments) {
        //TODO: what about Collection? for now only array is supported
        if (json.getClass().isArray()) {
            ArrayList<Object> array = new ArrayList<>(((Object[]) json).length);

            for (Object o : (Object[]) json) {
                array.add(extractBinaryObjects(o, attachments));
            }

            return array.toArray();
        } else if (json instanceof Map) {
            Map<Object, Object> map = new LinkedHashMap<>();
            Set<Map.Entry> entries = ((Map) json).entrySet();

            for (Map.Entry e : entries) {
                map.put(e.getKey(), extractBinaryObjects(e, attachments));
            }

            return map;
        } else if (json instanceof InputStream) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("_placeholder", true);
            map.put("num", attachments.size());
            attachments.add((InputStream) json);

            return map;
        } else {
            return json;
        }
    }

    /**
     * Looks for the placeholder objects in {@code json.getArgs() } {@code {"_placeholder":true,"num":1}} and
     * replaces it with {@code attachment}
     * This method to be used when binary object are received from the client
     *
     * @param packet packet to add a binary object
     * @param attachment binary object to insert
     * @throws SocketIOProtocolException if no placeholder object is found
     */
    public static void insertBinaryObject(BinaryPacket packet, InputStream attachment) throws SocketIOProtocolException {
        boolean[] found = new boolean[1];

        Object copy = insertBinaryObject(packet.getArgs(), attachment, packet.getAttachments().size(), found);
        if (!found[0]) {
            throw new SocketIOProtocolException("No placeholder found for a binary object");
        }

        packet.setArgs((Object[]) copy);
    }

    /*
     * This method makes a copy of {@code json} replacing placeholder entry with {@code attachment}
     *
     * @param json JSON object
     *
     * @param attachment InputStream object to insert
     *
     * @return copy of JSON object
     */
    @SuppressWarnings("unchecked")
    private static Object insertBinaryObject(Object json, InputStream attachment, int index, boolean[] found) throws SocketIOProtocolException {
        //TODO: what about Collection? for now only array is supported
        if (json.getClass().isArray()) {
            ArrayList<Object> copy = new ArrayList<>(((Object[]) json).length);

            for (Object o : (Object[]) json) {
                copy.add(insertBinaryObject(o, attachment, index, found));
            }

            return copy.toArray();
        } else if (json instanceof Map) {
            Map<Object, Object> map = (Map) json;

            if (isPlaceholder(map, index)) {
                found[0] = true;
                return attachment;
            }

            Map<Object, Object> copy = new LinkedHashMap<>();
            Set<Map.Entry<Object, Object>> entries = map.entrySet();

            for (Map.Entry e : entries) {
                copy.put(e.getKey(), insertBinaryObject(e.getValue(), attachment, index, found));
            }

            return copy;
        } else {
            return json;
        }
    }

    private static boolean isPlaceholder(Map<Object, Object> map, int index) throws SocketIOProtocolException {
        if (Boolean.TRUE.equals(map.get("_placeholder"))) {
            Object o = map.get("num");

            if (o == null) {
                return false;
            }

            if (o instanceof Number) {
                return index == ((Number) o).intValue();
            }

            if (o instanceof String) {
                try {
                    return index == Integer.parseInt(o.toString());
                } catch (NumberFormatException e) {
                    throw new SocketIOProtocolException("Invalid placeholder object", e);
                }
            }
        }
        return false;
    }
}
