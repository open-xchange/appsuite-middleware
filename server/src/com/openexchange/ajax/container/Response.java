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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.ajax.parser.ResponseParser;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.tools.UnsynchronizedStringWriter;

/**
 * Response data object.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Response {

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
    private OXException exception;

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
     * @deprecated use {@link ResponseWriter#getJSON(Response)}.
     */
    @Deprecated
    public JSONObject getJSON() throws JSONException {
        if (null == json) {
            json = ResponseWriter.getJSON(this);
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
        if (null == exception) {
            return null;
        }
        return exception.getMessage();
    }

    /**
     * Gets the formatted message.
     * <p>
     * For testing only
     * 
     * @return The formatted message or <code>null</code> if no error present
     */
    public String getFormattedErrorMessage() {
        if (null == exception) {
            return null;
        }
        return exception.getDisplayMessage(Locale.US);
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
     * @deprecated use {@link ResponseParser#parse(String)}.
     */
    @Deprecated
    public static Response parse(final String body) throws JSONException {
        return ResponseParser.parse(body);
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
     * @throws IOException If an I/O error occurs
     * @deprecated use {@link ResponseWriter#write(Response, Writer)}.
     */
    @Deprecated
    public static void write(final Response response, final Writer writer) throws JSONException, IOException {
        ResponseWriter.write(response, writer);
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
     * @deprecated use {@link ResponseWriter#write(Response, JSONWriter)}.
     */
    @Deprecated
    public static void write(final Response response, final JSONWriter writer) throws JSONException {
        ResponseWriter.write(response, writer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final UnsynchronizedStringWriter writer = new UnsynchronizedStringWriter();
        final JSONObject json = new JSONObject();
        try {
            ResponseWriter.write(this, json);
            json.write(writer);
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
    public void setException(final OXException exception) {
        this.exception = exception;
        isWarning = exception.getCategories().get(0).getType().equals(Category.EnumType.WARNING);
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
    public void setWarning(final OXException warning) {
        this.exception = warning;
        isWarning = true;
    }

    /**
     * Gets this response object's exception/warning.
     *
     * @return the exception or <code>null</code>
     */
    public OXException getException() {
        return exception;
    }

    /**
     * Gets this response object's warning
     *
     * @return the warning or <code>null</code>
     */
    public OXException getWarning() {
        return isWarning ? exception : null;
    }
}
