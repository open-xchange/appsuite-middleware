/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.json.JSONWriter;
import com.openexchange.tools.stack.ArrayStack;
import com.openexchange.tools.stack.Stack;

/**
 * OXJSONWriter - extends <code>{@link JSONWriter}</code> but does not use an underlying instance of <code>java.io.Writer</code> rather than
 * creating JSON values thus this JSONWriter will always hold a valid and complete JSON value accessible through
 * <code>{@link #getObject()}</code>.
 * <p>
 * The <code>{@link #isEmpty()}</code> method indicates if no root object has been started, yet, whereby the
 * <code>{@link #isComplete()}</code> method indicates that the root object is already completed.
 * <p>
 * Check the <code>{@link #isJSONArray()}</code> and <code>{@link #isJSONObject()}</code> methods which indicate whether an instance of
 * <code>{@link JSONArray}</code> or <code>{@link JSONObject}</code> is the root object. <blockquote>
 *
 * <pre>
 * final OXJSONWriter w = new OXJSONWriter();
 * ...
 *
 * if (w.isJSONArray()) {
 *     final JSONArray jsonArr = (JSONArray) w.getObject();
 *     ...
 *
 * } else if (w.isJSONObject()) { //what else?!
 *     final JSONObject jsonObj = (JSONObject) w.getObject();
 *     ...
 *
 * }
 * </pre>
 *
 * </blockquote>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXJSONWriter extends JSONWriter {

    private static final int MODE_INIT = 0;

    private static final int MODE_ARR = 1;

    private static final int MODE_OBJ = 2;

    private static final int MODE_KEY = 3;

    private static final int MODE_DONE = 4;

    private int mode = MODE_INIT;

    private JSONValue jsonValue;

    private int jsonObjectType;

    private final Stack<StackObject> stackObjs = new ArrayStack<StackObject>(StackObject.class);

    private String key;

    /**
     * Default constructor to start with an empty JSON writer
     */
    public OXJSONWriter() {
        super(null);
    }

    /**
     * Creates a JSON writer that further writes to given JSON object
     * <p>
     * <b>NOTE</b>: To get this writer into a complete state the <code>{@link #endObject()}</code> method must be invoked since this
     * constructor implicitly puts the writer into the same state as <code>{@link #object()}</code> does.
     *
     * @param jsonObject - the JSON object to write to
     * @throws JSONException - if unbalanced
     */
    public OXJSONWriter(final JSONObject jsonObject) throws JSONException {
        super(null);
        jsonValue = jsonObject;
        jsonObjectType = MODE_OBJ;
        mode = MODE_OBJ;
        pushObject(jsonObject);
    }

    /**
     * Creates a JSON writer that further writes to given JSON array
     * <p>
     * <b>NOTE</b>: To get this writer into a complete state the <code>{@link #endArray()}</code> method must be invoked since this
     * constructor implicitly puts the writer into the same state as <code>{@link #array()}</code> does.
     *
     * @param jsonArray - the JSON array to write to
     * @throws JSONException - if unbalanced
     */
    public OXJSONWriter(final JSONArray jsonArray) throws JSONException {
        super(null);
        jsonValue = jsonArray;
        jsonObjectType = MODE_ARR;
        pushArray(jsonArray);
    }

    /*
     * (non-Javadoc)
     * @see org.json.JSONWriter#array()
     */
    @Override
    public JSONWriter array() throws JSONException {
        if (mode != MODE_INIT && mode != MODE_OBJ && mode != MODE_ARR) {
            throw new JSONException("Misplaced array. Current mode: " + mode2string());
        }
        final JSONArray ja = new JSONArray();
        if (mode == MODE_INIT) {
            jsonValue = ja;
            jsonObjectType = MODE_ARR;
        }
        pushArray(ja);
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.json.JSONWriter#endArray()
     */
    @Override
    public JSONWriter endArray() throws JSONException {
        if (mode != MODE_ARR) {
            throw new JSONException("Misplaced endArray. Current mode: " + mode2string());
        }
        pop();
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.json.JSONWriter#object()
     */
    @Override
    public JSONWriter object() throws JSONException {
        if (mode != MODE_INIT && mode != MODE_OBJ && mode != MODE_ARR) {
            throw new JSONException("Misplaced object. Current mode: " + mode2string());
        }
        final JSONObject jo = new JSONObject();
        if (mode == MODE_INIT) {
            jsonValue = jo;
            jsonObjectType = MODE_OBJ;
            mode = MODE_OBJ;
        }
        pushObject(jo);
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.json.JSONWriter#endObject()
     */
    @Override
    public JSONWriter endObject() throws JSONException {
        if (mode != MODE_KEY) {
            throw new JSONException("Misplaced endObject. Current mode: " + mode2string());
        }
        pop();
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.json.JSONWriter#key(java.lang.String)
     */
    @Override
    public JSONWriter key(final String key) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        }
        if (mode != MODE_KEY) {
            throw new JSONException("Misplaced key. Current mode: " + mode2string());
        }
        this.key = key;
        mode = MODE_OBJ;
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.json.JSONWriter#value(boolean)
     */
    @Override
    public JSONWriter value(final boolean b) throws JSONException {
        return append(Boolean.valueOf(b));
    }

    /*
     * (non-Javadoc)
     * @see org.json.JSONWriter#value(double)
     */
    @Override
    public JSONWriter value(final double d) throws JSONException {
        return this.value(Double.valueOf(d));
    }

    /*
     * (non-Javadoc)
     * @see org.json.JSONWriter#value(long)
     */
    @Override
    public JSONWriter value(final long l) throws JSONException {
        return append(Long.valueOf(l));
    }

    /*
     * (non-Javadoc)
     * @see org.json.JSONWriter#value(java.lang.Object)
     */
    @Override
    public JSONWriter value(final Object o) throws JSONException {
        return append(o);
    }

    /**
     * Checks if this writer is left in an complete state; meaning all opened objects - either JSONArrays or JSONObjects - were properly
     * closed through corresponding <code>{@link #endArray()}</code> or <code>{@link #endObject()}</code> routine.
     *
     * @return <code>true</code> if JSON object is complete; otherwise <code>false</code>
     */
    public boolean isComplete() {
        return (mode == MODE_DONE);
    }

    /**
     * Checks if initial JSON object is an instance of <code>JSONArray</code>
     *
     * @return <code>true</code> if initial JSON object is an instance of <code>JSONArray</code>; otherwise <code>false</code>
     */
    public boolean isJSONArray() {
        return (jsonObjectType == MODE_ARR);
    }

    /**
     * Checks if initial JSON object is an instance of <code>JSONObject</code>
     *
     * @return <code>true</code> if initial JSON object is an instance of <code>JSONObject</code>; otherwise <code>false</code>
     */
    public boolean isJSONObject() {
        return (jsonObjectType == MODE_OBJ);
    }

    /**
     * Getter for initial JSON object created through this writer
     *
     * @return The JSON object; either <code>JSONArray</code> or <code>JSONObject</code>
     */
    public JSONValue getObject() {
        return jsonValue;
    }

    /**
     * Checks if nothing has been written to this writer, yet
     *
     * @return <code>true</code> if nothing has been written to this writer; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return (mode == MODE_INIT);
    }

    /**
     * Checks if next action should be any of the <code>{@link #value(X)}</code> methods. Within JSON object it's really expected and no
     * other action is allowed, but within JSON array <code>{@link #endArray()}</code> is also allowed
     *
     * @return <code>true</code> if a value is expected; otherwise <code>false</code>
     */
    public boolean isExpectingValue() {
        return (mode == MODE_OBJ || mode == MODE_ARR);
    }

    /**
     * Checks if next action can be the <code>{@link #key(String)}</code> method.
     *
     * @return <code>true</code> if a key is expected; otherwise <code>false</code>
     */
    public boolean isExpectingKey() {
        return (mode == MODE_KEY);
    }

    /**
     * Resets this <code>OXJSONWriter</code>
     */
    public void reset() {
        mode = MODE_INIT;
        stackObjs.clear();
        jsonValue = null;
        jsonObjectType = 0;
        key = null;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return jsonValue == null ? "[empty]" : jsonValue.toString();
    }

    /**
     * Append a value.
     *
     * @param value A value.
     * @return this
     * @throws JSONException If the value is out of sequence.
     */
    private JSONWriter append(final Object value) throws JSONException {
        if (mode != MODE_OBJ && mode != MODE_ARR) {
            throw new JSONException("Value out of sequence. Current mode: " + mode2string());
        }
        final StackObject so = stackObjs.top();
        if (MODE_ARR == so.type) {
            ((JSONArray) so.jsonValue).put(value);
        } else if (MODE_OBJ == so.type) {
            ((JSONObject) so.jsonValue).put(key, value);
            key = null;
        }
        if (mode == MODE_OBJ) {
            mode = MODE_KEY;
        }
        return this;
    }

    private void pushArray(final JSONArray ja) throws JSONException {
        if (!stackObjs.isEmpty()) {
            final StackObject stackObject = stackObjs.top();
            if (MODE_ARR == stackObject.type) {
                ((JSONArray) stackObject.jsonValue).put(ja);
            } else if (MODE_OBJ == stackObject.type) {
                ((JSONObject) stackObject.jsonValue).put(key, ja);
                key = null;
            }
        }
        stackObjs.push(new StackObject(MODE_ARR, ja));
        mode = MODE_ARR;
    }

    private void pop() {
        if (null == stackObjs.topAndPop() || stackObjs.isEmpty()) {
            /*
             * Done
             */
            mode = MODE_DONE;
            return;
        }
        if (MODE_ARR == stackObjs.top().type) {
            /*
             * Set mode to array
             */
            mode = MODE_ARR;
        } else if (MODE_OBJ == stackObjs.top().type) {
            /*
             * Set mode to object
             */
            mode = MODE_KEY;
        }
    }

    private void pushObject(final JSONObject jo) throws JSONException {
        if (!stackObjs.isEmpty()) {
            final StackObject stackObject = stackObjs.top();
            if (MODE_ARR == stackObject.type) {
                ((JSONArray) stackObject.jsonValue).put(jo);
            } else if (MODE_OBJ == stackObject.type) {
                ((JSONObject) stackObject.jsonValue).put(key, jo);
                key = null;
            }
        }
        stackObjs.push(new StackObject(MODE_OBJ, jo));
        mode = MODE_KEY;
    }

    private String mode2string() {
        switch (mode) {
        case MODE_INIT:
            return "INIT";
        case MODE_ARR:
            return "ARRAY";
        case MODE_OBJ:
            return "OBJECT";
        case MODE_KEY:
            return "KEY";
        case MODE_DONE:
            return "DONE";
        default:
            return "UNKNOWN";
        }
    }

    private static final class StackObject {

        public final int type;

        public final JSONValue jsonValue;

        public StackObject(final int type, final JSONValue jsonValue) {
            super();
            this.type = type;
            this.jsonValue = jsonValue;
        }

    }
}
