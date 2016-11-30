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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.common.io.ByteStreams;
import com.openexchange.socketio.server.SocketIOProtocolException;

/**
 * Implementation of Engine.IO Protocol version 3
 *
 * @author Alexander Sova (bird@codeminders.com)
 */
public final class EngineIOProtocol {

    public static final String SESSION_ID = "sid";
    public static final String TRANSPORT = "transport";
    public static final String JSONP_INDEX = "j";
    public static final String BASE64_FLAG = "b64";
    public static final String VERSION = "EIO";

    private EngineIOProtocol() {
        super();
    }

    public static String encode(EngineIOPacket packet) {
        return String.valueOf(packet.getType().value()) + packet.getTextData();
    }

    public static void binaryEncode(EngineIOPacket packet, OutputStream os) throws IOException {
        if (packet.getBinaryData() != null) {
            ByteArrayInputStream bytes;
            InputStream is = packet.getBinaryData();
            if (is instanceof ByteArrayInputStream) {
                bytes = (ByteArrayInputStream) is;
            } else {
                // Cannot avoid double copy. The protocol requires to send the length before the data
                //TODO: ask user to provide length? Could be useful to send files
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                ByteStreams.copy(is, buffer);
                bytes = new ByteArrayInputStream(buffer.toByteArray());
            }
            os.write(1); // binary packet
            os.write(encodeLength(bytes.available() + 1)); // +1 for packet type
            os.write(255);
            os.write(packet.getType().value());
            ByteStreams.copy(bytes, os);
        } else {
            assert (packet.getTextData() != null);

            os.write(0); // text packet
            os.write(encodeLength(packet.getTextData().length() + 1)); // +1 for packet type
            os.write(255);
            os.write(packet.getType().value() + '0');
            os.write(packet.getTextData().getBytes("UTF-8"));
        }

    }

    //this is most ridiculous encoding I ever seen
    private static byte[] encodeLength(int len) {
        byte[] bytes = String.valueOf(len).getBytes();
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] -= '0';
        }
        return bytes;
    }

    public static EngineIOPacket decode(String raw) throws SocketIOProtocolException {
        assert (raw != null);

        if (raw.length() < 1) {
            throw new SocketIOProtocolException("Empty EIO packet");
        }

        try {
            return new EngineIOPacket(EngineIOPacket.Type.fromInt(Integer.parseInt(raw.substring(0, 1))), raw.substring(1));
        } catch (NumberFormatException e) {
            throw new SocketIOProtocolException("Invalid EIO packet type: " + raw);
        }
    }

    public static EngineIOPacket decode(InputStream raw) throws SocketIOProtocolException {
        assert (raw != null);

        try {
            int type = raw.read();
            if (type == -1) {
                throw new SocketIOProtocolException("Empty binary object received");
            }

            return new EngineIOPacket(EngineIOPacket.Type.fromInt(type), raw);
        } catch (IOException e) {
            throw new SocketIOProtocolException("Cannot read packet type from binary object");
        }
    }

    public static EngineIOPacket createHandshakePacket(String session_id, String[] upgrades, long ping_interval, long ping_timeout) {
        try {
            JSONObject jMap = new JSONObject(6);
            jMap.put("sid", session_id);
            jMap.put("upgrades", new JSONArray(Arrays.asList(upgrades)));
            jMap.put("pingInterval", ping_interval);
            jMap.put("pingTimeout", ping_timeout);
            return new EngineIOPacket(EngineIOPacket.Type.OPEN, jMap.toString());
        } catch (Exception e) {
            // ignore. never happen
            return null;
        }
    }

    public static EngineIOPacket createOpenPacket() {
        return new EngineIOPacket(EngineIOPacket.Type.OPEN);
    }

    public static EngineIOPacket createClosePacket() {
        return new EngineIOPacket(EngineIOPacket.Type.CLOSE);
    }

    public static EngineIOPacket createPingPacket(String data) {
        return new EngineIOPacket(EngineIOPacket.Type.PING, data);
    }

    public static EngineIOPacket createPongPacket(String data) {
        return new EngineIOPacket(EngineIOPacket.Type.PONG, data);
    }

    public static EngineIOPacket createMessagePacket(String data) {
        return new EngineIOPacket(EngineIOPacket.Type.MESSAGE, data);
    }

    public static EngineIOPacket createMessagePacket(InputStream data) {
        return new EngineIOPacket(EngineIOPacket.Type.MESSAGE, data);
    }

    public static EngineIOPacket createUpgradePacket() {
        return new EngineIOPacket(EngineIOPacket.Type.UPGRADE);
    }

    public static EngineIOPacket createNoopPacket() {
        return new EngineIOPacket(EngineIOPacket.Type.NOOP);
    }

    public static List<EngineIOPacket> decodePayload(String payload) throws SocketIOProtocolException {
        ArrayList<EngineIOPacket> packets = new ArrayList<>();

        ParsePosition pos = new ParsePosition(0);

        while (pos.getIndex() < payload.length()) {
            int len = decodePacketLength(payload, pos);
            EngineIOPacket.Type type = decodePacketType(payload, pos);
            String data = payload.substring(pos.getIndex(), pos.getIndex() + len - 1);
            pos.setIndex(pos.getIndex() + 1 + len);

            switch (type) {
                case CLOSE:
                    packets.add(createClosePacket());
                    break;
                case PING:
                    packets.add(createPingPacket(data));
                    break;
                case MESSAGE:
                    packets.add(createMessagePacket(data));
                    break;
                case UPGRADE:
                    packets.add(createUpgradePacket());
                    break;
                case NOOP:
                    packets.add(createNoopPacket());
                    break;
                default:
                    throw new SocketIOProtocolException("Unexpected EIO packet type: " + type);
            }
        }

        return packets;
    }

    static int decodePacketLength(String data, ParsePosition pos) throws SocketIOProtocolException {
        Number len = new DecimalFormat("#").parse(data, pos);
        if (len == null) {
            throw new SocketIOProtocolException("No packet length defined");
        }

        pos.setIndex(pos.getIndex() + 1);

        return len.intValue();
    }

    static EngineIOPacket.Type decodePacketType(String data, ParsePosition pos) throws SocketIOProtocolException {
        int idx = pos.getIndex();
        EngineIOPacket.Type type = EngineIOPacket.Type.fromInt(Integer.parseInt(data.substring(idx, idx + 1)));
        pos.setIndex(idx + 1);
        return type;
    }

    static int decodePacketLength(InputStream is) throws IOException {
        int len = 0;

        while (true) {
            int b = is.read();
            if (b < 0) {
                return -1; // end of stream. time to go
            }
            if (b > 9) {
                break; // end of encoded length
            }
            len = len * 10 + b;
        }

        return len;
    }

    static EngineIOPacket.Type decodePacketType(InputStream is) throws IOException {
        int i = is.read();
        if (i < 0) {
            throw new SocketIOProtocolException("Unexpected end of stream");
        }
        return EngineIOPacket.Type.fromInt(i);
    }

    public static List<EngineIOPacket> binaryDecodePayload(InputStream is) throws IOException {
        ArrayList<EngineIOPacket> packets = new ArrayList<>();
        if (is.read() != 1) {
            throw new SocketIOProtocolException("Expected binary marker in the payload");
        }

        while (true) {
            int len = decodePacketLength(is);
            if (len < 0) {
                break;
            }

            if (len == 0) {
                throw new SocketIOProtocolException("Empty binary attahcment");
            }

            EngineIOPacket.Type type = decodePacketType(is);
            len -= 1;
            byte[] data = new byte[len];
            ByteStreams.readFully(is, data);
            switch (type) {
                case MESSAGE:
                    packets.add(createMessagePacket(new ByteArrayInputStream(data)));
                    break;
                default:
                    throw new SocketIOProtocolException("Unexpected EIO packet type: " + type);
            }
        }

        return packets;
    }

}
