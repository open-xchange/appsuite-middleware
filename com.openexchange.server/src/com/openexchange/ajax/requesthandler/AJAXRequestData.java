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

package com.openexchange.ajax.requesthandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.slf4j.Logger;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.fields.RequestConstants;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.dispatcher.Parameterizable;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.notify.hostname.internal.HostDataImpl;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.java.Strings;
import com.openexchange.mail.json.actions.AbstractMailAction;
import com.openexchange.server.ServiceExceptionCode;
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

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AJAXRequestData.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

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
    private final @NonNull Map<String, String> params;

    /** The {@code Parameterizable} reference */
    private Parameterizable parameterizable;

    /** Map for headers */
    private final @NonNull Map<String, String> headers;

    /** Map for properties */
    private final @NonNull Map<String, Object> properties;

    /** Associated server session */
    private @Nullable ServerSession session;

    /** Whether a secure connection has been established */
    private boolean secure;

    /** The request body data */
    private @Nullable Object data;

    /** The module string */
    private @Nullable String module;

    /** The action string */
    private @Nullable String action;

    /** The upload stream provider */
    private InputStreamProvider uploadStreamProvider;

    /** List of uploaded files */
    private final @NonNull List<UploadFile> files;

    /** The host name */
    private @Nullable String hostname;

    /** The remote address */
    private @Nullable String remoteAddress;

    /** The Servlet's request URI */
    private @Nullable String servletRequestUri;

    /** The route: &lt;http-session-id&gt; + <code>"." </code>+ &lt;route&gt; */
    private @Nullable String route;

    /** The upload event */
    private volatile @Nullable UploadEvent uploadEvent;

    /** The format */
    private @Nullable String format;

    /** The state reference */
    private @Nullable AJAXState state;

    /** The eTag as parsed from <code>"If-None-Match"</code> request header */
    private @Nullable String eTag;

    /** The <code>User-Agent</code> value */
    private @Nullable String userAgent;

    /** The expires millis */
    private long expires;

    /** The path information */
    private @Nullable String pathInfo;

    /** The HTTP Servlet request */
    private @Nullable HttpServletRequest httpServletRequest;

    /** The decorator identifiers */
    private final @NonNull List<String> decoratorIds;

    /** The port number to which the request was sent */
    private int serverPort;

    /** The multipart flag. */
    private boolean multipart;

    /** The path prefix; &lt;prefix&gt; + <code>'/'</code> + &lt;module&gt; */
    private @Nullable String prefix;

    /** The optional <code>HttpServletResponse</code> instance */
    private @Nullable HttpServletResponse httpServletResponse;

    /** The request's last-modified time stamp as parsed from <code>"If-Modified-Since"</code> request header */
    private @Nullable Long lastModified;

    /** The requests host data **/
    private @Nullable volatile HostData hostData;

    /** The maximum allowed size of a single uploaded file or <code>-1</code> */
    private long maxUploadFileSize = -1L;

    /** The maximum allowed size of a complete request or <code>-1</code> */
    private long maxUploadSize = -1L;

    /** Internal flag to remember whether request body has already been loaded (if any) */
    private boolean requestBodyLoaded;

    /**
     * Initializes a new {@link AJAXRequestData}.
     *
     * @param json The JSON data
     * @throws OXException If an AJAX error occurs
     */
    public AJAXRequestData(final @Nullable JSONObject json) throws OXException {
        this();
        data = DataParser.checkJSONObject(json, RequestConstants.DATA);
        serverPort = -1;
    }

    /**
     * Initializes a new {@link AJAXRequestData}.
     *
     * @param data The payload to use data
     */
    public AJAXRequestData(final @Nullable Object data) {
        this();
        this.data = data;
        serverPort = -1;
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
        serverPort = -1;
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
        copy.serverPort = serverPort;
        copy.prefix = prefix;
        copy.requestBodyLoaded = requestBodyLoaded;

        /*
         * Not sure about following members, therefore leave to null
         */
        copy.state = null;
        copy.uploadEvent = null;
        copy.uploadStreamProvider = null;
        return copy;
    }

    /**
     * Sets the maximum allowed size of a single uploaded file or <code>-1</code>
     *
     * @param maxUploadFileSize The maximum allowed size of a single uploaded file or <code>-1</code>
     */
    public void setMaxUploadFileSize(long maxUploadFileSize) {
        this.maxUploadFileSize = maxUploadFileSize;
    }

    /**
     * Sets the maximum allowed size of a complete request or <code>-1</code>
     *
     * @param maxUploadSize The maximum allowed size of a complete request or <code>-1</code>
     */
    public void setMaxUploadSize(long maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }

    /**
     * Sets the <code>User-Agent</code> value
     *
     * @param userAgent The <code>User-Agent</code> value
     */
    public void setUserAgent(final @Nullable String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Gets the <code>User-Agent</code> value
     *
     * @return The <code>User-Agent</code> value
     */
    public @Nullable String getUserAgent() {
        return userAgent;
    }

    /**
     * Gets the port number to which the request was sent
     *
     * @return The port or <code>-1</code> if not set
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Sets the port number to which the request was sent
     *
     * @param serverPort The port to set
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
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
    public @NonNull AJAXRequestData setHttpServletResponse(final @Nullable HttpServletResponse resp) {
        this.httpServletResponse = resp;
        return this;
    }

    /**
     * Examines specified {@code HttpServletRequest} instance.
     *
     * @param servletRequest The HTTP Servlet request to examine
     */
    public void examineServletRequest(final @Nullable HttpServletRequest servletRequest) {
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
    public @NonNull AJAXRequestData addDecoratorId(final @NonNull String decoratorId) {
        decoratorIds.add(decoratorId);
        return this;
    }

    /**
     * Adds specified decorator identifiers.
     *
     * @param decoratorId The decorator identifiers
     * @return This AJAX request data instance with decorator identifiers added
     */
    public @NonNull AJAXRequestData addDecoratorIds(final @NonNull Collection<String> decoratorIds) {
        final List<String> thisDecoratorIds = this.decoratorIds;
        for (final String decoratorId : decoratorIds) {
            thisDecoratorIds.add(decoratorId);
        }
        return this;
    }

    /**
     * Checks if this AJAX request data has access to <code>HttpServletResponse</code> instance; thus offering support for:
     * <ul>
     * <li> {@link #optOutputStream()} </li>
     * <li> {@link #optWriter()} </li>
     * <li> {@link #setCharacterEncoding(String)} </li>
     * <ul>
     * <p>
     *
     * @return <code>true</code> if available; otherwise <code>false</code>
     */
    public boolean isHttpServletResponseAvailable() {
        return null != httpServletResponse;
    }

    /**
     * Gets the optional {@link HttpServletResponse} instance associated with this request data
     *
     * @return The {@link HttpServletResponse} instance or <code>null</code>
     */
    public HttpServletResponse optHttpServletResponse() {
        return httpServletResponse;
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
    public @Nullable OutputStream optOutputStream() throws IOException {
        final HttpServletResponse httpResponse = httpServletResponse;
        return null == httpResponse ? null : httpResponse.getOutputStream();
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
    public @Nullable PrintWriter optWriter() throws IOException {
        final HttpServletResponse httpResponse = httpServletResponse;
        return null == httpResponse ? null : httpResponse.getWriter();
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
    public boolean setResponseCharacterEncoding(final @NonNull String charset) {
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
    public boolean setResponseHeader(final @NonNull String name, final @Nullable String value) {
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
     * @param expiry The optional expiry milliseconds, pass <code>-1</code> to set default expiry (+ 5 minutes)
     * @return <code>true</code> if set; otherwise <code>false</code>
     */
    public boolean setResponseETag(final @NonNull String eTag, final long expiry) {
        final HttpServletResponse resp = this.httpServletResponse;
        if (null != resp) {
            Tools.setETag(eTag, expiry > 0 ? expiry : -1L, resp);
            return true;
        }
        return false;
    }

    /**
     * Gets the decorator identifiers
     *
     * @return The decorator identifiers
     */
    public @NonNull List<String> getDecoratorIds() {
        return decoratorIds;
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public @Nullable ServerSession getSession() {
        return session;
    }

    /**
     * Sets the session
     *
     * @param session The session to set
     */
    public void setSession(final @Nullable ServerSession session) {
        this.session = session;
    }

    /**
     * Gets the HTTP Servlet request.
     *
     * @return The HTTP Servlet request or <code>null</code> if absent
     */
    public @Nullable HttpServletRequest optHttpServletRequest() {
        return httpServletRequest;
    }

    /**
     * Sets the HTTP Servlet request.
     *
     * @param httpServletRequest The HTTP Servlet request to set
     */
    public void setHttpServletRequest(final @Nullable HttpServletRequest httpServletRequest) {
        examineServletRequest(httpServletRequest);
        this.httpServletRequest = httpServletRequest;
        if (null != httpServletRequest && serverPort < 0) {
            serverPort = httpServletRequest.getServerPort();
        }
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
     * Gets the Last-Modified time taken from <code>"If-Modified-Since"</code> header.
     * <p>
     * <code>"If-Modified-Since"</code> header should be greater than server's last-modified time stamp. If so, then return 304.<br>
     * This header is ignored if any <code>"If-None-Match"</code> header is specified.
     *
     * <pre>
     * long ifModifiedSince = request.getDateHeader(&quot;If-Modified-Since&quot;);
     * if (ifNoneMatch == null &amp;&amp; ifModifiedSince != -1 &amp;&amp; ifModifiedSince + 1000 &gt; lastModified) {
     * response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
     * response.setHeader(&quot;ETag&quot;, eTag); // Required in 304.
     * response.setDateHeader(&quot;Expires&quot;, expires); // Postpone cache with 1 week.
     * return;
     * }
     * </pre>
     *
     * @return The Last-Modified time or <code>-1</code>
     */
    public long getLastModified() {
        Long lastModified = this.lastModified;
        if (null == lastModified) {
            String ifModifiedSince = getHeader("If-Modified-Since");
            if (null != ifModifiedSince) {
                try {
                    long time = Tools.parseHeaderDate(ifModifiedSince).getTime();
                    lastModified = Long.valueOf(time);
                    this.lastModified = lastModified;
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        return null == lastModified ? -1L : lastModified.longValue();
    }

    /**
     * Sets the Last-Modified time
     *
     * @param lastModified The Last-Modified time to set
     */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified < 0 ? null : Long.valueOf(lastModified);
    }

    /**
     * Gets the ETag which is the <code>"If-None-Match"</code> header taken from request.
     *
     * @return The ETag or <code>null</code>
     */
    public @Nullable String getETag() {
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
    public void setETag(final @Nullable String eTag) {
        this.eTag = eTag;
    }

    /**
     * Checks whether request body was already attempted being loaded.
     *
     * @return <code>true</code> if loaded; otherwise <code>false</code>
     */
    boolean isRequestBodyLoaded() {
        return requestBodyLoaded;
    }

    /**
     * Sets the whether request body was already attempted being loaded.
     *
     * @param requestBodyLoaded <code>true</code> if loaded; otherwise <code>false</code>
     */
    void setRequestBodyLoaded(boolean requestBodyLoaded) {
        this.requestBodyLoaded = requestBodyLoaded;
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
    public @Nullable String getPathInfo() {
        return pathInfo;
    }

    /**
     * Sets the extra path information.
     *
     * @param pathInfo The extra path information to set
     */
    public void setPathInfo(final @Nullable String pathInfo) {
        this.pathInfo = pathInfo;
    }

    /**
     * Sets the URI part after path to the Servlet.
     *
     * @param servletRequestUri The URI part
     */
    public void setServletRequestURI(final @Nullable String servletRequestUri) {
        this.servletRequestUri = servletRequestUri;
    }

    /**
     * Gets the URI part after path to the Servlet.
     *
     * @return The URI part or <code>null</code> if not applicable
     */
    public @Nullable String getSerlvetRequestURI() {
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
    public void putParameter(final @Nullable String name, final @Nullable String value) {
        putParameter0(name, value, true);
    }

    private void putParameter0(final @Nullable String name, final @Nullable String value, boolean writeThrough) {
        if (null == name) {
            throw new NullPointerException("name is null");
        }
        if (null == value) {
            params.remove(name);
        } else {
            params.put(name, value);
        }
        if (writeThrough) {
            Parameterizable parameterizable = this.parameterizable;
            if (null != parameterizable) {
                // Write-though
                try {
                    parameterizable.putParameter(name, value);
                } catch (Exception e) {
                    LOGGER.debug("Failed to add parameter {} to underlying {} instance", name, parameterizable.getClass().getName(), e);
                }
            }
        }
    }

    /**
     * Checks for presence of specified parameter.
     *
     * @param name The parameter name
     * @return <code>true</code> if such a parameter exists; otherwise <code>false</code> if absent
     */
    public boolean containsParameter(final @Nullable String name) {
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
    public int getIntParameter(final @Nullable String name) throws OXException {
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
    public @NonNull Map<String, String> getParameters() {
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
    public String[] getParameterValues(final @Nullable String name) {
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
    public @Nullable String getParameter(final @Nullable String name) {
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
    public @NonNull String checkParameter(final @Nullable String name) throws OXException {
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
    public @NonNull String requireParameter(final @Nullable String name) throws OXException {
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
    public @NonNull String nonEmptyParameter(final @Nullable String name) throws OXException {
        if (null == name) {
            throw new NullPointerException("name is null");
        }
        final String value = params.get(name);
        if (com.openexchange.java.Strings.isEmpty(value)) {
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
    public @NonNull int[] checkIntArray(final @NonNull String name) throws OXException {
        final String parameter = getParameter(name);
        if (null == parameter) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        if (name.equals(AJAXServlet.PARAMETER_COLUMNS)) {
            if (parameter.equals("all")) {
                return AbstractMailAction.FIELDS_ALL_ALIAS;
            }
            if (parameter.equals("list")) {
                return AbstractMailAction.FIELDS_LIST_ALIAS;
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
    public @NonNull String[] checkParameterArray(final @Nullable String name) throws OXException {
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
    public @Nullable <T> T getParameter(final @Nullable String name, final @NonNull Class<T> coerceTo) throws OXException {
        return getParameter(name, coerceTo, false);
    }

    /**
     * Get the value of an optional parameter
     *
     * @param name name of the optional parameter
     * @param coerceTo parse the value to the specified type
     * @param optional <code>true</code> if parameter is optional; otherwise <code>false</code> to throw an exception on absence
     * @return the coerced value
     * @throws OXException if coercion fails
     */
    public @Nullable <T> T getParameter(final @Nullable String name, final @NonNull Class<T> coerceTo, final boolean optional) throws OXException {
        final String value = getParameter(name);
        if (null == value) {
            if (!optional) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, "null");
            }
            return null;
        }

        try {
            // Check for StringParser
            StringParser parser = ServerServiceRegistry.getInstance().getService(StringParser.class);
            if (null != parser) {
                return parser.parse(value, coerceTo);
            }

            // Check by type
            if (boolean.class.equals(coerceTo) || Boolean.class.equals(coerceTo)) {
                return (T) Boolean.valueOf(value);
            }
            if (int.class.equals(coerceTo) || Integer.class.equals(coerceTo)) {
                return (T) Integer.valueOf(value);
            }
            if (long.class.equals(coerceTo) || Long.class.equals(coerceTo)) {
                return (T) Long.valueOf(value);
            }
            throw ServiceExceptionCode.absentService(StringParser.class);
        } catch (final RuntimeException e) {
            /*
             * Auto-unboxing may lead to NullPointerExceptions or NumberFormatExceptions if e.g. null or "Hello" should be coerced to an
             * integer value. Handle RuntimeException here to cover all possible non-declarable exceptions.
             */
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, name, value);
        }
    }

    /**
     * Gets all available parameter names wrapped by an {@link Iterator iterator}.
     *
     * @return The {@link Iterator iterator} for available parameter names
     */
    public @NonNull Iterator<String> getParameterNames() {
        return params.keySet().iterator();
    }

    /**
     * Gets an {@link Iterator iterator} for those parameters not matching given parameter names.
     *
     * @param nonMatchingParameterNames The non-matching parameter names
     * @return An {@link Iterator iterator} for non-matching parameters
     */
    public @NonNull Iterator<Entry<String, String>> getNonMatchingParameters(final Collection<String> nonMatchingParameterNames) {
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
    public @NonNull Iterator<Entry<String, String>> getMatchingParameters(final Collection<String> matchingParameterNames) {
        final Map<String, String> clone = new HashMap<String, String>(params);
        clone.keySet().retainAll(matchingParameterNames);
        return clone.entrySet().iterator();
    }

    /**
     * Gets the data object.
     *
     * @return The data object
     * @throws OXException If data object is <code>null</code>
     * @see #getData()
     */
    public @NonNull Object requireData() throws OXException {
        final Object data = this.data;
        if (null == data) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        return data;
    }

    /**
     * Gets the data object.
     *
     * @return The data object or <code>null</code> if not available
     * @see #requireData()
     */
    public @Nullable Object getData() {
        return data;
    }

    public @Nullable <T> T getData(Class<T> klazz) throws OXException {
        final Object local = this.data;
        if ((local == null) || (klazz == null)) {
            return null;
        }

        if (klazz.isInstance(local)) {
            return klazz.cast(local);
        }

        if (klazz == String.class) {
            return (T) local.toString();
        }

        try {
            return MAPPER.readValue(getData(String.class), klazz);
        } catch (JsonParseException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        } catch (JsonMappingException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        } catch (IOException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    /**
     * Sets the data object.
     *
     * @param data The data object to set
     */
    public void setData(final @Nullable Object data) {
        this.data = data;
    }

    /**
     * Gets the format
     *
     * @return The format
     */
    public @Nullable String getFormat() {
        return format;
    }

    /**
     * Sets the format
     *
     * @param format The format to set
     */
    public void setFormat(final @Nullable String format) {
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
    public boolean isExtendedResponse(final @NonNull AJAXRequestData request) {
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
    public @Nullable InputStream getUploadStream() throws IOException {
        final InputStreamProvider usp = uploadStreamProvider;
        return null == usp ? null : usp.getInputStream();
    }

    /**
     * Sets the upload stream provider
     *
     * @param uploadStream The upload stream provider to set
     */
    public void setUploadStreamProvider(final @Nullable InputStreamProvider uploadStreamProvider) {
        this.uploadStreamProvider = uploadStreamProvider;
    }

    /**
     * Checks if this request data has an upload stream provider set.
     *
     * @return <code>true</code> if an upload stream provider is available; otherwise <code>false</code>
     */
    public boolean hasUploadStreamProvider() {
        return null != this.uploadStreamProvider;
    }

    /**
     * Computes a list of missing parameters from a list of mandatory parameters. Or use {@link #require(String...)} to check for the
     * presence of certain parameters
     *
     * @param mandatoryParameters The mandatory parameters expected.
     * @return A list of missing parameter names
     */
    public @NonNull List<String> getMissingParameters(final String... mandatoryParameters) {
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
    public void setHeader(final @Nullable String header, final @Nullable String value) {
        if (!com.openexchange.java.Strings.isEmpty(header)) {
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
    public String getHeader(final @Nullable String header) {
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
    public @NonNull Map<String, String> getHeaders() {
        return new HashMap<String, String>(headers);
    }

    /**
     * Find out whether this request contains an uploaded file. Note that this is only possible via a servlet interface and not via the
     * multiple module.
     *
     * @return <code>true</code> if one or more files were uploaded, <code>false</code> otherwise.
     * @throws OXException If upload files cannot be processed
     * @see #hasUploads(long, long)
     */
    public boolean hasUploads() throws OXException {
        processUpload(maxUploadFileSize, maxUploadSize);
        return !files.isEmpty();
    }

    /**
     * Find out whether this request contains an uploaded file. Note that this is only possible via a servlet interface and not via the
     * multiple module.
     *
     * @param maxUploadFileSize The maximum allowed size of a single uploaded file or <code>-1</code>
     * @param maxUploadSize The maximum allowed size of a complete request or <code>-1</code>
     * @return <code>true</code> if one or more files were uploaded, <code>false</code> otherwise.
     * @throws OXException If upload files cannot be processed
     */
    public boolean hasUploads(long maxUploadFileSize, long maxUploadSize) throws OXException {
        this.maxUploadFileSize = maxUploadFileSize > 0 ? maxUploadFileSize : -1L;
        this.maxUploadSize = maxUploadSize > 0 ? maxUploadSize : -1L;
        processUpload(maxUploadFileSize, maxUploadSize);
        return !files.isEmpty();
    }

    /**
     * Retrieve file uploads.
     *
     * @return A list of file uploads.
     * @throws OXException If upload files cannot be processed
     * @see #getFiles(long, long)
     */
    public List<UploadFile> getFiles() throws OXException {
        processUpload(maxUploadFileSize, maxUploadSize);
        return Collections.unmodifiableList(files);
    }

    /**
     * Retrieve file uploads.
     *
     * @param maxUploadFileSize The maximum allowed size of a single uploaded file or <code>-1</code>
     * @param maxUploadSize The maximum allowed size of a complete request or <code>-1</code>
     * @return A list of file uploads.
     * @throws OXException If upload files cannot be processed
     */
    public List<UploadFile> getFiles(long maxUploadFileSize, long maxUploadSize) throws OXException {
        this.maxUploadFileSize = maxUploadFileSize > 0 ? maxUploadFileSize : -1L;
        this.maxUploadSize = maxUploadSize > 0 ? maxUploadSize : -1L;
        processUpload(maxUploadFileSize, maxUploadSize);
        return Collections.unmodifiableList(files);
    }

    /**
     * Retrieve a file with a given form name.
     *
     * @param name The name of the form field that include the file
     * @return The file, or null if no file field of this name was found
     * @throws OXException If upload files cannot be processed
     * @see #getFile(String, long, long)
     */
    public @Nullable UploadFile getFile(String name) throws OXException {
        processUpload(maxUploadFileSize, maxUploadSize);
        for (final UploadFile file : files) {
            if (file.getFieldName().equals(name)) {
                return file;
            }
        }
        return null;
    }

    /**
     * Retrieve a file with a given form name.
     *
     * @param name The name of the form field that include the file
     * @param maxUploadFileSize The maximum allowed size of a single uploaded file or <code>-1</code>
     * @param maxUploadSize The maximum allowed size of a complete request or <code>-1</code>
     * @return The file, or <code>null</code> if no file field of this name was found
     * @throws OXException If upload files cannot be processed
     */
    public @Nullable UploadFile getFile(String name, long maxUploadFileSize, long maxUploadSize) throws OXException {
        this.maxUploadFileSize = maxUploadFileSize > 0 ? maxUploadFileSize : -1L;
        this.maxUploadSize = maxUploadSize > 0 ? maxUploadSize : -1L;
        processUpload(maxUploadFileSize, maxUploadSize);
        for (final UploadFile file : files) {
            if (file.getFieldName().equals(name)) {
                return file;
            }
        }
        return null;
    }

    /**
     * Gets the associated upload event.
     *
     * @return The upload event
     * @throws OXException If upload files cannot be processed
     * @see #getUploadEvent(long, long)
     */
    public UploadEvent getUploadEvent() throws OXException {
        processUpload(maxUploadFileSize, maxUploadSize);
        return uploadEvent;
    }

    /**
     * Gets the associated upload event.
     *
     * @param maxUploadFileSize The maximum allowed size of a single uploaded file or <code>-1</code>
     * @param maxUploadSize The maximum allowed size of a complete request or <code>-1</code>
     * @return The upload event
     * @throws OXException If upload files cannot be processed
     */
    public UploadEvent getUploadEvent(long maxUploadFileSize, long maxUploadSize) throws OXException {
        this.maxUploadFileSize = maxUploadFileSize > 0 ? maxUploadFileSize : -1L;
        this.maxUploadSize = maxUploadSize > 0 ? maxUploadSize : -1L;
        processUpload(maxUploadFileSize, maxUploadSize);
        return uploadEvent;
    }

    private void processUpload(long maxFileSize, long maxOverallSize) throws OXException {
        if (!multipart || null == httpServletRequest) {
            return;
        }
        final List<UploadFile> thisFiles = this.files;
        synchronized (thisFiles) {
            UploadEvent uploadEvent = this.uploadEvent;
            if (null == uploadEvent) {
                uploadEvent = AJAXServlet.processUploadStatic(httpServletRequest, maxFileSize, maxOverallSize, session);
                this.uploadEvent = uploadEvent;
                final Iterator<UploadFile> iterator = uploadEvent.getUploadFilesIterator();
                while (iterator.hasNext()) {
                    thisFiles.add(iterator.next());
                }
                final Iterator<String> names = uploadEvent.getFormFieldNames();
                while (names.hasNext()) {
                    final String name = names.next();
                    putParameter0(name, uploadEvent.getFormField(name), false);
                }
            }
        }
    }

    /**
     * Constructs a URL to this server, injecting the host name and optionally the JVM route.
     *
     * <pre>
     * "http(s)://" + &lt;hostname&gt; + "/" + &lt;path&gt; + &lt;jvm-route&gt;
     * </pre>
     *
     * @param path The path on the server. If <code>null</code> no path is inserted
     * @param withRoute Whether to include the JVM route in the server URL or not
     * @return A string builder with the URL so far, ready for meddling.
     */
    public StringBuilder constructURL(final String path, final boolean withRoute) {
        return constructURL(null, path, withRoute, null);
    }

    /**
     * Constructs a URL to this server, injecting the host name and optionally the JVM route.
     *
     * <pre>
     * &lt;protocol&gt; + "://" + &lt;hostname&gt; + "/" + &lt;path&gt; + &lt;jvm-route&gt; + "?" + &lt;query-string&gt;
     * </pre>
     *
     * @param protocol The protocol to use (HTTP or HTTPS). If <code>null</code>, defaults to the protocol used for this request.
     * @param path The path on the server. If <code>null</code> no path is inserted
     * @param withRoute Whether to include the JVM route in the server URL or not
     * @param query The query string. If <code>null</code> no query is included
     * @return A string builder with the URL so far, ready for meddling.
     */
    public StringBuilder constructURL(final String protocol, final String path, final boolean withRoute, final String query) {
        final StringBuilder url = new StringBuilder(64 + (null == query ? 0 : query.length()));
        // Protocol/schema
        {
            String prot = protocol;
            if (prot == null) {
                prot = isSecure() ? "https://" : "http://";
            }
            url.append(prot);
            if (!prot.endsWith("://")) {
                url.append("://");
            }
        }
        // Host name
        url.append(hostname);
        // Path
        if (path != null) {
            if (!path.startsWith("/")) {
                url.append('/');
            }
            url.append(path);
        }
        // JVM route
        if (withRoute) {
            url.append(";jsessionid=").append(route);
        }
        // Query string
        if (query != null) {
            if (!query.startsWith("?")) {
                url.append('?');
            }
            url.append(query);
        }
        // Return URL
        return url;
    }

    /**
     * Constructs a URL to this server, injecting the host name and optionally the JVM route.
     *
     * <pre>
     * &lt;protocol&gt; + "://" + &lt;hostname&gt; + "/" + &lt;path&gt; + &lt;jvm-route&gt; + "?" + &lt;query-string&gt;
     * </pre>
     *
     * @param protocol The protocol to use (HTTP or HTTPS). If <code>null</code>, defaults to the protocol used for this request.
     * @param path The path on the server. If <code>null</code> no path is inserted
     * @param withRoute Whether to include the JVM route in the server URL or not
     * @param params The query string parameters. If <code>null</code> no query is included
     * @return A string builder with the URL so far, ready for meddling.
     */
    public StringBuilder constructURLWithParameters(final String protocol, final String path, final boolean withRoute, final Map<String, String> params) {
        final StringBuilder url = new StringBuilder(128);
        // Protocol/schema
        {
            String prot = protocol;
            if (prot == null) {
                prot = isSecure() ? "https://" : "http://";
            }
            url.append(prot);
            if (!prot.endsWith("://")) {
                url.append("://");
            }
        }
        // Host name
        url.append(hostname);
        // Path
        if (path != null) {
            if (!path.startsWith("/")) {
                url.append('/');
            }
            url.append(path);
        }
        // JVM route
        if (withRoute) {
            url.append(";jsessionid=").append(route);
        }
        // Query string
        if (params != null) {
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                if (!Strings.isEmpty(key)) {
                    if (first) {
                        url.append('?');
                        first = false;
                    } else {
                        url.append('&');
                    }
                    url.append(AJAXUtility.encodeUrl(key, true));
                    String value = entry.getValue();
                    if (!Strings.isEmpty(value)) {
                        url.append('=').append(AJAXUtility.encodeUrl(value, true));
                    }
                }
            }
        }
        // Return URL
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
     * Gets the host data. The result will not be <code>null</code> on instances
     * created by the dispatcher on normal HTTP requests.
     *
     * @return The host data
     */
    public @Nullable HostData getHostData() {
        HostData hostData = this.hostData;
        if (hostData == null) {
            synchronized (this) {
                hostData = this.hostData;
                if (hostData == null && hostname != null && route != null && prefix != null) {
                    String httpSesssionID = this.route;
                    String route = Tools.extractRoute(httpSesssionID);
                    hostData = new HostDataImpl(secure, hostname, serverPort, httpSesssionID, route, prefix);
                    this.hostData = hostData;
                }
            }
        }

        return hostData;
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
     * Gets the route: &lt;http-session-id&gt; + <code>"." </code>+ &lt;route&gt;
     *
     * @return The route
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
     * Sets the route: &lt;http-session-id&gt; + <code>"." </code>+ &lt;route&gt;
     *
     * @param route The route
     */
    public void setRoute(final String route) {
        this.route = route;
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
     * Gets the normalized module, e.g. <code>"files/myFile.txt"</code> will return <code>"files"</code>.
     * <p>
     * With '/' concatenated module identifiers will still be returned as they are, e. g. <code>"oauth/account"</code> will stay as it is.
     *
     * @return The normalized module
     */
    public String getNormalizedModule() {
        String lModule = module;
        int pos = lModule.indexOf('/');
        if ((pos > 0) && (pathInfo != null) && (lModule.endsWith(pathInfo))) {
            lModule = lModule.substring(0, pos);
        }
        return lModule;
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
        AJAXState state = this.state;
        if (state == null) {
            state = this.state = new AJAXState();
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
