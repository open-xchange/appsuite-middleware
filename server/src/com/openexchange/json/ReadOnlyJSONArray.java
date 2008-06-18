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

import java.util.Collection;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

/**
 * {@link ReadOnlyJSONArray} - A read-only {@link JSONArray JSON array}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ReadOnlyJSONArray extends JSONArray {

	/**
	 * A read-only constant for an empty JSON object
	 */
	public static final JSONArray EMPTY_JSON_ARRAY = new ReadOnlyJSONArray();

	/**
	 * Initializes a new {@link ReadOnlyJSONArray}.
	 * <p>
	 * Constructs an empty JSON array.
	 */
	public ReadOnlyJSONArray() {
		super();
	}

	/**
	 * Initializes a new {@link ReadOnlyJSONArray}.
	 * <p>
	 * Constructs a JSON array from a JSON tokener.
	 * 
	 * @param x
	 *            The JSON tokener
	 * @throws JSONException
	 *             If there is a syntax error.
	 */
	public ReadOnlyJSONArray(final JSONTokener x) throws JSONException {
		super(x);
	}

	/**
	 * Initializes a new {@link ReadOnlyJSONArray}.
	 * <p>
	 * Constructs a JSON array from a source JSON text.
	 * 
	 * @param string
	 *            A string that begins with <code>[</code>&nbsp;<small>(left
	 *            bracket)</small> and ends with <code>]</code>
	 *            &nbsp;<small>(right bracket)</small>.
	 * @throws JSONException
	 *             If there is a syntax error.
	 */
	public ReadOnlyJSONArray(final String string) throws JSONException {
		super(string);
	}

	/**
	 * Initializes a new {@link ReadOnlyJSONArray}.
	 * <p>
	 * Constructs a JSON array from a {@link Collection collection}.
	 * 
	 * @param collection
	 *            A Collection.
	 */
	public ReadOnlyJSONArray(final Collection<? extends Object> collection) {
		super(collection);
	}

	/**
	 * <b>Not supported by this read-only JSON array</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Resets this JSONArray for re-use
	 */
	@Override
	public void reset() {
		throw new UnsupportedOperationException("ReadOnlyJSONArray.reset()");
	}

	/**
	 * <b>Not supported by this read-only JSON array</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Append a boolean value. This increases the array's length by one.
	 * 
	 * @param value
	 *            A boolean value.
	 * @return this.
	 */
	@Override
	public JSONArray put(final boolean value) {
		throw new UnsupportedOperationException("ReadOnlyJSONArray.put()");
	}

	/**
	 * <b>Not supported by this read-only JSON array</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put a value in the JSONArray, where the value will be a JSONArray which
	 * is produced from a Collection.
	 * 
	 * @param value
	 *            A Collection value.
	 * @return this.
	 */
	@Override
	public JSONArray put(final Collection<? extends Object> value) {
		throw new UnsupportedOperationException("ReadOnlyJSONArray.put()");
	}

	/**
	 * <b>Not supported by this read-only JSON array</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Append a double value. This increases the array's length by one.
	 * 
	 * @param value
	 *            A double value.
	 * @throws JSONException
	 *             if the value is not finite.
	 * @return this.
	 */
	@Override
	public JSONArray put(final double value) throws JSONException {
		throw new UnsupportedOperationException("ReadOnlyJSONArray.put()");
	}

	/**
	 * <b>Not supported by this read-only JSON array</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Append an int value. This increases the array's length by one.
	 * 
	 * @param value
	 *            An int value.
	 * @return this.
	 */
	@Override
	public JSONArray put(final int value) {
		throw new UnsupportedOperationException("ReadOnlyJSONArray.put()");
	}

	/**
	 * <b>Not supported by this read-only JSON array</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Append an long value. This increases the array's length by one.
	 * 
	 * @param value
	 *            A long value.
	 * @return this.
	 */
	@Override
	public JSONArray put(final long value) {
		throw new UnsupportedOperationException("ReadOnlyJSONArray.put()");
	}

	/**
	 * <b>Not supported by this read-only JSON array</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put a value in the JSONArray, where the value will be a JSONObject which
	 * is produced from a Map.
	 * 
	 * @param value
	 *            A Map value.
	 * @return this.
	 */
	@Override
	public JSONArray put(final Map<String, ? extends Object> value) {
		throw new UnsupportedOperationException("ReadOnlyJSONArray.put()");
	}

	/**
	 * <b>Not supported by this read-only JSON array</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Append an object value. This increases the array's length by one.
	 * 
	 * @param value
	 *            An object value. The value should be a Boolean, Double,
	 *            Integer, JSONArray, JSONObject, Long, or String, or the
	 *            JSONObject.NULL object.
	 * @return this.
	 */
	@Override
	public JSONArray put(final Object value) {
		throw new UnsupportedOperationException("ReadOnlyJSONArray.put()");
	}

	/**
	 * <b>Not supported by this read-only JSON array</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put or replace a boolean value in the JSONArray. If the index is greater
	 * than the length of the JSONArray, then null elements will be added as
	 * necessary to pad it out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            A boolean value.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative.
	 */
	@Override
	public JSONArray put(final int index, final boolean value) throws JSONException {
		throw new UnsupportedOperationException("ReadOnlyJSONArray.put()");
	}

	/**
	 * <b>Not supported by this read-only JSON array</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put a value in the JSONArray, where the value will be a JSONArray which
	 * is produced from a Collection.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            A Collection value.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative or if the value is not finite.
	 */
	@Override
	public JSONArray put(final int index, final Collection<? extends Object> value) throws JSONException {
		throw new UnsupportedOperationException("ReadOnlyJSONArray.put()");
	}

	/**
	 * <b>Not supported by this read-only JSON array</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put or replace a double value. If the index is greater than the length of
	 * the JSONArray, then null elements will be added as necessary to pad it
	 * out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            A double value.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative or if the value is not finite.
	 */
	@Override
	public JSONArray put(final int index, final double value) throws JSONException {
		throw new UnsupportedOperationException("ReadOnlyJSONArray.put()");
	}

	/**
	 * <b>Not supported by this read-only JSON array</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put or replace an int value. If the index is greater than the length of
	 * the JSONArray, then null elements will be added as necessary to pad it
	 * out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            An int value.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative.
	 */
	@Override
	public JSONArray put(final int index, final int value) throws JSONException {
		throw new UnsupportedOperationException("ReadOnlyJSONArray.put()");
	}

	/**
	 * <b>Not supported by this read-only JSON array</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put or replace a long value. If the index is greater than the length of
	 * the JSONArray, then null elements will be added as necessary to pad it
	 * out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            A long value.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative.
	 */
	@Override
	public JSONArray put(final int index, final long value) throws JSONException {
		throw new UnsupportedOperationException("ReadOnlyJSONArray.put()");
	}

	/**
	 * <b>Not supported by this read-only JSON array</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put a value in the JSONArray, where the value will be a JSONObject which
	 * is produced from a Map.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            The Map value.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative or if the the value is an invalid
	 *             number.
	 */
	@Override
	public JSONArray put(final int index, final Map<String, ? extends Object> value) throws JSONException {
		throw new UnsupportedOperationException("ReadOnlyJSONArray.put()");
	}

	/**
	 * <b>Not supported by this read-only JSON array</b>. Invocation will lead
	 * to an {@link UnsupportedOperationException exception}.
	 * <p>
	 * Put or replace an object value in the JSONArray. If the index is greater
	 * than the length of the JSONArray, then null elements will be added as
	 * necessary to pad it out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            The value to put into the array. The value should be a
	 *            Boolean, Double, Integer, JSONArray, JSONObject, Long, or
	 *            String, or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative or if the the value is an invalid
	 *             number.
	 */
	@Override
	public JSONArray put(final int index, final Object value) throws JSONException {
		throw new UnsupportedOperationException("ReadOnlyJSONArray.put()");
	}

}
