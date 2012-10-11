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

package com.openexchange.ajp13.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13ServletInputStream;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.exception.AJPv13Exception.AJPCode;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.java.DefaultHashKeyGenerator;
import com.openexchange.java.HashKeyGenerator;
import com.openexchange.java.HashKeyMap;
import com.openexchange.mail.mime.ContentType;

/**
 * {@link ServletRequestWrapper}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ServletRequestWrapper implements ServletRequest {

    /**
     * The name of the "Content-Type" header.
     */
    public static final String CONTENT_TYPE = "content-type";

    /**
     * The name of the "Content-Length" header.
     */
    public static final String CONTENT_LENGTH = "content-length";

    /**
     * A set for known single-value headers. Those headers which occur only once in HTTP headers.
     */
    private static final Set<String> SINGLE_VALUE_HEADERS = new HashSet<String>(Arrays.asList(CONTENT_TYPE, CONTENT_LENGTH));

    private static final String HOST = "Host";

    private static final SecureRandom RANDOM = new SecureRandom();

    /*-
     * ------------------- Member stuff ---------------------
     */

    private final Map<String, Object> attributes;

    private final Map<String, String[]> parameters;

    protected final Map<String, String[]> headers;

    private String characterEncoding;

    private String protocol;

    private String remoteAddress;

    private String remoteHost;

    private String serverName;

    private String scheme;

    private int serverPort;

    private boolean secure;

    private AJPv13ServletInputStream servletInputStream;

    private final int max;

    /**
     * Initializes a new {@link ServletRequestWrapper}.
     *
     * @throws AJPv13Exception If instantiation fails
     */
    public ServletRequestWrapper() throws AJPv13Exception {
        super();
        max = AJPv13Config.getMaxRequestParameterCount();
        protocol = "HTTP/1.1";
        final String salt = Integer.toString(RANDOM.nextInt(), 10); // request-specific salt
        final HashKeyGenerator hashKeyGenerator = new DefaultHashKeyGenerator(salt);
        attributes = new HashMap<String, Object>(32);
        parameters = new HashKeyMap<String[]>(max > 0 ? max : 64).setGenerator(hashKeyGenerator);
        headers = new HashMap<String, String[]>(16);
        setHeaderInternal(CONTENT_LENGTH, String.valueOf(-1), false);
    }

    /**
     * Sets the <code>Content-Length</code> header.
     *
     * @param contentLength The content length
     * @throws AJPv13Exception If setting <code>Content-Length</code> header fails
     */
    public void setContentLength(final int contentLength) throws AJPv13Exception {
        setHeaderInternal(CONTENT_LENGTH, String.valueOf(contentLength), false);
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
        final String[] values = parameters.get(name);
        final String[] newValues;
        if (null == values) {
            if (max > 0 && parameters.size() >= max) {
                throw new IllegalStateException("Max. allowed number of request parameters ("+max+") exceeded");
            }
            newValues = new String[] { value };
        } else {
            final int len = values.length;
            newValues = new String[len + 1];
            System.arraycopy(values, 0, newValues, 0, len);
            newValues[len] = value;
        }
        parameters.put(name, newValues);
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
        final String[] prevValues = headers.get(name);
        if (null == prevValues || SINGLE_VALUE_HEADERS.contains(name)) {
            headers.put(name, new String[] { value });
        } else {
            /*
             * Header may carry multiple values
             */
            final String[] newValues = new String[prevValues.length + 1];
            System.arraycopy(prevValues, 0, newValues, 0, prevValues.length);
            newValues[newValues.length - 1] = value;
            headers.put(name, newValues);
        }
    }

    private final void handleContentType(final String value) throws AJPv13Exception {
        if (value != null && value.length() > 0) {
            final ContentType ct;
            try {
                ct = new ContentType(value);
            } catch (final OXException e) {
                com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ServletRequestWrapper.class)).error(e.getMessage(), e);
                throw new AJPv13Exception(AJPCode.INVALID_CONTENT_TYPE, true, e, value);
            }
            if (ct.containsCharsetParameter()) {
                try {
                    setCharacterEncoding(ct.getCharsetParameter());
                } catch (final UnsupportedEncodingException e) {
                    throw new AJPv13Exception(AJPCode.UNSUPPORTED_ENCODING, true, e, ct.getCharsetParameter());
                }
            } else {
                /*
                 * Although http defines to use charset "ISO-8859-1" if protocol is set to "HTTP/1.1", we use a pre-defined charset given
                 * through config file
                 */
                try {
                    setCharacterEncoding(ServerConfig.getProperty(Property.DefaultEncoding));
                } catch (final UnsupportedEncodingException e) {
                    throw new AJPv13Exception(AJPCode.UNSUPPORTED_ENCODING, true, e, ServerConfig.getProperty(Property.DefaultEncoding));
                }
            }
        } else {
            /*
             * Although http defines to use charset "ISO-8859-1" if protocol is set to "HTTP/1.1", we use a pre-defined charset given
             * through config file
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
    public String getHeader(final String name) {
        final String n = name.toLowerCase(Locale.ENGLISH);
        return makeString(headers.get(n));
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
    public Enumeration<String> getHeaders(final String name) {
        return makeEnumeration(headers.get(name.toLowerCase(Locale.ENGLISH)));
    }

    /**
     * Gets the header names contained in this servlet request.
     *
     * @return The header names as an {@link Enumeration}
     */
    public Enumeration<String> getHeaderNames() {
        return makeEnumeration(headers.keySet().iterator());
    }

    /**
     * Sets a parameter's values.
     *
     * @param name The parameter name to which the values shall be bound
     * @param values The parameter values
     */
    public void setParameterValues(final String name, final String[] values) {
        parameters.put(name, values);
    }

    @Override
    public String[] getParameterValues(final String name) {
        return clone(parameters.get(name));
    }

    @Override
    public String getParameter(final String name) {
        final String[] values = parameters.get(name);
        return null == values ? null : values[0];
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return makeEnumeration(parameters.keySet().iterator());
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
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
    public Enumeration<String> getAttributeNames() {
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
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    /**
     * Sets the servlet input stream of this servlet request.
     *
     * @param is The servlet input stream
     */
    public void setInputStream(final AJPv13ServletInputStream is) {
        servletInputStream = is;
    }

    /**
     * Sets/appends new data to this servlet request's input stream.
     *
     * @param newData The new data to set/append
     * @throws IOException If an I/O error occurs
     */
    public void setData(final byte[] newData) throws IOException {
        servletInputStream.setData(newData);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (servletInputStream == null) {
            throw new IOException("no ServletInputStream found!");
        }
        return servletInputStream;
    }

    /**
     * Removes the servlet request's input stream.
     */
    public void removeInputStream() {
        servletInputStream = null;
    }

    @Override
    public String getContentType() {
        return getHeader(CONTENT_TYPE);
    }

    @Override
    public int getContentLength() {
        return Integer.parseInt(getHeader(CONTENT_LENGTH));
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
                new StringBuilder(protocol.substring(0, protocol.indexOf('/')).toLowerCase(Locale.ENGLISH)).append(secure ? "s" : "").toString();
        }
        return scheme;
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
        if(header == null) {
            return null;
        }
        final int colonPos = header.indexOf(':');
        if(colonPos == -1) {
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
    protected static String makeString(final String[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    /**
     * Creates a new {@link Enumeration} for specified array.
     *
     * @param <T> The array's element type
     * @param array The array
     * @return A new {@link Enumeration}
     */
    protected static <T> Enumeration<T> makeEnumeration(final T[] array) {
        return (new Enumeration<T>() {

            private final int size = array.length;

            private int cursor;

            @Override
            public boolean hasMoreElements() {
                return (cursor < size);
            }

            @Override
            public T nextElement() {
                return array[cursor++];
            }
        });
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
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

    private static String[] clone(final String[] src) {
        if (null == src) {
            return null;
        }
        final int len = src.length;
        final String[] clone = new String[len];
        System.arraycopy(src, 0, clone, 0, len);
        return clone;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

}
