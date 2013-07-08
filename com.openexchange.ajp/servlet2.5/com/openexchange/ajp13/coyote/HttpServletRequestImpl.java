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

package com.openexchange.ajp13.coyote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.Services;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.exception.AJPv13Exception.AJPCode;
import com.openexchange.ajp13.servlet.ServletConfigLoader;
import com.openexchange.ajp13.servlet.ServletRequestWrapper;
import com.openexchange.ajp13.servlet.http.HttpSessionManagement;
import com.openexchange.ajp13.servlet.http.HttpSessionWrapper;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.dispatcher.Parameterizable;
import com.openexchange.exception.OXException;
import com.openexchange.java.DefaultHashKeyGenerator;
import com.openexchange.java.HashKeyGenerator;
import com.openexchange.java.HashKeyMap;
import com.openexchange.log.Log;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link HttpServletRequestImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HttpServletRequestImpl implements HttpServletRequest, Parameterizable {

    private static final org.apache.commons.logging.Log LOG =
        Log.valueOf(com.openexchange.log.LogFactory.getLog(HttpServletRequestImpl.class));

    /**
     * The name of the "Content-Type" header.
     */
    private static final String CONTENT_TYPE = Constants.CONTENT_TYPE;

    /**
     * The name of the "Content-Length" header.
     */
    private static final String CONTENT_LENGTH = Constants.CONTENT_LENGTH;

    /**
     * A set for known single-value headers. Those headers which occur only once in HTTP headers.
     */
    private static final Set<String> SINGLE_VALUE_HEADERS = Constants.SINGLE_VALUE_HEADERS;

    private static final String HOST = "Host";

    private static final SecureRandom RANDOM = Constants.RANDOM;

    /*-
     * ------------------- Member stuff ---------------------
     */

    private volatile HashKeyGenerator hashKeyGenerator;

    private final Map<String, Object> attributes;

    private final HashKeyMap<List<String>> parameters;

    private final Map<String, List<String>> headers;

    private String characterEncoding;

    private String protocol;

    private String remoteAddress;

    private String remoteHost;

    private String serverName;

    private String scheme;

    private int serverPort;

    private boolean secure;

    private String authType;

    private Cookie[] cookies;

    private String method;

    private String pathInfo;

    private String requestURL;

    private String requestURI;

    private String pathTranslated;

    private String servletPath;

    private String queryString;

    private String contextPath;

    private String remoteUser;

    private Principal userPrincipal;

    private boolean requestedSessionIdFromCookie;

    private boolean requestedSessionIdFromURL;

    private final HttpServletResponseImpl response;

    private HttpServlet servletInstance;

    private volatile long startTime;

    private ActionAwareServletInputStream servletInputStream;

    private String localAddr;

    private String localName;

    private int localPort;

    private int remotePort;

    private String instanceId;

    private boolean formData;

    private long contentLength;

    private final int max;

    /**
     * Initializes a new {@link ServletRequestWrapper}.
     */
    public HttpServletRequestImpl(final HttpServletResponseImpl response) {
        super();
        contentLength = -1L;
        max = AJPv13Config.getMaxRequestParameterCount();
        contextPath = "";
        requestedSessionIdFromCookie = true;
        this.response = response;
        protocol = "HTTP/1.1";
        scheme = "http";
        final String salt = Integer.toString(RANDOM.nextInt(), 10); // request-specific salt
        final HashKeyGenerator hashKeyGenerator = new DefaultHashKeyGenerator(salt);
        this.hashKeyGenerator = hashKeyGenerator;
        attributes = new HashMap<String, Object>(32);
        parameters = new HashKeyMap<List<String>>(max > 0 ? max : 64).setGenerator(hashKeyGenerator);
        headers = new HashMap<String, List<String>>(16);
        try {
            setHeaderInternal(CONTENT_LENGTH, Integer.toString(-1), false);
        } catch (final AJPv13Exception e) {
            // Cannot occur
        }
    }

    /**
     * Sets the input buffer
     *
     * @param inputBuffer The input buffer
     */
    public void setInputBuffer(final InputBuffer inputBuffer) {
        servletInputStream = null == inputBuffer ? null : new ActionAwareServletInputStream(inputBuffer, this);
    }

    /**
     * Dump specified bytes into buffer.
     *
     * @param bytes The bytes
     */
    public void dumpToBuffer(final byte[] bytes) {
        servletInputStream.dumpToBuffer(bytes);
    }

    /**
     * Append specified bytes to buffer.
     *
     * @param bytes The bytes
     */
    public void appendToBuffer(final byte[] bytes) {
        servletInputStream.appendToBuffer(bytes);
    }

    /**
     * Checks if this request's content type indicates the form data: <code>"application/x-www-form-urlencoded"</code>
     *
     * @return <code>true</code> if form data; otherwise <code>false</code>
     */
    public boolean isFormData() {
        return formData;
    }

    /**
     * Recycles this request.
     */
    public void recycle() {
        servletInputStream.recycle();
        final String salt = Integer.toString(RANDOM.nextInt(), 10); // request-specific salt
        final HashKeyGenerator hashKeyGenerator = new DefaultHashKeyGenerator(salt);
        this.hashKeyGenerator = hashKeyGenerator;
        attributes.clear();
        parameters.clear();
        parameters.setGenerator(hashKeyGenerator);
        headers.clear();
        try {
            setHeaderInternal(CONTENT_LENGTH, Integer.toString(-1), false);
        } catch (final AJPv13Exception e) {
            // Cannot occur
        }
        formData = false;
        instanceId = null;
        localAddr = null;
        localName = null;
        localPort = 0;
        remotePort = 0;
        characterEncoding = null;
        protocol = "HTTP/1.1";
        remoteAddress = null;
        remoteHost = null;
        serverName = null;
        scheme = "http";
        serverPort = 0;
        secure = false;
        authType = null;
        cookies = null;
        method = null;
        pathInfo = null;
        requestURL = null;
        requestURI = null;
        pathTranslated = null;
        servletPath = null;
        queryString = null;
        contextPath = "";
        remoteUser = null;
        userPrincipal = null;
        requestedSessionIdFromCookie = true;
        requestedSessionIdFromURL = false;
        servletInstance = null;
        startTime = 0L;
        contentLength = -1L;
    }

    /**
     * Sets the <code>Content-Length</code> header.
     *
     * @param contentLength The content length
     */
    public void setContentLength(final long contentLength) {
        try {
            setHeaderInternal(CONTENT_LENGTH, Long.toString(contentLength), false);
        } catch (final AJPv13Exception e) {
            // Cannot occur
        }
    }

    /**
     * Sets the <code>Content-Type</code> header.
     *
     * @param contentType The content type
     * @throws AJPv13Exception If setting <code>Content-Type</code> header fails
     */
    public void setContentType(final String contentType) throws AJPv13Exception {
        setHeaderInternal(CONTENT_TYPE, contentType, true);
    }

    /**
     * Sets a parameter.
     *
     * @param name The parameter name
     * @param value The parameter value
     */
    public void setParameter(final String name, final String value) {
        final List<String> values = parameters.get(name);
        if (null == values) {
            if (max > 0 && parameters.size() >= max) {
                throw new IllegalStateException("Max. allowed number of request parameters ("+max+") exceeded");
            }
            parameters.put(name, newLinkedList(value));
        } else {
            values.add(value);
        }
    }

    @Override
    public void putParameter(String name, String value) {
        if (null == name) {
            throw new NullPointerException("name is null");
        }
        if (null == value) {
            parameters.remove(name);
        } else {
            setParameter(name, value);
        }
    }

    private static List<String> newLinkedList(final String initialValue) {
        final List<String> list = new LinkedList<String>();
        list.add(initialValue);
        return list;
    }

    /**
     * Sets a header value bound to given header name.
     *
     * @param name The header name
     * @param value The header value
     * @param isContentType <code>true</code> if <tt>name</tt> denotes the <code>Content-Type</code> header; otherwise <code>false</code>
     * @throws AJPv13Exception If setting header fails
     */
    public final void setHeader(final String name, final String value, final boolean isContentType) throws AJPv13Exception {
        setHeaderInternal(name.toLowerCase(Locale.ENGLISH), value, isContentType);
    }

    private final void setHeaderInternal(final String name, final String value, final boolean isContentType) throws AJPv13Exception {
        if (isContentType) {
            handleContentType(value);
        }
        final List<String> prevValues = headers.get(name);
        if (null == prevValues || SINGLE_VALUE_HEADERS.contains(name)) {
            if (CONTENT_LENGTH.equals(name)) {
                contentLength = Long.parseLong(value);
            }
            headers.put(name, Collections.singletonList(value));
        } else {
            /*
             * Header may carry multiple values
             */
            if (null == prevValues) {
                headers.put(name, newLinkedList(value));
            } else {
                prevValues.add(value);
            }
        }
    }

    private static final String MIME_FORM_DATA = "application/x-www-form-urlencoded";

    private final void handleContentType(final String value) throws AJPv13Exception {
        if (value != null && value.length() > 0) {
            final ContentType ct;
            try {
                ct = new ContentType(value);
            } catch (final OXException e) {
                com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(HttpServletRequestImpl.class)).error(
                    e.getMessage(),
                    e);
                throw new AJPv13Exception(AJPCode.INVALID_CONTENT_TYPE, true, e, value);
            }
            if (ct.startsWith(MIME_FORM_DATA)) {
                formData = true;
            }
            if (ct.containsCharsetParameter()) {
                try {
                    setCharacterEncoding(ct.getCharsetParameter());
                } catch (final UnsupportedEncodingException e) {
                    throw new AJPv13Exception(AJPCode.UNSUPPORTED_ENCODING, true, e, ct.getCharsetParameter());
                }
            } else {
                /*
                 * Although HTTP specifies to use charset "ISO-8859-1" if protocol is set to "HTTP/1.1", we use a pre-defined charset given
                 * through configuration file
                 */
                try {
                    setCharacterEncoding(ServerConfig.getProperty(Property.DefaultEncoding));
                } catch (final UnsupportedEncodingException e) {
                    throw new AJPv13Exception(AJPCode.UNSUPPORTED_ENCODING, true, e, ServerConfig.getProperty(Property.DefaultEncoding));
                }
            }
        } else {
            /*
             * Although HTTP specifies to use charset "ISO-8859-1" if protocol is set to "HTTP/1.1", we use a pre-defined charset given
             * through configuration file
             */
            try {
                setCharacterEncoding(ServerConfig.getProperty(Property.DefaultEncoding));
            } catch (final UnsupportedEncodingException e) {
                throw new AJPv13Exception(AJPCode.UNSUPPORTED_ENCODING, true, e, ServerConfig.getProperty(Property.DefaultEncoding));
            }
        }
    }

    /**
     * Gets the header value associated with specified name.
     *
     * @param name The header name
     * @return The header name
     */
    @Override
    public String getHeader(final String name) {
        return makeString(headers.get(name.toLowerCase(Locale.ENGLISH)));
    }

    /**
     * Checks if this servlet request contains a header associated with specified name.
     *
     * @param name The header name
     * @return <code>true</code> if this servlet request contains such a header; otherwise <code>false</code>
     */
    public boolean containsHeader(final String name) {
        return headers.containsKey(name.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Gets the header values associated with specified header name.
     *
     * @param name The header name
     * @return The header values as an {@link Enumeration}
     */
    @Override
    public Enumeration<?> getHeaders(final String name) {
        return makeEnumeration(headers.get(name.toLowerCase(Locale.ENGLISH)));
    }

    /**
     * Gets the header names contained in this servlet request.
     *
     * @return The header names as an {@link Enumeration}
     */
    @Override
    public Enumeration<?> getHeaderNames() {
        return makeEnumeration(headers.keySet().iterator());
    }

    @Override
    public String[] getParameterValues(final String name) {
        final List<String> list = parameters.get(name);
        if (null == list || list.isEmpty()) {
            return null;
        }
        return list.toArray(new String[list.size()]);
    }

    @Override
    public String getParameter(final String name) {
        final List<String> values = parameters.get(name);
        return null == values || values.isEmpty() ? null : values.get(0);
    }

    @Override
    public Enumeration<?> getParameterNames() {
        return makeEnumeration(parameters.keySet().iterator());
    }

    @Override
    public Map<?, ?> getParameterMap() {
        final Map<String, String[]> retval = new HashKeyMap<String[]>(parameters.size()).setGenerator(hashKeyGenerator);
        for (final Entry<String, List<String>> entry : parameters.entrySet()) {
            final List<String> values = entry.getValue();
            retval.put(entry.getKey(), values.toArray(new String[values.size()]));
        }
        return retval;
    }

    @Override
    public Object getAttribute(final String name) {
        return attributes.get(name);
    }

    /**
     * Checks if this servlet request contains specified attribute.
     *
     * @param name The attribute name
     * @return <code>true</code> if this servlet request contains specified attribute; otherwise <code>false</code>
     */
    public boolean containsAttribute(final String name) {
        return attributes.containsKey(name);
    }

    @Override
    public Enumeration<?> getAttributeNames() {
        return makeEnumeration(attributes.keySet().iterator());
    }

    @Override
    public void removeAttribute(final String name) {
        attributes.remove(name);
    }

    @Override
    public void setAttribute(final String name, final Object value) {
        if (value != null) {
            attributes.put(name, value);
        }
    }

    @Override
    public String getRealPath(final String string) {
        return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(final String string) {
        return null;
    }

    @Override
    public void setCharacterEncoding(final String characterEncoding) throws UnsupportedEncodingException {
        String charset = characterEncoding;
        if (null == charset) {
            charset = "ISO-8859-1";
            return;
        }
        final int mlen;
        if (charset.charAt(0) == '"' && charset.charAt((mlen = charset.length() - 1)) == '"') {
            charset = charset.substring(1, mlen);
        }
        try {
            if (!Charset.isSupported(charset)) {
                throw new UnsupportedEncodingException(charset);
            }
        } catch (final java.nio.charset.IllegalCharsetNameException e) {
            final UnsupportedEncodingException uee = new UnsupportedEncodingException(charset);
            uee.initCause(e);
            throw uee;
        }
        this.characterEncoding = charset;
    }

    /**
     * Sets the protocol. The name and version of the protocol the request uses in the form <i>protocol/majorVersion.minorVersion</i>, for
     * example, HTTP/1.1.
     *
     * @param protocol The protocol to set
     */
    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public Enumeration<?> getLocales() {
        return null;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (servletInputStream == null) {
            throw new IOException("no ServletInputStream found!");
        }
        return servletInputStream;
    }

    @Override
    public String getContentType() {
        return getHeader(CONTENT_TYPE);
    }

    @Override
    public int getContentLength() {
        return (int) contentLength;
    }

    /**
     * Gets the content length's <code>long</code> value.
     *
     * @return The content length's <code>long</code> value
     */
    public long getContentLengthLong() {
        return contentLength;
    }

    @Override
    public String getCharacterEncoding() {
        /*
         * if (characterEncoding == null) { // CHARACTER ENCODING MUST NOT BE NULL characterEncoding =
         * ServerConfig.getProperty(Property.DefaultEncoding); }
         */
        return characterEncoding;
    }

    @Override
    public BufferedReader getReader() {
        return null;
    }

    /**
     * Sets the remote address of this request.
     *
     * @param remoteAddr The remote address; either a machine name, such as "java.sun.com", or a textual representation of an IP address
     */
    public void setRemoteAddr(final String remoteAddr) {
        this.remoteAddress = remoteAddr;
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddress;
    }

    /**
     * Sets the remote host; the fully qualified name of the client or the last proxy that sent the request.
     *
     * @param remoteHost The remote host denoting the fully qualified name of the client
     */
    public void setRemoteHost(final String remoteHost) {
        this.remoteHost = remoteHost;
    }

    @Override
    public String getRemoteHost() {
        return remoteHost;
    }

    @Override
    public String getScheme() {
        if (scheme == null) {
            if (protocol == null) {
                return null;
            }
            /*
             * Determine scheme from protocol (in the form protocol/majorVersion.minorVersion) and isSecure information
             */
            scheme =
                new com.openexchange.java.StringAllocator(protocol.substring(0, protocol.indexOf('/')).toLowerCase(Locale.ENGLISH)).append(secure ? "s" : "").toString();
        }
        return scheme;
    }

    /**
     * Sets the scheme
     *
     * @param scheme The scheme to set
     */
    public void setScheme(final String scheme) {
        this.scheme = scheme;
    }

    /**
     * Sets the host name of the server to which the request was sent.
     *
     * @param serverName The host name of the server to which the request was sent
     */
    public void setServerName(final String serverName) {
        this.serverName = serverName;
    }

    @Override
    public String getServerName() {
        final String host = getFromHost();

        return (host == null) ? serverName : host;
    }

    private String getFromHost() {
        final String header = getHeader(HOST);
        if (header == null) {
            return null;
        }
        final int colonPos = header.indexOf(':');
        if (colonPos == -1) {
            return header;
        }
        return header.substring(0, colonPos);
    }

    /**
     * Sets the port number to which the request was sent.
     *
     * @param serverPort The server port
     */
    public void setServerPort(final int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Sets whether this request was made using a secure channel, such as HTTPS.
     *
     * @param secure <code>true</code> if this request uses a secure channel; otherwise <code>false</code>
     */
    public void setSecure(final boolean secure) {
        this.secure = secure;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    /**
     * Gets the first {@link String} element contained in given array or <code>null</code> if array is <code>null</code> or empty.
     *
     * @param values The array
     * @return The first {@link String} element or <code>null</code>
     */
    protected static String makeString(final List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    private static final Enumeration EMPTY_ENUM = new Enumeration() {

        @Override
        public boolean hasMoreElements() {
            return false;
        }

        @Override
        public Object nextElement() {
            return null;
        }

    };

    /**
     * Creates a new {@link Enumeration} for specified array.
     *
     * @param <T> The list's element type
     * @param list The list
     * @return A new {@link Enumeration}
     */
    protected static <T> Enumeration<T> makeEnumeration(final List<T> list) {
        if (null == list) {
            return EMPTY_ENUM;
        }
        return (new Enumeration<T>() {

            private final int size = list.size();

            private int cursor;

            @Override
            public boolean hasMoreElements() {
                return (cursor < size);
            }

            @Override
            public T nextElement() {
                return list.get(cursor++);
            }
        });
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * Sets the remote port
     *
     * @param remotePort The remote port to set
     */
    public void setRemotePort(final int remotePort) {
        this.remotePort = remotePort;
    }

    @Override
    public String getLocalName() {
        return localName;
    }

    /**
     * Sets the local name
     *
     * @param localName The local name to set
     */
    public void setLocalName(final String localName) {
        this.localName = localName;
    }

    @Override
    public String getLocalAddr() {
        return localAddr;
    }

    /**
     * Sets the local address
     *
     * @param localAddr The local address to set
     */
    public void setLocalAddr(final String localAddr) {
        this.localAddr = localAddr;
    }

    @Override
    public int getLocalPort() {
        return localPort;
    }

    /**
     * Sets the local port
     *
     * @param localPort The local port to set
     */
    public void setLocalPort(final int localPort) {
        this.localPort = localPort;
    }

    protected static <T> Enumeration<T> makeEnumeration(final Iterator<T> iter) {
        return new Enumeration<T>() {

            @Override
            public boolean hasMoreElements() {
                return iter.hasNext();
            }

            @Override
            public T nextElement() {
                return iter.next();
            }
        };
    }

    @Override
    public String getAuthType() {
        return authType;
    }

    public void setCookies(final Cookie[] cookies) {
        this.cookies = new Cookie[cookies.length];
        System.arraycopy(cookies, 0, this.cookies, 0, cookies.length);
    }

    @Override
    public Cookie[] getCookies() {
        if (cookies == null) {
            return null;
        }
        final Cookie[] retval = new Cookie[cookies.length];
        System.arraycopy(cookies, 0, retval, 0, cookies.length);
        return retval;
    }

    @Override
    public long getDateHeader(final String name) {
        return containsHeader(name) ? getDateValueFromHeaderField(getHeader(name)) : -1;
    }

    @Override
    public int getIntHeader(final String name) {
        return containsHeader(name) ? Integer.parseInt(getHeader(name)) : -1;
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    @Override
    public String getMethod() {
        return method;
    }

    public void setPathInfo(final String pathInfo) {
        if (null == pathInfo) {
            this.pathInfo = null;
        } else {
            // Ensure starting slash '/' character
            this.pathInfo = pathInfo.length() > 0 && '/' != pathInfo.charAt(0) ? '/' + pathInfo : pathInfo;
        }
    }

    @Override
    public String getPathInfo() {
        return pathInfo;
    }

    /**
     * Gets the instance identifier aka JVM route
     *
     * @return The instance identifier aka JVM route
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Sets the instance identifier aka JVM route
     *
     * @param The instance identifier
     */
    public void setInstanceId(final String instanceId) {
        this.instanceId = instanceId;
    }

    public void setPathTranslated(final String path_translated) {
        pathTranslated = path_translated;
    }

    @Override
    public String getPathTranslated() {
        return pathTranslated;
    }

    public void setContextPath(final String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    public void setQueryString(final String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    public void setRemoteUser(final String remoteUser) {
        this.remoteUser = remoteUser;
    }

    @Override
    public String getRemoteUser() {
        return remoteUser;
    }

    @Override
    public boolean isUserInRole(final String role) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Method isUserInRole() is not implemented in HttpServletRequestWrapper, yet!");
        }
        return false;
    }

    @Override
    public java.security.Principal getUserPrincipal() {
        return userPrincipal;
    }

    public void setUserPrincipal(final Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    @Override
    public String getRequestedSessionId() {
        // TODO:
        return null;
    }

    public void setRequestURI(final String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    /**
     * Default port for HTTP.
     */
    private static final int PORT_HTTP = 80;

    /**
     * Default port for HTTPS.
     */
    private static final int PORT_HTTPS = 443;

    @Override
    public StringBuffer getRequestURL() {
        if (null == requestURL) {
            final StringBuilder tmp = new StringBuilder(256);
            if (isSecure()) {
                tmp.append("https://").append(getServerName());
                final int port = getServerPort();
                if (port != PORT_HTTPS) {
                    tmp.append(':').append(port);
                }
            } else {
                tmp.append("http://").append(getServerName());
                final int port = getServerPort();
                if (port != PORT_HTTP) {
                    tmp.append(':').append(port);
                }
            }
            /*
             * Append request URI
             */
            if (null != requestURI) {
                if (requestURI.charAt(0) != '/') {
                    tmp.append('/');
                }
                tmp.append(requestURI);
            }
            requestURL = tmp.toString();
        }
        return new StringBuffer(requestURL);
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }

    public void setServletPath(final String servletPath) {
        this.servletPath = servletPath;
    }

    @Override
    public HttpSession getSession(final boolean create) {
        HttpSessionWrapper session = null;
        /*
         * First look-up HttpSessionManagement if a session already exists
         */
        final AjpProcessor ajpProcessor = response.getAjpProcessor();
        final Cookie jsessionIdCookie = ajpProcessor.getHttpSessionCookie();
        final String httpSessionId = jsessionIdCookie.getValue();
        final HttpSessionWrapper httpSession = HttpSessionManagement.getHttpSession(httpSessionId);
        if (httpSession != null) {
            if (!HttpSessionManagement.isHttpSessionExpired(httpSession)) {
                session = httpSession;
                session.setNew(false);
                session.setServletContext(getServletContext());
                /*
                 * TODO: Add JSESSIONID cookie; reset max-age?
                 */
                // configureCookie(sessionCookie);
                // response.addCookie(sessionCookie);
                return session.touch();
            }
            /*
             * Invalidate session
             */
            httpSession.invalidate();
            HttpSessionManagement.removeHttpSession(httpSessionId);
        }
        /*
         * Check whether to create a new session
         */
        if (create) {
            session = ((HttpSessionWrapper) HttpSessionManagement.createAndGetHttpSession(httpSessionId));
            session.setNew(true);
            session.setServletContext(getServletContext());
            /*
             * Add JSESSIONID cookie
             */
            configureCookie(jsessionIdCookie);
            response.addCookie(jsessionIdCookie);
        }
        return session;
    }

    private static volatile Boolean autologin;

    private static boolean autologin() {
        Boolean tmp = autologin;
        if (null == tmp) {
            synchronized (HttpServletResponseImpl.class) {
                tmp = autologin;
                if (null == tmp) {
                    final ConfigurationService configurationService = Services.getService(ConfigurationService.class);
                    tmp = Boolean.valueOf(null != configurationService && configurationService.getBoolProperty("com.openexchange.sessiond.autologin", false));
                    autologin = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    private static volatile Integer maxAge;

    private static int maxAge() {
        Integer tmp = maxAge;
        if (null == tmp) {
            synchronized (HttpServletResponseImpl.class) {
                tmp = maxAge;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    tmp = Integer.valueOf(ConfigTools.parseTimespanSecs(null == service ?  "1W" : service.getProperty("com.openexchange.cookie.ttl", "1W")));
                    maxAge = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    public static void configureCookie(final Cookie jsessionIdCookie) {
        /*
         * Check if auto-login is enabled
         */
        if (autologin()) {
            final int maxAge = maxAge();
            jsessionIdCookie.setMaxAge(maxAge);
        } else {
            jsessionIdCookie.setMaxAge(-1); // cookies auto-expire
        }
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return !HttpSessionManagement.isHttpSessionExpired(getSession());
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return requestedSessionIdFromCookie;
    }

    public void setRequestedSessionIdFromCookie(final boolean requestedSessionIdFromCookie) {
        this.requestedSessionIdFromCookie = requestedSessionIdFromCookie;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return requestedSessionIdFromURL;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return requestedSessionIdFromURL;
    }

    public void setRequestedSessionIdFromURL(final boolean requestedSessionIdFromURL) {
        this.requestedSessionIdFromURL = requestedSessionIdFromURL;
    }

    public void setAuthType(final String authType) {
        this.authType = authType;
    }

    public void setServletInstance(final HttpServlet servletInstance) {
        this.servletInstance = servletInstance;
    }

    private static final long getDateValueFromHeaderField(final String headerValue) {
        try {
            return Tools.parseHeaderDate(headerValue).getTime();
        } catch (final ParseException e) {
            LOG.error(e.getMessage(), e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private final ServletContext getServletContext() {
        return ServletConfigLoader.getDefaultInstance().getContext(servletInstance.getClass().getCanonicalName(), servletPath);
    }

    /**
     * Sets the start time.
     *
     * @param startTime The start time
     */
    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the start time for this request.
     *
     * @return The start time
     */
    public long getStartTime() {
        return startTime;
    }

}
