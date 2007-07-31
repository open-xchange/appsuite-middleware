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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.io.Writer;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

/**
 * OXJSONWriter - extends <code>{@link JSONWriter}</code> but does not use an
 * underlying instance of <code>{@link Writer}</code> rather than creating
 * JSON objects thus this JSONWriter will never get into an incomplete state.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class OXJSONWriter extends JSONWriter {

	private static final int MODE_INIT = 0;

	private static final int MODE_ARR = 1;

	private static final int MODE_OBJ = 2;

	private static final int MODE_KEY = 3;

	private static final int MODE_DONE = 4;

	private int mode = MODE_INIT;

	private Object jsonObject;

	private int jsonObjectType;

	private final Stack<StackObject> stackObjs = new Stack<StackObject>();
	
	private String key;

	public OXJSONWriter() {
		super(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.json.JSONWriter#array()
	 */
	@Override
	public JSONWriter array() throws JSONException {
		if (mode == MODE_INIT || mode == MODE_OBJ || mode == MODE_ARR) {
			final JSONArray ja = new JSONArray();
			if (mode == MODE_INIT) {
				jsonObject = ja;
				jsonObjectType = MODE_ARR;
			}
			pushArray(ja);
			return this;
		}
		throw new JSONException("Misplaced array. Current mode: " + mode2string());
	}

	/*
	 * (non-Javadoc)
	 * 
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
	 * 
	 * @see org.json.JSONWriter#object()
	 */
	@Override
	public JSONWriter object() throws JSONException {
		if (mode == MODE_INIT || mode == MODE_OBJ || mode == MODE_ARR) {
			final JSONObject jo = new JSONObject();
			if (mode == MODE_INIT) {
				jsonObject = jo;
				jsonObjectType = MODE_OBJ;
				mode = MODE_OBJ;
			}
			pushObject(jo);
			return this;
		}
		throw new JSONException("Misplaced object. Current mode: " + mode2string());
	}

	/*
	 * (non-Javadoc)
	 * 
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
	 * 
	 * @see org.json.JSONWriter#key(java.lang.String)
	 */
	@Override
	public JSONWriter key(final String key) throws JSONException {
		if (key == null) {
			throw new JSONException("Null key.");
		}
		if (this.mode == MODE_KEY) {
			this.key = key;
			mode = MODE_OBJ;
			return this;
		}
		throw new JSONException("Misplaced key. Current mode: " + mode2string());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.json.JSONWriter#value(boolean)
	 */
	@Override
	public JSONWriter value(final boolean b) throws JSONException {
		return this.append(Boolean.valueOf(b));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.json.JSONWriter#value(double)
	 */
	@Override
	public JSONWriter value(final double d) throws JSONException {
		return this.value(Double.valueOf(d));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.json.JSONWriter#value(long)
	 */
	@Override
	public JSONWriter value(final long l) throws JSONException {
		return this.append(Long.valueOf(l));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.json.JSONWriter#value(java.lang.Object)
	 */
	@Override
	public JSONWriter value(final Object o) throws JSONException {
		return this.append(o);
	}

	/**
	 * Checks if this writer is left in an complete state; meaning all opened
	 * objects - either JSONArrays or JSONObjects - were properly closed through
	 * corresponding <code>{@link #endArray()}</code> or
	 * <code>{@link #endObject()}</code> routine.
	 * 
	 * @return <code>true</code> if JSON object is complete; otherwise
	 *         <code>false</code>
	 */
	public boolean isComplete() {
		return (mode == MODE_DONE);
	}

	/**
	 * Checks if initial JSON object is an instance of <code>JSONArray</code>
	 * 
	 * @return <code>true</code> if initial JSON object is an instance of
	 *         <code>JSONArray</code>; otherwise <code>false</code>
	 */
	public boolean isJSONArray() {
		return (jsonObjectType == MODE_ARR);
	}

	/**
	 * Checks if initial JSON object is an instance of <code>JSONObject</code>
	 * 
	 * @return <code>true</code> if initial JSON object is an instance of
	 *         <code>JSONObject</code>; otherwise <code>false</code>
	 */
	public boolean isJSONObject() {
		return (jsonObjectType == MODE_OBJ);
	}

	/**
	 * Getter for initial JSON object created through this writer
	 * 
	 * @return the JSON object; either <code>JSONArray</code> or
	 *         <code>JSONObject</code>
	 */
	public Object getObject() {
		return jsonObject;
	}

	/**
	 * Checks if nothing has been written to this writer, yet
	 * 
	 * @return <code>true</code> if nothing has been written to this writer;
	 *         otherwise <code>false</code>
	 */
	public boolean isEmpty() {
		return (mode == MODE_INIT);
	}

	/**
	 * Append a value.
	 * 
	 * @param value
	 *            A value.
	 * @return this
	 * @throws JSONException
	 *             If the value is out of sequence.
	 */
	private JSONWriter append(final Object value) throws JSONException {
		if (this.mode == MODE_OBJ || this.mode == MODE_ARR) {
			final StackObject so = stackObjs.peek();
			if (MODE_ARR == so.type) {
				((JSONArray) so.jsonObject).put(value);
			} else if (MODE_OBJ == so.type) {
				((JSONObject) so.jsonObject).put(key, value);
				key = null;
			}
			if (this.mode == MODE_OBJ) {
				this.mode = MODE_KEY;
			}
			return this;
		}
		throw new JSONException("Value out of sequence. Current mode: " + mode2string());
	}

	private void pushArray(final JSONArray ja) throws JSONException {
		if (!stackObjs.isEmpty()) {
			final StackObject stackObject = stackObjs.peek();
			if (MODE_ARR == stackObject.type) {
				((JSONArray) stackObject.jsonObject).put(ja);
			} else if (MODE_OBJ == stackObject.type) {
				((JSONObject) stackObject.jsonObject).put(key, ja);
				key = null;
			}
		}
		stackObjs.push(new StackObject(MODE_ARR, ja));
		this.mode = MODE_ARR;
	}

	private void pop() {
		if (null == stackObjs.pop() || stackObjs.isEmpty()) {
			/*
			 * Done
			 */
			mode = MODE_DONE;
			return;
		}
		if (MODE_ARR == stackObjs.peek().type) {
			/*
			 * Set mode to arary
			 */
			mode = MODE_ARR;
		} else if (MODE_OBJ == stackObjs.peek().type) {
			/*
			 * Set mode to object
			 */
			mode = MODE_KEY;
		}
	}

	private void pushObject(final JSONObject jo) throws JSONException {
		if (!stackObjs.isEmpty()) {
			final StackObject stackObject = stackObjs.peek();
			if (MODE_ARR == stackObject.type) {
				((JSONArray) stackObject.jsonObject).put(jo);
			} else if (MODE_OBJ == stackObject.type) {
				((JSONObject) stackObject.jsonObject).put(key, jo);
				key = null;
			}
		}
		stackObjs.push(new StackObject(MODE_OBJ, jo));
		this.mode = MODE_KEY;
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
	
	private static class StackObject {

		private final int type;

		private final Object jsonObject;

		public StackObject(final int type, final Object jsonObject) {
			super();
			this.type = type;
			this.jsonObject = jsonObject;
		}

		public Object getJsonObject() {
			return jsonObject;
		}

		public int getType() {
			return type;
		}

	}
}
