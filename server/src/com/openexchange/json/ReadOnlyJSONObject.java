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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.openexchange.tools.iterator.ReadOnlyIterator;

/**
 * {@link ReadOnlyJSONObject} - A read-only {@link JSONObject JSON object}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ReadOnlyJSONObject extends JSONObject {

	private static final String TRACE_PUT = "ReadOnlyJSONObject.put() not supported";

	/**
	 * A read-only constant for an empty JSON object
	 */
	public static final JSONObject EMPTY_JSON_OBJECT = new ReadOnlyJSONObject();

	/**
	 * Initializes a new {@link ReadOnlyJSONObject}.
	 * <p>
	 * Constructs an empty JSON object.
	 */
	public ReadOnlyJSONObject() {
		super();
	}

	/**
	 * Initializes a new {@link ReadOnlyJSONObject}.
	 * <p>
	 * Constructs a JSON object from a JSON tokener.
	 * 
	 * @param x
	 *            A JSON tokener object containing the source string.
	 * @throws JSONException
	 *             If there is a syntax error in the source string.
	 */
	public ReadOnlyJSONObject(final JSONTokener x) throws JSONException {
		super(x);
	}

	/**
	 * Initializes a new {@link ReadOnlyJSONObject}.
	 * <p>
	 * Constructs a JSON object from a string. This is the most commonly used
	 * JSON object constructor.
	 * 
	 * @param map
	 *            A map object that can be used to initialize the contents of
	 *            the JSON object.
	 */
	public ReadOnlyJSONObject(final Map<String, ? extends Object> map) {
		super(map);
	}

	/**
	 * Initializes a new {@link ReadOnlyJSONObject}.
	 * <p>
	 * Constructs a JSON object from a string. This is the most commonly used
	 * JSONObject constructor.
	 * 
	 * @param string
	 *            A string beginning with <code>{</code>&nbsp;<small>(left
	 *            brace)</small> and ending with <code>}</code>
	 *            &nbsp;<small>(right brace)</small>.
	 * @throws JSONException
	 *             If there is a syntax error in the source string.
	 */
	public ReadOnlyJSONObject(final String string) throws JSONException {
		super(string);
	}

	/**
	 * Initializes a new {@link ReadOnlyJSONObject}.
	 * <p>
	 * Constructs this JSON object from a subset of another JSON object. An
	 * array of strings is used to identify the keys that should be copied.
	 * Missing keys are ignored.
	 * 
	 * @param jo
	 *            A JSON object
	 * @param sa
	 *            An array of strings.
	 * @throws JSONException
	 *             If a value is a non-finite number.
	 */
	public ReadOnlyJSONObject(final JSONObject jo, final String[] sa) throws JSONException {
		super(jo, sa);
	}

	/**
	 * Initializes a new {@link ReadOnlyJSONObject}.
	 * <p>
	 * Constructs a JSON object from an object, using reflection to find the
	 * public members. The resulting JSON object's keys will be the strings from
	 * the names array, and the values will be the field values associated with
	 * those keys in the object. If a key is not found or not visible, then it
	 * will not be copied into the new JSON object.
	 * 
	 * @param object
	 *            An object that has fields that should be used to make a
	 *            JSONObject.
	 * @param names
	 *            An array of strings, the names of the fields to be used from
	 *            the object.
	 */
	public ReadOnlyJSONObject(final Object object, final String[] names) {
		super(object, names);
	}

	/**
	 * <b>Not supported by this read-only JSON object</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Fills JSONObject with the given source string. This method is dedicated
	 * for <b>re-using</b> a JSONObject in combination with the
	 * <code>reset()</code> method, since it gives the same possibility as the
	 * common-used <code>JSONObject(String string)</code> constructor to create
	 * a JSONObject from a string.
	 * 
	 * @param string
	 *            A string beginning with <code>{</code>&nbsp;<small>(left
	 *            brace)</small> and ending with <code>}</code>
	 *            &nbsp;<small>(right brace)</small>.
	 * @return this.
	 * @exception JSONException
	 *                If there is a syntax error in the source string.
	 * @see #reset()
	 */
	@Override
	public JSONObject parseJSONString(final String string) throws JSONException {
		throw new UnsupportedOperationException("ReadOnlyJSONObject.parseJSONString()");
	}

	/**
	 * <b>Not supported by this read-only JSON object</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Accumulate values under a key. It is similar to the put method except
	 * that if there is already an object stored under the key then a JSONArray
	 * is stored under the key to hold all of the accumulated values. If there
	 * is already a JSONArray, then the new value is appended to it. In
	 * contrast, the put method replaces the previous value.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            An object to be accumulated under the key.
	 * @return this.
	 * @throws JSONException
	 *             If the value is an invalid number or if the key is null.
	 */
	@Override
	public JSONObject accumulate(final String key, final Object value) throws JSONException {
		throw new UnsupportedOperationException("ReadOnlyJSONObject.accumulate() not supported");
	}

	/**
	 * <b>Not supported by this read-only JSON object</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Append values to the array under a key. If the key does not exist in the
	 * JSONObject, then the key is put in the JSONObject with its value being a
	 * JSONArray containing the value parameter. If the key was already
	 * associated with a JSONArray, then the value parameter is appended to it.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            An object to be accumulated under the key.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null or if the current value associated with
	 *             the key is not a JSONArray.
	 */
	@Override
	public JSONObject append(final String key, final Object value) throws JSONException {
		throw new UnsupportedOperationException("ReadOnlyJSONObject.append() not supported");
	}

	/**
	 * <b>Not supported by this read-only JSON object</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Resets this JSONObject for re-use
	 */
	@Override
	public void reset() {
		throw new UnsupportedOperationException("ReadOnlyJSONObject.reset() not supported");
	}

	/**
	 * <b>Not supported by this read-only JSON object</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put a key/boolean pair in the JSONObject.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            A boolean which is the value.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null.
	 */
	@Override
	public JSONObject put(final String key, final boolean value) throws JSONException {
		throw new UnsupportedOperationException(TRACE_PUT);
	}

	/**
	 * <b>Not supported by this read-only JSON object</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put a key/double pair in the JSONObject.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            A double which is the value.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null or if the number is invalid.
	 */
	@Override
	public JSONObject put(final String key, final double value) throws JSONException {
		throw new UnsupportedOperationException(TRACE_PUT);
	}

	/**
	 * <b>Not supported by this read-only JSON object</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put a key/int pair in the JSONObject.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            An int which is the value.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null.
	 */
	@Override
	public JSONObject put(final String key, final int value) throws JSONException {
		throw new UnsupportedOperationException(TRACE_PUT);
	}

	/**
	 * <b>Not supported by this read-only JSON object</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put a key/long pair in the JSONObject.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            A long which is the value.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null.
	 */
	@Override
	public JSONObject put(final String key, final long value) throws JSONException {
		throw new UnsupportedOperationException(TRACE_PUT);
	}

	/**
	 * <b>Not supported by this read-only JSON object</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put a key/value pair in the JSONObject, where the value will be a
	 * JSONObject which is produced from a Map.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            A Map value.
	 * @return this.
	 * @throws JSONException
	 */
	@Override
	public JSONObject put(final String key, final Map<String, ? extends Object> value) throws JSONException {
		throw new UnsupportedOperationException(TRACE_PUT);
	}

	private static final String DECLARING_CLASS = "org.json.JSONObject";

	private static final String FILE_NAME = "JSONObject.java";

	private static final String METHOD_NAME = "<init>";

	/**
	 * <b>Not supported by this read-only JSON object</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put a key/value pair in the JSONObject. If the value is null, then the
	 * key will be removed from the JSONObject if it is present.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            An object which is the value. It should be of one of these
	 *            types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
	 *            String, or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONException
	 *             If the value is non-finite number or if the key is null.
	 */
	@Override
	public JSONObject put(final String key, final Object value) throws JSONException {
		/*
		 * Check for internal invocation from within constructor
		 */
		final StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		for (int i = 3; i < elements.length; i++) {
			final StackTraceElement element = elements[i];
			if (METHOD_NAME.equals(element.getMethodName()) && DECLARING_CLASS.equals(element.getClassName())
					&& FILE_NAME.equals(element.getFileName())) {
				return super.put(key, value);
			}
		}
		throw new UnsupportedOperationException(TRACE_PUT);
	}

	/**
	 * <b>Not supported by this read-only JSON object</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put a key/value pair in the JSONObject, but only if the key and the value
	 * are both non-null.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            An object which is the value. It should be of one of these
	 *            types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
	 *            String, or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONException
	 *             If the value is a non-finite number.
	 */
	@Override
	public JSONObject putOpt(final String key, final Object value) throws JSONException {
		throw new UnsupportedOperationException(TRACE_PUT);
	}

	/**
	 * <b>Not supported by this read-only JSON object</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Remove a name and its value, if present.
	 * 
	 * @param key
	 *            The name to be removed.
	 * @return The value that was associated with the name, or null if there was
	 *         no value.
	 */
	@Override
	public Object remove(final String key) {
		throw new UnsupportedOperationException("ReadOnlyJSONObject.remove() not supported");
	}

	/**
	 * Get an enumeration of the keys of the JSON object.
	 * 
	 * @return An iterator of the keys.
	 */
	@Override
	public Iterator<String> keys() {
		return new ReadOnlyIterator<String>(super.keys());
	}

	/**
	 * Get the key set of the JSON object.
	 * 
	 * @return A key set
	 */
	@Override
	public Set<String> keySet() {
		return Collections.unmodifiableSet(super.keySet());
	}

	/**
	 * Get the entry set of the JSON object
	 * 
	 * @return A entry set
	 */
	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		return Collections.unmodifiableSet(super.entrySet());
	}

}
