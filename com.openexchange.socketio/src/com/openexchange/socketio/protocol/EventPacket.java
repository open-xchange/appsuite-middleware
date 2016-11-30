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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.socketio.server.SocketIOProtocolException;

/**
 * @author Alexander Sova (bird@codeminders.com)
 */
public abstract class EventPacket extends SocketIOPacket {

    private final String name;
    private Object[] args;

    protected EventPacket(Type type, int id, String ns, String name, Object[] args) {
        super(type, id, ns);
        this.name = name;
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    protected String encodeArgs() throws SocketIOProtocolException {
        Object[] args = getArgs();
        // adding name of the event as a first argument
        JSONArray data = new JSONArray(args.length + 1);

        // append arguments
        data.put(getName());
        for (Object arg : args) {
            data.put(coerceToJSON(arg));
        }
        return data.toString();
    }

    /**
     * Coerces given Java object to its JSON representation.
     *
     * @param value The Java object to coerce
     * @return The resulting JSON representation
     */
    private Object coerceToJSON(final Object value) {
        if (null == value || JSONObject.NULL.equals(value)) {
            return JSONObject.NULL;
        }

        if (value instanceof JSONValue) {
            return value;
        }

        if (value instanceof Map) {
            return new JSONObject((Map<String, ?>) value);
        }

        if (value instanceof Collection) {
            return new JSONArray((Collection<?>) value);
        }

        if (isArray(value)) {
            return new JSONArray(Arrays.asList((Object[]) value));
        }

        if (value instanceof String) {
            return value.toString();
        }

        if (value instanceof Number) {
            Number n = (Number) value;
            if (n instanceof AtomicInteger) {
                return Integer.valueOf(n.intValue());
            } else if (n instanceof AtomicLong) {
                return Long.valueOf(n.longValue());
            } else {
                return n;
            }
        }

        if (value instanceof byte[]) {
            return (value);
        }

        if (value instanceof Boolean) {
            return (value);
        }
        if (value instanceof AtomicBoolean) {
            return Boolean.valueOf(((AtomicBoolean) value).get());
        }

        // As string as last resort
        return value.toString();
    }

    /**
     * Checks if specified object is an array.
     *
     * @param object The object to check
     * @return <code>true</code> if specified object is an array; otherwise <code>false</code>
     */
    private boolean isArray(final Object object) {
        /*-
         * getClass().isArray() is significantly slower on Sun Java 5 or 6 JRE than on IBM.
         * So much that using clazz.getName().charAt(0) == '[' is faster on Sun JVM.
         */
        // return (null != object && object.getClass().isArray());
        return (null != object && '[' == object.getClass().getName().charAt(0));
    }
}
