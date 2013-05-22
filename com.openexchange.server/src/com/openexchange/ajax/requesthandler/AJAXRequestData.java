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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.RequestConstants;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.dispatcher.Parameterizable;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.mail.json.actions.AbstractMailAction;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.strings.StringParser;

/**
 * {@link AJAXRequestData} contains the parameters and the payload of the request.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AJAXRequestData {

    /**
     * The upload {@link InputStream stream} provider.
     */
    public static interface InputStreamProvider {

        /**
         * Gets this provider's input stream.
         * 
         * @return The input stream
         * @throws IOException If an I/O error occurs
         */
        InputStream getInputStream() throws IOException;
    }

    /** Map for parameters */
    private final Map<String, String> params;

    /** The {@code Parameterizable} reference */
    private Parameterizable parameterizable;

    /** Map for headers */
    private final Map<String, String> headers;

    /** Map for properties */
    private final Map<String, Object> properties;

    /** Associated server session */
    private ServerSession session;

    /** Whether a secure connection has been established */
    private boolean secure;

    /** The request body data */
    private Object data;

    /** The module string */
    private String module;

    /** The action string */
    private String action;

    /** The upload stream provider */
    private InputStreamProvider uploadStreamProvider;

    /** List of uploaded files */
    private final List<UploadFile> files;

    /** The host name */
    private String hostname;

    /** The remote address */
    private String remoteAddress;

    /** The Servlet's request URI */
    private String servletRequestUri;

    /** The AJP route: &lt;http-session-id&gt; + <code>"." </code>+ &lt;route&gt; */
    private String route;

    /** The upload event */
    private volatile UploadEvent uploadEvent;

    /** The format */
    private String format;

    /** The state reference */
    private AJAXState state;

    /** The eTag */
    private String eTag;

    /** The <code>User-Agent</code> value */
    private String userAgent;

    /** The expires millis */
    private long expires;

    /** The path information */
    private String pathInfo;

    /** The HTTP Servlet request */
    private HttpServletRequest httpServletRequest;

    /** The decorator identifiers */
    private final List<String> decoratorIds;

    /** The multipart flag. */
    private boolean multipart;

    /** The path prefix; &lt;prefix&gt; + <code>'/'</code> + &lt;module&gt; */
    private String prefix;

    /** The optional <code>HttpServletResponse</code> instance */
    private HttpServletResponse httpServletResponse;

    /**
     * Initializes a new {@link AJAXRequestData}.
     * 
     * @param json The JSON data
     * @throws OXException If an AJAX error occurs
     */
    public AJAXRequestData(final JSONObject json) throws OXException {
        this();
        data = DataParser.checkJSONObject(json, RequestConstants.DATA);
    }

    /**
     * Initializes a new {@link AJAXRequestData}.
     * 
     * @param data The payload to use data
     */
    public AJAXRequestData(final Object data) {
        this();
        this.data = data;
    }

    /**
     * Initializes a new {@link AJAXRequestData}.
     */
    public AJAXRequestData() {
        super();
        params = new LinkedHashMap<String, String>();
        headers = new LinkedHashMap<String, String>();
        properties = new HashMap<String, Object>(4);
        files = new LinkedList<UploadFile>();
        decoratorIds = new LinkedList<String>();
        expires = -1;
    }

    /**
     * Gets a best-guess copy of this request data.
     * <p>
     * <ul>
     * <li> {@link AJAXState} is set to <code>null</code></li>
     * <li> {@link UploadEvent} is set to <code>null</code></li>
     * <li> {@link InputStreamProvider} is set to <code>null</code></li>
     * </ul>
     * 
     * @return The copy
     */
    public AJAXRequestData copyOf() {
        final AJAXRequestData copy = new AJAXRequestData();
        copy.params.putAll(params);
        copy.headers.putAll(headers);
        copy.properties.putAll(properties);
        copy.decoratorIds.addAll(decoratorIds);
        copy.files.addAll(files);
        copy.parameterizable = parameterizable;
        copy.session = session;
        copy.secure = secure;
        copy.action = action;
        copy.data = data;
        copy.eTag = eTag;
        copy.expires = expires;
        copy.format = format;
        copy.hostname = hostname;
        copy.module = module;
        copy.pathInfo = pathInfo;
        copy.remoteAddress = remoteAddress;
        copy.route = route;
        copy.servletRequestUri = servletRequestUri;
        copy.userAgent = userAgent;
        /*
         * Not sure about following members, therefore leave to null
         */
        copy.state = null;
        copy.uploadEvent = null;
        copy.uploadStreamProvider = null;
        return copy;
    }
    
    /**
     * Sets the <code>User-Agent</code> value
     * 
     * @param userAgent The <code>User-Agent</code> value
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    /**
     * Gets the <code>User-Agent</code> value
     * 
     * @return The <code>User-Agent</code> value
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the <code>HttpServletResponse</code> instance to enable support for:
     * <ul>
     * <li> {@link #optOutputStream()} </li>
     * <li> {@link #optWriter()} </li>
     * <li> {@link #setCharacterEncoding(String)} </li>
     * <ul>
     * <p>
     * 
     * @param resp The <code>HttpServletResponse</code> instance
     * @return This request data with <code>HttpServletResponse</code> instance applied
     */
    public AJAXRequestData setHttpServletResponse(final HttpServletResponse resp) {
        this.httpServletResponse = resp;
        return this;
    }

    /**
     * Examines specified {@code HttpServletRequest} instance.
     * 
     * @param servletRequest The HTTP Servlet request to examine
     */
    public void examineServletRequest(final HttpServletRequest servletRequest) {
        if (null == servletRequest) {
            return;
        }
        this.parameterizable = servletRequest instanceof Parameterizable ? (Parameterizable) servletRequest : null;
    }

    /**
     * Adds specified decorator identifier.
     * 
     * @param decoratorId The decorator identifier
     * @return This AJAX request data instance with decorator identifier added
     */
    public AJAXRequestData addDecoratorId(final String decoratorId) {
        decoratorIds.add(decoratorId);
        return this;
    }

    /**
     * Adds specified decorator identifiers.
     * 
     * @param decoratorId The decorator identifiers
     * @return This AJAX request data instance with decorator identifiers added
     */
    public AJAXRequestData addDecoratorIds(final Collection<String> decoratorIds) {
        final List<String> thisDecoratorIds = this.decoratorIds;
        for (final String decoratorId : decoratorIds) {
            thisDecoratorIds.add(decoratorId);
        }
        return this;
    }

    /**
     * Returns a {@link OutputStream} suitable for writing binary data in the response. The servlet container does not encode the
     * binary data.
     * <p>
     * Calling flush() on the OutputStream commits the response. Either this method or {@link #getWriter} may be called to write the
     * body, not both.
     * 
     * @return A {@link OutputStream} for writing binary data or <code>null</code>
     * @throws IllegalStateException If the <code>getWriter</code> method has already been called
     * @throws IOException If an input or output exception occurred
     * @see #optWriter()
     */
    public OutputStream optOutputStream() throws IOException {
        if (null != httpServletResponse) {
            return httpServletResponse.getOutputStream();
        }
        return null;
    }

    /**
     * Returns a <code>PrintWriter</code> object that can send character text to the client. The <code>PrintWriter</code> uses the character
     * encoding returned by {@link #getCharacterEncoding}. If the response's character encoding has not been specified as described in
     * <code>getCharacterEncoding</code> (i.e., the method just returns the default value <code>ISO-8859-1</code>), <code>getWriter</code>
     * updates it to <code>ISO-8859-1</code>.
     * <p>
     * Calling flush() on the <code>PrintWriter</code> commits the response.
     * <p>
     * Either this method or {@link #getOutputStream} may be called to write the body, not both.
     * 
     * @return A <code>PrintWriter</code> object that can return character data to the client or <code>null</code>
     * @throws UnsupportedEncodingException If the character encoding is invalid
     * @throws IllegalStateException If the <code>getOutputStream</code> method has already been called
     * @throws IOException If an input or output exception occurred
     * @see #getOutputStream
     * @see #setCharacterEncoding
     */
    public PrintWriter optWriter() throws IOException {
        if (null != httpServletResponse) {
            return httpServletResponse.getWriter();
        }
        return null;
    }

    /**
     * Sets the character encoding (MIME charset) of the response being sent to the client, for example, to UTF-8.
     * <p>
     * This method can be called repeatedly to change the character encoding. This method has no effect if it is called after
     * <code>optWriter</code> has been called or after the response has been committed.
     * 
     * @param charset A String specifying only the character set defined by IANA Character Sets
     *            (http://www.iana.org/assignments/character-sets)
     * @return <code>true</code> if set; otherwise <code>false</code>
     */
    public boolean setResponseCharacterEncoding(final String charset) {
        final HttpServletResponse resp = this.httpServletResponse;
        if (null != resp) {
            resp.setCharacterEncoding(charset);
            return true;
        }
        return false;
    }

    /**
     * Sets specified header.
     * <p>
     * Requires a valid {@link HttpServletResponse} instance to be available; otherwise this is a no-op.
     * 
     * @param name The name
     * @param value The value
     * @return <code>true</code> if set; otherwise <code>false</code>
     */
    public boolean setResponseHeader(final String name, final String value) {
        final HttpServletResponse resp = this.httpServletResponse;
        if (null != resp) {
            resp.setHeader(name, value);
            return true;
        }
        return false;
    }

    /**
     * Remove <tt>Pragma</tt> response header value if we are going to write directly into servlet's output stream cause then some browsers
     * do not allow this header.
     * 
     * @return <code>true</code> if applied; otherwise <code>false</code>
     */
    public boolean removeCachingHeader() {
        final HttpServletResponse resp = this.httpServletResponse;
        if (null != resp) {
            Tools.removeCachingHeader(resp);
            return true;
        }
        return false;
    }

    /**
     * Sets specified ETag header (and implicitly removes/replaces any existing cache-controlling header: <i>Expires</i>,
     * <i>Cache-Control</i>, and <i>Pragma</i>)
     *
     * @param eTag The ETag value
     * @param expires The optional expires time, pass <code>-1</code> to set default expiry (+ 1 year)
     * @return <code>true</code> if set; otherwise <code>false</code>
     */
    public boolean setResponseETag(final String eTag, final long expires) {
        return setResponseETag(eTag, expires > 0 ? new Date(expires) : null);
    }

    /**
     * Sets specified ETag header (and implicitly removes/replaces any existing cache-controlling header: <i>Expires</i>,
     * <i>Cache-Control</i>, and <i>Pragma</i>)
     *
     * @param eTag The ETag value
     * @param expires The optional expires date, pass <code>null</code> to set default expiry (+ 1 year)
     * @return <code>true</code> if set; otherwise <code>false</code>
     */
    public boolean setResponseETag(final String eTag, final Date expires) {
        final HttpServletResponse resp = this.httpServletResponse;
        if (null != resp) {
            Tools.setETag(eTag, expires, resp);
            return true;
        }
        return false;
    }

    /**
     * Gets the decorator identifiers
     * 
     * @return The decorator identifiers
     */
    public List<String> getDecoratorIds() {
        return decoratorIds;
    }

    /**
     * Gets the session
     * 
     * @return The session
     */
    public ServerSession getSession() {
        return session;
    }

    /**
     * Sets the session
     * 
     * @param session The session to set
     */
    public void setSession(final ServerSession session) {
        this.session = session;
    }

    /**
     * Gets the HTTP Servlet request.
     * 
     * @return The HTTP Servlet request or <code>null</code> if absent
     */
    public HttpServletRequest optHttpServletRequest() {
        return httpServletRequest;
    }

    /**
     * Sets the HTTP Servlet request.
     * 
     * @param httpServletRequest The HTTP Servlet request to set
     */
    public void setHttpServletRequest(final HttpServletRequest httpServletRequest) {
        examineServletRequest(httpServletRequest);
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * Gets the expires time.
     * <p>
     * Have a notion of a time-to-live value.
     * 
     * @return The expires time or <code>-1</code> for no expiry
     */
    public long getExpires() {
        return expires;
    }

    /**
     * Sets the expires time
     * 
     * @param expires The expires time or <code>-1</code> for no expiry
     */
    public void setExpires(final long expires) {
        this.expires = expires;
    }

    /**
     * Gets the ETag which is the <code>"If-None-Match"</code> header taken from request.
     * 
     * @return The ETag or <code>null</code>
     */
    public String getETag() {
        String eTag = this.eTag;
        if (null == eTag) {
            eTag = getHeader("If-None-Match");
            if (null != eTag) {
                this.eTag = eTag;
            }
        }
        return eTag;
    }

    /**
     * Sets the ETag which is the <code>"If-None-Match"</code> header taken from request.
     * 
     * @param eTag The ETag to set
     */
    public void setETag(final String eTag) {
        this.eTag = eTag;
    }

    /**
     * Returns any extra path information associated with the URL the client sent when it made this request. The extra path information
     * follows the servlet path but precedes the query string and will start with a "/" character.
     * <p>
     * This method returns <code>null</code> if there was no extra path information.
     * <p>
     * Same as the value of the CGI variable PATH_INFO.
     * 
     * @return a <code>String</code>, decoded by the web container, specifying extra path information that comes after the servlet path but
     *         before the query string in the request URL; or <code>null</code> if the URL does not have any extra path information
     */
    public String getPathInfo() {
        return pathInfo;
    }

    /**
     * Sets the extra path information.
     * 
     * @param pathInfo The extra path information to set
     */
    public void setPathInfo(final String pathInfo) {
        this.pathInfo = pathInfo;
    }

    /**
     * Sets the URI part after path to the Servlet.
     * 
     * @param servletRequestUri The URI part
     */
    public void setServletRequestURI(final String servletRequestUri) {
        this.servletRequestUri = servletRequestUri;
    }

    /**
     * Gets the URI part after path to the Servlet.
     * 
     * @return The URI part or <code>null</code> if not applicable
     */
    public String getSerlvetRequestURI() {
        return servletRequestUri;
    }

    /**
     * Puts given name-value-pair into this data's parameters.
     * <p>
     * A <code>null</code> value removes the mapping.
     * 
     * @param name The parameter name
     * @param value The parameter value
     * @throws NullPointerException If name is <code>null</code>
     */
    public void putParameter(final String name, final String value) {
        if (null == name) {
            throw new NullPointerException("name is null");
        }
        if (null == value) {
            params.remove(name);
        } else {
            params.put(name, value);
        }
        final Parameterizable parameterizable = this.parameterizable;
        if (null != parameterizable) {
            // Write-though
            parameterizable.putParameter(name, value);
        }
    }

    /**
     * Checks for presence of specified parameter.
     * 
     * @param name The parameter name
     * @return <code>true</code> if such a parameter exists; otherwise <code>false</code> if absent
     */
    public boolean containsParameter(final String name) {
        if (null == name) {
            throw new NullPointerException("name is null");
        }
        return params.containsKey(name);
    }

    /**
     * Gets optional <code>int</code> parameter.
     * 
     * @param name The parameter name
     * @return The <code>int</code> value or <code>-1</code> if absent
     * @throws OXException If parameter value is not a number
     */
    public int getIntParameter(final String name) throws OXException {
        if (null == name) {
            throw new NullPointerException("name is null");
        }
        final String value = params.get(name);
        if (null == value) {
            return -1;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (final NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, value);
        }
    }

    /**
     * Gets this request's parameters as a {@link Map map}
     * 
     * @return The parameters as a {@link Map map}
     */
    public Map<String, String> getParameters() {
        return new HashMap<String, String>(params);
    }

    /**
     * Split pattern for CSV.
     */
    private static final Pattern SPLIT = Pattern.compile(" *, *");

    /**
     * Gets the comma-separated value.
     * 
     * @param name The parameter name
     * @return The values as an array
     */
    public String[] getParameterValues(final String name) {
        if (null == name) {
            throw new NullPointerException("name is null");
        }
        final String value = params.get(name);
        if (null == value) {
            return null;
        }
        return SPLIT.split(value, 0);
    }

    /**
     * Gets the value mapped to given parameter name.
     * 
     * @param name The parameter name
     * @return The value mapped to given parameter name or <code>null</code> if not present
     * @throws NullPointerException If name is <code>null</code>
     */
    public String getParameter(final String name) {
        if (null == name) {
            throw new NullPointerException("name is null");
        }
        return params.get(name);
    }

    /**
     * Gets the value mapped to given parameter name.
     * 
     * @param name The parameter name
     * @return The value mapped to given parameter name
     * @throws NullPointerException If name is <code>null</code>
     * @throws OXException If no such parameter exists
     */
    public String checkParameter(final String name) throws OXException {
        if (null == name) {
            throw new NullPointerException("name is null");
        }
        final String value = params.get(name);
        if (null == value) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        return value;
    }

    /**
     * Gets the value mapped to given parameter name.
     * 
     * @param name The parameter name
     * @return The value mapped to given parameter name
     * @throws NullPointerException If name is <code>null</code>
     * @throws OXException If no such parameter exists
     */
    public String requireParameter(final String name) throws OXException {
        return checkParameter(name);
    }

    /**
     * Gets the value mapped to given parameter name.
     * 
     * @param name The parameter name
     * @return The value mapped to given parameter name
     * @throws NullPointerException If name is <code>null</code>
     * @throws OXException If no such parameter exists or its value is empty
     */
    public String nonEmptyParameter(final String name) throws OXException {
        if (null == name) {
            throw new NullPointerException("name is null");
        }
        final String value = params.get(name);
        if (isEmpty(value)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        return value;
    }

    /**
     * Checks for presence of comma-separated <code>int</code> list.
     * 
     * @param name The parameter name
     * @return The <code>int</code> array
     * @throws OXException If an error occurs
     */
    public int[] checkIntArray(final String name) throws OXException {
        final String parameter = getParameter(name);
        if (null == parameter) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        if (name.equals(AJAXServlet.PARAMETER_COLUMNS)) {
            if (parameter.equals("all")) {
                return AbstractMailAction.COLUMNS_ALL_ALIAS;
            }
            if (parameter.equals("list")) {
                return AbstractMailAction.COLUMNS_LIST_ALIAS;
            }
        }
        final String[] sa = SPLIT.split(parameter, 0);
        final int[] ret = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            try {
                ret[i] = Integer.parseInt(sa[i].trim());
            } catch (final NumberFormatException e) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, parameter);
            }
        }
        return ret;
    }

    /**
     * Checks for presence of comma-separated <code>String</code> list.
     * 
     * @param name The parameter name
     * @return The <code>String</code> array
     * @throws OXException If parameter is absent
     */
    public String[] checkParameterArray(final String name) throws OXException {
        final String parameter = getParameter(name);
        if (null == parameter) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        return SPLIT.split(parameter, 0);
    }

    /**
     * Tries to get a parameter value as parsed as a certain type
     * 
     * @param name The parameter name
     * @param coerceTo The type the parameter should be interpreted as
     * @return The coerced value
     * @throws OXException if coercion fails
     */
    public <T> T getParameter(final String name, final Class<T> coerceTo) throws OXException {
        final String value = getParameter(name);
        try {
            return ServerServiceRegistry.getInstance().getService(StringParser.class).parse(value, coerceTo);
        } catch (final RuntimeException e) {
            /*
             * Auto-unboxing may lead to NullPointerExceptions or NumberFormatExceptions if e.g. null or "Hello" should be coerced to an
             * integer value. Handle RuntimeException here to cover all possible non-declarable exceptions.
             */
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, null == value ? "null" : value);
        }
    }

    /**
     * Gets all available parameter names wrapped by an {@link Iterator iterator}.
     * 
     * @return The {@link Iterator iterator} for available parameter names
     */
    public Iterator<String> getParameterNames() {
        return params.keySet().iterator();
    }

    /**
     * Gets an {@link Iterator iterator} for those parameters not matching given parameter names.
     * 
     * @param nonMatchingParameterNames The non-matching parameter names
     * @return An {@link Iterator iterator} for non-matching parameters
     */
    public Iterator<Entry<String, String>> getNonMatchingParameters(final Collection<String> nonMatchingParameterNames) {
        final Map<String, String> clone = new HashMap<String, String>(params);
        clone.keySet().removeAll(nonMatchingParameterNames);
        return clone.entrySet().iterator();
    }

    /**
     * Gets an {@link Iterator iterator} for those parameters matching given parameter names.
     * 
     * @param matchingParameterNames The matching parameter names
     * @return An {@link Iterator iterator} for matching parameters
     */
    public Iterator<Entry<String, String>> getMatchingParameters(final Collection<String> matchingParameterNames) {
        final Map<String, String> clone = new HashMap<String, String>(params);
        clone.keySet().retainAll(matchingParameterNames);
        return clone.entrySet().iterator();
    }

    /**
     * Gets the data object.
     * 
     * @return The data object or <code>null</code> if not available
     */
    public Object getData() {
        return data;
    }

    /**
     * Sets the data object.
     * 
     * @param data The data object to set
     */
    public void setData(final Object data) {
        this.data = data;
    }

    /**
     * Gets the format
     * 
     * @return The format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the format
     * 
     * @param format The format to set
     */
    public void setFormat(final String format) {
        this.format = format;
    }

    /**
     * Whether this request has a secure connection.
     * <p>
     * <b>Note</b>: This flag should already consider setting of <tt>'com.openexchange.forceHTTPS'</tt> property
     * 
     * @return <code>true</code> if this request has a secure connection; otherwise <code>false</code>
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Checks if this request data signals extended response.
     * 
     * @param request The request data
     * @return <code>true</code> if extended response is indicated; otherwise <code>false</code>
     */
    public boolean isExtendedResponse(final AJAXRequestData request) {
        return AJAXRequestDataTools.parseBoolParameter(ExtendedResponse.PARAM_EXTENDED_RESPONSE, request);
    }

    /**
     * Utility method that determines whether the request contains multipart content.
     * 
     * @return <code>true</code> if the request is multipart; <code>false</code> otherwise.
     */
    public boolean isMultipartContent() {
        return multipart;
    }

    /**
     * Sets the whether the request contains multipart content
     * 
     * @param multipart <code>true</code> if the request is multipart; <code>false</code> otherwise.
     */
    public void setMultipart(final boolean multipart) {
        this.multipart = multipart;
    }

    /**
     * Sets whether this request has a secure connection.
     * <p>
     * <b>Note</b>: This flag should already consider setting of <tt>'com.openexchange.forceHTTPS'</tt> property
     * 
     * @param secure <code>true</code> if this request has a secure connection; otherwise <code>false</code>
     */
    public void setSecure(final boolean secure) {
        this.secure = secure;
    }

    /**
     * Gets the upload stream. Retrieves the body of the request as binary data as an {@link InputStream}.
     * 
     * @return The upload stream or <code>null</code> if not available
     * @throws IOException If an I/O error occurs
     */
    public InputStream getUploadStream() throws IOException {
        return uploadStreamProvider.getInputStream();
    }

    /**
     * Sets the upload stream provider
     * 
     * @param uploadStream The upload stream provider to set
     */
    public void setUploadStreamProvider(final InputStreamProvider uploadStreamProvider) {
        this.uploadStreamProvider = uploadStreamProvider;
    }

    /**
     * Computes a list of missing parameters from a list of mandatory parameters. Or use {@link #require(String...)} to check for the
     * presence of certain parameters
     * 
     * @param mandatoryParameters The mandatory parameters expected.
     * @return A list of missing parameter names
     */
    public List<String> getMissingParameters(final String... mandatoryParameters) {
        final List<String> missing = new ArrayList<String>(mandatoryParameters.length);
        for (final String paramName : mandatoryParameters) {
            if (!params.containsKey(paramName)) {
                missing.add(paramName);
            }
        }
        return missing;
    }

    /**
     * Require a set of mandatory parameters, throw an exception, if a parameter is missing otherwise.
     */
    public void require(final String... mandatoryParameters) throws OXException {
        final List<String> missingParameters = getMissingParameters(mandatoryParameters);
        if (!missingParameters.isEmpty()) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(missingParameters.toString());
        }
    }

    /**
     * Signals if a parameter is set
     * 
     * @return <code>true</code> if set; otherwise <code>false</code>
     */
    public boolean isSet(final String paramName) {
        return params.containsKey(paramName);
    }

    /**
     * Sets a header value.
     * 
     * @param header The header name
     * @param value The header value
     */
    public void setHeader(final String header, final String value) {
        if (!isEmpty(header)) {
            if (null == value) {
                headers.remove(header);
            } else {
                headers.put(header, value);
            }
        }
    }

    /**
     * Gets a header value.
     * 
     * @return The header value or <code>null</code>
     */
    public String getHeader(final String header) {
        return headers.get(header);
    }

    /**
     * Gets a (coerced) header value
     * 
     * @return The header value or <code>null</code>
     */
    public <T> T getHeader(final String header, final Class<T> coerceTo) {
        return ServerServiceRegistry.getInstance().getService(StringParser.class).parse(getHeader(header), coerceTo);
    }

    /**
     * Gets the headers
     * 
     * @return The headers
     */
    public Map<String, String> getHeaders() {
        return new HashMap<String, String>(headers);
    }

    /**
     * Find out whether this request contains an uploaded file. Note that this is only possible via a servlet interface and not via the
     * multiple module.
     * 
     * @return <code>true</code> if one or more files were uploaded, <code>false</code> otherwise.
     * @throws OXException If upload files cannot be processed
     */
    public boolean hasUploads() throws OXException {
        processUpload();
        return !files.isEmpty();
    }

    /**
     * Retrieve file uploads.
     * 
     * @return A list of file uploads.
     * @throws OXException If upload files cannot be processed
     */
    public List<UploadFile> getFiles() throws OXException {
        processUpload();
        return Collections.unmodifiableList(files);
    }

    /**
     * Retrieve a file with a given form name.
     * 
     * @param name The name of the form field that include the file
     * @return The file, or null if no file field of this name was found
     * @throws OXException If upload files cannot be processed
     */
    public UploadFile getFile(final String name) throws OXException {
        processUpload();
        for (final UploadFile file : files) {
            if (file.getFieldName().equals(name)) {
                return file;
            }
        }
        return null;
    }

    private void processUpload() throws OXException {
        if (!multipart || null == httpServletRequest) {
            return;
        }
        final List<UploadFile> thisFiles = this.files;
        synchronized (thisFiles) {
            UploadEvent uploadEvent = this.uploadEvent;
            if (null == uploadEvent) {
                uploadEvent = AJAXServlet.processUploadStatic(httpServletRequest);
                final Iterator<UploadFile> iterator = uploadEvent.getUploadFilesIterator();
                while (iterator.hasNext()) {
                    thisFiles.add(iterator.next());
                }
                final Iterator<String> names = uploadEvent.getFormFieldNames();
                while (names.hasNext()) {
                    final String name = names.next();
                    putParameter(name, uploadEvent.getFormField(name));
                }
                this.uploadEvent = uploadEvent;
            }
        }
    }

    /**
     * Constructs a URL to this server, injecting the hostname and optionally the jvm route.
     * 
     * @param protocol The protocol to use (http or https). If <code>null</code>, defaults to the protocol used for this request.
     * @param path The path on the server. If <code>null</code> no path is inserted
     * @param withRoute Whether to include the jvm route in the server URL or not
     * @param query The query string. If <code>null</code> no query is included
     * @return A string builder with the URL so far, ready for meddling.
     */
    public StringBuilder constructURL(final String protocol, final String path, final boolean withRoute, final String query) {
        final StringBuilder url = new StringBuilder();
        String prot = protocol;
        if (prot == null) {
            prot = isSecure() ? "https://" : "http://";
        }
        url.append(prot);
        if (!prot.endsWith("://")) {
            url.append("://");
        }

        url.append(hostname);
        if (path != null) {
            if (path.length() == 0 || path.charAt(0) != '/') {
                url.append('/');
            }
            url.append(path);
        }
        if (withRoute) {
            url.append(";jsessionid=").append(route);
        }
        if (query != null) {
            if (query.length() == 0 || query.charAt(0) != '?') {
                url.append('?');
            }
            url.append(query);
        }
        return url;
    }

    /**
     * Gets the host name either fetched from HTTP request or from host name service
     * 
     * @return The host name
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the host name either fetched from HTTP request or from host name service
     * 
     * @param hostname The host name
     */
    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    /**
     * Gets the AJP route: &lt;http-session-id&gt; + <code>"." </code>+ &lt;route&gt;
     * 
     * @return The AJP route
     */
    public String getRoute() {
        return route;
    }

    /**
     * Gets the remote address
     * 
     * @return The remote address
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Sets the remote address
     * 
     * @param remoteAddress The remote address to set
     */
    public void setRemoteAddress(final String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    /**
     * Sets the AJP route: &lt;http-session-id&gt; + <code>"." </code>+ &lt;route&gt;
     * 
     * @param route The AJP route
     */
    public void setRoute(final String route) {
        this.route = route;
    }

    /**
     * Gets the associated upload event.
     * 
     * @return The upload event
     * @throws OXException If upload files cannot be processed
     */
    public UploadEvent getUploadEvent() throws OXException {
        processUpload();
        return uploadEvent;
    }

    /**
     * Cleans-up any upload resources.
     */
    public void cleanUploads() {
        synchronized (this.files) {
            final UploadEvent ue = uploadEvent;
            if (null != ue) {
                ue.cleanUp();
            }
        }
    }

    /**
     * Gets the module, e.g. <code>"mail"</code>.
     * 
     * @return The module
     */
    public String getModule() {
        return module;
    }

    /**
     * Sets the module, e.g. <code>"mail"</code>.
     * 
     * @param module The module
     */
    public void setModule(final String module) {
        this.module = module;
    }

    /**
     * Gets the action, e.g. <code>"all"</code>.
     * 
     * @return The action
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the action, e.g. <code>"all"</code>.
     * 
     * @param action The action
     */
    public void setAction(final String action) {
        this.action = action;
    }

    /**
     * Sets the data object.
     * 
     * @param object The data object
     * @param format The data's format
     */
    public void setData(final Object object, final String format) {
        setData(object);
        setFormat(format);
    }

    /**
     * Sets the request state.
     * 
     * @param state The state
     */
    public void setState(final AJAXState state) {
        this.state = state;
    }

    /**
     * Gets the request state.
     * 
     * @return The state
     */
    public AJAXState getState() {
        if (state == null) {
            state = new AJAXState();
        }
        return state;
    }

    /**
     * Tests existence of named property.
     * 
     * @param name The name
     * @return <code>true</code> if existent; else <code>false</code>
     */
    public boolean containsProperty(final String name) {
        if (null == name) {
            return false;
        }
        return properties.containsKey(name);
    }

    /**
     * Gets the property name.
     * 
     * @param name The name
     * @return The value or <code>null</code> if absent
     */
    @SuppressWarnings("unchecked")
    public <V> V getProperty(final String name) {
        if (null == name) {
            return null;
        }
        try {
            return (V) properties.get(name);
        } catch (final RuntimeException e) {
            return null;
        }
    }

    /**
     * Sets specified property. A <code>null</code> value performs a remove.
     * 
     * @param name The property name
     * @param value The property value
     */
    public void setProperty(final String name, final Object value) {
        if (null == value) {
            properties.remove(name);
        } else {
            properties.put(name, value);
        }
    }

    /**
     * Clears this request data's properties.
     */
    public void clearProperties() {
        properties.clear();
    }

    /**
     * Gets the property names.
     * 
     * @return The property names
     */
    public Set<String> getPropertyNames() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    /**
     * Gets the request data's properties.
     * 
     * @return The properties
     */
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /** Check for an empty string */
    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    /**
     * Sets the path prefix.
     * <p>
     * &lt;prefix&gt; + <code>'/'</code> + &lt;module&gt;
     * 
     * @param prefix The prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Gets the path prefix.
     * <p>
     * &lt;prefix&gt; + <code>'/'</code> + &lt;module&gt;
     * 
     * @return The prefix
     */
    public String getPrefix() {
        return prefix;
    }

}
