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

import java.io.InputStream;

/**
 * @author Alexander Sova (bird@codeminders.com)
 */
public class EngineIOPacket {

    public enum Type {
        OPEN(0),
        CLOSE(1),
        PING(2),
        PONG(3),
        MESSAGE(4),
        UPGRADE(5),
        NOOP(6),
        UNKNOWN(-1);

        private int value;

        Type(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Type fromInt(int i) {
            switch (i) {
                case 0:
                    return OPEN;
                case 1:
                    return CLOSE;
                case 2:
                    return PING;
                case 3:
                    return PONG;
                case 4:
                    return MESSAGE;
                case 5:
                    return UPGRADE;
                case 6:
                    return NOOP;
                default:
                    return UNKNOWN;
            }
        }
    }

    private final Type type;
    private final String textData;
    private final InputStream binaryData;

    public EngineIOPacket(Type type, String data) {
        super();
        this.type = type;
        this.textData = data;
        binaryData = null;
    }

    //TODO: support byte[] in addtion to InputStream
    public EngineIOPacket(Type type, InputStream binaryData) {
        super();
        this.type = type;
        this.binaryData = binaryData;
        textData = null;
    }

    public EngineIOPacket(Type type) {
        this(type, "");
    }

    public Type getType() {
        return type;
    }

    public String getTextData() {
        return textData;
    }

    public InputStream getBinaryData() {
        return binaryData;
    }

}
