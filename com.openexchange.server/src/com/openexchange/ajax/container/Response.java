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

package com.openexchange.ajax.container;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.ajax.parser.ResponseParser;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * Response data object.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Response {

    private static final Locale DEFAULT_LOCALE = Locale.US;

    /**
     * The optional properties.
     */
    private final Map<String, Object> properties;

    /**
     * The locale needed for i18n of display message.
     */
    private Locale locale;

    /**
     * The original JSON response.
     */
    private transient JSONObject json;

    /**
     * The literal data.
     */
    private Object data;

    /**
     * Time stamp for the last modification.
     */
    private Date timestamp;

    /**
     * Exception of request.
     */
    private OXException exception;

    /**
     * The list of warnings.
     */
    private final List<OXException> warnings;

    /**
     * The flag whether to include stack trace or not.
     */
    private boolean includeStackTraceOnError;

    /**
     * Signals whether the data provided by this response is not yet finished or final, but rather reflects an intermediate state and the
     * client is supposed to request again to get full results.
     */
    private UUID continuationUuid;

    /**
     * This constructor parses a server response into an object.
     *
     * @param response the response JSON object.
     */
    public Response(final JSONObject response) {
        this(DEFAULT_LOCALE);
        this.json = response;
        includeStackTraceOnError = false;
        continuationUuid = null;
    }

    /**
     * Constructor for generating responses.
     *
     * @param session The server session providing user data; if <code>null</code> default locale {@link Locale#US} is used
     * @throws OXException If user's locale cannot be detected
     */
    public Response(final Session session) throws OXException {
        this(localeFrom(session));
    }

    /**
     * Constructor for generating responses.
     *
     * @param session The server session providing user data; if <code>null</code> default locale {@link Locale#US} is used
     */
    public Response(final ServerSession session) {
        this(localeFrom(session));
    }

    /**
     * Constructor for generating responses.
     */
    public Response() {
        this(DEFAULT_LOCALE);
    }

    /**
     * Constructor for generating responses.
     *
     * @param locale The locale for possibly internationalizing the error message
     */
    public Response(final Locale locale) {
        super();
        warnings = new LinkedList<OXException>();
        properties = new HashMap<String, Object>(8);
        this.json = null;
        this.locale = locale;
        includeStackTraceOnError = false;
        continuationUuid = null;
    }

    /**
     * Puts specified properties.
     *
     * @param properties The properties to add
     * @return This response with properties added
     */
    public Response setProperties(final Map<String, Object> properties) {
        if (null != properties) {
            this.properties.putAll(properties);
        }
        return this;
    }

    /**
     * Gets the properties
     *
     * @return The properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Sets the specified locale needed for internationalization of display message.
     *
     * @param session The server session; if <code>null</code> default locale {@link Locale#US} is used
     * @return This {@link Response} with locale applied.
     */
    public Response setLocale(final ServerSession session) {
        this.locale = localeFrom(session);
        return this;
    }

    /**
     * Sets the specified locale needed for internationalization of display message.
     *
     * @param session The session; if <code>null</code> default locale {@link Locale#US} is used
     * @return This {@link Response} with locale applied.
     * @throws OXException If locale cannot be detected
     */
    public Response setLocale(final Session session) throws OXException {
        this.locale = localeFrom(session);
        return this;
    }

    /**
     * Sets the specified locale needed for internationalization of display message.
     *
     * @param locale The locale
     * @return This {@link Response} with locale applied.
     */
    public Response setLocale(final Locale locale) {
        this.locale = null == locale ? DEFAULT_LOCALE : locale;
        return this;
    }

    /**
     * Gets the locale used for internationalization of display message.
     *
     * @return locale The locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Gets the JSON object resulting from this response.
     *
     * @return The JSON object
     * @throws JSONException If composing the JSON object fails.
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
     * Resets the response object for re-use.
     * <p>
     * <b>Note</b>: Locale is maintained.
     */
    public void reset() {
        json = null;
        data = null;
        timestamp = null;
        exception = null;
        warnings.clear();
    }

    /**
     * Gets the data object.
     *
     * @return The data.
     */
    public Object getData() {
        return data;
    }

    /**
     * Gets the error message.
     * <p>
     * For testing only
     *
     * @return The errorMessage.
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
        return exception.getDisplayMessage(DEFAULT_LOCALE);
    }

    /**
     * @return Returns the time stamp.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @param data The data to set.
     */
    public void setData(final Object data) {
        this.data = data;
    }

    /**
     * @param timestamp The time stamp to set.
     */
    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Checks if if the response contains an error message or a warning.
     * <p>
     * For testing only.
     *
     * @return <code>true</code> if the response contains an error message or a warning.
     */
    public boolean hasError() {
        return exception != null;
    }

    /**
     * Checks if this response contains warnings.
     *
     * @return <code>true</code> if the response contains warnings; otherwise <code>false</code>
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Deserializes a response into the Response object.
     *
     * @param body JSON response string.
     * @return the parsed object.
     * @throws JSONException if parsing fails.
     * @deprecated use {@link ResponseParser#parse(String)}.
     */
    @Deprecated
    public static Response parse(final String body) throws JSONException {
        return ResponseParser.parse(body);
    }

    /**
     * Serializes a Response object to the writer.
     *
     * @param response Response object to serialize.
     * @param writer the serialized object will be written to this writer.
     * @throws JSONException if writing fails.
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
     * @param response - the <code>{@link Response}</code> object to serialize.
     * @param writer - the <code>{@link JSONWriter}</code> to write to
     * @throws JSONException - if writing fails
     * @deprecated use {@link ResponseWriter#write(Response, JSONWriter)}.
     */
    @Deprecated
    public static void write(final Response response, final JSONWriter writer) throws JSONException {
        ResponseWriter.write(response, writer, DEFAULT_LOCALE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final AllocatingStringWriter writer = new AllocatingStringWriter();
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
     * Sets this response object's exception and implicitly overwrites any existing exception.
     * <p>
     * <b>Note</b>: If exception's category is set to {@link Category#CATEGORY_WARNING} it is treated as a warning only.
     *
     * @param exception The exception to set
     * @return This response with exception applied
     */
    public Response setException(final OXException exception) {
        if (Category.CATEGORY_WARNING.equals(exception.getCategory())) {
            addWarning(exception);
        } else {
            this.exception = exception;
        }
        return this;
    }

    /**
     * Sets this response object's warning.
     * <p>
     * <b>Note</b>: {@link OXException}'s category is implicitly set to {@link Category#CATEGORY_WARNING}.
     *
     * @param warning The warning to add
     * @return This response with warning added
     */
    public Response addWarning(final OXException warning) {
        if (!Category.CATEGORY_WARNING.equals(warning.getCategory())) {
            warning.setCategory(Category.CATEGORY_WARNING);
        }
        warnings.add(warning);
        return this;
    }

    /**
     * Sets this response object's warnings and implicitly overwrites any existing warning/error.
     * <p>
     * <b>Note</b>: {@link OXException}'s category is implicitly set to {@link Category#CATEGORY_WARNING}.
     *
     * @param warnings The warnings to add
     * @return This response with warnings added
     */
    public Response addWarnings(final Collection<OXException> warnings) {
        for (final OXException warning : warnings) {
            if (!Category.CATEGORY_WARNING.equals(exception.getCategory())) {
                warning.setCategory(Category.CATEGORY_WARNING);
            }
        }
        warnings.addAll(warnings);
        return this;
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
     * Gets this response object's warnings
     *
     * @return The warnings as an unmodifiable list
     */
    public List<OXException> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Sets whether to include stack trace.
     *
     * @param includeStackTraceOnError <code>true</code> to include stack trace; otherwise <code>false</code>
     */
    public void setIncludeStackTraceOnError(final boolean includeStackTraceOnError) {
        this.includeStackTraceOnError = includeStackTraceOnError;
    }

    /**
     * Checks whether to include stack trace.
     *
     * @return <code>true</code> to include stack trace; otherwise <code>false</code>
     */
    public boolean includeStackTraceOnError() {
        return includeStackTraceOnError;
    }

    /**
     * Sets whether the data provided by this response is not yet finished or final, but rather reflects an intermediate state and the
     * client is supposed to request again to get full results.
     *
     * @param continuation The UUID to set
     * @return A reference to this response
     */
    public Response setContinuationUUID(final UUID continuationUuid) {
        this.continuationUuid = continuationUuid;
        return this;
    }

    /**
     * Checks whether the data provided by this response is not yet finished or final, but rather reflects an intermediate state and the
     * client is supposed to request again to get full results.
     *
     * @return The UUID or <code>null</code>
     */
    public UUID getContinuationUUID() {
        return continuationUuid;
    }

    private static Locale localeFrom(final Session session) throws OXException {
        if (null == session) {
            return DEFAULT_LOCALE;
        }
        return localeFrom(session instanceof ServerSession ? (ServerSession) session : ServerSessionAdapter.valueOf(session));
    }

    private static Locale localeFrom(final ServerSession serverSession) {
        if (null == serverSession) {
            return DEFAULT_LOCALE;
        }
        final User user = serverSession.getUser();
        return serverSession.isAnonymous() ? DEFAULT_LOCALE : (null == user ? DEFAULT_LOCALE : user.getLocale());
    }

}
