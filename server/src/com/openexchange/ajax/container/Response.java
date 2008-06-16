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

package com.openexchange.ajax.container;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * Response data object.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Response {

	/**
	 * Name of the JSON attribute containing the response data.
	 */
	public static final String DATA = "data";

	/**
	 * Name of the JSON attribute containing the error message.
	 */
	public static final String ERROR = "error";

	/**
	 * Name of the JSON attribute containing the array of the error message
	 * attributes.
	 */
	public static final String ERROR_PARAMS = "error_params";

	/**
	 * Name of the JSON attribute containing the error category.
	 */
	public static final String ERROR_CATEGORY = "category";

	/**
	 * Name of the JSON attribute containing the error code.
	 */
	public static final String ERROR_CODE = "code";

	/**
	 * Name of the JSON attribute containing the unique error identifier.
	 */
	public static final String ERROR_ID = "error_id";

	/**
	 * Name of the JSON attribute containing the timestamp of the most actual
	 * returned object.
	 */
	public static final String TIMESTAMP = "timestamp";

	/**
	 * Name of the JSON attribute containing the array of truncated attribute
	 * identifier.
	 */
	public static final String TRUNCATED = "truncated";

	/**
	 * The original JSON response.
	 */
	private transient JSONObject json;

	/**
	 * The literal data.
	 */
	private Object data;

	/**
	 * Timestamp for the last modification.
	 */
	private Date timestamp;

	/**
	 * Exception of request.
	 */
	private AbstractOXException exception;

	/**
	 * Whether to communicate exception as a warning to front-end or as an error
	 */
	private boolean isWarning;

	/**
	 * This constructor parses a server response into an object.
	 * 
	 * @param response
	 *            the response JSON object.
	 */
	public Response(final JSONObject response) {
		super();
		this.json = response;
	}

	/**
	 * Constructor for generating responses.
	 */
	public Response() {
		this(null);
	}

	/**
	 * @return the json object
	 * @throws JSONException
	 *             if putting the attributes into the json object fails.
	 */
	public JSONObject getJSON() throws JSONException {
		if (null != json) {
			return json;
		}
		json = new JSONObject();
		if (null != data) {
			json.put(DATA, data);
		}
		if (null != timestamp) {
			json.put(TIMESTAMP, timestamp.getTime());
		}
		if (null != exception) {
			json.put(ERROR, exception.getOrigMessage());
			if (exception.getMessageArgs() != null) {
				final JSONArray array = new JSONArray();
				for (final Object tmp : exception.getMessageArgs()) {
					array.put(tmp);
				}
				json.put(ERROR_PARAMS, array);
			}
			json.put(ERROR_CATEGORY, isWarning ? Category.WARNING.getCode() : exception.getCategory().getCode());
			json.put(ERROR_CODE, exception.getErrorCode());
			json.put(ERROR_ID, exception.getExceptionID());
			if (exception.getTruncatedIds().length > 0) {
				final JSONArray array = new JSONArray();
				json.put(TRUNCATED, array);
				for (final int i : exception.getTruncatedIds()) {
					array.put(i);
				}
			}
		}
		return json;
	}

	/**
	 * Resets the response object for re-use
	 */
	public void reset() {
		json = null;
		data = null;
		timestamp = null;
		exception = null;
		isWarning = false;
	}

	/**
	 * @return the data JSON object of the response.
	 * @throws JSONException
	 *             if an error occurs reading the data attribute of the response
	 *             JSON object.
	 */
	public JSONObject getResponseData() throws JSONException {
		final Object tmp = getJSON().get(DATA);
		if (tmp instanceof JSONObject) {
			return (JSONObject) tmp;
		}
		throw new JSONException("Use method getData()");
	}

	/**
	 * @return Returns the data.
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Returns the error message.
	 * <p>
	 * For testing only
	 * 
	 * @return Returns the errorMessage.
	 */
	public String getErrorMessage() {
		final String retval;
		if (null == exception) {
			retval = null;
		} else {
			retval = exception.getMessage();
		}
		return retval;
	}

	/**
	 * Returns the error parameters.
	 * <p>
	 * For testing only
	 * 
	 * @return Returns the errorParams.
	 */
	public JSONArray getErrorParams() {
		final JSONArray array = new JSONArray();
		if (exception != null && null != exception.getMessageArgs()) {
			for (final Object arg : exception.getMessageArgs()) {
				array.put(arg);
			}
		}
		return array;
	}

	/**
	 * @return Returns the timestamp.
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @param data
	 *            The data to set.
	 */
	public void setData(final Object data) {
		this.data = data;
	}

	/**
	 * @param timestamp
	 *            The timestamp to set.
	 */
	public void setTimestamp(final Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Checks if if the response contains an error message or a warning.
	 * <p>
	 * For testing only.
	 * 
	 * @return <code>true</code> if the response contains an error message or a
	 *         warning.
	 */
	public boolean hasError() {
		return exception != null;
	}

	/**
	 * @return <code>true</code> if the response contains a warning.
	 */
	public boolean hasWarning() {
		return exception != null && isWarning;
	}

	/**
	 * Deserializes a response into the Response object.
	 * 
	 * @param body
	 *            JSON response string.
	 * @return the parsed object.
	 * @throws JSONException
	 *             if parsing fails.
	 */
	public static Response parse(final String body) throws JSONException {
		final JSONObject response = new JSONObject(body);
		final Response retval = new Response(response);
		if (response.has(DATA)) {
			retval.setData(response.get(DATA));
		}
		if (response.has(TIMESTAMP)) {
			retval.setTimestamp(new Date(response.getLong(TIMESTAMP)));
		}
		final String message = response.optString(ERROR, null);
		final String code = response.optString(ERROR_CODE, null);
		if (message != null || code != null) {
			final Component component = parseComponent(code);
			final int number = parseErrorNumber(code);
			final int categoryCode = response.optInt(ERROR_CATEGORY, -1);
			final Category category;
			if (-1 == categoryCode) {
				category = Category.CODE_ERROR;
			} else {
				category = Category.byCode(categoryCode);
			}
			retval.exception = new AbstractOXException(component, category, number, message, null);
			retval.isWarning = (Category.WARNING.equals(category));
			if (response.has(ERROR_ID)) {
				retval.exception.overrideExceptionID(response.getString(ERROR_ID));
			}
			parseErrorMessageArgs(response.optJSONArray(ERROR_PARAMS), retval.exception);
			parseTruncatedIds(response.optJSONArray(TRUNCATED), retval.exception);
		}
		return retval;
	}

	/**
	 * Parses the component part of the error code.
	 * 
	 * @param code
	 *            error code to parse.
	 * @return the parsed component or {@link EnumComponent#NONE}.
	 */
	private static Component parseComponent(final String code) {
		if (code == null || code.length() == 0) {
			return EnumComponent.NONE;
		}
		final int pos = code.indexOf('-');
		if (pos != -1) {
			final String abbr = code.substring(0, pos);
			final EnumComponent component = EnumComponent.byAbbreviation(abbr);
			if (component != null) {
				return component;
			}
			return new StringComponent(abbr);
		}
		return EnumComponent.NONE;
	}

	/**
	 * Parses the error number out of the error code.
	 * 
	 * @param code
	 *            error code to parse.
	 * @return the parsed error number or 0.
	 */
	private static int parseErrorNumber(final String code) {
		if (code == null || code.length() == 0) {
			return 0;
		}
		final int pos = code.indexOf('-');
		if (pos != -1) {
			try {
				return Integer.parseInt(code.substring(pos + 1));
			} catch (final NumberFormatException e) {
				return 0;
			}
		}
		return 0;
	}

	/**
	 * Parses the error message arguments.
	 * 
	 * @param jArgs
	 *            the json array with the error message arguments or
	 *            <code>null</code>.
	 * @param exception
	 *            the error message arguments will be stored in this exception.
	 * @throws JSONException
	 *             if reading the error message arguments from the JSON array
	 *             fails.
	 */
	private static void parseErrorMessageArgs(final JSONArray jArgs, final AbstractOXException exception)
			throws JSONException {
		if (null != jArgs) {
			final Object[] args = new Object[jArgs.length()];
			for (int i = 0; i < jArgs.length(); i++) {
				args[i] = jArgs.get(i);
			}
			exception.setMessageArgs(args);
		}
	}

	/**
	 * Parses the identifier of the truncated attribute identifier.
	 * 
	 * @param jTrunc
	 *            the json array with the truncated attribute identifier or
	 *            <code>null</code>.
	 * @param exception
	 *            the truncated attribute identifier will be stored in this
	 *            exception.
	 * @throws JSONException
	 *             if reading the truncated attribute identifier from the JSON
	 *             array fails.
	 */
	private static void parseTruncatedIds(final JSONArray jTrunc, final AbstractOXException exception)
			throws JSONException {
		if (null != jTrunc) {
			for (int i = 0; i < jTrunc.length(); i++) {
				exception.addTruncatedId(jTrunc.getInt(i));
			}
		}
	}

	/**
	 * Serializes a Response object to the writer.
	 * 
	 * @param response
	 *            Response object to serialize.
	 * @param writer
	 *            the serialized object will be written to this writer.
	 * @throws JSONException
	 *             if writing fails.
	 */
	public static void write(final Response response, final Writer writer) throws JSONException {
		response.getJSON().write(writer);
	}

	/**
	 * Serializes a Response object to given instance of <code>
	 * {@link JSONWriter}</code>.
	 * 
	 * @param response
	 *            - the <code>{@link Response}</code> object to serialize.
	 * @param writer
	 *            - the <code>{@link JSONWriter}</code> to write to
	 * @throws JSONException
	 *             - if writing fails
	 */
	public static void write(final Response response, final JSONWriter writer) throws JSONException {
		writer.object();
		final Set<Map.Entry<String, Object>> entrySet = response.getJSON().entrySet();
		final int len = entrySet.size();
		final Iterator<Map.Entry<String, Object>> iter = entrySet.iterator();
		for (int i = 0; i < len; i++) {
			final Map.Entry<String, Object> e = iter.next();
			writer.key(e.getKey()).value(e.getValue());
		}
		writer.endObject();
	}

	/**
	 * Writes given instance of <code>AbstractOXException</code> into given
	 * instance of <code>JSONWriter</code> assuming that writer's mode is
	 * already set to writing a JSON object
	 * 
	 * @param exception
	 *            - the exception to write
	 * @param writer
	 *            - the writer to write to
	 * @throws JSONException
	 *             - if writing fails
	 */
	public static void writeException(final AbstractOXException exception, final JSONWriter writer)
			throws JSONException {
		writer.key(ERROR).value(exception.getOrigMessage());
		if (exception.getMessageArgs() != null) {
			final JSONArray array = new JSONArray();
			for (final Object tmp : exception.getMessageArgs()) {
				array.put(tmp);
			}
			writer.key(ERROR_PARAMS).value(array);
		}
		writer.key(ERROR_CATEGORY).value(exception.getCategory().getCode());
		writer.key(ERROR_CODE).value(exception.getErrorCode());
		writer.key(ERROR_ID).value(exception.getExceptionID());
		if (exception.getTruncatedIds().length > 0) {
			final JSONArray array = new JSONArray();
			for (final int i : exception.getTruncatedIds()) {
				array.put(i);
			}
			writer.key(TRUNCATED).value(array);
		}
	}

	/**
	 * Writes given instance of <code>AbstractOXException</code> as a warning
	 * into given instance of <code>JSONWriter</code> assuming that writer's
	 * mode is already set to writing a JSON object
	 * 
	 * @param warning
	 *            - the warning to write
	 * @param writer
	 *            - the writer to write to
	 * @throws JSONException
	 *             - if writing fails
	 */
	public static void writeWarning(final AbstractOXException warning, final JSONWriter writer) throws JSONException {
		writer.key(ERROR).value(warning.getOrigMessage());
		if (warning.getMessageArgs() != null) {
			final JSONArray array = new JSONArray();
			for (final Object tmp : warning.getMessageArgs()) {
				array.put(tmp);
			}
			writer.key(ERROR_PARAMS).value(array);
		}
		writer.key(ERROR_CATEGORY).value(Category.WARNING.getCode());
		writer.key(ERROR_CODE).value(warning.getErrorCode());
		writer.key(ERROR_ID).value(warning.getExceptionID());
		if (warning.getTruncatedIds().length > 0) {
			final JSONArray array = new JSONArray();
			for (final int i : warning.getTruncatedIds()) {
				array.put(i);
			}
			writer.key(TRUNCATED).value(array);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final StringWriter writer = new StringWriter();
		try {
			write(this, writer);
		} catch (final JSONException e) {
			e.printStackTrace(new PrintWriter(writer));
		}
		return writer.toString();
	}

	/**
	 * Sets this response object's exception and implicitly overwrites any
	 * existing warning/error.
	 * <p>
	 * <b>Note</b>: If exception's category is set to {@link Category#WARNING}
	 * it is treated as a warning only.
	 * 
	 * @param exception
	 *            The exception to set
	 * @see #setWarning(AbstractOXException)
	 */
	public void setException(final AbstractOXException exception) {
		this.exception = exception;
		isWarning = Category.WARNING.equals(exception.getCategory());
	}

	/**
	 * Sets this response object's warning and implicitly overwrites any
	 * existing warning/error.
	 * <p>
	 * <b>Note</b>: Resulting response object's category is implicitly set to
	 * {@link Category#WARNING}.
	 * 
	 * @param warning
	 *            The warning to set
	 */
	public void setWarning(final AbstractOXException warning) {
		this.exception = warning;
		isWarning = true;
	}

	/**
	 * Gets this response object's exception/warning.
	 * 
	 * @return the exception or <code>null</code>
	 */
	public AbstractOXException getException() {
		return exception;
	}

	/**
	 * Gets this response object's warning
	 * 
	 * @return the warning or <code>null</code>
	 */
	public AbstractOXException getWarning() {
		return isWarning ? exception : null;
	}

	private static final class StringComponent implements Component {
		private final String abbr;

		public StringComponent(final String abbr) {
			super();
			this.abbr = abbr;
		}

		public String getAbbreviation() {
			return abbr;
		}
	}
}
